package controllers;

import java.io.IOException;
import java.util.UUID;

import models.Project;
import models.User;

import jobs.StartUp;

import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;

public class DashboardController extends Controller {

	@Before
	public static void checkLogin() {

		if (session.get("user") == null) {
			UserController.login();
		}
	}

	public static void index(String project) {
		
		//if(params.get("dev") != null && params.get("dev").equals("true")) {
		//	try {
				try {
					StartUp.compileJavascripts();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		//	} catch (IOException e) {
		//		Logger.debug(e,"[DashboardController] Couldn't compile JS.");
		//	}
		//}
		
		// Create the 
		String url = "wss://" + request.host + "/" + "socket/connect";
		renderArgs.put("websocketUrl", url);
		
		if(project == null) {
			ProjectController.list();
		}
		
		UUID projectUuid = UUID.fromString(project);

		// Get the currentProject
		Project proj = Project.find("uuid = ?", projectUuid).first();
		if (proj != null) {
			session.put("currentProject", proj.uuid);
		}

		render();
	}
}
