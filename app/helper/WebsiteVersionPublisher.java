/*
 * Copyright 2019 hbz NRW (http://www.hbz-nrw.de/)
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

import java.io.File;
import java.nio.file.Files;

import actions.Modify;
import actions.Modify.UpdateNodeException;
import archive.fedora.CopyUtils;
import models.Gatherconf;
import models.Gatherconf.CrawlerSelection;
import models.Node;
import play.Logger;
import play.Play;
import com.yourmediashelf.fedora.client.request.GetDatastream;
import com.yourmediashelf.fedora.client.request.GetDatastreamDissemination;
import com.yourmediashelf.fedora.client.response.FedoraResponse;
import com.yourmediashelf.fedora.client.response.GetDatastreamResponse;

/**
 * Methoden zum Veröffentlichen von Websites (openWayback AccessPoint
 * "weltweit"). Methoden zum Zurückziehen veröffentlichter Websites (openWayback
 * AccessPoint "lesesaal" oder "wayback").
 * 
 * @author I. Kuss, hbz
 */
public class WebsiteVersionPublisher {

	private static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

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
	 * Zieht eine Webpage-Version (=Webschnitt) von der Veröffentlichung zurück,
	 * indem es sie in der Openwayback-Kollektion "weltweit" löscht.
	 * 
	 * @param node der Knoten des Webschnittes
	 * @throws RuntimeException Ausnahme aufgetreten
	 */
	public static void retreatWebpageVersion(Node node) throws RuntimeException {
		WebgatherLogger
				.info("Ein Webschnitt wird von der Veröffentlichung zurückgezogen.");
		try {
			getConfFromFedora(node.getPid(), node);
			Gatherconf conf = Gatherconf.create(node.getConf());
			WebgatherLogger.debug("conf=" + conf.toString());
			removeSoftlinkInPublicData(node, conf);
			setOpenwaybackLinkToRestrictedAccessPoint(node, conf);
		} catch (Exception e) {
			WebgatherLogger.error("Webpage Version " + node.getPid()
					+ " kann nicht von der Veröffentlichung zurück gezogen werden!");
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
	 * Ändert den Openwayback-Link in der Gatherconf des Webschnittes, so dass er
	 * auf den öffentlichen Zugriffspunkt zeigt. Tut er dies schon, wird nichts
	 * gemacht.
	 * 
	 * @param node der Knoten des Webschnitts
	 * @param conf die Gatherconf, Konfigurationsdatei für den Crawler, die für
	 *          diesen Webschnitt maßgeblich war
	 */
	private static void setOpenwaybackLinkToRestrictedAccessPoint(Node node,
			Gatherconf conf) {
		try {
			String openWaybackLink = conf.getOpenWaybackLink();
			WebgatherLogger.debug("openWaybackLink=" + openWaybackLink);
			play.Logger.debug("openWaybackLink=" + openWaybackLink);

			String restrictedAccessPoint = Play.application().configuration()
					.getString("regal-api.heritrix.openwaybackLink");
			if (openWaybackLink.startsWith(restrictedAccessPoint)) {
				WebgatherLogger
						.info("openWaybackLink steht schon auf beschränktem Zugriff.");
				return;
			}
			int startIndex = openWaybackLink.indexOf("weltweit/");
			if (startIndex < 0) {
				throw new RuntimeException(
						"Unbekannter Zugriffspunkt in OpenwaybackLink " + openWaybackLink
								+ " !");
			}
			String restrictedOpenWaybackLink =
					restrictedAccessPoint + openWaybackLink.substring(startIndex + 9);
			WebgatherLogger
					.info("Neuer Openwayback-Link: " + restrictedOpenWaybackLink);

			conf.setOpenWaybackLink(restrictedOpenWaybackLink);
			play.Logger
					.debug("restrictedOpenWaybackLink=" + restrictedOpenWaybackLink);
			String msg = new Modify().updateConf(node, conf.toString());
			WebgatherLogger.info(
					"Openwayback-Link wurde auf \"lesesaal|wayback\" gesetzt für Webschnitt "
							+ node.getPid() + ". Modify-Message: " + msg);
		} catch (UpdateNodeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			WebgatherLogger.error("Openwayback-Link für Webschnitt " + node.getPid()
					+ " kann nicht auf \"lesesaal|wayback\" gesetzt werden!");
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
	 * Löscht einen Softlink unterhalb von public-data/, der auf eine
	 * Webarchiv-Datei zeigt. Dadurch wird das Webarchiv in der
	 * Openwayback-Kollektion "weltweit" entfernt.
	 * 
	 * @param node Der Knoten des Webschnittes
	 * @param conf Die Konfigurationsdatei für das Webcrawling (Gatherconf), die
	 *          diesem Webschnitt zugrunde gelegt wurde.
	 */
	private static void removeSoftlinkInPublicData(Node node, Gatherconf conf) {
		String localDir = null;
		try {
			localDir = conf.getLocalDir();
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
			File publicCrawlDir = chkExistsPublicDataSubDir(subDir);
			getDataFromFedora(node, localDir);
			String DSLocation = node.getUploadFile();
			WebgatherLogger.debug("uploadFile=" + DSLocation);
			File uploadFile = new File(DSLocation);
			chkRemoveSoftlink(publicCrawlDir.getPath(), uploadFile.getName());
		} catch (Exception e) {
			WebgatherLogger.error("Softlink für Webschnitt " + node.getPid()
					+ " auf Verzeichnis " + localDir
					+ " konnte unterhalb von public-data/ nicht gelöscht werden!");
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
	 * Löscht einen Softlink auf eine eingesammelte WARC-Datei in einem
	 * Unterverzeichnis von public-data/. Macht nichts, falls der Softlink schon
	 * gelöscht ist.
	 * 
	 * @param publicCrawlDir Das Verzeichnis public-data/ mit Unterverzeichnissen,
	 *          in denen der Softlink gelöscht werden soll.
	 * @param uploadFile Der Dateiname der WARC-Datei (ohne Pfadangaben).
	 */
	private static void chkRemoveSoftlink(String publicCrawlDir,
			String uploadFile) {
		try {
			File softLink = new File(publicCrawlDir + "/" + uploadFile);
			if (!softLink.exists()) {
				WebgatherLogger.info(
						"Softlink " + softLink + " gibt es nicht bzw. ist schon gelöscht.");
				return;
			}
			Files.delete(softLink.toPath());
			WebgatherLogger
					.info("Softlink " + softLink.getPath() + " wurde entfernt.");
		} catch (Exception e) {
			WebgatherLogger.error("Kann Softlink " + uploadFile + " in Verzeichnis "
					+ publicCrawlDir + " nicht löschen !");
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
	 * Prüft die Existenz einer Verzeichnisstruktur unterhalb von public-data/
	 * 
	 * @param subDir die Verzeichnisstruktur unterhalb von public-data/.
	 *          Unterverzeichnisse sind durch "/" getrennt.
	 * @return das Unterverzeichnis (volle Pfadangabe) als Java-Klasse "File"
	 */
	private static File chkExistsPublicDataSubDir(String subDir) {
		try {
			String publicJobDir = Play.application().configuration()
					.getString("regal-api.public.jobDir");
			File publicCrawlDir = new File(publicJobDir + "/" + subDir);
			if (!publicCrawlDir.exists()) {
				throw new RuntimeException(
						"Datenverzeichnis " + publicCrawlDir.getPath() + "gibt es nicht !");
			}
			return publicCrawlDir;
		} catch (Exception e) {
			WebgatherLogger.error("Kann Verzeichnisstruktur " + subDir
					+ " unterhalb von public-data/ nicht überprüfen !");
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
