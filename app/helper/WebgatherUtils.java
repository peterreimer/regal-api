/*
 * Copyright 2017 hbz NRW (http://www.hbz-nrw.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package helper;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.CharMatcher;

import actions.Create;
import actions.Modify;
import actions.Modify.UpdateNodeException;
import actions.RegalAction;
import archive.fedora.CopyUtils;
import helper.mail.Mail;
import models.Gatherconf;
import models.Gatherconf.CrawlerSelection;
import models.Globals;
import models.Message;
import models.Node;
import play.Logger;
import play.Play;
import com.yourmediashelf.fedora.client.request.GetDatastream;
import com.yourmediashelf.fedora.client.request.GetDatastreamDissemination;
import com.yourmediashelf.fedora.client.response.FedoraResponse;
import com.yourmediashelf.fedora.client.response.GetDatastreamResponse;

/**
 * Eine Klasse mit nützlichen Methoden im Umfeld des Webgatherings
 * 
 * @author I. Kuss, hbz
 */
public class WebgatherUtils {

	private static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");
	/** Datumsformat für String-Repräsentation von Datümern */
	public static final DateFormat dateFormat =
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");

	/**
	 * Eine Methode zum Validieren und Umwandeln einer URL. Die URL wird nach
	 * ASCII konvertiert, falls sie noch nicht in dieser Kodierung ist. Wahlweise
	 * wird vorher nach Punycode konvertiert. Das Schema kann wahlweise erhalten
	 * (falls vorhanden) oder entfernt werden. Bei ungültigen URL wird eine
	 * URISyntaxException geschmissen.
	 * 
	 * von hier kopiert:
	 * https://nealvs.wordpress.com/2016/01/18/how-to-convert-unicode-url-to-ascii
	 * -in-java/
	 * 
	 * @param url ein Uniform Resource Locator als Zeichenkette
	 * @return eine URL als Zeichenkette
	 * @throws URISyntaxException eine Ausnahme, wenn die URL ungültig ist
	 */
	public static String convertUnicodeURLToAscii(String url) {
		try {
			URL u = new URL(url);
			URI uri =
					new URI(u.getProtocol(), u.getUserInfo(), IDN.toASCII(u.getHost()),
							u.getPort(), u.getPath(), u.getQuery(), u.getRef());
			String correctEncodedURL = uri.toASCIIString();
			return correctEncodedURL;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Erzeugt eine Nachricht für den Fall, dass eine URL umgezogen ist.
	 * 
	 * @param conf die Crawler-Settings (Gatherconf)
	 * @return die Nachricht
	 */
	public static Message createInvalidUrlMessage(Gatherconf conf) {
		Message msg = null;
		if (conf.getUrlNew() == null) {
			msg = new Message("Die Website ist unbekannt verzogen.\n"
					+ "Bitte geben Sie auf dem Tab \"Crawler settings\" eine neue, gültige URL ein. Solange wird die Website nicht erneut eingesammelt.");
		} else {
			msg = new Message("Die Website ist umgezogen nach " + conf.getUrlNew()
					+ ".\n"
					+ "Bitte bestätigen Sie den Umzug auf dem Tab \"Crawler settings\" (URL kann dort vorher editiert werden).");
		}
		return msg;
	}

	/**
	 * Schickt E-Mail mit einer Umzugsnotiz und Aufforderung, die neue URL zu
	 * bestätigen.
	 * 
	 * @param node der Knoten der Website
	 * @param conf die Gatherconf der umgezogenen Website
	 */
	public static void sendInvalidUrlEmail(Node node, Gatherconf conf) {
		WebgatherLogger.info("Schicke E-Mail mit Umzugsnotiz.");
		try {

			String siteName =
					conf.getName() == null ? node.getAggregationUri() : conf.getName();
			String mailMsg = "Die Website " + siteName + " ist ";
			if (conf.getUrlNew() == null) {
				mailMsg += "unbekannt verzogen.\n"
						+ "Bitte geben Sie auf diesem Webformular eine neue, gültige URL ein. Solange wird die Website nicht erneut eingesammelt: ";
			} else {
				mailMsg += "umgezogen.\n"
						+ "Bitte bestätigen Sie den Umzug auf diesem Webformular (URL kann dort vorher editiert werden): ";
			}
			mailMsg += Globals.urnbase + node.getAggregationUri() + "/crawler .";

			try {
				Mail.sendMail(mailMsg, "Die Website " + siteName + " ist umgezogen ! ");
			} catch (Exception e) {
				throw new RuntimeException("Email could not be sent successfully!");
			}
		} catch (Exception e) {
			WebgatherLogger.warn(e.toString());
		}
	}

	/**
	 * Veröffentlicht eine Webpage-Version (=Webschnitt), indem es sie in der
	 * Openwayback-Kollektion "weltweit" anlegt.
	 * 
	 * @param node der Knoten des Webschnittes
	 * @throws RuntimeException Ausnahme aufgetreten
	 */
	public static void publishWebpageVersion(Node node) throws RuntimeException {
		WebgatherLogger.info("Jetzt wird ein Webschnitt veröffentlicht.");
		try {
			getConfFromFedora(node.getPid(), node);
			Gatherconf conf = Gatherconf.create(node.getConf());
			WebgatherLogger.debug("conf=" + conf.toString());
			createSoftlinkInPublicData(node, conf);
			setOpenwaybackLinkToPublicAccessPoint(node, conf);
		} catch (Exception e) {
			WebgatherLogger.error("Webpage Version " + node.getPid()
					+ " kann nicht veröffentlicht werden!");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Ändert den Openwayback-Link in der Gatherconf des Webschnittes, so dass er
	 * auf den öffentlichen Zugriffspunkt zeigt. Tut er dies schon, wird nichts
	 * gemacht.
	 * 
	 * @param node der Knoten des Webschnitts
	 * @param conf die Gatherconf, Konfigurationsdatei für den Crawler, die für
	 *          diesen Webschnitt maßgeblich war
	 */
	private static void setOpenwaybackLinkToPublicAccessPoint(Node node,
			Gatherconf conf) {
		try {
			String openWaybackLink = conf.getOpenWaybackLink();
			WebgatherLogger.debug("openWaybackLink=" + openWaybackLink);
			play.Logger.debug("openWaybackLink=" + openWaybackLink);
			String publicOpenWaybackLink =
					openWaybackLink.replace("/wayback/", "/weltweit/");
			publicOpenWaybackLink =
					publicOpenWaybackLink.replace("/lesesaal/", "/weltweit/");
			conf.setOpenWaybackLink(publicOpenWaybackLink);
			play.Logger.debug("publicOpenWaybackLink=" + publicOpenWaybackLink);
			String msg = new Modify().updateConf(node, conf.toString());
			WebgatherLogger.info(
					"Openwayback-Link wurde auf \"weltweit\" gesetzt für Webschnitt "
							+ node.getPid() + ". Modify-Message: " + msg);
		} catch (UpdateNodeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			WebgatherLogger.error("Openwayback-Link für Webschnitt " + node.getPid()
					+ " kann nicht auf \"öffentlich\" gesetzt werden!");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Ermittelt Speicherort der Webarchiv-Datei (WARC) und legt einen Softlink
	 * unterhalb von public-data/ an, der auf diese Webarchiv-Datei zeigt. Dadurch
	 * wird das Webarchiv zur Openwayback-Kollektion "weltweit" hinzugefügt und
	 * subsequent im Openwayback-Zugriffspunkt "weltweit" indexiert, also
	 * veröffentlicht.
	 *
	 * @param node Der Knoten des Webschnittes
	 * @param conf Die Konfigurationsdatei für das Webcrawling (Gatherconf), die
	 *          diesem Webschnitt zugrunde gelegt wurde.
	 */
	private static void createSoftlinkInPublicData(Node node, Gatherconf conf) {
		try {
			String localDir = conf.getLocalDir();
			WebgatherLogger.debug("localDir=" + localDir);
			String jobDir = Play.application().configuration()
					.getString("regal-api." + conf.getCrawlerSelection() + ".jobDir");
			WebgatherLogger.debug("jobDir=" + jobDir);
			if (!localDir.startsWith(jobDir)) {
				throw new RuntimeException("Crawl-Verzeichnis " + localDir
						+ " beginnt nicht mit " + conf.getCrawlerSelection()
						+ "-jobDir für PID " + node.getPid());
			}
			if (conf.getCrawlerSelection().equals(CrawlerSelection.heritrix)) {
				// zusätzliches Unterverzeichnis für Heritrix-Crawls
				localDir = localDir.concat("/warcs");
			}
			String subDir = localDir.substring(jobDir.length() + 1);
			WebgatherLogger.debug("Unterverzeichnis für Webcrawl: " + subDir);
			File publicCrawlDir = createPublicDataSubDir(subDir);
			getDataFromFedora(node, localDir);
			String DSLocation = node.getUploadFile();
			WebgatherLogger.debug("uploadFile=" + DSLocation);
			File uploadFile = new File(DSLocation);
			chkCreateSoftlink(publicCrawlDir.getPath(), localDir,
					uploadFile.getName());
		} catch (Exception e) {
			WebgatherLogger.error("Softlink für Webschnitt " + node.getPid()
					+ " konnte unterhalb des Verzeichnisses public-data/ nicht angelegt werden!");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Erzeugt eine weiche Verknüpfung (Softlink) in einem Unterverzeichnis von
	 * public-data/ auf eine eingesammelte WARC-Datei in einem der
	 * Crawler-Verzeichnisse. Macht nichts, falls diese weiche Verknüpfung schon
	 * existiert.
	 * 
	 * @param publicCrawlDir Das Verzeichnis public-data/ mit Unterverzeichnissen,
	 *          in denen der Softlink angelegt werden soll.
	 * @param localDir Das Verzeichnis, in dem die WARC-Datei wirklich liegt.
	 * @param uploadFile Der Dateiname der WARC-Datei (ohne Pfadangaben).
	 */
	private static void chkCreateSoftlink(String publicCrawlDir, String localDir,
			String uploadFile) {
		try {
			File softLink = new File(publicCrawlDir + "/" + uploadFile);
			if (softLink.exists()) {
				WebgatherLogger
						.info("Softlink " + softLink.getPath() + " gibt es schon.");
				return;
			}
			File localFile = new File(localDir + "/" + uploadFile);
			if (!localFile.exists()) {
				throw new RuntimeException("Lokale Webarchiv-Datei "
						+ localFile.getPath() + " existiert nicht!");
			}
			Files.createSymbolicLink(softLink.toPath(), localFile.toPath());
			WebgatherLogger.info("Symbolischer Link " + softLink.getPath()
					+ " wurde erfolgreich angelegt.");
		} catch (Exception e) {
			WebgatherLogger.error("Kann Softlink " + uploadFile + " in Verzeichnis "
					+ publicCrawlDir + " nicht anlegen!");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Legt eine Verzeichnisstruktur unterhalb von public-data/ an, falls diese
	 * noch nicht vorhanden ist.
	 * 
	 * @param subDir die Verzeichnisstruktur unterhalb von public-data/.
	 *          Unterverzeichnisse sind durch "/" getrennt.
	 */
	private static File createPublicDataSubDir(String subDir) {
		try {
			String publicJobDir = Play.application().configuration()
					.getString("regal-api.public.jobDir");
			File publicCrawlDir = new File(publicJobDir + "/" + subDir);
			if (!publicCrawlDir.exists()) {
				publicCrawlDir.mkdirs();
			}
			return publicCrawlDir;
		} catch (Exception e) {
			WebgatherLogger.error("Kann Verzeichnisstruktur " + subDir
					+ " unterhalb von public-data/ nicht anlegen!");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Lies einen Datenstrom "conf" (Konfigurationsdatei für das Webgathering) aus
	 * der Fedora
	 * 
	 * @param pid
	 * @param node
	 * @throws Exception
	 */
	private static void getConfFromFedora(String pid, Node node)
			throws RuntimeException {
		try {
			FedoraResponse response =
					new GetDatastreamDissemination(pid, "conf").execute();
			node.setConf(
					CopyUtils.copyToString(response.getEntityInputStream(), "utf-8"));
		} catch (Exception e) {
			WebgatherLogger.warn("Datenstrom \"conf\" konnte nicht gelesen werden!");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Lies einen Datenstrom "data" (die WARC-Datei beschreibend) aus der Fedora
	 * 
	 * @param pid
	 * @param node
	 * @throws Exception
	 */
	private static void getDataFromFedora(Node node, String localDir)
			throws RuntimeException {
		try {
			GetDatastreamResponse response =
					new GetDatastream(node.getPid(), "data").execute();
			String dsLocation = response.getDatastreamProfile().getDsLocation();
			WebgatherLogger.debug("datastream location: " + dsLocation);
			// Bastle upload file zusammen
			File fileLocation = new File(dsLocation);
			String uploadFile = localDir + "/" + fileLocation.getName();
			WebgatherLogger.info("Upload File wird gesetzt auf: " + uploadFile);
			node.setUploadFile(uploadFile);
			// Hier könnte man noch weitere Felder in den Node übernehmen
		} catch (Exception e) {
			WebgatherLogger.warn(
					"Datenstrom \"data\" konnte nicht gelesen werden oder enthält ungültige Werte !");
			throw new RuntimeException(e);
		}
	}

}
