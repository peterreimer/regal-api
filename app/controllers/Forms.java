package controllers;

import actions.BasicAuth;
import authenticate.User;
import helper.HttpArchiveException;
import models.Message;
import play.data.Form;
import play.libs.F.Promise;
import play.mvc.Result;

@BasicAuth
public class Forms extends MyController {

	public static Promise<Result> getCatalogForm() {
		return new CreateAction().call((userId) -> {
			try {
				return JsonMessage(new Message(
						"Here the user will be able to Link a HT-Number to the Form in order to import Aleph data.",
						501));
			} catch (Exception e) {
				throw new HttpArchiveException(500, e);
			}
		});
	}

	public static Promise<Result> getArticleForm() {
		return new CreateAction().call((userId) -> {
			try {
				return JsonMessage(new Message(
						"Here the user will be able add bibligraphic data for articles.",
						501));
			} catch (Exception e) {
				throw new HttpArchiveException(500, e);
			}
		});
	}

	public static Promise<Result> getWebpageForm() {
		return new CreateAction().call((userId) -> {
			try {
				return JsonMessage(new Message(
						"Here the user will be able to Link a HT-Number to the Form in order to import Aleph data and to configure a webpage gathering.",
						501));
			} catch (Exception e) {
				throw new HttpArchiveException(500, e);
			}
		});
	}

	public static Promise<Result> getPartForm() {
		return new CreateAction().call((userId) -> {
			try {
				return JsonMessage(new Message(
						"Here the user will be able to create a new part Form.", 501));
			} catch (Exception e) {
				throw new HttpArchiveException(500, e);
			}
		});
	}

	public static Promise<Result> getFileForm() {
		return new CreateAction().call((userId) -> {
			try {
				return JsonMessage(new Message(
						"Here the user will be able to upload a new file.", 501));
			} catch (Exception e) {
				throw new HttpArchiveException(500, e);
			}
		});
	}

	public static Promise<Result> getLoginForm() {
		return Promise.promise(() -> {
			try {
				if (models.Globals.users.isLoggedIn(ctx())) {
					return redirect(routes.Forms.getLogoutForm());
				}
				Form<User> userForm = Form.form(User.class);

				return ok(views.html.login.render(userForm));

			} catch (Exception e) {
				throw new HttpArchiveException(500, e);
			}
		});
	}

	public static Promise<Result> getLogoutForm() {
		try {

			return Promise.promise(() -> {
				return ok(views.html.logout.render());
			});
		} catch (Exception e) {
			throw new HttpArchiveException(500, e);
		}
	}

	public static Promise<Result> postLogout() {
		return new CreateAction().call((userId) -> {
			session().clear();
			return redirect(routes.Application.index());
		});
	}

}
