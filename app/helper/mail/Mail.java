/*
 * Copyright 2019 hbz NRW (http://www.hbz-nrw.de/)
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
package helper.mail;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

import models.Globals;

/**
 * 
 * @author Jan Schnasse, I. Kuss
 * 
 *
 */
public class Mail {

	public static void sendMail(String mailMessage, String mailSubject) {
		try {
			Properties properties = new Properties();
			properties.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("mail.properties"));
			properties.forEach((k, v) -> {
				play.Logger.debug(k + " : " + v);
			});
			String sender = properties.getProperty("sender");
			if (sender == null || sender.isEmpty()) {
				sender = "root@" + Globals.server;
			}
			String rc = properties.getProperty("recipient");
			if (rc == null || rc.isEmpty()) {
				rc = "root@localhost";
			}
			List<String> recipients = Arrays.asList(rc.split(","));
			Session session = Session.getDefaultInstance(properties);
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(properties.getProperty("sender")));
			for (String recipient : recipients) {
				message.addRecipient(Message.RecipientType.TO,
						new InternetAddress(recipient));
			}
			message.setSubject(mailSubject, "ISO-8859-1");
			message.setText(mailMessage, "UTF-8");
			message.setSentDate(new Date());
			Transport.send(message);
			play.Logger.info("Email was sent to " + recipients);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
