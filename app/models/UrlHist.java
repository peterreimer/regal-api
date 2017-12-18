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

package models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.wordnik.swagger.core.util.JsonUtil;

/**
 * Das Datenmodell für eine URL-Umzugshistorie bei Webpages. Inhalt wird als
 * datastream an den jeweiligen Node (webpage) angehängt.
 * 
 * @author I. Kuss
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UrlHist {

	private ArrayList<UrlHistEntry> urlHistEntries;

	/**
	 * Konstruktor. Legt eine neue, leere URL-Umzugshistorie an.
	 */
	public UrlHist() {
		urlHistEntries = new ArrayList<>();
	}

	/**
	 * Konstruktor. Legt eine neue URL-Umzugshistorie mit der ersten URL an.
	 * 
	 * @param urlFirst die am Anfang gültige URL für die Webpage
	 */
	public UrlHist(String urlFirst) {
		urlHistEntries = new ArrayList<>();
		UrlHistEntry urlHistEntry = new UrlHistEntry(urlFirst);
		urlHistEntries.add(urlHistEntry);
	}

	/**
	 * Legt einen neuen URL-Historieneintrag mit nur einer URL an. Datum = initial
	 * (null).
	 * 
	 * @param url die URL des neuen Eintrages
	 */
	public void addUrlHistEntry(String url) {
		UrlHistEntry urlHistEntry = new UrlHistEntry(url);
		urlHistEntries.add(urlHistEntry);
	}

	/**
	 * Aktualisiert das Enddatum (= Datum, bis zu dem die URL gültig war) im
	 * neuesten URL-Historieneintrag
	 * 
	 * @param endDate das neue Enddatum
	 */
	public void updateLatestUrlHistEntry(Date endDate) {
		int lastIndex = urlHistEntries.size() - 1;
		UrlHistEntry urlHistEntry =
				new UrlHistEntry(urlHistEntries.get(lastIndex).url, endDate);
		urlHistEntries.set(lastIndex, urlHistEntry);
	}

	/**
	 * Liefert die Größe (=Anzahl Elemente) der Liste von URL-Historieneinträgen
	 * zurück.
	 * 
	 * @return die Anzahl Einträge in der URL-Historienliste.
	 */
	public int getSize() {
		return urlHistEntries.size();
	}

	/**
	 * Liest einen bestimmtern URL-Historieneintrag anhand des Index
	 * 
	 * @param index der Index des URL-Historieneintrages (0=ältester)
	 * @return ein URL-Historieneintrag
	 */
	public UrlHistEntry getUrlHistEntry(int index) {
		return urlHistEntries.get(index);
	}

	/**
	 * @return die Liste der URL-Historien-Einträge
	 */
	public ArrayList<UrlHistEntry> getUrlHistEntries() {
		return urlHistEntries;
	}

	/**
	 * @param json a json representation
	 * @return a new UrlHist build from json
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@JsonIgnore
	public static UrlHist create(String json)
			throws JsonParseException, JsonMappingException, IOException {
		return JsonUtil.mapper().readValue(json, UrlHist.class);
	}

	/**
	 * Klasse, die einen URL-Historien-Eintrag implementiert.
	 */
	public class UrlHistEntry {
		String url;
		Date endDate;

		/**
		 * Konstruktor nur mit url
		 * 
		 * @param url
		 */
		public UrlHistEntry(String url) {
			this.url = url;
			this.endDate = null;
		}

		/**
		 * Konstruktor mit URL und Datum
		 * 
		 * @param url
		 * @param endDate
		 */
		public UrlHistEntry(String url, Date endDate) {
			this.url = url;
			this.endDate = endDate;
		}

		public String getUrl() {
			return url;
		}
	}

}
