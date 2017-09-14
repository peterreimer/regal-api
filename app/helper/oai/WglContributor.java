/*Copyright (c) 2017 "hbz"

This file is part of lobid-rdf-to-json.

etikett is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
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
