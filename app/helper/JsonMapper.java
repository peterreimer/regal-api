/*
 * Copyright 2015 hbz NRW (http://www.hbz-nrw.de/)
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
package helper;

import static archive.fedora.FedoraVocabulary.HAS_PART;
import static archive.fedora.FedoraVocabulary.IS_PART_OF;
import static archive.fedora.Vocabulary.REL_HBZ_ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import models.Globals;
import models.Link;
import models.Node;
import archive.fedora.ApplicationProfile;
import archive.fedora.MapEntry;

/**
 * @author jan schnasse
 *
 */
public class JsonMapper {

    ApplicationProfile profile = Globals.profile;
    Node node = null;

    /**
     * @param node
     *            the node will be mapped to json ld in accordance to the
     *            profile
     */
    public JsonMapper(Node node) {
	this.node = node;
    }

    /**
     * @return a map without the context document
     */
    public Map<String, Object> getLdWithoutContext() {
	Map<String, Object> map = getLd();
	map.remove("@context");
	return map;

    }

    /**
     * @return a map without the context document
     */
    public Map<String, Object> getLdWithoutContextShortStyle() {
	Map<String, Object> map = getLdShortStyle();
	map.remove("@context");
	return map;
    }

    /**
     * @return a map representing the rdf data on this object
     */
    public Map<String, Object> getLd() {
	List<Link> ls = node.getLinks();
	Map<String, Object> rdf = new TreeMap<String, Object>();
	rdf.put("@id", node.getPid());
	rdf.put("primaryTopic", node.getPid());
	for (Link l : ls) {
	    if (HAS_PART.equals(l.getPredicate()))
		continue;
	    if (REL_HBZ_ID.equals(l.getPredicate()))
		continue;
	    if (IS_PART_OF.equals(l.getPredicate()))
		continue;
	    addLinkToJsonMap(rdf, l);
	}
	addPartsToJsonMap(rdf);
	rdf.remove("isNodeType");

	rdf.put("contentType", node.getContentType());
	rdf.put("accessScheme", node.getAccessScheme());
	rdf.put("publishScheme", node.getPublishScheme());
	rdf.put("transformer",
		node.getTransformer().stream().map(t -> t.getId())
			.collect(Collectors.toList()));
	rdf.put("catalogId", node.getCatalogId());

	if (node.getFulltext() != null)
	    rdf.put("fulltext-ocr", node.getFulltext());

	Map<String, Object> aboutMap = new TreeMap<String, Object>();
	aboutMap.put("@id", node.getAggregationUri() + ".rdf");
	if (node.getCreatedBy() != null)
	    aboutMap.put("createdBy", node.getCreatedBy());
	if (node.getLegacyId() != null)
	    aboutMap.put("legacyId", node.getLegacyId());
	if (node.getImportedFrom() != null)
	    aboutMap.put("importedFrom", node.getImportedFrom());
	if (node.getName() != null)
	    aboutMap.put("name", node.getName());
	if (node.getUrn() != null)
	    aboutMap.put("urn", node.getUrn());
	if (node.getLastModifiedBy() != null)
	    aboutMap.put("lastModifiedBy", node.getLastModifiedBy());
	aboutMap.put("modified", node.getLastModified());
	if (node.getObjectTimestamp() != null) {
	    aboutMap.put("objectTimestamp", node.getObjectTimestamp());
	} else {
	    aboutMap.put("objectTimestamp", node.getLastModified());
	}
	aboutMap.put("created", node.getCreationDate());
	aboutMap.put("describes", node.getAggregationUri());
	rdf.put("isDescribedBy", aboutMap);
	if (node.getDoi() != null)
	    rdf.put("doi", node.getDoi());
	if (node.getParentPid() != null)
	    rdf.put("parentPid", node.getParentPid());

	if (node.getMimeType() != null && !node.getMimeType().isEmpty()) {
	    Map<String, Object> hasDataMap = new TreeMap<String, Object>();
	    hasDataMap.put("@id", node.getDataUri());
	    hasDataMap.put("format", node.getMimeType());
	    hasDataMap.put("size", node.getFileSize());
	    if (node.getChecksum() != null) {
		Map<String, Object> checksum = new TreeMap<String, Object>();
		checksum.put("checksumValue", node.getChecksum());
		checksum.put("generator", "http://en.wikipedia.org/wiki/MD5");
		checksum.put("type",
			"http://downlode.org/Code/RDF/File_Properties/schema#Checksum");
		hasDataMap.put("checksum", checksum);
	    }
	    rdf.put("hasData", hasDataMap);
	}

	rdf.put("@context", getContext());
	return rdf;
    }

    /**
     * @return linked data json optimized for displaying large trees. Most of
     *         the metadata has been left out.
     */
    public Map<String, Object> getLdShortStyle() {
	List<Link> ls = node.getLinks();
	Map<String, Object> rdf = new HashMap<String, Object>();
	rdf.put("@id", node.getPid());
	for (Link l : ls) {
	    if (profile.nMap.get("title").uri.equals(l.getPredicate())) {
		addLinkToJsonMap(rdf, l);
		break;
	    }
	}
	addPartsToJsonMap(rdf);
	rdf.remove("isNodeType");
	rdf.put("contentType", node.getContentType());
	if (node.getParentPid() != null)
	    rdf.put("parentPid", node.getParentPid());
	if (node.getMimeType() != null && !node.getMimeType().isEmpty()) {
	    Map<String, Object> hasDataMap = new HashMap<String, Object>();
	    hasDataMap.put("@id", node.getDataUri());
	    hasDataMap.put("format", node.getMimeType());
	    hasDataMap.put("size", node.getFileSize());
	    if (node.getChecksum() != null) {
		Map<String, Object> checksum = new HashMap<String, Object>();
		checksum.put("checksumValue", node.getChecksum());
		checksum.put("generator", "http://en.wikipedia.org/wiki/MD5");
		checksum.put("type",
			"http://downlode.org/Code/RDF/File_Properties/schema#Checksum");
		hasDataMap.put("checksum", checksum);
	    }
	    rdf.put("hasData", hasDataMap);
	}
	return rdf;
    }

    private void addLinkToJsonMap(Map<String, Object> rdf, Link l) {

	Map<String, Object> resolvedObject = null;
	if (l.getObjectLabel() != null) {
	    String id = l.getObject();
	    String value = l.getObjectLabel();
	    resolvedObject = new HashMap<String, Object>();
	    resolvedObject.put("@id", id);
	    resolvedObject.put("prefLabel", value);
	}
	if (rdf.containsKey(getShortName(l))) {
	    @SuppressWarnings("unchecked")
	    List<Object> list = (List<Object>) rdf.get(getShortName(l));
	    if (resolvedObject == null) {
		list.add(l.getObject());
	    } else {
		list.add(resolvedObject);
	    }
	} else {
	    List<Object> list = new ArrayList<Object>();
	    if (resolvedObject == null) {
		list.add(l.getObject());
	    } else {
		list.add(resolvedObject);
	    }
	    rdf.put(getShortName(l), list);
	}
    }

    private void addPartsToJsonMap(Map<String, Object> rdf) {
	for (Link l : node.getPartsSorted()) {
	    if (l.getObjectLabel() == null || l.getObjectLabel().isEmpty())
		l.setObjectLabel(l.getObject());
	    // l.setPredicate(Globals.profile.nMap.get("hasPart").uri);
	    addLinkToJsonMap(rdf, l);
	}
    }

    /**
     * @return a Map representing additional information about the shortnames
     *         used in getLd
     */
    public Map<String, Object> getContext() {
	Map<String, Object> pmap;
	Map<String, Object> cmap = new HashMap<String, Object>();
	for (String key : Globals.profile.pMap.keySet()) {
	    MapEntry e = Globals.profile.pMap.get(key);
	    pmap = new HashMap<String, Object>();
	    pmap.put("@id", e.uri);
	    pmap.put("label", e.label);
	    pmap.put("@type", "@id");
	    if (e.name != null) {
		cmap.put(e.name, pmap);
	    }
	}
	return cmap;
    }

    /**
     * @return The short name of the predicate uses String.split on first index
     *         of '#' or last index of '/'
     */
    private String getShortName(Link l) {
	String result = null;
	String predicate = l.getPredicate();
	MapEntry e = profile.pMap.get(predicate);
	if (e != null) {
	    result = e.name;
	}
	if (result == null || result.isEmpty()) {
	    String prefix = "";
	    if (predicate.startsWith("http://purl.org/dc/elements"))
		prefix = "dc:";
	    if (predicate.contains("#"))
		return prefix + predicate.split("#")[1];
	    else if (predicate.startsWith("http")) {
		int i = predicate.lastIndexOf("/");
		return prefix + predicate.substring(i + 1);
	    }
	    result = prefix + predicate;
	}
	return result;
    }

}
