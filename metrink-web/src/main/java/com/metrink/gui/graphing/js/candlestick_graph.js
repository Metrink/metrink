/**
 * Candlestick chart using Google
 * https://developers.google.com/chart/interactive/docs/gallery/candlestickchart
 */

var draw_candlestick_chart = function(chartId, data, configuration) {
	var ops = { packages: ['corechart'], callback: function() {
    	// second arg treats first row as data
    	var dataTable = google.visualization.arrayToDataTable(data, true);

    	var options = { legend:'none' };
    	
    	var chart = new google.visualization.CandlestickChart($(chartId).get(0));
    	
    	chart.draw(dataTable, options);
    }};
	
    google.load('visualization', '1', ops);
	
};
