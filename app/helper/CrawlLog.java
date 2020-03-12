/**
 * 
 */
package helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import play.Logger;

/**
 * Java-Klasse für ein Crawl-Log. Z.Zt. nur für wpull-Crawls benutzt
 * 
 * @author I. Kuss
 */
public class CrawlLog {

	private int exitStatus = -1;
	private File logfile = null;

	private static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

	public int getExitStatus() {
		return this.exitStatus;
	}

	/**
	 * Konstruktor
	 */
	public CrawlLog(File logfile) {
		this.exitStatus = -1;
		this.logfile = logfile;
	}

	public void parse() {
		this.exitStatus = -1;
		BufferedReader buf = null;
		String regExp = "^INFO Exiting with status ([0-9]+)";
		Pattern pattern = Pattern.compile(regExp);
		try {
			buf = new BufferedReader(new FileReader(logfile));
			String line = null;
			while ((line = buf.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					this.exitStatus = Integer.parseInt(matcher.group(1));
					break;
				}
			}
		} catch (IOException e) {
			WebgatherLogger
					.warn("Crawler Exit Status cannot be defered from crawlLog "
							+ logfile.getAbsolutePath() + "!", e.toString());
		} finally {
			try {
				if (buf != null) {
					buf.close();
				}
			} catch (IOException e) {
				WebgatherLogger.warn("Read Buffer cannot be closed!");
			}
		}
	}

}
