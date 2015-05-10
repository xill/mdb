
// full data set.
var dataSet = undefined;
// full series data set.
var seriesList = undefined;

// request to fetch full data set.
$.ajax({
	"url" : "/api/all",
	"dataType" : "json",
	"success" : function(resp) {
		console.log(resp);
		dataSet = resp;
		seriesList = dataSet.series;
		
		if(document.readyState == 'complete') showDirectory();
	}
});

// build and show directory view.
function showDirectory() {
	// base directory element.
	var grid = $(".gridParent");
	
	// all the series with first letter of the series name as key.
	var values = {};
	for(var i = 0 ; i < seriesList.length; ++i) {
		if(seriesList[i]) {
			var k = seriesList[i][0];
			if(!values[k]) values[k] = [];
			
			values[k].push(seriesList[i]);
		}
	}
	
	// build a key set and sort everything alphabetically.
	var keys = [];
	for(var k in values) {
		keys.push(k);
		values[k].sort();
	}
	keys.sort();
	
	for(var i = 0; i < keys.length; ++i) {
		var keyName = keys[i];
		var keyValue = values[keyName];
		var subGrid = $('<div class="gridChild">');
		grid.append(subGrid);
		subGrid.append('<b>'+keyName+'</b><br/>');
		for(var f = 0 ; f < keyValue.length ; ++f) {
			subGrid.append($('<div><a href="/chapters/?name='+keyValue[f]+'">'+keyValue[f]+'</a></div>'));
		}
	}
}

$(document).ready(function()
{
	if(dataSet) showDirectory();
});