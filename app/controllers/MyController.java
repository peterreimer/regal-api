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
import helper.HttpArchiveException;

import java.io.StringWriter;

import models.Message;
import models.Node;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import actions.CreateNode;
import actions.DeleteNode;
import actions.IndexNode;
import actions.ModifyNode;
import actions.ReadNode;
import actions.TransformNode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.core.util.JsonUtil;

import views.html.*;

/**
 * 
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 * 
 */
public class MyController extends Controller {

    protected static ObjectMapper mapper = JsonUtil.mapper();

    static ReadNode read = new ReadNode();
    static CreateNode create = new CreateNode();
    static IndexNode index = new IndexNode();
    static ModifyNode modify = new ModifyNode();
    static DeleteNode delete = new DeleteNode();
    static TransformNode transform = new TransformNode();

    /**
     * @return Html or Json Output
     */
    public static Result AccessDenied() {
	Message msg = new Message("Access Denied!", 401);
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
    public static Result json(Object obj) {
	setJsonHeader();
	StringWriter w = new StringWriter();
	try {
	    mapper.writeValue(w, obj);
	} catch (Exception e) {
	    e.printStackTrace();
	    return internalServerError("Not able to create response!");
	}
	return ok(w.toString());
    }

    /**
     * @param msg
     *            the msg will be rendered as html using message view
     * @return a html rendering of msg
     */
    public static Result HtmlMessage(Message msg) {
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
	return status(msg.getCode(), msg.toString());
    }

    /**
     * @param accessScheme
     *            the accessScheme of the object
     * @param role
     *            the role of the user
     * @return true if the user is allowed to read the object
     */
    public static boolean readAccessIsAllowed(String accessScheme, String role) {
	if (!"edoweb-admin".equals(role)) {
	    if ("public".equals(accessScheme)) {
		return true;
	    } else if ("restricted".equals(accessScheme)) {
		if ("edoweb-editor".equals(role)
			|| "edoweb-reader".equals(role)) {
		    return true;
		}
	    } else if ("private".equals(accessScheme)) {
		if ("edoweb-editor".equals(role))
		    return true;
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

    interface ControllerAction {
	Result exec(Node node);
    }

    /**
     * @author Jan Schnasse
     *
     */
    public static class ReadAction {
	Result call(String pid, ControllerAction ca) {
	    try {
		Node node = read.readNode(pid);
		String role = (String) Http.Context.current().args.get("role");
		String accessScheme = node.getAccessScheme();
		if (!readAccessIsAllowed(accessScheme, role)) {
		    return AccessDenied();
		}
		return ca.exec(node);
	    } catch (HttpArchiveException e) {
		return JsonMessage(new Message(e, e.getCode()));
	    } catch (Exception e) {
		return JsonMessage(new Message(e, 500));
	    }
	}
    }

    /**
     * @author Jan Schnasse
     *
     */
    public static class ModifyAction {
	Result call(String pid, ControllerAction ca) {
	    try {
		String role = (String) Http.Context.current().args.get("role");
		if (!modifyingAccessIsAllowed(role)) {
		    return AccessDenied();
		}
		Node node = null;
		try {
		    node = read.readNode(pid);
		} catch (HttpArchiveException e) {

		}
		return ca.exec(node);
	    } catch (HttpArchiveException e) {
		return JsonMessage(new Message(e, e.getCode()));
	    } catch (Exception e) {
		return JsonMessage(new Message(e, 500));
	    }
	}
    }
}
