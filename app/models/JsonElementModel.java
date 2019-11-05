/**
 * 
 */
package models;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * @author aquast
 *
 */
public class JsonElementModel {

	private String path = null;
	private ArrayList<Hashtable<String, String>> elementList = null;
	private ArrayList<String> valueList = new ArrayList<>();

	/**
	 * @param path
	 * @param elementList
	 */
	public void setComplexElement(String path,
			ArrayList<Hashtable<String, String>> elementList) {
		this.path = path;
		this.elementList = new ArrayList<>();
		this.elementList = elementList;
	}

	/**
	 * @return
	 */
	public ArrayList<Hashtable<String, String>> getComplexElementList() {
		return elementList;
	}

	/**
	 * @param path
	 * @param valueList
	 */
	public void setArrayElement(String path, ArrayList<String> valueList) {
		this.path = path;
		this.valueList = new ArrayList<>();
		this.valueList = valueList;
	}

	/**
	 * @return
	 */
	public ArrayList<String> getArrayList() {
		return valueList;
	}

	/**
	 * @param jEM
	 * @return
	 */
	public boolean isArray(JsonElementModel jEM) {
		if (valueList != null) {
			return true;
		}
		return false;
	}

	/**
	 * @param jEM
	 * @return
	 */
	public boolean isObject(JsonElementModel jEM) {
		if (elementList != null) {
			return true;
		}
		return false;
	}

}
