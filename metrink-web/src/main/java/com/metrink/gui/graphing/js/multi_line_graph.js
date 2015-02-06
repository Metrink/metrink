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

    console.log(options);
    
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
    var color = metricIdCount > 10 ? d3.scale.category20() : d3.scale.category10();
    
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
    
    var svg = d3
        .select(selector)
        .append("svg")
        .attr("width",  options.width)
        .attr("height", options.height);

    if (metricIdCount == 0) {
        svg.append('text')
           .attr('x', options.margin.left)
           .attr('y', options.margin.top)
           .style('font', '20px sans-serif')
           .text('No Data');
        return;
    }
    
    var chart = svg.append("g")
        .attr("transform", "translate(" + options.margin.left + "," + options.margin.top + ")");
    
    // this entire block (sans the last line) is to interpolate missing values as zero
    if (options.stacked) {
        var xMap = [];
        var xSet = d3.set();
        for (var i in data.metrics) {
            xMap.push({});
            
            for (var j in data.metrics[i]) {
                xSet.add(data.metrics[i][j].x);
                xMap[i][data.metrics[i][j].x] = data.metrics[i][j].y;
            }
        }
        var n = function(a,b) { return a-b }; // numeric sort
        var xValues = xSet.values().map(function(v) { return +v; /* hack to deal with values str cast */ }).sort(n);
        xSet = null; // freeing
        
        for (var i in data.metrics) {
            var metrics = [];
            
            for (var j in xValues) {
                var x = xValues[j];
                
                if (x in xMap[i]) {
                    metrics.push({x: x, y: xMap[i][x]});
                } else {
                    metrics.push({x: x, y: 0});
                }
            }
            data.metrics[i] = metrics;
        }
        
        d3.layout.stack()(data.metrics);
    }
    
    // compute the extent for each of our data arrays
    var xScaleExtents = [];
    $.each(data.metrics, function(index, metric) {
        var res = d3.extent(metric, function(metricValue) {
            console.assert('x' in metricValue, 'metric value is missing x value')
            return metricValue.x;
        });
        
        xScaleExtents.push(res[0]);
        xScaleExtents.push(res[1]);
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
    var xScale = d3.time
        .scale()
        .domain(d3.extent(xScaleExtents))
        .range([0, chartWidth])
        .nice(d3.time.minute);
    var yScale = d3.scale
        .linear()
        .domain(d3.extent(yScaleExtents))
        .range([chartHeight, 0]); // inverted as SVGs grow downward

    var xAxis = d3.svg
        .axis()
        .scale(xScale)
        .tickSize(chartHeight) // ticks should run the full length of the chart
        .tickFormat(function (d) { return d3.time.format(options.axis.x.date_format)(d) })
        .tickPadding(options.axis.x.padding)
        .ticks(options.axis.x.ticks)
        .orient(options.axis.x.orientation);
    var yAxis = d3.svg
        .axis()
        .scale(yScale)
        .tickSize(chartWidth)
        .tickPadding(options.axis.y.padding)
        .orient(options.axis.y.orientation);
    
    chart.append("g").attr("class", "x axis").call(xAxis);
    chart.append("g").attr("class", "y axis").call(yAxis);
    
    var tooltip = d3
        .select('body')
        .append('div')
        .attr('class', 'tooltip')
        .style('opacity', 0);

    var pathDataGenerator;
    switch(options.type) {
    case 'line':
        pathDataGenerator = d3.svg
            .line()
            .y(function(metricValue) { return yScale(metricValue.y); } )
    break;
    case 'area':
        // TODO: for area charts, we only want to stroke the upper line. to fix this, area charts need to have a line
        // in addition to a area path generator. then the stroke should only be set on the line. the consequence now is
        // the stroke from the upper path slightly bleeds through the stroke from the lower path.
        
        var getY0IfExists = function(metricValue) { return 'y0' in metricValue ? metricValue.y0 : 0 ;}
        pathDataGenerator = d3.svg
            .area()
            .y0(function(metricValue) { return yScale(getY0IfExists(metricValue)); } )
            .y1(function(metricValue) { return yScale(getY0IfExists(metricValue) + metricValue.y); });
    break;
    default:
        console.error("Unknown type: %s", options.type);
        return;
    }
    
    pathDataGenerator.x(function(metricValue) { return xScale(metricValue.x); });

    $.each(data.metrics, function (metrickIndex, metrics) {
        chart
            .insert("g", "g.path") // paths should always be inserted underneath dots
            .attr("class", "path")
            .append("path")
            .datum(metrics)
            .attr("d", pathDataGenerator);
        chart
            .insert("g", "g.dot")
            .attr("class", "dot")
            .selectAll("circle")
            .data(metrics)
            .enter()
            .append("circle")
            .attr("d", pathDataGenerator)
            .attr("cx", pathDataGenerator.x())
            .attr("cy", pathDataGenerator.y())
            .attr("r", 3)
            .on('mouseover', function(metricValue, index) {
                tooltip.transition().duration(100).style('opacity', 0.95);
                tooltip.html(
                        '<span class="heading" style="color: '
                        // less than ideal method of getting the circle's fill color 
                        + d3.select(d3.select(this).node().parentNode).style('fill')
                        + '">'
                        + data.labels[metrickIndex] 
                        + ' <span class="value">'
                        + metricValue.y
                        + '</span></span><span class="date">' 
                        + d3.time.format(options.tooltip.date_format)(new Date(metricValue.x))
                        + '</span>')
                    .style('left', d3.event.pageX + 'px')
                    .style('top',  d3.event.pageY - options.tooltip.y_offset + 'px');
            })
            .on('mouseout', function(d) {
                tooltip.transition().duration(200).style('opacity', 0);
            });
    });
    
    // paths and lines are being inserted in reverse order, so we need to reverse the index when referencing them
    var fixIndex = function(index) { return metricIdCount - index - 1 };
    
    chart.selectAll("g.path")
        .style("stroke", function(metricValue, index) { return color(fixIndex(index)); })
        .style("fill", function(metricValue, index) {
            // for area, lighten the line, otherwise set the alpha to zero
            return options.type == 'area' ? d3.rgb(color(fixIndex(index))).brighter() : "rgba(0,0,0,0)";
        });
        
    chart
        .selectAll("g.dot")
        .style("fill", function(metricValue, index) { return color(fixIndex(index)); });
    

    var drawLegend = function() {
        
        var lopts = options.legend;
        
        var legend = svg
            .append("g")
            .attr("class", "legend")
            .attr("transform", "translate("
                    + lopts.margin.left + ","
                    + (chartBottom + lopts.margin.top) + ")");
        
        var blur = function(d, index) {
            chart.selectAll("g.path").style('opacity', 1);
            chart.selectAll("g.dot") .style('opacity', 1);
        };
        var focus = function(d, index) {
            var thisIndex = function(subdata, subindex) { return index == fixIndex(subindex); };
            
            chart.selectAll("g.path").style('opacity', 0.1).filter(thisIndex).style('opacity', 1);
            chart.selectAll("g.dot") .style('opacity', 0.0).filter(thisIndex).style('opacity', 1);
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