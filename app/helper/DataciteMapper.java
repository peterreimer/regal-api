/*
 * Copyright 2014 hbz NRW (http://www.hbz-nrw.de/)
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

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.DataciteRecord;
import models.Globals;
import models.Pair;

/**
 * @author Jan Schnasse
 *
 */
@SuppressWarnings("unchecked")
public class DataciteMapper {

	private static final String TYPE_VIDEO =
			"http://rdaregistry.info/termList/RDAMediaType/1008";
	private static final String TYPE_MISC =
			"http://purl.org/lobid/lv#Miscellaneous";

	/**
	 * @param doi the doi identifier
	 * @param ld the object as json-ld linked data in a map
	 * @return an object containing data for datacite
	 */
	public static DataciteRecord getDataciteRecord(String doi,
			Map<String, Object> map) {
		JsonNode ld = new ObjectMapper().convertValue(map, JsonNode.class);
		DataciteRecord rec = new DataciteRecord(doi);
		addSubjects(ld, rec);
		addCreators(ld, rec);
		addTitles(ld, rec);
		addPublisher(ld, rec);
		addPublicationYear(ld, rec);
		addLanguage(ld, rec);
		addDates(ld, rec);
		addAlternativeIdentifiers(ld, rec);
		addSizes(ld, rec);// http://purl.org/dc/terms/bibliographicCitation
		addFormats(ld, rec);// mime
		addResourceTypeGeneral(ld, rec);// bibliographic resource
		addResourceType(ld, rec);
		return rec;
	}

	private static void addResourceType(JsonNode ld, DataciteRecord rec) {
		rec.type = ld.at("/rdftype/0/prefLabel").asText();
		if ("Abschlussarbeit".equals(rec.type)) {
			rec.type = "Hochschulschrift";
		} else if ("Statistics".equals(rec.type)) {
			rec.type = "Statistik";
		} else if ("Leitlinie/Normschrift".equals(rec.type)) {
			rec.type = "Leitlinie";
		}
	}

	private static void addResourceTypeGeneral(JsonNode ld, DataciteRecord rec) {
		rec.typeGeneral = ld.at("/rdftype/0/@id").asText();
		play.Logger.debug(ld.asText());
		if (TYPE_VIDEO.equals(rec.typeGeneral)) {
			rec.typeGeneral = "AudioVisual";
		}
		if (TYPE_MISC.equals(rec.typeGeneral)) {
			rec.typeGeneral = "Other";
		} else {
			rec.typeGeneral = "Text";
		}
	}

	private static void addFormats(JsonNode ld, DataciteRecord rec) {
		try {
			rec.formats.add(new Pair<String, String>("application/pdf", "mime"));
		} catch (NullPointerException e) {
			play.Logger.debug("DataciteMapper: Metadatafield 'Format' not found!");
		}
	}

	private static void addSizes(JsonNode ld, DataciteRecord rec) {
		try {
			String bibDetails = ld.at("/bibliographicCitation").textValue();
			if (bibDetails != null) {
				rec.sizes.add(new Pair<String, String>(bibDetails, null));
			}
		} catch (NullPointerException e) {
			play.Logger.debug("DataciteMapper: Metadatafield 'Sizes' not found!");
		}
	}

	private static void addAlternativeIdentifiers(JsonNode ld,
			DataciteRecord rec) {
		try {
			// Lobid
			JsonNode lurl =
					ld.at("/parallelEdition").iterator().next().at("/prefLabel");
			String lobidUrl = lurl.asText();

			play.Logger.debug("HTNUMMER " + lobidUrl);
			if (lobidUrl != null) {
				// HT
				String ht = lobidUrl.substring(lobidUrl.lastIndexOf("/") + 1,
						lobidUrl.length() - 2);
				// HBZ-HT
				String hbzht = "HBZ" + ht;
				rec.alternateIdentifiers
						.add(new Pair<String, String>(ht, "hbzAleph-IDN"));
				rec.alternateIdentifiers
						.add(new Pair<String, String>(hbzht, "Consortium-IDN"));
				rec.alternateIdentifiers
						.add(new Pair<String, String>(lobidUrl, "LOD-Catalog"));
			}
			// URN
			String urn = ld.get("urn").toString();
			if (urn != null && !urn.isEmpty()) {
				rec.alternateIdentifiers.add(new Pair<String, String>(urn, "URN"));
			}
			// URL
			String pid = ld.at("/@id").textValue();
			String url = Globals.urnbase + pid;
			rec.alternateIdentifiers.add(new Pair<String, String>(url, "URL"));
		} catch (Exception e) {
			play.Logger.debug("", e);
		}
	}

	private static void addDates(JsonNode ld, DataciteRecord rec) {
		try {
			String date = ld.at("/isDescribedBy/created").asText();
			rec.dates
					.add(new Pair<String, String>(date.substring(0, 10), "Submitted"));
		} catch (Exception e) {
			play.Logger.debug("DataciteMapper: Metadatafield 'Submitted' not found!",
					e);
		}
	}

	private static void addLanguage(JsonNode ld, DataciteRecord rec) {
		try {
			JsonNode languages = ld.at("/language");
			if (languages != null) {
				for (JsonNode item : languages) {
					String langid = item.at("/@id").asText();
					rec.language = langid.substring(langid.length() - 3);
				}
			}
		} catch (NullPointerException e) {
			play.Logger.debug("DataciteMapper: Metadatafield 'Language' not found!",
					e);
		}
	}

	private static void addPublicationYear(JsonNode ld, DataciteRecord rec) {
		try {
			rec.publicationYear = ld.at("/issued").asText();
		} catch (NullPointerException e) {
			play.Logger
					.debug("DataciteMapper: Metadatafield 'PublicationYear' not found!");
		}
		if (rec.publicationYear == null || rec.publicationYear.isEmpty()) {
			rec.publicationYear = getCurrentYear();
		}
	}

	public static String getCurrentYear() {
		return java.time.Year.now().toString();
	}

	private static void addPublisher(JsonNode ld, DataciteRecord rec) {
		try {
			play.Logger.debug(ld.get("publisher").getClass() + "");
			play.Logger.debug(ld.get("publisher") + "");
			rec.publisher = ld.at("/publisher").asText();

		} catch (NullPointerException e) {
			play.Logger.debug("DataciteMapper: Metadatafield 'Publisher' not found!");
		}
		try {
			if (rec.publisher == null || rec.publisher.isEmpty()) {
				rec.publisher = ld.at("/P60489").asText();
			}
		} catch (NullPointerException e) {

			play.Logger.debug(
					"DataciteMapper: Metadatafield 'thesisInformation' not found!");
		}
		try {
			if (rec.publisher == null || rec.publisher.isEmpty()) {
				JsonNode institutions = ld.at("/institution");
				if (institutions != null) {
					for (JsonNode item : institutions) {
						String subject = item.at("/prefLabel").asText();
						rec.publisher = subject;
					}
				}
			}
		} catch (Exception e) {

			play.Logger
					.debug("DataciteMapper: Metadatafield 'institution' not found!");
		}

		if (rec.publisher == null || rec.publisher.isEmpty()) {
			rec.publisher = "PUBLISSO";
		}

	}

	private static void addTitles(JsonNode ld, DataciteRecord rec) {
		try {
			JsonNode list = ld.at("/title");
			if (list != null) {
				for (JsonNode item : list) {
					rec.titles.add(new Pair<String, String>(item.asText(), ""));
				}
			}
			list = ld.at("/alternativeTitle");
			if (list != null) {
				for (JsonNode item : list) {
					rec.titles
							.add(new Pair<String, String>(item.asText(), "alternativeTitle"));
				}
			}
			list = ld.at("/otherTitleInformation");
			if (list != null) {
				for (JsonNode item : list) {
					rec.titles.add(
							new Pair<String, String>(item.asText(), "otherTitleInformation"));
				}
			}
			list = ld.at("/fullTitle");
			if (list != null) {
				for (JsonNode item : list) {
					rec.titles.add(new Pair<String, String>(item.asText(), "fullTitle"));
				}
			}
			list = ld.at("/shortTitle");
			if (list != null) {
				for (JsonNode item : list) {
					rec.titles.add(new Pair<String, String>(item.asText(), "shortTitle"));
				}
			}
		} catch (NullPointerException e) {
			play.Logger.debug("", e);
		}
	}

	private static void addCreators(JsonNode ld, DataciteRecord rec) {
		try {
			JsonNode creators = ld.at("/creator");
			if (creators != null) {
				for (JsonNode item : creators) {
					String subject = item.at("/prefLabel").asText();
					rec.creators.add(new Pair<String, String>(subject, null));
				}
			}
			JsonNode creatorNames = ld.at("/creatorName");
			if (creatorNames != null) {
				for (JsonNode item : creatorNames) {
					rec.creators.add(new Pair<String, String>(item.asText(), null));
				}
			}

			JsonNode contributors = ld.at("/contributor");
			if (contributors != null) {
				for (JsonNode item : contributors) {
					String subject = item.at("/prefLabel").asText();
					rec.creators.add(new Pair<String, String>(subject, null));
				}
			}
			JsonNode contributorNames = ld.at("/contributorName");
			if (contributorNames != null) {
				for (JsonNode item : contributorNames) {
					rec.creators.add(new Pair<String, String>(item.asText(), null));
				}
			}

			if (rec.creators.isEmpty()) {
				JsonNode editors = ld.at("/editor");
				if (editors != null) {
					for (JsonNode item : editors) {
						String subject = item.at("/prefLabel").asText();
						rec.creators.add(new Pair<String, String>(subject, null));
					}
				}
			}

			if (rec.creators.isEmpty()) {
				rec.creators.add(
						new Pair<String, String>("No author information available.", null));
			}
		} catch (NullPointerException e) {
			play.Logger.debug("", e);
		}
	}

	private static void addSubjects(JsonNode ld, DataciteRecord rec) {
		JsonNode subjects = ld.at("/subject");
		if (subjects != null) {
			for (JsonNode item : subjects) {
				String subject = item.at("/prefLabel").asText();
				String id = item.at("/@id").asText();
				String type = "GND";
				if (id.startsWith("http://d-nb.info/gnd/")) {
					type = "GND";
				} else if (id.startsWith("http://dewey.info/")) {
					type = "DDC";
				}
				rec.subjects.add(new Pair<String, String>(subject, type));
			}
		}
		// List<String> freeSubjects = (HashSet<String>)ld.get("dc:subject");
		// if (freeSubjects != null) {
		// for (String item : freeSubjects) {
		// rec.subjects.add(new Pair<String, String>(item, "FREE"));
		// }
		// }
	}

}
