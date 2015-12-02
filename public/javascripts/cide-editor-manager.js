


function EditorManager() {
	this.constCheckSaving = 750;
	this.constCheckUpdate = 2000;
	this.lineChanged = new Array();
	this.change = false;
	this.lastChange;
};

// Register a change into a line.
EditorManager.prototype.changeLine = function (line) {
	console.log ("Add ligne : '"+line+"'");
	this.lineChanged["ln" + line] = line;
	this.change = true;
	this.lastChange = new Date().getTime();
}

function updateEditor () {
	console.log ("Commit [" + Cide.EditorManager.lineChanged.length + "]");
	for(i=0;i<Cide.EditorManager.lineChanged.length;i++) {
		console.log ("Commit line [" + Cide.EditorManager.lineChanged[i] + "]");
	}
}

function savingEditor () {
	updateEditor ();
	setTimeout(savingEditor, Cide.EditorManager.constCheckSaving);
}

EditorManager.prototype.activeSave = function () {
	savingEditor ();
}
