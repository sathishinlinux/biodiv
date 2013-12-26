${"==============================uyerfwseirfwse"}

<r:script>
$(document).ready(function(){

$(function () {
console.log("here 1")
$("#demo2").jstree({ 
"core" : { "initially_open" : [ "root" ] },
"html_data" : {
"data" : "<li id='root'><a href='#'>Root node</a><ul><li><a href='#'>Child node</a></li></ul></li><li id = 'new_node'><a href='#'> Node 1</a><ul><li>child node 1</li></ul></li>  "
},
"plugins" : [ "themes", "html_data" ]
});
});




$(function () {
$("#demo3").jstree({ 
"html_data" : {
"ajax" : {
"url" : "http://indiabiodiversity.localhost.org/action/sendData",
"data" : function (n) { 
return { id : n.attr ? n.attr("id") : 0 }; 
}
}
},
"plugins" : [ "themes", "html_data" ]
});
});
});
</r:script>
<div id = demo2>${"================ertgergergergergergergerg"}</div>

<div id = demo3></div>

