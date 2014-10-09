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

import static archive.fedora.FedoraVocabulary.HAS_PART;
import static archive.fedora.FedoraVocabulary.IS_PART_OF;
import helper.Globals;
import helper.HttpArchiveException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import models.DublinCoreData;
import models.Message;
import models.Node;
import models.RegalObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.*;
import views.html.*;
import actions.BasicAuth;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
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
@Api(value = "/resource", description = "The resource endpoint allows one to manipulate and access complex objects as http resources. ")
@SuppressWarnings("javadoc")
public class Resource extends MyController {

    final static Logger logger = LoggerFactory.getLogger(Resource.class);

    @ApiOperation(produces = "application/json", nickname = "listUrn", value = "listUrn", notes = "Returns infos about urn", httpMethod = "GET")
    public static Promise<Result> listUrn(@PathParam("pid") String pid) {
	return new ReadMetadataAction().call(pid, new NodeAction() {
	    public Result exec(Node node) {
		response().setHeader("Access-Control-Allow-Origin", "*");
		return json(read.getUrnStatus(node));
	    }
	});
    }

    @ApiOperation(produces = "application/json", nickname = "listNodes", value = "listNodes", notes = "Returns all nodes for a list of ids", httpMethod = "GET")
    public static Promise<Result> listNodes(@QueryParam("ids") String ids) {
	return new ListAction().call(() -> {
	    try {
		List<String> is = Arrays.asList(ids.split(","));
		return json(read.getNodes(is));
	    } catch (HttpArchiveException e) {
		return JsonMessage(new Message(e, e.getCode()));
	    } catch (Exception e) {
		return JsonMessage(new Message(e, 500));
	    }
	});

    }

    @ApiOperation(produces = "application/json,text/html,text/csv", nickname = "listResources", value = "listResources", notes = "Returns a list of ids", httpMethod = "GET")
    public static Promise<Result> listResources(
	    @QueryParam("namespace") String namespace,
	    @QueryParam("contentType") String contentType,
	    @QueryParam("from") int from, @QueryParam("until") int until) {
	try {
	    if (request().accepts("text/html")) {
		return htmlList(namespace, contentType, from, until);
	    } else {
		return jsonList(namespace, contentType, from, until);
	    }
	} catch (HttpArchiveException e) {
	    return Promise.promise(new Function0<Result>() {
		public Result apply() {
		    return JsonMessage(new Message(e, e.getCode()));
		}
	    });
	} catch (Exception e) {
	    return Promise.promise(new Function0<Result>() {
		public Result apply() {
		    return JsonMessage(new Message(e, 500));
		}
	    });
	}
    }

    private static Promise<Result> jsonList(String namespace,
	    String contentType, int from, int until) {
	return new ListAction().call(() -> {
	    try {
		List<Map<String, Object>> nodes = read.nodelistToMap(read
			.listRepo(contentType, namespace, from, until));
		return json(nodes);
	    } catch (HttpArchiveException e) {
		return JsonMessage(new Message(e, e.getCode()));
	    } catch (Exception e) {
		return JsonMessage(new Message(e, 500));
	    }
	});
    }

    private static Promise<Result> htmlList(String namespace,
	    String contentType, int from, int until) {
	return new ListAction().call(() -> {
	    try {
		String servername = Play.application().configuration()
			.getString("regal-api.serverName");
		response().setHeader("Access-Control-Allow-Origin", "*");
		response().setContentType("text/html");
		List<Node> nodes = read.listRepo(contentType, namespace, from,
			until);
		return ok(resourceList.render(nodes, "http://" + servername
			+ "/resource/"));
	    } catch (HttpArchiveException e) {
		return HtmlMessage(new Message(e, e.getCode()));
	    } catch (Exception e) {
		return HtmlMessage(new Message(e, 500));
	    }
	});

    }

    @ApiOperation(produces = "application/json+regal-v0.4.0,application/json,text/html,application/json+compact,application/rdf+xml,text/plain", nickname = "listResource", value = "listResource", notes = "Returns a resource. Redirects in dependends to the accept header ", response = Message.class, httpMethod = "GET")
    public static Promise<Result> listResource(@PathParam("pid") String pid) {

	try {
	    response().setHeader("Access-Control-Allow-Origin", "*");
	    if (request().accepts("text/html"))
		return asHtml(pid);
	    if (request().accepts("application/json"))
		return asJson(pid, null);
	    if (request().accepts("application/json+compact"))
		return asJson(pid, "compact");
	    if (request().accepts("application/rdf+xml"))
		return asRdf(pid);
	    if (request().accepts("text/plain"))
		return asRdf(pid);
	    if (request().accepts("application/json+regal-v0.4.0"))
		return asRegalObject(pid);
	    return asRdf(pid);
	} catch (HttpArchiveException e) {
	    return Promise.promise(new Function0<Result>() {
		public Result apply() {
		    return JsonMessage(new Message(e, e.getCode()));
		}
	    });
	} catch (Exception e) {
	    return Promise.promise(new Function0<Result>() {
		public Result apply() {
		    return JsonMessage(new Message(e, 500));
		}
	    });
	}

    }

    @ApiOperation(produces = "text/plain", nickname = "listMetadata", value = "listMetadata", notes = "Shows Metadata of a resource.", response = play.mvc.Result.class, httpMethod = "GET")
    public static Promise<Result> listMetadata(@PathParam("pid") String pid) {
	return new ReadMetadataAction().call(pid, node -> {
	    response().setHeader("Access-Control-Allow-Origin", "*");
	    String result = read.readMetadata(pid);
	    return ok(result);
	});
    }

    @ApiOperation(produces = "application/octet-stream", nickname = "listData", value = "listData", notes = "Shows Data of a resource", response = play.mvc.Result.class, httpMethod = "GET")
    public static Promise<Result> listData(@PathParam("pid") String pid) {
	return new ReadDataAction().call(
		pid,
		node -> {
		    try {
			response()
				.setHeader("Access-Control-Allow-Origin", "*");
			URL url = new URL(Globals.fedoraIntern + "/objects/"
				+ pid + "/datastreams/data/content");
			HttpURLConnection connection = (HttpURLConnection) url
				.openConnection();
			InputStream is = connection.getInputStream();
			response().setContentType(connection.getContentType());
			return ok(is);
		    } catch (FileNotFoundException e) {
			throw new HttpArchiveException(404, e);
		    } catch (MalformedURLException e) {
			throw new HttpArchiveException(500, e);
		    } catch (IOException e) {
			throw new HttpArchiveException(500, e);
		    }
		});
    }

    @ApiOperation(produces = "application/json", nickname = "listDc", value = "listDc", notes = "Shows internal dublin core stream", response = play.mvc.Result.class, httpMethod = "GET")
    public static Promise<Result> listDc(@PathParam("pid") String pid) {
	return new ReadMetadataAction().call(pid, node -> {
	    DublinCoreData dc = read.readDC(pid);
	    return json(dc);
	});
    }

    @ApiOperation(produces = "application/json", nickname = "updateResource", value = "updateResource", notes = "Updates or Creates a Resource with the path decoded pid", response = Message.class, httpMethod = "PUT")
    @ApiImplicitParams({ @ApiImplicitParam(value = "New Object", required = true, dataType = "RegalObject", paramType = "body") })
    public static Promise<Result> updateResource(@PathParam("pid") String pid) {
	return new ModifyAction().call(pid, node -> {
	    try {
		String[] p = pid.split(":");
		return createResource(p[1], p[0]);
	    } catch (JsonMappingException e) {
		throw new HttpArchiveException(500, e);
	    } catch (JsonParseException e) {
		throw new HttpArchiveException(500, e);
	    } catch (IOException e) {
		throw new HttpArchiveException(500, e);
	    }
	});
    }

    private static Result createResource(String id, String namespace)
	    throws IOException, JsonParseException, JsonMappingException {
	Object o = request().body().asJson();
	RegalObject object;
	if (o != null) {
	    object = (RegalObject) MyController.mapper.readValue(o.toString(),
		    RegalObject.class);
	} else {
	    throw new NullPointerException(
		    "Please PUT at least a type, e.g. {\"type\":\"monograph\"}");
	}
	Node newnode = create.createResource(object.getType(),
		object.getParentPid(), object.getTransformer(),
		object.getAccessScheme(), object.getPublishScheme(), id,
		namespace);
	String result = newnode.getPid() + " created/updated!";
	return JsonMessage(new Message(result, 200));
    }

    @ApiOperation(produces = "application/json", nickname = "createNewResource", value = "createNewResource", notes = "Creates a Resource on a new position", response = Message.class, httpMethod = "PUT")
    @ApiImplicitParams({ @ApiImplicitParam(value = "New Object", required = true, dataType = "RegalObject", paramType = "body") })
    public static Promise<Result> createResource(
	    @PathParam("namespace") String namespace) {
	return new CreateAction().call(() -> {
	    try {
		String pid = create.pid(namespace);
		String[] p = pid.split(":");
		createResource(p[1], p[0]);
		Node node = read.readNode(pid);
		response().setHeader("Location",
			read.getHttpUriOfResource(node));
		return status(201, jsonString(node, "compact"));
	    } catch (JsonMappingException e) {
		throw new HttpArchiveException(500, e);
	    } catch (JsonParseException e) {
		throw new HttpArchiveException(500, e);
	    } catch (IOException e) {
		throw new HttpArchiveException(500, e);
	    }
	});
    }

    @ApiOperation(produces = "application/json", nickname = "updateMetadata", value = "updateMetadata", notes = "Updates the metadata of the resource using n-triples.", response = Message.class, httpMethod = "PUT")
    @ApiImplicitParams({ @ApiImplicitParam(value = "Metadata", required = true, dataType = "string", paramType = "body") })
    public static Promise<Result> updateMetadata(@PathParam("pid") String pid) {
	return new ModifyAction().call(
		pid,
		node -> {
		    String result = modify.updateMetadata(pid, request().body()
			    .asText());
		    return JsonMessage(new Message(result));
		});
    }

    @ApiOperation(produces = "application/json", nickname = "updateData", value = "updateData", notes = "Updates the data of a resource", response = Message.class, httpMethod = "PUT")
    @ApiImplicitParams({ @ApiImplicitParam(name = "data", value = "data", dataType = "file", required = true, paramType = "body") })
    public static Promise<Result> updateData(@PathParam("pid") String pid,
	    @QueryParam("md5") String md5) {
	return new ModifyAction()
		.call(pid,
			node -> {
			    try {
				MultipartFormData body = request().body()
					.asMultipartFormData();
				FilePart d = body.getFile("data");
				if (d == null) {
				    return JsonMessage(new Message(
					    "Missing File.", 400));
				}
				String mimeType = d.getContentType();
				String name = d.getFilename();
				FileInputStream content = new FileInputStream(d
					.getFile());
				modify.updateData(pid, content, mimeType, name,
					md5);
				return JsonMessage(new Message(
					"File uploaded! Type: " + mimeType
						+ ", Name: " + name));
			    } catch (IOException e) {
				throw new HttpArchiveException(500, e);
			    }
			});
    }

    @ApiOperation(produces = "application/json", nickname = "updateDc", value = "updateDc", notes = "Updates the dc data of a resource", response = Message.class, httpMethod = "PUT")
    @ApiImplicitParams({ @ApiImplicitParam(value = "Add Dublin Core", required = true, dataType = "DublinCoreData", paramType = "body") })
    public static Promise<Result> updateDc(@PathParam("pid") String pid) {
	return new ModifyAction().call(
		pid,
		node -> {
		    try {
			Object o = request().body().asJson();
			DublinCoreData dc;
			if (o != null) {
			    dc = (DublinCoreData) MyController.mapper
				    .readValue(o.toString(),
					    DublinCoreData.class);
			} else {
			    dc = new DublinCoreData();
			}
			String result = modify.updateDC(pid, dc);
			return JsonMessage(new Message(result, 200));
		    } catch (IOException e) {
			throw new HttpArchiveException(500, e);
		    }
		});
    }

    @ApiOperation(produces = "application/json", nickname = "deleteResource", value = "deleteResource", notes = "Deletes a resource", response = Message.class, httpMethod = "DELETE")
    public static Promise<Result> deleteResource(@PathParam("pid") String pid) {
	return new ModifyAction().call(
		pid,
		node -> {
		    Chunks<String> chunks = new StringChunks() {
			public void onReady(Chunks.Out<String> out) {
			    delete.setMessageQueue(out);
			}
		    };
		    ExecutorService executorService = Executors
			    .newSingleThreadExecutor();
		    executorService.execute(new Runnable() {
			public void run() {
			    delete.delete(pid);
			    delete.closeMessageQueue();
			}
		    });
		    executorService.shutdown();
		    return ok(chunks);
		});
    }

    @ApiOperation(produces = "application/json", nickname = "deleteMetadata", value = "deleteMetadata", notes = "Deletes a resources metadata", response = Message.class, httpMethod = "DELETE")
    public static Promise<Result> deleteMetadata(@PathParam("pid") String pid) {
	return new ModifyAction().call(pid, node -> {
	    String result = delete.deleteMetadata(pid);
	    return JsonMessage(new Message(result));
	});
    }

    @ApiOperation(produces = "application/json", nickname = "deleteData", value = "deleteData", notes = "Deletes a resources data", response = Message.class, httpMethod = "DELETE")
    public static Promise<Result> deleteData(@PathParam("pid") String pid) {
	return new ModifyAction().call(pid, node -> {
	    String result = delete.deleteData(pid);
	    return JsonMessage(new Message(result));
	});
    }

    @ApiOperation(produces = "application/json", nickname = "deleteDc", value = "deleteDc", notes = "Not implemented", response = Message.class, httpMethod = "DELETE")
    public static Promise<Result> deleteDc(@PathParam("pid") String pid) {
	return new ReadMetadataAction().call(pid,
		node -> JsonMessage(new Message("Not implemented!", 500)));
    }

    @ApiOperation(produces = "application/json", nickname = "deleteResources", value = "deleteResources", notes = "Deletes a set of resources", response = Message.class, httpMethod = "DELETE")
    public static Promise<Result> deleteResources(String namespace,
	    String type, String src, int from, int until) {
	return new BulkAction().call(() -> {
	    Chunks<String> chunks = new StringChunks() {
		public void onReady(Chunks.Out<String> out) {
		    delete.setMessageQueue(out);
		}
	    };
	    ExecutorService executorService = Executors
		    .newSingleThreadExecutor();
	    executorService.execute(new Runnable() {
		public void run() {
		    delete.deleteAll(read
			    .listRepo(type, namespace, from, until).stream()
			    .map((Node n) -> n.getPid())
			    .collect(Collectors.toList()));
		}
	    });
	    executorService.shutdown();

	    return ok(chunks);
	});
    }

    @ApiOperation(produces = "application/json", nickname = "listParts", value = "listParts", notes = "List resources linked with hasPart", response = play.mvc.Result.class, httpMethod = "GET")
    public static Promise<Result> listParts(@PathParam("pid") String pid) {
	return new ReadMetadataAction().call(
		pid,
		node -> {
		    List<String> nodeIds = read.readNode(pid).getRelatives(
			    HAS_PART);
		    List<Map<String, Object>> result = read.nodelistToMap(read
			    .getNodes(nodeIds));
		    return json(result);
		});
    }

    @ApiOperation(produces = "application/json", nickname = "listParents", value = "listParents", notes = "Shows resources linkes with isPartOf", response = play.mvc.Result.class, httpMethod = "GET")
    public static Promise<Result> listParents(@PathParam("pid") String pid) {
	return new ReadMetadataAction().call(
		pid,
		node -> {
		    List<String> nodeIds = read.readNode(pid).getRelatives(
			    IS_PART_OF);
		    List<Map<String, Object>> result = read.nodelistToMap(read
			    .getNodes(nodeIds));
		    return json(result);
		});
    }

    @ApiOperation(produces = "application/html", nickname = "asHtml", value = "asHtml", notes = "Returns a html display of the resource", response = Message.class, httpMethod = "GET")
    public static Promise<Result> asHtml(@PathParam("pid") String pid) {
	return new ReadMetadataAction().call(pid,
		node -> ok(resourceLong.render(node.toString())));
    }

    @ApiOperation(produces = "application/rdf+xml,text/plain", nickname = "asRdf", value = "asRdf", notes = "Returns a rdf display of the resource", response = Message.class, httpMethod = "GET")
    public static Promise<Result> asRdf(@PathParam("pid") String pid) {
	return new ReadMetadataAction().call(pid, node -> {
	    String result = "";
	    if (request().accepts("application/rdf+xml")) {
		result = transform.oaiore(node, "application/rdf+xml");
		response().setContentType("application/rdf+xml");
		return ok(result);
	    } else if (request().accepts("text/plain")) {
		result = transform.oaiore(node, "text/plain");
		response().setContentType("text/plain");
		return ok(result);
	    }
	    return JsonMessage(new Message(result));
	});
    }

    @ApiOperation(produces = "application/json", nickname = "asJson", value = "asJson", notes = "Returns a json display of the resource", response = Message.class, httpMethod = "GET")
    public static Promise<Result> asJson(@PathParam("pid") String pid,
	    String style) {
	return new ReadMetadataAction().call(pid, node -> {
	    String result = jsonString(node, style);
	    response().setContentType("application/json");
	    return ok(result);
	});
    }

    private static String jsonString(Node node, String style) {
	String result = "ERROR";
	if ("compact".equals(style))
	    result = transform.oaiore(node, "application/json+compact");
	else
	    result = transform.oaiore(node, "application/json");
	return result;
    }

    @ApiOperation(produces = "application/xml", nickname = "asOaiDc", value = "asOaiDc", notes = "Returns a oai dc display of the resource", response = Message.class, httpMethod = "GET")
    public static Promise<Result> asOaiDc(@PathParam("pid") String pid) {
	return new ReadMetadataAction().call(pid, node -> {
	    String result = transform.oaidc(pid);
	    response().setContentType("application/xml");
	    return ok(result);
	});
    }

    @ApiOperation(produces = "application/xml", nickname = "asEpicur", value = "asEpicur", notes = "Returns a epicur display of the resource", response = Message.class, httpMethod = "GET")
    public static Promise<Result> asEpicur(@PathParam("pid") String pid) {
	return new ReadMetadataAction().call(pid, node -> {
	    String result = transform.epicur(pid);
	    response().setContentType("application/xml");
	    return ok(result);
	});
    }

    @ApiOperation(produces = "application/xml", nickname = "asAleph", value = "asAleph", notes = "Returns a aleph xml display of the resource", response = Message.class, httpMethod = "GET")
    public static Promise<Result> asAleph(@PathParam("pid") String pid) {
	return new ReadMetadataAction().call(pid, node -> {
	    String result = transform.aleph(pid);
	    response().setContentType("application/xml");
	    return ok(result);
	});
    }

    @ApiOperation(produces = "application/json", nickname = "asRegalObject", value = "asRegalObject", notes = "The basic regal object", response = Node.class, httpMethod = "GET")
    public static Promise<Result> asRegalObject(@PathParam("pid") String pid) {
	return new ReadMetadataAction().call(pid, node -> {
	    response().setHeader("Access-Control-Allow-Origin", "*");
	    response().setContentType("application/json");
	    Node result = read.readNode(pid);
	    return json(result);
	});

    }

    @ApiOperation(produces = "application/pdf", nickname = "asPdfa", value = "asPdfa", notes = "Returns a pdfa conversion of a pdf datastream.", httpMethod = "GET")
    public static Promise<Result> asPdfa(@PathParam("pid") String pid) {
	return new ReadMetadataAction().call(
		pid,
		node -> {
		    try {
			String redirectUrl = transform.getPdfaUrl(pid);
			URL url;
			url = new URL(redirectUrl);
			HttpURLConnection connection = (HttpURLConnection) url
				.openConnection();
			InputStream is = connection.getInputStream();
			response().setContentType("application/pdf");
			return ok(is);
		    } catch (MalformedURLException e) {
			return JsonMessage(new Message(e, 500));
		    } catch (IOException e) {
			return JsonMessage(new Message(e, 500));
		    }
		});
    }

    @ApiOperation(produces = "text/plain", nickname = "asPdfboxTxt", value = "asPdfboxTxt", notes = "Returns text display of a pdf datastream.", response = String.class, httpMethod = "GET")
    public static Promise<Result> asPdfboxTxt(@PathParam("pid") String pid) {
	return new ReadMetadataAction().call(pid, node -> {
	    String result = transform.pdfbox(pid);
	    response().setContentType("text/plain");
	    return ok(result);
	});
    }

    @ApiOperation(produces = "text/plain", nickname = "updateOaiSets", value = "updateOaiSets", notes = "Links resource to oai sets and creates new sets if needed", response = String.class, httpMethod = "POST")
    public static Promise<Result> updateOaiSets(@PathParam("pid") String pid) {
	return new ModifyAction().call(pid, node -> {
	    String result = modify.makeOAISet(pid);
	    response().setContentType("text/plain");
	    return ok(result);
	});
    }

}