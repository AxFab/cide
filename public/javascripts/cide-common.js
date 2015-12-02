/**
 * @namespace: cideCore.common
 */

cideCore.common = {};
(function() {

	/**
	 * Execute a function by name
	 */
	this.executeFunctionByName = function(functionName, context, args) {
		
		console.log(functionName + " - "+context + " - "+args);
		var args = Array.prototype.slice.call(arguments).splice(2);
		var namespaces = functionName.split(".");
		var func = namespaces.pop();
		for ( var i = 0; i < namespaces.length; i++) {
			context = context[namespaces[i]];
		}
		return context[func].apply(this, args);
	}

	/*
	 * Dynamically update user settings
	 */
	this.Settings = function() {
		this.onLoad = function() {
			// Update a single setting
			cide.socket.registerEvent("updatedSetting", this, function(self,
					msg) {
				if (msg.body.object.name === undefined
						|| msg.body.object.value === undefined)
					return;

				cide.settings[msg.body.object.name] = msg.body.object.value;
			});
		}
	};

	/*
	 * Class : LayoutManager Retrieves each widget from the server and
	 * executes the associated initialization code. Once all the widgets
	 * have been added, loadWidgets must be called.
	 */
	this.LayoutManager = function() {
		
		var loaded = false; // Whether the widgets have been loaded
		var widgetCount = 0;
		var loadFunctions = [];

		var hidden = true;
		var self = this;
		
		this.showBottomLayout = function() {
			
			if(hidden) {
				$("#layout-bottom").stop().animate({bottom:'0px'},{queue:false,duration:160});
				$("#layout-bottom .hide-btn a").text("Hide");
				hidden = false;
			}
		}
		
		this.hideBottomLayout = function() {
			
			if(!hidden) {
				$("#layout-bottom").stop().animate({bottom:'-250px'},{queue:false,duration:160});
				$("#layout-bottom .hide-btn a").text("Show");
				hidden = true;
			}
		}
		
		//Initialize the bottom Layout
		$('#layout-bottom .hide-btn').click(function() {
			
			if(hidden) {
				
				self.showBottomLayout();
				
			} else {
				
				self.hideBottomLayout();
			}
		}); 
		
		$('#layout-left .pills').tabs();
		$('#layout-bottom .pills').tabs();
		
		/**
		 * Add a widget from the server. All widgets must be unloaded
		 * widgetConf is a configuration object with the following format :
		 * {
		 * 		'layout' : pos,
		 * 		'html' : url,
		 * 		'js' : [url, url]
		 * }
		 */
		this.addWidget = function(widgetName, widgetConf) {
			if (loaded) {
				console
						.log("Warning: A widget was added while widgets were loaded");
			}

			if ($("#widget-" + widgetName).length || cide.widgets[widgetName]) {
				console.log("Error: Tried to load widget " + widgetName
						+ " twice");
				return;
			}

			if (!$("#layout-" + widgetConf.layout).length) {
				console.log("Error: Layout " + widgetConf.layout
						+ " not found when adding widget " + widgetName);
				return;
			}

			// Load the widget

			// Load HTML
			$.ajax({
				url : widgetConf.html,
				success : function(data) {

					$("#layout-" + widgetConf.layout).append(data);

					// Load each JS file once HTML is loaded
					widgetConf.js.forEach(function(url) {

						var script   = document.createElement("script");
						script.type  = "text/javascript";
						script.src   = url;

						document.head.appendChild(script);
						console.log("Script added to <head>: " + url);
					});

					widgetCount++;
					console
							.log("Widget " + widgetName
									+ " loaded successfully");
				}
			});

		};

		/**
		 * Remove a widget from the server. All widgets must be unloaded
		 */
		this.removeWidget = function(widgetName) {
			if (loaded) {
				console
						.log("Warning: A widget was removed while widgets were loaded");
			}

			delete cide.widgets[widgetName];
			$("#widget-" + widgetName).remove();
			widgetCount--;
		};

		/**
		 * Remove all widgets
		 */
		this.clearWidget = function() {
			console.log("Removing all widgets");

			$(".widget").remove();
			cide.widgets = {};
			widgetCount = 0;
		};

		/**
		 * Register a widget and call load function if all widgets were loaded
		 */
		this.registerWidget = function(widget) {
			cide.widgets[widget.name] = widget;

			if (Object.keys(cide.widgets).length == widgetCount)
				loadWidgets();
		};

		/**
		 * Register a function to be executed when all functions are loaded before the 
		 * widgets are loaded 
		 */
		this.onLoad = function(fn) {
			loadFunctions.push(fn);
		};

		/**
		 * Load every widget
		 */
		var loadWidgets = function() {
			if (loaded) {
				console.log("Warning: Reloading widgets when already loaded");
			}

			for ( var i = 0; i < loadFunctions.length; i++) {
				loadFunctions[i]();
			}

			for (w in cide.widgets) {
				console.log("onload" + w);
				cide.widgets[w].onLoad();
			}

			loaded = true;
		};

		/**
		 * Unload every widget
		 */
		this.unloadWidgets = function() {
			if (!loaded) {
				console.log("Warning: Unloading widgets when already unloaded");
			}

			for (w in cide.widgets) {
				cide.widgets[w].onUnload();
			}

			loaded = false;
		};
	}
}).apply(cideCore.common);
