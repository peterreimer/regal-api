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
package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Andres Quast
 *
 */
public class OpenAireRecord implements java.io.Serializable {

	@Override
	public String toString() {
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
		Document doc = docBuilder.newDocument();

		Element root = doc.createElement("resource");
		root.setAttribute("xmlns:rdf",
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		root.setAttribute("xmlns:datacite", "http://datacite.org/schema/kernel-4");
		root.setAttribute("xmlns", "http://namespace.openaire.eu/schema/oaire/");
		root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		root.setAttribute("xsi:schemaLocation",
				"http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.1/metadata.xsd\n"
						+ "	http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd \n"
						+ "	http://datacite.org/schema/kernel-4  http://schema.datacite.org/meta/kernel-4.1/metadata.xsd\n"
						+ "	http://namespace.openaire.eu/schema/oaire/  https://www.openaire.eu/schema/repo-lit/4.0/openaire.xsd\">\n"
						+ "");

		doc.appendChild(root);
		return doc.getXmlVersion();

	}
}
