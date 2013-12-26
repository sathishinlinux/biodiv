<html>
    <head>
        <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
        <script src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.10.1/jquery-ui.js"></script>

        <script type="text/javascript" src="/js/jstree-v.pre1.0/jquery.jstree.js"></script>
    </head>

    <body>
        <script>
           
            /* 
            
            $(function () {
            $("#demo2").bind('loaded.jstree', function(){
            }).jstree({ 
            "core" : { "initially_open" : [ "root" ] },
            "html_data" : {
            "data" : "<li id=60816><a href='#'><input type='checkbox'>Kingdom</a>
            <ul>
                <li id=60817><a href='#'>Phylum</a>
                <ul>
                    <li id = 60818><a href='#'>Family</a>
                    <ul>
                        <li id = 60819><a href='#'>Mango</a></li>
                    </ul>
                    </li>
                    <li id = 60822><a href='#'>Fox</a></li>
                </ul>
                </li>
            </ul>
            </li>},
            "plugins" : [ "themes", "html_data" ]
            });
            });

            */


            /*
            <li id=60816><a href='#'>hello</a>
            <ul>
                <li><a href='#'>Child hello node</a>
                <ul>
                    <li>erweweweews</li>
                </ul>
                </li>
            </ul>
            </li>
            */


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
            

            function getChecked(){
                alert( $("[type=checkbox]:checked").size());
            }
            
            function getAllChild(){
            var nId =  $("[type=checkbox]:checked").closest("li").attr("id")
            console.log(nId);
                $.ajax({
                url: "http://indiabiodiversity.localhost.org/species/getInnerNodesId",
                type: 'POST',
                dataType: "json",
                data:{'nodeId':nId},
                success: function(data) {

                }     
            });
            }

        </script>
        <div id = demo2></div>
        <div id = demo3></div>
        <button onClick="getChecked()">Details</button>
        <button onClick="getAllChild()">Get All Child</button>
    </body>
</html>
