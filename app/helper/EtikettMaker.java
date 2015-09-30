package helper;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Play;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Jan Schnasse
 *
 */
public class EtikettMaker {
    /**
     * A map with URIs as key and labels,icons, shortnames as values
     */
    Map<String, Map<String, Object>> pMap = new HashMap<String, Map<String, Object>>();

    /**
     * A map with Shortnames as key and labels,icons, uris as values
     */
    Map<String, Map<String, Object>> nMap = new HashMap<String, Map<String, Object>>();

    /**
     * The context will be loaded on startup. You can reload the context with
     * POST /utils/reloadContext
     * 
     */
    Map<String, Object> context = new HashMap<String, Object>();

    /**
     * The labels will be loaded on startup. You can reload the context with
     * POST /utils/reloadLabels
     * 
     */
    List<Map<String, Object>> labels = new ArrayList<Map<String, Object>>();

    /**
     * The profile provides a json context an labels
     */
    public EtikettMaker() {
	initContext();
	initMaps();
    }

    private void initContext() {
	context = createContext("context.json");
    }

    private void initMaps() {
	try {
	    labels = createLabels("labels.json");

	    for (Map<String, Object> etikett : labels) {
		pMap.put((String) etikett.get("uri"), etikett);
		nMap.put((String) etikett.get("name"), etikett);
	    }
	} catch (Exception e) {
	    play.Logger.debug("", e);
	}

    }

    private List<Map<String, Object>> createLabels(String fileName) {
	List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
	result = loadUrl(fileName, List.class);
	if (result == null) {
	    play.Logger.info("...not succeed! Load from local resource: "
		    + fileName);
	    result = loadFile(fileName, List.class);
	} else {
	    play.Logger.info("...succeeded!");
	}
	if (result == null)
	    play.Logger.info("...not succeeded!");
	return result;
    }

    /**
     * @return a Map representing additional information about the shortnames
     *         used in getLd
     */
    Map<String, Object> createContext(String fileName) {
	Map<String, Object> result = new HashMap<String, Object>();
	result = loadUrl(fileName, Map.class);
	if (result == null) {
	    play.Logger.info("...not succeeded! Load from local resource: "
		    + fileName);
	    result = loadFile(fileName, Map.class);
	} else {
	    play.Logger.info("...succeed!");
	}
	if (result == null)
	    play.Logger.info("...not succeeded!");
	return result;
    }

    @SuppressWarnings("unchecked")
    private <T> T loadFile(String fileName, Class<T> type) {
	try (InputStream in = Play.application().resourceAsStream(
		"public/" + fileName)) {
	    return new ObjectMapper().readValue(in, type);
	} catch (Exception e) {
	    throw new RuntimeException("Error during initialization!", e);
	}
    }

    @SuppressWarnings("unchecked")
    private <T> T loadUrl(String fileName, Class<T> type) {
	try {
	    String url = Play.application().configuration()
		    .getString("regal-api.etikettUrl")
		    + fileName;
	    play.Logger.info("Load context from " + url);
	    return new ObjectMapper().readValue(new URL(url), type);
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * @param predicate
     * @return The short name of the predicate uses String.split on first index
     *         of '#' or last index of '/'
     */
    public String getJsonName(String predicate) {
	String result = null;
	Map<String, Object> e = pMap.get(predicate);
	if (e != null) {
	    result = (String) e.get("name");
	}
	if (result == null || result.isEmpty()) {
	    String prefix = "";
	    if (predicate.startsWith("http://purl.org/dc/elements"))
		prefix = "dc:";
	    if (predicate.contains("#"))
		return prefix + predicate.split("#")[1];
	    else if (predicate.startsWith("http")) {
		int i = predicate.lastIndexOf("/");
		return prefix + predicate.substring(i + 1);
	    }
	    result = prefix + predicate;
	}
	return result;
    }

    /**
     * @param predicate
     * @return a label for the predicate
     */
    public String getLabel(String predicate) {
	try {
	    String label = (String) pMap.get(predicate).get("label");
	    return label == null ? predicate : label;
	} catch (Exception e) {
	    return predicate;
	}
    }

    /**
     * @param predicate
     * @return a label for the predicate
     */
    public String getIcon(String predicate) {
	try {
	    String icon = (String) pMap.get(predicate).get("icon");
	    return icon == null ? predicate : icon;
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * @param jsonName
     * @return the corresponding uri to the passed jsonName
     */
    public String getUriFromJsonName(String jsonName) {
	try {
	    String uri = (String) nMap.get(jsonName).get("uri");
	    return uri == null ? jsonName : uri;
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * @return a map with a json-ld context
     */
    public Map<String, Object> getContext() {
	return context;
    }
}
