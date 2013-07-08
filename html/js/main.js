requirejs.config({
    paths: {
        jquery: '../libs/jquery.min',
        bootstrap: '../libs/bootstrap.min',
        dot: '../libs/doT.min',
        spin: '../libs/spin.min'
    },
    shim: {
        'bootstrap': { deps: ['jquery'] }
    }
});

require(['jquery', 'bootstrap', 'dot', 'spinner'], function(_$, _bootstrap, doT, $) {
    //var queryEndpoint = "http://localhost:5555/getdatasets",
    var queryEndpoint = "http://139.18.2.164:5555/getdatasets",
        // cache dom pointers
        $goBtn = $("#goBtn"),
        $searchInput = $("#searchInput"),
        $resultContainer = $("#resultContainer"),
        // spinner
        progressSpinner = $("#searchProgress"),
        // tempaltes
        resultTemplate = doT.template( $('#resultTemplate').html() ),

    // functions
    doSearch = function(){
        var query = $searchInput.val();
        $searchInput.val('');

        // show spinner
        progressSpinner.parent().show();

        // do request
        $.getJSON(queryEndpoint+"?endpoint="+encodeURIComponent(query), function(data){
            // hide spinner
            progressSpinner.parent().hide();

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

    // init spinner
    progressSpinner.spin();
});
