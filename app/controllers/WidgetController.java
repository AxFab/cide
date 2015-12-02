package controllers;

import java.io.File;

import play.Logger;
import play.Play;
import play.mvc.Controller;

public class WidgetController extends Controller {

	public static void get(String widgetId) {

		Logger.trace("[WidgetController] Loading widget '" + widgetId + "'");
		String stylesheetsUrl = "/public/stylesheets/widgets/" + widgetId + ".css";
		String javascriptUrl = "/public/javascripts/widgets/" + widgetId + ".js";
		String documentUrl = "@widgets." + widgetId + "." + widgetId;

		renderTemplate(documentUrl, javascriptUrl, stylesheetsUrl);
	}
}
