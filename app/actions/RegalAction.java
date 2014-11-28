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

import models.Globals;
import models.Node;
import play.cache.Cache;

/**
 * @author Jan Schnasse
 *
 */
public class RegalAction {

    void updateIndexAndCache(Node node) {
	new Index().index(node);
	writeNodeToCache(node);
    }

    Node readNodeFromCache(String pid) {
	return (Node) Cache.get(pid);
    }

    void writeNodeToCache(Node node) {
	Cache.set(node.getPid(), node);
    }

    void removeFromCache(Node node) {
	Cache.remove(node.getPid());
    }

    protected String createAggregationUri(String pid) {
	return Globals.useHttpUris ? "http://" + Globals.server + "/resource/"
		+ pid : pid;
    }

    /**
     * @param node
     *            the node
     * @return the http address of the resource
     */
    public String getHttpUriOfResource(Node node) {
	return Globals.useHttpUris ? node.getAggregationUri() : "http://"
		+ Globals.server + "/resource/" + node.getAggregationUri();
    }

    /**
     * @param nodes
     *            a list of nodes
     * @param action
     *            a action performed on each node
     * @return a message
     */
    public String apply(List<Node> nodes, ProcessNode action) {
	StringBuffer str = new StringBuffer();
	for (Node n : nodes) {
	    try {
		str.append("\n Updated " + action.process(n));
	    } catch (Exception e) {
		str.append("\n Not updated " + n.getPid() + " "
			+ e.getMessage());
	    }
	}
	str.append("\n");
	return str.toString();
    }
}
