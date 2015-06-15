package models;

import java.util.ArrayList;
import java.util.List;

public class DataciteRecord {
    public String doi = null;
    public String identifier = null;
    public List<Pair<String, String>> creators = new ArrayList<Pair<String, String>>();
    public List<Pair<String, String>> titles = new ArrayList<Pair<String, String>>();
    public String publisher = null;
    public String publicationYear = null;
    public List<Pair<String, String>> subjects = new ArrayList<Pair<String, String>>();
    public List<Pair<String, String>> dates = new ArrayList<Pair<String, String>>();
    public String language = null;
    public List<Pair<String, String>> alternateIdentifiers = new ArrayList<Pair<String, String>>();
    public List<Pair<String, String>> sizes = new ArrayList<Pair<String, String>>();
    public List<Pair<String, String>> formats = new ArrayList<Pair<String, String>>();
    public String type = null;

    public DataciteRecord(String doi) {
	this.doi = doi;
    }

    public String toString() {
	StringBuilder xml = new StringBuilder();
	xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	xml.append("<resource xmlns=\"http://datacite.org/schema/kernel-2.2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://datacite.org/schema/kernel-2.2 http://schema.datacite.org/meta/kernel-2.2/metadata.xsd\">");
	xml.append("<identifier identifierType=\"DOI\">" + doi
		+ "</identifier>");
	if (creators != null && !creators.isEmpty()) {
	    xml.append("<creators>");
	    for (Pair<String, String> c : creators) {
		xml.append("<creator>");
		xml.append("<creatorName>");
		xml.append(c.getLeft());
		xml.append("</creatorName>");
		xml.append("</creator>");
	    }
	    xml.append("</creators>");
	}
	if (titles != null && !titles.isEmpty()) {
	    xml.append("<titles>");
	    for (Pair<String, String> c : titles) {
		xml.append("<title>");
		xml.append(c.getLeft());
		xml.append("</title>");
	    }
	    xml.append("</titles>");
	}
	if (publisher != null && !publisher.isEmpty()) {
	    xml.append("<publisher>");
	    xml.append(publisher);
	    xml.append("</publisher>");
	}
	if (publicationYear != null && !publicationYear.isEmpty()) {
	    xml.append("<publicationYear>");
	    xml.append(publicationYear);
	    xml.append("</publicationYear>");
	}
	if (subjects != null && !subjects.isEmpty()) {
	    xml.append("<subjects>");
	    for (Pair<String, String> c : subjects) {
		if (c.getRight().isEmpty()) {
		    xml.append("<subject>");
		} else {
		    xml.append("<subject subjectScheme=\"" + c.getRight()
			    + "\">");
		}

		xml.append(c.getLeft());
		xml.append("</subject>");
	    }
	    xml.append("</subjects>");
	}

	if (dates != null && !dates.isEmpty()) {
	    xml.append("<dates>");
	    for (Pair<String, String> c : dates) {
		if (c.getRight().isEmpty()) {
		    xml.append("<date>");
		} else {
		    xml.append("<date dateType=\"" + c.getRight() + "\">");
		}

		xml.append(c.getLeft());
		xml.append("</date>");
	    }
	    xml.append("</dates>");
	}
	if (language != null && !language.isEmpty()) {
	    xml.append("<language>");
	    xml.append(language);
	    xml.append("</language>");
	}
	if (alternateIdentifiers != null && !alternateIdentifiers.isEmpty()) {
	    xml.append("<alternateIdentifiers>");
	    for (Pair<String, String> c : alternateIdentifiers) {
		if (c.getRight().isEmpty()) {
		    xml.append("<alternateIdentifier>");
		} else {
		    xml.append("<alternateIdentifier alternateIdentifierType=\""
			    + c.getRight() + "\">");
		}

		xml.append(c.getLeft());
		xml.append("</alternateIdentifier>");
	    }
	    xml.append("</alternateIdentifiers>");
	}
	if (sizes != null && !sizes.isEmpty()) {
	    xml.append("<sizes>");
	    for (Pair<String, String> c : sizes) {
		xml.append("<size>");
		xml.append(c.getLeft());
		xml.append("</size>");
	    }
	    xml.append("</sizes>");
	}
	if (formats != null && !formats.isEmpty()) {
	    xml.append("<formats>");
	    for (Pair<String, String> c : formats) {
		xml.append("<format>");
		xml.append(c.getLeft());
		xml.append("</format>");
	    }
	    xml.append("</formats>");
	}
	xml.append("</resource>");
	return xml.toString();
    }
}
