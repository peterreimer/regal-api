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

import java.io.StringWriter;

import models.Message;
import play.mvc.Controller;
import play.mvc.Result;

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

    /**
     * @param obj
     *            an arbitrary object
     * @return json serialization of obj
     */
    public static Result json(Object obj) {
	StringWriter w = new StringWriter();
	try {
	    mapper.writeValue(w, obj);
	} catch (Exception e) {
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
		"POST, GET, OPTIONS, PUT, DELETE");
	response().setHeader("Access-Control-Max-Age", "3600");
	response()
		.setHeader("Access-Control-Allow-Headers",
			"Origin, X-Requested-With, Content-Type, Accept, Authorization, X-Auth-Token");
	response().setHeader("Access-Control-Allow-Credentials", "true");
	response().setHeader("Content-Type", "application/json; charset=utf-8");
	return status(msg.getCode(), msg.toString());
    }
}
