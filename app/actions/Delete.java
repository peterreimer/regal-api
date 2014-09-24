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

import helper.Globals;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import models.Node;

/**
 * @author Jan Schnasse
 *
 */
public class Delete {

    final static Logger logger = LoggerFactory.getLogger(Delete.class);

    /**
     * @param pids
     *            The pids that must be deleted
     * @return A short message.
     */
    public String deleteAll(List<String> pids) {
	if (pids == null || pids.isEmpty()) {
	    return "Nothing to delete!";
	}
	StringBuffer msg = new StringBuffer();
	for (String pid : pids) {
	    try {
		msg.append(delete(pid) + "\n");
	    } catch (Exception e) {
		logger.warn(pid + " " + e.getMessage());
	    }
	}
	return msg.toString();
    }

    /**
     * @param pid
     *            The pid that must be deleted
     * @return A short Message
     */
    public String delete(String pid) {
	StringBuffer msg = new StringBuffer();
	List<Node> pids = null;
	try {
	    pids = Globals.fedora.deleteComplexObject(pid);
	} catch (Exception e) {
	    msg.append("\n" + e);
	}
	try {
	    if (pids != null) {
		for (Node n : pids) {
		    msg.append("\n" + removeIdFromPublicAndPrivateIndex(n));
		}
	    }
	} catch (Exception e) {
	    msg.append("\n" + e);
	}
	return pid + " successfully deleted! \n" + msg + "\n";
    }

    private String removeIdFromPublicAndPrivateIndex(Node n) {
	StringBuffer msg = new StringBuffer();
	try {
	    String namespace = n.getNamespace();
	    String m = new Index().removeFromIndex(namespace,
		    n.getContentType(), n.getPid());
	    msg.append("\n" + m);
	    m = new Index().removeFromIndex("public_" + namespace,
		    n.getContentType(), n.getPid());
	    msg.append("\n" + m);
	} catch (Exception e) {
	    msg.append("\n" + e);
	}
	return msg.toString();
    }

    /**
     * @param pid
     *            a namespace qualified id
     * @return a message
     */
    public String deleteMetadata(String pid) {

	Globals.fedora.deleteDatastream(pid, "metadata");
	new Index().index(new Read().readNode(pid));
	return pid + ": metadata - datastream successfully deleted! ";
    }

    /**
     * @param pid
     *            the pid og the object
     * @return a message
     */
    public String deleteData(String pid) {
	Globals.fedora.deleteDatastream(pid, "data");
	new Index().index(new Read().readNode(pid));
	return pid + ": data - datastream successfully deleted! ";
    }
}
