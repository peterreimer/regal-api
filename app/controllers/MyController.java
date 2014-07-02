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

import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.core.util.JsonUtil;

public class MyController extends Controller {

    protected static ObjectMapper mapper = JsonUtil.mapper();

    public static Result JsonResponse(Object obj) {
	return JsonResponse(obj, 200);
    }

    public static Result JsonResponse(Object obj, int code) {
	response().setHeader("Access-Control-Allow-Methods",
		"POST, GET, OPTIONS, PUT, DELETE");
	response().setHeader("Access-Control-Max-Age", "3600");
	response()
		.setHeader("Access-Control-Allow-Headers",
			"Origin, X-Requested-With, Content-Type, Accept, Authorization, X-Auth-Token");
	response().setHeader("Access-Control-Allow-Credentials", "true");
	response().setHeader("Content-Type", "application/json; charset=utf-8");

	StringWriter w = new StringWriter();
	try {
	    mapper.writeValue(w, obj);
	} catch (Exception e) {
	    return internalServerError("Not able to create response!");
	}
	switch (code) {
	case 500:
	    return internalServerError(w.toString());
	case 404:
	    return notFound(w.toString());
	case 400:
	    return badRequest(w.toString());
	case 200:
	    return ok(w.toString());
	}
	return internalServerError(w.toString());
    }
}
