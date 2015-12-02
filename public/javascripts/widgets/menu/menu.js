/**
 * Menu widget
 * 
 * @author: Gabriel Féron
 */

cideWidgets.Menu = function() {

	this.name = "menu";

	var self = this;

	this.onLoad = function() {

		/**
		 * Update status on socket open
		 */
		cide.socket.onSocketOpen(function() {
			$("#widget-menu-status").css("color", "#0069D6");
		});

		/**
		 * Update status on socket close
		 */
		cide.socket.onSocketClose(function() {
			$("#widget-menu-status").css("color", "#f00");
		});

		// Initiate the dropdown menu
		$("#widget-menu").dropdown();

		/**
		 * Event for the buttons
		 */

		$('#new-folder-btn').click(
				function() {
					self.showDialog("new-folder", self.onNewFolderLoad,
							self.onNewFolderClose);
				});

		$('#new-file-btn').click(
				function() {
					self.showDialog("new-file", self.onNewFileLoad,
							self.onNewFileClose);
				});

		$('#delete-file-btn').click(function() {
			cide.widgets["editor"].deleteFile();
		});

		$('#editor-style-btn').click(
				function() {
					self.showDialog("editor-style", self.onEditorStyleLoad,
							self.onEditorStyleClose);
				});

		$('#project-properties-btn').click(function() {
			self.showDialog("project-properties");
		});

		$("#undo-btn").click(function() {
			cide.widgets["editor"].undo();
		});

		$("#redo-btn").click(function() {
			cide.widgets["editor"].redo();
		});

		$("#cut-btn").click(function() {
			cide.widgets["editor"].cutText();
		});

		$("#copy-btn").click(function() {
			cide.widgets["editor"].copyText();
		});

		$("#paste-btn").click(function() {
			cide.widgets["editor"].pasteText();
		});

		$("#find-btn").click(function() {
			cide.widgets["editor"].findText();
		});

		$("#run-btn").click(function() {
			cide.widgets["console"].runProject();
		});

		$("#build-btn").click(function() {
			cide.widgets["console"].buildProject();
		});

		$("#clean-btn").click(function() {
			cide.widgets["console"].cleanProject();
		});

	};

	this.onUnload = function() {

	};

	this.showDialog = function(dialogId, onLoad, onClose) {

		$.ajax({
			url : "/dialog/" + dialogId,
			success : function(data) {

				// Unbind previous dialog
				$('#modal-ajax').unbind();
				$('#modal-ajax').remove();

				// Insert data in the DOM
				$('#layout-modals').append(data);

				// Load the dialog
				// onLoad();

				// Disable the terminal
				cide.widgets["console"].term.disable();

				// Display the modal
				$('#modal-ajax').modal({
					backdrop : true,
					show : true
				});

				$('#modal-ajax .btn.primary').click(function(event) {
					$('#modal-ajax').modal('hide');
					onClose();
				});
			}
		});
	};

	this.onNewFileLoad = function() {

		console.log("New file!");
	}

	this.onNewFolderLoad = function() {

	}

	this.onEditorStyleLoad = function() {

	}

	this.onNewFileClose = function() {

		var fileName = $("#modalInputFilename").val();
		console.log(fileName);

		cide.socket.sendMessage(new cideCore.net.NetworkMessage(self.name,
				"core.module.controller.ProjectModule", "newFile", fileName));
	}

	this.onNewFolderClose = function() {

		var folderName = $("#modalInputFilename").val();
		console.log(folderName);

		cide.socket
				.sendMessage(new cideCore.net.NetworkMessage(self.name,
						"core.module.controller.ProjectModule", "newFolder",
						folderName));
	}

	this.onEditorStyleClose = function() {

		var aceStyle = $("#modalEditorStyleSelect option:selected").val();

		window.editor.setTheme('ace/theme/' + aceStyle);

		var fontSize = $("#modalFontSizeSelect option:selected").val();
		window.editor.setFontSize(fontSize);

		// var codeFolding = $("#modalCodeFoldingSelect option:selected").val();
		// window.editor.session.setFoldStyle(codeFolding);

		window.editor.getSession().setUseSoftTabs(true);

		var keyBindings = $("#modalKeybindingsSelect option:selected").val();
		window.editor.setKeyboardHandler(keyBindings);
	}
};

cide.manager.registerWidget(new cideWidgets.Menu());
