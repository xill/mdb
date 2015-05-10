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
	}
});

$(document).ready(function(){
	if(seriesData) showSeriesData();
});

function showSeriesData() {
	// check if valid series
	if(seriesData && seriesData.name) {
		$(".seriesName").text(seriesData.name);
		var chapterList = $(".chapterList");
		var chapters = seriesData.chapters;
		for( var i = 0 ; i < chapters.length; ++i ) {
			var chapter = chapters[i];
			var line = $('<a href="/reader/?name='+seriesName+'&ch='+i+'" class="chapterLine">' + chapter.name + '</a>');
			chapterList.append(line);
			chapterList.append($('<br/>'));
		}
	}
	// not a valid series
	else {
	
	}
}