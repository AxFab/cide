/**
 * @namespace: cideCore.net
 */

cideCore.net = {};
(function() {
	/**
	 * JSON serializer
	 */
	var JSON = {};  
	JSON.stringify = function (obj) {
		var t = typeof (obj);
		if (t != "object" || obj === null) {
			// simple data type
			if (t == "string") {
				obj = '"'+obj+'"';
			}
			return String(obj);
		}
		else {
			// recurse array or object
			var n, v, json = [], arr = (obj && obj.constructor == Array);
			for (n in obj) {
				v = obj[n];
				t = typeof(v);
				if (t == "string") 
					v = '"'+v+'"';
				else if (t == "object" && v !== null) 
					v = JSON.stringify(v);
				json.push((arr ? "" : '"' + n + '":') + String(v));
			}
			return (arr ? "[" : "{") + String(json) + (arr ? "]" : "}");
		}
	}
	
	String.prototype.addSlashes = function() {
		  return this.replace(/\\/g,'\\\\')
			 .replace(/\'/g,'\\\'')
		     .replace(/\"/g,'\\\"')
		     .replace(/\0/g,'\\\0');
	};
	
	/**
	 * NetworkMessage to send to server
	 */
	this.NetworkMessage = function(source, destination, command, data) {
		this.header = {
	    	source: source,
	    	destination: destination
	    };
	    this.body = {
			message: command,
			object: data
	    };
	};
	
	/**
	 * Manage connection with server
	 */
	this.WebSocketConnection = function(servAddr) {
		
		var ws; // JS Websocket
		var address = servAddr; // Address to open socket on the server
		var channel = {}; // Channel that events are registered in
		
		var open = true; // Whether the socket is supposed to be open
		
		var callbacks = {};

		/**
		 * Open a socket to the server
		 */
		this.openSocket = function() {
			
			console.log(address);
			
			// Support Firefox
			if (!window.WebSocket && window.MozWebSocket) { 
				window.WebSocket = MozWebSocket; 
			}
			
			createSocket();
		};
		
		/**
		 * Register callback for when the socket is opened
		 */
		this.onSocketOpen = function (fn) {
			if (!callbacks['socketOpen']) {
				callbacks['socketOpen'] = [];
			}
			callbacks['socketOpen'].push(fn);
		}
		
		/**
		 * Register callback for when the socket is closed
		 */
		this.onSocketClose = function (fn) {
			if (!callbacks['socketClose']) {
				callbacks['socketClose'] = [];
			}
			callbacks['socketClose'].push(fn);
		}
		
		/**
		 * Fire the event evt
		 */
		var fireEvent = function (evt) {
			if (callbacks[evt]) {
				callbacks[evt].forEach(function(fn) {
					fn ();
				});
			}
		}
		
		/**
		 * Open a previously opened socket
		 */
		this.closeSocket = function () {
			open = false;
			
			ws.send("quit");
		};
		
		/**
		 * send a message to a plugin on the server
		 */
		this.sendMessage = function (networkMessage) {

			if(typeof(networkMessage.body.object) == "string") {
				networkMessage.body.object = networkMessage.body.object.addSlashes();
			}
			if(networkMessage.body.message != "setCursor") {
				console.log("Sending message : " + JSON.stringify(networkMessage));
			}
			ws.send (JSON.stringify(networkMessage));
		};
		
		/**
		 * Register a widget to receive network messages
		 */
		this.registerEvent = function (event, widget, callback) {
		    if (channel[event] === undefined) {
		    	channel[event] = new Array ();
		    }
		
		    channel[event].push({widget : widget, callback : callback});
		
		    console.log("Widget " + widget.name + " registered to receive " + event);
		};
		
		/**
		 * Unregister a widget with the widget manager
		 */
		this.unregisterEvent = function (event, widget) {
			if (channel[event]) {
				channel[event].forEach(function(w) {
					if (w.widget == widget) {
						delete channel[event][i];
					}
				});
			}
		};
		
		// Open or reopen socket
		var createSocket = function () {
			var waitTime = 20;
			
			// New socket
			ws = new WebSocket(address);
			
			// Socket opened
			ws.onopen = function () {
				waitTime = 1;
				fireEvent ('socketOpen');
			};

			// Message received from server
			ws.onmessage = function (evt) {
			    var msg = jQuery.parseJSON(evt.data);
				if(msg.body.message != "setForeignCursor") {
					console.log("Receiving message : " + evt.data);
				}
			    
			    if (channel[msg.body.message] === undefined) {
			    	console.log("Uncaught NetworkMessage " + msg.body.message);
					return;
			    }
			    
			    // Fire callbacks
			    channel[msg.body.message].forEach(function(w) {
			    	w.callback(w.widget, msg);
			    });
	
			};
			
			// Socket closed
			ws.onclose = function() {
				if (open) {
					console.log("Socket broken, reopening");
					
					waitTime *= 2;
					setTimeout(createSocket, waitTime);
				}
				fireEvent ('socketClose');
			}
		};
	}
}).apply(cideCore.net);
