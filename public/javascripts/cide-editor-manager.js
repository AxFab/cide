


function EditorManager() {
	this.constCheckSaving = 750;
	this.constCheckUpdate = 2000;
	this.lineChanged = null;
	this.change = false;
	this.lastChange;
};

// Register a change into a line.
EditorManager.prototype.changeLine = function (line) {
	console.log ("Add ligne : '"+line+"'");
	this.lineChanged = line;
	// this.lastChange = new Date().getTime();
}

EditorManager.prototype.newLine = function (line) {
	console.log ("New ligne : '"+line+"'");
	
}

function updateEditor () {
	if (CIDE.editorManager.lineChanged != null) {
		var str = Cide.Editor.getSession().doc.getTextLine (CIDE.editorManager.lineChanged);
		console.log ("We commit the line [" + CIDE.editorManager.lineChanged + "]: "+ str);
		var msg = {};
		msg.message = "addChangeToLine";		
		var data = {};
		data.line = CIDE.editorManager.lineChanged;
		data.text = str;
		data.cursor = 0;
		msg.object = JSON.stringify(data);
		CIDE.server.sendMsg (CIDE.server.getHeader("aceEditor", "Cide.EditorManager"), msg);
		CIDE.editorManager.lineChanged = null;
	}
}

function savingEditor () {
	updateEditor ();
	setTimeout(savingEditor, CIDE.editorManager.constCheckSaving);
}

EditorManager.prototype.activeSave = function () {
	savingEditor ();
}
