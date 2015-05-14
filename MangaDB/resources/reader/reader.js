
var seriesName = gup("name");
var chapterNumber = parseInt(gup("ch"));
var chapterData = undefined;
var pageIndex = 0;
var readerView = undefined;

// get start page from location hash.
if(window.location.hash) {
	pageIndex = parseInt(window.location.hash.substring(1));
}
if(!chapterNumber) chapterNumber = 0;

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

$(document).ready(function(){
	if(chapterData) showReader();
});

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

function buildPageUrl( pageNumber ) {
	var str = chapterData.pages[pageNumber];
	str = "/" + str;
	while(str.indexOf("\\") != -1) str = str.replace("\\","/");
	return str;
}