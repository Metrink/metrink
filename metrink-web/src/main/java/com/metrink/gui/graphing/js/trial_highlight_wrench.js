/**
 * Puts a pop-up box around the wrench if they're a trial user. 
 */
  var trialHighlightWrench = function() {
      var currentQuery = $('#search-query').val();
      
      // We only want to display this helper if a graph hasn't been rendered.
      if (currentQuery == "") {                
	      $('#query-wizard-link').qtip({
	          content: {
	              title: '<i class="icofont-info-sign"></i>&nbsp;&nbsp;Getting Started',
	              text: "Open Metrink's query wizard with the wrench icon. This dialog will help you become " +
	                    "familiar with the query language by constructing queries for you.",
	          },
	          hide: {
	              target: $('#search-query, #query-wizard-link, .dropdown-toggle'),
	              event: 'click',
	          },
	          show: {
	              event: 'showtooltip'
	          },
	          style: {
	              classes: 'qtip-bootstrap'
	          },
	          position: {
	              my: 'top left',
	              at: 'bottom left',
	              adjust: {
	                  x: 5,
	              },
	          },
	      })
	      // Display the helper by default
	      .trigger('showtooltip');
      }
  };
  $(trialHighlightWrench);

