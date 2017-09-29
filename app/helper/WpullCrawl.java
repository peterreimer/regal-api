/*
 * Copyright 2014 hbz NRW (http://www.hbz-nrw.de/)
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
package helper;

import models.Gatherconf;
import play.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder;
import java.util.ArrayList;

/**
 * a class to implement a wpull crawl
 * 
 * @author Ingolf Kuss
 *
 */
public class WpullCrawl {

	private Gatherconf conf = null;
	private File crawlDir = null;
	private String localpath = null;
	private int exitState = 0;

	private static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

	public File getCrawlDir() {
		return crawlDir;
	}

	public String getLocalpath() {
		return localpath;
	}

	public int getExitState() {
		return exitState;
	}

	/**
	 * a constructor of a wpull crawl
	 * 
	 * @param conf the crawler configuration for the website
	 */
	public WpullCrawl(Gatherconf conf) {
		this.conf = conf;
	}

	/**
	 * creates a new wpull crawler job
	 */
	public void createJob() {
		// create subdirectory in wpull-data/
		// crawlDir = ...
	}

	/**
	 * starts crawling with wpull
	 */
	public void startJob() {
		try {
			// localpath = ...
			String urlRaw = conf.getUrl().replaceAll("http://", "")
					.replaceAll("https://", "").replaceAll("/$", "");
			// date = ...
			// String warcFilename = ...
			String executeCommand = "wpull3 " + conf.getUrl();
			ArrayList<String> domains = conf.getDomains();
			// loop over domains
			executeCommand +=
					" --span-hosts --domains=$url_raw,www.bernkastel.de,www.rlpdirekt.de --recursive";
			ArrayList<String> urlsExcluded = conf.getUrlsExcluded();
			// loop over urlsExcluded
			executeCommand +=
					" --reject-regex=\".*Veranstaltungskalender|Bürgerservice.*\"";
			executeCommand += " --link-extractors=javascript,html,css";
			executeCommand += " --warc-file=WEB-" + urlRaw + "-${datum}"; // replace
																																		// by
																																		// warcFilename
			executeCommand +=
					" --user-agent \"InconspiciousWebBrowser/1.0\" --no-robots";
			executeCommand += " --escaped-fragment --strip-session-id";
			executeCommand +=
					" --no-host-directories --convert-links --page-requisites --no-parent";
			executeCommand += " --database WEB-${url_raw}-${datum}.db";
			executeCommand += " --no-check-certificate";
			String[] execArr = executeCommand.split(" ");
			ProcessBuilder pb = new ProcessBuilder(execArr);
			pb.directory(crawlDir);
			File log = new File(crawlDir.toString() + "/crawl.log");
			pb.redirectErrorStream(true);
			pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
			Process proc = pb.start();
			assert pb.redirectInput() == ProcessBuilder.Redirect.PIPE;
			assert pb.redirectOutput().file() == log;
			assert proc.getInputStream().read() == -1;
			// exitState = proc.waitFor(); // don't wait
		} catch (IOException ioe) {
			WebgatherLogger.error(ioe.toString());
			throw new RuntimeException("wpull crawl not successfully started!", ioe);
		}
	}

}
