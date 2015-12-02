
// TODO Define message protocole

JSON.stringify = JSON.stringify || function (obj) {
	var t = typeof (obj);
	if (t != "object" || obj === null) {
		// simple data type
		if (t == "string") 
			obj = '"'+obj+'"';
		return String(obj);
	}
	else {
		// recurse array or object
		var n, v, json = [], arr = (obj && obj.constructor == Array);
		for (n in obj) {
			v = obj[n]; t = typeof(v);
			if (t == "string") 
				v = '"'+v+'"';
			else if (t == "object" && v !== null) 
				v = JSON.stringify(v);
			json.push((arr ? "" : '"' + n + '":') + String(v));
		}
		return (arr ? "[" : "{") + String(json) + (arr ? "]" : "}");
	}
};

function NetworkMessage (src, dest, cmd) {
	this.header = {};
	this.header.source = src;
	this.header.destination = dest;
	this.body = {};
	this.body.message = cmd;
	this.body.object = null;
}

NetworkMessage.prototype.setData = function (obj) {
	this.body.object = JSON.stringify (obj);
}

function serverConnect() {
	this.ws;
	this.actions = new Array();
};

serverConnect.prototype.getConnect = function (address) {
	// Support Firefox
	if (!window.WebSocket && window.MozWebSocket) { 
		window.WebSocket = MozWebSocket; 
	}
	
	// New socket
	this.ws = new WebSocket(address);	
	this.ws.parent = this;
	
	// Open the socket
	this.ws.onopen = function () {
		console.log ("Socket Opened");
	};
	
	// Receive a message
	this.ws.onmessage = function (evt) {
		console.log ("Message received: " + evt.data);
		var msg = jQuery.parseJSON(evt.data);
		if (msg.header.destination == "dbg_cide") {
			console.log (msg.header.source + " :: " + msg.body);
		}
		else {
			// console.log (this);
			if (this.parent.actions[msg.header.destination] != null) 
				this.parent.actions[msg.header.destination] (msg);
			else
				console.log ("Unknown destination: " + msg.header.destination);
		}
	}
	
	// Clode the socket
	this.ws.onclose = function() { 
		console.log ("Socket Closed");
		// Check if we need to reconnect....
	}
}

// Interface to send a message to the server
serverConnect.prototype.sendMsg = function (header, body) {
	var jsonObj = {};
	jsonObj.header = header;
	jsonObj.body = body;	
	var str = JSON.stringify(jsonObj);
	console.log ("Message sended: " + str);
	this.ws.send (str);
}

serverConnect.prototype.sendMessage = function (msg) {
	var str = JSON.stringify(msg);
	console.log ("Message sended: " + str);
	this.ws.send (str);
}

// Register a callback
serverConnect.prototype.onReceive = function (id, callback) {
	this.actions[id] = callback;
}

serverConnect.prototype.closeSocket = function () {
	this.ws.send ("quit");
}

serverConnect.prototype.getHeader = function (src, dest) {
	var head = {};
	head.destination = dest;
	head.source = src;
	return head;
}
