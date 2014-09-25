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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import helper.Globals;
import models.Node;

/**
 * @author Jan Schnasse
 *
 */
public class Index {

    /**
     * @param index
     *            the elasticsearch index
     * @param type
     *            the type of the resource
     * @param pid
     *            The namespaced pid to remove from index
     * @return A short message
     */
    public String removeFromIndex(String index, String type, String pid) {

	Globals.search.delete(index, type, pid);
	return pid + " of type " + type + " removed from index " + index + "!";
    }

    /**
     * @param p
     *            The pid with namespace that must be indexed
     * @param index
     *            the name of the index. Convention is to use the namespace of
     *            the pid.
     * @param type
     *            the type of the resource
     * @return a short message.
     */
    public String index(String p, String index, String type) {
	String jsonCompactStr = new Transform().oaiore(p,
		"application/json+compact");
	Globals.search.index(index, type, p, jsonCompactStr);
	return p + " indexed!";
    }

    /**
     * @param p
     *            The pid with namespace that must be indexed
     * @param index
     *            the name of the index. Convention is to use the namespace of
     *            the pid.
     * @param type
     *            the type of the resource
     * @return a short message.
     */
    public String publicIndex(String p, String index, String type) {
	Globals.search.index(index, type, p, new Read().readNode(p).toString());
	return p + " indexed!";
    }

    protected String index(Node n) {
	String namespace = n.getNamespace();
	String pid = n.getPid();
	return index(pid, namespace, n.getContentType());
    }

    public List<String> indexAll(String indexName) {
	Read read = new Read();
	List<String> msgs = Globals.search.indexAll(
		read.getNodes(read.listRepoNamespace(indexName)), indexName
			+ "-" + getCurrentDate());
	return msgs;
    }

    private String getCurrentDate() {
	DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	Date date = new Date();
	return dateFormat.format(date);
    }
}
