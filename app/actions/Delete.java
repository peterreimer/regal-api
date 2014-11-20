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

import play.mvc.Results.Chunks;
import models.Globals;
import models.Node;

/**
 * @author Jan Schnasse
 *
 */
public class Delete extends RegalAction {

    Chunks.Out<String> messageOut;

    /**
     * @param pids
     *            The pids that must be deleted
     * @return A short message.
     */
    public String deleteAll(List<String> pids) {
	if (pids == null || pids.isEmpty()) {
	    return "Nothing to delete!";
	}
	for (String pid : pids) {
	    try {
		delete(pid);
	    } catch (Exception e) {
		play.Logger.warn(pid + " " + e.getMessage());
	    }
	}
	messageOut.close();
	return "Successfuly finished\n";
    }

    /**
     * @param pid
     *            The pid that must be deleted
     */
    public void delete(String pid) {
	StringBuffer msg = new StringBuffer();
	List<Node> pids = null;
	try {
	    pids = Globals.fedora.deleteComplexObject(pid);
	} catch (Exception e) {
	    messageOut.write("\n" + e);
	}
	try {
	    if (pids != null) {
		for (Node n : pids) {
		    messageOut.write("\n"
			    + removeIdFromPublicAndPrivateIndex(n));
		}
	    }
	} catch (Exception e) {
	    messageOut.write("\n" + e);
	}
	messageOut.write(pid + " successfully deleted! \n" + msg + "\n");

    }

    private String removeIdFromPublicAndPrivateIndex(Node n) {
	StringBuffer msg = new StringBuffer();
	try {
	    String namespace = n.getNamespace();
	    String m = new Index().remove(n.getPid(), namespace,
		    n.getContentType());
	    msg.append("\n" + m);
	    m = new Index().remove(n.getPid(), "public_" + namespace,
		    n.getContentType());
	    msg.append("\n" + m);
	} catch (Exception e) {
	    msg.append("\n" + e);
	} finally {
	    removeFromCache(n);
	}
	return msg.toString();
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

    /**
     * @param out
     *            messages for chunked responses
     */
    public void setMessageQueue(Chunks.Out<String> out) {
	messageOut = out;
    }

    /**
     * Close messageQueue for chunked responses
     * 
     */
    public void closeMessageQueue() {
	if (messageOut != null)
	    messageOut.close();
    }

}
