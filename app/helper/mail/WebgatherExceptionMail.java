/**
 * 
 */
package helper.mail;

import play.Logger;
import play.Play;

/**
 * @author I. Kuss
 * @description die Methode sendMail() verschickt eine E-Mail, dass eine Website
 *              nicht gecrawlt werden konnte
 *
 */
public class WebgatherExceptionMail {

	private static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

	public static void sendMail(String pid, String url) {

		WebgatherLogger.error("Website {} kann nicht gecrawlt werden !", pid);
		// E-Mail erzeugen, dass Website nicht gecrawlt werden kann
		Mail mail = new Mail();
		mail.setTo(Play.application().configuration().getString("javax.mail.to"));
		mail.setFrom(
				Play.application().configuration().getString("javax.mail.from"));
		String mailMsg = "Das Crawlen der Website " + pid + " ist fehlgeschlagen !";
		if (url != null) {
			mailMsg += "\nURL: " + url;
		}
		mail.setMessage(mailMsg);
		mail.setSubject("ERROR crawling Webpage " + pid);

		String to = mail.getTo();
		int result = 0;
		result = mail.sendMail();
		if (result == 0) {
			WebgatherLogger
					.info(" Webgather Exception E-Mail erfolgreich verschickt an " + to);
		} else {
			WebgatherLogger
					.error("E-Mail an " + to + " konnte nicht zugestellt werden !");
		}
	}

}
