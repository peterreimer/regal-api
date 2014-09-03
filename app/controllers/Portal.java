package controllers;

import java.util.Arrays;
import java.util.List;

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
    static public Result search() {
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

}
