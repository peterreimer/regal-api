package archive.fedora;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import models.Link;
import models.RdfResource;

/**
 * @author Jan Schnasse
 *
 */
public class ApplicationProfile {

    private static final Map<String, String> pMap;
    static {
	Map<String, String> map = new HashMap<String, String>();

	map.put("http://purl.org/dc/terms/title", "Titel");
	map.put("http://purl.org/dc/terms/alternative", "Titelzusatz");
	map.put("http://rdvocab.info/Elements/otherTitleInformation",
		"Titelzusatz weitere");
	map.put("http://purl.org/dc/terms/title", "Titel");
	map.put("http://purl.org/ontology/bibo/shortTitle", "Kurztitel");
	map.put("http://purl.org/dc/terms/creator", "Autor");
	map.put("http://purl.org/dc/elements/1.1/contributor", "Mitwirkende");
	map.put("http://purl.org/dc/elements/1.1/creator", "Autor");
	map.put("http://rdvocab.info/Elements/statementOfResponsibility",
		"Verantwortlich");
	map.put("http://purl.org/dc/terms/contributor", "Mitwirkende");
	map.put("http://id.loc.gov/vocabulary/relators/act", "Schauspieler");
	map.put("http://id.loc.gov/vocabulary/relators/aft",
		"Autor des Nachwortes");
	map.put("http://id.loc.gov/vocabulary/relators/aui",
		"Autor der Einleitung");
	map.put("http://id.loc.gov/vocabulary/relators/aus", "Screenwriter");
	map.put("http://id.loc.gov/vocabulary/relators/clb", "");
	map.put("http://id.loc.gov/vocabulary/relators/cmp", "");
	map.put("http://id.loc.gov/vocabulary/relators/cnd", "");
	map.put("http://id.loc.gov/vocabulary/relators/cng", "");
	map.put("http://id.loc.gov/vocabulary/relators/col", "");
	map.put("http://id.loc.gov/vocabulary/relators/ctb", "");
	map.put("http://id.loc.gov/vocabulary/relators/ctg", "");
	map.put("http://id.loc.gov/vocabulary/relators/drt", "");
	map.put("http://id.loc.gov/vocabulary/relators/dte", "");
	map.put("http://id.loc.gov/vocabulary/relators/egr", "");
	map.put("http://id.loc.gov/vocabulary/relators/ill", "");
	map.put("http://id.loc.gov/vocabulary/relators/ive", "");
	map.put("http://id.loc.gov/vocabulary/relators/ivr", "");
	map.put("http://id.loc.gov/vocabulary/relators/mus", "");
	map.put("http://id.loc.gov/vocabulary/relators/pht", "");
	map.put("http://id.loc.gov/vocabulary/relators/prf", "");
	map.put("http://id.loc.gov/vocabulary/relators/pro", "");
	map.put("http://id.loc.gov/vocabulary/relators/sng", "");
	map.put("http://id.loc.gov/vocabulary/relators/hnr", "");
	map.put("http://rdvocab.info/Elements/publicationStatement",
		"Ort und Jahr");
	map.put("http://purl.org/dc/terms/source", "");
	map.put("http://rdvocab.info/Elements/placeOfPublication", "Ort");
	map.put("http://purl.org/dc/elements/1.1/publisher", "Herausgeber");
	map.put("http://purl.org/dc/terms/issued", "Erschienen");
	map.put("http://www.w3.org/2002/07/owl#sameAs", "Identisch zu");
	map.put("http://umbel.org/umbel#isLike", "Ähnlich zu");
	map.put("http://purl.org/ontology/bibo/issn", "Issn");
	map.put("http://purl.org/ontology/bibo/eissn", "Eissn");
	map.put("http://purl.org/ontology/bibo/lccn", "Lccn");
	map.put("http://purl.org/ontology/bibo/oclcnum", "Oclc");
	map.put("http://purl.org/ontology/bibo/isbn", "Isbn");
	map.put("http://purl.org/ontology/bibo/isbn13", "Isbn13");
	map.put("http://purl.org/ontology/mo/ismn", "Ismn");
	map.put("http://purl.org/lobid/lv#urn", "Urn");
	map.put("http://purl.org/ontology/bibo/doi", "Doi");
	map.put("http://purl.org/ontology/mo/wikipedia", "Wikipedia");
	map.put("http://rdvocab.info/RDARelationshipsWEMI/workManifested", "");
	map.put("http://purl.org/dc/terms/medium", "Träger");
	map.put("http://purl.org/dc/terms/hasPart", "Bestandteile");
	map.put("http://purl.org/dc/terms/isPartOf", "Bestandteil von");
	map.put("http://www.w3.org/2000/01/rdf-schema#inDataset", "");
	map.put("http://purl.org/dc/elements/1.1/isPartOf", "Bestandteil von");
	map.put("http://purl.org/dc/terms/hasVersion", "Vgl.");
	map.put("http://purl.org/dc/terms/isFormatOf", "");
	map.put("http://purl.org/dc/terms/hasFormat", "");
	map.put("http://www.w3.org/2000/01/rdf-schema#seeAlso",
		"Weitere Informationen");
	map.put("http://purl.org/dc/terms/tableOfContents",
		"Inhaltsverzeichnis");
	map.put("http://purl.org/lobid/lv#hbzID", "Katalog Id");
	map.put("http://purl.org/dc/terms/language", "Sprache");
	map.put("http://iflastandards.info/ns/isbd/elements/P1053", "");
	map.put("http://purl.org/ontology/bibo/edition", "");
	map.put("http://purl.org/dc/terms/abstract", "");
	map.put("http://purl.org/dc/terms/subject", "Schlagwort");
	map.put("http://purl.org/dc/elements/1.1/subject", "Schlagwort");
	map.put("http://purl.org/ontology/bibo/editor", "Herausgeber");
	map.put("http://purl.org/ontology/bibo/translator", "Übersetzer");
	map.put("http://purl.org/ontology/bibo/volume", "Band");
	map.put("http://purl.org/ontology/daia/label", "");
	map.put("http://purl.org/vocab/frbr/core#owner", "");
	map.put("http://www.w3.org/2007/05/powder-s#describedby",
		"Beschrieben durch");
	map.put("http://d-nb.info/standards/elementset/gnd#dateOfBirth",
		"Geboren am");
	map.put("http://d-nb.info/standards/elementset/gnd#dateOfDeath",
		"Gestorben am");
	map.put("http://d-nb.info/standards/elementset/gnd#preferredNameForThePerson",
		"Name");
	map.put("http://www.w3.org/2004/02/skos/core#prefLabel", "");
	map.put("http://xmlns.com/foaf/0.1/primaryTopic", "");
	map.put("http://rdvocab.info/Elements/longitudeAndLatitude", "");
	map.put("http://d-nb.info/standards/elementset/gnd#preferredNameForThePlaceOrGeographicName",
		"");
	map.put("http://d-nb.info/standards/elementset/gnd#preferredNameForTheSubjectHeading",
		"");
	map.put("http://d-nb.info/standards/elementset/gnd#preferredNameForTheConferenceOrEvent",
		"");
	map.put("http://d-nb.info/standards/elementset/gnd#preferredNameForTheCorporateBody",
		"");
	map.put("http://d-nb.info/standards/elementset/gnd#preferredNameEntityForThePerson",
		"");
	map.put("http://d-nb.info/standards/elementset/gnd#preferredNameForTheFamily",
		"");
	map.put("http://d-nb.info/standards/elementset/gnd#preferredNameForTheWork",
		"");
	map.put("http://d-nb.info/standards/elementset/gnd#preferredName",
		"Name");
	map.put("http://purl.org/lobid/lv#fulltextOnline", "Download");
	map.put("http://geni-orca.renci.org/owl/topology.owl#hasURN", "Urn");
	map.put("http://hbz-nrw.de/regal#contentType", "Repository Typ");
	map.put("http://www.openarchives.org/ore/terms/aggregates", "");
	map.put("http://www.openarchives.org/ore/terms/isDescribedBy",
		"Wird beschrieben durch");
	map.put("http://www.openarchives.org/ore/terms/similarTo", "Vgl.");
	map.put("http://www.umbel.org/specifications/vocabulary#isLike",
		"Ähnlich zu");
	map.put("http://purl.org/dc/terms/created", "Erstellt am");
	map.put("http://purl.org/dc/terms/modified", "Zuletzt bearbeitet");
	map.put("http://www.openarchives.org/ore/terms/describes", "");
	map.put("http://downlode.org/Code/RDF/File_Properties/schema#size",
		"Größe");
	map.put("http://downlode.org/Code/RDF/File_Properties/schema#checksumValue",
		"Prüfsumme");
	map.put("http://downlode.org/Code/RDF/File_Properties/schema#generator",
		"");
	map.put("http://www.w3.org/2000/01/rdf-schema#type", "Typ");
	map.put("http://iflastandards.info/ns/isbd/elements/P1004",
		"Gesamttitel");
	map.put("http://iflastandards.info/ns/isbd/elements/P1006",
		"Titelzusatz");
	map.put("http://iflastandards.info/ns/isbd/elements/P1016", "Ort");
	map.put("http://lobid.org/vocab/lobid#fulltextOnline",
		"Entfernter Download");
	map.put("http://www.w3.org/2000/01/rdf-schema#label", "Etikett");
	map.put("http://purl.org/dc/terms/format", "Format");
	map.put("http://downlode.org/Code/RDF/File_Properties/schema#checksum",
		"Prüfsumme");
	map.put("http://hbz-nrw.de/regal#hasTransformer", "Transformer");
	map.put("http://hbz-nrw.de/regal#hasData", "Download");
	map.put("http://www.openarchives.org/ore/terms/isAggregatedBy",
		"Teil von");
	map.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "Typ");
	map.put("http://xmlns.com/foaf/0.1/isPrimaryTopicOf", "Gegenstand von");
	map.put("info:hbz/hbz-ingest:def/model#isNodeType", "Fedora Typ");
	map.put("info:fedora/fedora-system:def/relations-external#isMemberOf",
		"Oai Set");
	map.put("http://www.openarchives.org/OAI/2.0/itemID", "Oai Id");
	map.put("info:fedora/fedora-system:def/relations-external#hasPart",
		"Kindobjekte");
	map.put("info:fedora/fedora-system:def/relations-external#isPartOf",
		"Elternobjekt");
	map.put("http://purl.org/vocab/frbr/core#exemplar", "Exemplar");
	map.put("info:hbz/hbz-ingest:def/model#contentType", "Regal Typ");
	map.put("info:hbz/hbz-ingest:def/model#accessScheme",
		"Sichtbarkeit Daten");
	map.put("info:hbz/hbz-ingest:def/model#publishScheme",
		"Sichtbarkeit Metadaten");
	map.put("http://purl.org/dc/terms/bibliographicCitation",
		"Veröffentlichungs Details");

	pMap = Collections.unmodifiableMap(map);
    }

    /**
     * Creates new RdfResource with labels for objects and predicates
     * 
     * @param r
     *            a RdfResource
     * 
     * @return new RdfResource with labels for objects and predicates
     */
    public static RdfResource addLabels(final RdfResource r) {
	RdfResource result = new RdfResource(r.getUri());
	for (Link l : r.getLinks()) {
	    l.setPredicateLabel(getPredicateLabel(l.getPredicate()));
	    if (l.isLiteral()) {
		l.setObjectLabel(l.getObject());
	    }
	    result.addLink(l);
	}
	return result;
    }

    /**
     * @param key
     * @return a label or, if not available the key itself
     */
    public static String getPredicateLabel(String key) {
	String value = pMap.get(key);
	if (value != null && !value.isEmpty())
	    return value;
	return key;
    }
}
