package helper;

import java.io.File;
import java.lang.Process;
import java.lang.ProcessBuilder;
import models.Gatherconf;
import play.Logger;

public class WpullThread extends Thread {

	Gatherconf conf = null;
	ProcessBuilder pb = null;
	File log = null;

	private static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

	public WpullThread(Gatherconf conf, ProcessBuilder pb, File log) {
		this.conf = conf;
		this.pb = pb;
		this.log = log;
	}

	public void run() {
		try {
			Process proc = pb.start();
			assert pb.redirectInput() == ProcessBuilder.Redirect.PIPE;
			assert pb.redirectOutput().file() == log;
			assert proc.getInputStream().read() == -1;
			int exitState = proc.waitFor();
			/**
			 * Mögliche Exit-Status: 0 = Crawl erfolgreich beendet, "INFO FINISHED."
			 */
			WebgatherLogger.info("Webcrawl for " + conf.getName()
					+ " exited with exitState " + exitState);
			if (exitState != 0) {
				// Crawl wird erneut angestoßen
				WebgatherLogger.info(
						"Webcrawl for " + conf.getName() + " wird erneut angestoßen.");
				WpullThread wpullThread = new WpullThread(conf, pb, log);
				wpullThread.start();
			}
		} catch (Exception e) {
			WebgatherLogger.error(e.toString());
			throw new RuntimeException("wpull crawl not successfully started!", e);
		}
	}

}
