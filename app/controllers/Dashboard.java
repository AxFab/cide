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
public class Dashboard extends Controller {

	public static void index() {
		
		if(session.get("user") == null) {
			Authenticate.login();
		}
		render();
	}
}
