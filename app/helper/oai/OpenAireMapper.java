
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
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.OpenAireData;
import models.JsonElementModel;
import models.Node;

/**
 * @author Andres Quast
 *
 */
public class OpenAireMapper {

	Node node;
	String uri;

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
		Iterator<JsonElementModel> jemIt = null;

		// generate title
		ArrayList<JsonElementModel> jemList = mapper.getElement("root.title");
		sb.append("<datacite:title>"
				+ jemList.get(0).getComplexElementList().get("root.title")
				+ "</datacite:title>");
		for (int i = 1; i < jemList.size(); i++) {

			sb.append("<datacite:title>"
					+ jemList.get(i).getComplexElementList().get("root.title")
					+ "</datacite:title>");
		}
		// generate creator
		jemIt = mapper.getElement("root.creator").iterator();
		sb.append("<datacite:creators>");
		while (jemIt.hasNext()) {
			JsonElementModel jem = jemIt.next();

			sb.append("<datacite:creator><datacite:creatorName>"
					+ jem.getComplexElementList().get("prefLabel")
					+ "<datacite:creatorName>");
			if (jem.getComplexElementList().get("@id")
					.startsWith("http://orchid.org")) {
				sb.append(
						"<datacite:creator><datacite:nameIdentifier nameIdentifierScheme=\"ORCID\">"
								+ jem.getComplexElementList().get("@id")
								+ "<datacite:nameIdentifier>");
			}
			sb.append("</datacite:creator>");
		}
		sb.append("</datacite:creators>");

		// generate FundingReference
		jemIt = mapper.getElement("root.joinedFunding.fundingJoined").iterator();
		sb.append("<oaire:fundingReferences>");
		while (jemIt.hasNext()) {
			JsonElementModel jem = jemIt.next();
			sb.append("<oaire:fundingReference><oaire:funderName>"
					+ jem.getComplexElementList().get("prefLabel")
					+ "<oaire:funderName>");
			if (jem.getComplexElementList().get("@id")
					.startsWith("http://orchid.org")) {
				sb.append(
						"<oaire:funderIdentifier funderIdentifierType=\"Crossref Funder ID\">"
								+ jem.getComplexElementList().get("@id")
								+ "</oaire:funderIdentifier>");
			}
			sb.append("</oaire:fundingReference>");
		}
		sb.append("</oaire:fundingReferences>");

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
				+ "	xsi:schemaLocation=\"http://purl.org/dc/terms/ http://dublincore.org/schemas/xmls/qdc/dcterms.xsd \n"
				+ "	http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd \n"
				+ "	http://purl.org/dc/elements/1.1/ http://dublincore.org/schemas/xmls/qdc/2003/04/02/dc.xsd\">\n"
				+ "");
		return preamb;
	}
}
