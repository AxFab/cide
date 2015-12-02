/*
 * Class : LayoutManager
 */

function LayoutManager () {
     
};

LayoutManager.prototype.addWidget = function (widgetName, layoutName) {
        var layout = $("#layout-" + layoutName);
        var widget = $("#widget-" + widgetName + "-" + layoutName);
        
        //If the layout exists and the widget doesn't exist
        if (layout && widget.length < 1) {
            $.ajax({
                url : "widget/" + widgetName + "/" + layoutName,
                type : "GET",
                success : function(result) {
                    
                    // Adding template file
                    $(layout).append(result);
                }
            });
        }
    }
    
LayoutManager.prototype.removeWidget = function (widgetName, layoutName) {
        $("#widget-" + widgetName + "-" + layoutName).remove();
    }
    
LayoutManager.prototype.removeAllWidget = function () {
        $(".widget").each(function(){
            $(this).remove();
        });
    }