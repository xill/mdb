
var seriesName = gup("name");
var chapterNumber = parseInt(gup("ch"));
var chapterData = undefined;
var seriesData = undefined;
var pageIndex = 0;
var readerView = undefined;

var layout = -1;
var LAYOUT_STANDARD = 0;
var LAYOUT_LARGE = 1;
var LAYOUT_FIT = 2;

if(typeof localStorage !== "undefined") {
	try {
		layout = parseInt(localStorage.getItem("layout"));
		if(isNaN(layout)) layout = -1;
	} catch(e) {
		layout = -1;
	}
}


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
	$("#readerChapterLink").find("a").attr("href","/chapters/?name="+seriesName);
	
	if(seriesData) showChapters();
	if(chapterData) showReader();
});

// set chapter select dropdown.
function showChapters() {
	
	var topMenuDrop = $("#dropMenu");
	var data = seriesData.chapters;
	$("#readerChapterLink").find("a").text(seriesData.names[0]);
	
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
	
	var prevPageDiv = $("#previousPage");
	var nextPageDiv = $("#nextPage");
	
	var eventKey = "click";
	prevPageDiv.bind(eventKey,previousPage);
	nextPageDiv.bind(eventKey,nextPage);
	
	setupBaseTopbarFunctionality();
	
	// setup page select
	var scrollContainer = $("#pageScrollContainer");
	for( var i = 0; i < chapterData.pages.length; ++i ) {
		var li = $('<li class="pageScrollLine pageline'+i+'">'+(i+1)+'</li>');
		scrollContainer.append(li);
	}
	
	// stored previous coordinates.
	var prevX = undefined;
	var prevY = undefined;
	// tracks scroll distance
	var dist = 0;
	// max distance and still considered a click.
	var maxDist = 5;
	// distance modifier for scrolling
	var scrollMod = 2;
	
	var pageScroll = $("#pageScroll");
	pageScroll.bind("mousedown",function(e){

		// get initial mouse position
		prevX = e.originalEvent.pageX;
		prevY = e.originalEvent.pageY;
		
		e.stopPropagation();
		e.preventDefault();
	});
	
	pageScroll.bind("mousemove",function(e){
		
		// keep scrolling if mouse is pressed down.
		if(typeof prevY !== "undefined" && e) {
			var x = e.originalEvent.pageX;
			var y = e.originalEvent.pageY;
			
			var diffX = prevX - x;
			var diffY = prevY - y;
			dist += Math.abs(diffX) + Math.abs(diffY);
			
			prevX = x;
			prevY = y;

			pageScroll.scrollTop(pageScroll.scrollTop() + diffY * scrollMod);
			e.stopPropagation();
			e.preventDefault();
		}
		
	});
	
	pageScroll.bind("mouseup mouseout",function(e){
		
		var ev = e.toElement || e.relatedTarget;
		// handle clicks
		if(e.type === "mouseup" && dist <= maxDist) {
			// determine what was clicked.
			var t = (ev) ? $(ev).text() : undefined;
			if(t) {
				pageIndex = parseInt(t)-1;
				goToPage();
			}
		}
		
		// handle end of drags.
		if((e.type === "mouseout" && (ev == pageScroll[0] || ev == scrollContainer[0])) 
			|| e.type === "mouseup"
		) {
			if(typeof prevY !== "undefined" && e) {
				
				var x = e.originalEvent.pageX;
				var y = e.originalEvent.pageY;
				
				var diffX = prevX - x;
				var diffY = prevY - y;
				
				prevX = undefined;
				prevY = undefined;
				dist = 0;

				pageScroll.scrollTop(pageScroll.scrollTop() + diffY * scrollMod);
				e.stopPropagation();
				e.preventDefault();
			}
		}
	});
	
	// setup layout buttons
	var plainModeBtn = $('#plainModeButton');
	var largeModeBtn = $('#largeModeButton');
	var fitModeBtn = $('#fitModeButton');
	
	plainModeBtn.bind("click",function(){
		toMode(LAYOUT_STANDARD);
		fitReader();
	});
	
	largeModeBtn.bind("click",function(){
		toMode(LAYOUT_LARGE);
		fitReader();
	});
	
	fitModeBtn.bind("click",function(){
		toMode(LAYOUT_FIT);
		fitReader();
	});
	
	// chapter change buttons.
	var prevButton = $('.prevButton');
	var nextButton = $('.nextButton');
	
	prevButton.bind("click",function(){
		window.location.href = "/reader/?name="+seriesName+"&ch="+(chapterNumber-1);
	});
	
	nextButton.bind("click",function(){
		window.location.href = "/reader/?name="+seriesName+"&ch="+(chapterNumber+1);
	});
	
	$(window).bind("resize",function() {
		fitReader();
	});
	
	// set previous layout mode.
	if(layout != -1) {
		var tmp = layout;
		layout = -1;
		toMode(tmp);
	}
	// set large basic layout for mobile
	else if(navigator.userAgent.toLowerCase().indexOf("mobile") != -1) {
		toMode(LAYOUT_LARGE);
	}
	// set standard basic layout for everyone else.
	else {
		toMode(LAYOUT_STANDARD);
	}

	// finalize view.
	goToPage();
	fitReader();
}

function toMode( modeId ) {
	if(modeId == layout) return;
	layout = modeId;
	
	if(typeof localStorage !== "undefined") {
		localStorage.setItem("layout",""+layout);
	}

	var container = $("#readerContainer");
	container.removeClass("largeLayout");
	container.removeClass("fitLayout");
	
	$('#plainModeButton').removeClass("active");
	$('#largeModeButton').removeClass("active");
	$('#fitModeButton').removeClass("active");
	
	if(layout == LAYOUT_STANDARD) {
		$('#plainModeButton').addClass("active");
	}
	else if(layout == LAYOUT_LARGE) {
		container.addClass("largeLayout");
		$('#largeModeButton').addClass("active");
	}
	else if(layout == LAYOUT_FIT) {
		container.addClass("fitLayout");
		$('#fitModeButton').addClass("active");
	}
}

function fitReader( imgW , imgH ) {

	if(layout == LAYOUT_STANDARD || layout == LAYOUT_LARGE) {
		readerView.css({
			"width":"",
			"height":""
		});
		return;
	}

	var topBar = $('.topBar');
	var topbarSize = parseInt(topBar.css("height")) + parseInt(topBar.css("margin-bottom"));
	var windowWidthLeft = window.innerWidth;
	var windowHeightLeft = window.innerHeight - topbarSize;
	if(!imgW) imgW = readerView[0].width;
	if(!imgH) imgH = readerView[0].height;
	
	if(imgW == 0 || imgH == 0) {
		setTimeout(fitReader,1);
		return;
	}
	
	var nW = 0;
	var nH = 0;
	
	var s = windowHeightLeft / imgH;
	if(s*imgW > windowWidthLeft) {
		nW = windowWidthLeft;
		s = windowWidthLeft / imgW;
		nH = parseInt(s*imgH);
	}
	else {
		nW = parseInt(s*imgW);
		nH = windowHeightLeft;
	}
	
	readerView.css({
		"width":nW+"px",
		"height":nH+"px"
	});
	
}

// progress to next page
function nextPage() {
	pageIndex++;
	if(pageIndex >= chapterData.pages.length) {
		// redirect to next chapter.
		window.location.href = "/reader/?name="+seriesName+"&ch="+(chapterNumber+1);
		return;
	}
	
	goToPage();
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
	
	goToPage();
}

function goToPage() {
	var pageUrl = buildPageUrl(pageIndex);
	var img = new Image();
	var spinner = $('#spinnerIcon');
	spinner.show();
	img.onload = function() {
		readerView.attr("src",img.src);
		if(layout == LAYOUT_FIT) fitReader(img.width,img.height);
		scrollUp();
		window.location.hash = "#"+pageIndex;
		spinner.hide();
	}
	img.src = pageUrl;
	
	// remove previous page indicator
	var lines = $(".pageScrollLine");
	lines.removeClass("enabled");
	
	// add new page indicator
	var pageLine = $(".pageline"+pageIndex);
	pageLine.addClass("enabled");
	pageLine[0].parentNode.parentNode.scrollTop = pageLine[0].offsetTop - pageLine[0].parentNode.offsetTop;
}

/**
 * scroll up animation performed on page change.
 */
function scrollUp() {
	var scrollTo = (window.pageYOffset > 60) ? 62 : 0;
	$("html, body").animate({ scrollTop: scrollTo+"px" });
}

// helper function for building page url.
function buildPageUrl( pageNumber ) {
	var str = chapterData.pages[pageNumber];
	str = "/" + str;
	while(str.indexOf("\\") != -1) str = str.replace("\\","/");
	return str;
}
