function requestPermission(url) {
    //TODO:open up the navigator for selection
    var selectedNodes = getSelectedNodes($("#requestPermission"));
    if(selectedNodes.length === 0 ) {
        alert("Please select atleast one node");
        return;
    }
    $.ajax({
        url:url,
        dataType:"json",
        type:"POST",
        data:{'selectedNodes' = selectedNodes.join(",")}
        success : function(data){
            if(data.status == 'success') {
            
            }
            else {
            
            }
        },
        error: function(xhr, ajaxOptions, error) {
            var successHandler = this.success, errorHandler = showUpdateStatus;
            handleError(xhr, ajaxOptions, error, successHandler, errorHandler);
        }
    });
}
