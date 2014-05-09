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
