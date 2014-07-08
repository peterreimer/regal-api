package models;

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

/**
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 * 
 * @see <a
 *      href="http://digitalsanctum.com/2012/06/07/basic-authentication-in-the-play-framework-using-custom-action-annotation/http://digitalsanctum.com/2012/06/07/basic-authentication-in-the-play-framework-using-custom-action-annotation">digitalsanctum</a>
 */
@SuppressWarnings("javadoc")
public class User {
    String role = null;

    public User authenticate(String username, String password) {
	// authenticate user against LDAP and find it's role

	role = username;
	return this;
    }

    public String getRole() {
	return role;
    }

    public void setRole(String role) {
	this.role = role;
    }

}
