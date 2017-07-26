package views;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.core.util.JsonUtil;

import actions.Read;
import helper.MyEtikettMaker;
import models.Gatherconf;
import models.Globals;
import models.Node;

public class Helper {

	public static List<String> getOrderedListOfKeysFromContext(
			Map<String, Object> context) {
		return (List<String>) context.entrySet().stream()
				.sorted(new Comparator<Map.Entry<String, Object>>() {
					public int compare(Map.Entry<String, Object> o1,
							Map.Entry<String, Object> o2) {

						String s1 =
								(String) ((Map<String, Object>) o1.getValue()).get("weight");
						if (s1 == null)
							s1 = "99999";
						String s2 =
								(String) ((Map<String, Object>) o2.getValue()).get("weight");
						if (s2 == null)
							s2 = "99999";
						int i1 = Integer.parseInt(s1);
						int i2 = Integer.parseInt(s2);
						return i1 - i2;
					}
				}).map(e -> e.getKey()).collect(Collectors.toList());
	}

	public static String getDataLink(Map<String, Object> hit) {
		Object parts = hit.get("hasPart");
		if (parts instanceof List) {
			Object part = ((List) parts).get(0);
			if (part instanceof Map) {
				String id = (String) ((Map<String, Object>) part).get("@id");
				return ("/resource/" + id + "/data");
			}
		}
		String id = (String) hit.get("@id");
		return "/resource/" + id + "/data";
	}

	public static String getTitle(Map<String, Object> hit) {
		Object t = hit.get("title");
		if (t instanceof List) {
			Object l = ((List) t).get(0);
			if (l instanceof String) {
				return l + "";
			}

		}
		if (t instanceof Set) {
			return ((Set) t).iterator().next() + "";
		}

		return t + "";
	}

	public static String getSeries(Set<Map<String, Object>> hits) {
		return getBibliographicParent(hits, "series");
	}

	public static String getMultiVolumeWork(Set<Map<String, Object>> hits) {
		return getBibliographicParent(hits, "multiVolumeWork");
	}

	public static String getBibliographicParent(Set<Map<String, Object>> hits,
			String rel) {
		try {
			StringBuffer result = new StringBuffer();
			for (Map<String, Object> hit : hits) {
				// result.append("" + hit);
				String numbering = (String) hit.get("numbering");
				Map<String, Object> series =
						((Set<Map<String, Object>>) hit.get(rel)).iterator().next();
				String label = (String) series.get("prefLabel");
				String id = (String) series.get("@id");
				id = id.trim();
				String prefix = models.Globals.rechercheUrlPrefix.substring(0,
						models.Globals.rechercheUrlPrefix.length() - 1);
				String internLink = prefix + URLEncoder
						.encode("\"" + id + models.Globals.rechercheUrlSuffix, "utf-8");
				result.append(String.format(
						"<a title=\"Ähnliche Objekte suchen\" href=\"%s\"> %s</a>",
						internLink, label));
				result.append(String.format(
						"<span class=\"separator\">|</span><a href=\"%s\"><span class=\"glyphicon glyphicon-link\"></span></a>, Band %s",
						id, numbering));
			}
			return result.toString();
		} catch (Exception e) {
			play.Logger.warn("", e);
			return "Can't process data";
		}
	}

	public static String getWaybackLink(String pid) {
		try {
			play.Logger.debug("Get Waybacklinkg for " + pid);
			String waybackLink = "";
			Node node = new Read().readNode(pid);
			String confstring = node.getConf();
			if (confstring == null)

				return "../" + pid;

			ObjectMapper mapper = JsonUtil.mapper();
			Gatherconf conf = mapper.readValue(confstring, Gatherconf.class);
			if (conf.getOpenWaybackLink() == null
					|| conf.getOpenWaybackLink().isEmpty()) {
				String owDatestamp =
						new SimpleDateFormat("yyyyMMdd").format(conf.getStartDate());
				conf.setOpenWaybackLink(Globals.heritrix.openwaybackLink + owDatestamp
						+ "/" + conf.getUrl());

			}
			play.Logger.debug(waybackLink);
			waybackLink = conf.getOpenWaybackLink();
			return waybackLink != null ? waybackLink : "../" + pid;
		} catch (Exception e) {
			play.Logger.error("", e);
			return "../" + pid;

		}
	}

	public static List<Map<String, Object>> listContributions(
			Map<String, Object> h) {
		List<Map<String, Object>> result = new ArrayList<>();
		JsonNode hit = new ObjectMapper().valueToTree(h);
		for (JsonNode c : hit.at("/contribution")) {
			String name = c.at("/agent/0/label").asText();
			String role = c.at("/role/0/label").asText();
			String roleUri = c.at("/role/0/@id").asText();
			String uri = c.at("/agent/0/@id").asText();
			if (!"http://id.loc.gov/vocabulary/relators/ctb".equals(roleUri)
					&& !"http://id.loc.gov/vocabulary/relators/cre".equals(roleUri)) {
				Map<String, Object> contribution = new HashMap<>();
				contribution.put("id", uri);
				contribution.put("label", name);
				contribution.put("roleName", role);
				contribution.put("roleId", roleUri);
				result.add(contribution);
			}
		}
		return result;
	}

	public static List<Object> listSubjects(Map<String, Object> h) {
		List<Object> result = new ArrayList<>();
		JsonNode hit = new ObjectMapper().valueToTree(h);
		for (JsonNode c : hit.at("/subject")) {
			if (c.has("componentList")) {
				result.add(getComponentList(c));
			} else {
				String name = c.at("/label").asText();
				String uri = c.at("/@id").asText();
				if (uri.contains("rpb#nr"))
					continue;
				String sourceId = c.at("/source/0/@id").asText();
				String source = c.at("/source/0/label").asText();

				Map<String, Object> subject = new HashMap<>();
				subject.put("id", uri);
				subject.put("label", name);
				subject.put("source", source);
				subject.put("sourceId", sourceId);
				result.add(subject);
			}
		}
		return result;

	}

	public static String getSubjectSource(String sourceId, String uri) {
		String source = "";
		if ("https://w3id.org/lobid/rpb2".equals(sourceId)) {
			source = "lbz " + getLbzId(uri);
		} else if ("https://w3id.org/lobid/rpb".equals(sourceId)) {
			source = "rpb " + getRPbId(uri);
		} else if ("http://d-nb.info/gnd/4149423-4".equals(sourceId)) {
			source = "ddc " + getDdcId(uri);
		} else if ("http://d-nb.info/gnd/7749153-1".equals(sourceId)) {
			source = "gnd " + getGndId(uri);
		} else if ("http://purl.org/lobid/nwbib".equals(sourceId)) {
			source = "nwbib " + getNwbibId(uri);
		}
		return source;
	}

	private static String getNwbibId(String uri) {
		try {
			return uri.split("#")[1].substring(1);
		} catch (Exception e) {

		}
		return "";
	}

	private static String getGndId(String uri) {
		try {
			String[] parts = uri.split("/");
			return parts[parts.length - 1];
		} catch (Exception e) {

		}
		return "";
	}

	private static String getDdcId(String uri) {
		try {
			String[] parts = uri.split("/");
			return parts[parts.length - 1];
		} catch (Exception e) {

		}
		return "";
	}

	private static String getRPbId(String uri) {
		try {
			return uri.split("#")[1].substring(1);
		} catch (Exception e) {

		}
		return "";
	}

	private static String getLbzId(String uri) {
		try {
			return uri.split("#")[1].substring(1);
		} catch (Exception e) {

		}
		return "";
	}

	private static List<Map<String, Object>> getComponentList(
			JsonNode componentList) {
		List<Map<String, Object>> result = new ArrayList<>();
		for (JsonNode c : componentList.at("/componentList")) {
			String name = c.at("/label").asText();
			String uri = c.at("/@id").asText();
			String source = c.at("/source/0/label").asText();
			String sourceId = c.at("/source/0/@id").asText();

			Map<String, Object> subject = new HashMap<>();
			subject.put("id", uri);
			subject.put("label", name);
			subject.put("source", source);
			subject.put("sourceId", sourceId);

			result.add(subject);
		}
		return result;
	}

	public static String getRechercheUrl(String uri) {
		try {
			return "" + Globals.rechercheUrlPrefix + ""
					+ URLEncoder.encode(uri, "utf-8") + "" + Globals.rechercheUrlSuffix;
		} catch (Exception e) {
			play.Logger.warn("", e);
		}
		return "" + Globals.rechercheUrlPrefix + "" + uri + ""
				+ Globals.rechercheUrlSuffix;
	}

	public static List<Map<String, Object>> listAuthors(Map<String, Object> h) {
		List<Map<String, Object>> result = new ArrayList<>();
		JsonNode hit = new ObjectMapper().valueToTree(h);
		for (JsonNode c : hit.at("/contribution")) {
			String name = c.at("/agent/0/label").asText();
			String role = c.at("/role/0/label").asText();
			String roleUri = c.at("/role/0/@id").asText();
			String uri = c.at("/agent/0/@id").asText();

			if ("http://id.loc.gov/vocabulary/relators/ctb".equals(roleUri)
					|| "http://id.loc.gov/vocabulary/relators/cre".equals(roleUri)) {
				Map<String, Object> contribution = new HashMap<>();
				contribution.put("id", uri);
				contribution.put("label", name);
				contribution.put("roleName", role);
				contribution.put("roleId", roleUri);
				result.add(contribution);
			}
		}
		return result;
	}

	public static boolean contributionContainsAdditionalFields(
			Map<String, Object> h) {
		JsonNode hit = new ObjectMapper().valueToTree(h);
		for (JsonNode c : hit.at("/contribution")) {
			String roleUri = c.at("/role/0/@id").asText();
			if (!"http://id.loc.gov/vocabulary/relators/ctb".equals(roleUri)
					&& !"http://id.loc.gov/vocabulary/relators/cre".equals(roleUri)) {
				return true;
			}
		}
		return false;
	}

	public static String getContainedIn(Set<Map<String, Object>> containedIn) {
		JsonNode hit = new ObjectMapper().valueToTree(containedIn);
		String uri = hit.at("/0/@id").asText();
		String label = hit.at("/0/label").asText();
		if (uri != null && !uri.isEmpty()) {
			label = MyEtikettMaker.getLabelFromEtikettWs(uri);
		}
		return "<a href=\"" + uri + "\">" + label + "</a>";
	}

	public static String getLobidIsPartOf(Set<Map<String, Object>> isPartOf) {

		JsonNode hit = new ObjectMapper().valueToTree(isPartOf);
		String uri = hit.at("/0/hasSuperordinate/0/@id").asText();
		String label = hit.at("/0/hasSuperordinate/0/label").asText();
		if (uri != null && !uri.isEmpty()) {
			label = MyEtikettMaker.getLabelFromEtikettWs(uri);
		}
		String numbering = hit.at("/0/numbering").asText();
		if (numbering != null && !numbering.isEmpty()) {
			return "<a href=\"" + uri + "\">" + label + "</a>, " + numbering;
		}
		return label;
	}

	public static String getPublication(Set<Map<String, Object>> publ) {
		for (Map<String, Object> p : publ) {
			JsonNode hit = new ObjectMapper().valueToTree(p);
			String location = hit.at("/location").asText();
			String publishedBy = hit.at("/publishedBy").asText();
			String startDate = hit.at("/startDate").asText();
			return startDate + "<br/>" + location + "<br/>" + publishedBy;
		}
		return "Can't process data!";
	}
}
