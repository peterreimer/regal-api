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

import java.util.List;
import java.util.Map;

import models.Node;
import play.cache.Cache;
import static archive.fedora.FedoraVocabulary.IS_PART_OF;

/**
 * @author Jan Schnasse
 *
 */
public class RegalAction {

    void updateIndexAndCache(Node node) {
	new Index().index(node);
	writeNodeToCache(node);
    }

    void updateIndexAndCacheWithParents(Node node) {
	new Index().index(node);
	writeNodeToCache(node);
	List<String> ps = node.getRelatives(IS_PART_OF);
	if (ps.isEmpty() || ps == null)
	    return;
	String parentPid = ps.get(0);
	invalidateParents(parentPid);
    }

    void invalidateParents(String parent) {
	play.Logger.debug("Fetch parent: " + parent);
	if (parent == null)
	    return;
	Node p = new Read().readNode(parent);
	updateIndexAndCacheWithParents(p);
    }

    Node readNodeFromCache(String pid) {
	play.Logger.debug("Read from cache: " + pid);
	return (Node) Cache.get(pid);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> readJsonCompactFromCache(String pid) {
	String key = createJsonCompactKey(pid);
	play.Logger.debug("Read from cache: " + key);
	return (Map<String, Object>) Cache.get(key);
    }

    void writeNodeToCache(Node node) {
	play.Logger.debug("Caching: " + node.getPid());
	Cache.set(node.getPid(), node);
    }

    void writeJsonCompactToCache(String pid, Map<String, Object> map) {
	String key = createJsonCompactKey(pid);
	map.put("primaryTopic", pid);
	play.Logger.debug("Caching: " + key);
	Cache.set(key, map);
    }

    private String createJsonCompactKey(String pid) {
	return pid + "-json-compact";
    }

    void removeFromCache(Node node) {
	Cache.remove(node.getPid());
	Cache.remove(createJsonCompactKey(node.getPid()));
	invalidateParents(node.getParentPid());
    }

}
