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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.DublinCoreData;
import models.Globals;
import models.Node;

/**
 * @author Jan Schnasse
 *
 */
public class WglMapper {

	OaiDcMapper dcMapper;

	public WglMapper(Node node) {
		dcMapper = new OaiDcMapper(node);
	}

	public DublinCoreData getData() {
		DublinCoreData data = dcMapper.getData();
		data.setWglContributor(getWglContributor(dcMapper.getNode()));
		return data;
	}

	private List<String> getWglContributor(Node node) {
		List<String> result = new ArrayList<>();
		JsonNode n = new ObjectMapper().valueToTree(node.getLd());
		JsonNode collectionOneArray = n.at("/collectionOne");
		for (JsonNode item : collectionOneArray) {
			String id = item.at("/@id").asText("no Value found");
			result.add(Globals.wglContributor.acronymOf(id));
		}
		if (result.isEmpty()) {
			JsonNode institutionArray = n.at("/institution");
			for (JsonNode item : institutionArray) {
				String id = item.at("/@id").asText("no Value found");
				result.add(Globals.wglContributor.acronymOf(id));
			}
		}
		return result;
	}

}
