// TODO use ajax /api/<seriesname> to get series data
var seriesName = gup("name");
var seriesData = undefined;

$.ajax({
	"url" : "/api/series/"+seriesName,
	"dataType" : "json",
	"success" : function(resp) {
		console.log(resp);
		seriesData = resp;
		
		if(document.readyState == 'complete') showSeriesData();
	},
	"error" : function(resp) {
		console.error(resp);
	}
});

$(document).ready(function(){
	if(seriesData) showSeriesData();
});

function showSeriesData() {
	// check if valid series
	if(seriesData && seriesData.name) { 
		$(".seriesName").text(seriesData.name); // TODO use one of the actual names.
		var chapterList = $(".chapterList");
		var chapters = seriesData.chapters;
		for( var i = 0 ; i < chapters.length; ++i ) {
			var chapter = chapters[i];
			var line = $('<a href="/reader/?name='+seriesName+'&ch='+i+'" class="chapterLine">' + chapter.name + '</a>');
			chapterList.append(line);
			chapterList.append($('<br/>'));
		}
		
		var tagList = $("#tagList");
		var tagData = seriesData.tags;
		for( var i = 0; i < tagData.length ; ++i ) {
			var tagLink = $('<a href="/search/?tags='+tagData[i]+'">'+tagData[i]+'</a>');
			tagList.append(tagLink);
		}
		
		var descriptionField = $("#descriptionField");
		descriptionField.text(seriesData.description);
		
		var authorName = $(".authorName");
		var authorValue = seriesData.author;
		var artistValue = seriesData.artist;
		if(!authorValue) {
			authorName.text("Unknown");
		}
		else {
			if(authorValue == artistValue) {
				authorName.append($('<a href="/search/?author='+authorValue+'">'+authorValue+'</a>'));
			}
			else {
				authorName.append($('<a href="/search/?author='+authorValue+'">'+authorValue+'</a>'));
				authorName.append(', ');
				authorName.append($('<a href="/search/?artist='+artistValue+'">'+artistValue+'</a>'));
			}
		}
		
		if(seriesData.thumbnail) {
			var thumbnail = $("#thumbnail");
			var img = new Image();
			img.onload = function() {
				var w = 0;
				var h = 0;
				var m = 175;
				if(img.width < img.height) {
					w = m;
					h = w * (img.height / img.width);
				}
				else {
					h = m;
					w = h * (img.width / img.height);
				}
				
				thumbnail.css({
					"background-image":"url("+img.src+")",
					"width" : w + "px",
					"height" : h + "px"
				});
			};
			img.src = "/"+seriesName+"/"+seriesData.thumbnail;
		}
	}
	// not a valid series
	else {
	
	}
}
