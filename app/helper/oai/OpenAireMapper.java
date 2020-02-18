
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
import models.CoarModel;
import models.EmbargoModel;

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

	/**
	 * @return
	 */
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
		resource.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
		resource.setAttribute("xmlns:dcterms", "http://purl.org/dc/terms/");
		resource.setAttribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		resource.setAttribute("xsi:schemaLocation",
				"http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.1/metadata.xsd \n"
						+ "	http://namespace.openaire.eu/schema/oaire/  https://www.openaire.eu/schema/repo-lit/4.0/openaire.xsd \n"
						+ "	http://purl.org/dc/terms/ http://dublincore.org/schemas/xmls/qdc/dcterms.xsd \n"
						+ "	http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd \n"
						+ "	http://purl.org/dc/elements/1.1/ http://dublincore.org/schemas/xmls/qdc/2003/04/02/dc.xsd \n"
						+ "	http://datacite.org/schema/kernel-4  http://schema.datacite.org/meta/kernel-4.1/metadata.xsd \n"
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

			// prevent record from displaying local ids
			if (!jemList.get(i).get("@id").startsWith("https://frl")
					&& !jemList.get(i).get("@id").startsWith("https://api.ellinet")) {
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

			Element cp = doc.createElement("fundingStream");
			cp.appendChild(
					doc.createTextNode(jemList.get(i).get("fundingProgramJoined")));
			sE.appendChild(cp);

			Element cpi = doc.createElement("awardNumber");
			cpi.appendChild(
					doc.createTextNode(jemList.get(i).get("projectIdJoined")));
			sE.appendChild(cpi);

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

		// generate language
		jemList = jMapper.getElement("root.language");
		for (int i = 0; i < jemList.size(); i++) {
			Element language = doc.createElement("dc:language");
			language.appendChild(
					doc.createTextNode(jemList.get(i).get("@id").substring(38)));
			resource.appendChild(language);
		}

		// generate description
		jemList = jMapper.getElement("root.abstractText");
		for (int i = 0; i < jemList.size(); i++) {
			Element description = doc.createElement("datacite:description");
			description.appendChild(
					doc.createTextNode(jemList.get(i).get("root.abstractText")));
			resource.appendChild(description);
		}

		// generate identifier
		jemList = jMapper.getElement("root");
		for (int i = 0; i < jemList.size(); i++) {
			if (jemList.get(i).containsKey("@id")) {
				Element identifier = doc.createElement("datacite:identifier");
				identifier.appendChild(doc.createTextNode(
						"https://frl.publisso.de/" + jemList.get(i).get("@id")));
				identifier.setAttribute("identifierType", "PURL");
				resource.appendChild(identifier);
			}
		}

		// generate source
		jemList = jMapper.getElement("root.containedIn");
		for (int i = 0; i < jemList.size(); i++) {
			Element source = doc.createElement("dc:source");
			source.appendChild(doc.createTextNode(jemList.get(i).get("prefLabel")));
			// source.setAttribute("identifierType", "PURL");
			resource.appendChild(source);
		}

		// generate subjects
		Element subjects = doc.createElement("datacite:subjects");
		jemList = jMapper.getElement("root.ddc");
		for (int i = 0; i < jemList.size(); i++) {
			Element sE = doc.createElement("datacite:subject");
			sE.appendChild(doc.createTextNode(jemList.get(i).get("prefLabel")));
			sE.setAttribute("subjectScheme", "DDC");
			sE.setAttribute("schemeURI", "http://dewey.info");
			sE.setAttribute("valueURI", jemList.get(i).get("@id"));

			subjects.appendChild(sE);
			resource.appendChild(subjects);
		}

		jemList = jMapper.getElement("root.subject");
		for (int i = 0; i < jemList.size(); i++) {
			Element sE = doc.createElement("datacite:subject");
			sE.appendChild(doc.createTextNode(jemList.get(i).get("prefLabel")));

			// prevent record from displaying local id's
			if (!jemList.get(i).get("@id").startsWith("https://frl")
					&& !jemList.get(i).get("@id").startsWith("https://api.ellinet")) {
				sE.setAttribute("valueURI", jemList.get(i).get("@id"));
			}

			subjects.appendChild(sE);
			resource.appendChild(subjects);
		}

		// generate publisher
		jemList = jMapper.getElement("root.containedIn");
		for (int i = 0; i < jemList.size(); i++) {
			Element publisher = doc.createElement("dc:publisher");
			publisher
					.appendChild(doc.createTextNode(jemList.get(i).get("prefLabel")));
			resource.appendChild(publisher);
		}

		// generate oaire:file
		jemList = jMapper.getElement("root.hasPart");
		for (int i = 0; i < jemList.size(); i++) {
			Element oairefile = doc.createElement("file");
			oairefile.appendChild(
					doc.createTextNode("https://repository.publisso.de/resource/"
							+ jemList.get(i).get("@id") + "/data"));
			resource.appendChild(oairefile);
		}

		// generate licenseCondition
		jemList = jMapper.getElement("root.license");
		for (int i = 0; i < jemList.size(); i++) {
			Element license = doc.createElement("licenseCondition");
			license.appendChild(doc.createTextNode(jemList.get(i).get("prefLabel")));
			license.setAttribute("uri", jemList.get(i).get("@id"));
			resource.appendChild(license);
		}

		// generate dates
		Element dates = doc.createElement("datacite:dates");

		// generate dateIssued
		jemList = jMapper.getElement("root.publicationYear");
		for (int i = 0; i < jemList.size(); i++) {
			Element issued = doc.createElement("datacite:date");
			issued.appendChild(
					doc.createTextNode(jemList.get(i).get("root.publicationYear")));
			issued.setAttribute("dateType", "Issued");
			dates.appendChild(issued);
		}
		jemList = jMapper.getElement("root.embargoTime");
		for (int i = 0; i < jemList.size(); i++) {
			Element available = doc.createElement("datacite:date");
			available.appendChild(
					doc.createTextNode(jemList.get(i).get("root.embargoTime")));
			available.setAttribute("dateType", "Available");
			dates.appendChild(available);
			// available.appendChild(doc.createTextNode(jMapper.getElement("root.isDescribedBy.created").toString()));
			// available.setAttribute("dateType", "Accepted");
		}
		resource.appendChild(dates);

		// generate accessRights
		if (jMapper.elementExists("root.embargoTime")) {
			jemList = jMapper.getElement("root.embargoTime");
			for (int i = 0; i < jemList.size(); i++) {
				String acScheme = null;
				EmbargoModel emb = new EmbargoModel();
				System.out.println(
						emb.isActiveEmbargo(jemList.get(i).get("root.embargoTime")));
				if (emb.isActiveEmbargo(jemList.get(i).get("root.embargoTime"))) {
					acScheme =
							emb.getAccessScheme(jemList.get(i).get("root.embargoTime"));
					System.out.println(acScheme);
				} else {
					acScheme = "public";
				}
				Element rights = doc.createElement("dc:rights");
				rights.appendChild(
						doc.createTextNode(CoarModel.getElementValue(acScheme)));
				rights.setAttribute("uri", CoarModel.getUriAttributeValue(acScheme));
				resource.appendChild(rights);
			}
		} else {
			jemList = jMapper.getElement("root");
			for (int i = 0; i < jemList.size(); i++) {
				if (jemList.get(i).containsKey("accessScheme")) {
					Element rights = doc.createElement("dc:rights");
					rights.appendChild(doc.createTextNode(
							CoarModel.getElementValue(jemList.get(i).get("accessScheme"))));
					rights.setAttribute("uri", CoarModel
							.getUriAttributeValue(jemList.get(i).get("accessScheme")));
					resource.appendChild(rights);
				}
			}
		}

		doc.appendChild(resource);

		return archive.fedora.XmlUtils.docToString(doc);
	}

}
