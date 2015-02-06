/*
 * Data passed into this function should be of the form:
 * 
 * { "labels": [ ],
 *  "metrics": [ [ [timestamp, value], [ , ] ],
 *               [ [timestamp, value], [ , ] ]
 *             ]
 * }
 */

var multi_line_graph = function(selector, data, configuration) {

    var options = $.extend({
        axis: {
            x: { 
                ticks: 8,
                padding: 8,
                date_format: "%H:%M",
                orientation: "bottom",
            },
            y: { 
                padding: 4,
                orientation: "right",
            },
        },
        
        tooltip: {
            date_format: "%Y-%m-%d %H:%M",
            y_offset: 28,
        },
        
        margin: {
            top: 20,
            right: 50, // extra padding for the y-axis text
            bottom: 20,
            left: 20,
        },
        
        legend: {
            column_width: 385, // allows for 40 characters of text
            row_height: 20,
            
            margin: {
                top: 5,
                left: 20,
            },
            
            box: {
                size: 10,
                margin: 3,
            },
        },
        
        width: 'auto',
        height: 'auto',
        
        minimum: {
            width: 400,
            height: 200,
        },
        
        height_ratio: 0.9, // window ratio when height set to auto
        
        type: 'auto', // auto, line, area
        stacked: false,
        
    }, configuration);

    if (options.height == null) {
        options.height = 'auto';
    }
    
    if (options.type == 'auto') {
        options.type = options.stacked ? 'area' : 'line';
    }
    if (options.width == 'auto') {
        options.width = Math.max(options.minimum.width, $(selector).width());
    }
    if (options.height == 'auto') {
        var windowHeightRemaining = $(window).height() - $(selector).offset().top;
        options.height = Math.max(options.minimum.height, windowHeightRemaining * options.height_ratio);
    }
    
    var metricIdCount = data.labels.length;
    var color = d3.scale.category20c();
    
    var chartWidth = options.width - options.margin.left - options.margin.right;
    
    var legendColumnCount = Math.floor(chartWidth / options.legend.column_width) || 1; // must have at least one column
    var legendRowCount = Math.ceil(metricIdCount / legendColumnCount);
    var legendHeight = legendRowCount * options.legend.row_height;
    
    var chartMargin = options.margin.top + options.margin.bottom;
    var chartHeight = options.height - (chartMargin + options.legend.margin.top + legendHeight);
    var chartBottom = chartHeight + chartMargin;
    
    if (options.height == 'auto') {
        options.height = chartBottom + options.legend.margin.top + legendHeight;
    }
    
    console.log("%s svg(%dx%d) chart(%dx%d) chartBottom:%d legend(autox%d) legend.RxC(%dx%d))",
            selector,
            options.width,
            options.height,
            chartWidth,
            chartHeight,
            chartBottom,
            legendHeight,
            legendColumnCount,
            legendRowCount);
    
    var svg = d3.select(selector).append("svg")
        .attr("width", options.width)
        .attr("height", options.height)
        
    var chart = svg.append("g")
        .attr("transform", "translate(" + options.margin.left + "," + options.margin.top + ")");
    
    // this entire block is to interpolate missing values as zero
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    var xMap = [];
    var xSet = d3.set();
    var lastDx = 0; // dx wont be available when the the entry is missing. since its identical everywhere, storing it 
    for (var i in data.metrics) {
        xMap.push({});
        
        for (var j in data.metrics[i]) {
            xSet.add(data.metrics[i][j].x);
            xMap[i][data.metrics[i][j].x] = data.metrics[i][j];
            lastDx = data.metrics[i][j].dx;
        }
    }
    var n = function(a,b) { return a-b }; // numeric sort
    var xValues = xSet.values().map(function(value) { return +value; /* hack to deal with values str cast */ }).sort(n);
    xSet = null; // freeing
    
    for (var i in data.metrics) {
        var metrics = [];
        
        for (var j in xValues) {
            var x = xValues[j];
            
            if (x in xMap[i]) {
                metrics.push({x: x, dx: xMap[i][x].dx, y: xMap[i][x].y});
            } else {
                metrics.push({x: x, dx: lastDx, y: 0});
            }
        }
        data.metrics[i] = metrics;
    }
    console.debug(xValues.sort());
    console.debug(data.metrics);
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    d3.layout.stack()(data.metrics);
    
    // compute the extent for each of our data arrays
    var xScaleExtents = [];
    $.each(data.metrics, function(index, metric) {
        var lower = d3.extent(metric, function(metricValue) {
            console.assert('x' in metricValue, 'metric value is missing x value')
            return metricValue.x;
        });
        var upper = d3.extent(metric, function(metricValue) {
            console.assert('x' in metricValue, 'metric value is missing x value')
            console.assert('dx' in metricValue, 'metric value is missing dx value')
            return metricValue.x + metricValue.dx;
        });
        
        xScaleExtents.push(lower[0]);
        xScaleExtents.push(upper[1]);
    });
    
    // compute the extent for each of our data arrays
    var yScaleExtents = [];
    $.each(data.metrics, function(index, metric) {
        var res = d3.extent(metric, function(metricValue) {
            console.assert('y' in metricValue, 'metric value is missing y value')
            
            // y0 is populated if stacking
            if ('y0' in metricValue) {
                return metricValue.y + metricValue.y0
            } else {
                return metricValue.y;
            }
        });
        
        yScaleExtents.push(res[0]);
        yScaleExtents.push(res[1]);
    });
    
    // scales translate from data coordinates to pixel coordinates
    var xScale = d3.scale
        .linear()
        .domain(d3.extent(xScaleExtents))
        .range([0, chartWidth]);
    var yScale = d3.scale
        .linear()
        .domain(d3.extent(yScaleExtents))
        .range([chartHeight, 0]); // inverted as SVGs grow downward

    var xAxis = d3.svg
        .axis()
        .scale(xScale)
        .tickSize(chartHeight) // ticks should run the full length of the chart
        .orient(options.axis.x.orientation);
    var yAxis = d3.svg
        .axis()
        .scale(yScale)
        .tickSize(chartWidth)
        .tickPadding(options.axis.y.padding)
        .orient(options.axis.y.orientation);
    
    chart.append("g").attr("class", "x axis").call(xAxis);
    chart.append("g").attr("class", "y axis").call(yAxis);
    
    var formatCount = d3.format(",.0f");
    var formatRangeCount = d3.format(",.2f");
    
    // The bar width is the x scale translation smallest combination of x and dx (which doesn't actually ever change).
    var barWidth = xScale(d3.min(data.metrics, function(m) { return d3.min(m, function(d) { return d.x + d.dx; }) }));
    
    var tooltip = d3
        .select('body')
        .append('div')
        .attr('class', 'tooltip')
        .style('opacity', 0);
    
    $.each(data.metrics, function (metrickIndex, metrics) {
        var bar = chart
            .append("g")
            .attr("class", "bars")
            .selectAll(".bar" + metrickIndex)
            .data(metrics)
            .enter()
            .append("g")
            .attr("class", "bar")
            .attr("transform", function(d) {
                d.y0 = d.y0 || 0;
                console.log(d.x, d.dx, d.y, d.y0, yScale(d.y + d.y0), xScale(d.x));
                return "translate(" + xScale(d.x) + "," + yScale(d.y + d.y0) + ")";
            });
        
        bar.append("rect")
            .attr("x", 0)
            .attr("width", barWidth - 2)
            .attr("height", function(d) { return chartHeight - yScale(d.y); })
            .style("stroke", function(metricValue, index) { return color(metrickIndex); })
            .style("fill", function(metricValue, index) { return d3.rgb(color(metrickIndex)); })
            .on('mouseover', function(metricValue, index) {
                tooltip.transition().duration(100).style('opacity', 0.95);
                tooltip.html(
                        '<span class="heading" style="color: '
                        // less than ideal method of getting the circle's fill color 
                        + color(metrickIndex)
                        + '">'
                        + data.labels[metrickIndex] 
                        + ' <span class="count">'
                        + metricValue.y
                        + ' ('
                        + (metricValue.y + metricValue.y0)
                        + ' total)</span></span><span class="range">' 
                        + formatRangeCount(metricValue.x)
                        + ' to '
                        + formatRangeCount(metricValue.x + metricValue.dx)
                        + '</span>')
                    .style('left', d3.event.pageX + 'px')
                    .style('top',  d3.event.pageY - options.tooltip.y_offset + 'px');
            })
            .on('mouseout', function(d) {
                tooltip.transition().duration(200).style('opacity', 0);
            });
        
        bar.append("text")
            .attr("dy", ".75em")
            .attr("y", 6)
            .attr("x", barWidth / 2)
            .attr("text-anchor", "middle")
            .text(function(d) { return d.y != 0 ? formatCount(d.y) : ""; });
    });
    
    var drawLegend = function() {
        
        var lopts = options.legend;
        
        var legend = svg
            .append("g")
            .attr("class", "legend")
            .attr("transform", "translate("
                    + lopts.margin.left + ","
                    + (chartBottom + lopts.margin.top) + ")");
        
        var blur = function(d, index) {
            chart.selectAll("g.bars").style('opacity', 1);
        };
        var focus = function(d, index) {
            var thisIndex = function(subdata, subindex) { return index == subindex; };
            
            chart.selectAll("g.bars").style('opacity', 0.1).filter(thisIndex).style('opacity', 1);
        };
        
        var indexToXOffset = function(index) { return index % legendColumnCount * lopts.column_width; };
        var indexToYOffset = function(index) { return Math.floor(index / legendColumnCount) * lopts.row_height; };
        
        legend.selectAll('rect')
              .data(d3.range(0, metricIdCount))
              .enter()
              .append("rect")
              .attr("x", indexToXOffset)
              .attr("y", indexToYOffset)
              .attr("width", lopts.box.size)
              .attr("height", lopts.box.size)
              .style("fill", function(d, index) { return color(index); })
              .on('mouseover', focus)
              .on('mouseout', blur);
    
        legend.selectAll('text')
              .data(d3.range(0, metricIdCount))
              .enter()
              .append("text")
              .attr("x", function(d, index) { return indexToXOffset(index) + lopts.box.size + lopts.box.margin; })
              .attr("y", function(d, index) { return indexToYOffset(index) + lopts.box.size; })
              .text(function(d, index) { return data.labels[index]; })
              .on('mouseover', focus)
              .on('mouseout', blur);
    }
    
    drawLegend();
}
