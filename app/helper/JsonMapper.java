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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import akka.util.Collections;
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
    final static String fileLabel = "fileLabel";

    final static String[] typePrios = new String[] { "http://purl.org/lobid/lv#Biography",
	    "http://purl.org/library/Game", "http://purl.org/lobid/lv#Schoolbook",
	    "http://purl.org/ontology/mo/PublishedScore", "http://purl.org/lobid/lv#Legislation",
	    "http://purl.org/ontology/bibo/ReferenceSource", "http://purl.org/lobid/lv#OfficialPublication",
	    "http://purl.org/lobid/lv#Bibliography", "http://purl.org/lobid/lv#Festschrift",
	    "http://purl.org/ontology/bibo/Proceedings", "http://purl.org/lobid/lv#EditedVolume",
	    "http://purl.org/ontology/bibo/Thesis", "http://purl.org/lobid/lv#Miscellaneous",
	    "http://purl.org/ontology/mo/Record", "http://purl.org/ontology/bibo/Map",
	    "http://purl.org/ontology/bibo/AudioVisualDocument", "http://purl.org/ontology/bibo/AudioDocument",
	    "http://purl.org/ontology/bibo/Image", "http://purl.org/ontology/bibo/Article",
	    "http://rdvocab.info/termList/RDACarrierType/1018", "http://rdvocab.info/termList/RDACarrierType/1010",
	    "http://iflastandards.info/ns/isbd/terms/mediatype/T1002", "http://purl.org/ontology/bibo/MultiVolumeBook",
	    "http://purl.org/ontology/bibo/Journal", "http://purl.org/ontology/bibo/Newspaper",
	    "http://purl.org/ontology/bibo/Series", "http://purl.org/ontology/bibo/Periodical",
	    "http://purl.org/ontology/bibo/Collection", "http://purl.org/ontology/bibo/Book",
	    "http://data.archiveshub.ac.uk/def/ArchivalResource", "http://purl.org/ontology/bibo/Document",
	    "http://purl.org/vocab/frbr/core#Manifestation", "http://purl.org/dc/terms/BibliographicResource" };

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
	    if ("parentPid".equals(Globals.profile.getJsonName(l.getPredicate()))) {
		l.setPredicate("http://hbz-nrw.de/regal#externalParent");
	    }
	    addLinkToJsonMap(rdf, l);
	}
	addPartsToJsonMap(rdf);
	rdf.remove("isNodeType");

	rdf.put(contentType, node.getContentType());
	rdf.put(accessScheme, node.getAccessScheme());
	rdf.put(publishScheme, node.getPublishScheme());
	rdf.put(transformer, node.getTransformer().stream().map(t -> t.getId()).collect(Collectors.toList()));
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
	if (node.getUrn() != null) {
	    Link l = new Link();
	    l.setPredicate(Globals.profile.getUriFromJsonName("urn"));
	    l.setObject(node.getUrn());
	    l.setLiteral(true);
	    addLinkToJsonMap(rdf, l);
	}

	if (node.getParentPid() != null)
	    rdf.put(parentPid, node.getParentPid());

	if (node.getMimeType() != null && !node.getMimeType().isEmpty()) {
	    Map<String, Object> hasDataMap = new TreeMap<String, Object>();
	    hasDataMap.put("@id", node.getDataUri());
	    hasDataMap.put(format, node.getMimeType());
	    hasDataMap.put(size, node.getFileSize());
	    if (node.getFileLabel() != null)
		hasDataMap.put(fileLabel, node.getFileLabel());
	    if (node.getChecksum() != null) {
		Map<String, Object> checksumMap = new TreeMap<String, Object>();
		checksumMap.put(checksumValue, node.getChecksum());
		checksumMap.put(generator, "http://en.wikipedia.org/wiki/MD5");
		checksumMap.put(type, "http://downlode.org/Code/RDF/File_Properties/schema#Checksum");
		hasDataMap.put(checksum, checksumMap);
	    }
	    rdf.put(hasData, hasDataMap);
	}
	rdf.put("@context", Globals.profile.getContext().get("@context"));
	rdf.put(type, getType(rdf));
	sortCreatorAndContributors(rdf);
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
		checksumMap.put(type, "http://downlode.org/Code/RDF/File_Properties/schema#Checksum");
		hasDataMap.put(checksum, checksumMap);
	    }
	    rdf.put("hasData", hasDataMap);
	}
	rdf.put(type, getType(rdf));

	sortCreatorAndContributors(rdf);
	return rdf;
    }

    private void sortCreatorAndContributors(Map<String, Object> rdf) {
	List<Map<String, Object>> cr = getSortedListOfCreators(rdf);
	if (!cr.isEmpty()) {
	    rdf.put("creator", cr);
	    rdf.remove("creatorName");
	    rdf.remove("contributorName");
	}
	List<Map<String, Object>> co = getSortedListOfContributors(rdf);
	if (!co.isEmpty())
	    rdf.put("contributor", co);
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
	    List<Object> list = (List<Object>) rdf.get(profile.getJsonName(l.getPredicate()));
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

    private List<Object> getType(Map<String, Object> rdf) {
	@SuppressWarnings("unchecked")
	List<Object> result = new ArrayList<Object>();
	List<Map<String, Object>> types = (List<Map<String, Object>>) rdf.get(type);
	if (types != null) {
	    for (String s : typePrios) {
		for (Map<String, Object> m : types) {
		    if (s.equals(m.get("@id"))) {
			result.add(m);
			return result;
		    }
		}
	    }
	}
	result.add(noTypeMap());
	return result;
    }

    private Map<String, Object> noTypeMap() {
	Map<String, Object> noTypeMap = new HashMap<String, Object>();
	noTypeMap.put("@id", typePrios[typePrios.length - 1]);
	noTypeMap.put("pref", typePrios[typePrios.length - 1]);
	return noTypeMap;
    }

    private void addPartsToJsonMap(Map<String, Object> rdf) {
	for (Link l : node.getPartsSorted()) {
	    if (l.getObjectLabel() == null || l.getObjectLabel().isEmpty())
		l.setObjectLabel(l.getObject());
	    addLinkToJsonMap(rdf, l);
	}
    }

    public List<Map<String, Object>> getSortedListOfCreators(Map<String, Object> nodeAsMap) {
	List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
	List<String> carray = (List<String>) nodeAsMap.get("contributorOrder");
	if (carray == null || carray.isEmpty())
	    return result;
	for (String cstr : carray) {
	    String[] contributorOrdered = cstr.contains("|") ? cstr.split("\\|") : new String[] { cstr };
	    for (String s : contributorOrdered) {
		Map<String, Object> map = findCreator(nodeAsMap, s.trim());
		if (!map.isEmpty())
		    result.add(map);
	    }
	}
	return result;
    }

    public List<Map<String, Object>> getSortedListOfContributors(Map<String, Object> nodeAsMap) {
	List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
	List<String> carray = (List<String>) nodeAsMap.get("contributorOrder");
	if (carray == null || carray.isEmpty())
	    return result;
	for (String cstr : carray) {
	    String[] contributorOrdered = cstr.contains("|") ? cstr.split("\\|") : new String[] { cstr };
	    for (String s : contributorOrdered) {
		Map<String, Object> map = findContributor(nodeAsMap, s.trim());
		if (!map.isEmpty())
		    result.add(map);
	    }
	}
	return result;
    }

    private Map<String, Object> findCreator(Map<String, Object> m, String authorsId) {
	if (!authorsId.startsWith("http")) {
	    Map<String, Object> creatorWithoutId = new HashMap<String, Object>();
	    creatorWithoutId.put("prefLabel", authorsId);
	    creatorWithoutId.put("@id", Globals.protocol + Globals.server + "/authors/" + urlEncode(authorsId));
	    return creatorWithoutId;
	}
	List<Map<String, Object>> creators = (List<Map<String, Object>>) m.get("creator");
	if (creators != null) {
	    for (Map<String, Object> creator : creators) {
		String currentId = (String) creator.get("@id");
		if (authorsId.compareTo(currentId) == 0) {
		    return creator;
		}
	    }
	}
	return new HashMap<String, Object>();
    }

    private String urlEncode(String str) {
	try {
	    return URLEncoder.encode(str, "UTF-8");
	} catch (Exception e) {
	    throw new HttpArchiveException(500, e);
	}
    }

    private Map<String, Object> findContributor(Map<String, Object> m, String authorsId) {
	List<Map<String, Object>> contributors = (List<Map<String, Object>>) m.get("contributor");
	if (contributors != null) {
	    for (Map<String, Object> contributor : contributors) {
		String currentId = (String) contributor.get("@id");
		if (authorsId.compareTo(currentId) == 0) {
		    return contributor;
		}
	    }
	}
	return new HashMap<String, Object>();
    }

}
