
// full data set.
var dataSet = undefined;

var SERIES_MODE = "/api/all";
var TAGS_MODE = "/api/tags";
var REGUEST_MODE = SERIES_MODE

if(gup("mode")=="tags") {
	REGUEST_MODE = TAGS_MODE;
}

// request to fetch full data set.
$.ajax({
	"url" : REGUEST_MODE,
	"dataType" : "json",
	"success" : function(resp) {
		console.log(resp);
		dataSet = resp;
		
		if(document.readyState == 'complete') showDirectory();
	},
	"error" : function(e) {
		console.error(e);
	}
});

// build and show directory view.
function showDirectory() {
	// base directory element.
	var grid = $(".gridParent");
	
	var data = undefined;
	if(REGUEST_MODE == TAGS_MODE) {
		data = dataSet.tags;
	} else {
		data = dataSet.series;
	}
	
	// all the data with first letter of the data name as key.
	var values = {};
	for(var i = 0 ; i < data.length; ++i) {
		if(data[i]) {
			var k = data[i][0];
			if(!values[k]) values[k] = [];
			
			values[k].push(data[i]);
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
			if(REGUEST_MODE == TAGS_MODE) {
				subGrid.append($('<div><a href="/search/?tags='+keyValue[f]+'">'+keyValue[f]+'</a></div>'));
			} else {
				subGrid.append($('<div><a href="/chapters/?name='+keyValue[f]+'">'+keyValue[f]+'</a></div>'));
			}
		}
	}
}

$(document).ready(function()
{
	if(dataSet) showDirectory();
});
