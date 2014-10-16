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
package controllers;

import helper.Globals;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import models.Message;
import models.Transformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.libs.F.Promise;
import play.mvc.Result;
import actions.BasicAuth;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * 
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 * 
 *         Api is documented using swagger. See:
 *         https://github.com/wordnik/swagger-ui
 * 
 */
@BasicAuth
@Api(value = "/utils", description = "The utils endpoint provides rpc style methods.")
@SuppressWarnings("javadoc")
public class MyUtils extends MyController {
    final static Logger logger = LoggerFactory.getLogger(MyUtils.class);

    @ApiOperation(produces = "application/json,application/html", nickname = "index", value = "index", notes = "Adds resource to private elasticsearch index", response = List.class, httpMethod = "POST")
    public static Promise<Result> index(@PathParam("pid") String pid,
	    @QueryParam("contentType") final String type,
	    @QueryParam("index") final String indexName) {
	return new ModifyAction().call(pid, node -> {
	    String curIndex = indexName.isEmpty() ? pid.split(":")[0]
		    : indexName;
	    String result = index.index(pid, curIndex, type);
	    return JsonMessage(new Message(result));
	});
    }

    @ApiOperation(produces = "application/json,application/html", nickname = "indexAll", value = "indexAll", notes = "Adds resource to private elasticsearch index", response = List.class, httpMethod = "POST")
    public static Promise<Result> indexAll(
	    @QueryParam("index") final String indexName) {
	return new BulkAction().call(() -> {
	    Chunks<String> chunks = new StringChunks() {
		public void onReady(Chunks.Out<String> out) {
		    index.setMessageQueue(out);
		}
	    };
	    ExecutorService executorService = Executors
		    .newSingleThreadExecutor();
	    executorService.execute(new Runnable() {
		public void run() {
		    index.indexAll(indexName);
		}
	    });
	    executorService.shutdown();
	    return ok(chunks);
	});
    }

    @ApiOperation(produces = "application/json,application/html", nickname = "removeFromIndex", value = "removeFromIndex", notes = "Removes resource to elasticsearch index", httpMethod = "DELETE")
    public static Promise<Result> removeFromIndex(@PathParam("pid") String pid,
	    @QueryParam("contentType") final String type) {
	return new ModifyAction().call(pid, node -> {
	    String result = index.remove(pid, pid.split(":")[0], type);
	    return JsonMessage(new Message(result));
	});
    }

    @ApiOperation(produces = "application/json,application/html", nickname = "publicIndex", value = "publicIndex", notes = "Adds resource to public elasticsearch index", httpMethod = "POST")
    public static Promise<Result> publicIndex(@PathParam("pid") String pid,
	    @QueryParam("contentType") final String type,
	    @QueryParam("index") final String indexName) {
	return new ModifyAction().call(pid, node -> {
	    String curIndex = indexName.isEmpty() ? pid.split(":")[0]
		    : indexName;
	    String result = index.publicIndex(pid, "public_" + curIndex, type);
	    return JsonMessage(new Message(result));
	});
    }

    @ApiOperation(produces = "application/json,application/html", nickname = "removeFromPublicIndex", value = "removeFromPublicIndex", notes = "Removes resource to public elasticsearch index", httpMethod = "DELETE")
    public static Promise<Result> removeFromPublicIndex(
	    @PathParam("pid") String pid,
	    @QueryParam("contentType") final String type) {
	return new ModifyAction().call(
		pid,
		node -> {
		    String result = index.remove(pid,
			    "public_" + pid.split(":")[0], type);
		    return JsonMessage(new Message(result));
		});
    }

    @ApiOperation(produces = "application/json,application/html", nickname = "lobidify", value = "lobidify", notes = "Fetches metadata from lobid.org and PUTs it to /metadata.", httpMethod = "POST")
    public static Promise<Result> lobidify(@PathParam("pid") String pid) {
	return new ModifyAction().call(pid, node -> {
	    String result = modify.lobidify(pid);
	    return JsonMessage(new Message(result));
	});
    }

    @ApiOperation(produces = "application/json,application/html", nickname = "addUrn", value = "addUrn", notes = "Adds a urn to the /metadata of the resource.", httpMethod = "POST")
    public static Promise<Result> addUrn(@QueryParam("id") final String id,
	    @QueryParam("namespace") final String namespace,
	    @QueryParam("snid") final String snid) {
	return new BulkAction().call(() -> {
	    String result = modify.addUrn(id, namespace, snid);
	    return JsonMessage(new Message(result));
	});
    }

    @ApiOperation(produces = "application/json,application/html", nickname = "replaceUrn", value = "replaceUrn", notes = "Replaces a urn on the /metadata of the resource.", httpMethod = "POST")
    public static Promise<Result> replaceUrn(@QueryParam("id") final String id,
	    @QueryParam("namespace") final String namespace,
	    @QueryParam("snid") final String snid) {
	return new BulkAction().call(() -> {
	    String result = modify.replaceUrn(id, namespace, snid);
	    return JsonMessage(new Message(result));
	});
    }

    @ApiOperation(produces = "application/json,application/html", nickname = "initContentModels", value = "initContentModels", notes = "Initializes default transformers.", httpMethod = "POST")
    public static Promise<Result> initContentModels(
	    @DefaultValue("") @QueryParam("namespace") String namespace) {
	return new BulkAction()
		.call(() -> {
		    List<Transformer> transformers = new Vector<Transformer>();
		    transformers.add(new Transformer(namespace + "epicur",
			    "epicur", Globals.server + "/resource/(pid)."
				    + namespace + "epicur"));
		    transformers.add(new Transformer(namespace + "oaidc",
			    "oaidc", Globals.server + "/resource/(pid)."
				    + namespace + "oaidc"));
		    transformers.add(new Transformer(namespace + "pdfa",
			    "pdfa", Globals.server + "/resource/(pid)."
				    + namespace + "pdfa"));
		    transformers.add(new Transformer(namespace + "pdfbox",
			    "pdfbox", Globals.server + "/resource/(pid)."
				    + namespace + "pdfbox"));
		    transformers.add(new Transformer(namespace + "aleph",
			    "aleph", Globals.server + "/resource/(pid)."
				    + namespace + "aleph"));
		    create.contentModelsInit(transformers);
		    String result = "Reinit contentModels " + namespace
			    + "epicur, " + namespace + "oaidc, " + namespace
			    + "pdfa, " + namespace + "pdfbox, " + namespace
			    + "aleph";
		    return JsonMessage(new Message(result));
		});
    }
}
