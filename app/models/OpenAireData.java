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
package models;

import java.io.StringWriter;
import java.util.Hashtable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.core.util.JsonUtil;

/**
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 * @author Andres Quast, quast@hbz-nrw.de
 * 
 */

public class OpenAireData implements java.io.Serializable {

	private Hashtable<String, String> element = new Hashtable<>();

	/**
	 * @param element
	 */
	public void setElement(Hashtable<String, String> element) {
		this.element = element;
	}

	/**
	 * @param element
	 */
	public void addElement(String key, String value) {
		this.element.put(key, value);
	}

	/**
	 * @param key
	 * @return
	 */
	public String getElementValue(String key) {
		return this.element.get(key);
	}

	@Override
	public String toString() {
		ObjectMapper mapper = JsonUtil.mapper();
		StringWriter w = new StringWriter();
		try {
			mapper.writeValue(w, this);
		} catch (Exception e) {
			return super.toString();
		}
		return w.toString();
	}

}
