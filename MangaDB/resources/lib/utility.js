/* generic functionality. */

// redict correctly if this is not an iframed.
if(self==top) {
	var url = window.location.protocol + "//" + window.location.host + "/?url=" + window.location.pathname + window.location.search;
	if(window.location.hash) url += "&hash="+window.location.hash.replace("#","");
	window.location.href = url;
}

/* helper functions */

function gup( name, url ) {
	if (!url) url = location.href
	name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
	var regexS = "[\\?&]"+name+"=([^&#]*)";
	var regex = new RegExp( regexS );
	var results = regex.exec( url );
	return results == null ? "" : results[1];
}

function isAndroid() {
	return navigator.userAgent.toLowerCase().indexOf("android") != -1;
}

/* shared topbar functionality */

function setupBaseTopbarFunctionality() {
	var menuBtn = $("#menuBtn");
	menuBtn.bind("click",function(){
		toggleMenu();
	});
	
	var popupMouseEater = $("#popupMouseEater");
	popupMouseEater.bind("click",function(){
		toggleMenu();
	});
	
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
		$("#menuDrop").css("font-size",fs+"px");
	};
	
	$(window).bind("resize",resizeFunc);
	resizeFunc();
}

function toggleMenu() {
	var menuDrop = $("#menuDrop");
	var menuImg = $("#menuImg");
	var popupMouseEater = $("#popupMouseEater");
	var isVisible = menuDrop.is(":visible");
	if(isVisible) {
		menuDrop.hide();
		menuImg.css("transform","");
		popupMouseEater.hide();
	} else {
		menuDrop.show();
		menuImg.css("transform","rotate(90deg)");
		popupMouseEater.show();
	}
}

function toggleFullScreen( elem ) {
	if(!elem) elem = parent.document.documentElement;
	if (!parent.document.fullscreenElement &&    // alternative standard method
		!parent.document.mozFullScreenElement && !parent.document.webkitFullscreenElement && !parent.document.msFullscreenElement ) {  // current working methods
		if (elem.requestFullscreen) {
			elem.requestFullscreen();
		} else if (elem.msRequestFullscreen) {
			elem.msRequestFullscreen();
		} else if (elem.mozRequestFullScreen) {
			elem.mozRequestFullScreen();
		} else if (elem.webkitRequestFullscreen) {
			elem.webkitRequestFullscreen(Element.ALLOW_KEYBOARD_INPUT);
		}
	} else {
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
}
