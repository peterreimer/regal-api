/**
 * 
 */
package models;

import java.util.Hashtable;

/**
 * @author aquast
 *
 *         Class for representing COAR Values as constants
 */
public abstract class CoarModel {

	public static final String OPEN_ACCESS = new String("open access");
	public static final String OPEN_ACCESS_URI =
			new String("http://purl.org/coar/access_right/c_abf2");
	public static final String EMBARGOED_ACCESS = new String("embargoed access");
	public static final String EMBARGOED_ACCESS_URI =
			new String("http://purl.org/coar/access_right/c_f1cf");
	public static final String RESTRICTED_ACCESS =
			new String("restricted access");
	public static final String RESTRICTED_ACCESS_URI =
			new String("http://purl.org/coar/access_right/c_16ec");
	public static final String METADATA_ONLY_ACCESS =
			new String("metadata only access");
	public static final String METADATA_ONLY_ACCESS_URI =
			new String("http://purl.org/coar/access_right/c_14cb");

	// https://openaire-guidelines-for-literature-repository-managers.readthedocs.io/en/v4.0.0/field_publicationtype.html
	public static final String JOURNAL_ARTICLE = new String("journal article");
	public static final String JOURNAL_ARTICLE_URI =
			new String("http://purl.org/coar/resource_type/c_6501");
	public static final String BOOK_PART = new String("book part");
	public static final String BOOK_PART_URI =
			new String("http://purl.org/coar/resource_type/c_3248");
	public static final String CONFERENCE_OBJECT =
			new String("conference object");
	public static final String CONFERENCE_OBJECT_URI =
			new String("http://purl.org/coar/resource_type/c_c94f");

	// resourceTypeGeneral
	public static final String LITERATURE = new String("literature");
	public static final String DATASET = new String("dataset");
	public static final String SOFTWARE = new String("software");
	public static final String OTHER_RESEARCH_PRODUCT =
			new String("other research product");

	public static Hashtable<String, String> elementContent = new Hashtable<>();
	public static Hashtable<String, String> uriAttributeContent =
			new Hashtable<>();
	public static Hashtable<String, String> resourceTypeGeneralAttribute =
			new Hashtable<>();

	private static void setElementHashtable() {
		elementContent.put("public", CoarModel.OPEN_ACCESS);
		elementContent.put("embargoed", CoarModel.EMBARGOED_ACCESS);
		elementContent.put("restricted", CoarModel.RESTRICTED_ACCESS);
		elementContent.put("metadata", CoarModel.METADATA_ONLY_ACCESS);
		elementContent.put("article", CoarModel.JOURNAL_ARTICLE);
		elementContent.put("Buchkapitel", CoarModel.BOOK_PART);
		elementContent.put("Kongressbeitrag", CoarModel.CONFERENCE_OBJECT);
	}

	private static void setUriHashtable() {
		uriAttributeContent.put("public", CoarModel.OPEN_ACCESS_URI);
		uriAttributeContent.put("embargoed", CoarModel.EMBARGOED_ACCESS_URI);
		uriAttributeContent.put("restricted", CoarModel.RESTRICTED_ACCESS_URI);
		uriAttributeContent.put("metadata", CoarModel.METADATA_ONLY_ACCESS_URI);
		uriAttributeContent.put("article", CoarModel.JOURNAL_ARTICLE_URI);
		uriAttributeContent.put("Buchkapitel", CoarModel.BOOK_PART_URI);
		uriAttributeContent.put("Kongressbeitrag", CoarModel.CONFERENCE_OBJECT_URI);
	}

	private static void setResourceHashtable() {
		resourceTypeGeneralAttribute.put("article", CoarModel.LITERATURE);
		resourceTypeGeneralAttribute.put("Buchkapitel", CoarModel.LITERATURE);
		resourceTypeGeneralAttribute.put("Kongressbeitrag", CoarModel.LITERATURE);

	}

	/**
	 * mapping for lobid to coar
	 * 
	 * @param value
	 * @return the element value according coar schema
	 */
	public static String getElementValue(String value) {
		setElementHashtable();
		return elementContent.get(value);
	}

	/**
	 * mapping for lobid to coar
	 * 
	 * @param value
	 * @return the uri attribute value according coar schema
	 */
	public static String getUriAttributeValue(String value) {
		setUriHashtable();
		return uriAttributeContent.get(value);
	}

	/**
	 * mapping for lobid to coar
	 * 
	 * @param value
	 * @return the resourceTypeGeneral attribute value according coar schema
	 */
	public static String getResourceTypeGeneralAttribute(String value) {
		setResourceHashtable();
		return resourceTypeGeneralAttribute.get(value);
	}

}
