package controllers;

import models.Globals;
import play.libs.F.Promise;
import play.mvc.Result;

public class Context extends MyController {

	public static Promise<Result> getContext() {
		return Promise.promise(() -> {

			return getJsonResult(Globals.profile.getContext());
		});
	}

}
