$('document').ready( function() {

    $(function () {
        $('.ui-widget-ace-tabs').tabs()
    });

    window.editor = ace.edit("editor");
       
    window.editor.setTheme("ace/theme/textmate");
    
    //var JavaScriptMode = require("ace/mode/javascript").Mode;
    //editor.getSession().setMode(new JavaScriptMode());
    
    $('#editor').focus(function() {
        
       window.console.disable(); 
    });

});