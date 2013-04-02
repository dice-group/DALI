requirejs.config({
    paths: {
        jquery: ['../libs/jquery.min'],
        bootstrap: '../libs/bootstrap.min',
        dot: '../libs/doT.min',
    },
    shim: {
        'bootstrap':{deps: ['jquery']}
    }
});

require(['jquery', 'bootstrap', 'dot'], function($, _bootstrap, doT) {
    var queryEndpoint = "result",
        // cache dom pointers
        $goBtn = $("#goBtn"),
        $searchInput = $("#searchInput"),
        $resultContainer = $("#resultContainer"),
        // tempaltes
        resultTemplate = doT.template( $('#resultTemplate').html() ),

    // functions
    doSearch = function(){
        var query = $searchInput.val();
        $searchInput.val('');

        $.getJSON(queryEndpoint+"?endpoint="+encodeURIComponent(query), function(data){
            console.log(data);

            var results = [],
                html = "",
                i = 0,
                item, len,
                key, rel;

            for(key in data){
                rel = Math.floor( parseFloat(data[key]) * 100.0 );
                item = {
                    url: key,
                    relevance: rel,
                    type: 'file'
                };
                if( i % 2 === 0 ){
                    item.type = 'sparql';
                }
                i++;
                results.push(item);
            }
            results.sort(function(a,b) { return b.relevance - a.relevance; });

            for(i = 0, len = results.length; i < len; i++){
                html += resultTemplate(results[i]);
            }

            $resultContainer.html(html);
        });
    };

    $goBtn.on('click', doSearch);
    $searchInput.on('keyup', function(e){
        if(e.keyCode === 13)
            doSearch();
    });
});
