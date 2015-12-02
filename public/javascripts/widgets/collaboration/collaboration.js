// CIDE Collaboration Widget JS
//
// Gabriel Féron
// January 2012

/**
 * Collaboration widget
 */
cideWidgets.Collaboration = function() {

	this.name = "collaboration";
	this.currentNode = {};

	this.usernames = [];
	
	var self = this;

	this.onLoad = function() {

		cide.socket.registerEvent("userOpenedFile", this, this.onUserOpenedFile);

		cide.socket.registerEvent("userExit", this, this.onUserExit);
		
		cide.socket.registerEvent("chatMessage", this, this.onChatMessage);
		
		// Register jQuery events for the chat
		$('#widget-collaboration .btn.primary').click(this.triggerMessage);
		$('#chatInput').keypress(this.triggerMessage);
	}

	this.unLoad = function() {

	}
	
	this.triggerMessage = function(e) {
		
		if(e.keyCode != null) {
			// If key different than RETURN
			if(e.keyCode != 13) {
				return;
			}
		}
		
		var msg = $("#chatInput").val();
		$("#chatInput").val('');
		console.log("Sending message "+msg);
		self.sendMessage(msg);
	};
	
	this.sendMessage = function(msg) {
		
		if(cide.widgets["editor"].currentFile == null) {
			cide.widgets["error"].displayInfoBox("you can't chat without a document opened for collaboration!");
			return;
		}
		
		if(msg == null || msg == "") {
			return;
		}
		
		var data = {
			'message': msg,
			'filepath': cide.widgets["editor"].currentFile
		}
		
		cide.socket
		.sendMessage(new cideCore.net.NetworkMessage(
				this.name,
				"core.module.controller.EditorModule",
				"chatMessage",
				data));
	}
	
	this.onChatMessage = function(self, msg) {
		
		console.log("Received message from "+msg.body.object.username);
		
		// Adding a new line in the table
		var msgElement = $('<td />').html('<b>'+msg.body.object.username+':</b> '+msg.body.object.message);
		var msgRow = $('<tr />');
		msgRow.append(msgElement);
		$('#widget-collaboration .zebra-striped').append(msgRow);
		
		// Scroll at bottom
		var container = document.getElementById("chat-history");
		container.scrollTop = container.scrollHeight;
	}

	this.onUserOpenedFile = function(self, msg) {

		self.usernames = msg.body.object.usernames;

		self.refreshList();
	}

	this.onUserExit = function(self, msg) {

		var occurence = self.usernames.indexOf(msg.body.object);
		if (occurence != -1) {
			self.usernames.splice(occurence, occurence);
		}

		self.refreshList();
	}

	this.refreshList = function() {

		$('#widget-collaboration .users').empty();

		var usernamesString = "";
		this.usernames.forEach(function(username) {

			usernamesString = username + " " + usernamesString;
		});
		if(usernamesString == "") {
			usernamesString = "Just you :)";
		}

		$('#widget-collaboration .users').append('<b>On this file: </b>'+usernamesString);
	}
};

cide.manager.registerWidget(new cideWidgets.Collaboration());
