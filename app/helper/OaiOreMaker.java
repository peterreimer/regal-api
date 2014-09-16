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
package helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import models.Node;
import models.ObjectType;
import models.Transformer;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.BasicWriterSettings;
import org.openrdf.rio.helpers.JSONLDMode;
import org.openrdf.rio.helpers.JSONLDSettings;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;

import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JSONUtils;

/**
 * @author Jan Schnasse schnasse@hbz-nrw.de
 * 
 */
@SuppressWarnings("serial")
public class OaiOreMaker {

    final static Logger logger = LoggerFactory.getLogger(OaiOreMaker.class);

    Node node = null;
    String dcNamespace = "http://purl.org/dc/elements/1.1/";
    String dctermsNamespace = "http://purl.org/dc/terms/";
    String foafNamespace = "http://xmlns.com/foaf/0.1/";
    String oreNamespace = "http://www.openarchives.org/ore/terms/";
    String rdfNamespace = " http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    String rdfsNamespace = "http://www.w3.org/2000/01/rdf-schema#";
    String regalNamespace = "http://hbz-nrw.de/regal#";
    String fpNamespace = "http://downlode.org/Code/RDF/File_Properties/schema#";
    String wnNamespace = "http://xmlns.com/wordnet/1.6/";
    String hydraNamespace = "http://purl.org/hydra/core#";

    RepositoryConnection con = null;

    @SuppressWarnings("javadoc")
    public OaiOreMaker(Node node) {

	this.node = node;
	try {
	    con = createRdfRepository();
	} catch (RepositoryException e) {
	    throw new CreateRepositoryException(e);
	}
    }

    /**
     * @param format
     *            application/rdf+xml text/plain application/json
     * @param parents
     *            all parents of the pid
     * @param children
     *            all children of the pid
     * @param transformers
     *            transformers of the object
     * @return a oai_ore resource map
     */
    public String getReM(String format, List<Transformer> transformers) {

	String result = null;
	addDescriptiveMetadata();
	addStructuralData(transformers);
	result = write(format);
	closeRdfRepository();
	return result;

    }

    private void addDescriptiveMetadata() {
	try {
	    con.add(new StringReader(node.getMetadata()), node.getPid(),
		    RDFFormat.N3);
	} catch (Exception e) {
	    logger.debug("", e);
	}
    }

    private String write(String format) {
	try {
	    if ("application/json+compact".equals(format)) {
		InputStream contextDocument = Play.application()
			.resourceAsStream("edoweb-resources.json");
		StringWriter out = new StringWriter();
		RDFWriter writer = null;
		writer = configureWriter("application/json", out, writer);
		String jsonString = write(out, writer);
		Object json = JSONUtils.fromString(jsonString);

		System.out.println(Play.application().resource(
			"edoweb-resources.json"));
		@SuppressWarnings("rawtypes")
		Map context = (Map) JSONUtils.fromInputStream(contextDocument);
		JsonLdOptions options = new JsonLdOptions();
		@SuppressWarnings("unchecked")
		Map<String, Object> normalized = (Map<String, Object>) expandSimpleValues((Map<String, Object>) JsonLdProcessor
			.compact(json, context, options));
		normalized.remove("@context");
		normalized.put("@context", node.getContextDocumentUri());

		return JSONUtils.toPrettyString(normalized);
	    }
	    StringWriter out = new StringWriter();
	    RDFWriter writer = null;
	    writer = configureWriter(format, out, writer);
	    return write(out, writer);
	} catch (Exception e) {
	    throw new WriteRdfException(e);
	}
    }

    @SuppressWarnings("unchecked")
    private Object expandSimpleValues(Object element) {

	if (element instanceof String) {
	    final List<Object> result = new ArrayList<Object>();
	    final Map<String, Object> actualValues = new LinkedHashMap<String, Object>();
	    actualValues.put("@value", element);
	    result.add(actualValues);
	    return result;
	}
	if (element instanceof List) {
	    final List<Object> result = new ArrayList<Object>();
	    for (final Object item : (List<Object>) element) {
		final Object compactedItem = expandSimpleValues(item);
		if (compactedItem != null) {
		    result.add(compactedItem);
		}
	    }
	    return result;
	}
	if (element instanceof Map) {

	    Map<String, Object> result = new LinkedHashMap<String, Object>();
	    for (Map.Entry<String, Object> c : ((Map<String, Object>) element)
		    .entrySet()) {
		String key = c.getKey();
		Object value = c.getValue();
		if ("@value".equals(key)) {
		    result.put(key, value);
		} else if ("@id".equals(key)) {
		    result.put(key, value);
		} else if ("@type".equals(key)) {
		    result.put(key, value);
		} else if ("@language".equals(key)) {
		    result.put(key, value);
		} else {
		    result.put(key, expandSimpleValues(value));
		}
	    }
	    return result;
	}

	return element;
    }

    private String write(StringWriter out, RDFWriter writer)
	    throws RepositoryException {
	String result = null;
	try {

	    writer.startRDF();
	    RepositoryResult<Statement> statements = con.getStatements(null,
		    null, null, false);

	    while (statements.hasNext()) {
		Statement statement = statements.next();
		writer.handleStatement(statement);
	    }
	    writer.endRDF();
	    out.flush();
	    result = out.toString();

	} catch (RDFHandlerException e) {
	    logger.error(e.getMessage());
	} finally {
	    try {
		out.close();
	    } catch (IOException e) {
		logger.error(e.getMessage());
	    }
	}

	return result;
    }

    private RDFWriter configureWriter(String format, StringWriter out,
	    RDFWriter writer) {
	if ("application/rdf+xml".equals(format)) {
	    writer = Rio.createWriter(RDFFormat.RDFXML, out);
	} else if ("text/plain".equals(format)) {
	    writer = Rio.createWriter(RDFFormat.NTRIPLES, out);
	} else if ("application/json".equals(format)) {
	    writer = Rio.createWriter(RDFFormat.JSONLD, out);
	    writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE,
		    JSONLDMode.EXPAND);
	    writer.getWriterConfig()
		    .set(BasicWriterSettings.PRETTY_PRINT, true);
	} else {
	    throw new HttpArchiveException(406, format + " is not supported");
	}
	return writer;
    }

    private RepositoryConnection createRdfRepository()
	    throws RepositoryException {
	RepositoryConnection mycon = null;
	SailRepository myRepository = new SailRepository(new MemoryStore());
	myRepository.initialize();
	mycon = myRepository.getConnection();
	return mycon;
    }

    private void closeRdfRepository() {
	try {
	    if (con != null)
		con.close();
	} catch (Exception e) {
	    throw new CreateRepositoryException(e);
	}
    }

    private void addStructuralData(List<Transformer> transformers) {
	try {
	    ValueFactory f = con.getValueFactory();
	    // Things
	    URI aggregation = f.createURI(node.getAggregationUri());
	    URI rem = f.createURI(node.getRemUri());
	    URI data = f.createURI(node.getDataUri());
	    Literal cType = f.createLiteral(node.getContentType());
	    Literal lastTimeModified = f.createLiteral(node.getLastModified());
	    Literal firstTimeCreated = f.createLiteral(node.getCreationDate());
	    String mime = node.getFileMimeType();
	    String label = node.getFileLabel();
	    String accessScheme = node.getAccessScheme();
	    String fileSize = node.getFileSizeAsString();
	    String fileChecksum = node.getFileChecksum();
	    // Predicates
	    // ore
	    URI describes = f.createURI(oreNamespace, "describes");
	    URI isDescribedBy = f.createURI(oreNamespace, "isDescribedBy");
	    URI aggregates = f.createURI(oreNamespace, "aggregates");
	    URI isAggregatedBy = f.createURI(oreNamespace, "isAggregatedBy");
	    URI similarTo = f.createURI(oreNamespace, "similarTo");
	    // dc
	    URI isPartOf = f.createURI(dctermsNamespace, "isPartOf");
	    URI hasPart = f.createURI(dctermsNamespace, "hasPart");
	    URI modified = f.createURI(dctermsNamespace, "modified");
	    URI created = f.createURI(dctermsNamespace, "created");
	    URI dcFormat = f.createURI(dctermsNamespace, "format");
	    @SuppressWarnings("unused")
	    URI dcHasFormat = f.createURI(dctermsNamespace, "hasFormat");
	    // rdfs
	    URI rdfsLabel = f.createURI(rdfsNamespace, "label");
	    URI rdfsType = f.createURI(rdfsNamespace, "type");
	    // regal
	    URI contentType = f.createURI(regalNamespace, "contentType");
	    URI hasData = f.createURI(regalNamespace, "hasData");
	    URI hasTransformer = f.createURI(regalNamespace, "hasTransformer");
	    URI hasAccessScheme = f.createURI(regalNamespace, "accessScheme");
	    // FileProperties
	    URI fpSize = f.createURI(fpNamespace, "size");
	    BNode theChecksumBlankNode = f.createBNode();
	    URI fpChecksum = f.createURI(fpNamespace, "checksum");
	    URI fpChecksumType = f.createURI(fpNamespace, "Checksum");
	    URI fpChecksumGenerator = f.createURI(fpNamespace, "generator");
	    URI fpChecksumAlgo = f.createURI(wnNamespace, "Algorithm");
	    URI md5Uri = f.createURI("http://en.wikipedia.org/wiki/MD5");
	    URI fpChecksumValue = f.createURI(fpNamespace, "checksumValue");

	    // Statements
	    if (mime != null && !mime.isEmpty()) {
		Literal dataMime = f.createLiteral(mime);
		con.add(data, dcFormat, dataMime);
		con.add(aggregation, aggregates, data);
		con.add(aggregation, hasData, data);
	    }

	    if (accessScheme != null && !accessScheme.isEmpty()) {
		Literal a = f.createLiteral(accessScheme);
		con.add(aggregation, hasAccessScheme, a);
	    }

	    if (fileSize != null && !fileSize.isEmpty()) {
		Literal dataSize = f.createLiteral(fileSize);
		con.add(data, fpSize, dataSize);
	    }

	    if (fileChecksum != null && !fileChecksum.isEmpty()) {
		Literal dataChecksum = f.createLiteral(fileChecksum);
		con.add(theChecksumBlankNode, rdfsType, fpChecksumType);
		con.add(theChecksumBlankNode, fpChecksumGenerator, md5Uri);
		con.add(md5Uri, rdfsType, fpChecksumAlgo);
		con.add(theChecksumBlankNode, fpChecksumValue, dataChecksum);
		con.add(data, fpChecksum, theChecksumBlankNode);
	    }

	    if (label != null && !label.isEmpty()) {
		Literal labelLiteral = f.createLiteral(label);
		con.add(data, rdfsLabel, labelLiteral);
	    }

	    String str = getOriginalUri(node.getPid());
	    if (str != null && !str.isEmpty()
		    && !cType.stringValue().equals(ObjectType.file.toString())) {
		URI originalObject = f.createURI(str);
		con.add(aggregation, similarTo, originalObject);

	    }

	    if (transformers != null && transformers.size() > 0) {
		for (Transformer t : transformers) {
		    Literal transformerId = f.createLiteral(t.getId());
		    con.add(aggregation, hasTransformer, transformerId);
		}
	    }

	    URI fedoraObject = f.createURI(Play.application().configuration()
		    .getString("regal-api.fedoraIntern")
		    + "/objects/" + node.getPid());

	    con.add(rem, describes, aggregation);
	    con.add(rem, modified, lastTimeModified);
	    con.add(rem, created, firstTimeCreated);

	    con.add(aggregation, isDescribedBy, rem);

	    con.add(aggregation, similarTo, fedoraObject);
	    con.add(aggregation, contentType, cType);

	    for (String rel : node.getRelatives("IS_PART_OF")) {
		URI relUrl = f.createURI(rel);
		con.add(aggregation, isAggregatedBy, relUrl);
		con.add(aggregation, isPartOf, relUrl);
	    }

	    for (String rel : node.getRelatives("HAS_PART")) {
		URI relUrl = f.createURI(rel);
		con.add(aggregation, aggregates, relUrl);
		con.add(aggregation, hasPart, relUrl);

	    }
	} catch (Exception e) {
	    logger.debug("", e);
	}
    }

    private String getOriginalUri(String pid) {
	String pidWithoutNamespace = pid.substring(pid.indexOf(':') + 1);
	String originalUri = null;
	if (pid.contains("edoweb") || pid.contains("ellinet")) {
	    if (pid.length() <= 17) {
		originalUri = "http://klio.hbz-nrw.de:1801/webclient/MetadataManager?pid="
			+ pidWithoutNamespace;

	    }
	}
	if (pid.contains("dipp")) {
	    originalUri = "http://193.30.112.23:9280/fedora/get/" + pid
		    + "/QDC";

	}
	if (pid.contains("ubm")) {
	    originalUri = "http://ubm.opus.hbz-nrw.de/frontdoor.php?source_opus="
		    + pidWithoutNamespace + "&la=de";

	}
	if (pid.contains("fhdd")) {
	    originalUri = "http://fhdd.opus.hbz-nrw.de/frontdoor.php?source_opus="
		    + pidWithoutNamespace + "&la=de";

	}
	if (pid.contains("kola")) {
	    originalUri = "http://kola.opus.hbz-nrw.de/frontdoor.php?source_opus="
		    + pidWithoutNamespace + "&la=de";

	}
	return originalUri;
    }

    private class CreateRepositoryException extends RuntimeException {
	public CreateRepositoryException(Throwable e) {
	    super(e);
	}
    }

    private class WriteRdfException extends RuntimeException {
	public WriteRdfException(Throwable e) {
	    super(e);
	}
    }

}
