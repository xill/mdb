
var tags = gup("tags");
var rawTags = tags;
if(tags) tags = tags.split(",");
for( var i = 0 ; i < tags.length ; ++i ) {
	tags[i] = decodeURIComponent(tags[i]);
}

var name = gup("name");
if(!name) name = "";

var authorName = gup("author");
if(!authorName) authorName = "";

var artistName = gup("artist");
if(!artistName) artistName = "";

var dataSet = undefined;
var tagSet = undefined;

// get all tags
$.ajax({
	"url" : "/api/tags",
	"dataType" : "json",
	"success" : function(resp) {
		console.log(resp);
		tagSet = resp;
		
		if(document.readyState == 'complete') buildTags();
	},
	"error" : function(e) {
		console.error(e);
	}
});

// request to fetch full data set.
$.ajax({
	"url" : "/api/search?tags="+rawTags+"&name="+name+"&author="+authorName+"&artist="+artistName,
	"dataType" : "json",
	"success" : function(resp) {
		console.log(resp);
		dataSet = resp;
		
		if(document.readyState == 'complete') showSearchResults();
	},
	"error" : function(e) {
		console.error(e);
	}
});

function buildTags() {
	var gridParent = $(".checkbox-grid");
	var data = tagSet.tags;
	data.sort();
	
	for( var i = 0; i < data.length ; ++i ) {
		var checkBox = $('<input class="tagBox" type="checkbox" name="'+data[i]+'" value="'+data[i]+'" /><label for="'+data[i]+'">'+data[i]+'</label>');
		if(tags.indexOf(data[i]) != -1) checkBox[0].checked = true;
		gridParent.append($('<li></li>').append(checkBox));
	}
}

function showSearchResults() {
	// searchResults
	var searchResults = $(".searchResults");
	var data = dataSet.series;
	
	if(data) {
		for( var i = 0 ; i < data.length ; ++i ) {
			searchResults.append($('<div><a href="/chapters/?name='+data[i]+'">'+data[i]+'</a></div>'));
		}
	}
}

function onSearch() {

	var nameFieldValue = $("#nameField").val();
	var authorFieldValue = $("#authorField").val();
	var artistFieldValue = $("#artistField").val();
	
	var nTags = [];
	var tagBoxes = $(".tagBox");
	for( var i = 0 ; i < tagBoxes.length ; ++i ) {
		var tagBox = tagBoxes[i];
		if(tagBox.checked) nTags.push($(tagBox).val());
	}

	window.location.href = location.protocol + '//' + location.host + location.pathname 
	+ "?tags=" + nTags.join(",") + "&name=" + nameFieldValue + "&author=" + authorFieldValue + "&artist=" + artistFieldValue;
}

$(document).ready(function()
{
	$("#nameField").val(decodeURIComponent(name));
	$("#authorField").val(decodeURIComponent(authorName));
	$("#artistField").val(decodeURIComponent(artistName));

	if(tagSet) buildTags();
	if(dataSet) showSearchResults();
	
	$(document).bind("keydown",function(e){
		// enter key
		if(e.keyCode === 13) {
			onSearch();
		}
	});
});

