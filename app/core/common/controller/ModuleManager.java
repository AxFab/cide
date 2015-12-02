package core.common.controller;

import java.util.HashMap;
import java.util.Map;

import models.Project;
import play.Logger;
import core.common.type.CideException;
import core.common.type.Event;
import core.common.type.Module;
import core.net.controller.ConnectionManager;
import core.net.type.core.NetworkMessage;

public class ModuleManager {

	/**
	 * Instances of ModuleManagers
	 */
	static public HashMap<Project, ModuleManager> instances = new HashMap<>();

	/**
	 * Project we are working on
	 */
	public Project project;

	private ModuleManager(Project project) {

		this.project = project;
	}

	/**
	 * Module instances, used to dispatch NetworkMessages
	 */
	private Map<String, Module> modules = new HashMap<>();
	private Map<String, Module> eventSubscribers = new HashMap<>();

	/**
	 * Return the existing instance of the module manager related to a given
	 * project
	 * 
	 * @param project
	 *            Project
	 * @return instance
	 */
	public static ModuleManager getInstance(Project project) {

		Logger.info(
				"[Core] Getting instance of ModuleManager for project '%s'",
				project);

		ModuleManager mm = instances.get(project);
		if (mm == null) {

			Logger.debug("[Core] Instanciating ModuleManager for project '%s'",
					project);
			mm = new ModuleManager(project);

			// Add this to the static map of ModuleManagers
			instances.put(project, mm);

			mm.start();
		}

		return instances.get(project);
	}

	/**
	 * Called on project load
	 */
	public void start() {

		// Scan the classpath for modules
		ClassLoader classLoader = ModuleManager.class.getClassLoader();

		try {

			for (String moduleClass : SettingsManager.getModules()) {
				Logger.info("Loading module %s", moduleClass);
				Class aClass = classLoader.loadClass(moduleClass);

				Object instance = aClass.newInstance();
				if (instance instanceof Module) {

					Module module = (Module) instance;
					register(module);

				}
				Logger.debug(aClass.getName());
			}

		} catch (ClassNotFoundException e) {

			Logger.error(e, "[ModuleManager] " + e.getMessage());

		} catch (Exception e) {

			Logger.error(e, "[ModuleManager] " + e.getMessage());
		}
	}

	/**
	 * Register a plugin so that it can receive NetworkMessages
	 * 
	 * @param module
	 */
	public void register(Module module) {

		if (!modules.containsKey(module.getClass().getCanonicalName())) {

			modules.put(module.getClass().getCanonicalName(), module);
			Logger.info("[ModuleManager] Registering module '%s'", module
					.getClass().getCanonicalName());

		} else {

			Logger.warn(
					"[ModuleManager] The module '%s' is already registered.",
					module.getClass().getCanonicalName());
		}

		try {

			module.onRegister(this);

		} catch (Exception ex) {

			Logger.error(ex,
					"[ModuleManager] Error while registering module '%s'",
					module.id());
		}
	}

	/**
	 * Destroy registered module
	 * 
	 * @param module
	 */
	public void unregister(Module module) {
		module.onUnregister();

		modules.remove(module);
	}

	/**
	 * Subscribe a module to a given event
	 * 
	 * @param message
	 * @param m
	 */
	public void subscribeEvent(String message, Module m) {

		if (!eventSubscribers.containsKey(message)) {
			eventSubscribers.put(message, m);
		}
		eventSubscribers.get(message);
	}

	/**
	 * Unsubscribe an module from a given event
	 * 
	 * @param event
	 * @param module
	 */
	public void unsubscribeEvent(String message, Module module) {
		if (eventSubscribers.containsKey(message)) {
			eventSubscribers.remove(message);

			if (eventSubscribers.get(message) == null) {
				eventSubscribers.remove(message);
			}
		} else {
			Logger.error(
					"[ModuleManager] Unsubscribing event which was never subscribed %s",
					message);
		}
	}

	/**
	 * Send an event to all modules that subscribed to it
	 * 
	 * @param evt
	 */
	public void dispatchEvent(Event evt) {
		
		Logger.debug("Event dispatch: "+evt.message);

		if (eventSubscribers.containsKey(evt.message)) {
			
			Module m = eventSubscribers.get(evt.message);

			try {
				
				m.onEvent(evt);
				
			} catch (CideException ex) {

				Logger.error(ex,
						"[ModuleManager] Error while dispatching event '%s'",
						evt.message);
			}
		}
	}

	/**
	 * Dispatch a network message
	 * 
	 * @param msg
	 */
	public void dispatchNetworkMessage(NetworkMessage msg) {
		
		if (modules.containsKey(msg.header.destination)) {

			Module lmod = modules.get(msg.header.destination);

			try {

				lmod.onNetworkMessage(msg);

			} catch (CideException ex) {
				Logger.error(ex, 
						"[ModuleManager] The message '%s' was dispatched but the process failed!",
						msg.body.message);
				
				NetworkMessage answer = msg.createAnswer();
				answer.body.message = "unexpectedError";
				answer.body.object = ex.getMessage();
				ConnectionManager.sendMessage(answer);
			}

		} // If it's a wildcard message
		else if(msg.header.destination.equals("*")) {
			
			for(String modName: modules.keySet()) {
				
				try {

					modules.get(modName).onNetworkMessage(msg);

				} catch (CideException ex) {
					Logger.error(ex, 
							"[ModuleManager] The wildcard message '%s' was dispatched but the process failed!",
							msg.body.message);
					
					NetworkMessage answer = msg.createAnswer();
					answer.body.message = "unexpectedError";
					answer.body.object = ex.getMessage();
					ConnectionManager.sendMessage(answer);
				}
			}
			
		} else {
			Logger.error(
					"[ModuleManager] Unable to send a message to unregistered plugin %s",
					msg.header.destination);
		}
	}
}
