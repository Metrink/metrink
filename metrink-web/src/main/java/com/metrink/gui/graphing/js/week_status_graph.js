/*
 * Stacked bar chart:
 * https://google-developers.appspot.com/chart/interactive/docs/gallery/columnchart
 * 
 * Data comes in this format:
 * [ ['< 250ms', '250ms - 500ms', '>500ms', { role: 'annotation' } ],
     ['Mon', 10, 24, 20, ''],
     ['Tue', 16, 22, 23, ''],
     ['Wed', 28, 19, 29, '']
   ]
 */
var week_status_graph = function(chartId, data, configuration) {
	var ops = { packages: ['corechart'], callback: function() {
    	var dataTable = google.visualization.arrayToDataTable(data);

    	var options = {
    			legend: { position: 'top', maxLines: 3 },
    			bar: { groupWidth: '75%' },
    			colors: [ '#109618', '#3366cc', '#ff9900', '#dc3912', '#222222'],
    			vAxis: {
    					 maxValue: 100,
    					 viewWindow: { max: 100 },
    					 title: '% Requests'
    	               },
                height: configuration.height,
    			isStacked: true
    	};
    	
    	var chart = new google.visualization.ColumnChart($(chartId).get(0));
    	
    	chart.draw(dataTable, options);
    }};
	
    google.load('visualization', '1', ops);	
};
