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

import static archive.fedora.FedoraVocabulary.IS_MEMBER_OF;
import static archive.fedora.FedoraVocabulary.ITEM_ID;
import helper.Globals;
import helper.HttpArchiveException;
import helper.OaiSet;
import helper.OaiSetBuilder;
import helper.URN;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import models.DublinCoreData;
import models.Link;
import models.Node;
import models.Transformer;
import archive.fedora.CopyUtils;
import archive.fedora.RdfException;
import archive.fedora.RdfUtils;

/**
 * @author Jan Schnasse
 *
 */
public class ModifyNode {

    final static Logger logger = LoggerFactory.getLogger(ModifyNode.class);

    /**
     * @param pid
     *            the pid that must be updated
     * @param content
     *            the file content as byte array
     * @param mimeType
     *            the mimetype of the file
     * @param name
     *            the name of the file
     * @param md5Hash
     *            a hash for the content. Can be null.
     * @return A short message
     * @throws IOException
     *             if data can not be written to a tmp file
     */
    public String updateData(String pid, InputStream content, String mimeType,
	    String name, String md5Hash) throws IOException {
	if (content == null) {
	    throw new HttpArchiveException(406, pid
		    + " you've tried to upload an empty stream."
		    + " This action is not supported. Use HTTP DELETE instead.");
	}
	File tmp = File.createTempFile(name, "tmp");
	tmp.deleteOnExit();
	CopyUtils.copy(content, tmp);
	Node node = new ReadNode().readNode(pid);
	if (node != null) {
	    node.setUploadData(tmp.getAbsolutePath(), mimeType);
	    node.setFileLabel(name);
	    node.setMimeType(mimeType);
	    Globals.fedora.updateNode(node);
	} else {
	    throw new HttpArchiveException(500, "Lost Node!");
	}
	new IndexNode().index(node);
	if (md5Hash != null && !md5Hash.isEmpty()) {
	    node = new ReadNode().readNode(pid);
	    String fedoraHash = node.getChecksum();
	    if (!md5Hash.equals(fedoraHash)) {
		throw new HttpArchiveException(417, pid + " expected a MD5 of "
			+ fedoraHash + " but you provided a MD5 value of "
			+ md5Hash);
	    }
	}
	return pid + " data successfully updated!";
    }

    /**
     * @param pid
     *            The pid that must be updated
     * @param dc
     *            A dublin core object
     * @return a short message
     */
    public String updateDC(String pid, DublinCoreData dc) {
	Node node = new ReadNode().readNode(pid);
	node.setDublinCoreData(dc);
	Globals.fedora.updateNode(node);
	new IndexNode().index(node);
	return pid + " dc successfully updated!";
    }

    /**
     * @param pid
     *            The pid that must be updated
     * @param content
     *            The metadata as rdf string
     * @return a short message
     */
    public String updateMetadata(String pid, String content) {
	try {
	    if (content == null) {
		throw new HttpArchiveException(406, pid
			+ "You've tried to upload an empty string."
			+ " This action is not supported."
			+ " Use HTTP DELETE instead.");
	    }
	    RdfUtils.validate(content);
	    File file = CopyUtils.copyStringToFile(content);
	    Node node = new ReadNode().readNode(pid);
	    if (node != null) {
		node.setMetadataFile(file.getAbsolutePath());
		Globals.fedora.updateNode(node);
	    }
	    new IndexNode().index(node);
	    return pid + " metadata successfully updated!";
	} catch (RdfException e) {
	    throw new HttpArchiveException(400);
	} catch (IOException e) {
	    throw new UpdateNodeException(e);
	}
    }

    /**
     * @param node
     *            read metadata from the Node to the repository
     * @return a message
     */
    public String updateMetadata(Node node) {
	Globals.fedora.updateNode(node);
	String pid = node.getPid();
	new IndexNode().index(node);
	return pid + " metadata successfully updated!";
    }

    /**
     * @param pid
     *            The pid to which links must be added
     * @param links
     *            list of links
     * @return a short message
     */
    public String addLinks(String pid, List<Link> links) {
	Node node = new ReadNode().readNode(pid);
	for (Link link : links) {
	    node.addRelation(link);
	}
	Globals.fedora.updateNode(node);
	new IndexNode().index(node);
	return pid + " " + links + " links successfully added.";
    }

    /**
     * @param pid
     *            The pid to which links must be added uses: Vector<Link> v =
     *            new Vector<Link>(); v.add(link); return addLinks(pid, v);
     * @param link
     *            a link
     * @return a short message
     */
    public String addLink(String pid, Link link) {
	Vector<Link> v = new Vector<Link>();
	v.add(link);
	return addLinks(pid, v);
    }

    /**
     * Generates a urn
     * 
     * @param pid
     *            usually the pid of an object
     * @param namespace
     *            usually the namespace
     * @param snid
     *            the urn subnamespace id
     * @return the urn
     */
    public String replaceUrn(String pid, String namespace, String snid) {
	String subject = namespace + ":" + pid;
	String urn = generateUrn(subject, snid);
	String hasUrn = "http://purl.org/lobid/lv#urn";
	String metadata = new ReadNode().readMetadata(subject);
	metadata = RdfUtils.replaceTriple(subject, hasUrn, urn, true, metadata);
	updateMetadata(namespace + ":" + pid, metadata);
	return "Update " + subject + " metadata " + metadata;
    }

    /**
     * @param p
     *            the id part of a pid
     * @param namespace
     *            the namespace part of a pid
     * @param transformerId
     *            the id of the transformer
     */
    public void addTransformer(String p, String namespace, String transformerId) {
	String pid = namespace + ":" + p;
	Node node = new ReadNode().readNode(pid);
	node.addTransformer(new Transformer(transformerId));
	Globals.fedora.updateNode(node);
    }

    /**
     * Generates a urn
     * 
     * @param pid
     *            usually the pid of an object
     * @param namespace
     *            usually the namespace
     * @param snid
     *            the urn subnamespace id
     * @return the urn
     */
    public String addUrn(String pid, String namespace, String snid) {
	String subject = namespace + ":" + pid;
	String urn = generateUrn(subject, snid);
	String hasUrn = "http://purl.org/lobid/lv#urn";
	String metadata = new ReadNode().readMetadata(subject);
	if (RdfUtils.hasTriple(subject, hasUrn, urn, metadata))
	    throw new HttpArchiveException(409, subject + "already has a urn: "
		    + metadata);
	metadata = RdfUtils.addTriple(subject, hasUrn, urn, true, metadata);
	updateMetadata(namespace + ":" + pid, metadata);
	return "Update " + subject + " metadata " + metadata;
    }

    @SuppressWarnings({ "serial" })
    private class UpdateNodeException extends RuntimeException {
	public UpdateNodeException(Throwable cause) {
	    super(cause);
	}
    }

    /**
     * @param pid
     *            adds lobidmetadata (if avaiable) to the node and updates the
     *            repository
     * @return a message
     */
    public String lobidify(String pid) {
	Node node = new ReadNode().readNode(pid);
	node = lobidify(node);
	new IndexNode().index(node);
	return updateMetadata(node);
    }

    /**
     * @param pid
     *            the pid of a node that must be published on the oai interface
     * @return A short message.
     */
    public String makeOAISet(String pid) {
	return makeOAISet(pid, Globals.fedoraIntern);
    }

    /**
     * @param pid
     *            the pid of a node that must be published on the oai interface
     * @param fedoraExtern
     *            the fedora endpoint for external users
     * @return A short message.
     */
    public String makeOAISet(String pid, String fedoraExtern) {

	Node node = Globals.fedora.readNode(pid);
	try {
	    URL metadata = new URL(fedoraExtern + "/objects/" + pid
		    + "/datastreams/metadata/content");
	    OaiSetBuilder oaiSetBuilder = new OaiSetBuilder();
	    RepositoryResult<Statement> statements = RdfUtils
		    .getStatements(metadata);
	    while (statements.hasNext()) {
		Statement st = statements.next();
		String subject = st.getSubject().stringValue();
		String predicate = st.getPredicate().stringValue();
		String object = st.getObject().stringValue();

		OaiSet set = oaiSetBuilder.getSet(subject, predicate, object);
		if (set == null) {
		    continue;
		}
		if (!Globals.fedora.nodeExists(set.getPid())) {
		    createOAISet(set.getName(), set.getSpec(), set.getPid());
		}
		linkObjectToOaiSet(node, set.getSpec(), set.getPid());
	    }
	    String name = "open_access";
	    String spec = "open_access";
	    String namespace = "oai";
	    String oaipid = namespace + ":" + "open_access";
	    if (!Globals.fedora.nodeExists(oaipid)) {
		createOAISet(name, spec, oaipid);
	    }
	    linkObjectToOaiSet(node, spec, oaipid);
	    return pid + " successfully created oai sets!";
	} catch (Exception e) {
	    throw new MetadataNotFoundException(e);
	}
    }

    private void linkObjectToOaiSet(Node node, String spec, String pid) {
	node.removeRelations(ITEM_ID);
	node.removeRelation(IS_MEMBER_OF, "info:fedora/" + pid);
	Link link = new Link();
	link.setPredicate(IS_MEMBER_OF);
	link.setObject("info:fedora/" + pid, false);
	node.addRelation(link);
	link = new Link();
	link.setPredicate(ITEM_ID);
	link.setObject(Globals.server + "/" + "resource" + "/" + node.getPid(),
		false);
	node.addRelation(link);
	Globals.fedora.updateNode(node);
    }

    private void createOAISet(String name, String spec, String pid) {
	String setSpecPred = "http://www.openarchives.org/OAI/2.0/setSpec";
	String setNamePred = "http://www.openarchives.org/OAI/2.0/setName";
	Link setSpecLink = new Link();
	setSpecLink.setPredicate(setSpecPred);
	Link setNameLink = new Link();
	setNameLink.setPredicate(setNamePred);
	String namespace = "oai";
	{
	    Node oaiset = new Node();
	    oaiset.setNamespace(namespace);
	    oaiset.setPID(pid);
	    setSpecLink.setObject(spec, true);
	    oaiset.addRelation(setSpecLink);
	    setNameLink.setObject(name, true);
	    oaiset.addRelation(setNameLink);
	    DublinCoreData dc = oaiset.getDublinCoreData();
	    dc.addTitle(name);
	    oaiset.setDublinCoreData(dc);
	    Globals.fedora.createNode(oaiset);
	}
    }

    /**
     * Generates a urn
     * 
     * @param niss
     *            usually the pid of an object
     * @param snid
     *            usually the namespace
     * @return the urn
     */
    public String generateUrn(String niss, String snid) {
	URN urn = new URN(snid, niss);
	return urn.toString();
    }

    /**
     * @param node
     *            generate metadatafile with lobid data for this node
     * @return a short message
     */
    public Node lobidify(Node node) {
	String pid = node.getPid();
	List<String> identifier = node.getDublinCoreData().getIdentifier();
	String alephid = "";
	for (String id : identifier) {
	    if (id.startsWith("TT") || id.startsWith("HT")) {
		alephid = id;
		break;
	    }
	}
	if (alephid.isEmpty()) {
	    throw new HttpArchiveException(500, pid + " no Catalog-Id found");
	}
	String lobidUri = "http://lobid.org/resource/" + alephid;
	try {
	    URL lobidUrl = new URL("http://lobid.org/resource/" + alephid);
	    RDFFormat inFormat = RDFFormat.TURTLE;
	    String accept = "text/turtle";
	    String str = RdfUtils.readRdfToString(lobidUrl, inFormat,
		    RDFFormat.NTRIPLES, accept);
	    if (str.contains("http://www.w3.org/2002/07/owl#sameAs")) {
		str = RdfUtils.followSameAsAndInclude(lobidUrl, pid, inFormat,
			accept);
	    }
	    str = Pattern.compile(lobidUri).matcher(str)
		    .replaceAll(Matcher.quoteReplacement(pid))
		    + "<"
		    + pid
		    + "> <http://www.umbel.org/specifications/vocabulary#isLike> <"
		    + lobidUri + "> .";
	    File metadataFile = CopyUtils.copyStringToFile(str);
	    node.setMetadataFile(metadataFile.getAbsolutePath());
	    return node;
	} catch (MalformedURLException e) {
	    throw new HttpArchiveException(500, e);
	} catch (IOException e) {
	    throw new HttpArchiveException(500, e);
	}

    }

    @SuppressWarnings({ "serial", "javadoc" })
    public class MetadataNotFoundException extends RuntimeException {
	public MetadataNotFoundException(Throwable e) {
	    super(e);
	}
    }
}
