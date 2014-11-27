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

import models.Globals;
import models.Node;

/**
 * @author Jan Schnasse
 *
 */
public class Index {

    /**
     * @param pid
     *            The namespaced pid to remove from index
     * @param index
     *            the elasticsearch index
     * @param type
     *            the type of the resource
     * @return A short message
     */
    public String remove(String pid, String index, String type) {

	Globals.search.delete(pid, index, type);
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
	Globals.search.index(index, type, p, new Read().readNode(p).toString());
	return p + " indexed in " + index + "!";
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
	String msg = "";
	if ("public".equals(n.getPublishScheme())) {
	    msg = index(pid, "public_" + namespace, n.getContentType());
	} else {
	    msg = remove(pid, "public_" + namespace, n.getContentType());
	}
	return msg + "\n" + index(pid, namespace, n.getContentType());
    }

    /**
     * @param nodes
     *            a list of nodes
     * @param namespace
     *            a namespace to create a new index from
     * @return the list of indexed objects as string
     */
    public String indexAll(List<Node> nodes, String namespace) {
	String indexNameWithDatestamp = namespace + "-" + getCurrentDate();
	return Globals.search.indexAll(nodes, indexNameWithDatestamp)
		.toString();
    }

    private String getCurrentDate() {
	DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	Date date = new Date();
	return dateFormat.format(date);
    }
}
