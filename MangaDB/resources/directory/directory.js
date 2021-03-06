/**
 * Directory views main javascript file.
 * 
 * Depending on the mode it can show tags or series.
 */


// full data set.
var dataSet = undefined;

// api command for series data.
var SERIES_MODE = "/api/all";
// api command for tag data.
var TAGS_MODE = "/api/tags";
// api command for content tag data.
var CONTENT_TAGS_MODE = "/api/ctags";

// variable to determine which mode is used.
var REGUEST_MODE = SERIES_MODE

// read mode from url.
if(gup("mode")=="tags") {
	REGUEST_MODE = TAGS_MODE;
}
else if(gup("mode")=="ctags") {
	REGUEST_MODE = CONTENT_TAGS_MODE;
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
	
	// link used name to key
	var nameToKey = {};

	var data = undefined;
	// setup data for tags.
	if(REGUEST_MODE == TAGS_MODE || REGUEST_MODE == CONTENT_TAGS_MODE) {
		for(var i = 0 ; i < dataSet.tags.length; ++i) {
			nameToKey[dataSet.tags[i]] = dataSet.tags[i];
		}
		data = dataSet.tags;
	}
	// setup data for series.
	else {
		var sNames = [];
		for(var i = 0 ; i < dataSet.series.length; ++i) {
			nameToKey[dataSet.series[i].names[0]] = dataSet.series[i].name;
			sNames.push(dataSet.series[i].names[0]);
		}
		data = sNames;
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
	
	// set data links.
	for(var i = 0; i < keys.length; ++i) {
		var keyName = keys[i];
		var keyValue = values[keyName];
		var subGrid = $('<div class="gridChild">');
		grid.append(subGrid);
		subGrid.append('<b>'+keyName+'</b><br/>');
		for(var f = 0 ; f < keyValue.length ; ++f) {
			if(REGUEST_MODE == TAGS_MODE || REGUEST_MODE == CONTENT_TAGS_MODE) {
				subGrid.append($('<div><a href="/search/?tags='+nameToKey[keyValue[f]]+'">'+keyValue[f]+' ('+dataSet.count[keyValue[f]]+')</a></div>'));
			} else {
				subGrid.append($('<div><a href="/chapters/?name='+nameToKey[keyValue[f]]+'">'+keyValue[f]+'</a></div>'));
			}
		}
	}
	
	// initialize topbar.
	setupBaseTopbarFunctionality();
}

$(document).ready(function()
{
	if(dataSet) showDirectory();
});
