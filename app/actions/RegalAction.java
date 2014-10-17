package actions;

import java.util.List;
import java.util.Map;

import models.Node;
import play.cache.Cache;
import static archive.fedora.FedoraVocabulary.IS_PART_OF;

public class RegalAction {

    void updateIndexAndCache(Node node) {
	new Index().index(node);
	writeNodeToCache(node);
	try {
	    writeJsonCompactToCache(node.getPid(),
		    new Transform().getOaiOreJsonMap(node));
	} catch (Exception e) {
	    Cache.remove(createJsonCompactKey(node.getPid()));
	}
    }

    void updateIndexAndCacheWithParents(Node node) {
	new Index().index(node);
	writeNodeToCache(node);
	try {
	    writeJsonCompactToCache(node.getPid(),
		    new Transform().getOaiOreJsonMap(node));
	} catch (Exception e) {
	    Cache.remove(createJsonCompactKey(node.getPid()));
	}
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
