/*
  CIDE - Console Widget
 */

cideWidgets.Console = function() {

	this.name = "console";
	
	this.term = {};
	var self = this;

	this.onLoad = function() {

		// Initiate the console tabs
		$('.ui-widget-console-tabs').tabs();

		self.term = $('#widget-console')
				.terminal(
						function(command, term) {

							if (command == 'disable') {
								
								term.disable();

							} else if (command == 'help') {
								
								term.echo("You can switch between bash mode (server-side) and JS mode (client-side)");

								term.echo("Client-side commands must be prefixed with 'cide.'");
								term.echo("Here is the list of the available client-side commands:\n");
								
								term.echo("-- File management --");
								term.echo("refreshTree");
								term.echo("newFile");
								term.echo("newFolder");
								term.echo("deleteOpenedFile");
								term.echo("\n");
								
								term.echo("-- Editor --");
								term.echo("editor.undo");
								term.echo("editor.redo");
								term.echo("\n");
								//term.echo("editor.find");
								//term.echo("editor.replace");
								
								term.echo("-- Compilation --");
								term.echo("buildProject");
								term.echo("cleanProject");
								term.echo("runProject");
								term.echo("\n");
								
								/**
								 * Switch to the server terminal
								 */
							} else if(command.indexOf("cide") == 0) {
								
								/*
								 * 
								 */
								
								switch(command) {
								
								case "cide.resetTree":
									var browser = cide.widgets["browser"];
									browser.resetTree(browser);
									break;
									
								case "cide.newFile":
									var menu = cide.widgets["menu"];
									menu.showDialog("new-file", menu.onNewFileLoad, menu.onNewFileClose);
									break;
									
								case "cide.newFolder":
									var menu = cide.widgets["menu"];
									menu.showDialog("new-folder", menu.onNewFolderLoad, menu.onNewFolderClose);
									break;
									
								case "cide.deleteOpenedFile":
									cide.widgets["editor"].deleteFile();
									break;	
									
								case "editor.undo":
									window.editor.undo();
									break;
									
								case "editor.redo":
									window.editor.redo();
									break;
									
								case "cide.buildProject":
									cide.widgets["console"].buildProject();
									break;
									
								case "cide.cleanProject":
									cide.widgets["console"].cleanProject();
									break;
									
								case "cide.runProject":
									cide.widgets["console"].runProject();
									break;
									
								default:
									term.echo("Invalid command!");
								}
								
							} else if (command == 'bash') {
								
								cide.socket
								.sendMessage(new cideCore.net.NetworkMessage(
										"console",
										"core.module.controller.ProcessIOModule",
										"bash",
										command));

								// Ask the server to create a chroot
								/*
								 * cide.socket .sendMessage(new
								 * cideCore.net.NetworkMessage( "console",
								 * "core.module.controller.ProcessIOModule",
								 * "createChroot", command));
								 */
								
								term
										.push(
												function(command, term) {

													if (command == 'help') {
														term
																.echo('This shell is used to send direct commands which will be executed on the server.');

													} else if(command.substr(0, 3) == 'man') {
													
														window.open('http://man.yolinux.com/cgi-bin/man2html?cgi_command='+command.substr(4, command.length));
														
													} else if (command.length > 0) {
													

														// Pause the Terminal,
														// will be resumed once
														// stdin comes
														term.pause();

														// var cmd =
														// command.addSlashes();
														console.log(command);

														cide.socket
																.sendMessage(new cideCore.net.NetworkMessage(
																		"console",
																		"core.module.controller.ProcessIOModule",
																		"stdin",
																		command));
													}

												}, {
													prompt : '[[;#01DF01;]bash-3.2$]',
													name : 'server'
												});

								/**
								 * Switch to the client terminal
								 */
							} else {

								var result = window.eval(command);
								if (result != undefined) {
									term.echo(String(result));
								}
								// term.echo("Erreur!");
								// }
							}
						},
						{
							greetings : '[[;#013ADF;]Welcome to the CIDE Dashboard].\n[[;#0040FF;]Type \'help\' to get some info on how to use the terminal.]',
							height : 220,
							prompt : '[[;#FF4000;]cide$]',
							enabled : false,
							name : 'cide'
						}

				);
	

		// Register to events
		cide.socket.registerEvent("consoleStdout", this, function(self, msg) {
			
			if(msg.body.object === undefined) {
				return;
			}
			self.term.echo(msg.body.object);
		});
		
		cide.socket.registerEvent("consoleStderr", this, function(self, msg) {
			
			if(msg.body.object === undefined) {
				return;
			}
			self.term.error(msg.body.object);
		});
		
		cide.socket.registerEvent("consoleResume", this, function(self, msg) {

			if(msg.body.object === undefined) {
				return;
			}
			self.term.resume();
			self.term.echo(msg.body.object);
		});
		
		cide.socket.registerEvent("sessionClosed", this, function(self, msg) {
			
			self.term.pop();
		});
	};

	this.onUnload = function() {

	};
	
	this.buildProject = function() {
		
		this.term.clear();
		this.term.echo("[[;#013ADF;]Launching project build...]");
		cide.manager.showBottomLayout();
		
		cide.socket
		.sendMessage(new cideCore.net.NetworkMessage(
				"console",
				"core.module.controller.ProcessIOModule",
				"build", "build"));
	}
	
	this.cleanProject = function() {
		
		this.term.clear();
		this.term.echo("[[;#013ADF;]Cleaning project output...]");
		cide.manager.showBottomLayout();
		
		cide.socket
		.sendMessage(new cideCore.net.NetworkMessage(
				"console",
				"core.module.controller.ProcessIOModule",
				"build",
				"clean"));
	}
	
	this.runProject = function() {
		
		this.buildProject();
		
		this.term.clear();
		this.term.echo("[[;#013ADF;]Running project output...]");
		cide.manager.showBottomLayout();
		
		cide.socket
		.sendMessage(new cideCore.net.NetworkMessage(
				"console",
				"core.module.controller.ProcessIOModule",
				"build",
				"run"));
	}
};

cide.manager.registerWidget(new cideWidgets.Console());
