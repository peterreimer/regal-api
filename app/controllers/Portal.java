package controllers;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.PathParam;

import models.Message;

import org.elasticsearch.search.SearchHit;

import archive.search.SearchFacade;
import play.mvc.Result;
import play.*;
import views.html.*;

/**
 * 
 * @author Jan Schnasse
 * 
 */
public class Portal extends MyController {

    /**
     * @return Welcome page
     */
    static public Result index() {
	return ok(portal.render("Your new application is ready."));
    }

    /**
     * @return search page
     */
    static public Result opensearch() {
	String escluster = Play.application().configuration()
		.getString("regal-api.escluster");
	SearchFacade s = new SearchFacade(escluster);

	s.init(Play.application().configuration()
		.getString("regal-api.namespace").split("\\s*,[,\\s]*"),
		"public-index-config.json");
	List<SearchHit> hits = Arrays.asList(s.listResources("public_edoweb",
		null, 0, 20).getHits());
	// String servername = Play.application().configuration()
	// .getString("regal-api.serverName");
	return ok(search.render(hits,
		"http://edoweb-anonymous:nopassword@localhost:9000/resource/"));
    }

    /**
     * @return search page
     */
    static public Result search(@PathParam("path") String path) {
	try {
	    URL url = new URL("http://localhost:9200/" + path);
	    HttpURLConnection connection = (HttpURLConnection) url
		    .openConnection();
	    InputStream is = connection.getInputStream();
	    response().setContentType(connection.getContentType());
	    return ok(is);
	} catch (Exception e) {
	    return JsonMessage(new Message(e, 500));
	}
    }
}
