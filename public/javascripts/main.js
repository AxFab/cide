/**
 * CIDE - JS Init
 */

$('document').ready(function(){

	// Init of the layout manager
	window.layoutManager = new LayoutManager();

	// Adding Widgets
	window.layoutManager.addWidget("menu", "header");
	window.layoutManager.addWidget("browser", "left");
	window.layoutManager.addWidget("ace", "center");
	window.layoutManager.addWidget("console", "bottom");

});
