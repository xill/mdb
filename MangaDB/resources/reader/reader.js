
var seriesName = gup("name");
var chapterNumber = parseInt(gup("ch"));
var chapterData = undefined;
var seriesData = undefined;
var pageIndex = 0;
var readerView = undefined;

// get start page from location hash.
if(window.location.hash) {
	pageIndex = parseInt(window.location.hash.substring(1));
}
if(!chapterNumber) chapterNumber = 0;

// get current chapter data
$.ajax({
	"url" : "/api/series/"+seriesName+"/"+chapterNumber,
	"dataType" : "json",
	"success" : function(resp) {
		console.log(resp);
		chapterData = resp;
		// no chapter to show. redirect back to chapters view.
		if(!chapterData.pages || chapterData.pages.length == 0) {
			window.location.href = "/chapters/?name="+seriesName;
		}
		
		if(document.readyState == 'complete') showReader();
	}
});

// get overall series chapters data.
$.ajax({
	"url" : "/api/series/"+seriesName,
	"dataType" : "json",
	"success" : function(resp) {
		console.log(resp);
		seriesData = resp;
		
		if(document.readyState == 'complete') showChapters();
	}
});

$(document).ready(function(){
	// set back button href
	$("#backLink").attr("href","/chapters/?name="+seriesName)
	
	if(seriesData) showChapters();
	if(chapterData) showReader();
});

// set chapter select dropdown.
function showChapters() {
	var topMenuDrop = $("#dropMenu");
	var data = seriesData.chapters;
	
	for( var i = 0; i < data.length ; ++i ) {
		topMenuDrop.append($('<option value="'+ data[i].name +'">' + data[i].name + '</option>'));
	}
	topMenuDrop[0].value = data[chapterNumber].name;
	topMenuDrop.bind("change",function(){
		var ind = -1;
		for( var i = 0; i < data.length ; ++i ) {
			if(data[i].name === this.value) {
				ind = i;
				break;
			}
		}
		// changed to current chapter. ignore.
		if(ind == chapterNumber || ind == -1) return;
		
		window.location.href = "/reader/?name="+seriesName+"&ch="+ind;
	});
}

// set reader view
function showReader() {
	readerView = $("#readerView");
	// fix pageIndex if its garbage.
	if(pageIndex >= chapterData.pages.length) {
		pageIndex = chapterData.pages.length-1;
	}
	else if(pageIndex < 0) {
		pageIndex = 0;
	}
	
	readerView.attr("src",buildPageUrl(pageIndex));
	window.location.hash = "#"+pageIndex;
	
	var prevPageDiv = $("#previousPage");
	var nextPageDiv = $("#nextPage");
	
	var eventKey = "click";
	prevPageDiv.bind(eventKey,previousPage);
	nextPageDiv.bind(eventKey,nextPage);
	
}

// progress to next page
function nextPage() {
	pageIndex++;
	if(pageIndex >= chapterData.pages.length) {
		// redirect to next chapter.
		window.location.href = "/reader/?name="+seriesName+"&ch="+(chapterNumber+1);
		return;
	}
	
	readerView.attr("src",buildPageUrl(pageIndex));
	scrollUp();
	window.location.hash = "#"+pageIndex;
}

// progress to previous page
function previousPage() {
	pageIndex--;
	if(pageIndex < 0) {
		if(chapterNumber > 0) {
			// redirect to previous chapter to last page.
			window.location.href = "/reader/?name="+seriesName+"&ch="+(chapterNumber-1)+"#9999";
		}
		pageIndex = 0;
		return;
	}
	
	readerView.attr("src",buildPageUrl(pageIndex));
	scrollUp();
	window.location.hash = "#"+pageIndex;
}

/**
 * scroll up animation performed on page change.
 */
function scrollUp() {
	$("html, body").animate({ scrollTop: "0px" });
}

// helper function for building page url.
function buildPageUrl( pageNumber ) {
	var str = chapterData.pages[pageNumber];
	str = "/" + str;
	while(str.indexOf("\\") != -1) str = str.replace("\\","/");
	return str;
}