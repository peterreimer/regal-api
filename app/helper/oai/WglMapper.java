package helper.oai;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.DublinCoreData;
import models.Globals;
import models.Node;

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

		JsonNode institutionArray = n.at("/institution");
		for (JsonNode item : institutionArray) {
			String id = item.at("/@id").asText("no Value found");
			result.add(Globals.wglContributor.acronymOf(id));
		}

		return result;
	}

}
