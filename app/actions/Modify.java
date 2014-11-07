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

import models.DublinCoreData;
import models.Link;
import models.Node;
import models.Pair;
import models.Transformer;

import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;

import play.mvc.Results.Chunks;
import archive.fedora.CopyUtils;
import archive.fedora.RdfException;
import archive.fedora.RdfUtils;

/**
 * @author Jan Schnasse
 *
 */
public class Modify extends RegalAction {
    Chunks.Out<String> messageOut;

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
	Node node = new Read().readNode(pid);
	if (node != null) {
	    node.setUploadData(tmp.getAbsolutePath(), mimeType);
	    node.setFileLabel(name);
	    node.setMimeType(mimeType);
	    Globals.fedora.updateNode(node);
	} else {
	    throw new HttpArchiveException(500, "Lost Node!");
	}
	new Index().index(node);
	if (md5Hash != null && !md5Hash.isEmpty()) {
	    node = new Read().readNode(pid);
	    String fedoraHash = node.getChecksum();
	    if (!md5Hash.equals(fedoraHash)) {
		throw new HttpArchiveException(417, pid + " expected a MD5 of "
			+ fedoraHash + " but you provided a MD5 value of "
			+ md5Hash);
	    }
	}
	updateIndexAndCache(node);
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
	Node node = new Read().readNode(pid);
	node.setDublinCoreData(dc);
	Globals.fedora.updateNode(node);
	updateIndexAndCache(node);
	return pid + " dc successfully updated!";
    }

    /**
     * @param pid
     *            the node's pid
     * @param content
     *            a json array to provide ordering information about the
     *            object's children
     * @return a message
     */
    public String updateSeq(String pid, String content) {
	try {
	    if (content == null) {
		throw new HttpArchiveException(406, pid
			+ " You've tried to upload an empty string."
			+ " This action is not supported."
			+ " Use HTTP DELETE instead.\n");
	    }
	    // RdfUtils.validate(content);
	    play.Logger.info(content);
	    File file = CopyUtils.copyStringToFile(content);
	    Node node = new Read().readNode(pid);
	    if (node != null) {
		node.setSeqFile(file.getAbsolutePath());
		Globals.fedora.updateNode(node);
	    }
	    updateIndexAndCache(node);
	    return pid + " sequence of child objects updated!";
	} catch (RdfException e) {
	    throw new HttpArchiveException(400, e);
	} catch (IOException e) {
	    throw new UpdateNodeException(e);
	}
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
			+ " You've tried to upload an empty string."
			+ " This action is not supported."
			+ " Use HTTP DELETE instead.\n");
	    }
	    RdfUtils.validate(content);
	    File file = CopyUtils.copyStringToFile(content);
	    Node node = new Read().readNode(pid);
	    if (node != null) {
		node.setMetadataFile(file.getAbsolutePath());
		Globals.fedora.updateNode(node);
	    }
	    updateIndexAndCache(new Read().readNode(pid));
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
	updateIndexAndCache(node);
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
	Node node = new Read().readNode(pid);
	for (Link link : links) {
	    node.addRelation(link);
	}
	Globals.fedora.updateNode(node);
	updateIndexAndCache(node);
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
	String metadata = new Read().readMetadata(subject);
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
	Node node = new Read().readNode(pid);
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
	String metadata = new Read().readMetadata(subject);
	if (RdfUtils.hasTriple(subject, hasUrn, metadata))
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
	Node node = new Read().readNode(pid);
	node = lobidify(node);
	updateIndexAndCache(node);
	return updateMetadata(node);
    }

    /**
     * @param node
     *            the node to be published on the oai interface
     * @return A short message.
     */
    public String makeOAISet(Node node) {
	try {
	    String pid = node.getPid();
	    OaiSetBuilder oaiSetBuilder = new OaiSetBuilder();
	    RepositoryResult<Statement> statements = RdfUtils.getStatements(
		    node.getMetadata(), "fedora:info/");
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

	    if ("public".equals(node.getAccessScheme())) {
		addSet(node, "open_access");
	    }
	    if (node.hasUrn()) {
		addSet(node, "epicur");
		String urn = node.getUrn();
		if (urn.startsWith("urn:nbn:de:hbz:929:01")) {
		    addSet(node, "urn-set-1");
		} else if (urn.startsWith("urn:nbn:de:hbz:929:02")) {
		    addSet(node, "urn-set-2");
		}
	    }
	    if (node.hasLinkToCatalogId()) {
		play.Logger.info(node.getPid() + " add aleph set!");
		addSet(node, "aleph");
	    }
	    updateIndexAndCache(node);
	    return pid + " successfully created oai sets!";
	} catch (Exception e) {
	    throw new MetadataNotFoundException(e);
	}

    }

    private void addSet(Node node, String name) {
	String spec = name;
	String namespace = "oai";
	String oaipid = namespace + ":" + name;
	if (!Globals.fedora.nodeExists(oaipid)) {
	    createOAISet(name, spec, oaipid);
	}
	linkObjectToOaiSet(node, spec, oaipid);
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
	link.setObject("info:fedora/" + node.getPid(), false);
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
	List<Pair<String, String>> identifier = node.getDublinCoreData()
		.getIdentifier();
	String alephid = "";
	for (Pair<String, String> id : identifier) {
	    if (id.getLeft().startsWith("TT") || id.getLeft().startsWith("HT")) {
		alephid = id.getLeft();
		break;
	    }
	}
	if (alephid.isEmpty()) {
	    throw new HttpArchiveException(500, pid + " no Catalog-Id found");
	}
	String lobidUri = "http://lobid.org/resource/" + alephid;
	try {
	    URL lobidUrl = new URL("http://lobid.org/resource/" + alephid
		    + "/about");
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

    /**
     * @param indexName
     */
    public void reinitOaisets(String namespace) {
	try {
	    Read read = new Read();
	    int until = 0;
	    int stepSize = 100;
	    int from = 0 - stepSize;
	    List<String> nodes = read.listRepoNamespace(namespace);
	    messageOut.write(nodes.toString());
	    messageOut.write("size: " + nodes.size());
	    do {
		until += stepSize;
		from += stepSize;
		if (nodes.isEmpty())
		    break;
		if (until > nodes.size())
		    until = nodes.size();
		messageOut.write(reinitOaiSets(read.getNodes(nodes.subList(
			from, until))));
	    } while (until < nodes.size());
	    messageOut.write("Attempted to index: " + nodes.size());
	    messageOut.write("\nSuccessfuly Finished\n");
	} finally {
	    messageOut.close();
	}
    }

    private String reinitOaiSets(List<Node> nodes) {
	StringBuffer str = new StringBuffer();
	for (Node n : nodes) {
	    try {
		str.append("\n" + makeOAISet(n));
	    } catch (MetadataNotFoundException e) {
		str.append("\nProblems with " + n.getPid() + "\n"
			+ e.getMessage());
	    }
	}
	return str.toString();
    }

    /**
     * @param out
     *            messages for chunked responses
     */
    public void setMessageQueue(Chunks.Out<String> out) {
	messageOut = out;
    }

    /**
     * Close messageQueue for chunked responses
     * 
     */
    public void closeMessageQueue() {
	if (messageOut != null)
	    messageOut.close();
    }

    @SuppressWarnings({ "serial", "javadoc" })
    public class MetadataNotFoundException extends RuntimeException {
	public MetadataNotFoundException(Throwable e) {
	    super(e);
	}
    }
}
