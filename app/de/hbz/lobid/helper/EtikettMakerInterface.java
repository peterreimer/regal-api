/*
 * Copyright 2017 hbz NRW (http://www.hbz-nrw.de/)
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
package de.hbz.lobid.helper;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * @author Jan Schnasse
 *
 */
public interface EtikettMakerInterface {

	/**
	 * @return a map with a json-ld context
	 */
	Map<String, Object> getContext();

	/**
	 * @param uri the uri
	 * @return an etikett object contains uri, icon, label, jsonname,
	 *         referenceType
	 */
	Etikett getEtikett(String uri);

	/**
	 * @param name the json name
	 * @return an etikett object contains uri, icon, label, jsonname,
	 *         referenceType
	 */
	Etikett getEtikettByName(String name);

	/**
	 * @return a list of all Etiketts
	 */
	Collection<Etikett> getValues();

	/**
	 * @return true if the implementor provides etiketts for all kind of values
	 *         (uris on object position). false if the implementor provides
	 *         etiketts for fields (uris in predicate position) only.
	 */
	boolean supportsLabelsForValues();

	/**
	 * @return the idAlias is used to substitute all occurrences of "\@id"
	 */
	public String getIdAlias();

	/**
	 * @return the typeAlias is used to substitute all occurrences of "\@type"
	 */
	public String getTypeAlias();

	/**
	 * @return returns a json key that is used to store label info
	 */
	public String getLabelKey();

	/**
	 * 
	 * @param labelIn stream with array of Etikett objects
	 */
	void updateLabels(InputStream labelIn);

}