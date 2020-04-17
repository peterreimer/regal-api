/**
 * 
 */
package models;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * @author aquast A Class representing an json-DOM within different Types of
 *         ArrayList Model aims to unify methods to access the json-DOM Elements
 *         All object elements will be broken down into key:value pairs where
 *         key is the canonical Path of each object-element. literal elements
 *         will be treated similar Both kind of key value pairs will be stored
 *         in an ArrayList of Hashtable<String, String>
 * 
 *         In contrast Arrays will be stored in an ArrayList of Values
 * 
 */
public class JsonElementModel {

	private String path = null;
	private Hashtable<String, String> elementList = null;
	private ArrayList<String> valueList = null;

	/**
	 * @param path
	 */
	public JsonElementModel(String path) {
		this.path = path;
	}

	/**
	 * @return
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * @param elementList
	 */
	public void setComplexElement(Hashtable<String, String> elementList) {
		this.elementList = new Hashtable<>();
		this.elementList = elementList;
	}

	/**
	 * @return
	 */
	public Hashtable<String, String> getComplexElementList() {
		return elementList;
	}

	/**
	 * @param valueList
	 */
	public void setArrayElement(ArrayList<String> valueList) {
		this.valueList = new ArrayList<>();
		this.valueList = valueList;
	}

	/**
	 * @param value
	 */
	public void addArrayElement(String value) {
		if (this.valueList == null) {
			this.valueList = new ArrayList<>();
		}
		this.valueList.add(value);
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
	public boolean isArray() {
		if (this.valueList != null) {
			return true;
		}
		return false;
	}

	/**
	 * @param jEM
	 * @return
	 */
	public boolean isEmpty() {
		if (elementList == null) {
			return true;
		}
		return false;
	}
}
