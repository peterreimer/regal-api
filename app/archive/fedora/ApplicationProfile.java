package archive.fedora;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

import play.Play;
import models.Link;
import models.RdfResource;

/**
 * @author Jan Schnasse
 *
 */
public class ApplicationProfile {

    private Map<String, String> pMap = new HashMap<String, String>();

    public ApplicationProfile() {
	loadToMap("regal.ntriple");
	loadToMap("rpb.ntriple");
    }

    private void loadToMap(String fileName) {
	Graph g = RdfUtils.readRdfToGraph(
		Play.application().resourceAsStream(fileName),
		RDFFormat.NTRIPLES, "");
	Iterator<Statement> statements = g.iterator();

	while (statements.hasNext()) {
	    Statement st = statements.next();
	    if ("http://www.w3.org/2004/02/skos/core#prefLabel".equals(st
		    .getPredicate().stringValue())) {
		pMap.put(st.getSubject().stringValue(), st.getObject()
			.stringValue());
		play.Logger.debug(st.getSubject().stringValue() + ","
			+ st.getObject().stringValue());
	    }
	}
    }

    /**
     * Creates new RdfResource with labels for objects and predicates
     * 
     * @param r
     *            a RdfResource
     * 
     * @return new RdfResource with labels for objects and predicates
     */
    public RdfResource addLabels(final RdfResource r) {
	RdfResource result = new RdfResource(r.getUri());
	for (Link l : r.getLinks()) {

	    String label = pMap.get(l.getPredicate());
	    if (label == null) {
		l.setPredicateLabel("No Label");
	    } else {
		l.setPredicateLabel(label);
	    }

	    if (!l.isLiteral() && l.getObjectLabel() == null) {

		label = pMap.get(l.getObject());
		if (label == null)
		    l.setObjectLabel("No Label");
		else
		    l.setObjectLabel(label);
	    }
	    result.addLink(l);
	}
	return result;
    }
}
