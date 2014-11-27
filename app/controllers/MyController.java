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

import helper.HttpArchiveException;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import models.Message;
import models.Node;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.message;
import actions.Create;
import actions.Delete;
import actions.Index;
import actions.Modify;
import actions.Read;
import actions.Transform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.core.util.JsonUtil;

/**
 * 
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 * 
 */
public class MyController extends Controller {

    protected static ObjectMapper mapper = JsonUtil.mapper();

    static Read read = new Read();
    static Create create = new Create();
    static Index index = new Index();
    static Modify modify = new Modify();
    static Delete delete = new Delete();
    static Transform transform = new Transform();

    /**
     * @return Html or Json Output
     */
    public static Result AccessDenied() {
	Message msg = new Message("Access Denied!", 401);
	play.Logger.debug("\nResponse: " + msg.toString());
	if (request().accepts("text/html")) {
	    return HtmlMessage(msg);
	} else {
	    return JsonMessage(msg);
	}
    }

    private static void setJsonHeader() {
	response().setHeader("Access-Control-Allow-Origin", "*");
	response().setContentType("application/json");
    }

    /**
     * @param obj
     *            an arbitrary object
     * @return json serialization of obj
     */
    public static Result getJsonResult(Object obj) {
	setJsonHeader();
	try {
	    return ok(json(obj));
	} catch (Exception e) {
	    return internalServerError("Not able to create response!");
	}
    }

    protected static String json(Object obj) throws Exception {
	StringWriter w = new StringWriter();
	mapper.writeValue(w, obj);
	String result = w.toString();
	debugPrint(result);
	return result;

    }

    private static void debugPrint(String str) {
	play.Logger.debug("\nResponse:\nHeaders\n\t"
		+ mapToString(response().getHeaders()) + "\nBody:\n\t" + str);
    }

    /**
     * @param msg
     *            the msg will be rendered as html using message view
     * @return a html rendering of msg
     */
    public static Result HtmlMessage(Message msg) {
	play.Logger.debug("\nResponse: " + msg.toString());
	return status(msg.getCode(), message.render(msg.toString()));
    }

    /**
     * @param msg
     *            the msg will be rendered as json
     * @return a json rendering of msg
     */
    public static Result JsonMessage(Message msg) {
	response().setHeader("Access-Control-Allow-Methods",
		"POST, GET, PUT, DELETE");
	response().setHeader("Access-Control-Max-Age", "3600");
	response()
		.setHeader("Access-Control-Allow-Headers",
			"Origin, X-Requested-With, Content-Type, Accept, Authorization, X-Auth-Token");
	response().setHeader("Access-Control-Allow-Credentials", "true");
	response().setHeader("Content-Type", "application/json; charset=utf-8");

	debugPrint(msg.toString());
	return status(msg.getCode(), msg.toString());
    }

    /**
     * @param accessScheme
     *            the accessScheme of the object
     * @param role
     *            the role of the user
     * @return true if the user is allowed to read the object
     */
    public static boolean readData_accessIsAllowed(String accessScheme,
	    String role) {
	if (!"edoweb-admin".equals(role)) {
	    if ("public".equals(accessScheme)) {
		return true;
	    } else if ("restricted".equals(accessScheme)
		    || "remote".equals(accessScheme)) {
		if ("edoweb-editor".equals(role)
			|| "edoweb-reader".equals(role)) {
		    return true;
		}
	    } else if ("private".equals(accessScheme)
		    || "single".equals(accessScheme)) {
		if ("edoweb-editor".equals(role))
		    return true;
	    }
	} else {
	    return true;
	}
	return false;
    }

    /**
     * @param publishScheme
     *            the publishScheme of the object
     * @param role
     *            the role of the user
     * @return true if the user is allowed to read the object
     */
    public static boolean readMetadata_accessIsAllowed(String publishScheme,
	    String role) {
	if (!"edoweb-admin".equals(role)) {
	    if ("public".equals(publishScheme)) {
		return true;
	    } else if ("private".equals(publishScheme)) {
		if ("edoweb-editor".equals(role)
			|| "edoweb-reader".equals(role)) {
		    return true;
		}
	    }
	} else {
	    return true;
	}
	return false;
    }

    /**
     * @param role
     *            the role of the user
     * @return true if the user is allowed to modify the object
     */
    public static boolean modifyingAccessIsAllowed(String role) {
	if ("edoweb-admin".equals(role) || "edoweb-editor".equals(role))
	    return true;
	return false;
    }

    interface NodeAction {
	Result exec(Node node);
    }

    interface Action {
	Result exec();
    }

    /**
     * @author Jan Schnasse
     *
     */
    public static class ReadMetadataAction {
	Promise<Result> call(String pid, NodeAction ca) {
	    return Promise
		    .promise(() -> {
			try {
			    Node node = null;
			    if (pid != null) {
				node = read.readNode(pid);
				String role = (String) Http.Context.current().args
					.get("role");
				String publishScheme = node.getPublishScheme();
				if (!readMetadata_accessIsAllowed(
					publishScheme, role)) {
				    return AccessDenied();
				}
			    }
			    return ca.exec(node);
			} catch (HttpArchiveException e) {
			    if (request().accepts("text/html")) {
				return HtmlMessage(new Message(e, e.getCode()));
			    }
			    return JsonMessage(new Message(e, e.getCode()));
			} catch (Exception e) {
			    if (request().accepts("text/html")) {
				return HtmlMessage(new Message(e, 500));
			    }
			    return JsonMessage(new Message(e, 500));
			}
		    });
	}
    }

    /**
     * @author Jan Schnasse
     *
     */
    public static class ReadDataAction {
	Promise<Result> call(String pid, NodeAction ca) {
	    return Promise.promise(() -> {
		try {
		    Node node = null;
		    if (pid != null) {
			node = read.readNode(pid);
			String role = (String) Http.Context.current().args
				.get("role");
			String accessScheme = node.getAccessScheme();
			if (!readData_accessIsAllowed(accessScheme, role)) {
			    return AccessDenied();
			}
		    }
		    return ca.exec(node);
		} catch (HttpArchiveException e) {
		    return JsonMessage(new Message(e, e.getCode()));
		} catch (Exception e) {
		    return JsonMessage(new Message(e, 500));
		}
	    });
	}

    }

    /**
     * @author Jan Schnasse
     *
     */
    public static class ListAction {
	Promise<Result> call(Action ca) {
	    return Promise.promise(() -> {
		try {
		    String role = (String) Http.Context.current().args
			    .get("role");
		    if (!readMetadata_accessIsAllowed("private", role)) {
			return AccessDenied();
		    }
		    return ca.exec();
		} catch (HttpArchiveException e) {
		    return JsonMessage(new Message(e, e.getCode()));
		} catch (Exception e) {
		    return JsonMessage(new Message(e, 500));
		}
	    });
	}
    }

    /**
     * @author Jan Schnasse
     *
     */
    public static class ModifyAction {
	Promise<Result> call(String pid, NodeAction ca) {
	    return Promise.promise(() -> {
		try {
		    String role = (String) Http.Context.current().args
			    .get("role");
		    play.Logger.debug("Try to access with role: " + role);
		    if (!modifyingAccessIsAllowed(role)) {
			return AccessDenied();
		    }
		    Node node = null;
		    try {
			node = read.readNode(pid);
		    } catch (Exception e) {
			// play.Logger.debug("", e);
		}
		return ca.exec(node);
	    } catch (HttpArchiveException e) {
		return JsonMessage(new Message(e, e.getCode()));
	    } catch (Exception e) {
		return JsonMessage(new Message(e, 500));
	    }
	})  ;
	}
    }

    /**
     * @author Jan Schnasse
     *
     */
    public static class CreateAction {
	Promise<Result> call(Action ca) {
	    return Promise.promise(() -> {
		try {
		    String role = (String) Http.Context.current().args
			    .get("role");
		    if (!modifyingAccessIsAllowed(role)) {
			return AccessDenied();
		    }
		    return ca.exec();
		} catch (HttpArchiveException e) {
		    return JsonMessage(new Message(e, e.getCode()));
		} catch (Exception e) {
		    return JsonMessage(new Message(e, 500));
		}
	    });
	}
    }

    /**
     * @author Jan Schnasse
     *
     */
    public static class BulkAction {
	Promise<Result> call(Action ca) {
	    return Promise.promise(() -> {
		try {
		    String role = (String) Http.Context.current().args
			    .get("role");
		    if (!modifyingAccessIsAllowed(role)) {
			return AccessDenied();
		    }
		    return ca.exec();
		} catch (HttpArchiveException e) {
		    return JsonMessage(new Message(e, e.getCode()));
		} catch (Exception e) {
		    return JsonMessage(new Message(e, 500));
		}
	    });
	}
    }

    /**
     * @param map
     * @return a pritn of the map
     */
    public static String mapToString(Map<String, String> map) {
	StringBuilder sb = new StringBuilder();
	Iterator<Entry<String, String>> iter = map.entrySet().iterator();
	while (iter.hasNext()) {
	    Entry<String, String> entry = iter.next();
	    sb.append(entry.getKey());
	    sb.append('=').append('"').append(entry.getValue()).append('"');
	    if (iter.hasNext()) {
		sb.append("\n\t'");
	    }
	}
	return sb.toString();

    }
}
