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
package helper.oai;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import helper.HttpArchiveException;
import play.Play;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 * @author Jan Schnasse
 *
 */
public class WglContributor {
	private static final CsvPreference DELIMITER =
			new CsvPreference.Builder('"', ';', "\n").build();

	Map<String, String> gndIdToAcronym;

	Map<String, String> gndIdToLabel;

	public WglContributor() {
		initMaps();
	}

	public String acronymOf(String id) {
		return gndIdToAcronym.get(id);
	}

	public String labelOf(String id) {
		return gndIdToLabel.get(id);
	}

	private void initMaps() {
		gndIdToAcronym = new HashMap<>();

		gndIdToLabel = new HashMap<>();
		try (ICsvMapReader mapReader = new CsvMapReader(
				new InputStreamReader(
						Play.application().resourceAsStream("wglcontributor.csv")),
				DELIMITER)) {
			final String[] header = mapReader.getHeader(true);
			final CellProcessor[] processors = getProcessors();
			Map<String, Object> row;

			while ((row = mapReader.read(header, processors)) != null) {
				String label = (String) row.get("Label");
				String wglAcronym = (String) row.get("WGL-contributor");
				String gndId = (String) row.get("DNB-Link");
				gndIdToAcronym.put(gndId.trim(), wglAcronym.trim());
				gndIdToLabel.put(gndId.trim(), label.trim());
			}
		} catch (Exception e) {
			play.Logger.error("", e);
			throw new HttpArchiveException(500, e);
		}
	}

	/*
	 * Label;WGL-contributor;DNB-Link
	 */
	private static CellProcessor[] getProcessors() {
		final CellProcessor[] processors = new CellProcessor[] { new Optional(), // Label
				new Optional(), // Wgl Acronym
				new UniqueHashCode(), // GND URI
		}; // EMPTY
		return processors;
	}
}
