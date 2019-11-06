
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

	public OpenAireData getData() {
		OpenAireData data = new OpenAireData();
		if (node == null)
			return null;

		JsonNode jNode = new ObjectMapper().valueToTree(node.getLd2());

		JsonLDMapper mapper = new JsonLDMapper();
		// ArrayList<JsonElementModel> jemList =
		// mapper.mapToJsonElementModel(jNode);

		StringBuffer sb = new StringBuffer();

		// generate creator
		ArrayList<JsonElementModel> jemList = mapper.getElement("root.creator");
		Iterator<JsonElementModel> jemIt = jemList.iterator();
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

		data.addElement("creator", sb.toString());

		// generate FundingReference
		sb = new StringBuffer();
		jemList = mapper.getElement("root.joinedFunding.fundingJoined");
		jemIt = jemList.iterator();
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
		data.addElement("fundingReference", sb.toString());

		// generate alternateIdentifiers, title
		sb = new StringBuffer();
		jemList = mapper.getElement("root");
		jemIt = jemList.iterator();
		while (jemIt.hasNext()) {
			JsonElementModel jem = jemIt.next();
			if (jem.getComplexElementList().get("bibo:doi") != null) {
				sb.append("<datacite:alternateIdentifiers>");
				sb.append("<datacite:alternateIdentifier type=\"DOI\">"
						+ jem.getComplexElementList().get("bibo:doi")
						+ "<oaire:funderName>");
				sb.append("</datacite:alternateIdentifier>");
				sb.append("</datacite:alternateIdentifier>");
				data.addElement("alternateIdentifier", sb.toString());
				sb = new StringBuffer();
			}
			if (jem.getComplexElementList().get("title") != null) {
				sb.append(
						"<datacite:title>" + jem.getComplexElementList().get("title"));
				sb.append("</datacite:title>");
				data.addElement("title", sb.toString());
			}
		}

		return data;
	}
}
