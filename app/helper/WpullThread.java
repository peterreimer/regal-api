package helper;

import java.io.File;
import java.lang.Process;
import java.lang.ProcessBuilder;
import models.Gatherconf;
import play.Logger;

/**
 * @author I. Kuss Ein Thread, in dem ein Webcrawl gestartet wird. Der Thread
 *         wartet, bis der Crawl beendet ist. Ist der Crawl mit Fehler beendet,
 *         wird ein neuer Thread aufgerufen, der einen erneuten Crawl-Versuch
 *         macht. Es gibt eine Obergrenze für die Anzahl Crawl-Versuche:
 *         maxNumberAttempts.
 */
public class WpullThread extends Thread {

	Gatherconf conf = null;
	ProcessBuilder pb = null;
	File log = null;
	/**
	 * Der wievielte Versuch ist es, diesen Crawl zu starten ?
	 */
	int attempt = 1;

	private static int maxNumberAttempts = 10;
	private static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

	/**
	 * Der Konstruktor für diese Klasse.
	 * 
	 * @param conf Die Gatherconf der Website, die gecrawlt werden soll.
	 * @param pb Ein Objekt der Klasse ProcessBuilder mit Aufrufinformationen für
	 *          den Crawl.
	 * @param log Eine Logdatei für den Webcrawl (crawl.log)
	 * @param attempt Der wievielte Versuch es ist, diesen Webschnitt zu sammeln.
	 */
	public WpullThread(Gatherconf conf, ProcessBuilder pb, File log,
			int attempt) {
		this.conf = conf;
		this.pb = pb;
		this.log = log;
		this.attempt = attempt;
	}

	/**
	 * This methods starts a webcrawl and waits for completion.
	 */
	@Override
	public void run() {
		try {
			Process proc = pb.start();
			assert pb.redirectInput() == ProcessBuilder.Redirect.PIPE;
			assert pb.redirectOutput().file() == log;
			assert proc.getInputStream().read() == -1;
			int exitState = proc.waitFor();
			/**
			 * Exit-Status: 0 = Crawl erfolgreich beendet
			 */
			WebgatherLogger.info("Webcrawl for " + conf.getName()
					+ " exited with exitState " + exitState);
			if (exitState == 0) {
				return;
			}
			attempt++;
			if (attempt > maxNumberAttempts) {
				throw new RuntimeException("Webcrawl für " + conf.getName()
						+ " fehlgeschlagen: Maximale Anzahl Versuche überschritten !");
			}
			// Crawl wird erneut angestoßen
			WebgatherLogger.info("Webcrawl for " + conf.getName()
					+ " wird erneut angestoßen. " + attempt + ". Versuch.");
			WpullThread wpullThread = new WpullThread(conf, pb, log, attempt);
			wpullThread.start();
		} catch (Exception e) {
			WebgatherLogger.error(e.toString());
			throw new RuntimeException("wpull crawl not successfully started!", e);
		}
	}

}
