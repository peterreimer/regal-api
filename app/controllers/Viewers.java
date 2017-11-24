package controllers;

import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;

import helper.HttpArchiveException;
import models.Globals;
import play.libs.F.Promise;
import play.mvc.Result;

public class Viewers extends MyController {

	public static Promise<Result> getViewer(String type, String pid) {
		return new ReadDataAction().call(pid, node -> {
			try {
				if ("video".equals(type)) {
					return ok(views.html.standardViewer.render("video"));
				}
				if ("audio".equals(type)) {
					return ok(views.html.standardViewer.render("audio"));
				}
				if ("deepzoom".equals(type)) {
					return ok(views.html.deepzoomViewer.render());
				}
				return redirect(routes.Resource.listData(pid));
			} catch (Exception e) {
				throw new HttpArchiveException(500, e);
			}
		});
	}
}
