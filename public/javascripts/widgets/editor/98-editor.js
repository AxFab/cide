/**
 * CIDE - Editor widget
 */

cideWidgets.Editor = function() {

	this.name = "editor";
	var self = this;

	var openFileList = {};

	this.currentFile = null;

	// Use to display the icons on the left-side
	var annotations = new Array();

	var tabCount = 0;
	
	var lineBuffer;
	var lockedLine = -1;

	var lockTimeoutId = 0;
	var spawnTimeoutCount = 0;

	// Color array for the cursors
	var colors = [ 'green', 'red', 'orange', 'blue', 'purple', 'white', 'pink',
			'cyan', 'lime' ];
	var colorsCounter = 0;

	var aceDoc;

	var dmp = new diff_match_patch();

	this.onLoad = function() {

		// Register events
		cide.socket.registerEvent("sourceFile", this, this.onSourceFile);
		cide.socket
				.registerEvent("userLockedLine", this, this.onUserLockedLine);
		cide.socket.registerEvent("userUnlockedLine", this,
				this.onUserUnlockedLine);
		cide.socket.registerEvent("userOpenedFile", this, this.onUserOpenedFile);
		cide.socket.registerEvent("userClosedFile", this, this.onUserClosedFile);
		cide.socket.registerEvent("userChangedFile", this, this.onUserChangedFile);
		cide.socket.registerEvent("setForeignCursor", this,
				this.onSetForeignCursor);

		// Initialize ACE
		this.initAce();

		this.registerAceEvents();

		cide.socket.registerEvent("lineLocked", this, function(self, msg) {

			console.log("Line successfully locked!");
		});

		cide.socket.registerEvent("lineAlreadyLockedError", this, function(
				self, msg) {

			onLockError(msg.body.object);
			console.log("Line already locked by user "
					+ msg.body.object.username);
		});

		cide.socket.registerEvent("lineNotLockedError", this, function(self,
				msg) {

			onLockError(msg.body.object);
			console.log("Line already locked by user "
					+ msg.body.object.username);
		});
	};

	/**
	 * Initialize Ace and set any options
	 */
	this.initAce = function() {

		window.editor = ace.edit("editor");
		aceDoc = window.editor.getSession().doc;
		/*window.editor.setUseSoftTabs(true);*/
		window.editor.setTheme("ace/theme/textmate");
		window.editor.setShowPrintMargin(false);

		window.editor.setMainCursor("Me", "black");
	};

	/**
	 * Register events specific to ace
	 */
	this.registerAceEvents = function() {

		window.editor.getSession().addEventListener('change', onUpdateText);

		window.editor.getSession().getSelection().addEventListener(
				'changeCursor', onUpdateCursor);
	};

	/**
	 * Unregister events specific to ace
	 */
	this.unregisterAceEvents = function() {

		window.editor.getSession().removeEventListener('change', onUpdateText);

		window.editor.getSession().getSelection().removeEventListener(
				'changeCursor', onUpdateCursor);
	};

	/**
	 * Display a file which was received by the server
	 */
	this.onSourceFile = function(self, msg) {

		self.unregisterAceEvents();

		self.currentFile = msg.body.object.filepath;

		// Create tab if necessary
		if (openFileList[self.currentFile] === undefined) {

			var title = "";
			if(self.currentFile.lastIndexOf("/") != 1) {
				title = self.currentFile.substr(self.currentFile.lastIndexOf("/")+1, self.currentFile.length);	
			} else {
				title = self.currentFile;
			}
			
			// Create a elem
			var aElem = $(
					'<a style="cursor:pointer" rel="' + self.currentFile + '">'
							+ title + '</a>').bind('click', function(e) {

				// Open the file
				self.openFile($(this).attr("rel"));

				e.preventDefault();
			});

			var closeElem = $(
					'<img style="cursor:pointer; margin-top:4px; float:right; margin-right: 20px" src="/public/images/widgets/editor/icon-close.png"/>')
					.bind(
							"click",
							function() {

								// Get the parent
								var parent = $(this).parent();

								// Remove file from the
								// openFileList
								var filepath = $(parent).children("a").first()
										.attr("rel");
								delete openFileList[filepath];
								
								// Closing the file on the server
								cide.socket.sendMessage(new cideCore.net.NetworkMessage(self.name,
										"core.module.controller.EditorModule", "closeFile", filepath));

								// Remove the parent
								$(parent).remove();

								// Active the first tab if
								// exists
								if ($("#editor-tabs li").length > 0) {
									$("#editor-tabs li").first().children("a")
											.first().click();
								} else {
									self.unregisterAceEvents();
									window.editor.getSession().setValue("");
									self.registerAceEvents();
								}

								return false;
							});

			// Create li elem
			var liElem = $('<li id="editor-tabs-' + tabCount + '"></li>');

			// Combine Elements
			aElem.appendTo(liElem);
			closeElem.appendTo(liElem);
			liElem.appendTo("#editor-tabs");

			// Add file to the array
			openFileList[self.currentFile] = tabCount;

			// Inscrement tab count
			tabCount++;
		}

		// Switch to tab
		showTab(self.currentFile, msg.body.object.content, msg.body.object.locks);

		// Switch syntax coloration mode
		changeAceSyntaxMode(msg.body.object.extension);

		msg.body.object.usernames.forEach(function(value) {
			colorsCounter = (colorsCounter + 1) % 10;
			window.editor.addForeignCursor(value, colors[colorsCounter]);
		});

		self.registerAceEvents();
	};

	/**
	 * Set the position of another user's cursor
	 */
	this.onSetForeignCursor = function(self, msg) {

		window.editor.setForeignCursorPosition(msg.body.object.username, {
			row : msg.body.object.row,
			column : msg.body.object.column
		});
	};

	/**
	 * Called when nother user locked a line 
	 */
	this.onUserLockedLine = function(self, msg) {

		annotations.push({
			row : msg.body.object.row,
			column : 0,
			text : "Line locked by " + msg.body.object.username,
			type : "lock_others",
			lint : 0
		});
		window.editor.getSession().setAnnotations(annotations);
	};
	
	/**
	 * Called when a line previously locked by another user is unlocked 
	 */
	this.onUserUnlockedLine = function(self, msg) {

		for ( var idx in annotations) {

			if (annotations[idx].row == msg.body.object.row) {
				annotations.splice(idx, 1);
			}
		}
		window.editor.getSession().setAnnotations(annotations);
	};
	
	/**
	 * Called when another user opened a file
	 */
	this.onUserOpenedFile = function(self, msg) {
		if (msg.body.object.filepath == self.currentFile) {
			msg.body.object.usernames.forEach(function(value) {
				colorsCounter = (colorsCounter + 1) % 10;
				window.editor.addForeignCursor(value, colors[colorsCounter]);
			});
		}
	};
	
	/**
	 * Called when another user closed a file
	 */
	this.onUserClosedFile = function(self, msg) {

		console.log("Removing cursor of user "+msg.body.object.usernames[0])
		window.editor.removeForeignCursor(msg.body.object.usernames[0]);
	};
	
	/**
	 * Changes have been made to the file which must be applied to the local copy
	 */
	this.onUserChangedFile = function(self, msg) {

		self.unregisterAceEvents();
		// Update lines
		for (var i=0; i<msg.body.object.lineChanges.length; i++) {
			var change = msg.body.object.lineChanges[i];
			if (change.isInsertOperation) {
				aceDoc.insertNewLine({row: change.row, column:0});
				
				if (lockedLine >= change.row) {
					console.log("AAA Changing lock from " + lockedLine + " to " + (lockedLine+1));
					lockedLine += 1;
				}
			} else {
				aceDoc.removeLines(change.row, change.row);
				
				if (lockedLine > change.row) {
					console.log("AAA Changing lock from " + lockedLine + " to " + (lockedLine-1));
					lockedLine -= 1;
				}
			}
		}
		
		window.editor.getSession().setAnnotations(annotations);
		
		// Apply patches

		var curPos = window.editor.getCursorPosition();

		for (line in msg.body.object.patches) {
			var line_patches = dmp.patch_fromText(msg.body.object.patches[line]);
			var oldLine = aceDoc.getLine(line);
			var newLine = dmp.patch_apply(line_patches, oldLine)[0];
			
			var Range = new require("ace/range").Range;
			aceDoc.replace(new Range(line, 0, line, oldLine.length), newLine);
		}

		window.editor.moveCursorTo(curPos.row, curPos.column);

		self.registerAceEvents();
	};

	/**
	 * Called when the cursor is moved
	 */
	var onUpdateCursor = function(e) {

		if (self.currentFile == null) {
			return;
		}

		var cursor = window.editor.getCursorPosition();

		if (lockedLine != -1 && lockedLine != cursor.row) {
			sendLinePatch();
			resetLock();
		}

		var data = {
			'filepath' : self.currentFile,
			'row' : cursor.row,
			'column' : cursor.column
		};

		cide.socket.sendMessage(new cideCore.net.NetworkMessage(self.name,
				"core.module.controller.EditorModule", "setCursor", data));
	};

	/**
	 * Reset the lock internally, and tell the server it's done
	 */
	var resetLock = function() {
		
		if(lockedLine == -1) {
			throw "Error: resetting lock which wasn't locked";
			
			return;
		}
		

		console.log("Unlocking line " + lockedLine);
		
		window.clearTimeout(lockTimeoutId);

		// Ask the server to unlock the line
		var data = {
				'filepath' : self.currentFile,
				'row' : lockedLine,
			};		

		cide.socket.sendMessage(new cideCore.net.NetworkMessage(self.name,
				"core.module.controller.EditorModule", "unlockLine",
				data));
		
		//Remove lock icon
		for ( var idx in annotations) {

			if (annotations[idx].row == lockedLine) {
				annotations.splice(idx, 1);
			}
		}
		window.editor.getSession().setAnnotations(annotations);
				
		//Unlock the line
		lockedLine = -1;
	};
		
	/**
	 * Send a patch of changes on the current line to the server
	 */
	var sendLinePatch = function() {
		window.clearTimeout(lockTimeoutId);

		var patches = dmp.patch_make(lineBuffer, aceDoc.getLine(lockedLine));

		var data = {
			'filepath' : self.currentFile,
			'patches' : dmp.patch_toText(patches),
			'row' : lockedLine
		};
		// Sending the patch
		cide.socket.sendMessage(new cideCore.net.NetworkMessage(self.name,
				"core.module.controller.EditorModule", "applyLinePatch", data));
		
		lineBuffer = null;
		lineDeltas = [];
	};
	
	/**
	 * Spawn the timeout to send a new line patch and to unlock the
	 * line
	 */
	var spawnTimeout = function() {
		window.clearTimeout(lockTimeoutId);
		spawnTimeoutCount += 1;
		
		if (spawnTimeoutCount > 30) {
			spawnTimeoutCount = 0;
			sendLinePatch();
		}
		
		lockTimeoutId = window.setTimeout(function() {
			spawnTimeoutCount = 0;
			sendLinePatch();
			resetLock();
		}, 200);
	};

	/**
	 * Called when a lock error has occurred
	 */
	var onLockError = function(msg) {

		window.clearTimeout(lockTimeoutId);

		self.unregisterAceEvents();

		var curPos = window.editor.getCursorPosition();
		aceDoc.revertDeltas(lineDeltas);
		window.editor.moveCursorTo(curPos.row, curPos.column);

		self.registerAceEvents();
		
		resetLock();
	};

	/**
	 * Get the state of the row before the event e occured
	 */
	var getLineBeforeChange = function(e, row) {

		self.unregisterAceEvents();

		var curPos = window.editor.getCursorPosition();

		aceDoc.revertDeltas([ e.data ]);
		var line = aceDoc.getLine(row);
		aceDoc.applyDeltas([ e.data ]);

		window.editor.moveCursorTo(curPos.row, curPos.column);

		self.registerAceEvents();

		return line;
	};

	/**
	 * Called when text is modified
	 */
	var onUpdateText = function(e) {
		
		window.clearTimeout(lockTimeoutId);
		
		var currentLine = window.editor.getCursorPosition().row;

		if (e.data.action == "insertText") {

			if (e.data.text.indexOf("\n") != -1) {

				// If the previous line is not locked, we need to force a lock
				// before applying any changes to it
				if (lockedLine != currentLine) {

					lockLine(e.data.range.start.row);
					lineBuffer = getLineBeforeChange(e, lockedLine);
					lineDeltas = [];
				}
				
				sendLinePatch();
				resetLock();
				
				// Manually lock the second line
				currentLine = e.data.range.end.row;
				lockLine(currentLine);
				lineBuffer = "";

				var data = {
					'filepath' : self.currentFile,
					'row' : currentLine

				};
				cide.socket.sendMessage(new cideCore.net.NetworkMessage(
						self.name, "core.module.controller.EditorModule",
						"insertLine", data));


				spawnTimeout();

			} else { // Normal text insertion
				//If another line is locked, send the changes to the server and unlock the line
				if (lockedLine != -1 && lockedLine != currentLine) {

					sendLinePatch();
					resetLock();
				}

				if (lockedLine == -1) {

					lockLine(currentLine);
					lineBuffer = getLineBeforeChange(e, lockedLine);
					lineDeltas = [];
				}

				// Keeping the deltas computed by Ace in order to be able to revert it
				lineDeltas.push(e.data);
				
				spawnTimeout();
			}

		} else if (e.data.action == "removeText") {

			// If we removed a line
			if (e.data.text.indexOf("\n") != -1) {

				currentLine = e.data.range.end.row;
				
				if (lockedLine != -1 && lockedLine != currentLine) {

					sendLinePatch();
					resetLock();
				}
				
				console.log(e.data);
				
				if (lockedLine == -1) {
					lockLine(currentLine);
				}

				// Remove the first line
				var data = {
					'filepath' : self.currentFile,
					'row' : e.data.range.end.row

				};
				
				cide.socket.sendMessage(new cideCore.net.NetworkMessage(
						self.name, "core.module.controller.EditorModule",
						"removeLine", data));
				
				//Remove lock
				resetLock();

				// Manually locking the second line
				currentLine = e.data.range.start.row;
				lockLine(currentLine);
				lineBuffer = getLineBeforeChange(e, lockedLine);
				lineDeltas = [];

				spawnTimeout();

			} else {

				if (lockedLine != -1 && lockedLine != currentLine) {

					sendLinePatch();
					resetLock();
				}

				if (lockedLine == -1) {

					lockLine(currentLine);
					lineBuffer = getLineBeforeChange(e, lockedLine);
					lineDeltas = [];
				}
				
				// Keeping the deltas computed by Ace in order to be able to revert it
				lineDeltas.push(e.data);

				spawnTimeout();
			}

		} else if (e.data.action == "removeLines") {
			
			// Send line changes for the previously locked line if any
			if (lockedLine != -1) {
				sendLinePatch();
				resetLock();
			}
			
			console.log(e.data);
			
			for (var line = e.data.range.start.row; line < e.data.range.end.row; line++) {
				lockLine(line);
				
				console.log("XXXXX Removing line " + line);

				// Remove the first line
				var data = {
					'filepath' : self.currentFile,
					'row' : line
				};
				cide.socket.sendMessage(new cideCore.net.NetworkMessage(
						self.name, "core.module.controller.EditorModule",
						"removeLine", data));

				resetLock();
			}
		}

	};

	var lockLine = function(row) {
		if (lockedLine != -1) {
			throw "Locking a second line !! locked: " + lockedLine + " locking: " + row;
		}
		
		console.log("Locking line " + row);
		
		lockedLine = row;

		annotations.push({
			row : row,
			column : 0,
			text : "Line locked!",
			type : "lock",
			lint : 0
		});
		window.editor.getSession().setAnnotations(annotations);

		var lineToLock = {
			'filepath' : self.currentFile,
			'row' : row
		};

		// Asking the server to lock the line
		cide.socket.sendMessage(new cideCore.net.NetworkMessage(self.name,
				"core.module.controller.EditorModule", "lockLine", lineToLock));
	};

	this.onUnload = function() {

	};

	var changeAceSyntaxMode = function(extension) {

		var AceMode = {};

		switch (extension) {

		case "css":
			AceMode = require("ace/mode/css").Mode;
			break;

		case "html":
			AceMode = require("ace/mode/html").Mode;
			break;

		case "js":
			AceMode = require("ace/mode/javascript").Mode;
			break;

		case "php":
			AceMode = require("ace/mode/php").Mode;
			break;

		case "json":
			AceMode = require("ace/mode/json").Mode;
			break;

		case "py":
			AceMode = require("ace/mode/python").Mode;
			break;

		case "rb":
			AceMode = require("ace/mode/ruby").Mode;
			break;

		case "xml":
			AceMode = require("ace/mode/xml").Mode;
			break;

		case "java":
			AceMode = require("ace/mode/java").Mode;
			break;

		case "c":
			AceMode = require("ace/mode/c_cpp").Mode;
			break;

		default:
			AceMode = require("ace/mode/text").Mode;
			console.log("Unsupported extension! (." + extension + ")");
		}

		// Change mode
		window.editor.getSession().setMode(new AceMode());
	};

	/**
	 * Switch to a given tab
	 */
	var showTab = function(filePath, content, locks) {

		if (openFileList[filePath] === undefined) {
			return;
		}

		$("#editor-tabs").children().removeClass("current");

		$('#editor-tabs-' + openFileList[filePath]).addClass("current");

		//Update the displayed text
		window.editor.getSession().setValue("");
		window.editor.getSession().doc.insertLines(0, content);
		
		//Update annotations
		annotations = [];
		
		for (user in locks) {
			for (var i=0; i<locks[user]; i++) {

				annotations.push({
					row : locks[user][i],
					column : 0,
					text : "Line locked by " + user,
					type : "lock_others",
					lint : 0
				});
			}
		}

		window.editor.getSession().setAnnotations(annotations);

		// Remove the empty line created by Ace on startup (duh!)
		window.editor.moveCursorTo(0, 0);
	};

	/**
	 * Display the given file, retrieving it from the server if necessary
	 */
	this.openFile = function(filePath) {

		// Check if we have too many files opened
		if (openFileList.length >= 4) {

			// TODO: cide.widgets["error"].echo("Too many files opened!");
			alert("Too many files opened!");

		} else {
			cide.socket.sendMessage(new cideCore.net.NetworkMessage("editor",
					"core.module.controller.EditorModule", "getSourceFile",
					filePath));
		}
	};

	/**
	 * Close a file and cleanup
	 */
	this.closeFile = function(file) {
		alert(file + " close");
	};

	/**
	 * Undo a change
	 */
	this.undo = function() {
		window.editor.undo();
	};

	/**
	 * Redo a change
	 */
	this.redo = function() {
		window.editor.redo();
	};

	/**
	 * Copy selection to local buffer
	 */
	this.copyText = function() {
		window.editor.onCopy();
	};

	/**
	 * Paste from local buffer
	 */
	this.pasteText = function() {
		window.editor.getCopyText();
	};

	/**
	 * Cut selection to local buffer
	 */
	this.cutText = function() {
		window.editor.onCut();
	};

	/**
	 * TODO
	 */
	this.findText = function() {
		var text = prompt("Rechercher : ", "");
		window.editor.find(text, {});
	};
	
	this.deleteFile = function(args) {
		
		if(args == null) {
			args = self.currentFile;
		}
		cide.socket.sendMessage(new cideCore.net.NetworkMessage(self.name, "core.module.controller.EditorModule",
				"deleteFile", args));
	}
};

cide.manager.registerWidget(new cideWidgets.Editor());
