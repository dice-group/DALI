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

            var results = data,
                html = "",
                i = 0, len = results.length;

            // convert relevance to % & sort descending
            for(i = 0; i < len; i++){
                results[i].relevance = Math.floor( parseFloat(results[i].relevance) * 100.0 );
            }
            results.sort(function(a,b) { return b.relevance - a.relevance; });

            // render
            for(i = 0; i < len; i++){
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
