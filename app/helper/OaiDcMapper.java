package helper;

import java.util.List;

import models.DublinCoreData;
import models.Link;
import models.Node;

/**
 * @author Jan Schnasse
 *
 */
public class OaiDcMapper {
    Node node = null;

    /**
     * @param node
     *            the node will be mapped by getData to DublinCore
     */
    public OaiDcMapper(Node node) {
	this.node = node;
    }

    /**
     * @return DublinCore object of node
     */
    public DublinCoreData getData() {

	DublinCoreData data = new DublinCoreData();
	if (node == null)
	    return data;
	List<Link> ld = node.getLinks();
	for (Link l : ld) {
	    if ("http://purl.org/dc/terms/title".equals(l.getPredicate())) {
		data.addTitle(l.getObject());
	    } else if ("http://purl.org/dc/terms/creator".equals(l
		    .getPredicate())) {
		data.addCreator(l.getObject());
	    } else if ("http://purl.org/dc/terms/issued".equals(l
		    .getPredicate())) {
		data.addDate(l.getObject());
	    } else if ("http://purl.org/dc/elements/1.1/contributor".equals(l
		    .getPredicate())) {
		data.addContributor(l.getObject());
	    } else if ("http://purl.org/dc/elements/1.1/creator".equals(l
		    .getPredicate())) {
		data.addCreator(l.getObject());
	    } else if ("http://purl.org/dc/elements/1.1/publisher".equals(l
		    .getPredicate())) {
		data.addPublisher(l.getObject());
	    } else if ("http://purl.org/lobid/lv#urn".equals(l.getPredicate())) {
		data.addIdentifier(l.getObject(), "hbz:urn");
	    } else if ("http://purl.org/lobid/lv#hbzID"
		    .equals(l.getPredicate())) {
		data.addIdentifier(l.getObject(), "hbz:alephId");
	    } else if ("http://purl.org/dc/elements/1.1/subjectr".equals(l
		    .getPredicate())) {
		data.addSubject(l.getObject());
	    }
	}
	data.addIdentifier(node.getAggregationUri(), "hbz:edowebId");
	data.addType(node.getContentType());
	return data;
    }
}
