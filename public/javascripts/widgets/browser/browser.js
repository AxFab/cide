// jQuery File Tree Plugin
// 
// Heavily modified for the CIDE Project
// Version X.XX
//
// Cory S.N. LaViska
// A Beautiful Site (http://abeautifulsite.net/)
// 24 March 2008
//
// Gabriel Féron
// January 2012

/**
 * Browser widget
 */
cideWidgets.Browser = function() {

	this.name = "browser";
	this.currentNode = {};

	// Default settings
	this.o = {};
	this.o.root = '/';
	this.o.script = 'file';
	this.o.folderEvent = 'click';
	this.o.expandSpeed = 500;
	this.o.collapseSpeed = 500;
	this.o.expandEasing = null;
	this.o.collapseEasing = null;
	this.o.multiFolder = true;
	this.o.loadMessage = 'Loading...';

	this.requestTree = function(c, t) {

		this.currentNode = c;

		console.log("Requesting directory: " + t);

		cide.socket.sendMessage(new cideCore.net.NetworkMessage(this.name,
				"core.module.controller.ProjectModule", "getFileTree", t));
	}

	this.refreshTree = function(files, c, t) {

		// We need to build the DOM for the file-tree
		var list = $('<ul />').attr('class', 'jqueryFileTree').attr('style', 'display: none;');

		// Loop through the elements received
		for ( var idx = 0; idx < files.length; idx++) {

			var file = files[idx];

			var className = {};
			if (file.isDirectory) {
				className = "directory collapsed";
			} else {
				className = "file ext_" + file.extension;
			}

			var listElement = $('<li />').attr('class', className);

			var slashPosition = file.filepath.lastIndexOf("/");
			var filename = {};
			if (slashPosition != -1) {
				filename = file.filepath.substr(slashPosition + 1, file.filepath.length);
			} else {
				filename = file.filepath;
			}
			var listLink = $('<a />').attr('href', '#').attr('rel', file.filepath).text(filename);

			listElement.append(listLink);
			list.append(listElement);
		}

		this.showTree(list, c, t);
	}

	this.showTree = function(newTree, c, t) {

		$(c).find('.start').html('');
		$(c).append(newTree);
		if (this.o.root == t)
			$(c).find('UL:hidden').show();
		else
			$(c).find('UL:hidden').slideDown({
				duration : this.o.expandSpeed,
				easing : this.o.expandEasing
			});
		this.bindTree(this, c);
	};

	this.bindTree = function(self, t) {

		$(t).find('LI A').bind(this.o.folderEvent, function() {
			if ($(this).parent().hasClass('directory')) {
				if ($(this).parent().hasClass('collapsed')) {
					// Expand
					if (!self.o.multiFolder) {
						$(this).parent().parent().find('UL').slideUp({
							duration : self.o.collapseSpeed,
							easing : self.o.collapseEasing
						});
						$(this).parent().parent().find('LI.directory').removeClass('expanded').addClass('collapsed');
					}
					$(this).parent().find('UL').remove(); // cleanup

					self.requestTree($(this).parent(), $(this).attr('rel'));

					$(this).parent().removeClass('collapsed').addClass('expanded');
				} else {
					// Collapse
					$(this).parent().find('UL').slideUp({
						duration : self.o.collapseSpeed,
						easing : self.o.collapseEasing
					});
					$(this).parent().removeClass('expanded').addClass('collapsed');
				}
			} else {
				self.selectFile($(this).attr('rel'));
			}
			return false;
		});
		// Prevent A from triggering the # on non-click events
		if (this.o.folderEvent.toLowerCase != 'click')
			$(t).find('LI A').bind('click', function() {
				return false;
			});
	};

	/**
	 * Callback used when a normal file is clicked
	 */
	this.selectFile = function(filepath) {

		cide.widgets["editor"].openFile(filepath);
	}

	this.onLoad = function() {

		cide.socket.registerEvent("projectFileTree", this, function(self, msg) {

			self.refreshTree(msg.body.object, self.currentNode, self.path);
		});
		
		cide.socket.registerEvent("refreshFileTree", this, function(self, msg) {

			this.currentNode = $("#widget-browser");
			$(this.currentNode).html('<ul class="jqueryFileTree start"><li class="wait">' + self.o.loadMessage + '<li></ul>');
			self.refreshTree(msg.body.object, self.currentNode, self.path);
		});

		// Get the initial file list
		setTimeout(this.resetTree, 100, this);
	};
	
	this.resetTree = function(self) {

		self.currentNode = $("#widget-browser");
		$(this.currentNode).html('<ul class="jqueryFileTree start"><li class="wait">' + self.o.loadMessage + '<li></ul>');
		self.requestTree(self.currentNode, "/");
		
		$.contextMenu({
		    selector: '.file', 
		    callback: function(key, options) {
		        if (key === "delete")
		        {
		        	var a = $(this).children().first();
		        	
		        	if (confirm("Are you sure you want to delete this file ?"))
		        	{
		        		cide.widgets["editor"].deleteFile($(a).attr('rel'));
		        	}
		        }
		    },
		    items: {
		        "delete": {name: "Delete", icon: "delete"}
		    }
		});
		
		$.contextMenu({
		    selector: '.directory', 
		    callback: function(key, options) {
		        if (key === "delete")
		        {
		        	var a = $(this).children().first();
		        	
		        	if (confirm("Are you sure you want to delete this folder and its content ?"))
		        	{
		        		cide.widgets["editor"].deleteFile($(a).attr('rel'));
		        	}
		        }
		    },
		    items: {
		        "delete": {name: "Delete", icon: "delete"}
		    }
		});
	};

	this.unLoad = function() {

	};
};

cide.manager.registerWidget(new cideWidgets.Browser());
