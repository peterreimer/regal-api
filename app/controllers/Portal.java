package controllers;

import helper.Globals;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.PathParam;
import models.Message;
import org.elasticsearch.search.SearchHit;
import play.mvc.Result;
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
	List<SearchHit> hits = Arrays.asList(Globals.search
		.listResources("public_edoweb", null, 0, 20).getHits());
	return ok(search.render(hits,
		"http://edoweb-anonymous:nopassword@localhost:9000/resource/"));
    }

    /**
     * @param path
     *            path to past forward to 9200
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
