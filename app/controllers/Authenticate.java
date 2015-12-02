package controllers;

import core.PluginManager;
import core.manager.UserManager;
import play.mvc.Controller;

public class Authenticate extends Controller{
		
		
	/* Login Methods */
	//Use to connect the User
	public static UserManager userManager = null;
	public static void doLogin  (String username, String password) {
		
		if (userManager == null)
			userManager = (UserManager) PluginManager.getInstance("Cide.UserManager");
		
		if (userManager.isValidLogin(username, password)) {
			session.put("user", username);
			Application.index();
		}
		else {
			validation.addError("login", "Login/Password combination was incorrect");
			validation.keep();
			login();
		}
	}
	
	//Use to display the login page
	public static void login() {
	
		if (session.get("user") == null) {
			render();
		}
		
		Dashboard.index();
	}
	
	//Use to disconnect the User
	public static void logout() {
		session.remove("user");
		login();
	}
	
	
	/* Register Methods */
	//Use to register the new User
	public static void doRegister (String firstname, String lastname, String login, String email, String password, String passwordConfirm) {
		
		UserManager userManager = (UserManager) PluginManager.getInstance("Cide.UserManager");
		userManager.createUser(firstname, lastname, lastname, email, password);
	}
	
	//Use to display the register page
	public static void register () {
		renderText("TODO");
	}
}
