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
	play.Logger.debug("Read from cache: " + pid);
	return (Node) Cache.get(pid);
    }

    void writeNodeToCache(Node node) {
	Cache.set(node.getPid(), node);
    }

    void removeFromCache(Node node) {
	Cache.remove(node.getPid());
    }

}
