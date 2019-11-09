
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
	Document doc = null;

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
		JsonLDMapper jMapper = new JsonLDMapper(jNode);

		DocumentBuilderFactory.newInstance();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.isValidating();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		doc = docBuilder.newDocument();

		Element resource = doc.createElement("resource");
		resource.setAttribute("xmlns:rdf",
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		resource.setAttribute("xmlns:datacite",
				"http://datacite.org/schema/kernel-4");
		resource.setAttribute("xmlns",
				"http://namespace.openaire.eu/schema/oaire/");
		resource.setAttribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		resource.setAttribute("xsi:schemaLocation",
				"http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.1/metadata.xsd\n"
						+ "	http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd \n"
						+ "	http://datacite.org/schema/kernel-4  http://schema.datacite.org/meta/kernel-4.1/metadata.xsd\n"
						+ "	http://namespace.openaire.eu/schema/oaire/  https://www.openaire.eu/schema/repo-lit/4.0/openaire.xsd\">\n"
						+ "");

		Hashtable<String, String> jsonElementList = new Hashtable<>();

		// Element elem = null;

		// generate title
		ArrayList<Hashtable<String, String>> jemList =
				jMapper.getElement("root.title");
		for (int i = 0; i < jemList.size(); i++) {
			Element title = doc.createElement("datacite:title");
			title.appendChild(doc.createTextNode(jemList.get(i).get("root.title")));
			if (i > 0) {
				title.setAttribute("titleType", "Subtitle");
			}
			resource.appendChild(title);
		}

		// generate creators
		Element creators = doc.createElement("datacite:creators");
		jemList = jMapper.getElement("root.creator");
		for (int i = 0; i < jemList.size(); i++) {
			Element sE = doc.createElement("datacite:creator");
			creators.appendChild(sE);
			Element cn = doc.createElement("datacite:creatorName");
			cn.appendChild(doc.createTextNode(jemList.get(i).get("prefLabel")));
			sE.appendChild(cn);

			// prevent record from displayinglocal ids
			if (!jemList.get(i).get("@id").startsWith("https://frl")) {
				Element ci = doc.createElement("datacite:creatorIdentifier");
				ci.appendChild(doc.createTextNode(jemList.get(i).get("@id")));
				sE.appendChild(ci);
			}

			creators.appendChild(sE);
			resource.appendChild(creators);

		}

		// generate fundingreference
		Element funding = doc.createElement("fundingReferences");
		jemList = jMapper.getElement("root.joinedFunding.fundingJoined");
		for (int i = 0; i < jemList.size(); i++) {
			Element sE = doc.createElement("fundingReference");
			funding.appendChild(sE);
			Element cn = doc.createElement("funderName");
			cn.appendChild(doc.createTextNode(jemList.get(i).get("prefLabel")));
			sE.appendChild(cn);

			if (!jemList.get(i).get("@id").startsWith("https://frl")) {
				Element ci = doc.createElement("funderIdentifier");
				ci.appendChild(doc.createTextNode(jemList.get(i).get("@id")));
				sE.appendChild(ci);
			}

			funding.appendChild(sE);
			resource.appendChild(funding);

		}

		// generate alternateIdentifiers
		Element alternate = doc.createElement("alternateIdentifiers");
		jemList = jMapper.getElement("root.bibo:doi");
		for (int i = 0; i < jemList.size(); i++) {
			Element id = doc.createElement("alternateIdentifier");
			id.appendChild(doc.createTextNode(jemList.get(i).get("root.bibo:doi")));
			id.setAttribute("alternateIdentifierType", "DOI");
			alternate.appendChild(id);
		}

		resource.appendChild(alternate);

		// root.appendChild(elem);

		// elem.appendChild(doc.createTextNode("hallo"));

		doc.appendChild(resource);

		return archive.fedora.XmlUtils.docToString(doc);
	}

	/**
	 * @return
	 */
	public String setFilePreamble() {
		String preamb = new String(
				"<resource xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
						+ "    xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\"\n"
						+ "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"
						+ "	xmlns:dcterms=\"http://purl.org/dc/terms/\" \n"
						+ "	xmlns:dc=\"http://purl.org/dc/elements/1.1/\" \n"
						+ "	xmlns:datacite=\"http://datacite.org/schema/kernel-4\"\n"
						+ "	xmlns=\"http://namespace.openaire.eu/schema/oaire/\"\n"
						+ "	xsi:schemaLocation=\"http://purl.org/dc/terms/ http://dublincore.org/schemas/xmls/qdc/dcterms.xsd \n"
						+ "	http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd \n"
						+ "	http://purl.org/dc/elements/1.1/ http://dublincore.org/schemas/xmls/qdc/2003/04/02/dc.xsd\n"
						+ "	http://datacite.org/schema/kernel-4  http://schema.datacite.org/meta/kernel-4.1/metadata.xsd\n"
						+ "	http://namespace.openaire.eu/schema/oaire/  https://www.openaire.eu/schema/repo-lit/4.0/openaire.xsd\">\n"
						+ "");
		return preamb;
	}
}
