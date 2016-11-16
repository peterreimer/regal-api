import static org.junit.Assert.assertEquals;
import static play.test.Helpers.fakeApplication;

import org.junit.Before;

import base.BaseModelTest;
import helper.mail.Mail;

/**
 * @author I. Kuss
 *
 *         <ul>
 *         <li>Tests sending an E-Mail with javax.mail</li>
 *         <li>Tests the class helper/Mail.java</li>
 *         </ul>
 * 
 */
public class MailTest extends BaseModelTest {

	private Mail mail = null;

	@Before
	public void setUp() {
		mail = new Mail();
		mail.setTo(
				fakeApplication().configuration().getString("javax.mail.totest"));
		mail.setFrom(
				fakeApplication().configuration().getString("javax.mail.from"));
		String mailMsg =
				"This is a test message from the JUnitTest regal-api/test/MailTest.java";
		mail.setMessage(mailMsg);
		mail.setSubject("INFO Test message from regal-api/test/MailTest");
	}

	public final void testSendMail() {
		// Test-E-Mail abschicken, Returnwert muss NULL sein
		assertEquals(mail.sendMail(), (int) 0);
	}

}
