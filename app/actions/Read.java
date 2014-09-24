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
import helper.Globals;
import helper.HttpArchiveException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

import models.DublinCoreData;
import models.Node;

import org.elasticsearch.search.SearchHit;
import org.openrdf.rio.RDFFormat;

import archive.fedora.FedoraVocabulary;
import archive.fedora.RdfException;
import archive.fedora.RdfUtils;
import archive.fedora.UrlConnectionException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author Jan Schnasse
 *
 */
public class Read {

    /**
     * @param pid
     *            the will be read to the node
     * @return a Node containing the data from the repository
     */
    public Node readNode(String pid) {
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
     *            the pid of the node
     * @return a Map that represents the node
     */
    public Map<String, Object> readNodeFromIndex(String pid) {
	return Globals.search.get(pid);
    }

    /**
     * @param list
     *            a list of nodes to create a json like map for
     * @return a map with objects
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> nodelistToMap(List<Node> list) {
	try {

	    List<Map<String, Object>> map = new ArrayList<Map<String, Object>>();
	    for (Node node : list) {
		Map<String, Object> m = new ObjectMapper().readValue(
			new Transform().oaiore(node,
				"application/json+compact"), HashMap.class);
		m.put("primaryTopic", node.getPid());
		map.add(m);
	    }
	    return map;
	} catch (Exception e) {
	    throw new HttpArchiveException(500, e);
	}
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
	return ids.stream().map((String id) -> readNode(id))
		.collect(Collectors.toList());
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

    private List<String> listRepoNamespace(String namespace, int from, int until) {
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
	if (node != null)
	    return node.getDublinCoreData();
	return null;
    }

    /**
     * @param pid
     *            the pid of the object
     * @return n-triple metadata
     */
    public String readMetadata(String pid) {
	try {
	    Node node = readNode(pid);

	    return node.getMetadata();
	} catch (RdfException e) {
	    throw new HttpArchiveException(500, e);
	} catch (UrlConnectionException e) {
	    throw new HttpArchiveException(404, e);
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

    String createAggregationUri(String pid) {
	return Globals.useHttpUris ? "http://" + Globals.server + "/resource/"
		+ pid : pid;
    }

    /**
     * Returns an existing urn. Throws UrnException if found 0 urn or more than
     * 1 urns.
     * 
     * @param pid
     *            the pid of an object
     * @return the urn
     */
    public String getUrn(String pid) {
	try {
	    String newUrn = "http://purl.org/lobid/lv#urn";
	    List<String> urns = RdfUtils.findRdfObjects(pid, newUrn,
		    readMetadata(pid), RDFFormat.NTRIPLES, "text/plain");
	    if (urns == null || urns.isEmpty()) {
		throw new UrnException("Found no urn!");
	    }
	    if (urns.size() != 1) {
		throw new UrnException("Found " + urns.size() + " urns. "
			+ urns + "\n Expected exactly one urn.");
	    }
	    return urns.get(0);
	} catch (Exception e) {
	    throw new UrnException(e);
	}
    }

    @SuppressWarnings({ "serial" })
    private class UrnException extends RuntimeException {
	public UrnException(String arg0) {
	    super(arg0);
	}

	public UrnException(Throwable arg0) {
	    super(arg0);
	}

    }
}
