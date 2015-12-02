package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import core.interfaces.IPlugin;
import core.manager.EditorManager;
import core.manager.LocalFileManager;
import core.manager.ProjectManager;
import core.manager.UserManager;
import java.util.HashMap;
import play.Logger;
import core.type.NetworkMessage;
import data.Event;

public class PluginManager {

	static HashMap<String, IPlugin> plugins;
	static Map<Event, List<IPlugin>> eventSubscribers;

	public static void start() {
		plugins = new HashMap<String, IPlugin>();
		eventSubscribers = new HashMap<Event, List<IPlugin>>();
		
		//Register plugins
		IPlugin plugin;
		plugin = new ProjectManager();
		Logger.debug ("Load plugin: '"+plugin.id()+"'");
		plugins.put(plugin.id(), plugin);
		plugin = new LocalFileManager();
		Logger.debug ("Load plugin: '"+plugin.id()+"'");
		plugins.put(plugin.id(), plugin);
		plugin = new UserManager();
		Logger.debug ("Load plugin: '"+plugin.id()+"'");
		plugins.put(plugin.id(), plugin);
		plugin = new EditorManager();
		Logger.debug ("Load plugin: '"+plugin.id()+"'");
		plugins.put(plugin.id(), plugin);
		
		for (IPlugin pluginInst : plugins.values()) {
			Logger.info ("Starting plugin '" + pluginInst.id() + "'");
			pluginInst.start ();
		}
	}

	public static String register(IPlugin plugin) throws Exception {
		if (plugins.get(plugin.id()) != null) {
			throw new Exception("Plugin already registered");
		}
		plugins.put(plugin.id(), plugin);
		return plugin.id();
	}

	public static void unregister(IPlugin plugin) {
		plugins.remove(plugin.id());
	}

	public static IPlugin getInstance(String pluginId) {
		Logger.debug("Search of plugin: " + pluginId);
		IPlugin plug = plugins.get(pluginId);
		if (plug != null) {
			return plug;
		}
		try {
			// load plugin
			register(plug);
		} catch (Exception e) {
			return null;
		}
		return plug;
	}
	
	public static IPlugin getInstance(String pluginId, Class type) {
		IPlugin plug = getInstance(pluginId);
		if (plug != null)
			if (type.isInstance(plug))
				return plug;
		return null;
	}

	public static void dispatchNetworkMessage(NetworkMessage msg) {
		if (msg == null) 
			return;
		IPlugin plugin = plugins.get(msg.header.destination);
		if (plugin != null)
			plugin.onNetworkMessage(msg);
		else
			Logger.error ("Try to send a message to unknown plugin: " + msg.header.destination);
	}
	
	public static void subscribeEvent (Event evt, IPlugin p) {
		if (!eventSubscribers.containsKey(evt)) {
			eventSubscribers.put(evt, new ArrayList<IPlugin>());
		}
		eventSubscribers.get(evt).add(p);
	}
	
	public static void dispatchEvent(Event evt, Object data) {
		List<IPlugin> instances = eventSubscribers.get(evt);
		
		if (instances != null) {
			for (IPlugin p : instances) {
				p.onEvent(evt, data);
			}
		}
	}
}
