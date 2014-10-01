package controllers;

import static archive.fedora.FedoraVocabulary.HAS_PART;
import static archive.fedora.FedoraVocabulary.IS_PART_OF;
import helper.HttpArchiveException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import models.Message;
import models.Node;
import play.libs.F.Promise;
import play.mvc.Result;
import actions.BasicAuth;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@BasicAuth
@Api(value = "/resourceIndex", description = "The resourceIndex endpoint reads resources from elasticsearch")
@SuppressWarnings("javadoc")
public class ResourceIndex extends MyController {

    @ApiOperation(produces = "application/json", nickname = "listNodes", value = "listNodes", notes = "Returns all nodes for a list of ids", httpMethod = "GET")
    public static Promise<Result> listNodes(@QueryParam("ids") String ids) {
	return new ReadAction().call(null, new NodeAction() {
	    public Result exec(Node nodes) {
		try {
		    List<String> is = Arrays.asList(ids.split(","));
		    return json(read.getNodesFromIndex(is));
		} catch (HttpArchiveException e) {
		    return JsonMessage(new Message(e, e.getCode()));
		} catch (Exception e) {
		    return JsonMessage(new Message(e, 500));
		}
	    }
	});
    }

    @ApiOperation(produces = "application/json,text/html,text/csv", nickname = "listResources", value = "listResources", notes = "Returns a list of ids", httpMethod = "GET")
    public static Promise<Result> listResources(
	    @QueryParam("namespace") String namespace,
	    @QueryParam("contentType") String contentType,
	    @QueryParam("from") int from, @QueryParam("until") int until) {
	return new ReadAction().call(null, new NodeAction() {
	    public Result exec(Node nodes) {
		try {
		    if (request().accepts("text/html")) {
			return htmlList(namespace, contentType, from, until);
		    } else {
			return jsonList(namespace, contentType, from, until);
		    }
		} catch (HttpArchiveException e) {
		    return JsonMessage(new Message(e, e.getCode()));
		} catch (Exception e) {
		    return JsonMessage(new Message(e, 500));
		}
	    }
	});
    }

    private static Result jsonList(String namespace, String contentType,
	    int from, int until) {
	try {
	    List<Map<String, Object>> hits = read.hitlistToMap(read.listSearch(
		    contentType, namespace, from, until));
	    return json(hits);
	} catch (HttpArchiveException e) {
	    return JsonMessage(new Message(e, e.getCode()));
	} catch (Exception e) {
	    return JsonMessage(new Message(e, 500));
	}
    }

    private static Result htmlList(String namespace, String contentType,
	    int from, int until) {
	try {
	    // String servername = Play.application().configuration()
	    // .getString("regal-api.serverName");
	    return HtmlMessage(new Message(
		    "Not able to render data from elasticsearch as HTML. Elasticsearch output is only supported as application/json !",
		    415));
	} catch (HttpArchiveException e) {
	    return HtmlMessage(new Message(e, e.getCode()));
	} catch (Exception e) {
	    return HtmlMessage(new Message(e, 500));
	}
    }

    @ApiOperation(produces = "application/json", nickname = "listResource", value = "listResource", notes = "Returns a resource. Redirects in dependends to the accept header ", response = Message.class, httpMethod = "GET")
    public static Promise<Result> listResource(@PathParam("pid") String pid) {
	ReadAction action = new ReadAction();
	return action.call(pid, new NodeAction() {
	    public Result exec(Node node) {
		return json(read.readNodeFromIndex(pid));
	    }
	});
    }

    @ApiOperation(produces = "application/json", nickname = "listParts", value = "listParts", notes = "List resources linked with hasPart", response = play.mvc.Result.class, httpMethod = "GET")
    public static Promise<Result> listParts(@PathParam("pid") String pid) {
	return new ReadAction().call(pid, new NodeAction() {
	    public Result exec(Node nodes) {
		List<String> nodeIds = read.readNode(pid)
			.getRelatives(HAS_PART);
		List<Map<String, Object>> result = read
			.getNodesFromIndex(nodeIds);
		return json(result);
	    }
	});
    }

    @ApiOperation(produces = "application/json", nickname = "listParents", value = "listParents", notes = "Shows resources linkes with isPartOf", response = play.mvc.Result.class, httpMethod = "GET")
    public static Promise<Result> listParents(@PathParam("pid") String pid) {
	ReadAction action = new ReadAction();
	return action.call(pid, new NodeAction() {
	    public Result exec(Node node) {
		List<String> nodeIds = read.readNode(pid).getRelatives(
			IS_PART_OF);
		List<Map<String, Object>> result = read
			.getNodesFromIndex(nodeIds);
		return json(result);
	    }
	});
    }

}