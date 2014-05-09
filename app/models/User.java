package models;

import play.Play;

public class User {

    String name;
    String pwd;

    public User() {

	name = Play.application().configuration().getString("regal-api.user");
	pwd = Play.application().configuration()
		.getString("regal-api.password");
    }

    public User authenticate(String username, String password) {
	return name.equals(username) ? this : null;
    }
}
