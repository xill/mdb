/* generic functionality. */

// redict correctly if this is not an iframed.
if(self==top) {
	var url = window.location.protocol + "//" + window.location.host + "/?url=" + window.location.pathname + window.location.search;
	if(window.location.hash) url += "&hash="+window.location.hash.replace("#","");
	window.location.href = url;
}

/* helper functions */

/**
 * Read url parameter
 * 
 * @param {String} name - name of the parameter
 * @param {String} url - url to parse from. [optional]
 */
function gup( name, url ) {
	if (!url) url = location.href
	name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
	var regexS = "[\\?&]"+name+"=([^&#]*)";
	var regex = new RegExp( regexS );
	var results = regex.exec( url );
	return results == null ? "" : results[1];
}

/**
 * @return {boolean} - true if running on android device. false otherwise.
 */
function isAndroid() {
	return navigator.userAgent.toLowerCase().indexOf("android") != -1;
}

function isMobile() {
	return navigator.userAgent.toLowerCase().indexOf("mobile") != -1;
}

/* shared topbar functionality */

/**
 * Initialize topbar in current view.
 */
function setupBaseTopbarFunctionality() {

	if(isAndroid() || isMobile()) {
		$("body").addClass("mobile");
	}
	
	// set menu button click event.
	var menuBtn = $("#menuBtn");
	menuBtn.bind("click",function(){
		toggleMenu();
	});
	
	// set mouse eater click event.
	var popupMouseEater = $("#popupMouseEater");
	popupMouseEater.bind("click",function(){
		toggleMenu();
	});
	
	// button for showing options.
	var readerOptionLink = $("#readerOptionLink");
	readerOptionLink.bind("click",toggleOptions);
	
	// button for hiding options.
	var optionBackBtn = $("#optionBackBtn");
	optionBackBtn.bind("click",toggleOptions);
	
	// set fullscreen button
	var fsButton = $(".fsButton");
	var fsIcon = $("#fsIcon");
	fsButton.bind("click",function(){
		toggleFullScreen();
		if (!document.fullscreenElement &&    // alternative standard method
		!document.mozFullScreenElement && !document.webkitFullscreenElement && !document.msFullscreenElement ) {  // current working methods
			// not in fullscreen.
			fsIcon.attr("src","../images/enterfs.png");
		}
		else {
			fsIcon.attr("src","../images/exitfs.png");
		}
	});
	
	// topbar resize function
	var resizeFunc = function() {
		var fs = (window.innerWidth/400 * 16);
		if(fs > 16) fs = 16;
		$("body").css("font-size",fs+"px");
	};
	$(window).bind("resize",resizeFunc);
	resizeFunc();
}

/**
 * Menu button toggle function
 */
function toggleMenu() {
	var optionTab = $("#optionTab");
	var menuDrop = $("#menuDrop");
	var menuImg = $("#menuImg");
	var popupMouseEater = $("#popupMouseEater");
	var isVisible = menuDrop.is(":visible");
	if(isVisible) {
		optionTab.hide();
		menuDrop.hide();
		menuImg.css("transform","");
		popupMouseEater.hide();
	} else {
		menuDrop.show();
		menuImg.css("transform","rotate(90deg)");
		popupMouseEater.show();
	}
}

/**
 * Options panel toggle function
 */
function toggleOptions() {
	var optionTab = $("#optionTab");
	if(optionTab.is(":visible")) {
		// hide options and show menu.
		optionTab.hide();
	}
	else {
		// hide menu and show options.
		optionTab.show();
	}
}

/**
 * Fullscreen button toggle function
 * 
 * @param {DomElement} elem - element to fullscreen. [optional]
 */
function toggleFullScreen( elem ) {
	if(!elem) elem = parent.document.documentElement;
	if (!parent.document.fullscreenElement &&    // alternative standard method
		!parent.document.mozFullScreenElement && !parent.document.webkitFullscreenElement && !parent.document.msFullscreenElement ) {  // current working methods
		enterFullScreen(elem);
	} else {
		exitFullScreen();
	}
}

function enterFullScreen(elem) {
	if (elem.requestFullscreen) {
		elem.requestFullscreen();
	} else if (elem.msRequestFullscreen) {
		elem.msRequestFullscreen();
	} else if (elem.mozRequestFullScreen) {
		elem.mozRequestFullScreen();
	} else if (elem.webkitRequestFullscreen) {
		elem.webkitRequestFullscreen(Element.ALLOW_KEYBOARD_INPUT);
	}
}

function exitFullScreen() {
	if (parent.document.exitFullscreen) {
		parent.document.exitFullscreen();
	} else if (parent.document.msExitFullscreen) {
		parent.document.msExitFullscreen();
	} else if (document.mozCancelFullScreen) {
		parent.document.mozCancelFullScreen();
	} else if (document.webkitExitFullscreen) {
		parent.document.webkitExitFullscreen();
	}
}
