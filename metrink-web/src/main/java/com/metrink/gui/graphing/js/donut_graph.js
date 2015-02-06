/*
 * Donut chart:
 * https://google-developers.appspot.com/chart/interactive/docs/gallery/piechart#donut
 * 
 * Data comes in this format:
 * [ ['Labels', 'Values'],
 *   ['Label 1', 16 ],
 *   ['Label 2', 10 ],
 *   ['Label 3', 28 ]
 * ]
 * 
 * Configuration:
 *   pieHole: the size of the hole in the middle
 *   height: the height of the chart
 *   title: the chart's title
 *   legend: either 'none' or a position: 'top', 'bottom', etc
 */
var donut_graph = function(panelId, data, configuration) {
	var ops = { packages: ['corechart'], callback: function() {
		
	    	var dataTable = google.visualization.arrayToDataTable(data);
	    	
	    	var legendFun = function(config) {
	    		if(typeof config.legend === 'undefined' || config.legend == 'none') {
	    			return 'none';
	    		} else {
	    			return { position: config.legend, alignment: 'center' };
	    		}
	    	};
	
	    	var options = {
	    			// colors: [ '#109618', '#3366cc', '#ff9900', '#dc3912', '#222222'],
	    	        //title: configuration.title,
	    	        //titleTextStyle: { fontSize: 18 },
	                height: 300,
	                width: legendFun(configuration) === 'none' ? 300 : 500,
	                pieHole: 0.4,
	                legend: legendFun(configuration),
	                chartArea: { top: 0, left: 0, }
	    	};
	    	
	    	var chartDiv = $(panelId +' #graph-div');
	    	var valueDiv = $(panelId + ' .overlay-value');
	    	var titleDiv = $(panelId + ' .overlay-title');
	    	
	    	var chart = new google.visualization.PieChart(chartDiv.get(0));
	    	
	    	chart.draw(dataTable, options);
	    	
	        var chartBoundingBox = chart.getChartLayoutInterface().getChartAreaBoundingBox();
	        
	        //
	        // Figure out the overlay value for the left side
	        //
	        var valueOverlayLeft, titleOverlayLeft;
	        var valueOverlayTop = Math.floor(chartBoundingBox.height / 2) - 10; 
	        var titleOverlayTop = chartBoundingBox.height - 5; 
	        
	        if(legendFun(configuration) === 'none') {
	        	valueOverlayLeft = Math.floor(chartBoundingBox.width / 2) - Math.floor(valueDiv.width()/2);
	        	titleOverlayLeft = Math.floor(chartBoundingBox.width / 2) - Math.floor(titleDiv.width()/2);
	        } else {
	        	valueOverlayLeft = Math.floor(chartBoundingBox.width / 2) - Math.floor(valueDiv.width()/2) - 56;
	        	titleOverlayLeft = Math.floor(chartBoundingBox.width / 2) - Math.floor(titleDiv.width()/2) - 56;
	        }	        
	        
	        valueDiv.get(0).style.top  = valueOverlayTop  + "px";
	        valueDiv.get(0).style.left = valueOverlayLeft + "px";
	    	
	    	titleDiv.get(0).style.top  = titleOverlayTop  + "px";
	    	titleDiv.get(0).style.left = titleOverlayLeft + "px";	    	
	    }
	};
	
    google.load('visualization', '1', ops);	
};
