/**
 * Menu widget
 * @mandatory
 * @author: Gabriel Féron
 */

$('document').ready(function(){
    
    // Initialize the menu 
    $("#widget-menu-header").dropdown();
       
    /**
     * Undo button callback
     */
    $("#undo-btn").click(function(){
        
       window.editor.undo();
    });
    
    /**
     * Redo button callback
     */
    $("#redo-btn").click(function(){
        
       window.editor.redo();
    });
    
    /**
     * Cut button callback
     */
    $("#cut-btn").click(function(){
        
        alert('Cut, copy and paste will probably not be implemented!');
    });
    
    /**
     * Copy button callback
     */
    $("#copy-btn").click(function(){
        
        alert('Cut, copy and paste will probably not be implemented!');
    });
    
    /**
     * Paste button callback
     */
    $("#paste-btn").click(function(){
        
        alert('Cut, copy and paste will probably not be implemented!');
    });
});