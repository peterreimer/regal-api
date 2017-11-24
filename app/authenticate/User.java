package authenticate;

import java.util.ArrayList;
import java.util.List;

import play.data.validation.ValidationError;

public class User {

	String username;
	String password;
	Role role;

	public User() {

	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	/**
	 * Validates Form<LoginFormData>. Called automatically in the controller by
	 * bindFromRequest(). Checks to see that email and password are valid
	 * credentials.
	 * 
	 * @return Null if valid, or a List[ValidationError] if problems found.
	 */
	public List<ValidationError> validate() {

		List<ValidationError> errors = new ArrayList<>();

		if (!models.Globals.users.isValid(username, password)) {
			errors.add(new ValidationError("username", ""));
			errors.add(new ValidationError("password", ""));
		}

		return (errors.size() > 0) ? errors : null;
	}
}
