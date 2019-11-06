/**
 * 
 */
package helper.oai;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;

import models.JsonElementModel;
import models.Node;

/**
 * @author aquast
 *
 */
public class JsonLDMapper {

	Hashtable<String, String> simpleElements = new Hashtable<>();
	ArrayList<Hashtable<String, String>> complexElement = new ArrayList<>();
	ArrayList<Hashtable<String, String>> simpleElement = new ArrayList<>();
	ArrayList<Hashtable<String, ArrayList<String>>> arrayElement =
			new ArrayList<>();
	ArrayList<JsonElementModel> jemElement = new ArrayList<>();
	JsonElementModel jEM = null;
	Hashtable<String, ArrayList<Integer>> index = new Hashtable<>();

	StringBuffer pathBuffer = new StringBuffer("");

	/**
	 * @param node
	 * @return
	 */
	public ArrayList<JsonElementModel> mapToJsonElementModel(JsonNode node) {
		ArrayList<JsonElementModel> jList =
				mapToJsonElementModel(node, new StringBuffer("root"));
		createIndex();
		return jList;
	}

	/**
	 * @param node
	 * @param pBuffer
	 * @return
	 */
	public ArrayList<JsonElementModel> mapToJsonElementModel(JsonNode node,
			StringBuffer pBuffer) {

		Iterator<String> it = node.fieldNames();
		while (it.hasNext()) {
			int l = pBuffer.length();

			String key = it.next();
			if (node.get(key).isValueNode()) {
				Hashtable<String, String> iE = new Hashtable<>();
				complexElement.add(iE);

				if (jEM != null && jEM.isObject()) {
					Hashtable<String, String> ha = jEM.getComplexElementList();
					ha.put(key, node.get(key).asText());
					jEM.setComplexElement(ha);
				} else {
					iE.put(pBuffer + "." + key, node.get(key).asText());
					iE.put(key, node.get(key).asText());
					jEM = new JsonElementModel(pBuffer.toString());
					jEM.setComplexElement(iE);
					jemElement.add(jEM);
				}
			}

			if (node.get(key).isObject()) {
				pBuffer.append("." + key);
				JsonNode complexNode = node.get(key);

				jEM = new JsonElementModel(pBuffer.toString());
				jEM.setComplexElement(new Hashtable<String, String>());
				Hashtable<String, String> ha = jEM.getComplexElementList();
				mapToJsonElementModel(complexNode, pBuffer);
				jemElement.add(jEM);
			}

			if (node.get(key).isArray()) {
				pBuffer.append("." + key);

				JsonNode complexNode = node.get(key);
				jEM = new JsonElementModel(pBuffer.toString());

				Iterator<JsonNode> nIt = complexNode.elements();
				while (nIt.hasNext()) {
					JsonNode arrayNode = nIt.next();
					if (arrayNode.isObject()) {
						jEM = new JsonElementModel(pBuffer.toString());
						jEM.setComplexElement(new Hashtable<String, String>());
						mapToJsonElementModel(arrayNode, pBuffer);
						jemElement.add(jEM);

					} else {
						Hashtable<String, String> iE = new Hashtable<>();
						iE.put(pBuffer.toString(), arrayNode.asText());
						complexElement.add(iE);
						jEM.addArrayElement(arrayNode.asText());
					}
				}
				if (jEM.isArray()) {
					jemElement.add(jEM);
				}
			}
			pBuffer.setLength(l);

		}
		return jemElement;
	}

	/**
	 * method for examine the classes result
	 * 
	 */
	// TODO: move into separate testing Class
	public void printElements() {
		Iterator<Hashtable<String, String>> it = complexElement.iterator();
		for (int i = 0; i < jemElement.size(); i++) {
			if (jemElement.get(i).isArray()) {
				System.out.print(jemElement.get(i).getPath() + ":\t");
				Iterator jit = jemElement.get(i).getArrayList().iterator();
				while (jit.hasNext()) {
					System.out.print(jit.next().toString() + "\t");
				}
			} else if (jemElement.get(i).isObject()) {
				System.out.println(jemElement.get(i).getPath());
				Enumeration jEnum = jemElement.get(i).getComplexElementList().keys();
				while (jEnum.hasMoreElements()) {
					String key = jEnum.nextElement().toString();
					System.out.print("\t" + "\t" + key + "\t");
					System.out
							.print(jemElement.get(i).getComplexElementList().get(key) + "\n");
				}

			}
			System.out.println("" + jemElement.size());
		}
	}

	public void createIndex() {

		Iterator<JsonElementModel> jemIt = jemElement.iterator();
		ArrayList<Integer> position = new ArrayList<>();
		int i = 0;
		while (jemIt.hasNext()) {
			JsonElementModel jEM = jemIt.next();
			if (index.containsKey(jEM.getPath())) {
				position = index.get(jEM.getPath());
				// System.out.println(jEM.getPath());
			} else {
				position = new ArrayList<>();
				System.out.println("da");
			}

			int pos = i;
			position.add(Integer.valueOf(pos));
			index.put(jEM.getPath(), position);
			System.out.println(position.size());
			System.out.println(index.get(jEM.getPath()));
			i++;
		}
	}

	public ArrayList<JsonElementModel> getElement(String path) {

		ArrayList<JsonElementModel> result = new ArrayList<JsonElementModel>();
		ArrayList<Integer> fieldIndex = index.get(path);
		System.out.println(fieldIndex.size());
		for (int i = 0; i < fieldIndex.size(); i++) {
			JsonElementModel sJem = jemElement.get(fieldIndex.get(i));
			result.add(sJem);
		}
		return result;
	}

}
