package helper;

import java.io.InputStream;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebgatherUtilsTest {
	@Test
	public void testUrlOld() {
		try (InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("url-succeding-tests.json")) {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode testdata = mapper.readValue(in, JsonNode.class).at("/tests");
			for (JsonNode test : testdata) {
				String url = test.at("/in").asText();
				String expected = test.at("/out").asText();
				String encodedUrl = "ERROR";

				try {
					encodedUrl = encodeUrlOld(url);
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
				if (!expected.equals(encodedUrl)) {
					System.out.println("In:\t" + url);
					System.out.println("Expect:\t" + expected);
					System.out.println("Actual:\t" + encodedUrl);
					System.out.println("");
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String encodeUrlOld(String url) throws URISyntaxException {
		return WebgatherUtils.validateURL(url, true);
	}

	@Test
	public void testUrlNew() {
		try (InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("url-succeding-tests.json")) {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode testdata = mapper.readValue(in, JsonNode.class).at("/tests");
			for (JsonNode test : testdata) {
				String url = test.at("/in").asText();
				String expected = test.at("/out").asText();
				String encodedUrl = encodeUrlNew(url);

				org.junit.Assert.assertTrue(expected.equals(encodedUrl));
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String encodeUrlNew(String url) throws URISyntaxException {
		try {
			URL u = new URL(url);
			URI uri =
					new URI(u.getProtocol(), u.getUserInfo(), IDN.toASCII(u.getHost()),
							u.getPort(), u.getPath(), u.getQuery(), u.getRef());
			String correctEncodedURL = uri.toASCIIString();
			return correctEncodedURL;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
