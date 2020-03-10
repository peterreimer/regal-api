package helper;

import java.io.File;
import java.lang.Process;
import java.lang.ProcessBuilder;

import play.Logger;

public class WpullThread extends Thread {

	ProcessBuilder pb = null;
	File log = null;

	private static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

	public WpullThread(ProcessBuilder pb, File log) {
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
		} catch (Exception e) {
			WebgatherLogger.error(e.toString());
			throw new RuntimeException("wpull crawl not successfully started!", e);
		}
	}

}
