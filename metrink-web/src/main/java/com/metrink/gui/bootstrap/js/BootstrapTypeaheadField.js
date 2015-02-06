var settings = {
		"source": function(query, process) {
					$.ajax({
								url: "${listener_url}",
								data: { "${param_name}": query },
								success: function(data, status, jqXHR) { process(data); }
							});
				  },
		"items": ${items},
		"minLength": ${min_length}
};

$('#${component_id}').typeahead(settings);


