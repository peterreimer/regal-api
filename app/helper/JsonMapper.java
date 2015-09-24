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

/**
 * @author jan schnasse
 *
 */
public class JsonMapper {

    /**
     * Here are some short names that must be defined in the context document
     * that is loaded at Globals.context.
     */
    final static String primaryTopic = "primaryTopic";
    final static String contentType = "contentType";
    final static String accessScheme = "accessScheme";
    final static String publishScheme = "publishScheme";
    final static String transformer = "transformer";
    final static String catalogId = "catalogId";
    final static String createdBy = "createdBy";
    final static String legacyId = "legacyId";
    final static String importedFrom = "importedFrom";
    final static String name = "name";
    final static String urn = "urn";
    final static String lastModifiedBy = "lastModifiedBy";
    final static String modified = "modified";
    final static String objectTimestamp = "objectTimestamp";
    final static String created = "created";
    final static String describes = "describes";
    final static String isDescribedBy = "isDescribedBy";
    final static String doi = "doi";
    final static String parentPid = "parentPid";
    final static String format = "format";
    final static String size = "size";
    final static String checksumValue = "checksumValue";
    final static String generator = "generator";
    final static String type = "type";
    final static String checksum = "checksum";
    final static String hasData = "hasData";
    final static String fulltext_ocr = "fulltext-ocr";
    final static String title = "title";

    EtikettMaker profile = Globals.profile;

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
	rdf.put(primaryTopic, node.getPid());
	for (Link l : ls) {
	    if (HAS_PART.equals(l.getPredicate()))
		continue;
	    if (REL_HBZ_ID.equals(l.getPredicate()))
		continue;
	    if (IS_PART_OF.equals(l.getPredicate()))
		continue;
	    if ("parentPid"
		    .equals(Globals.profile.getJsonName(l.getPredicate()))) {
		l.setPredicate("http://hbz-nrw.de/regal#externalParent");
	    }
	    addLinkToJsonMap(rdf, l);
	}
	addPartsToJsonMap(rdf);
	rdf.remove("isNodeType");

	rdf.put(contentType, node.getContentType());
	rdf.put(accessScheme, node.getAccessScheme());
	rdf.put(publishScheme, node.getPublishScheme());
	rdf.put(transformer, node.getTransformer().stream().map(t -> t.getId())
		.collect(Collectors.toList()));
	rdf.put(catalogId, node.getCatalogId());

	if (node.getFulltext() != null)
	    rdf.put(fulltext_ocr, node.getFulltext());

	Map<String, Object> aboutMap = new TreeMap<String, Object>();
	aboutMap.put("@id", node.getAggregationUri() + ".rdf");
	if (node.getCreatedBy() != null)
	    aboutMap.put(createdBy, node.getCreatedBy());
	if (node.getLegacyId() != null)
	    aboutMap.put(legacyId, node.getLegacyId());
	if (node.getImportedFrom() != null)
	    aboutMap.put(importedFrom, node.getImportedFrom());
	if (node.getName() != null)
	    aboutMap.put(name, node.getName());
	if (node.getUrn() != null)
	    aboutMap.put(urn, node.getUrn());
	if (node.getLastModifiedBy() != null)
	    aboutMap.put(lastModifiedBy, node.getLastModifiedBy());
	aboutMap.put(modified, node.getLastModified());
	if (node.getObjectTimestamp() != null) {
	    aboutMap.put(objectTimestamp, node.getObjectTimestamp());
	} else {
	    aboutMap.put(objectTimestamp, node.getLastModified());
	}
	aboutMap.put(created, node.getCreationDate());
	aboutMap.put(describes, node.getAggregationUri());
	rdf.put(isDescribedBy, aboutMap);
	if (node.getDoi() != null)
	    rdf.put(doi, node.getDoi());
	if (node.getParentPid() != null)
	    rdf.put(parentPid, node.getParentPid());

	if (node.getMimeType() != null && !node.getMimeType().isEmpty()) {
	    Map<String, Object> hasDataMap = new TreeMap<String, Object>();
	    hasDataMap.put("@id", node.getDataUri());
	    hasDataMap.put(format, node.getMimeType());
	    hasDataMap.put(size, node.getFileSize());
	    if (node.getChecksum() != null) {
		Map<String, Object> checksumMap = new TreeMap<String, Object>();
		checksumMap.put(checksumValue, node.getChecksum());
		checksumMap.put(generator, "http://en.wikipedia.org/wiki/MD5");
		checksumMap
			.put(type,
				"http://downlode.org/Code/RDF/File_Properties/schema#Checksum");
		hasDataMap.put(checksum, checksumMap);
	    }
	    rdf.put(hasData, hasDataMap);
	}
	rdf.put("@context", Globals.profile.getContext().get("@context"));
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
	    if (profile.getUriFromJsonName(title).equals(l.getPredicate())) {
		addLinkToJsonMap(rdf, l);
		break;
	    }
	}
	addPartsToJsonMap(rdf);
	rdf.remove("isNodeType");
	rdf.put(contentType, node.getContentType());
	if (node.getParentPid() != null)
	    rdf.put(parentPid, node.getParentPid());
	if (node.getMimeType() != null && !node.getMimeType().isEmpty()) {
	    Map<String, Object> hasDataMap = new HashMap<String, Object>();
	    hasDataMap.put("@id", node.getDataUri());
	    hasDataMap.put(format, node.getMimeType());
	    hasDataMap.put(size, node.getFileSize());
	    if (node.getChecksum() != null) {
		Map<String, Object> checksumMap = new HashMap<String, Object>();
		checksumMap.put(checksumValue, node.getChecksum());
		checksumMap.put(generator, "http://en.wikipedia.org/wiki/MD5");
		checksumMap
			.put(type,
				"http://downlode.org/Code/RDF/File_Properties/schema#Checksum");
		hasDataMap.put(checksum, checksumMap);
	    }
	    rdf.put("hasData", hasDataMap);
	}
	return rdf;
    }

    private void addLinkToJsonMap(Map<String, Object> rdf, Link l) {

	Map<String, Object> resolvedObject = null;

	String id = l.getObject();
	String value = l.getObjectLabel();
	String jsonName = profile.getJsonName(l.getPredicate());
	if (l.getObjectLabel() != null) {
	    resolvedObject = new HashMap<String, Object>();
	    resolvedObject.put("@id", id);
	    resolvedObject.put("prefLabel", value);
	}
	if (rdf.containsKey(jsonName)) {
	    @SuppressWarnings("unchecked")
	    List<Object> list = (List<Object>) rdf.get(profile.getJsonName(l
		    .getPredicate()));
	    if (resolvedObject == null) {
		if (l.isLiteral()) {
		    list.add(l.getObject());
		} else {
		    resolvedObject = new HashMap<String, Object>();
		    resolvedObject.put("@id", id);
		    resolvedObject.put("prefLabel", id);
		    list.add(resolvedObject);
		}
	    } else {
		list.add(resolvedObject);
	    }
	} else {
	    List<Object> list = new ArrayList<Object>();
	    if (resolvedObject == null) {
		if (l.isLiteral()) {
		    list.add(l.getObject());
		} else {
		    resolvedObject = new HashMap<String, Object>();
		    resolvedObject.put("@id", id);
		    resolvedObject.put("prefLabel", id);
		    list.add(resolvedObject);
		}
	    } else {
		list.add(resolvedObject);
	    }
	    rdf.put(profile.getJsonName(l.getPredicate()), list);
	}
    }

    private void addPartsToJsonMap(Map<String, Object> rdf) {
	for (Link l : node.getPartsSorted()) {
	    if (l.getObjectLabel() == null || l.getObjectLabel().isEmpty())
		l.setObjectLabel(l.getObject());
	    addLinkToJsonMap(rdf, l);
	}
    }

}
