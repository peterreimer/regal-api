package views;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
}
