/*
 * Data passed into this function should be of the form:
 * 
 * { "labels": [ ],
 *  "metrics": [ [ [timestamp, value], [ , ] ],
 *               [ [timestamp, value], [ , ] ]
 *             ]
 * }
 */

var area_graph = function(selector, data, configuration) {
    multi_line_graph(selector, data, {
        type: 'area',
        stacked: true,
    });
}