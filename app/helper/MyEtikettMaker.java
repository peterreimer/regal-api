package helper;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.hbz.lobid.helper.Etikett;
import de.hbz.lobid.helper.EtikettMaker;
import de.hbz.lobid.helper.EtikettMakerInterface;

public class MyEtikettMaker implements EtikettMakerInterface {

    EtikettMaker maker;

    public MyEtikettMaker(InputStream inputStream) {
	maker = new EtikettMaker(inputStream);
    }

    @Override
    public Map<String, Object> getContext() {
	return getAnnotatedContext();
    }

    @Override
    public Etikett getEtikett(String uri) {
	if(uri==null){
	    throw new RuntimeException("Do not pass null!");
	}
	Etikett result = null;
	try {
	    result = maker.getEtikett(uri);
	} catch (RuntimeException e) {
	    play.Logger.debug("No label defined for "+uri);
	}
	if (result == null) {
	    result = new Etikett(uri);
	    result.setName(getJsonName(result));
	}
	if (result.getLabel() == null || result.getLabel().isEmpty()) {
	    result.setLabel(result.getUri());
	}
	return result;
    }

    @Override
    public Etikett getEtikettByName(String name) {
	return maker.getEtikettByName(name);
    }

    /**
     * @param predicate
     * @return The short name of the predicate uses String.split on first index
     *         of '#' or last index of '/'
     */
    public String getJsonName(Etikett e) {
	String result = null;
	String uri = e.getUri();
	
	if (e.getName() != null) {
	    result = e.getName();
	}
	if (result == null || result.isEmpty()) {
	    String prefix = "";
	    if (uri.startsWith("http://purl.org/dc/elements"))
		prefix = "dc:";
	    if (uri.contains("#"))
		return prefix + uri.split("#")[1];
	    else if (uri.startsWith("http")) {
		int i = uri.lastIndexOf("/");
		return prefix + uri.substring(i + 1);
	    }
	    result = prefix + uri;
	}
	return result;
    }
    
    private Map<String, Object> getAnnotatedContext() {
        Map<String, Object> pmap;
        Map<String, Object> cmap = new HashMap<String, Object>();
        for (Etikett l : getValues()) {
            if ("class".equals(l.getReferenceType()) || l.getReferenceType() == null || l.getName() == null)
                continue;
            pmap = new HashMap<String, Object>();
            pmap.put("@id", l.getUri());
            addFieldToMap(pmap,"label",l.getLabel());
            addFieldToMap(pmap,"icon",l.getIcon());
            addFieldToMap(pmap,"weight",l.weight);
            addFieldToMap(pmap,"comment",l.comment);
            if (!"String".equals(l.getReferenceType())) {
                addFieldToMap(pmap,"@type",l.getReferenceType());
            }
            addFieldToMap(pmap,"@container",l.container);
            cmap.put(l.getName(), pmap);
        }
        Map<String, Object> contextObject = new HashMap<String, Object>();
        addAliases(cmap);
        contextObject.put("@context", cmap);
        return contextObject;
    }
    private void addFieldToMap(Map<String,Object> map,String key, String value){
	 if ( value != null && !value.isEmpty()) {
	 map.put(key, value);
	 }
    }
    private void addAliases(Map<String, Object> cmap) {
            cmap.put("id", "@id");
            cmap.put("type", "@type");
    }

    @Override
    public Collection<Etikett> getValues() {
	return maker.getValues();
    }

    @Override
    public boolean supportsLabelsForValues() {
	return true;
    }
    
}
