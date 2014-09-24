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
package actions;

import helper.AlephMabMaker;
import helper.Globals;
import helper.OaiOreMaker;
import helper.Services;
import models.Node;

/**
 * @author Jan Schnasse
 *
 */
public class TransformNode {
    Services services = new Services(Globals.fedora, Globals.server);

    /**
     * @param node
     *            pid with namespace:pid
     * @return a aleph mab xml representation
     */
    public String aleph(Node node) {
	AlephMabMaker am = new AlephMabMaker();
	return am.aleph(node, "http://" + Globals.server);
    }

    /**
     * @param pid
     *            pid with namespace:pid
     * @return a aleph mab xml representation
     */
    public String aleph(String pid) {
	return aleph(new ReadNode().readNode(pid));
    }

    /**
     * @param pid
     *            pid with namespace:pid
     * @return a URL to a pdfa conversion
     */
    public String getPdfaUrl(String pid) {
	return getPdfaUrl(new ReadNode().readNode(pid));
    }

    /**
     * @param node
     *            a node with a pdf data stream
     * @return a URL to a PDF/A Conversion
     */
    public String getPdfaUrl(Node node) {
	return services.getPdfaUrl(node, Globals.fedoraIntern);
    }

    /**
     * @param pid
     *            the pid of the object
     * @return a epicur display for the pid
     */
    public String epicur(String pid) {
	String url = Globals.urnbase + pid;
	return services.epicur(url, new ReadNode().getUrn(pid));
    }

    /**
     * @param pid
     *            The pid of an object
     * @return The metadata a oaidc-xml
     */
    public String oaidc(String pid) {
	return services.oaidc(pid);
    }

    /**
     * @param pid
     *            the pid of a node with pdf data
     * @return the plain text content of the pdf
     */
    public String pdfbox(String pid) {
	return services.pdfbox(new ReadNode().readNode(pid),
		Globals.fedoraIntern);
    }

    /**
     * @param node
     *            the node with pdf data
     * @return the plain text content of the pdf
     */
    public String pdfbox(Node node) {
	return services.pdfbox(node, Globals.fedoraIntern);
    }

    /**
     * @param node
     *            the node with pdf data
     * @return the plain text content of the pdf
     */
    public String itext(Node node) {
	return services.itext(node, Globals.fedoraIntern);
    }

    /**
     * @param pid
     *            the pid
     * @param format
     *            application/rdf+xml text/plain application/json
     * @return a oai_ore resource map
     */
    public String oaiore(String pid, String format) {
	return oaiore(new ReadNode().readNode(pid), format);
    }

    /**
     * @param node
     *            a node to get a oai-ore representation from
     * @param format
     *            application/rdf+xml text/plain application/json
     * @return a oai_ore resource map
     */
    public String oaiore(Node node, String format) {
	OaiOreMaker ore = new OaiOreMaker(node);
	return ore.getReM(format, node.getTransformer());
    }

}
