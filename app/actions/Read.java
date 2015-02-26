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

import static archive.fedora.Vocabulary.REL_CONTENT_TYPE;
import static archive.fedora.Vocabulary.REL_IS_NODE_TYPE;
import static archive.fedora.Vocabulary.TYPE_OBJECT;
import static archive.fedora.FedoraVocabulary.HAS_PART;
import static archive.fedora.FedoraVocabulary.IS_PART_OF;
import helper.HttpArchiveException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

import models.DublinCoreData;
import models.Globals;
import models.Link;
import models.Node;
import models.Urn;

import org.elasticsearch.search.SearchHit;
import org.openrdf.rio.RDFFormat;

import play.Logger;
import archive.fedora.FedoraVocabulary;
import archive.fedora.RdfUtils;
import archive.fedora.UrlConnectionException;

/**
 * 
 * @author Jan Schnasse
 *
 */
public class Read extends RegalAction {

    /**
     * @param pid
     *            the will be read to the node
     * @return a Node containing the data from the repository
     */
    public Node readNode(String pid) {
	Node n = internalReadNode(pid);
	addLabelsForParts(n);
	writeNodeToCache(n);
	return n;
    }

    /**
     * @param pid
     *            the will be read to the node
     * @return a Node containing the data from the repository
     */
    private Node internalReadNode(String pid) {
	Node n = Globals.fedora.readNode(pid);
	n.setAggregationUri(createAggregationUri(n.getPid()));
	n.setRemUri(n.getAggregationUri() + ".rdf");
	n.setDataUri(n.getAggregationUri() + "/data");
	n.setContextDocumentUri("http://" + Globals.server
		+ "/public/edoweb-resources.json");
	return n;
    }

    /**
     * @param pid
     *            the will be read to the node
     * @return a Node containing the data from the repository
     */
    public Node readCachedNode(String pid) {
	Node c = readNodeFromCache(pid);
	if (c == null) {
	    return readNode(pid);
	}
	// addLabelsForParts(c);
	return c;
    }

    void addLabelsForParts(Node n) {
	List<Link> rels = n.getRelsExt();
	for (Link l : rels) {
	    if (HAS_PART.equals(l.getPredicate())
		    || IS_PART_OF.equals(l.getPredicate())) {
		addLabel(n, l);
	    }
	}
    }

    private void addLabel(Node n, Link l) {
	try {
	    String label = readMetadata(l.getObject(), "title");
	    l.setObjectLabel(label);
	    n.removeRelation(l.getPredicate(), l.getObject());
	    n.addRelation(l);
	} catch (Exception e) {

	}
    }

    /**
     * @param pid
     *            the pid of the node
     * @return a Map that represents the node
     */
    public Map<String, Object> readNodeFromIndex(String pid) {
	return Globals.search.get(pid);
    }

    /**
     * @param node
     * @return all parts and their parts recursively
     */
    public List<Node> getParts(Node node) {
	List<Node> result = new ArrayList<Node>();
	result.add(node);
	List<Node> parts = getNodesFromCache(node.getRelatives(HAS_PART)
		.stream().map((Link l) -> l.getObject())
		.collect(Collectors.toList()));
	for (Node p : parts) {
	    result.addAll(getParts(p));
	}
	return result;
    }

    /**
     * @param list
     *            a list of nodes to create a json like map for
     * @return a map with objects
     */
    public List<Map<String, Object>> hitlistToMap(List<SearchHit> list) {
	List<Map<String, Object>> map = new ArrayList<Map<String, Object>>();
	for (SearchHit hit : list) {
	    Map<String, Object> m = hit.getSource();
	    m.put("primaryTopic", hit.getId());
	    map.add(m);
	}
	return map;
    }

    /**
     * @param type
     *            The objectTyp
     * @param namespace
     *            list only objects in this namespace
     * @param from
     *            show only hits starting at this index
     * @param until
     *            show only hits ending at this index
     * @return A list of pids with type {@type}
     */
    public List<SearchHit> listSearch(String type, String namespace, int from,
	    int until) {
	return Arrays.asList(Globals.search.list(namespace, type, from, until)
		.getHits());
    }

    /**
     * @param type
     *            The objectTyp
     * @param namespace
     *            list only objects in this namespace
     * @param from
     *            show only hits starting at this index
     * @param until
     *            show only hits ending at this index
     * @return a list of nodes
     */
    public List<Node> listRepo(String type, String namespace, int from,
	    int until) {
	List<String> list = null;
	if (from < 0 || until <= from) {
	    throw new HttpArchiveException(316,
		    "until and from not sensible. choose a valid range, please.");
	} else if (type == null || type.isEmpty() && namespace != null
		&& !namespace.isEmpty()) {
	    return getNodes(listRepoNamespace(namespace, from, until));
	} else if (namespace == null || namespace.isEmpty() && type != null
		&& !type.isEmpty()) {
	    return getNodes(listRepoType(type, from, until));
	} else if ((namespace == null || namespace.isEmpty())
		&& (type == null || type.isEmpty())) {
	    list = listRepoAll();
	} else {
	    list = listRepo(type, namespace);
	}
	return getNodes(sublist(list, from, until));
    }

    /**
     * @param ids
     *            a list of ids to get objects for
     * @return a list of nodes
     */
    public List<Node> getNodes(List<String> ids) {
	return ids.stream().map((String id) -> {
	    try {
		return readNode(id);
	    } catch (Exception e) {
		Logger.error("" + id, e);
		return new Node(id);
	    }
	}).filter(n -> n != null).collect(Collectors.toList());
    }

    /**
     * @param ids
     *            a list of ids to get objects for
     * @return a list of nodes
     */
    public List<Node> getNodesFromCache(List<String> ids) {
	return ids.stream().map((String id) -> {
	    try {
		return readCachedNode(id);
	    } catch (Exception e) {
		Logger.warn("", e);
		return null;
	    }
	}).filter(n -> n != null).collect(Collectors.toList());
    }

    /**
     * @param ids
     *            a list of ids to get objects for
     * @return a list of Maps each represents a node
     */
    public List<Map<String, Object>> getNodesFromIndex(List<String> ids) {
	return ids.stream().map((String id) -> readNodeFromIndex(id))
		.collect(Collectors.toList());
    }

    private List<String> listRepo(String type, String namespace) {
	List<String> result = new ArrayList<String>();
	List<String> typedList = listRepoType(type);
	if (namespace != null && !namespace.isEmpty()) {
	    for (String item : typedList) {
		if (item.startsWith(namespace + ":")) {
		    result.add(item);
		}
	    }
	    return result;
	} else {
	    return typedList;
	}
    }

    private List<String> listRepoType(String type) {
	List<String> typedList;
	String query = "* <" + REL_CONTENT_TYPE + "> \"" + type + "\"";
	InputStream in = Globals.fedora.findTriples(query,
		FedoraVocabulary.SPO, FedoraVocabulary.N3);
	typedList = RdfUtils.getFedoraSubject(in);

	return typedList;
    }

    private List<String> listRepoAll() {
	List<String> typedList;
	String query = "* <" + REL_IS_NODE_TYPE + "> <" + TYPE_OBJECT + ">";
	InputStream in = Globals.fedora.findTriples(query,
		FedoraVocabulary.SPO, FedoraVocabulary.N3);
	typedList = RdfUtils.getFedoraSubject(in);
	return typedList;
    }

    private List<String> listRepoType(String type, int from, int until) {
	List<String> list = listRepoType(type);
	return sublist(list, from, until);
    }

    /**
     * List all pids within a namespace
     * 
     * @param namespace
     *            a valid namespace
     * @return a list of pids
     */
    public List<String> listRepoNamespace(String namespace) {
	return listByQuery(namespace + ":*");
    }

    /**
     * @param namespace
     *            list only objects in this namespace
     * @param from
     *            show only hits starting at this index
     * @param until
     *            show only hits ending at this index
     * @return a list of nodes
     */
    public List<String> listRepoNamespace(String namespace, int from, int until) {
	List<String> list = listRepoNamespace(namespace);
	return sublist(list, from, until);
    }

    private List<String> listByQuery(String query) {
	List<String> objects = null;
	objects = Globals.fedora.findNodes(query);
	return objects;
    }

    private List<String> sublist(List<String> list, int from, int until) {
	if (from >= list.size()) {
	    return new Vector<String>();
	}
	if (until < list.size()) {
	    return list.subList(from, until);
	} else {
	    return list.subList(from, list.size());
	}
    }

    /**
     * @param pid
     *            The pid to read the dublin core stream from.
     * @return A DCBeanAnnotated java object.
     */
    public DublinCoreData readDC(String pid) {
	Node node = readNode(pid);
	String uri = getHttpUriOfResource(node);
	if (node != null)
	    return node.getDublinCoreData().addIdentifier(uri);
	return null;
    }

    /**
     * @param pid
     *            the pid of the object
     * @param field
     *            if field is specified, only the value of a certain field will
     *            be returned
     * @return n-triple metadata
     */
    public String readMetadata(String pid, String field) {
	try {
	    Node node = internalReadNode(pid);
	    String metadata = node.getMetadata();
	    if (field == null || field.isEmpty()) {
		return metadata;
	    } else {
		String pred = Globals.profile.nMap.get(field).uri;
		List<String> value = RdfUtils.findRdfObjects(pid, pred,
			metadata, RDFFormat.NTRIPLES);

		return value.isEmpty() ? "No " + field : value.get(0);
	    }
	} catch (UrlConnectionException e) {
	    throw new HttpArchiveException(404, e);
	} catch (Exception e) {
	    throw new HttpArchiveException(500, e);
	}
    }

    /**
     * @param pid
     *            the pid of the object
     * @param field
     *            if field is specified, only a certain field of the node's
     *            metadata will be returned
     * @return n-triple metadata
     */
    public String readMetadataFromCache(String pid, String field) {
	try {
	    Node node = readCachedNode(pid);
	    String metadata = node.getMetadata();
	    if (metadata == null || metadata.isEmpty())
		throw new HttpArchiveException(404, "No Metadata on " + pid
			+ " available!");
	    if (field == null || field.isEmpty()) {
		return metadata;
	    } else {
		String pred = Globals.profile.nMap.get(field).uri;
		List<String> value = RdfUtils.findRdfObjects(pid, pred,
			metadata, RDFFormat.NTRIPLES);

		return value.isEmpty() ? "No " + field : value.get(0);
	    }
	} catch (UrlConnectionException e) {
	    throw new HttpArchiveException(404, e);
	} catch (Exception e) {
	    throw new HttpArchiveException(500, e);
	}
    }

    /**
     * @param node
     *            the pid of the object
     * @return ordered json array of parts
     */
    public String readSeq(Node node) {
	try {
	    return node.getSeq();
	} catch (UrlConnectionException e) {
	    throw new HttpArchiveException(404, e);
	} catch (Exception e) {
	    throw new HttpArchiveException(500, e);
	}
    }

    /**
     * @param pid
     *            the pid
     * @return the last modified date
     */
    public Date getLastModified(String pid) {
	Node node = readNode(pid);
	return node.getLastModified();
    }

    /**
     * @param node
     *            the node to read a urn from
     * @return a urn object that describes the status of the urn
     */
    public Urn getUrnStatus(Node node) {
	String urn = getUrn(node);
	Urn result = new Urn(urn);
	result.init(Globals.urnbase + node.getPid());
	return result;
    }

    String getUrn(Node node) {
	List<String> urns = getNodeLdProperty(node,
		"http://purl.org/lobid/lv#urn");
	if (urns == null || urns.isEmpty()) {
	    throw new HttpArchiveException(500, "No urn fount at: "
		    + node.getPid());
	}
	if (urns.size() > 1) {
	    throw new HttpArchiveException(500, "Found multiple urns at: "
		    + node.getPid());
	}
	return urns.get(0);
    }

    /**
     * @param node
     *            the node to fetch a certain property from
     * @param predicate
     *            the property in its long form
     * @return all objects that are referenced using the predicate
     */
    public List<String> getNodeLdProperty(Node node, String predicate) {
	List<String> linkedObjects = RdfUtils.findRdfObjects(node.getPid(),
		predicate, node.getMetadata(), RDFFormat.NTRIPLES);
	return linkedObjects;
    }

}
