/*
 * Copyright 2016 hbz NRW (http://www.hbz-nrw.de/)
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
package models;

import controllers.MyController;
import org.apache.commons.codec.digest.DigestUtils;
import play.Play;

/**
 * @author Ingolf Kuss, hbz
 * @email  kuss@hbz-nrw.de
 * 
 */
public class EncryptedUser implements User {
    String role = null;

    /**
     * Dieser Nutzer zeichnet sich dadurch aus, dass er/sie sich mit verschlüsseltem Passwort anmeldet.
     * SALT und HASH sind die hier gewählten Verschlüsselungsmethoden für das Passwort.
     * 
     * Das Passwort wird per https übertragen, dann von der Klasse BasicAuthAction in der Methode "call" dekodiert.
     * Anschließend wird es an diese Klasse, Methode "authenticate" übergeben.
     * Diese Methode liest "Salz" und Hash aus der Konfigurationsdatei "application.conf".
     * Das der Methode übergebene Passwort wird nun "gesalzen", dann mit dem SHA-256 Algorithmus verschlüsselt,
     * und schließlich mit dem hinterlegten verschlüsselten Passwort (Hash) verglichen. 
     * 
     */
    public EncryptedUser() {
    	
    String adminSalt;
    String adminHash;
	String editorSalt;
	String editorHash;
	String readerSalt;
	String readerHash;
	String subscriberSalt;
	String subscriberHash;
	String remoteSalt;
	String remoteHash;
	
	adminSalt = Play.application().configuration().getString("regal-api.admin-salt");
	adminHash = Play.application().configuration().getString("regal-api.admin-hash");
	editorSalt = Play.application().configuration().getString("regal-api.editor-salt");
	editorHash = Play.application().configuration().getString("regal-api.editor-hash");
	readerSalt = Play.application().configuration().getString("regal-api.reader-salt");	
	readerHash = Play.application().configuration().getString("regal-api.reader-hash");
	subscriberSalt = Play.application().configuration().getString("regal-api.subscriber-salt");
	subscriberHash = Play.application().configuration().getString("regal-api.subscriber-hash");
	remoteSalt = Play.application().configuration().getString("regal-api.remote-salt");	
	remoteHash = Play.application().configuration().getString("regal-api.remote-hash");

	if(   adminSalt == null || adminHash == null 
	   || editorSalt == null || editorHash == null
	   || readerSalt == null || readerHash == null
	   || subscriberSalt == null || subscriberHash == null
	   || remoteSalt == null || remoteHash == null ) {
		throw new RuntimeException("Bitte in der Konfigurationsdatei SALT und HASH für die Passwörter aller Rollen hinterlegen !");
		}
	}

    @Override
    public User authenticate(String username, String password) {
	role = null;
	// "Luschi"-Rolle anonymous; geht auch ohne Passwort durch
	if (username == null || username.isEmpty()) {
	    role = MyController.ANONYMOUS_ROLE;
	} else if (MyController.ANONYMOUS_ROLE.equals(username)) {
	    role = MyController.ANONYMOUS_ROLE;
	} else if (password == null || password.isEmpty()) {
	    role = MyController.ANONYMOUS_ROLE;
	}
	if( role != null ) {
		play.Logger.info("Sie haben sich als anonymer Benutzer angemeldet.");
		return this;
	}
	
	// ab hier geht's nur noch mit gültigem Passwort zum Erfolg;
	// Authentifizierung mit Verschlüsselungsmechanismus SALT + HASH
	String saltedPasswd = null;
	String hashedPasswd = null;
	if (MyController.ADMIN_ROLE.equals(username)) {
		saltedPasswd = Play.application().configuration().getString("regal-api.admin-salt")+password;		
		hashedPasswd = Play.application().configuration().getString("regal-api.admin-hash");
	} else if (MyController.EDITOR_ROLE.equals(username)) {
		saltedPasswd = Play.application().configuration().getString("regal-api.editor-salt")+password;		
		hashedPasswd = Play.application().configuration().getString("regal-api.editor-hash");
	} else if (MyController.READER_ROLE.equals(username)) {
		saltedPasswd = Play.application().configuration().getString("regal-api.reader-salt")+password;		
		hashedPasswd = Play.application().configuration().getString("regal-api.reader-hash");
	} else if (MyController.SUBSCRIBER_ROLE.equals(username)) {
		saltedPasswd = Play.application().configuration().getString("regal-api.subscriber-salt")+password;		
		hashedPasswd = Play.application().configuration().getString("regal-api.subscriber-hash");
	} else if (MyController.REMOTE_ROLE.equals(username)) {
		saltedPasswd = Play.application().configuration().getString("regal-api.remote-salt")+password;		
		hashedPasswd = Play.application().configuration().getString("regal-api.remote-hash");
	}
	if( saltedPasswd == null ) {
		throw new RuntimeException("Authentifizierung ungültig !");
	}
	play.Logger.trace("gesalzenes Passwort: \""+saltedPasswd+"\"");
	String encryptedPasswd = DigestUtils.sha256Hex(saltedPasswd);
	if( ! encryptedPasswd.equals(hashedPasswd) ) {
		play.Logger.error("Falsches Passwort für Rolle "+username+" !");
		play.Logger.error("  erwarteter Hash:"+hashedPasswd);
		play.Logger.error("  erzeugter  Hash:"+encryptedPasswd);
		throw new RuntimeException("Authentifizierung ungültig !");
	}
	// Authentifizierung war erfolgreich
	role = username;
	play.Logger.trace("verschlüsseltes Passwort ist: \""+encryptedPasswd+"\"");
	play.Logger.info("Sie haben sich erfolgreich eingeloggt als: " + role);
	return this;
    }
    
    @Override
    public String getRole() {
	return role;
    }

    @Override
    public void setRole(String role) {
	this.role = role;
    }
}
