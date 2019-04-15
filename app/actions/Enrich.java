package actions;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.w3c.dom.Element;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import archive.fedora.RdfUtils;
import archive.fedora.XmlUtils;
import helper.MyEtikettMaker;
import models.Globals;
import models.Node;

public class Enrich {
	private static final String alternateName =
			"http://www.geonames.org/ontology#alternateName";
	private static final String first =
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#first";
	private static final String rest =
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#rest";
	private static final String nil =
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil";

	private static final String PREF_LABEL =
			"http://www.w3.org/2004/02/skos/core#prefLabel";

	public static String enrichMetadata2(Node node) {
		try {
			play.Logger.info("Enrich 2 " + node.getPid());
			String metadata = node.getMetadata2();
			if (metadata == null || metadata.isEmpty()) {
				play.Logger.info("No metadata2 to enrich " + node.getPid());
				return "No metadata2 to enrich " + node.getPid();
			}
			List<Statement> enrichStatements = new ArrayList<>();
			enrichAll(node, metadata, enrichStatements);
			metadata = RdfUtils.replaceTriples(enrichStatements, metadata);
			new Modify().updateMetadata2(node, metadata);
		} catch (Exception e) {
			play.Logger.debug("", e);
			return "Enrichment of " + node.getPid() + " partially failed !\n"
					+ e.getMessage();
		}
		return "Enrichment of " + node.getPid() + " succeeded!";
	}

	public static String enrichMetadata(Node node) {
		try {
			play.Logger.info("Enrich " + node.getPid());
			String metadata = node.getMetadata1();
			if (metadata == null || metadata.isEmpty()) {
				play.Logger.info("Not metadata to enrich " + node.getPid());
				return "Not metadata to enrich " + node.getPid();
			}
			List<Statement> enrichStatements = new ArrayList<>();
			enrichAll(node, metadata, enrichStatements);
			metadata = RdfUtils.replaceTriples(enrichStatements, metadata);
			new Modify().updateMetadata1(node, metadata);

		} catch (Exception e) {
			play.Logger.warn(e.getMessage());
			play.Logger.debug("", e);
			return "Enrichment of " + node.getPid() + " partially failed !\n"
					+ e.getMessage();
		}
		return "Enrichment of " + node.getPid() + " succeeded!";
	}

	private static void enrichAll(Node node, String metadata,
			List<Statement> enrichStatements) {
		enrichGnd(node, metadata, enrichStatements);

		enrichGeonames(node, metadata, enrichStatements);

		enrichOsm(node, metadata, enrichStatements);

		enrichOrcid(node, metadata, enrichStatements);

		enrichAdhocIds(node, metadata, enrichStatements);

		enrichAgrovoc(node, metadata, enrichStatements);

		enrichInstitution(node, enrichStatements);

		enrichIsPartOf(node, metadata, enrichStatements);

		enrichSeries(node, metadata, enrichStatements);

		enrichMultivolumeWork(node, metadata, enrichStatements);

		enrichLanguage(node, metadata, enrichStatements);

		enrichRecordingLocation(node, metadata, enrichStatements);

		enrichCollectionOne(node, metadata, enrichStatements);

		enrichCollectionTwo(node, metadata, enrichStatements);

		enrichContainedIn(node, metadata, enrichStatements);

		enrichFundingId(node, metadata, enrichStatements);
	}

	private static void enrichFundingId(Node node, String metadata,
			List<Statement> enrichStatements) {
		try {
			play.Logger.debug("ENRICH FUNDING!-----------------------------");
			enrichStatements
					.addAll(find(node, metadata, "http://hbz-nrw.de/regal#fundingId"));
		} catch (Exception e) {
			play.Logger.debug("", e);
		}
	}

	private static void enrichContainedIn(Node node, String metadata,
			List<Statement> enrichStatements) {
		try {
			List<Statement> containedIn =
					find(node, metadata, "http://purl.org/lobid/lv#containedIn");
			enrichStatements.addAll(containedIn);
		} catch (Exception e) {
			play.Logger.debug("", e);
		}
	}

	private static void enrichCollectionTwo(Node node, String metadata,
			List<Statement> enrichStatements) {
		try {
			List<Statement> collectionTwo =
					find(node, metadata, "info:regal/zettel/collectionTwo");
			enrichStatements.addAll(collectionTwo);
		} catch (Exception e) {
			play.Logger.debug("", e);
		}
	}

	private static void enrichCollectionOne(Node node, String metadata,
			List<Statement> enrichStatements) {
		List<Statement> collectionOne =
				find(node, metadata, "info:regal/zettel/collectionOne");
		enrichStatements.addAll(collectionOne);
	}

	private static void enrichRecordingLocation(Node node, String metadata,
			List<Statement> enrichStatements) {
		try {
			play.Logger.info("Enrich " + node.getPid() + " with recordingLocation.");
			List<Statement> recordingLocation =
					find(node, metadata, "http://hbz-nrw.de/regal#recordingLocation");
			enrichStatements.addAll(recordingLocation);
		} catch (Exception e) {
			play.Logger.debug("", e);
		}
	}

	private static void enrichLanguage(Node node, String metadata,
			List<Statement> enrichStatements) {
		try {
			play.Logger.info("Enrich " + node.getPid() + " with language.");
			List<Statement> language =
					find(node, metadata, "http://purl.org/dc/terms/language");
			enrichStatements.addAll(language);
		} catch (Exception e) {
			play.Logger.debug("", e);
		}
	}

	private static void enrichMultivolumeWork(Node node, String metadata,
			List<Statement> enrichStatements) {
		try {
			play.Logger.info("Enrich " + node.getPid() + " with multiVolumeWork.");
			List<Statement> multiVolumeWork =
					find(node, metadata, "http://purl.org/lobid/lv#multiVolumeWork");
			enrichStatements.addAll(multiVolumeWork);
		} catch (Exception e) {
			play.Logger.debug("", e);
		}
	}

	private static void enrichSeries(Node node, String metadata,
			List<Statement> enrichStatements) {
		try {
			play.Logger.info("Enrich " + node.getPid() + " with inSeries.");
			List<Statement> series =
					find(node, metadata, "http://purl.org/lobid/lv#series");
			enrichStatements.addAll(series);
		} catch (Exception e) {
			play.Logger.debug("", e);
		}
	}

	private static void enrichIsPartOf(Node node, String metadata,
			List<Statement> enrichStatements) {
		try {
			play.Logger.info("Enrich " + node.getPid() + " with parent.");
			List<Statement> catalogParents =
					find(node, metadata, "http://purl.org/dc/terms/isPartOf");
			enrichStatements.addAll(catalogParents);
		} catch (Exception e) {
			play.Logger.debug("", e);
		}
	}

	private static void enrichInstitution(Node node,
			List<Statement> enrichStatements) {
		try {
			play.Logger.info("Enrich " + node.getPid() + " with institution.");
			List<Statement> institutions = findInstitution(node);
			enrichStatements.addAll(institutions);
		} catch (Exception e) {
			play.Logger.debug("", e);
		}
	}

	private static void enrichAgrovoc(Node node, String metadata,
			List<Statement> enrichStatements) {
		try {
			play.Logger.info("Enrich " + node.getPid() + " with agrovoc.");
			List<String> agrovocIds = findAllAgrovocIds(metadata);
			for (String uri : agrovocIds) {
				enrichStatements.addAll(getAgrovocStatements(uri));
			}
		} catch (Exception e) {
			play.Logger.debug("", e);
		}
	}

	private static void enrichAdhocIds(Node node, String metadata,
			List<Statement> enrichStatements) {
		try {
			play.Logger.info("Enrich " + node.getPid() + " with adhoc keys.");
			List<String> adhocIds = findAllAdhocIds(metadata);
			for (String uri : adhocIds) {
				enrichStatements.addAll(getAdhocStatements(uri));
			}
		} catch (Exception e) {
			play.Logger.debug("", e);
		}
	}

	private static void enrichOrcid(Node node, String metadata,
			List<Statement> enrichStatements) {
		try {
			play.Logger.info("Enrich " + node.getPid() + " with orcid.");
			List<String> orcidIds = findAllOrcidIds(metadata);
			for (String uri : orcidIds) {
				enrichStatements.addAll(getOrcidStatements(uri));
			}
		} catch (Exception e) {
			play.Logger.debug("", e);
		}
	}

	private static void enrichOsm(Node node, String metadata,
			List<Statement> enrichStatements) {
		try {
			play.Logger.info("Enrich " + node.getPid() + " with openstreetmap.");
			List<String> osmIds = findAllOsmIds(metadata);
			for (String uri : osmIds) {
				enrichStatements.addAll(getOsmStatements(uri));
			}
		} catch (Exception e) {
			play.Logger.debug("", e);
		}
	}

	private static void enrichGeonames(Node node, String metadata,
			List<Statement> enrichStatements) {
		try {
			play.Logger.info("Enrich " + node.getPid() + " with geonames.");
			List<String> geoNameIds = findAllGeonameIds(metadata);
			for (String uri : geoNameIds) {
				enrichStatements.addAll(getGeonamesStatements(uri));
			}
		} catch (Exception e) {
			play.Logger.debug("", e);
		}
	}

	private static void enrichGnd(Node node, String metadata,
			List<Statement> enrichStatements) {
		try {
			play.Logger.info("Enrich " + node.getPid() + " with gnd.");
			List<String> gndIds = findAllGndIds(metadata);
			for (String uri : gndIds) {
				enrichStatements.addAll(getStatements(uri));
			}
		} catch (Exception e) {
			play.Logger.debug("", e);
		}
	}

	private static List<Statement> find(Node node, String metadata, String pred) {
		List<Statement> result = new ArrayList<Statement>();
		// getIsPartOf

		List<String> statements = RdfUtils.findRdfObjects(node.getPid(), pred,
				metadata, RDFFormat.NTRIPLES);
		for (String p : statements) {
			ValueFactory v = RdfUtils.valueFactory;
			String label = getEtikett(p);
			Statement st = v.createStatement(v.createIRI(p), v.createIRI(PREF_LABEL),
					v.createLiteral(Normalizer.normalize(label, Normalizer.Form.NFKC)));
			result.add(st);
			play.Logger.debug(
					"Found on " + pred + " -> object: " + st.getObject().stringValue()
							+ " -> subject: " + st.getSubject().stringValue());
		}
		return result;
	}

	private static String getEtikett(String p) {
		String prefLabel = MyEtikettMaker.getLabelFromEtikettWs(p);
		return prefLabel;
	}

	private static List<Statement> findInstitution(Node node) {
		List<Statement> result = new ArrayList<Statement>();
		try {
			String alephid = new Read().getIdOfParallelEdition(node);
			String uri = Globals.lobidHbz01 + alephid + "/about?format=source";
			play.Logger.info("GET " + uri);
			try (InputStream in =
					RdfUtils.urlToInputStream(new URL(uri), "application/xml")) {
				String gndEndpoint = "http://d-nb.info/gnd/";
				List<Element> institutionHack = XmlUtils.getElements(
						"//datafield[@tag='078' and @ind1='r' and @ind2='1']/subfield", in,
						null);

				for (Element el : institutionHack) {
					String marker = el.getTextContent();
					if (!marker.contains("ellinet"))
						continue;
					if (!marker.contains("GND"))
						continue;
					String gndId = gndEndpoint
							+ marker.replaceFirst(".*ellinet.*GND:.*\\([^)]*\\)", "");
					if (gndId.endsWith("16269969-4")) {
						gndId = gndEndpoint + "2006655-7";
					}
					play.Logger.trace("Add data from " + gndId);
					ValueFactory v = RdfUtils.valueFactory;
					Statement link = v.createStatement(v.createIRI(node.getPid()),
							v.createIRI("http://dbpedia.org/ontology/institution"),
							v.createIRI(gndId));
					result.add(link);
					result.addAll(getStatements(gndId));
				}

			}
		} catch (Exception e) {
			play.Logger.info("No institution found for " + node.getPid());

		}
		play.Logger.info("ADD to collection: " + result);
		return result;
	}

	private static List<Statement> getStatements(String uri) {
		play.Logger.info("GET " + uri);
		List<Statement> filteredStatements = new ArrayList<Statement>();
		try {
			for (Statement s : RdfUtils.readRdfToGraph(new URL(uri + "/about/lds"),
					RDFFormat.RDFXML, "application/rdf+xml")) {
				boolean isLiteral = s.getObject() instanceof Literal;
				if (!(s.getSubject() instanceof BNode)) {
					if (isLiteral) {
						ValueFactory v = RdfUtils.valueFactory;

						play.Logger.trace("Get data from " + uri);
						Statement newS = v.createStatement(v.createIRI(uri),
								s.getPredicate(), v.createLiteral(Normalizer.normalize(
										s.getObject().stringValue(), Normalizer.Form.NFKC)));
						filteredStatements.add(newS);
					}
				}
			}
		} catch (Exception e) {
			play.Logger.warn("Not able to get data from" + uri, e);
		}
		return filteredStatements;
	}

	private static List<Statement> getOrcidStatements(String uri) {
		play.Logger.trace("GET " + uri);
		List<Statement> filteredStatements = new ArrayList<Statement>();
		try (InputStream in =
				RdfUtils.urlToInputStream(new URL(uri), "application/json")) {
			String str =
					CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8));
			JsonNode hit = new ObjectMapper().readValue(str, JsonNode.class);
			String label = hit.at("/person/name/family-name/value").asText() + ", "
					+ hit.at("/person/name/given-names/value").asText();
			ValueFactory v = RdfUtils.valueFactory;
			Literal object =
					v.createLiteral(Normalizer.normalize(label, Normalizer.Form.NFKC));
			Statement newS =
					v.createStatement(v.createIRI(uri), v.createIRI(PREF_LABEL), object);
			play.Logger.trace("Get data from " + uri + " " + newS);
			filteredStatements.add(newS);
		} catch (Exception e) {
			play.Logger.warn("", e);
		}
		return filteredStatements;
	}

	private static List<Statement> getAdhocStatements(String uri) {
		play.Logger.info("GET " + uri);
		List<Statement> filteredStatements = new ArrayList<Statement>();
		try {
			for (Statement s : RdfUtils.readRdfToGraph(new URL(uri), RDFFormat.RDFXML,
					"application/rdf+xml")) {
				boolean isLiteral = s.getObject() instanceof Literal;
				if (!(s.getSubject() instanceof BNode)) {
					if (isLiteral) {
						ValueFactory v = RdfUtils.valueFactory;

						play.Logger.trace("Get data from " + uri);
						Statement newS = v.createStatement(v.createIRI(uri),
								s.getPredicate(), v.createLiteral(Normalizer.normalize(
										s.getObject().stringValue(), Normalizer.Form.NFKC)));
						filteredStatements.add(newS);
					}
				}
			}
		} catch (Exception e) {
			play.Logger.warn("Not able to get data from" + uri, e);
		}
		return filteredStatements;
	}

	private static List<Statement> getOsmStatements(String uri) {
		play.Logger.trace("GET " + uri);
		List<Statement> filteredStatements = new ArrayList<Statement>();
		try {
			URL url = new URL(uri);
			Map<String, String> map = new LinkedHashMap<String, String>();
			String query = url.getQuery();
			for (String pair : query.split("&")) {
				String[] keyValue = pair.split("=");
				int idx = pair.indexOf("=");
				map.put(URLDecoder.decode(keyValue[0], "UTF-8"),
						URLDecoder.decode(keyValue[1], "UTF-8"));
			}
			ValueFactory v = RdfUtils.valueFactory;
			Literal object = v.createLiteral(Normalizer.normalize(
					map.get("mlat") + "," + map.get("mlon"), Normalizer.Form.NFKC));
			Statement newS =
					v.createStatement(v.createIRI(uri), v.createIRI(PREF_LABEL), object);
			play.Logger.trace("Get data from " + uri + " " + newS);
			filteredStatements.add(newS);
		} catch (Exception e) {
			play.Logger.warn(e.getMessage());
			play.Logger.debug("", e);
		}
		return filteredStatements;
	}

	private static List<Statement> getGeonamesStatements(String uri) {
		play.Logger.trace("GET " + uri);
		ValueFactory vf = SimpleValueFactory.getInstance();
		List<Statement> filteredStatements = new ArrayList<Statement>();
		List<Literal> alternateNames = new ArrayList<Literal>();
		try {
			for (Statement s : RdfUtils.readRdfToGraph(new URL(uri + "/about.rdf"),
					RDFFormat.RDFXML, "application/rdf+xml")) {
				boolean isLiteral = s.getObject() instanceof Literal;
				if (!(s.getSubject() instanceof BNode)) {
					if (isLiteral) {
						Literal l = (Literal) s.getObject();
						Literal object = vf.createLiteral(Normalizer
								.normalize(s.getObject().stringValue(), Normalizer.Form.NFKC),
								l.getLanguage().get());
						Statement newS =
								vf.createStatement(vf.createIRI(uri), s.getPredicate(), object);
						play.Logger.trace("Get data from " + uri + " " + newS);

						if (alternateName.equals(s.getPredicate().stringValue())) {
							newS = vf.createStatement(vf.createIRI(uri), vf.createIRI(
									s.getPredicate().stringValue() + "_" + object.getLanguage()),
									object);
						}
						filteredStatements.add(newS);
					}
				}
			}
		} catch (Exception e) {
			play.Logger.warn("Not able to get data from" + uri);
		}

		return filteredStatements;
	}

	private static List<Statement> getAgrovocStatements(String uri) {
		play.Logger.trace("GET " + uri);
		ValueFactory vf = SimpleValueFactory.getInstance();
		List<Statement> filteredStatements = new ArrayList<Statement>();
		List<Literal> prefLabel = new ArrayList<Literal>();
		try {
			for (Statement s : RdfUtils.readRdfToGraph(new URL(uri), RDFFormat.RDFXML,
					"application/rdf+xml")) {
				boolean isLiteral = s.getObject() instanceof Literal;
				if (!(s.getSubject() instanceof BNode)) {
					if (isLiteral) {
						Literal l = (Literal) s.getObject();
						Literal object = vf.createLiteral(Normalizer
								.normalize(s.getObject().stringValue(), Normalizer.Form.NFKC),
								l.getLanguage().get());
						Statement newS =
								vf.createStatement(vf.createIRI(uri), s.getPredicate(), object);
						play.Logger.trace("Get data from " + uri + " " + newS);

						if (PREF_LABEL.equals(s.getPredicate().stringValue())) {
							if ("de".equals(object.getLanguage())) {
								newS = vf.createStatement(vf.createIRI(uri), s.getPredicate(),
										object);
								filteredStatements.add(newS);
							}
							newS = vf.createStatement(vf.createIRI(uri), vf.createIRI(
									s.getPredicate().stringValue() + "_" + object.getLanguage()),
									object);
						}
						filteredStatements.add(newS);
					}
				}
			}
		} catch (Exception e) {
			play.Logger.warn("Not able to get data from" + uri);
		}

		return filteredStatements;
	}

	private static List<Statement> getListAsStatements(List<Literal> list,
			String uri, String predicate) {
		List<Statement> listStatements = new ArrayList<Statement>();
		ValueFactory vf = SimpleValueFactory.getInstance();
		BNode head = vf.createBNode();
		Statement newS =
				vf.createStatement(vf.createIRI(uri), vf.createIRI(predicate), head);
		listStatements.add(newS);

		Resource cur = head;
		for (Literal l : list) {
			BNode r = vf.createBNode();
			Statement linkToRest = vf.createStatement(cur, vf.createIRI(rest), r);
			Statement linkToValue = vf.createStatement(cur, vf.createIRI(first), l);
			cur = r;
			listStatements.add(linkToRest);
			listStatements.add(linkToValue);
		}
		Statement endOfList = listStatements.get(listStatements.size() - 1);
		listStatements.remove(listStatements.size() - 1);
		Statement linkToNill = vf.createStatement(endOfList.getSubject(),
				vf.createIRI(rest), vf.createIRI(nil));
		listStatements.add(linkToNill);
		return listStatements;
	}

	private static List<String> findAllGndIds(String metadata) {
		HashMap<String, String> result = new HashMap<String, String>();
		Matcher m = Pattern.compile("http://d-nb.info/gnd/[1234567890-]*[A-Z]*")
				.matcher(metadata);
		while (m.find()) {
			String id = m.group();
			result.put(id, id);
		}
		return new Vector<String>(result.keySet());
	}

	private static List<String> findAllGeonameIds(String metadata) {
		HashMap<String, String> result = new HashMap<String, String>();
		Matcher m = Pattern.compile("http://www.geonames.org/[1234567890-]*")
				.matcher(metadata);
		while (m.find()) {
			String id = m.group();
			result.put(id, id);
		}
		return new Vector<String>(result.keySet());
	}

	private static List<String> findAllOsmIds(String metadata) {
		HashMap<String, String> result = new HashMap<String, String>();
		Matcher m =
				Pattern.compile("http://www.openstreetmap.org/[^>]*").matcher(metadata);
		while (m.find()) {
			String id = m.group();
			result.put(id, id);
		}
		return new Vector<String>(result.keySet());
	}

	private static List<String> findAllOrcidIds(String metadata) {
		HashMap<String, String> result = new HashMap<>();
		Matcher m = Pattern.compile("https?://orcid.org/[^>]*").matcher(metadata);
		while (m.find()) {
			String id = m.group();
			result.put(id, id);
		}
		return new Vector<String>(result.keySet());
	}

	private static List<String> findAllAdhocIds(String metadata) {
		HashMap<String, String> result = new HashMap<>();
		Matcher m =
				Pattern.compile(Globals.protocol + Globals.server + "/adhoc/[^>]*")
						.matcher(metadata);
		while (m.find()) {
			String id = m.group();
			result.put(id, id);
		}
		return new Vector<String>(result.keySet());
	}

	private static List<String> findAllAgrovocIds(String metadata) {
		HashMap<String, String> result = new HashMap<>();
		Matcher m = Pattern.compile("http://aims.fao.org/aos/agrovoc/[^>]*")
				.matcher(metadata);
		while (m.find()) {
			String id = m.group();
			result.put(id, id);
		}
		return new Vector<String>(result.keySet());
	}

}
