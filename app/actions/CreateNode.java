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

import static archive.fedora.Vocabulary.TYPE_OBJECT;
import helper.Globals;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import models.Node;
import models.Transformer;

/**
 * @author Jan Schnasse
 *
 */
public class CreateNode {

    final static Logger logger = LoggerFactory.getLogger(CreateNode.class);

    /**
     * @param type
     *            the type of the new resource
     * @param parent
     *            the parent of a new rsource
     * @param transformers
     *            transformers connected to the resource
     * @param accessScheme
     *            a string that signals who is allowed to access this node
     * @param input
     *            the input defines the contenttype and a optional parent
     * @param rawPid
     *            the pid without namespace
     * @param namespace
     *            the namespace
     * @return the Node representing the resource
     */
    public Node createResource(String type, String parent,
	    List<String> transformers, String accessScheme, String rawPid,
	    String namespace) {
	logger.debug("create " + type);
	Node node = createNodeIfNotExists(type, parent, transformers,
		accessScheme, rawPid, namespace);
	new IndexNode().removeFromIndex(namespace, node.getContentType(),
		node.getPid());
	node.setAccessScheme(accessScheme);
	setNodeType(type, node);
	linkWithParent(parent, node);
	updateTransformer(transformers, node);
	Globals.fedora.updateNode(node);
	new IndexNode().index(node);
	return node;
    }

    private Node createNodeIfNotExists(String type, String parent,
	    List<String> transformers, String accessScheme, String rawPid,
	    String namespace) {
	String pid = namespace + ":" + rawPid;
	Node node = null;
	if (Globals.fedora.nodeExists(pid)) {
	    node = new ReadNode().readNode(pid);
	} else {
	    node = new Node();
	    node.setNamespace(namespace).setPID(pid);
	    node.setContentType(type);
	    node.setAccessScheme(accessScheme);
	    Globals.fedora.createNode(node);
	}
	return node;
    }

    private void setNodeType(String type, Node node) {
	node.setType(TYPE_OBJECT);
	node.setContentType(type);
	new IndexNode().index(node);
    }

    private void linkWithParent(String parentPid, Node node) {
	Globals.fedora.unlinkParent(node);
	Globals.fedora.linkToParent(node, parentPid);
	Globals.fedora.linkParentToNode(parentPid, node.getPid());
	new IndexNode().index(node);
    }

    private void updateTransformer(List<String> transformers, Node node) {
	node.removeAllContentModels();
	if (transformers != null) {
	    for (String t : transformers) {
		node.addTransformer(new Transformer(t));
	    }
	}
    }

    /**
     * 
     * @param cms
     *            a List of Transformers
     * @return a message
     */
    public String contentModelsInit(List<Transformer> cms) {
	Globals.fedora.updateContentModels(cms);
	return "Success!";
    }

}