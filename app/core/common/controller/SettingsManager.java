/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.common.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import play.Logger;
import play.Play;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * @author Gabriel
 */
public class SettingsManager {

	private HashMap<String, String> settings;
	private List<String> modules;
	private List<String> widgets;
	
	private static SettingsManager instance;

	/**
	 * Private constructor for the singleton pattern
	 * 
	 */
	private SettingsManager() {

		settings = new HashMap<>();
		modules = new ArrayList<>();
		widgets = new ArrayList<>();
	}

	public static void createInstance() throws Exception {

		instance = new SettingsManager();
		instance.readSettings();
	}

	/**
	 * Singleton getter
	 * 
	 */
	public static SettingsManager getInstance() {

		return instance;
	}

	public static String getValue(String key) {

		return getInstance().settings.get(key);
	}
	
	public static List<String> getModules() {
		
		return getInstance().modules;
	}
	
	public static List<String> getWidgets() {
		
		return getInstance().widgets;
	}

	/**
	 * Read the settings stored in the CIDE configuration file
	 * 
	 * @throws IOException
	 */
	private void readSettings() throws Exception {

		File settingsFile = new File(Play.applicationPath + "/cide-conf.json");

		if (!settingsFile.exists()) {
			throw new IOException(
					"The settings file 'cide-conf.json' is not present in the root directory.");
		}

		// Open the settings file
		BufferedReader reader = new BufferedReader(new FileReader(settingsFile));

		JsonParser parser = new JsonParser();
		JsonObject rootElement = parser.parse(reader).getAsJsonObject();

		JsonObject settingsElement = rootElement.get("main.settings")
				.getAsJsonObject();

		String rootPath = settingsElement.get("rootPath").getAsString();
		if (rootPath == null) {
			throw new IllegalArgumentException(
					"The 'rootPath' element is missing in the configuration file!");
		} else {
			settings.put("rootPath", rootPath);
		}
		
		String templatesPath = settingsElement.get("templatesPath").getAsString();
		if (templatesPath == null) {
			throw new IllegalArgumentException(
					"The 'templatesPath' element is missing in the configuration file!");
		} else {
			settings.put("templatesPath", templatesPath);
		}

		String loadPlugins = settingsElement.get("loadPlugins").getAsString();
		settings.put("loadPlugins", loadPlugins);
		
		JsonArray pluginsArray = rootElement.get("main.modules").getAsJsonArray();
		for(JsonElement element: pluginsArray) {
			
			modules.add(element.getAsString());
		}
		
		JsonArray widgetsArray = rootElement.get("main.widgets").getAsJsonArray();
		for(JsonElement element: widgetsArray) {
			
			widgets.add(element.getAsString());
		}
		
		Logger.info("Settings loaded succesfully.");
	}
}