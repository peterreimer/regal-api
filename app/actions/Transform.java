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

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.w3c.dom.Element;

import archive.fedora.CopyUtils;
import archive.fedora.XmlUtils;
import helper.AlephMabMaker;
import helper.Globals;
import helper.HttpArchiveException;
import helper.OaiDcMapper;
import helper.OaiOreMaker;
import helper.PdfText;
import models.DublinCoreData;
import models.Node;

/**
 * @author Jan Schnasse
 *
 */
public class Transform {

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
	return aleph(new Read().readNode(pid));
    }

    /**
     * @param pid
     *            pid with namespace:pid
     * @return a URL to a pdfa conversion
     */
    public String getPdfaUrl(String pid) {
	return getPdfaUrl(new Read().readNode(pid));
    }

    /**
     * @param node
     *            a node with a pdf data stream
     * @return a URL to a PDF/A Conversion
     */
    public String getPdfaUrl(Node node) {
	String redirectUrl = null;
	try {
	    String dataUri = getHttpDataUri(node);
	    URL pdfaConverter = new URL(
		    "http://nyx.hbz-nrw.de/pdfa/api/convertFromUrl?inputFile="
			    + dataUri);
	    HttpURLConnection connection = (HttpURLConnection) pdfaConverter
		    .openConnection();
	    connection.setRequestMethod("POST");
	    connection.setRequestProperty("Accept", "application/xml");
	    Element root = XmlUtils.getDocument(connection.getInputStream());
	    List<Element> elements = XmlUtils.getElements("//resultFileUrl",
		    root, null);
	    if (elements.size() != 1) {
		throw new HttpArchiveException(500,
			"PDFa conversion returns wrong numbers of resultFileUrls: "
				+ elements.size());
	    }
	    redirectUrl = elements.get(0).getTextContent();
	    return redirectUrl;
	} catch (MalformedURLException e) {
	    throw new HttpArchiveException(500, e);
	} catch (IOException e) {
	    throw new HttpArchiveException(500, e);
	}

    }

    private String getHttpDataUri(Node node) {
	return Globals.useHttpUris ? node.getDataUri() : Globals.server
		+ "/resource/" + node.getDataUri();
    }

    /**
     * @param pid
     *            the pid of the object
     * @return a epicur display for the pid
     */
    public String epicur(String pid) {
	String url = Globals.urnbase + pid;
	Read ra = new Read();
	String urn = ra.getUrn(ra.readNode(pid));
	String status = "urn_new";
	String result = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<epicur xmlns=\"urn:nbn:de:1111-2004033116\" xsi:schemaLocation=\"urn:nbn:de:1111-2004033116 http://www.persistent-identifier.de/xepicur/version1.0/xepicur.xsd\">\n"
		+ "\t<administrative_data>\n"
		+ "\t\t<delivery>\n"
		+ "\t\t\t<update_status type=\""
		+ status
		+ "\"></update_status>\n"
		+ "\t\t\t<transfer type=\"oai\"></transfer>\n"
		+ "\t\t</delivery>\n"
		+ "\t</administrative_data>\n"
		+ "<record>\n"
		+ "\t<identifier scheme=\"urn:nbn:de\">"
		+ urn
		+ "</identifier>\n"
		+ "\t<resource>\n"
		+ "\t\t<identifier origin=\"original\" role=\"primary\" scheme=\"url\" type=\"frontpage\">"
		+ url
		+ "</identifier>\n"
		+ "\t\t<format scheme=\"imt\">text/html</format>\n"
		+ "\t</resource>" + "</record>\n" + "</epicur> ";
	return result;
    }

    /**
     * @param pid
     *            The pid of an object
     * @return a dc mapping
     */
    public DublinCoreData oaidc(String pid) {
	Node node = new Read().readNode(pid);
	String uri = Globals.urnbase + node.getPid();
	DublinCoreData data = new OaiDcMapper(node).getData().addIdentifier(
		uri, "dcterms:Uri");
	return data;
    }

    /**
     * @param pid
     *            the pid of a node with pdf data
     * @return the plain text content of the pdf
     */
    public String pdfbox(String pid) {
	return pdfbox(new Read().readNode(pid));
    }

    /**
     * @param node
     *            the node with pdf data
     * @return the plain text content of the pdf
     */
    public String pdfbox(Node node) {
	String pid = node.getPid();
	String mimeType = node.getMimeType();
	if (mimeType == null)
	    throw new HttpArchiveException(
		    404,
		    "The node "
			    + pid
			    + " does not provide a mime type. It may not even contain data at all!");
	if (mimeType.compareTo("application/pdf") != 0)
	    throw new HttpArchiveException(406,
		    "Wrong mime type. Cannot extract text from " + mimeType);
	URL content = null;
	try {
	    content = new URL(getHttpDataUri(node));
	    File pdfFile = CopyUtils.download(content);
	    PdfText pdf = new PdfText();
	    return pdf.toString(pdfFile);
	} catch (MalformedURLException e) {
	    throw new HttpArchiveException(500, e);
	} catch (IOException e) {
	    throw new HttpArchiveException(500, e);
	}
    }

    /**
     * @param node
     *            the node with pdf data
     * @return the plain text content of the pdf
     */
    public String itext(Node node) {
	String pid = node.getPid();

	String mimeType = node.getMimeType();
	if (mimeType == null)
	    throw new HttpArchiveException(
		    404,
		    "The node "
			    + pid
			    + " does not provide a mime type. It may not even contain data at all!");
	if (mimeType.compareTo("application/pdf") != 0)
	    throw new HttpArchiveException(406,
		    "Wrong mime type. Cannot extract text from " + mimeType);
	URL content = null;
	try {
	    content = new URL(getHttpDataUri(node));
	    File pdfFile = CopyUtils.download(content);
	    PdfText pdf = new PdfText();
	    return pdf.itext(pdfFile);
	} catch (MalformedURLException e) {
	    throw new HttpArchiveException(500, e);
	} catch (IOException e) {
	    throw new HttpArchiveException(500, e);
	}
    }

    /**
     * @param pid
     *            the pid
     * @param format
     *            application/rdf+xml text/plain application/json
     * @return a oai_ore resource map
     */
    public String oaiore(String pid, String format) {
	return oaiore(new Read().readNode(pid), format);
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
