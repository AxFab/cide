// CIDE Error Widget JS
//
// Gabriel Féron
// January 2012

/**
 * Error widget
 */
cideWidgets.Error = function() {

	this.name = "error";

	this.onLoad = function() {

		cide.socket.registerEvent("unexpectedError", this, this.onUnexpectedError);
		cide.socket.registerEvent("userExit", this, this.onUserExit);

		// Init with bootstrap
		// $("#widget-error-box").alert();

		$("#widget-error-box .close").click(hideBox);
	}

	this.unLoad = function() {

	}

	this.onUnexpectedError = function(self, msg) {

		self.displayErrorBox(msg.body.object);
	}
	
	this.onUserExit = function(self, msg) {

		self.displayWarningBox("<b>Oops!</b> It seems that user <b>"+msg.body.object+"</b> just rage-quit (return 1, Errno: 22)");
	}

	this.displayErrorBox = function(msg) {

		$('#widget-error-box').hide();
		$('#widget-error-box').removeClass();
		$('#widget-error-box').addClass('alert-message');
		$('#widget-error-box').addClass('error');
		$('#widget-error-message').html('<b>Ouch!</b> '+msg);
		showBox();
	}

	this.displayWarningBox = function(msg) {

		$('#widget-error-box').hide();
		$('#widget-error-box').removeClass();
		$('#widget-error-box').addClass('alert-message');
		$('#widget-error-box').addClass('warning');
		$('#widget-error-message').html('<b>Mmm...</b>' +msg);
		showBox();
	}
	
	this.displayInfoBox = function(msg) {

		$('#widget-error-box').hide();
		$('#widget-error-box').removeClass();
		$('#widget-error-box').addClass('alert-message');
		$('#widget-error-box').addClass('info');
		$('#widget-error-message').html('<b>Hey there,</b> '+msg);
		showBox();
	}
	
	this.displaySuccessBox = function(msg) {

		$('#widget-error-box').hide();
		$('#widget-error-box').removeClass();
		$('#widget-error-box').addClass('alert-message');
		$('#widget-error-box').addClass('success');
		$('#widget-error-message').html('<b>Yeah!</b> '+msg);
		showBox();
	}

	var showBox = function() {

		$('#layout-left').animate({
			'top' : '94'
		}, 200);
		$('#layout-center').animate({
			'top' : '94'
		}, 200);
		$('#widget-error-box').fadeIn(500);
	}

	var hideBox = function() {

		$('#widget-error-box').hide();
		$('#layout-left').animate({
			'top' : '50'
		}, 200);
		$('#layout-center').animate({
			'top' : '50'
		}, 200);
	}
};

cide.manager.registerWidget(new cideWidgets.Error());
