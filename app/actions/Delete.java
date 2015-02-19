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

/**
 * @author Jan Schnasse
 *
 */
public class Delete extends RegalAction {

    /**
     * Deletes only this single node. Child objects will remain.
     * 
     * @param n
     *            a node to delete
     * @return a message
     */
    public String delete(Node n) {
	StringBuffer message = new StringBuffer();
	message.append(new Index().remove(n));
	Globals.fedora.deleteNode(n.getPid());
	return message.toString() + "\n" + n.getPid() + " deleted!";
    }

    /**
     * Each node in the list will be deleted. Child objects will remain
     * 
     * @param nodes
     *            a list of nodes to delete.
     * @return a message
     */
    public String delete(List<Node> nodes) {
	return apply(nodes, n -> delete(n));
    }

    /**
     * @param pid
     *            the id of a resource.
     * @return a message
     */
    public String deleteSeq(String pid) {
	Globals.fedora.deleteDatastream(pid, "seq");
	updateIndexAndCache(new Read().readNode(pid));
	return pid + ": seq - datastream successfully deleted! ";
    }

    /**
     * @param pid
     *            a namespace qualified id
     * @return a message
     */
    public String deleteMetadata(String pid) {
	Globals.fedora.deleteDatastream(pid, "metadata");
	updateIndexAndCache(new Read().readNode(pid));
	return pid + ": metadata - datastream successfully deleted! ";
    }

    /**
     * @param pid
     *            the pid og the object
     * @return a message
     */
    public String deleteData(String pid) {
	Globals.fedora.deleteDatastream(pid, "data");
	updateIndexAndCache(new Read().readNode(pid));
	return pid + ": data - datastream successfully deleted! ";
    }
}
