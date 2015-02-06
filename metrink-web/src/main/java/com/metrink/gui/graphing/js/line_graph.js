/*
 * Data passed into this function should be of the form:
 * 
 * { "labels": [ ],
 *  "metrics": [ [ { x: timestamp, y: value }, { } ],
 *               [ { x: timestamp, y: value }, { } ]
 *             ]
 * }
 */

var line_graph = function(chartId, data, configuration) {
    var labels = data.labels;
    var metrics = data.metrics;
    var palette = new Rickshaw.Color.Palette( { scheme: 'munin' } );
    var series = [];
    var options = $.extend({
        height: null,
    }, configuration);
    
    /**
     * Helper function which calculates the average and deviation of an array of numeric values.
     */
    var statistics = function(array) {
        var result = { mean: 0, deviation: 0};
        var sum = 0;
        for (var i = 0; i < array.length; i++) {
            sum += array[i];
        }
        result.mean = sum / array.length;
        sum = 0;
        for (var i = 0; i < array.length; i++) {
            sum += Math.pow(array[i] - result.mean, 2);
        }
        result.deviation = Math.sqrt(sum / array.length);
        return result;
    };

    var min = Number.MAX_VALUE;
    var maxes = [];
    for (var i = 0; i < metrics.length; ++i) {
        var metric = metrics[i];
        var local_max = Number.MIN_VALUE;
        for (var j = 0; j < metric.length; j++) {
            var point = metric[j];
            min = Math.min(min, point.y);
            local_max = Math.max(local_max, point.y);
        }
        maxes.push(local_max);
    }
    
    var max_stats = statistics(maxes);
    var threshold = 2 * max_stats.deviation + max_stats.mean;
    // Use this value to disable the dual axis
    //var threshold = Number.MAX_VALUE;
    
    var lower_max = 0, upper_max = 0;
    for (var i = 0; i < maxes.length; ++i) {
        var value = maxes[i];
        if (value > threshold) {
            upper_max = Math.max(upper_max, value);
        } else {
            lower_max = Math.max(lower_max, value);
        }
    }
    
    var lower_scale = null;
    var upper_scale = null;
    if (lower_max != 0) {
        lower_scale = d3.scale.linear().domain([ min, lower_max ]).nice();
    }
    if (upper_max != 0) {
        upper_scale = d3.scale.linear().domain([ min, upper_max ]).nice();
    }
    if (lower_scale == null) {
        lower_scale = upper_scale;
        upper_scale = null;
    }
    
    for(var i=0; i < labels.length; ++i) {
        console.log(maxes[i] > threshold);
        series.push({
            color: palette.color(),
            data: metrics[i],
            name: labels[i],
            scale: maxes[i] > threshold && upper_scale != null ? upper_scale : lower_scale,
        });
    }
    
    // compute height and width
    var width = Math.max(400, $(chartId).width());;
    var windowHeightRemaining = $(window).height() - $(chartId).offset().top;
    var height = options.height == null ? Math.max(200, windowHeightRemaining * 0.9) : options.height;
    
    var graph = new Rickshaw.Graph( {
        element: $(chartId)[0],
        width: width,
        height: height,
        renderer: 'line',
        series: series
    } );
    
    graph.configure({'interpolation': false});
    graph.render();
    
    var roundToTwo = function(num) {    
        return +(Math.round(num + "e+2")  + "e-2");
    };
    
    var hoverDetail = new Rickshaw.Graph.HoverDetail( {
        graph: graph,
        formatter: function(series, x, y) {
            var date = '<span class="date">' + new Date(x * 1000).toString() + '</span>';
            var swatch = '<span class="detail_swatch" style="background-color: ' + series.color + '"></span>';
            var data = '<span class="data">' + series.name + ": " + roundToTwo(y) + '</span>'
            var content = swatch + data + '<br>' + date;
            return content;
        }
    } );

    // The parent container holds the axis and legend.
    var parentContainer = $(chartId).parent();
    
    var legend = new Rickshaw.Graph.Legend( {
        graph: graph,
        element: $('.legend', parentContainer).get(0)
    
    } );
    
    var shelving = new Rickshaw.Graph.Behavior.Series.Toggle( {
        graph: graph,
        legend: legend
    } );
    
    var highlighter = new Rickshaw.Graph.Behavior.Series.Highlight( {
        graph: graph,
        legend: legend
    } );
    
    var axis0 = $('.axis0', parentContainer);
    var axis1 = $('.axis1', parentContainer);
    axis0.height(height);
    axis1.height(height).css('left', width + $(chartId).offset().left - 100);
    new Rickshaw.Graph.Axis.Y.Scaled({
      element: axis0.get(0),
      graph: graph,
      orientation: 'right',
      scale: lower_scale,
      tickFormat: Rickshaw.Fixtures.Number.formatKMBT,
    }).render();

    if (upper_scale != null){
        new Rickshaw.Graph.Axis.Y.Scaled({
          element: axis1.get(0),
          graph: graph,
          grid: false,
          orientation: 'left',
          scale: upper_scale,
          tickFormat: Rickshaw.Fixtures.Number.formatKMBT,
        }).render();
    }
    
    var x_axes = new Rickshaw.Graph.Axis.Time( {
        graph: graph,
        timeFixture: new Rickshaw.Fixtures.Time.Local()
    } );
    
    x_axes.render();
    
}
