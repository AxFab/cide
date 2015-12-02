package controllers;

import models.User;

import org.apache.commons.lang.NotImplementedException;

import play.mvc.Controller;
import play.mvc.Before;
import core.net.controller.ConnectionManager;

public class UserController extends Controller {	

	public static void doLogin(String username, String password) {
            
		User user = User.isValidLogin(username, password);
		if (user != null) {
			
			session.put("user", user.username);
			session.put("user.firstname", user.firstname);
			session.put("user.lastname", user.lastname);
			ProjectController.list();
                        
		} else {
			
			validation.addError("login", "Login/Password combination was incorrect");
			validation.keep();
			login();
		}
	}
	
	// Use to display the login page
	public static void login() {
		
		if (session.get("user") == null) {
			render();
		}

		ProjectController.list();
	}

	//Use to disconnect the User
	public static void logout() {
	
		ConnectionManager.closeConnection(session.get("user"));
		
		session.remove("user");
		login();
	}

	/* Register Methods */
	// Use to register the new User
	public static void doRegister(String firstname, String lastname, String login, String email, String password, String passwordConfirm) {
		throw new NotImplementedException();
	}

	// Use to display the register page
	public static void register() {
		throw new NotImplementedException();
	}
}
