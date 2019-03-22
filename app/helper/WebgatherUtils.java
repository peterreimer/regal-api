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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.CharMatcher;

import actions.Create;
import actions.Modify;
import actions.RegalAction;
import helper.mail.Mail;
import models.Gatherconf;
import models.Globals;
import models.Message;
import models.Node;
import play.Logger;
import play.Play;

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
				new Mail().sendMail(mailMsg,
						"Die Website " + siteName + " ist umgezogen ! ");
			} catch (Exception e) {
				throw new RuntimeException("Email could not be sent successfully!");
			}
		} catch (Exception e) {
			WebgatherLogger.warn(e.toString());
		}
	}

}
