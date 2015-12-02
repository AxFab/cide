package controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.Project;
import models.ProjectType;
import models.User;
import play.Logger;
import play.mvc.Controller;

public class ProjectController extends Controller {

	public static void post(String command, String project) {

		if (command == null) {
			ProjectController.list();
		}

		if (command.contains("New project")) {
			ProjectController.create();

		} else if (command.contains("Load project")) {
			DashboardController.index(project);
		}
	}

	public static void doCreate(String name, String url, String type, String[] users) {

		boolean isValid = true;

		Logger.debug("[ProjectController] %s", name);

		if (name.isEmpty()) {
			validation.addError("name", "error");
			isValid = false;
		}

		if (url.isEmpty()) {
			validation.addError("url", "error");
			isValid = false;
		}

		if (!isValid) {

			validation.keep();
			create();
		}

		// Current user
		User currentUser = User.find("username = ?", session.get("user"))
				.first();

		ProjectType projectType = ProjectType.find("name = ?", type).first();

		List<User> usersList = new ArrayList<>();
		usersList.add(currentUser);
		for(String user: users) {
			usersList.add(User.getUser(user));
		}

		Project project = Project.createProject(name, url, currentUser, usersList,
				projectType);
		project.save();

		// Opening the project in the dashboard
		DashboardController.index(project.uuid.toString());
	}

	public static void addUser(String username) {
		User user = User.find("username = ?", username).first();

		if (user != null)
			renderText("{\"username\" : \"" + user.username
					+ "\", \"firstname\" : \"" + user.firstname
					+ "\", \"lastname\" : \"" + user.lastname + "\"}");

		renderText("-1");
	}

	public static void create() {

		// Get all the available ProjectTypes
		List<ProjectType> projectTypes = ProjectType.findAll();

		renderArgs.put("projectTypes", projectTypes);
		render();
	}

	public static void list() {

		// Current user
		User currentUser = User.find("username = ?", session.get("user"))
				.first();

		// Fetching the projects of the current user
		// List<Project> projects =
		// Project.find("SELECT DISTINCT p FROM Project p INNER JOIN p.users u WHERE u.username = ?",
		// currentUser.username).fetch();

		renderArgs.put("firstname", currentUser.firstname);
		renderArgs.put("projects", currentUser.projects);
		render();
	}
}
