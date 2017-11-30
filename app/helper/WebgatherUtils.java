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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.CharMatcher;

import play.Logger;

/**
 * Eine Klasse mit nützlichen Methoden im Umfeld des Webgatherings
 * 
 * @author I. Kuss, hbz
 */
public class WebgatherUtils {

	private static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

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
	 * @param includeScheme das Schema wird mit zurückgegeben (J/N)
	 * @param convertToPunycode falls wahr, wird nach Punycode konvertiert (und
	 *          dann erst nach ASCII)
	 * @return eine URL als Zeichenkette
	 * @throws URISyntaxException eine Ausnahme, wenn die URL ungültig ist
	 */
	public static String validateURL(String url, boolean includeScheme,
			boolean convertToPunycode) throws URISyntaxException {
		if (url == null) {
			return url;
		}
		String urlRet = url;
		urlRet = url.trim();
		// Handle international domains by detecting non-ascii and converting them
		// to punycode
		boolean isAscii = CharMatcher.ASCII.matchesAllOf(urlRet);
		URI uri = new URI(urlRet);

		boolean hasScheme = true;
		// URI needs a scheme to work properly with authority parsing
		if (uri.getScheme() == null) {
			uri = new URI("http://" + urlRet);
			hasScheme = false;
		}

		String scheme = uri.getScheme() != null ? uri.getScheme() + "://" : null;
		/* authority includes domain and port */
		String authority =
				uri.getRawAuthority() != null ? uri.getRawAuthority() : "";
		// WebgatherLogger.debug("authority=" + authority);
		// Must convert domain to punycode separately from the path
		if (convertToPunycode) {
			authority = IDN.toASCII(authority);
		}
		String path = uri.getRawPath() != null ? uri.getRawPath() : "";
		String queryString =
				uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "";

		urlRet = ((hasScheme && includeScheme) ? scheme : "") + authority + path
				+ queryString;
		// WebgatherLogger.debug("urlRet=" + urlRet);

		// Convert path from unicode to ascii encoding
		if (!isAscii) {
			urlRet = new URI(urlRet).toASCIIString();
		}
		WebgatherLogger.debug("url validated = " + urlRet);

		return urlRet;
	}

	/**
	 * konvertiert eine URL nach ASCII und konvertiert nach Punycode. Das Schema
	 * wird beibehalten (falls vorhanden).
	 * 
	 * @param url
	 * @return
	 * @throws URISyntaxException
	 */
	public static String convertUnicodeURLToAscii(String url)
			throws URISyntaxException {
		return convertUnicodeURLToAscii(url, true);
	}

	/**
	 * konvertiert eine URL nach ASCII und konvertiert nach Punycode. Das Schema
	 * kann wahlweise entfernt werden.
	 * 
	 * @param url
	 * @param includeScheme
	 * @return
	 * @throws URISyntaxException
	 */
	public static String convertUnicodeURLToAscii(String url,
			boolean includeScheme) throws URISyntaxException {
		return validateURL(url, includeScheme, true);
	}

	/**
	 * Testet die HTTP-Verbindung einer URL; liest Response-Code und Header-Felder
	 * bei Anfrage an diese HTTP-Verbindung aus. Z.B. für Umzugsservice
	 * interessant.
	 * 
	 * @param url eine URL als Zeichenkette
	 */
	public static void getHttpUrlConnectionHeaderFields(String url) {
		try {
			HttpURLConnection httpConnection =
					(HttpURLConnection) new URL(url).openConnection();
			httpConnection.setRequestMethod("GET");
			int status = httpConnection.getResponseCode();
			WebgatherLogger.debug("HTTP Response Code: " + status);
			for (Entry<String, List<String>> header : httpConnection.getHeaderFields()
					.entrySet()) {
				WebgatherLogger.debug(header.getKey() + "=" + header.getValue());
			}
			httpConnection.disconnect();
		} catch (MalformedURLException e) {
			WebgatherLogger.warn("Fehlgeformte URL :" + url);
		} catch (IOException e) {
			WebgatherLogger.warn("Fehlgeformte URL :" + url);
		}
	}

}
