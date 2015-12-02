package jobs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import models.BuildEngine;
import models.BuildEngineOption;
import models.Project;
import models.ProjectType;
import models.User;

import org.apache.commons.io.FileUtils;

import play.Logger;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import core.common.controller.SettingsManager;
import core.common.type.CideException;

@OnApplicationStart
public class StartUp extends Job {

	@Override
	public void doJob() throws CideException {

		Logger.info("[Core] Welcome to CIDE!");

		initDB();

		try {
			compileJavascripts();
			SettingsManager.createInstance();

		} catch (IOException ex) {

			throw new CideException("Couldn't concatenate the JS files!", ex);

		} catch (Exception ex) {

			throw new CideException("Couldn't read the settings.", ex);
		}
	}

	/**
	 * Concatenate JS files so we just have to load a FAT js file!
	 * 
	 * @throws IOException
	 */
	public static void compileJavascripts() throws IOException {

		File jsPath = new File(Play.applicationPath + File.separator + "public"
				+ File.separator + "javascripts" + File.separator + "widgets");

		// In each directories, we compress all the JS files
		for (String widgetFoldername : jsPath.list()) {

			Logger.debug("[Startup] Concatening JS files of '%s'...",
					widgetFoldername);

			// Looping through all the JS files
			File javascriptFolder = new File(jsPath + File.separator
					+ widgetFoldername);

			if (javascriptFolder.isDirectory()) {
				File javascriptFile = new File(javascriptFolder
						+ File.separator + widgetFoldername + "-fat.js");
				if (javascriptFile.exists()) {
					javascriptFile.delete();
					Logger.debug("[Startup] JS file "
							+ javascriptFile.getName()
							+ " already exists, deleting.");
				}
				
				List<String> files = Arrays.asList(javascriptFolder.list());
				Collections.sort(files);
				
				for (String javascriptFilename : files) {

					File javascriptFileToAppend = new File(javascriptFolder
							+ File.separator + javascriptFilename);

					if (javascriptFileToAppend.getName().contains(".js") && !javascriptFileToAppend.getName().contains(".js~")) {

						Logger.debug("[Startup] ... " + javascriptFilename);

						FileUtils.writeLines(javascriptFile,
								FileUtils.readLines(javascriptFileToAppend),
								null, true);
					}
				}
			}
		}

		Logger.debug("[AppPath] %s", Play.applicationPath);
	}

	public void initDB() {

		if (User.count() == 0) {
			
			List<User> users = new ArrayList<>();
			User user1 = User.createUser("Guillaume", "Brunier", "gbrunier",
					"brunier@ece.fr", "0000", true).save();
			User user2 = User.createUser("Gabriel", "Féron", "gferon",
					"feron@ece.fr", "24682468", true).save();
			User user3 = User.createUser("Alexandre", "Trufanow", "truff",
					"trufanow@ece.fr", "lol", true).save();
			User user4 = User.createUser("Fabien", "Bavent", "fbavent",
					"bavent@ece.fr", "123", true).save();
			User user5 = User.createUser("Richard", "Stallman", "rstallman",
					"rstallman@gnu.org", "gnu", true).save();
			User user6 = User.createUser("James", "Gosling", "jgosling",
					"jgosling@oracle.com", "java", true).save();

			users.add(user1);
			users.add(user2);
			users.add(user3);
			users.add(user4);
			users.add(user5);
			users.add(user6);

			// Options for the BuildEngine
			BuildEngineOption option1 = new BuildEngineOption("gccFlag",
					"-Wall").save();
			BuildEngineOption option2 = new BuildEngineOption("gccFlag", "-03")
					.save();

			// BuildEngine
			BuildEngine buildEngine = new BuildEngine("gcc", "The GNU C Compiler (GCC)");
			buildEngine.options.add(option1);
			buildEngine.options.add(option2);
			buildEngine.save();

			BuildEngine buildEngine2 = new BuildEngine("bash", "Bash, the most popular UNIX shell");
			buildEngine2.save();

			BuildEngine buildEngine3 = new BuildEngine("jdk7", "The Java Development Kit (JDK7) from Oracle");
			buildEngine3.save();

			// ProjectType
			ProjectType type = new ProjectType("Console - C", "Command-line C program");
			type.buildEngine = buildEngine;
			type.save();

			ProjectType type2 = new ProjectType("Bash", "Small script in shell-format");
			type2.buildEngine = buildEngine2;
			type2.save();

			ProjectType type3 = new ProjectType("Console - Java", "Command-line Java program");
			type3.buildEngine = buildEngine3;
			type3.save();

			ProjectType type4 = new ProjectType("Web - PHP", "Web application in PHP5");
			type4.save();
			
			// Project
			Project project = Project.createProject("Hello, world",
					"http://ece.fr/~feron/hello-word", user2, new ArrayList<User>(), 
					type);
			// Manually set the UUID so it stays the same during development
			project.uuid = UUID
					.fromString("e7f300f7-a428-4e5c-aafe-b215df363ec2");
			project.type = type;
			project.users.add(user1);
			project.users.add(user2);
			project.users.add(user3);
			project.users.add(user4);
			project.users.add(user5);
			project.users.add(user6);
			project.save();
			
			Project project2 = Project.createProject("Fibonacci",
					"http://ece.fr/", user1, new ArrayList<User>(), type);
			// Manually set the UUID so it stays the same during development
			project2.uuid = UUID
					.fromString("82e99dd0-47fa-11e1-b86c-0800200c9a66");
			project2.type = type2;
			project2.users.add(user1);
			project2.users.add(user2);
			project2.users.add(user3);
			project2.users.add(user4);
			project2.users.add(user5);
			project2.users.add(user6);
			project2.save();
			
			Project project3 = Project.createProject("Javabien",
					"http://ece.fr/", user1, new ArrayList<User>(), type);
			// Manually set the UUID so it stays the same during development
			project3.uuid = UUID
					.fromString("6d7907d1-4a95-4aea-a3d8-379b67662f24");
			project3.type = type3;
			project3.users.add(user1);
			project3.users.add(user2);
			project3.users.add(user3);
			project3.users.add(user4);
			project2.users.add(user5);
			project2.users.add(user6);
			project3.save();
			
			Project project4 = Project.createProject("CakePHP",
					"http://ece.fr/", user1, new ArrayList<User>(), type);
			// Manually set the UUID so it stays the same during development
			project4.uuid = UUID
					.fromString("9ef50780-47fa-11e1-b86c-0800200c9a66");
			project4.type = type4;
			project4.users.add(user1);
			project4.users.add(user2);
			project4.users.add(user3);
			project4.users.add(user4);
			project2.users.add(user5);
			project2.users.add(user6);
			project4.save();
			
			Project project5 = Project.createProject("GNU Wget",
					"http://www.gnu.org/software/wget/", user1, new ArrayList<User>(), type);
			// Manually set the UUID so it stays the same during development
			project5.uuid = UUID
					.fromString("17b7c560-4862-11e1-b86c-0800200c9a66");
			project5.type = type4;
			project5.users.add(user1);
			project5.users.add(user2);
			project5.users.add(user3);
			project5.users.add(user4);
			project2.users.add(user5);
			project2.users.add(user6);
			project5.save();
			
		}
	}
}
