package authenticate;

import java.util.HashMap;
import java.util.Map;

import play.Play;
import play.mvc.Http.Context;

public class UserDB {

	Map<String, User> users;

	public UserDB() {
		users = new HashMap<>();
		User admin = new User();
		/*
		 * User reader = new User(); User editor = new User(); User subscriber = new
		 * User(); User remote = new User(); User guest = new User();
		 */

		admin.setPassword("test");
		admin.setUsername("admin");
		admin.setRole(Role.ADMIN);
		users.put(admin.getUsername(), admin);
	}

	public User getUser(String username) {
		return users.get(username);
	}

	public boolean isLoggedIn(Context ctx) {
		play.Logger.debug("Look up context " + ctx);
		play.Logger.debug("Look up context " + ctx.session());
		return (getUser(ctx) != null);
	}

	public User getUser(Context ctx) {
		return getUser(ctx.session().get("username"));
	}

	public boolean isUser(String username) {
		return users.containsKey(username);
	}

	public boolean isValid(String usernane, String password) {
		return ((usernane != null) && (password != null) && isUser(usernane)
				&& getUser(usernane).getPassword().equals(password));
	}
}
