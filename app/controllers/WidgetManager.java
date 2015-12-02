/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import play.mvc.Controller;

/**
 *
 * @author Gabriel
 */
public class WidgetManager extends Controller {

	public static void getWidget(String widgetId, String layoutId) {

		// TODO: CSS for the widgets?
		String javascriptUrl = "/public/javascripts/widgets/" + widgetId + "/" + widgetId + ".js";

		renderTemplate("widgets/" + widgetId + "/" + widgetId + "-" + layoutId + ".html", javascriptUrl);
	}
}
