
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
package helper.oai;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.Node;

/**
 * @author Andres Quast
 *
 */
public class OpenAireMapper {

	Node node;
	String uri;

	/**
	 * @param node
	 * @param uri
	 */
	public OpenAireMapper(Node node, String uri) {
		this.node = node;
		this.uri = uri;
	}

	public String getData() {

		JsonNode jNode = new ObjectMapper().valueToTree(node.getLd2());

		// create one time the mapper object we work with until we have all data
		// required
		JsonLDMapper mapper = new JsonLDMapper(jNode);
		StringBuffer sb = new StringBuffer();
		sb.append(setFilePreamble());
		Iterator<Hashtable<String, String>> jsonElementModelIterator = null;
		Hashtable<String, String> element = null;
		// generate title
		ArrayList<Hashtable<String, String>> jemList =
				mapper.getElement("root.title");
		sb.append("<datacite:title>" + jemList.get(0).get("title")
				+ "</datacite:title>\n");
		for (int i = 1; i < jemList.size(); i++) {

			sb.append("<datacite:title>" + jemList.get(i).get("title")
					+ "</datacite:title>\n");
		}
		// generate creator
		jsonElementModelIterator = mapper.getElement("root.creator").iterator();
		sb.append("<datacite:creators>\n");
		while (jsonElementModelIterator.hasNext()) {
			element = jsonElementModelIterator.next();
			sb.append("\t<datacite:creator>\n\t\t<datacite:creatorName>"
					+ element.get("prefLabel") + "</datacite:creatorName>\n");
			if (element.get("@id").startsWith("")) {
				sb.append("\t\t<datacite:nameIdentifier nameIdentifierScheme=\"ORCID\">"
						+ element.get("@id") + "<datacite:nameIdentifier>\n");
			}
			sb.append("\t</datacite:creator>\n");
		}
		sb.append("</datacite:creators>\n");

		// generate FundingReference
		jsonElementModelIterator =
				mapper.getElement("root.joinedFunding.fundingJoined").iterator();
		sb.append("<oaire:fundingReferences>\n");
		while (jsonElementModelIterator.hasNext()) {
			element = jsonElementModelIterator.next();
			sb.append("\t<oaire:fundingReference>\n\t\t<oaire:funderName>"
					+ element.get("prefLabel") + "</oaire:funderName>\n");
			if (element.get("@id").startsWith("")) {
				sb.append(
						"\t\t<oaire:funderIdentifier funderIdentifierType=\"Crossref Funder ID\">"
								+ element.get("@id") + "</oaire:funderIdentifier>\n");
			}
			sb.append("\t</oaire:fundingReference>\n");
		}
		sb.append("</oaire:fundingReferences>\n");

		return sb.toString();
	}

	/**
	 * @return
	 */
	public String setFilePreamble() {
		String preamb = new String("<oai_dc:dc \n"
				+ "    xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\"\n"
				+ "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"
				+ "	xmlns:dcterms=\"http://purl.org/dc/terms/\" \n"
				+ "	xmlns:dc=\"http://purl.org/dc/elements/1.1/\" \n"
				+ "	xmlns:datacite=\"http://datacite.org/schema/kernel-4\"\n"
				+ "	xmlns:oaire=\"http://namespace.openaire.eu/schema/oaire/\"\n"
				+ "	xsi:schemaLocation=\"http://purl.org/dc/terms/ http://dublincore.org/schemas/xmls/qdc/dcterms.xsd \n"
				+ "	http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd \n"
				+ "	http://purl.org/dc/elements/1.1/ http://dublincore.org/schemas/xmls/qdc/2003/04/02/dc.xsd\n"
				+ "	http://datacite.org/schema/kernel-4  http://schema.datacite.org/meta/kernel-4.1/metadata.xsd\n"
				+ "	http://namespace.openaire.eu/schema/oaire/  https://www.openaire.eu/schema/repo-lit/4.0/openaire.xsd\">\n"
				+ "");
		return preamb;
	}
}
