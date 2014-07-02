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

import helper.Actions;
import helper.HttpArchiveException;

import java.util.List;
import java.util.Vector;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import models.Message;
import models.ObjectList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Result;
import actions.BasicAuth;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import de.nrw.hbz.regal.datatypes.Transformer;

/**
 * 
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 * 
 *         Api is documented using swagger. See:
 *         https://github.com/wordnik/swagger-ui
 * 
 */
@Api(value = "/utils", description = "The utils endpoint provides rpc style methods.")
@SuppressWarnings("javadoc")
public class MyUtils extends MyController {
    final static Logger logger = LoggerFactory.getLogger(MyUtils.class);

    @BasicAuth
    @ApiOperation(produces = "application/json,application/html", nickname = "index", value = "index", notes = "Adds resource to private elasticsearch index", response = ObjectList.class, httpMethod = "POST")
    public static Result index(@PathParam("pid") String pid,
	    @QueryParam("type") final String type) {
	try {
	    Actions actions = Actions.getInstance();
	    String result = actions.index(pid, pid.split(":")[0], type);
	    return JsonResponse(new Message(result));
	} catch (HttpArchiveException e) {
	    return JsonResponse(new Message(e, e.getCode()), e.getCode());
	} catch (Exception e) {

	    return JsonResponse(new Message(e, 500), 500);
	}

    }

    @BasicAuth
    @ApiOperation(produces = "application/json,application/html", nickname = "removeFromIndex", value = "removeFromIndex", notes = "Removes resource to elasticsearch index", response = ObjectList.class, httpMethod = "DELETE")
    public static Result removeFromIndex(@PathParam("pid") String pid,
	    @QueryParam("type") final String type) {
	try {
	    Actions actions = Actions.getInstance();
	    String result = actions.removeFromIndex(pid.split(":")[0], type,
		    pid);
	    return JsonResponse(new Message(result));
	} catch (Exception e) {
	    return JsonResponse(new Message(e, 500), 500);
	}
    }

    @BasicAuth
    @ApiOperation(produces = "application/json,application/html", nickname = "publicIndex", value = "publicIndex", notes = "Adds resource to public elasticsearch index", response = ObjectList.class, httpMethod = "POST")
    public static Result publicIndex(@PathParam("pid") String pid,
	    @QueryParam("type") final String type) {
	try {
	    Actions actions = Actions.getInstance();
	    String result = actions.index(pid, "public_" + pid.split(":")[0],
		    type);
	    return JsonResponse(new Message(result));
	} catch (Exception e) {
	    return JsonResponse(new Message(e, 500), 500);
	}
    }

    @BasicAuth
    @ApiOperation(produces = "application/json,application/html", nickname = "removeFromPublicIndex", value = "removeFromPublicIndex", notes = "Removes resource to public elasticsearch index", response = ObjectList.class, httpMethod = "DELETE")
    public static Result removeFromPublicIndex(@PathParam("pid") String pid,
	    @QueryParam("type") final String type) {
	try {
	    Actions actions = Actions.getInstance();
	    String result = actions.removeFromIndex("public_"
		    + pid.split(":")[0], type, pid);
	    return JsonResponse(new Message(result));
	} catch (Exception e) {
	    return JsonResponse(new Message(e, 500), 500);
	}
    }

    @BasicAuth
    @ApiOperation(produces = "application/json,application/html", nickname = "lobidify", value = "lobidify", notes = "Fetches metadata from lobid.org and PUTs it to /metadata.", response = ObjectList.class, httpMethod = "POST")
    public static Result lobidify(@PathParam("pid") String pid) {
	try {
	    Actions actions = Actions.getInstance();
	    String result = actions.lobidify(pid);
	    return JsonResponse(new Message(result));
	} catch (Exception e) {
	    return JsonResponse(new Message(e, 500), 500);
	}
    }

    @BasicAuth
    @ApiOperation(produces = "application/json,application/html", nickname = "addUrn", value = "addUrn", notes = "Adds a urn to the /metadata of the resource.", response = ObjectList.class, httpMethod = "POST")
    public static Result addUrn(@QueryParam("id") final String id,
	    @QueryParam("namespace") final String namespace,
	    @QueryParam("snid") final String snid) {
	try {
	    Actions actions = Actions.getInstance();
	    String result = actions.addUrn(id, namespace, snid);
	    return JsonResponse(new Message(result));
	} catch (Exception e) {
	    return JsonResponse(new Message(e, 500), 500);
	}
    }

    @BasicAuth
    @ApiOperation(produces = "application/json,application/html", nickname = "replaceUrn", value = "replaceUrn", notes = "Replaces a urn on the /metadata of the resource.", response = ObjectList.class, httpMethod = "POST")
    public static Result replaceUrn(@QueryParam("id") final String id,
	    @QueryParam("namespace") final String namespace,
	    @QueryParam("snid") final String snid) {
	try {
	    Actions actions = Actions.getInstance();
	    String result = actions.replaceUrn(id, namespace, snid);
	    return JsonResponse(new Message(result));
	} catch (Exception e) {
	    return JsonResponse(new Message(e, 500), 500);
	}
    }

    @BasicAuth
    @ApiOperation(produces = "application/json,application/html", nickname = "initContentModels", value = "initContentModels", notes = "Initializes default transformers.", response = ObjectList.class, httpMethod = "POST")
    public static Result initContentModels(
	    @DefaultValue("") @QueryParam("namespace") String namespace) {
	try {
	    Actions actions = Actions.getInstance();
	    List<Transformer> transformers = new Vector<Transformer>();
	    transformers.add(new Transformer(namespace + "epicur", "epicur",
		    actions.getServer() + "/resource/(pid)." + namespace
			    + "epicur"));
	    transformers.add(new Transformer(namespace + "oaidc", "oaidc",
		    actions.getServer() + "/resource/(pid)." + namespace
			    + "oaidc"));
	    transformers.add(new Transformer(namespace + "pdfa", "pdfa",
		    actions.getServer() + "/resource/(pid)." + namespace
			    + "pdfa"));
	    transformers.add(new Transformer(namespace + "pdfbox", "pdfbox",
		    actions.getServer() + "/resource/(pid)." + namespace
			    + "pdfbox"));
	    transformers.add(new Transformer(namespace + "aleph", "aleph",
		    actions.getServer() + "/resource/(pid)." + namespace
			    + "aleph"));
	    actions.contentModelsInit(transformers);
	    String result = "Reinit contentModels " + namespace + "epicur, "
		    + namespace + "oaidc, " + namespace + "pdfa, " + namespace
		    + "pdfbox, " + namespace + "aleph";
	    return JsonResponse(new Message(result));
	} catch (Exception e) {
	    return JsonResponse(new Message(e, 500), 500);
	}
    }

}
