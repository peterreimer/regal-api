package archive.fedora;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import models.Link;
import models.RdfResource;

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

import play.Play;

/**
 * @author Jan Schnasse
 *
 */
public class ApplicationProfile {

    /**
     * The class analyses values linked by this certain rdf predicate
     */
    public final static String prefLabel = "http://www.w3.org/2004/02/skos/core#prefLabel";

    /**
     * A map with URIs as key and lables as values
     */
    public Map<String, String> pMap = new HashMap<String, String>();

    private final String defaultMap = "/tmp/regal-default.ntriple";

    /**
     * Associates lables to rdf predicates or known objects
     */
    public ApplicationProfile() {
	loadDefaultConfig();
	loadToMap("regal.ntriple");
	loadToMap("rpb.ntriple");
    }

    private void loadDefaultConfig() {
	try {
	    loadToMap(new FileInputStream(new File(defaultMap)));
	} catch (Exception e) {
	    play.Logger.info("Default config file " + defaultMap
		    + " not found.");
	}
    }

    private void loadToMap(String fileName) {
	try {
	    InputStream in = Play.application().resourceAsStream(fileName);
	    loadToMap(in);
	} catch (Exception e) {
	    e.printStackTrace();
	    play.Logger.info("config file " + fileName + " not found.");
	}
    }

    private void loadToMap(InputStream in) {
	Graph g = RdfUtils.readRdfToGraph(in, RDFFormat.NTRIPLES, "");
	Iterator<Statement> statements = g.iterator();
	while (statements.hasNext()) {
	    Statement st = statements.next();
	    if (prefLabel.equals(st.getPredicate().stringValue())) {
		pMap.put(st.getSubject().stringValue(), st.getObject()
			.stringValue());
	    }
	}
    }

    /**
     * stores the pmap to a file. Each entry is represented in one row forming
     * an ntriple key prefLabel value
     */
    public void saveMap() {
	play.Logger.info("Write labels to map please hold on!!!");
	String result = new String();
	Set<Entry<String, String>> set = pMap.entrySet();
	for (Entry<String, String> e : set) {
	    result = RdfUtils.addTriple(e.getKey(), prefLabel, e.getValue(),
		    true, result);
	}
	XmlUtils.newStringToFile(new File(defaultMap), result);
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

	    } else {
		l.setPredicateLabel(label);
	    }

	    if (!l.isLiteral() && l.getObjectLabel() == null) {

		label = pMap.get(l.getObject());
		if (label == null) {
		} else
		    l.setObjectLabel(label);
	    }
	    result.addLink(l);
	}
	return result;
    }
}
