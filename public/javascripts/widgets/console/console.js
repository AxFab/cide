/**
 * CIDE - Console Widget
 */

$('document').ready(function(){

   $('.ui-widget-console-tabs').tabs();
});
  
$('.ui-widget-console-tabs').bind('change', function (e) {
    //e.target // activated tab
    //e.relatedTarget // previous tab
});

$(document).ready(function($) {
    
    $(function($, undefined) {
        window.console = $('#widget-console').terminal(function(command, term) {
            if (command !== '') {
                var result = window.eval("(" + command + ")");
                if (result !== undefined) {
                    term.echo(String(result));
                }
            } else {
               term.echo('');
            }

        }, {
            greetings: 'CIDE Interpreter\nType \'console.disable()\' to end the focus.',
            name: 'widget-console',
            height: 160,
            prompt: 'cide>',
            enabled: false});
    });
});