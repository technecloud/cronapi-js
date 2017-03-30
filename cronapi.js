(function() {
	'use strict';

	this.cronapi = {};
	/**
	 * @category Conversion
	 * @categorySynonymous Conversão|Convert
	 */
	this.cronapi.conversion = {};
	/**
	 * @type function
	 * @name Texto para texto binário
	 * @nameSynonymous asciiToBinary
	 * @description Função para converter texto para texto binário
	 * @param {string} astring - The x value.
	 */
	this.cronapi.conversion.asciiToBinary = function(astring) {
		var binary = "";
		if (astring.length > 0) {
			for (var i = 0; i < astring.length; i++) {
				var value = astring.charCodeAt(i);
				for (var j = 7; j >= 0; j--) {
					binary += ((value >> j) & 1);
				}
			}
		}
		return binary;
	};

	this.cronapi.conversion.teste = function(astring) {
		parseBoolean(astring);
	};

	//Private functions
	var parseBoolean = function(value) {
		if (value == null || typeof value == "undefined") {
			return false;
		}

		if (typeof value == "boolean") {
			return value;
		}

		if (!value.toString) {
			return false;
		}

		value = trim(value.toString().toUpperCase());

		return value == "1" || value == "S" || value == "V" || value == "T"
				|| value == "Y" || value == "TRUE" || value == "VERDADE"
				|| value == "VERDADEIRO" || value == "YES" || value == "SIM";
	};

	var getHTTPObject = function() {
		if (typeof XMLHttpRequest != 'undefined') {
			return new XMLHttpRequest();
		}
		try {
			return new ActiveXObject("Msxml2.XMLHTTP");
		} catch (e) {
			try {
				return new ActiveXObject("Microsoft.XMLHTTP");
			} catch (e) {
			}
		}
		return false;
	}
  
	var getHTTPObjectXML = function() {
		var http_request;
		if (window.XMLHttpRequest) { // Mozilla, Safari,...
			http_request = new XMLHttpRequest();
			if (http_request.overrideMimeType) {
				http_request.overrideMimeType('text/xml');
			}
		} else if (window.ActiveXObject) { // IE
			try {
				http_request = new ActiveXObject("Msxml2.XMLHTTP");
			} catch (e) {
				try {
					http_request = new ActiveXObject("Microsoft.XMLHTTP");
				} catch (e) {
				}
			}
		}
		if (!http_request) {
			interactionError('Cannot create XMLHTTP instance');
			return false;
		}

		return http_request;
	}

	var translateAcentos = function(aValue) {
		var CHR_ACENTUADA = "������������������������������������������������";
		var CHR_NAO_ACENTUADA = "aeiouaeiouaeiouaocnaeiouAEIOUAEIOUAEIOUAOCNAEIOU";

		var idx, idxpos;
		var result = "";

		for (idx = 0; idx < aValue.length; idx++) {
			idxpos = CHR_ACENTUADA.indexOf(aValue.charAt(idx));
			if (idxpos != -1) {
				result += CHR_NAO_ACENTUADA.charAt(idxpos);
			} else {
				result += aValue.charAt(idx);
			}
		}

		return result;
	}

	var fix = function (v) {
		return v < 10 ? '0' + v : v;
	}

	var getDateTime = function() {
		var d = new Date();
		return fix(d.getDate()) + "/" + fix(d.getMonth() + 1) + "/"
				+ d.getFullYear();
	}


	var isEvent = function(object) {
		var isEvent = false;
		try {
			if (!isNullable(object)) {
				if (object.isEvent) {
					isEvent = true;
				} else {
					try {
						isEvent = (typeof object.preventDefault != "undefined");
					} catch (e) {
					}

					try {
						isEvent = (isEvent || typeof object.cancelBubble != "undefined");
					} catch (e) {
					}

					try {
						isEvent = (isEvent || typeof object.stopPropagation != "undefined");
					} catch (e) {
					}
				}
			}
		} catch (e) {
		}
		return isEvent;
	}

	var arrayRemove = function(array, value) {
		var i = arrayIndexOf(array, value);
		if (i != -1) {
			array.splice(i, 1);
		}
	}

	var arrayIndexOf = function(array, value, type) {
		var isNumber = type == 'number' || type == 'double'
				|| type == "integer" || type == "float";
		if (isNumber) {
			try {
				value = parseFloat(value);
			} catch (e) {
				isNumber = false;
				//Abafa erro de conversão
			}
		}
		var found = false;
		var index = 0;
		while (!found && index < array.length) {
			var v2 = array[index];
			if (isNumber) {
				try {
					v2 = parseFloat(v2);
				} catch (e) {
					//Abafa erro de convers�o
				}
			}
			if (value == v2) {
				found = true;
			} else {
				index++;
			}
		}
		return (found) ? index : -1;
	}

	window.removeCurrentWindowFromOpener = function() {
		try {
			var p = getOpenerWindow(window);
			if (p && p.children) {
				arrayRemove(p.children, window);
				if (p.children.length == 0) {
					p.children = null;
				}
			}
		} catch (e) {
			//
		}
	}

	function MM_openBrWindow(theURL, winName, features) { //v2.0
		if (winName)
			winName = winName.toString().replace(/\(|\)/g, "_");
		var w = window.open(theURL, winName, features);
		try {
			if (w) {
				if (w.setFocus)
					w.setFocus();
				else
					w.focus();
			}
		} catch (ex) {
		}
		return w;
	}

	function MM_findObj(n, d) { //v4.01
		var p, i, x;

		if (!d)
			d = document;

		if ((p = n.indexOf("?")) > 0 && parent.frames.length) {
			d = parent.frames[n.substring(p + 1)].document;
			n = n.substring(0, p);
		}

		if (!(x = d[n]) && d.all)
			x = d.all[n];

		for (i = 0; !x && i < d.forms.length; i++)
			x = d.forms[i][n];

		for (i = 0; !x && d.layers && i < d.layers.length; i++)
			x = MM_findObj(n, d.layers[i].document);

		if (!x && d.getElementById)
			x = d.getElementById(n);

		return x;
	}

	function MM_showHideLayers() { //v6.0
		var i, p, v, obj, args = MM_showHideLayers.arguments;
		for (i = 0; i < (args.length - 2); i += 3)
			if ((obj = MM_findObj(args[i])) != null) {
				v = args[i + 2];
				if (obj.style) {
					obj = obj.style;
					v = (v == 'show') ? 'visible' : (v == 'hide')
							? 'hidden'
							: v;
				}
				obj.visibility = v;
			}
	}

	function visibility(obj, v) {
		if (obj.style) {
			obj = obj.style;
			v = (v == 'show') ? 'visible' : (v == 'hide') ? 'hidden' : v;
		}
		obj.visibility = v;
	}

	function MM_changeProp(objName, x, theProp, theValue) { //v6.0
		var obj = MM_findObj(objName);
		if (obj && (theProp.indexOf("style.") == -1 || obj.style)) {
			if (theValue == true || theValue == false)
				eval("obj." + theProp + "=" + theValue);
			else
				eval("obj." + theProp + "='" + theValue + "'");
		}
	}

	function ruleOpenForm(id, frm, w, h, mode) {
		var left = (parent.screen.width - w) / 2;
		var top = (parent.screen.height - h - 60) / 2;
		var win = MM_openBrWindow(
				'form' + PAGES_EXTENSION + '?sys=' + id
						+ '&action=openform&formID=' + frm
						+ '&align=0&goto=-1&filter=&mode=' + mode,
				frm,
				'toolbar=no,location=no,status=yes,menubar=no,scrollbars=no,resizable=no,width='
						+ w + ',height=' + h + ',left=' + left + ',top=' + top);
		win.doOnLoad = true;
		win.focus();
	}

	function showFormHelp(id, frm) {
		var left = (screen.width - 300) / 2;
		var top = (screen.height - 360) / 2;
		MM_openBrWindow(
				'helpform' + PAGES_EXTENSION + '?sys=' + id + '&formID=' + frm,
				'WFRHELPFORM',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=no,width=300,height=360,left='
						+ left + ',top=' + top)
	}

	function getPath(url) {
		var regexS = "(.*/).*\\?.*";
		var regex = new RegExp(regexS);
		var results = regex.exec(url);
		if (results == null)
			return "";
		else
			return results[1];
		return url;
	}

	function getParameterByName(name, url) {
		if (!url) {
			url = window.location.href;
		}
		try {
			name = name.replace(/[\[\]]/g, "\\$&");
			var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"), results = regex
					.exec(url);
			if (!results)
				return null;
			if (!results[2])
				return '';
			return decodeURIComponent(results[2].replace(/\+/g, " "));
		} catch (e) {
			return "";
		}
	}

	function openForm(properties) {
		if (!properties.popup && getParameterByName("popup") != "true") {
			if (getParameterByName("formID")) {
				properties.fromForm = getParameterByName("formID");
			}

			properties.openerId = window._JQUERY_WINDOW_ID;

			return _JQUERY_WINDOW_FIND_CONTROLLER().openForm(properties);
		} else {
			var left = (screen.width - properties.width) / 2;
			var top = (screen.height - properties.height) / 2;
			var url = 'form'
					+ PAGES_EXTENSION
					+ '?sys='
					+ properties.sys
					+ '&action=openform&formID='
					+ properties.form
					+ '&align=0'
					+ (!isNullable(properties.mode) ? '&mode='
							+ properties.mode : '')
					+ '&goto='
					+ (!isNullable(properties.gotoRow)
							? properties.gotoRow
							: -1)
					+ '&filter='
					+ (!isNullable(properties.filter) ? properties.filter : '')
					+ '&scrolling='
					+ (properties.scrollbars ? 'yes' : 'no')
					+ (!isNullable(properties.onClose) ? '&popup=true&onClose='
							+ properties.onClose : '');

			if (!properties.centralized) {
				left = properties.posX ? properties.posX : 0;
				top = properties.posY ? properties.posY : 0;
			}

			var toolbar = properties.toolbar ? 'yes' : 'no';
			var location = properties.location ? 'yes' : 'no';
			var status = properties.status ? 'yes' : 'no';
			var menubar = properties.menubar ? 'yes' : 'no';
			var scrollbars = properties.scrollbars ? 'yes' : 'no';
			var resizable = properties.resizable ? 'yes' : 'no';

			var target = !isNullable(properties.target)
					? properties.target
					: '';
			var params = 'toolbar=' + toolbar + ',location=' + location
					+ ',status=' + status + ',menubar=' + menubar
					+ ',scrollbars=' + scrollbars + ',resizable=' + resizable
					+ ',width=' + properties.width + ',height='
					+ properties.height + ',left=' + left + ',top=' + top;
			if (properties.modal && window.showModalDialog) {
				var realHeight = parseInt(properties.height);
				var realWidth = parseInt(properties.width);

				var values = new Object();
				values.parentWindow = window;

				var px = "";
				if (IE) {
					px = "px";
				}

				// Necess�rio pois o IE8 n�o necessita do c�lculo abaixo, entretanto as vers�es anteriores precisam
				var IE8 = (navigator.userAgent.indexOf("MSIE 8") > -1);

				if (IE && !IE8) {
					var diffHeight = screen.Height - screen.availHeight;
					var diffWidth = screen.Width - screen.availWidth;
					// Verifica��o necess�ria, pois o IE8+, quando em modo de compatibilidade, torna-se IE7
					if (!(isNullable(diffHeight) || isNullable(diffWidth))) {
						realHeight += diffHeight;
						realWidth += diffWidth;
					}
				}

				window.showModalDialog(url, values, "dialogHeight:"
						+ realHeight + px + ";dialogWidth:" + realWidth + px
						+ ";dialogleft:" + left + px + ";dialogtop:" + top + px
						+ ";center=yes;resizable:" + resizable + ";status:"
						+ status);
			} else if (properties.newWindow) {
				var w = MM_openBrWindow(url, target, params);
				if (properties.returnWindow) {
					return w;
				}
			} else {
				window.location = url;
				if (properties.returnWindow) {
					return window;
				}
			}
		}
	}

	var nativeOpenForm = openForm;

	function openWFRForm(id, frm, w, h, mode, codigo, codFormComp) {
		var janela;
		if (typeof (codigo) != "undefined") {
			janela = openWFRFilterForm2(id, frm, w, h, '', mode, codigo,
					codFormComp);
		} else {
			janela = openWFRFilterForm(id, frm, w, h, '', mode);
		}
	}

	function openWFRFilterForm2(id, frm, w, h, filter, mode, codigo,
			codFormComp) {
		var smode = '';
		if (typeof (mode) != "undefined")
			smode = '&mode=' + mode;
		var left = (screen.width - w) / 2;
		var top = (screen.height - h - 60) / 2;
		var janela = MM_openBrWindow(
				'form' + PAGES_EXTENSION + '?codFormComp=' + codFormComp
						+ '&codigo=' + codigo + '&sys=' + id
						+ '&action=openform&formID=' + frm
						+ '&align=0&goto=-1&filter=' + filter + smode,
				frm,
				'toolbar=no,location=no,status=yes,menubar=no,scrollbars=no,resizable=no,width='
						+ w + ',height=' + h + ',left=' + left + ',top=' + top);
		janela.doOnLoad = true;

		return janela;
	}

	function openWFRFilterForm(id, frm, w, h, filter, mode) {
		var smode = '';
		if (typeof (mode) != "undefined")
			smode = '&mode=' + mode
		var left = (screen.width - w) / 2;
		var top = (screen.height - h - 60) / 2;

		var janela = MM_openBrWindow(
				'form' + PAGES_EXTENSION + '?sys=' + id
						+ '&action=openform&formID=' + frm
						+ '&align=0&goto=-1&filter=' + filter + smode,
				frm,
				'toolbar=no,location=no,status=yes,menubar=no,scrollbars=no,resizable=no,width='
						+ w + ',height=' + h + ',left=' + left + ',top=' + top);
		janela.doOnLoad = true;

		return janela;
	}

	function openWFRFilterFormXY(id, frm, w, h, filter, mode, x, y) {
		var smode = '';
		if (typeof (mode) != "undefined")
			smode = '&mode=' + mode
		var left = x ? x : (screen.width - w) / 2;
		var top = y ? y : (screen.height - h - 60) / 2;
		var janela = MM_openBrWindow(
				'form' + PAGES_EXTENSION + '?sys=' + id
						+ '&action=openform&formID=' + frm
						+ '&align=0&goto=-1&filter=' + filter + smode,
				frm,
				'toolbar=no,location=no,status=yes,menubar=no,scrollbars=no,resizable=no,width='
						+ w + ',height=' + h + ',left=' + left + ',top=' + top);
		janela.doOnLoad = true;
		return janela;
	}

	function updateFormQuery(id, frm) {
		var gt = 1 + parseInt(gridrn) + parseInt(gridini);
		var u = 'form.do?sys=' + id + '&action=form&param=goto&formID=' + frm
				+ '&align=-1&mode=&goto=' + gt + '&filter=';
		var mform = mainform.document;
		mainform.getAndEval(u);
		mform.n.normal();
		// Seleciona a aba
		mform.t.tabs[0].select();
	}

	function openUpload(id, frm, c, crip, showRemove) {
		var removeButton = "";
		if (showRemove) {
			removeButton = "&showRemove=true";
		}

		w = 350;
		h = 100;
		var left = (screen.width - w) / 2;
		var top = (screen.height - h) / 2;
		MM_openBrWindow('upload' + PAGES_EXTENSION + '?sys=' + id + '&formID='
				+ frm + '&comID=' + c + removeButton, 'UPLOAD' + frm,
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no,width='
						+ w + ',height=' + h + ',left=' + left + ',top=' + top);
	}

	function openCapture(id, frm, c) {
		var w = 320;
		var h = 311;
		var left = (screen.width - w) / 2;
		var top = (screen.height - h) / 2;
		var winid = 'ID' + parseInt((Math.random() * 9999999));
		MM_openBrWindow('camera' + PAGES_EXTENSION + '?detectflash=false&sys='
				+ id + '&formID=' + frm + '&comID=' + c, 'UPLOAD' + winid,
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no,width='
						+ w + ',height=' + h + ',left=' + left + ',top=' + top);
	}

	function openDigitalCapture(id, frm, c, crip) {
		w = 200;
		h = 200;
		var left = (screen.width - w) / 2;
		var top = (screen.height - h) / 2;
		MM_openBrWindow('digitalcapture' + PAGES_EXTENSION + '?sys=' + id
				+ '&formID=' + frm + '&comID=' + c + '&crip=' + crip,
				'WFRCAPTURE',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no,width='
						+ w + ',height=' + h + ',left=' + left + ',top=' + top);
	}

	function openQueryDigitalCapture(id, field) {
		w = 2;
		h = 2;
		var left = (screen.width - w) / 2;
		var top = (screen.height - h) / 2;
		MM_openBrWindow('digitalcapturequery' + PAGES_EXTENSION + '?sys=' + id
				+ '&field=' + field, 'WFRCAPTURE',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no,width='
						+ w + ',height=' + h + ',left=' + left + ',top=' + top);
	}

	function openLogonDigitalCapture(id, dataConnection) {
		w = 2;
		h = 2;
		var left = (screen.width - w) / 2;
		var top = (screen.height - h) / 2;
		MM_openBrWindow('digitalcapturelogon' + PAGES_EXTENSION + '?sys=' + id
				+ '&dataConnection=' + dataConnection, 'WFRCAPTURE',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no,width='
						+ w + ',height=' + h + ',left=' + left + ',top=' + top);
	}

	function openActionDigitalCapture(id, p, type, to, sw, msg) {
		w = 2;
		h = 2;
		var left = (screen.width - w) / 2;
		var top = (screen.height - h) / 2;
		MM_openBrWindow('digitalcaptureaction' + PAGES_EXTENSION + '?sys=' + id
				+ '&procedure=' + p + '&type=' + type + '&timeout=' + to
				+ '&showwindow=' + sw + '&message=' + msg, 'WFRCAPTURE',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no,width='
						+ w + ',height=' + h + ',left=' + left + ',top=' + top);
	}

	function WFRZoomImg(url, w, h) {
		var left = (screen.width - w) / 2;
		var top = (screen.height - h) / 2;

		MM_openBrWindow(
				'zoom' + PAGES_EXTENSION + '?' + url,
				'WFRZOOM',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=yes,width='
						+ w + ',height=' + h + ',left=' + left + ',top=' + top);
	}

	function customZoomImage(url, width, height) {
		var left = (screen.width - width) / 2;
		var top = (screen.height - height) / 2;

		return MM_openBrWindow(
				'customZoom' + PAGES_EXTENSION + '?' + url,
				'CustomZoom',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=yes,width='
						+ width
						+ ',height='
						+ height
						+ ',left='
						+ left
						+ ',top=' + top);
	}

	function openFormAccess(sys, frm, name) {
		w = 790;
		h = 450;
		var left = (screen.width - w) / 2;
		var top = (screen.height - h) / 2;
		MM_openBrWindow('comaccess' + PAGES_EXTENSION + '?type=F&sys=' + sys
				+ '&form=' + frm + '&name=' + name, 'COMACCESS',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no,width='
						+ w + ',height=' + h + ',left=' + left + ',top=' + top);
	}

	function openComAccess(sys, frm, com, name, comName) {
		w = 790;
		h = 450;
		var left = (screen.width - w) / 2;
		var top = (screen.height - h) / 2;
		MM_openBrWindow('comaccess' + PAGES_EXTENSION + '?type=C&sys=' + sys
				+ '&form=' + frm + '&com=' + com + '&name=' + name
				+ '&comName=' + comName, 'COMACCESS',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no,width='
						+ w + ',height=' + h + ',left=' + left + ',top=' + top);
	}

	function openReportAccess(sys, report, name) {
		w = 790;
		h = 450;
		var left = (screen.width - w) / 2;
		var top = (screen.height - h) / 2;
		MM_openBrWindow('comaccess' + PAGES_EXTENSION + '?type=R&sys=' + sys
				+ '&report=' + report + '&name=' + name, 'COMACCESS',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no,width='
						+ w + ',height=' + h + ',left=' + left + ',top=' + top);
	}

	function openMenuAccess(sys, menu, form, name, report) {
		w = 790;
		h = 450;
		var left = (screen.width - w) / 2;
		var top = (screen.height - h) / 2;
		if (!report)
			report = '';
		MM_openBrWindow('comaccess' + PAGES_EXTENSION + '?type=M&sys=' + sys
				+ '&menu=' + menu + '&name=' + name + '&menuForm=' + form
				+ '&menuReport=' + report, 'COMACCESS',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no,width='
						+ w + ',height=' + h + ',left=' + left + ',top=' + top);
	}

	function openWFRDate(sys, f, year, month) {
		var x = tempX;
		var y = tempY + 20;

		if (x + 250 > screen.width)
			x = screen.width - 260;

		if (y + 150 > screen.height - 70)
			y = screen.height - 220;

		MM_openBrWindow(
				'getdate?sys=' + sys + '&month=' + month + '&year=' + year
						+ '&field=' + f,
				'WFRDate',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no,width=250,height=150,top='
						+ y + ',left=' + x);
	}

	function openWFRAdvancedFields(qs) {
		var left = (screen.width - 200) / 2;
		var top = (screen.height - 300) / 2;
		MM_openBrWindow(
				'advanced_query_fields' + PAGES_EXTENSION + '?' + qs,
				'WFRAdvancedFields',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=no,width=200,height=300,left='
						+ left + ',top=' + top);
	}

	function openWFRAdvancedQuerySaved(qs) {
		var left = (screen.width - 250) / 2;
		var top = (screen.height - 300) / 2;
		MM_openBrWindow(
				'advanced_query_saved' + PAGES_EXTENSION + '?' + qs,
				'WFRAdvancedSaved',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=no,width=250,height=300,left='
						+ left + ',top=' + top);
	}

	function openWFRPassword(sys) {
		var width = 387;
		var height = 210;
		var left = (screen.width - width) / 2;
		var top = (screen.height - height) / 2;
		MM_openBrWindow(
				'password' + PAGES_EXTENSION + '?sys=' + sys,
				'WFRPassword',
				'popup=yes,toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no,width='
						+ width
						+ ',height='
						+ height
						+ ',left='
						+ left
						+ ',top=' + top);
	}

	function openWFRPassword2(sys, msgKey) {
		var width = 387;
		var height = 210;
		var left = (screen.width - width) / 2;
		var top = (screen.height - height) / 2;
		MM_openBrWindow('password' + PAGES_EXTENSION + '?sys=' + sys
				+ '&msgKey=' + msgKey, 'WFRPassword',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no,width='
						+ width + ',height=' + height + ',left=' + left
						+ ',top=' + top);
	}

	function pt(v) {
		var r = parseInt(v.replace('px', '').replace('pt', ''));
		if (isNaN(r))
			return document.body.clientHeight;
		else
			return r;
	}

	function openWFRProgressbar(sys, text) {
		var left = (parent.screen.width - 300) / 2;
		var top = (parent.screen.height - 120) / 2;
		return MM_openBrWindow(
				'progressbar' + PAGES_EXTENSION + '?sys=' + sys + '&text='
						+ text,
				'WFRProgressbar' + sys,
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no,width=300,height=120,left='
						+ left + ',top=' + top);
	}

	function openWFRReport(sys, rid, title, ptf) {
		var left = (screen.width - 440) / 2;
		var top = (screen.height - 440) / 2;
		MM_openBrWindow(
				'report' + PAGES_EXTENSION + '?sys=' + sys + '&reportID=' + rid
						+ '&title=' + URLEncode(title) + '&ptf=' + ptf,
				'WFRReport' + rid,
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=no,width=440,height=440,left='
						+ left + ',top=' + top);
	}

	function openWFRReport2(sys, rid, fid, title, useFormFields, filter) {
		var left = (screen.width - 445) / 2;
		var top = (screen.height - 440) / 2;
		var winname = 'WFRReport' + rid;
		if (isNaN(rid)) {
			winname = 'WFRReport' + sys;
		}
		MM_openBrWindow(
				'report' + PAGES_EXTENSION + '?sys=' + sys + '&reportID='
						+ URLEncode(rid) + '&formID=' + fid + '&title=' + title
						+ '&useFormFields=' + useFormFields + '&filter='
						+ filter,
				winname,
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no,width=445,height=440,left='
						+ left + ',top=' + top);
	}

	function openWFRReportFinal(rid, file, type, nopopup) {
		var scroll = "yes";
		var menu = "yes";
		if (type == 'PDF') {
			scroll = "no";
			menu = "no";
		}

		if (type == 'REM' || type == 'TXT' || type == 'XLS' || type == 'RTF') {
			window.location = file;
		} else {
			var win = window;
			if (!document.body) {
				document.writeln("<body></body>");
			}

			// #### Caso a largura da tela seja 0 e exista parent, ent�o obt�m-se o window deste
			if (document.body.clientWidth == 0 && window.parent) {
				win = window.parent;
			}

			if (nopopup) {
				win.location = file;
			} else {
				var w, h;
				if (parent && parent.screen) {
					w = parent.screen.availWidth;
					h = parent.screen.availHeight;
				} else {
					w = screen.availWidth;
					h = screen.availHeight;
				}

				if (win.parent) {
					win = win.parent;
				}

				if (win.reportOptWindow) {
					win.moveTo(0, 0);
					win.resizeTo(0, 0);
					win.resizeTo(w, h);
					win.location = 'about:blank';
					win.location = file;
				} else {
					MM_openBrWindow(file, 'WFRReportOpen' + rid,
							'toolbar=no,location=no,status=no,menubar=' + menu
									+ ',scrollbars=' + scroll
									+ ',resizable=no,width=' + w + ',height='
									+ h + ',left=0,top=0');
				}
			}
		}
	}

	function openWFRHTML(u) {
		MM_openBrWindow(
				u,
				'WFRReportOpen',
				'toolbar=no,location=no,status=no,menubar=yes,scrollbars=yes,resizable=no,width='
						+ screen.availWidth
						+ ',height='
						+ screen.availHeight
						+ ',left=0,top=0');
	}

	function openWFRLocalReport(u) {
		window.location = u;
	}

	function openWFRExport(sys, f, type) {
		if (!document.navAction)
			MM_openBrWindow(
					'export' + PAGES_EXTENSION + '?sys=' + sys + '&formID=' + f
							+ '&type=' + type,
					'WFRExport',
					'toolbar=yes,location=yes,status=yes,menubar=yes,scrollbars=yes,resizable=yes,width='
							+ screen.availWidth
							+ ',height='
							+ screen.availHeight + ',left=0,top=0');
	}

	function openDefaultValues(sys, fId) {
		var left = (screen.width - 395) / 2;
		var top = (screen.height - 295) / 2;
		MM_openBrWindow(
				'defaultValues' + PAGES_EXTENSION + '?sys=' + sys + '&formID='
						+ fId,
				'WFRDefaultValues',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=no,width=395,height=295,left='
						+ left + ',top=' + top);
	}

	function openWFRImport(sys) {
		var left = (screen.width - 395) / 2;
		var top = (screen.height - 295) / 2;
		MM_openBrWindow(
				'import' + PAGES_EXTENSION + '?sys=' + sys,
				'WFRImport',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=no,width=485,height=350,left='
						+ left + ',top=' + top);
	}

	function openWFRRemGridOrder(sys, f) {
		if (!document.navAction)
			if (confirm($mainform().getLocaleMessage(
					"INFO.GRID_REMOVE_GRID_ORDER")))
				WFRFormComands.location = 'order.do?sys=' + sys + '&formID='
						+ f + '&action=order&field=$';
	}

	function openWFRReportOrder(sys, rid) {
		var left = (screen.width - 627) / 2;
		var top = (screen.height - 248) / 2;
		var w = MM_openBrWindow(
				'order' + PAGES_EXTENSION + '?sys=' + sys + '&reportID=' + rid,
				'WFRReportOrder' + rid,
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no,width=627,height=248,left='
						+ left + ',top=' + top);
	}

	function openWFRHelp(sys, file) {
		parent
				.MM_openBrWindow(
						file,
						'WFRHelp' + sys,
						'toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=no,width='
								+ screen.availWidth
								+ ',height='
								+ screen.availHeight + ',left=0,top=0');
	}

	function openWFRSQLScriptExecute(sys) {
		var url = 'ExecuteScript' + PAGES_EXTENSION + '?sys=' + sys;
		var width = 270;
		var height = 125;
		var left = (screen.width - width) / 2;
		var top = (screen.height - height) / 2;
		parent
				.MM_openBrWindow(
						url,
						'WFRSQLScriptExecute' + sys,
						'toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=no,width='
								+ width
								+ ',height='
								+ height
								+ ',left='
								+ left
								+ ',top=' + top);
	}

	function openWFRConfigureSubconnections(pSys) {
		var lUrl = 'configureSubconnections.do?sys=' + pSys;
		var lWidth = 500;
		var lHeight = 430;
		var lLeft = (screen.width - lWidth) / 2;
		var lTop = (screen.height - lHeight) / 2;
		parent
				.MM_openBrWindow(
						lUrl,
						'WFRConfigureSubconnections' + pSys,
						'toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=no,width='
								+ lWidth
								+ ',height='
								+ lHeight
								+ ',left='
								+ lLeft + ',top=' + lTop);
	}

	function openWFRDataImport(sys, formID) {
		var url = "importadorDados" + PAGES_EXTENSION + "?sys=" + sys
				+ "&formID=" + formID;
		var left = (screen.width - 700) / 2;
		var top = (screen.height - 600) / 2;
		MM_openBrWindow(
				url,
				'FileImport',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=no,width=700,height=600,left='
						+ left + ',top=' + top);
	}

	function openFormQuery(t, sys, fid, tit, w, h) {
		if (!edit && !insert) {
			u = 'basic_query' + PAGES_EXTENSION + '?sys=' + sys + '&formID='
					+ fid + '&title=' + tit + '&width=' + (w - 10) + '&height='
					+ (h - 100);
			MM_setTextOfLayer(
					t,
					'',
					'<iframe name=WFRFormQuery src=\"'
							+ u
							+ '\" width='
							+ w
							+ ' height='
							+ h
							+ ' frameborder=no border=0 marginwidth=0 marginheight=0 scrolling=no></iframe>');
		} else if (insert) {
			interactionInfo(getLocaleMessage("INFO.INCLUDE_MODE_EXIT"));
			queryMode = false;
		} else if (edit) {
			interactionInfo(getLocaleMessage("INFO.EDIT_MODE_EXIT"));
			queryMode = false;
		}
	}

	function $(n, d) {
		return MM_findObj(n, d);
	}

	function $mainform() {
		var r = null;
		try {
			if (parentWindow && parentWindow.mainsystem) {
				r = parentWindow.mainsystem;
			} else if (parentWindow && parentWindow.mainform) {
				r = parentWindow.mainform;
			} else if (window.mainform) {
				r = window.mainform;
			} else {
				r = window;
			}
		} catch (e) {
			r = window;
		}

		return r;
	}

	function $mainframe() {
		try {
			if (parent && parent.mainframe) {
				return parent.mainframe;
			} else {
				return window;
			}
		} catch (e) {
			return window;
		}
	}

	function $controller() {
		return $mainform().controller;
	}

	function $c(component, formID) {
		return $controller().getElementById(component, formID);
	}

	function openFormLog(sys, fid, tit, pkeys, tipoLog) {
		openFullScreen('log_popup' + PAGES_EXTENSION + '?sys=' + sys
				+ '&formID=' + fid + '&title=' + tit + '&pkeys=' + pkeys
				+ '&tipoLog=' + (tipoLog ? tipoLog : '2'), 'Log');
	}

	function setNavText(tx, bc, tc) {
		MM_setTextOfLayer('WFRNavMsg', '',
				'<table width=100% height=100% border=0 cellpadding=0 cellspacing=0 bgcolor='
						+ bc + '><tr><td><center><font color=' + tc + '><b>'
						+ tx + '</b></font></center></td></tr></table>');
	}

	function trim(str) {
		var result = "";
		if (str) {
			result = str.toString().replace(/^\s+|\s+$/g, '');
		}
		return result;
	}

	function executeStoredProcedure(id, frm, name, params) {
		u = 'storedprocedure.do?sys=' + id + '&action=storedprocedure&formID='
				+ frm + '&name=' + name + '&params=' + params;
		WFRFormComands.location = u;
	}

	function executeStoredProcedureRT(id, frm, name, obj) {
		params = '';
		for (j = 0; j < obj.length; j++) {
			o = MM_findObj(obj[j]);
			if (params != '')
				params = params + '$;$';
			params = params + o.value;

		}

		u = 'storedprocedure.do?sys=' + id + '&action=storedprocedure&formID='
				+ frm + '&name=' + name + '&params=' + params;
		WFRFormComands.location = u;
	}

	function executeStoredProcedureAfterSubmit(id, frm, name, params) {
		if (controller.checkRequireds()) {
			MM_findObj('storedProcedureName').value = name;
			MM_findObj('storedProcedureParams').value = params;

			MM_findObj('param').value = 'post';
			MM_findObj('goto').value = formrow;
			MM_findObj('WFRForm').submit();

			MM_findObj('storedProcedureName').value = '';
			MM_findObj('storedProcedureParams').value = '';
		}

	}

	function validate_CPF(s) {
		if (isNaN(s)) {
			return false;
		}
		var i;
		var c = s.substr(0, 9);
		var dv = s.substr(9, 2);
		var d1 = 0;
		for (i = 0; i < 9; i++) {
			d1 += c.charAt(i) * (10 - i);
		}
		if (d1 == 0) {
			return false;
		}
		d1 = 11 - (d1 % 11);
		if (d1 > 9)
			d1 = 0;
		if (dv.charAt(0) != d1) {
			return false;
		}
		d1 *= 2;
		for (i = 0; i < 9; i++) {
			d1 += c.charAt(i) * (11 - i);
		}
		d1 = 11 - (d1 % 11);
		if (d1 > 9)
			d1 = 0;
		if (dv.charAt(1) != d1) {
			return false;
		}
		return true;
	}

	function validate_CGC(s) {
		if (isNaN(s)) {
			return false;
		}
		var i;
		var c = s.substr(0, 12);
		var dv = s.substr(12, 2);
		var d1 = 0;
		for (i = 0; i < 12; i++) {
			d1 += c.charAt(11 - i) * (2 + (i % 8));
		}
		if (d1 == 0)
			return false;
		d1 = 11 - (d1 % 11);
		if (d1 > 9)
			d1 = 0;
		if (dv.charAt(0) != d1) {
			return false;
		}
		d1 *= 2;
		for (i = 0; i < 12; i++) {
			d1 += c.charAt(11 - i) * (2 + ((i + 1) % 8));
		}
		d1 = 11 - (d1 % 11);
		if (d1 > 9)
			d1 = 0;
		if (dv.charAt(1) != d1) {
			return false;
		}
		return true;
	}

	function CPF(v) {

		if (v == '')
			return true;
		v = v.replace('.', '').replace('.', '');;
		v = v.replace('-', '');

		return validate_CPF(v);
	}

	function CNPJ(v) {

		if (v == '')
			return true;
		v = v.replace('.', '').replace('.', '');
		v = v.replace('-', '');
		v = v.replace('/', '');

		return validate_CGC(v);
	}

	function fixXMLDocument(doc) {
		if (!IE)
			fixXMLNode(doc.documentElement);
	}

	function fixXMLNode(node) {
		var children = node.childNodes;
		for (var i = 0; i < children.length; i++) {
			var child = children[i];
			if ((trim(child.nodeValue) == '') && (!child.tagName)) {
				child.parentNode.removeChild(child);
			} else
				fixXMLNode(child);
		}
	}

	function loadXML(xml) {
		// code for IE
		if (window.ActiveXObject) {
			var xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
			xmlDoc.async = false;
			xmlDoc.loadXML(xml);
			return xmlDoc;
		}
		// code for Mozilla, Firefox, Opera, etc.
		else if (document.implementation
				&& document.implementation.createDocument) {
			return (new DOMParser()).parseFromString(xml, "text/xml");;
		} else {
			alert('Your browser cannot handle this script');
		}
	}

	var httpprocessing = false;

	function evalResponse() {
	}

	function doEval(s) {
		lastReceivedCommand = s;
		eval(s);
	}

	function replaceAll(str, token, newToken) {
		return str.toString().split(token).join(newToken);
	}

	function convertNonUnicodeChars(value) {
		if (IE && !isNullable(value) && value.length > 0) {
			return value.replace(/\x80/g, String.fromCharCode(8364));
		}
		return value;
	}

	var lastReceivedContent = "";
	function getAndEvalReturn(http) {
		if (http.readyState == 4) {
			iniprofile = (new Date()).getTime();
			try {
				var content = convertNonUnicodeChars(http.responseText);
				lastReceivedContent = content;
				eval(content);
			} catch (e) {
				interactionError(e.toString());
			} finally {
				hideWait();
				httpPool.leave(http);
			}
			window.status = 'Processado em '
					+ ((new Date()).getTime() - iniprofile) + 'ms';
			httpprocessing = false;
		}
	}

	function getAndEval(url) {
		if (httpprocessing) {
		} else {
			httpprocessing = true;
			showWait();
			timeout(getAndEval2, 0, [url]);
		}
	}

	function getAndEval2(url, throwsException) {
		showWait();
		var http = httpPool.get();
		try {
			http.open('GET', url, true);
			http.onreadystatechange = function() {
				var httpObject = http;
				getAndEvalReturn(httpObject);
			}
			http.send(null);
			httpPool.leave(http);
		} catch (e) {
			if (throwsException) {
				throw e;
			} else {
				getAndEval2(url, true);
			}
		}
	}

	function getAndEvalSync(url) {
		if (httpprocessing) {
		} else {
			httpprocessing = true;
			showWait();
			timeout(getAndEvalSync2, 0, [url]);
		}
	}

	function getAndEvalSync2(url, throwsException) {
		showWait();
		var http = httpPool.get();
		try {
			http.open('GET', url, false);
			http.send(null);
			var content = convertNonUnicodeChars(http.responseText);
			httpPool.leave(http);
			throwsException = true;
			hideWait();
			httpprocessing = false;
			lastReceivedContent = content;
			eval(content);
		} catch (e) {
			if (throwsException)
				throw e;
			else
				getAndEvalSync2(url, true);
		}
	}

	function getAndEvalAsync(url, callback, throwsException) {
		getContentAsync(url, function(data) {
			eval(data);
			if (callback) {
				callback(data);
			}
		}.bind(this));
	}

	function get(url, throwsException) {
		var http = httpPool.get();
		try {
			http.open('GET', url, false);
			http.send(null);
			httpPool.leave(http);
		} catch (e) {
			if (throwsException)
				throw e;
			else
				get(url, true);
		}
	}

	function getURL(url, throwsException) {
		var http = httpPool.get();
		try {
			http.open('GET', url, false);
			http.send(null);
			httpPool.leave(http);
		} catch (e) {
			if (throwsException)
				throw e;
			else
				getURL(url, true);
		}
	}

	function getAbsolutContextPath() {
		var indice = location.pathname.lastIndexOf('/');
		var path = location.pathname.substring(0, indice + 1);
		var url = location.protocol + "//" + location.host + path;
		return url;
	}

	function timeout(handler, delay, fparams) {
		var self = this;
		var params = fparams;
		var wrapper = function() {
			if (params)
				handler.apply(self, params);
			else
				handler.apply(self);
		};
		return window.setTimeout(wrapper, delay ? delay : 0);
	}

	function getXMLContent(url) {
		if (!httpprocessing) {
			var http = getHTTPObjectXML();
			try {
				httpprocessing = true;
				http.open('GET', url, false);
				http.send(null);
				httpprocessing = false;

				var xmldoc = http.responseXML;

				if (xmldoc) {
					var node = xmldoc.getElementsByTagName('root');
					if (node) {
						node = node.item(0);
						if (node) {
							var node = node.getElementsByTagName('error');
							if (node && node.item(0)) {
								node = node.item(0);
								timeout(interactionError, 0, [
										node.getAttribute('message'), null,
										null, null, node.firstChild.nodeValue]);
								return null;
							}
						}
					}
				}
				return xmldoc;
			} catch (e) {
				httpprocessing = false;
				interactionError(getLocaleMessage("ERROR.GET_XML_DATA_FAILED")
						+ "<br>" + e.toString());
			}
		} else {
			return null;
		}

	}

	function getContentAsync(url, callback, throwsException,
			dontConvertUnicodeChars) {
		var http = httpPool.get();
		try {
			http.open('GET', url, true);
			http.onreadystatechange = function() {
				if (http.readyState == 4) {
					try {
						var content = http.responseText;
						if (!dontConvertUnicodeChars) {
							content = convertNonUnicodeChars(content);
						}

						if (callback) {
							callback(content);
						}
					} catch (e) {
						interactionError(e.toString());
					} finally {
						httpPool.leave(http);
					}
				}

			}

			http.send(null);

			httpPool.leave(http);
		} catch (e) {
			if (throwsException)
				throw e;
			else
				getContentAsync(url, callback, true, dontConvertUnicodeChars);
		}
	}

	function getContent(url, throwsException, dontConvertUnicodeChars) {
		var http = httpPool.get();
		try {
			http.open('GET', url, false);
			http.send(null);
			var content = http.responseText;
			if (!dontConvertUnicodeChars) {
				content = convertNonUnicodeChars(content);
			}
			httpPool.leave(http);
			return content;
		} catch (e) {
			if (throwsException)
				throw e;
			else
				return getContent(url, true, dontConvertUnicodeChars);
		}
	}

	function hideMainMessage() {
		if (document.messageDIV) {
			document.body.removeChild(document.messageDIV);
			document.messageDIV = null;
		}
	}

	function showWait() {
		httpprocessing = true;
		try {
			if (mainform.document.ac)
				mainform.document.ac.showProcessing(true);
		} catch (e) {
		}
	}

	function hideWait() {
		httpprocessing = false;
		try {
			if (mainform.document.ac)
				mainform.document.ac.showProcessing(false);
		} catch (e) {
		}
	}

	function setFocus() {
		try {
			if (IE && document.body)
				document.body.focus();
			else
				window.focus();
		} catch (e) {
			try {
				window.focus();
			} catch (e2) {
			}
		}
	}

	function getWindowHeight() {
		var dimensions = getWindowDimensions();
		return dimensions.height;
	}

	function getWindowWidth() {
		var dimensions = getWindowDimensions();
		return dimensions.width;
	}

	function getWindowDimensions() {
		var myWidth = 0, myHeight = 0;

		if (typeof (window.innerWidth) == 'number') {
			myWidth = window.innerWidth;
			myHeight = window.innerHeight;
		} else if (document.documentElement
				&& (document.documentElement.clientWidth || document.documentElement.clientHeight)) {
			myWidth = document.documentElement.clientWidth;
			myHeight = document.documentElement.clientHeight;
		} else if (document.body
				&& (document.body.clientWidth || document.body.clientHeight)) {
			myWidth = document.body.clientWidth;
			myHeight = document.body.clientHeight;
		}

		return {
			width : myWidth,
			height : myHeight
		};
	}

	function showMainMessage(m, w, h, focus) {

		if (focus)
			setFocus();

		var myWidth = 0, myHeight = 0;

		if (typeof (window.innerWidth) == 'number') {
			myWidth = window.innerWidth;
			myHeight = window.innerHeight;
		} else if (document.documentElement
				&& (document.documentElement.clientWidth || document.documentElement.clientHeight)) {
			myWidth = document.documentElement.clientWidth;
			myHeight = document.documentElement.clientHeight;
		} else if (document.body
				&& (document.body.clientWidth || document.body.clientHeight)) {
			myWidth = document.body.clientWidth;
			myHeight = document.body.clientHeight;
		}

		l = (myWidth - w) / 2;
		t = (myHeight - h) / 2;

		if (m != '') {
			var div = document.createElement("div");
			div.style.width = w + 'px';
			div.style.height = h + 'px';
			div.style.left = l + 'px';
			div.style.top = t + 'px';
			div.style.position = 'absolute';
			div.name = 'messageDIV';
			div.id = 'messageDIV';
			div.style.display = 'block';
			div.style.zIndex = 1000000;

			div.innerHTML = '<table width='
					+ w
					+ ' height='
					+ h
					+ ' border=0 cellpadding=2 cellspacing=1 bgcolor=#000000><tr><td bgcolor=#FFFF99><center><font face=arial size=2>'
					+ m + '</font></center></td></tr></table>';
			document.messageDIV = div;
			document.body.appendChild(div);

		} else
			hideMainMessage();
	}

	function so_clearInnerHTML(obj) {
	}

	function doEnter(evt, func, nextInput) {
		if (evt.keyCode == 13 || evt.keyCode == 10) {
			func();
			return false;
		} else if (evt.keyCode == 9) {
			if (nextInput && $(nextInput)) {
				$(nextInput).focus();
				return false;
			}
		} else if (evt.keyCode == 34) {
			if (evt.preventDefault) {
				evt.preventDefault();
				evt.stopPropagation();
			} else {
				evt.keyCode = 0;
				evt.returnValue = false;
			}

			var grid = WFRQueryResults.obj;
			if (grid) {
				grid.setProperty("selection/index", 0);
				grid.element().focus();
			}

			return false;
		}
		return true;
	}

	/**
	 *
	 *  URL encode / decode
	 *  http://www.webtoolkit.info/
	 *
	 **/

	var Url = {

		// public method for url encoding
		encode : function(string) {
			return escape(this._utf8_encode(string)).replace(/\+/g, "%2B");
		},

		// public method for url decoding
		decode : function(string) {
			return this._utf8_decode(unescape(string));
		},

		// private method for UTF-8 encoding
		_utf8_encode : function(string) {
			string = string.toString().replace(/\r\n/g, "\n");
			var utftext = "";

			for (var n = 0; n < string.length; n++) {

				var c = string.charCodeAt(n);

				if (c < 128) {
					utftext += String.fromCharCode(c);
				} else if ((c > 127) && (c < 2048)) {
					utftext += String.fromCharCode((c >> 6) | 192);
					utftext += String.fromCharCode((c & 63) | 128);
				} else {
					utftext += String.fromCharCode((c >> 12) | 224);
					utftext += String.fromCharCode(((c >> 6) & 63) | 128);
					utftext += String.fromCharCode((c & 63) | 128);
				}

			}

			return utftext;
		},

		// private method for UTF-8 decoding
		_utf8_decode : function(utftext) {
			var string = "";
			var i = 0;
			var c = c1 = c2 = 0;

			while (i < utftext.length) {

				c = utftext.charCodeAt(i);

				if (c < 128) {
					string += String.fromCharCode(c);
					i++;
				} else if ((c > 191) && (c < 224)) {
					c2 = utftext.charCodeAt(i + 1);
					string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
					i += 2;
				} else {
					c2 = utftext.charCodeAt(i + 1);
					c3 = utftext.charCodeAt(i + 2);
					string += String.fromCharCode(((c & 15) << 12)
							| ((c2 & 63) << 6) | (c3 & 63));
					i += 3;
				}

			}

			return string;
		}

	}

	function URLEncode(plaintext, forceUTF8) {
		if (ENCODING == "UTF-8" || forceUTF8) {
			return Url.encode(plaintext);
		} else {
			return URLEncode2(plaintext);
		}
	}

	function URLEncode3(plaintext) {
		if (plaintext == null || typeof (plaintext) == 'undefined'
				|| plaintext === '' || plaintext.toString() == 'NaN') {
			return "";
		}

		return encodeURIComponent(plaintext);
	}

	function URLEncode2(plaintext) {
		if (plaintext == null || typeof (plaintext) == 'undefined'
				|| plaintext === '' || plaintext.toString() == 'NaN') {
			return "";
		}

		plaintext = plaintext.toString();

		// The Javascript escape and unescape functions do not correspond
		// with what browsers actually do...
		var SAFECHARS = "0123456789" + // Numeric
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ" + // Alphabetic
		"abcdefghijklmnopqrstuvwxyz" + "-_.!~*'()€"; // RFC2396 Mark characters
		var HEX = "0123456789ABCDEF";

		var encoded = "";
		for (var i = 0; i < plaintext.length; i++) {
			var ch = plaintext.charAt(i);
			if (ch == " ") {
				encoded += "+"; // x-www-urlencoded, rather than %20
			} else if (SAFECHARS.indexOf(ch) != -1) {
				encoded += ch;
			} else {
				var charCode = ch.charCodeAt(0);
				if (charCode > 255) {
					/*interactionError("Unicode Character '"
					                  + ch
					                  + "' cannot be encoded using standard URL encoding.\n"
					                  + "(URL encoding only supports 8-bit characters.)\n"
					                  + "A space (+) will be substituted.");*/

					encoded += "+";
				} else {
					encoded += "%";
					encoded += HEX.charAt((charCode >> 4) & 0xF);
					encoded += HEX.charAt(charCode & 0xF);
				}
			}
		}

		return encoded;
	}

	function stringToHTMLString(value) {
		var formated = "";

		if (value) {
			for (var i = 0; i < value.length; i++) {
				var c = value.charAt(i);
				if (c == " ") {
					formated += " ";
				} else {
					if (c == "\"") {
						formated += "&quot;";
					} else if (c == "&") {
						formated += "&amp;";
					} else if (c == "<") {
						formated += "&lt;";
					} else if (c == ">") {
						formated += "&gt;";
					} else if (c == "\n") {
						formated += "&lt;br/&gt;";
					} else {
						var ci = 0xffff & c.charCodeAt(0);
						if (ci < 160) {
							formated += c;
						} else {
							formated += "&#";
							formated += parseInt(ci);
							formated += ";";
						}
					}
				}
			}
		}

		return formated;
	}

	function stringToJs(value) {
		var formated = "";

		if (!isNullable(value)) {
			value = value.toString();
			for (var i = 0; i < value.length; i++) {
				var c = value.charAt(i);
				if (c == "\\") {
					formated += "\\\\";
				} else if (c == "'") {
					formated += "\\'";
				} else if (c == "\"") {
					formated += "\\\"";
				} else if (c == "\n") {
					formated += "\\n";
				} else if (c == "\r") {
				} else {
					formated += c;
				}
			}
		}

		return formated;
	}

	function isTypeOf(obj, clazz) {
		var classCompare = false;
		try {
			classCompare = (classCompare || (obj instanceof eval(clazz)));
			classCompare = (classCompare || (obj.constructor === eval(clazz)));
		} catch (e) {
		}
		return (typeof obj == clazz || classCompare)
				|| (obj.constructor && obj.constructor.toString && obj.constructor
						.toString().indexOf('function ' + clazz) != -1);
	}

	function ArrayInstance(obj) {
		return obj;
	}

	function JSONInstance(obj) {
		return obj;
	}

	function serialize(_obj, useQuot, recursive, emptyIfNull) {
		if (isNullable(_obj)) {
			return (emptyIfNull ? '' : 'null');
		}

		var quot = '';
		if (useQuot) {
			quot = '\'';
		}

		switch (typeof _obj) {
			case 'boolean' :
			case 'number' :
				return _obj;
				break;

			case 'string' :
				return quot + stringToJs(_obj) + quot;
				break;

			case 'object' :
				var str = '';
				if (isTypeOf(_obj, 'Array')) {
					if (!recursive) {
						str = 'ArrayInstance(';
					}
					str += '[';
					var first = true;
					for (var i = 0; i < _obj.length; i++) {
						if (!first) {
							str += ', ';
						}

						first = false;
						str += serialize(_obj[i], true, true);
					}

					str += ']';
					if (!recursive) {
						str += ')';
					}
				} else if (isTypeOf(_obj, 'Map')) {
					if (!recursive) {
						str = 'JSONInstance(';
					}
					str += _obj.toStringSerialized(true);
					if (!recursive) {
						str += ')';
					}
				} else if (isTypeOf(_obj, 'Date')) {
					str = quot + 'new Date(' + _obj.getTime() + ')' + quot;
				} else if (isTypeOf(_obj, 'Time')) {
					str = quot + 'new Date(' + _obj.getDate().getTime() + ')'
							+ quot;
				} else if (isTypeOf(_obj, 'HTMLElement')) {
					str = quot + _obj.outerHTML + quot;
				} else {
					if (!recursive) {
						str = 'JSONInstance(';
					}
					str += '{';
					var key;
					for (key in _obj) {
						if (typeof _obj != 'function') {
							str += key + ':' + serialize(_obj[key], true, true)
									+ ',';
						}
					}
					str = str.replace(/\,$/, '');
					str += "}";
					if (!recursive) {
						str += ")";
					}
				}
				return str;
				break;

			default :
				return (emptyIfNull ? '' : 'null');
				break;
		}
	}

	function executeRule(sysId, formId, ruleName, params, fields, extraParams) {
		window.setTimeout(
				function() {
					document.hasRuleErrors = false;
					var id = 'RULE' + parseInt((Math.random() * 9999999));

					var iframe;
					iframe = document.createElement("iframe");
					iframe.name = id;
					iframe.id = id;
					iframe.frameBorder = 0;
					iframe.setAttribute("frameborder", "no");
					iframe.setAttribute("border", 0);
					iframe.setAttribute("marginwidth", 0);
					iframe.setAttribute("marginheight", 0);
					iframe.width = 0;
					iframe.height = 0;
					iframe.src = "";

					document.body.appendChild(iframe);

					var frm = document.createElement("form");
					frm.target = id;
					frm.method = "POST";
					frm.action = "executeRule.do";
					document.body.appendChild(frm);

					var hidden;
					hidden = createHiddenForRule("iframeId", id);
					frm.appendChild(hidden);

					hidden = createHiddenForRule("sys", sysId);
					frm.appendChild(hidden);

					/*
					 * O ID do formul�rio pode vir nulo quando executando uma regra ao abrir ou fechar sistema.
					 * Momento este que n�o possui formul�rio, apenas o sistema
					 */
					if (formId != null) {
						hidden = createHiddenForRule("formID", formId);
						frm.appendChild(hidden);
					}

					hidden = createHiddenForRule("action", "executeRule");
					frm.appendChild(hidden);

					hidden = createHiddenForRule("ruleName", ruleName);
					frm.appendChild(hidden);

					for (var i = 0; i < params.length; i++) {
						var value = params[i];
						var isObject = (typeof value == 'object');

						if (isObject) {
							if (value) {
								value = value.value;
							}
						}

						var isLiteral = (typeof value == 'string');

						if (!isLiteral) {
							if (value) {
								try {
									value = eval("document.c_" + value
											+ ".getValue()");
								} catch (e) {
									value = "";
								}
							} else {
								value = "";
							}
						}

						hidden = createHiddenForRule("P_" + i, value);
						frm.appendChild(hidden);
						frm["P_" + i] = hidden;

					}

					var position = -1;
					if (extraParams && extraParams.length > 0) {
						for (var i = 0; i < extraParams.length; i++) {
							var param = extraParams[i];
							if (!isNullable(param, true) && !isEvent(param)) {
								position++;
								var hidden = frm["P_" + position];
								if (!hidden) {
									hidden = createHiddenForRule("P_"
											+ position,
											normalizeRuleParam(param));
									frm.appendChild(hidden);
								} else {
									hidden.value = normalizeRuleParam(param);
								}
							}
						}
					}

					for (var i = 0; i < fields.length; i++) {

						var value = fields[i];
						var code = fields[i];

						var isObject = (typeof value == 'object');

						if (isObject) {
							code = value.code;
							value = value.value;
						}

						var isLiteral = (typeof value == 'string');

						if (!isLiteral) {
							if (value) {
								try {
									value = eval("document.c_" + value
											+ ".getValue()");
								} catch (e) {
									value = "";
								}

							} else {
								value = "";
							}
						}

						hidden = createHiddenForRule("F_" + i + "_" + code,
								value);
						frm.appendChild(hidden);
					}

					iframe.onload = function() {
						document.body.removeChild(frm);
						document.body.removeChild(iframe);
					}

					frm.submit();
				}, 0);
	}

	function isNullable(value, dontCheckEmpty) {
		return (value == null || typeof value == 'undefined'
				|| (!dontCheckEmpty && value === '') || (value.toString && value
				.toString() == 'NaN'));
	}

	function parseBoolean(value) {
		if (value == null || typeof value == "undefined") {
			return false;
		}

		if (typeof value == "boolean") {
			return value;
		}

		if (!value.toString) {
			return false;
		}

		value = trim(value.toString().toUpperCase());

		return value == "1" || value == "S" || value == "V" || value == "T"
				|| value == "Y" || value == "TRUE" || value == "VERDADE"
				|| value == "VERDADEIRO" || value == "YES" || value == "SIM";
	}

	function parseNumeric(value) {
		if (isNullable(value)) {
			return 0.0;
		}

		if (typeof value == "number") {
			return parseFloat(value);
		}

		if (typeof value == "boolean") {
			if (value) {
				return 1.0;
			} else {
				return 0.0;
			}
		}

		if (!value.toString) {
			return 0.0;
		}

		var groupingPointRegExp = new RegExp(("\\" + GROUPING_POINT), "g");
		value = new String(value).replace(groupingPointRegExp, "").replace(
				DECIMAL_POINT, ".");

		return parseFloat(value);
	}

	function normalizeRuleParam(value, dontSerialize) {
		if (value == null || typeof value == "undefined") {
			return "";
		}

		if (typeof value == "number") {
			return value.toString().replace(".", DECIMAL_POINT);
		}

		if (value.maskFormat) { // Date
			var dateFormat;
			if (parent.$mainform().DATE_PATTERN) {
				dateFormat = parent.$mainform().DATE_PATTERN.toLowerCase();
			} else {
				dateFormat = DATE_PATTERN.toLowerCase();
			}
			return value.maskFormat(dateFormat + " HH:MM:ss");
		}

		if (isTypeOf(value, "Time")) {
			return (value.getDate().maskFormat("HH:MM:ss") + "." + value
					.getDate().getMilliseconds());
		}

		if (!dontSerialize && !isTypeOf(value, "String")
				&& (typeof value == "object")) {
			return serialize(value, false, false, true);
		}

		return value;
	}

	function executeSyncRule(sysId, formId, ruleName, params, fields,
			extraParams) {
		try {
			if (window.disableSyncRule) {
				executeRule(sysId, formId, ruleName, params, fields,
						extraParams);
			} else {

				var ruleParams = new Array();
				for (var i = 0; i < params.length; i++) {
					var value = params[i];
					var isObject = (typeof value == 'object');

					if (isObject) {
						if (value) {
							value = value.value;
						}
					}

					var isLiteral = (typeof value == 'string');

					if (!isLiteral) {
						if (value) {
							value = eval("document.c_" + value + ".getValue()");
						} else {
							value = "";
						}
					}

					ruleParams.push(value);
				}

				for (var i = 0; i < extraParams.length; i++) {
					ruleParams.push(extraParams[i]);
				}

				return executeSyncJavaRule(sysId, formId, ruleName, ruleParams);
			}

		} finally {
			window.disableSyncRule = false;
		}
	}

	function executeSyncJavaRule(sysId, formId, ruleName) {
		document.hasRuleErrors = false;
		var url = "action=executeRule&pType=2&";
		url += ("ruleName=" + URLEncode(ruleName, postForceUTF8) + "&");
		url += ("sys=" + sysId + "&");
		url += ("formID=" + (formId ? formId : "") + "&");
		url += ("parentRID=" + (this && this.getRID ? this.getRID() : ""));
		if ((ENCODING == "UTF-8") && (isFirefoxVersionAbove3)) {
			url += "&decodedParams=true";
		}

		// Caso existam par�metros de entrada para a regra
		if (arguments.length > 3) {
			if (arguments[3] instanceof Array) {
				for (var i = 0; i < arguments[3].length; i++) {
					var value = normalizeRuleParam(arguments[3][i]);

					// Ex.: &P_0=valor
					url += ("&P_" + i + "=" + URLEncode(value, postForceUTF8));
				}
			} else {
				for (var i = (arguments.length - 1); i >= 3; i--) {
					var value = normalizeRuleParam(arguments[i]);

					// Ex.: &P_0=valor
					url += ("&P_" + (i - 3) + "=" + URLEncode(value,
							postForceUTF8));
				}
			}
		}

		// Fields da tela
		var fields = null;
		try {
			fields = controller ? controller.getFormElements() : new Array();
			for (var i = 0; i < fields.length; i++) {
				var field = fields[i];

				// Ex.: &F_1_1234=valor
				if (parseInt(field.getCode()) != -1) {
					url += ("&F_" + i + "_" + field.getCode() + "=" + URLEncode(
							field.getValue(), postForceUTF8));
				}
			}
		} catch (e) {
			//Controller n�o existe
		}

		var content = postURL("executeRule.do", url);

		$mainform().document._ruleReturn = null;

		doEval(content);

		if (document.hasRuleErrors) {
			throw StopRuleExecution();
		}

		return $mainform().document._ruleReturn;
	}

	function postURL(url, postData, throwsException) {
		var http = httpPool.get();
		try {
			var contentType = "application/x-www-form-urlencoded";
			if (isSafari) {
				contentType += ";charset=UTF-8";
			}
			http.open("POST", url, false);
			http.setRequestHeader("Content-Type", contentType);

			http.send(postData);

			var content = convertNonUnicodeChars(http.responseText);

			httpPool.leave(http);

			return content;

		} catch (e) {
			if (throwsException)
				throw e;
			else
				return postURL(url, postData, true);
		}
	}

	function createHiddenForRule(name, value) {
		var hidden = document.createElement("input");
		hidden.name = name;
		hidden.type = "hidden";
		hidden.value = value;

		return hidden;
	}

	function retirarZerosIniciais(value) {
		if (value != null && (typeof value != "undefined")) {
			while (value.charAt(0) == "0" && value.length > 1) {
				value = value.substring(1);
			}
		}
		return value;
	}

	function firstToUpper(texto) {
		if (texto) {
			if (texto.length == 1) {
				return texto.toUpperCase();
			} else if (texto.length > 1) {
				return texto.substring(0, 1).toUpperCase()
						+ texto.substring(1).toLowerCase();
			} else {
				return "";
			}
		}
	}

	function executeRuleFromJS(ruleName, params) {
		var reducedName = reduceVariable(ruleName);
		var sysCode = d.WFRForm.sys.value;
		var formCode = d.WFRForm.formID.value;

		var isJava = false;
		var ruleFunction;
		try {
			ruleFunction = window.eval(reducedName);
		} catch (ex) {
			isJava = true;
		}

		var value = null;
		if (isJava) {
			if (params && params instanceof Array && params.length > 0) {
				value = executeSyncJavaRule(sysCode, formCode, ruleName, params);
			} else {
				value = executeSyncJavaRule(sysCode, formCode, ruleName);
			}
		} else {
			var ruleInstance = new ruleFunction(null, sysCode, formCode);

			if (ruleInstance && ruleInstance.run) { // � JS
				value = executeJSRule(sysCode, formCode, reducedName, params,
						true);
			}
		}

		return value;
	}

	function executeJSRule(sysId, formId, funcao, params, throwErrors,
			extraParams) {
		if (!throwErrors)
			document.hasRuleErrors = false;

		var paramQryStr = new Array();

		var position = 0;
		if (params instanceof Array && params.length > 0) {
			for (; position < params.length; position++) {
				if (params[position]) {
					var value = params[position];
					var isObject = (typeof value == 'object');

					if (isObject && value) {
						value = value.value;
					}

					var isLiteral = (typeof value == 'string');

					if (!isLiteral) {
						if (value) {
							value = eval("document.c_" + value + ".getValue()");
						} else {
							value = "";
						}
					}

					paramQryStr.push(value);
				} else {
					paramQryStr.push(null);
				}
			}
		}

		if (extraParams && extraParams.length > 0) {
			for (var i = 0; i < extraParams.length; i++) {
				var param = extraParams[i];
				if (!isNullable(param, true) && !isEvent(param)) {
					paramQryStr.splice(i, 1, param);
				}
			}
		}
		var func = window.eval(funcao);
		var ruleInstance = new func(null, sysId, formId);

		if (throwErrors) {
			return ruleInstance.run.apply(ruleInstance, paramQryStr);
		} else {
			try {
				/*
				 * Meio alternativo de sobrepor um problema no firefox.
				 * Ele estava perdendo a refer�ncia de parent.mainform quando cliente chamava java.
				 */
				parent.mainform;
				return ruleInstance.run.apply(ruleInstance, paramQryStr);
			} catch (ex) {
				handleException(ex);
			}
		}
	}

	function executeJSRuleNoField(sysId, formId, funcao, params, throwErrors) {
		var paramQryStr = new Array(params.length);

		for (var i = 0; i < params.length; i++) {
			if (params[i]) {
				paramQryStr[i] = params[i];
			} else {
				paramQryStr[i] = null;
			}
		}

		funcao = reduceVariable(funcao)
		var func = eval(funcao);
		var ruleInstance = new func(null, sysId, formId);

		if (throwErrors) {
			return ruleInstance.run.apply(ruleInstance, paramQryStr);
		} else {
			try {
				return ruleInstance.run.apply(ruleInstance, paramQryStr);
			} catch (ex) {
				handleException(ex);
			}
		}
	}

	function getFormFieldValue(f) {
		var coms = controller.getElementsByField(f);
		var value = "";
		if (coms && coms.length > 0)
			value = coms[0].getValue();
		return value;
	}

	function changeFormFieldValue(f, v) {
		var coms = controller.getElementsByField(f);
		if (coms) {
			v = normalizeRuleParam(v);
			for (var i = 0; i < coms.length; i++) {
				coms[i].setValue(v, true);
			}
		}
	}

	function getRuntimeContent(sql, param) {
		return getContent("runtimeContent.do?action=runtimeContent&sys="
				+ sysCode + "&formID=" + idForm + "&sql=" + URLEncode(sql)
				+ "&param=" + param);
	}

	function interactionKeydown(evt, countDiv, obj, countOptions) {
		var r = true;

		var TAB = 9;
		var ESC = 27;
		var LEFT = 37;
		var UP = 38;
		var RIGHT = 39;
		var DOWN = 40;
		var SPACE = 32;

		document.disableEvents = true;

		var keyCode = evt.keyCode || evt.which;
		if (keyCode == TAB || keyCode == LEFT || keyCode == RIGHT) {
			var divInteraction = MM_findObj("divInteraction_" + countDiv);
			if (divInteraction) {
				findNode(divInteraction, obj).focus();
			}
			r = false;
		} else if (keyCode == ESC) {
			interactionCancel(countDiv);
			r = false;
		} else if (keyCode == SPACE) {
			interactionCancel(countDiv);
			r = false;
		} else if (keyCode == DOWN) {
			if (countOptions && countOptions != 0) {
				var divInteraction = MM_findObj("divInteraction_" + countDiv);

				var checkedIndex = -1;
				for (var i = 0; i < countOptions; i++) {
					var interactionChosen = findNode(
							MM_findObj("divInteraction_" + countDiv),
							"options_" + countDiv + "_" + i);
					if (interactionChosen.firstChild.checked) {
						checkedIndex = i;
						break;
					}
				}

				var newIndex = checkedIndex + 1;
				if (newIndex == countOptions) {
					newIndex = 0;
				}

				divInteraction.selectedValue = newIndex;
				var inputToSelect = findNode(divInteraction, "options_"
						+ countDiv + "_" + newIndex);
				inputToSelect.firstChild.checked = true;

				r = false;
			}
		} else if (keyCode == UP) {
			if (countOptions && countOptions != 0) {
				var divInteraction = MM_findObj("divInteraction_" + countDiv);

				var checkedIndex = -1;
				for (var i = 0; i < countOptions; i++) {
					var interactionChosen = findNode(divInteraction, "options_"
							+ countDiv + "_" + i);
					if (interactionChosen.firstChild.checked) {
						checkedIndex = i;
						break;
					}
				}

				var newIndex = checkedIndex - 1;
				if (newIndex == -1) {
					newIndex = countOptions - 1;
				}

				divInteraction.selectedValue = newIndex;
				var inputToSelect = findNode(divInteraction, "options_"
						+ countDiv + "_" + newIndex);
				inputToSelect.firstChild.checked = true;

				r = false;
			}
		}

		if (!r) {
			if (evt.preventDefault) {
				evt.preventDefault();
				evt.stopPropagation();
			} else {
				evt.keyCode = 0;
				evt.returnValue = false;
			}
			return false;
		} else {
			return true;
		}
	}

	function interactionConfirmWithEvents(msg, f, fParams, fParent,
			cancelFunction, cancelParams, cancelTarget) {
		interaction(msg, ['Ok'], [f], [fParams], [fParent], true,
				cancelFunction, cancelParams, cancelTarget);
	}

	function interaction(text, radioValues, functions, params, targets,
			interactionConfirm, cancelFunction, cancelParams, cancelTarget) {

		if (window._WINDOW_WINDOW_CONTROLLER == window) {
			var buttonsLabels = new Array();
			for (var i = 0; i < radioValues.length; i++) {
				buttonsLabels.push({
					value : radioValues[i]
				});
			}

			buttonsLabels.push({
				value : "Cancelar"
			});

			$jq.msgBox({
				title : text,
				content : text,
				type : "confirm",
				opacity : 0.7,
				buttons : buttonsLabels,
				success : function(result) {
					var idx = arrayIndexOf(radioValues, result);

					if (idx != -1) {

						var selectedValue = idx;

						if (functions && functions.length > selectedValue
								&& functions[selectedValue]) {
							var functionToCall = functions[selectedValue];

							var hasParams = params
									&& params.length > selectedValue
									&& params[selectedValue];

							var target = this;
							if (targets && targets.length > selectedValue
									&& targets[selectedValue]) {
								target = targets[selectedValue];
							}

							if (hasParams) {
								functionToCall.apply(target,
										params[selectedValue]);
							} else {
								functionToCall.call(target);
							}
						}

					}
				}
			});
		} else {
			window._WINDOW_WINDOW_CONTROLLER.interaction(text, radioValues,
					functions, params, targets, interactionConfirm,
					cancelFunction, cancelParams, cancelTarget);
		}

	}

	function interactionOk(count) {
		document.disableEvents = false;

		var divInteraction = MM_findObj("divInteraction_" + count);
		if (divInteraction) {
			var functions = divInteraction.functions;
			var params = divInteraction.params;
			var targets = divInteraction.targets;
			var selectedValue = divInteraction.selectedValue;

			if (functions && functions.length > selectedValue
					&& functions[selectedValue]) {
				var functionToCall = functions[selectedValue];

				var hasParams = params && params.length > selectedValue
						&& params[selectedValue];

				var target = this;
				if (targets && targets.length > selectedValue
						&& targets[selectedValue]) {
					target = targets[selectedValue];
				}

				if (hasParams) {
					functionToCall.apply(target, params[selectedValue]);
				} else {
					functionToCall.call(target);
				}
			}
		}

		removeInteraction(count);

		return false;
	}

	function interactionCancel(count) {
		document.disableEvents = false;

		var divInteraction = MM_findObj("divInteraction_" + count);
		if (divInteraction) {
			var functionToCall = divInteraction.cancelFunction;
			var params = divInteraction.cancelParams;
			var target = divInteraction.cancelTarget;

			if (functionToCall) {
				if (!target) {
					target = this;
				}

				var hasParams = (params && params.length > 0);
				if (hasParams) {
					functionToCall.apply(target, params);
				} else {
					functionToCall.call(target);
				}
			}
		}

		removeInteraction(count);

		return false;
	}

	function removeInteraction(count) {
		var divInteraction = MM_findObj("divInteraction_" + count);
		var divBckInteraction = MM_findObj("divBckInteraction_" + count);
		var divInteractionIframe = MM_findObj("divInteractionIframe_" + count);

		if (divInteraction) {
			var execFunction = divInteraction.execFunction;
			if (execFunction) {
				var execParams = divInteraction.execParams;
				var execTarget = divInteraction.execTarget
						? divInteraction.execTarget
						: this;

				if (execParams) {
					execFunction.apply(execTarget, execParams);
				} else {
					execFunction.call(execTarget);
				}
			}

			if (divInteraction)
				divInteraction.parentNode.removeChild(divInteraction);
		}

		if (divInteractionIframe) {
			divInteractionIframe.parentNode.removeChild(divInteractionIframe);
		}
		if (divBckInteraction) {
			divBckInteraction.parentNode.removeChild(divBckInteraction);
		}
	}

	function alertText(text, execFunction, execParams, execTarget) {
		alert(text);
		if (execFunction) {
			var target = (execTarget ? execTarget : this);
			var hasParams = (execParams && execParams.length > 0);
			if (hasParams) {
				execFunction.apply(target, execParams);
			} else {
				execFunction.call(target);
			}
		}
	}

	function interactionInfo(text, execFunction, execParams, execTarget) {
		var varExists = false;
		try {
			messagesAsAlert;
			varExists = true;
		} catch (e) {
		}

		if (varExists && messagesAsAlert) {
			alertText(text, execFunction, execParams, execTarget);
		} else {
			interactionMessage(text, 0, execFunction, execParams, execTarget);
		}
	}

	function interactionError(text, execFunction, execParams, execTarget,
			excecao) {

		document.hasRuleErrors = true;
		document.hasRuleException = true;
		document.ruleErrorMessage = text;

		var varExists = false;
		try {
			messagesAsAlert;
			varExists = true;
		} catch (e) {
		}

		if (varExists && messagesAsAlert) {
			alertText(text, execFunction, execParams, execTarget);
		} else {
			interactionMessage(text, 1, execFunction, execParams, execTarget,
					excecao, true);
		}
	}

	function interactionShowException(num) {
		var left = (screen.width - 500) / 2;
		var top = (screen.height - 370) / 2;
		var w = MM_openBrWindow(
				'interactionException' + PAGES_EXTENSION + '?id=' + num,
				'interactionException',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no,width=500,height=370,left='
						+ left + ',top=' + top);
	}

	function getInteractionException(num) {
		var doc;
		try {
			doc = mainform.document;
		} catch (e) {
			doc = document;
		}
		return doc.interactionException[num];
	}

	function interactionAlertMessage(text, type, execFunction, execParams,
			execTarget, excecao, error) {
		alert(text);
		if (execFunction) {
			if (execParams) {
				execFunction.apply(execTarget ? execTarget : this, execParams);
			} else {
				execFunction.call(execTarget);
			}
		}
	}

	// Type = 0 -> Info
	// Type = 1 -> Error
	function interactionMessage(text, type, execFunction, execParams,
			execTarget, excecao, error) {
		var win = window._WINDOW_WINDOW_CONTROLLER;

		if (win == window) {
			if (!window._JQUERY_WINDOW_CONTROLLER) {
				interactionAlertMessage(text, type, execFunction, execParams,
						execTarget, excecao, error);
				return;
			}

			if (type == 1 && excecao) {
				console.log(excecao);
			}

			if (text.indexOf("getLocaleMessage") == -1
					&& text.indexOf("d is null") == -1
					&& text.indexOf("_JQUERY_WINDOW_ID") == -1
					&& text.indexOf("WFRForm") == -1) {

				$jq.msgBox({
					title : (type == 1 ? "Erro" : "Info"),
					content : text,
					type : (type == 1 ? "error" : "info"),
					opacity : 0.7,
					success : function(result) {

						if (execFunction) {

							if (execParams) {
								execFunction.apply(execTarget
										? execTarget
										: this, execParams);
							} else {
								execFunction.call(execTarget);
							}
						}

					}
				});
			}
		} else {
			if (win && win._WINDOW_WINDOW_CONTROLLER) {
				win._WINDOW_WINDOW_CONTROLLER.interactionMessage(text, type,
						execFunction, execParams, execTarget, excecao);
			} else {
				interactionAlertMessage(text, type, execFunction, execParams,
						execTarget, excecao, error);
			}
		}

		/*
		var doc;
		try {
		doc = mainform.document;
		} catch(e) {
		doc = document;
		}

		var skin = this.skin;
		if (!skin) skin = "Resource/";

		if (text == null || typeof text == "undefined") {
		text = "";
		} else {
		text = text.toString().replace(/\n/g, "<br>");
		}

		if (!doc.body) {
		doc.writeln("<body></body>");
		}

		// #### Caso a largura da tela seja 0 e exista parent, ent�o obt�m-se o document deste
		if (doc.body.clientWidth == 0 && window.parent) {
		doc = window.parentWindow.document;
		}

		if (!doc.zoomCount) doc.zoomCount = 1000000;
		if (!doc.divCount) doc.divCount = 1;

		doc.zoomCount = doc.zoomCount - 2;
		doc.divCount++;

		// #### Cria um div transparente do tamanho da tela atual e adiciona no body
		var divBckInteraction = doc.createElement("div");
		divBckInteraction.id = "divBckInteraction_" + doc.divCount;
		divBckInteraction.style.width = doc.body.clientWidth;
		divBckInteraction.style.height = doc.body.clientHeight;
		divBckInteraction.style.left = doc.body.scrollLeft+"px";
		divBckInteraction.style.top = doc.body.scrollTop+"px";
		divBckInteraction.style.zIndex = doc.zoomCount;
		divBckInteraction.style.position = "absolute";
		divBckInteraction.style.backgroundColor = "#FFFFFF";
		divBckInteraction.innerHTML = "<table width=100% height=100%><tr><td>&nbsp;</td></tr></table>";
		divBckInteraction.style.filter = "alpha(opacity=30)";
		divBckInteraction.style.opacity = .3;
		doc.body.appendChild(divBckInteraction);

		// #### Cria um div com o zindex maior que o div acima
		var divInteraction = doc.createElement("div");
		divInteraction.id = "divInteraction_" + doc.divCount;
		divInteraction.style.width = "382px";
		divInteraction.style.height = "117px";
		divInteraction.style.zIndex = doc.zoomCount+1;
		divInteraction.style.position = "absolute";
		divInteraction.style.display = "block";
		divInteraction.setAttribute("type", "custom_msg");
		divInteraction.execFunction = execFunction;
		divInteraction.execParams = execParams;
		divInteraction.execTarget = execTarget;

		// #### Monta o conte�do da div criada acima

		// #### CSS
		var textStyle = "font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 10px; color: #333333; text-decoration: none;";
		var headerStyle = "font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 10px; color: #333333; font-weight: bold;";

		// #### Cria��o da tela
		var divInteractionInnerHTML = "<table id=\"interactionTable\" width=\"100%\" border=\"0\" cellpadding=\"1\" cellspacing=\"0\">\n";
		divInteractionInnerHTML += "<tr>\n";
		divInteractionInnerHTML += "<td align=\"center\" valign=\"middle\" style=\"background-color: #94A6B5;\">\n";

		divInteractionInnerHTML += "<table width=\"100%\" height=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n";
		divInteractionInnerHTML += "<tr>\n";
		divInteractionInnerHTML += "<td align=\"center\" valign=\"middle\">\n";
		divInteractionInnerHTML += "<table width=\"380\" height=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n";
		divInteractionInnerHTML += "<tr>\n";
		divInteractionInnerHTML += "<td valign=\"top\" bgcolor=\"#EFEBE7\" onselectstart=\"return false;\" onmousedown=\"return false;\">\n";
		divInteractionInnerHTML += "<table width=\"380\" height=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n";

		// T�tulo da Tela
		divInteractionInnerHTML += "<tr>\n";
		divInteractionInnerHTML += "<td height=\"26\" colspan=\"2\" background=\"" + skin + "int_topo.jpg\">\n";
		divInteractionInnerHTML += "<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n";
		divInteractionInnerHTML += "<tr>\n";
		divInteractionInnerHTML += "<td width=\"10\" align=\"center\" valign=\"middle\">&nbsp;</td>\n";
		divInteractionInnerHTML += "<td width=\"350\"><span style=\"" + headerStyle + "\">" + doc.title + "</span></td>\n";
		divInteractionInnerHTML += "<td width=\"20\" align=\"right\" valign=\"middle\" onclick=\"return interactionCancel(" + doc.divCount + ");\" style=\"cursor:pointer;\"><img src=\"" + skin + "int_close.jpg\" width=\"11\" height=\"11\">&nbsp;&nbsp;</td>\n";
		divInteractionInnerHTML += "</tr>\n";
		divInteractionInnerHTML += "</table>\n";
		divInteractionInnerHTML += "</td>\n";
		divInteractionInnerHTML += "</tr>\n";

		// Mensagem
		divInteractionInnerHTML += "<tr valign=\"top\">\n";
		divInteractionInnerHTML += "<td>\n";
		divInteractionInnerHTML += "<table width=\"380\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n";
		divInteractionInnerHTML += "<tr>\n";

		if (type == 0) { // Info
		divInteractionInnerHTML += "<td width=\"28%\" align=\"center\" valign=\"middle\"><br><img src=\"" + skin + "int_info.jpg\"></td>\n";
		} else if (type == 1) { // Error
		divInteractionInnerHTML += "<td width=\"28%\" align=\"center\" valign=\"middle\"><br><img src=\"" + skin + "int_erro.jpg\"></td>\n";
		}

		divInteractionInnerHTML += "<td width=\"72%\" align=\"center\" valign=\"middle\"><div id=\"message\" name=\"message\" style=\"" + textStyle + "\">" + text + "</div></td>\n";
		divInteractionInnerHTML += "</tr>\n";
		divInteractionInnerHTML += "</table>\n";
		divInteractionInnerHTML += "</td>\n";
		divInteractionInnerHTML += "</tr>\n";

		var detailsStr;
		var okStr;
		if ($mainform && $mainform().getLocaleMessage) {
		detailsStr = $mainform().getLocaleMessage("LABEL.DETAILS");
		okStr = $mainform().getLocaleMessage("LABEL.OK");
		} else {
		detailsStr = "Details";
		okStr = "Ok";
		}

		if (excecao) {
		if (!doc.interactionException) {
		  doc.interactionException = new Array();
		}
		
		doc.interactionException[doc.divCount] = excecao;

		divInteractionInnerHTML += "<tr>\n";
		divInteractionInnerHTML += "<td>\n";
		divInteractionInnerHTML += "<table><tr>\n";
		divInteractionInnerHTML += "<td height=\"28\" width=\"188\" background=\"" + skin + "int_bg_cancel.gif\">";
		//Bot�o DETALHES
		divInteractionInnerHTML += "<table style=\"cursor: pointer;\" onkeydown=\"return interactionKeydown(event, " + doc.divCount + ", 'btnOk', 0);\" onclick=\"interactionShowException("+doc.divCount+");\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">";
		divInteractionInnerHTML += "  <tr>";
		divInteractionInnerHTML += "    <td width=\"2\" height=\"2\"><img src=\"Resource/button_container_01.gif\" width=\"2\" height=\"2\" alt=\"\"></td>";
		divInteractionInnerHTML += "    <td background=\"Resource/button_container_02.gif\"></td>";
		divInteractionInnerHTML += "    <td width=\"2\" height=\"2\"><img src=\"Resource/button_container_03.gif\" width=\"2\" height=\"2\" alt=\"\"></td>";
		divInteractionInnerHTML += "  </tr>";
		divInteractionInnerHTML += "  <tr>";
		divInteractionInnerHTML += "    <td background=\"Resource/button_container_04.gif\" width=\"2\"></td>";
		divInteractionInnerHTML += "    <td background=\"Resource/button_container_05.gif\">";
		divInteractionInnerHTML += "      <table>";
		divInteractionInnerHTML += "        <tr>";
		divInteractionInnerHTML += "          <td width=\"5\"></td>";
		divInteractionInnerHTML += "          <td width=\"18\">";
		divInteractionInnerHTML += "            <img width=\"16\" height=\"16\" src=\"Resource/int_details.gif\">";
		divInteractionInnerHTML += "          </td>";
		divInteractionInnerHTML += "          <td>";
		divInteractionInnerHTML += "            <a style=\"" + textStyle + "\" id=\"btnDetails\" href=\"#\" onkeydown=\"return interactionKeydown(event, " + doc.divCount + ", 'btnOk', 0);\" onclick=\"interactionShowException("+doc.divCount+");\">"+ detailsStr +"</a>";
		divInteractionInnerHTML += "          </td>";
		divInteractionInnerHTML += "          <td width=\"5\"></td>"; 
		divInteractionInnerHTML += "        </tr>";
		divInteractionInnerHTML += "      </table>";
		divInteractionInnerHTML += "    </td>";
		divInteractionInnerHTML += "    <td background=\"Resource/button_container_06.gif\" width=\"2\"></td>";
		divInteractionInnerHTML += "  </tr>";
		divInteractionInnerHTML += "  <tr>";
		divInteractionInnerHTML += "    <td width=\"2\" height=\"2\"><img src=\"Resource/button_container_07.gif\" width=\"2\" height=\"2\" alt=\"\"></td>";
		divInteractionInnerHTML += "    <td background=\"Resource/button_container_08.gif\"></td>";
		divInteractionInnerHTML += "    <td width=\"2\" height=\"2\"><img src=\"Resource/button_container_09.gif\" width=\"2\" height=\"2\" alt=\"\"></td>";
		divInteractionInnerHTML += "  </tr>";
		divInteractionInnerHTML += "</table>";
		
		
		divInteractionInnerHTML += "</td>\n";
		divInteractionInnerHTML += "<td align=\"right\" height=\"28\" width=\"188\" background=\"" + skin + "int_bg_ok.gif\">";
		//Bot�o OK
		divInteractionInnerHTML += "<table style=\"cursor: pointer;\" onkeydown=\"return interactionKeydown(event, " + doc.divCount + ", 'btnDetails', 0);\" onclick=\"return interactionCancel(" + doc.divCount + ");\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">";
		divInteractionInnerHTML += "  <tr>";
		divInteractionInnerHTML += "    <td width=\"2\" height=\"2\"><img src=\"Resource/button_container_01.gif\" width=\"2\" height=\"2\" alt=\"\"></td>";
		divInteractionInnerHTML += "    <td background=\"Resource/button_container_02.gif\"></td>";
		divInteractionInnerHTML += "    <td width=\"2\" height=\"2\"><img src=\"Resource/button_container_03.gif\" width=\"2\" height=\"2\" alt=\"\"></td>";
		divInteractionInnerHTML += "  </tr>";
		divInteractionInnerHTML += "  <tr>";
		divInteractionInnerHTML += "    <td background=\"Resource/button_container_04.gif\" width=\"2\"></td>";
		divInteractionInnerHTML += "    <td background=\"Resource/button_container_05.gif\">";
		divInteractionInnerHTML += "      <table>";
		divInteractionInnerHTML += "        <tr>";
		divInteractionInnerHTML += "          <td width=\"5\"></td>";
		divInteractionInnerHTML += "          <td width=\"18\">";
		divInteractionInnerHTML += "            <img width=\"16\" height=\"16\" src=\"Resource/int_ok.gif\">";
		divInteractionInnerHTML += "          </td>";
		divInteractionInnerHTML += "          <td>";
		divInteractionInnerHTML += "            <a style=\"" + textStyle + "\" id=\"btnOk\" href=\"#\" onkeydown=\"return interactionKeydown(event, " + doc.divCount + ", 'btnDetails', 0);\" onclick=\"return interactionCancel(" + doc.divCount + ");\" >"+ okStr +"</a>";
		divInteractionInnerHTML += "          </td>";
		divInteractionInnerHTML += "          <td width=\"5\"></td>"; 
		divInteractionInnerHTML += "        </tr>";
		divInteractionInnerHTML += "      </table>";
		divInteractionInnerHTML += "    </td>";
		divInteractionInnerHTML += "    <td background=\"Resource/button_container_06.gif\" width=\"2\"></td>";
		divInteractionInnerHTML += "  </tr>";
		divInteractionInnerHTML += "  <tr>";
		divInteractionInnerHTML += "    <td width=\"2\" height=\"2\"><img src=\"Resource/button_container_07.gif\" width=\"2\" height=\"2\" alt=\"\"></td>";
		divInteractionInnerHTML += "    <td background=\"Resource/button_container_08.gif\"></td>";
		divInteractionInnerHTML += "    <td width=\"2\" height=\"2\"><img src=\"Resource/button_container_09.gif\" width=\"2\" height=\"2\" alt=\"\"></td>";
		divInteractionInnerHTML += "  </tr>";
		divInteractionInnerHTML += "</table>";
		
		divInteractionInnerHTML += "</td>\n";
		divInteractionInnerHTML += "</tr></table>\n";
		divInteractionInnerHTML += "</td>\n";
		divInteractionInnerHTML += "</tr>\n";
		divInteractionInnerHTML += "</table>\n";
		divInteractionInnerHTML += "</td>\n";
		divInteractionInnerHTML += "</tr>\n";
		divInteractionInnerHTML += "</table>\n";
		divInteractionInnerHTML += "</td>\n";
		divInteractionInnerHTML += "</tr>\n";
		divInteractionInnerHTML += "</table>\n";
		} else {
		divInteractionInnerHTML += "<tr>\n";
		divInteractionInnerHTML += "<td height=\"28\">\n";
		divInteractionInnerHTML += "<div align=\"left\"></div>\n";
		divInteractionInnerHTML += "<div align=\"right\">\n";
		divInteractionInnerHTML += "<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n";
		divInteractionInnerHTML += "<tr>\n";
		divInteractionInnerHTML += "<td valign=\"bottom\">\n";
		divInteractionInnerHTML += "<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n";
		divInteractionInnerHTML += "<tr>\n";
		divInteractionInnerHTML += "<td width=\"188\" height=\"12\" background=\"" + skin + "int_base.jpg\"></td>\n";
		divInteractionInnerHTML += "</tr>";
		divInteractionInnerHTML += "</table>\n";
		divInteractionInnerHTML += "</td>\n";
		divInteractionInnerHTML += "<td width=\"188\" height=\"28\" background=\"" + skin + "int_bg_ok.gif\" align=\"right\" valign=\"middle\">";
		
		//Bot�o OK
		divInteractionInnerHTML += "<table style=\"cursor: pointer;\" onkeydown=\"return interactionKeydown(event, " + doc.divCount + ", 'btnDetails', 0);\" onclick=\"return interactionCancel(" + doc.divCount + ");\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">";
		divInteractionInnerHTML += "  <tr>";
		divInteractionInnerHTML += "    <td width=\"2\" height=\"2\"><img src=\"Resource/button_container_01.gif\" width=\"2\" height=\"2\" alt=\"\"></td>";
		divInteractionInnerHTML += "    <td background=\"Resource/button_container_02.gif\"></td>";
		divInteractionInnerHTML += "    <td width=\"2\" height=\"2\"><img src=\"Resource/button_container_03.gif\" width=\"2\" height=\"2\" alt=\"\"></td>";
		divInteractionInnerHTML += "  </tr>";
		divInteractionInnerHTML += "  <tr>";
		divInteractionInnerHTML += "    <td background=\"Resource/button_container_04.gif\" width=\"2\"></td>";
		divInteractionInnerHTML += "    <td background=\"Resource/button_container_05.gif\">";
		divInteractionInnerHTML += "      <table>";
		divInteractionInnerHTML += "        <tr>";
		divInteractionInnerHTML += "          <td width=\"5\"></td>";
		divInteractionInnerHTML += "          <td width=\"18\">";
		divInteractionInnerHTML += "            <img width=\"16\" height=\"16\" src=\"Resource/int_ok.gif\">";
		divInteractionInnerHTML += "          </td>";
		divInteractionInnerHTML += "          <td>";
		divInteractionInnerHTML += "            <a style=\"" + textStyle + "\" id=\"btnOk\" href=\"#\" onkeydown=\"return interactionKeydown(event, " + doc.divCount + ", 'btnDetails', 0);\" onclick=\"return interactionCancel(" + doc.divCount + ");\" >"+ okStr +"</a>";
		divInteractionInnerHTML += "          </td>";
		divInteractionInnerHTML += "          <td width=\"5\"></td>"; 
		divInteractionInnerHTML += "        </tr>";
		divInteractionInnerHTML += "      </table>";
		divInteractionInnerHTML += "    </td>";
		divInteractionInnerHTML += "    <td background=\"Resource/button_container_06.gif\" width=\"2\"></td>";
		divInteractionInnerHTML += "  </tr>";
		divInteractionInnerHTML += "  <tr>";
		divInteractionInnerHTML += "    <td width=\"2\" height=\"2\"><img src=\"Resource/button_container_07.gif\" width=\"2\" height=\"2\" alt=\"\"></td>";
		divInteractionInnerHTML += "    <td background=\"Resource/button_container_08.gif\"></td>";
		divInteractionInnerHTML += "    <td width=\"2\" height=\"2\"><img src=\"Resource/button_container_09.gif\" width=\"2\" height=\"2\" alt=\"\"></td>";
		divInteractionInnerHTML += "  </tr>";
		divInteractionInnerHTML += "</table>";
		
		divInteractionInnerHTML += "</td>\n";
		divInteractionInnerHTML += "</tr>\n";
		divInteractionInnerHTML += "</table>\n";
		divInteractionInnerHTML += "</div>\n";
		divInteractionInnerHTML += "</td>\n";
		divInteractionInnerHTML += "</tr>";
		divInteractionInnerHTML += "</table>\n";
		divInteractionInnerHTML += "</td>\n";
		divInteractionInnerHTML += "</tr>\n";
		divInteractionInnerHTML += "</table>\n";
		}

		divInteractionInnerHTML += "</td>\n";
		divInteractionInnerHTML += "</tr>\n";
		divInteractionInnerHTML += "</table>";

		doc.body.appendChild(divInteraction);

		// Centraliza o div na tela
		centerInteractionMsg(doc, divInteraction);

		divInteraction.innerHTML = divInteractionInnerHTML;

		// Centraliza o div na tela
		centerInteractionMsg(doc, divInteraction);

		var interactionTable = findNode(divInteraction, "interactionTable");

		// Quando a mensagem ultrapassa a altura da janela
		if (interactionTable.offsetHeight > doc.body.clientHeight) {
		divInteraction.style.top = "1px";
		divInteraction.style.height = (doc.body.clientHeight - 2) + "px";

		var divMsg = findNode(divInteraction, "message");
		divMsg.innerHTML = "";
		divMsg.style.overflow = "scroll";
		divMsg.style.height = (doc.body.clientHeight - 59) + "px";
		divMsg.innerHTML = text;
		}
		
		setTimeout(function() {
		// Quando a mensagem ultrapassa a largura da janela
		if (interactionTable.offsetWidth > doc.body.clientWidth) {
		  divInteraction.style.left = "1px";

		  var divMsg = findNode(divInteraction, "message");
		  divMsg.style.width = (doc.body.clientWidth - 59) + "px";
		  divMsg.innerHTML = text;
		}
		}, 0);

		if(IE) {
		  // #### Cria um div com o zindex maior que o div acima
		  var divInteractionIframe = doc.createElement("div");
		  divInteractionIframe.id = "divInteractionIframe_" + doc.divCount;
		  divInteractionIframe.style.width = divInteraction.style.width;
		  divInteractionIframe.style.height = divInteraction.style.height;
		  divInteractionIframe.style.zIndex = doc.zoomCount-1;
		  divInteractionIframe.style.position = "absolute";
		  divInteractionIframe.style.display = "block";
		  divInteractionIframe.style.left = divInteraction.style.left;
		  divInteractionIframe.style.top = divInteraction.style.top;
		  divInteractionIframe.innerHTML = "<iframe frameborder='0' scrolling='no' width='"+ divInteraction.style.width +"' height='"+ (parseInt(divInteraction.style.height)+ 35) +"'></iframe>";
		  doc.body.appendChild(divInteractionIframe);
		}
		
		var btnOk = findNode(divInteraction, "btnOk");
		//setTimeout foi colocado para resolver um bug no Firefox onde, ao pressionar enter para sair
		//de um componente, a intera��o era exibida e logo em seguida finalizada.
		if (isFirefox) {
		setTimeout(function() {
		    btnOk.focus();
		  }, 0);
		} else {
		btnOk.focus();
		}*/
	}

	function findNode(node, name) {
		var r = null;
		var id = node.id || node.name;
		if (id == name)
			r = node;
		if (!r) {
			for (var i = 0; i < node.childNodes.length; i++) {
				r = findNode(node.childNodes.item(i), name);
				if (r)
					break;
			}
		}
		return r;
	}

	function centerInteractionMsg(doc, divInteraction) {
		var height = divInteraction.offsetHeight;

		var interactionTable = findNode(divInteraction, "interactionTable");
		if (interactionTable) {
			var tableHeight = interactionTable.offsetHeight;
			height = Math.max(height, tableHeight);
		}
		var divCenterWidth = (doc.body.clientWidth - divInteraction.offsetWidth) / 2;
		var divCenterHeight = (doc.body.clientHeight - height) / 2;
		divInteraction.style.left = (doc.body.scrollLeft + divCenterWidth)
				+ "px";
		divInteraction.style.top = (doc.body.scrollTop + divCenterHeight)
				+ "px";
	}

	function interactionConfirm(msg, f, fParams, fParent) {
		interaction(msg, ['Ok'], [f], [fParams], [fParent], true);
	}

	function openWFRGridSort(sys, f) {
		var left = (screen.width - 300) / 2;
		var top = (screen.height - 300) / 2;
		MM_openBrWindow(
				'grid_sort' + PAGES_EXTENSION + '?sys=' + sys + '&formID=' + f,
				'WFRGridSort',
				'toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=no,width=300,height=300,left='
						+ left + ',top=' + top);
	}

	function setGridSaveAction(grid, sys, form, fields, disableUserCustomize) {
		grid.getTemplate("layout").sys = sys;
		grid.getTemplate("layout").form = form;
		grid.getTemplate("layout").fields = fields;
		grid.getTemplate("layout").header = grid.getTemplate("top/item");
		grid.getTemplate("layout").grid = grid;
		grid.getTemplate("layout").actAjust = obj.getTemplate("layout")
				.getAction("adjustSize");
		var localDisableUserCustomize = disableUserCustomize;
		grid.getTemplate("layout").setAction(
				"adjustSize",
				function(e) {
					this.actAjust.call(this, e);
					if (!localDisableUserCustomize) {
						getAndEvalSync('setsize.do?sys=' + this.sys
								+ '&action=setsize&formID=' + this.form
								+ '&field=' + this.fields[this.header.$index]
								+ '&size='
								+ pt(this.header.element().style.width));
					}
				});
	}

	function removeEvents(obj) {
		try {
			obj.onclick = null;
			obj.onmouseover = null;
			obj.onmouseout = null;
			obj.onmousemove = null;
			obj.onmousedown = null;
			obj.onmouseup = null;
			obj.onkeypress = null;
			obj.onkeydown = null;
			obj.onkeyup = null;
			obj.onfocus = null;
			obj.onblur = null;
			obj.onchange = null;
			obj.parent = null;
			obj.parentClass = null;
		} catch (e) {
		}
	}

	function flushDocument() {
		var pfstart = new Date().getTime();
		for ( var i in document) {
			try {
				if (document[i]) {
					removeEvents(document[i]);
					try {
						if (document[i].flush)
							document[i].flush();
					} catch (e) {
					}
					try {
						document[i] = null;
					} catch (e) {
					}
				}
			} catch (e) {
			}
		}
		alert('Flush Document em ' + (new Date().getTime() - pfstart) + 'ms');
	}

	function navigationAction(sys, formID, comID, action, param) {
		if (!document.navAction) {
			document.navAction = true;
			document.location = action + '.do?sys=' + sys + '&formID=' + formID
					+ '&componentID=' + comID + '&action=' + action + '&param='
					+ param;
		}
	}

	function designRSButton(sys, formID, param, active) {
		var sel = '';
		var click = 'navigationAction(\'' + sys + '\', ' + formID
				+ ', -1, \'navigate\', \'' + param + '\');';
		if (active) {
			sel = '_des';
			click = '';
		}
		document.write('<td width=3></td><td>');
		document.write('<a class="' + (!active ? 'NavButton' : 'NavButtonDes')
				+ '" href="javascript:' + click + '"><img src="' + skin
				+ 'nav_' + param + sel + '.gif" border="0"></a>');
		document.write('</td>');
	}

	function designRSNavigation(sys, formID, first, previous, next, last) {
		document
				.write('<table border=0 cellspacing=0 cellpadding=0 align=right><tr>');
		designRSButton(sys, formID, 'first', first);
		designRSButton(sys, formID, 'previous', previous);
		designRSButton(sys, formID, 'next', next);
		designRSButton(sys, formID, 'last', last);
		document.write('</tr></table>');
	}

	function buttonOver(bt) {
		if (bt.onclick)
			bt.setAttribute('background', skin + 'button_back_over.gif');
	}

	function buttonOut(bt) {
		if (bt.onclick)
			bt.setAttribute('background', skin + 'button_back.gif');
	}

	// Testa o tipo de arquvo
	function checkTypeOfFile(fileObj, extensions) {

		indice = fileObj.value.lastIndexOf(".");
		tipo = fileObj.value.substr(indice + 1);

		if (extensions.indexOf(tipo.toUpperCase()) != -1) {
			return true;
		}

		return false;
	}

	/*
	 * Cria um Cookie
	 * name - Nome do Cookie
	 * value - Valor do Cookie
	 * [expires] - Data de expira��o do Cookie
	 *   (padr�o finalizar junto com a sess�o corrente)
	 * [path] - path v�lida para armazenar o Cookie
	 *   (padr�o pasta do cliente do navegador)
	 * [domain] - dominio onde ser� armazenado o Cookie
	 *   (padr�o dom�nio de onde o documento foi chamado)
	 * [secure] - Booleano indicando se o Cookie requer uma tranmiss�o segura
	 */

	function setCookie(name, value, expires, path, domain, secure) {
		var curCookie = name + "=" + escape(value)
				+ ((expires) ? "; expires=" + expires.toGMTString() : "")
				+ ((path) ? "; path=" + path : "")
				+ ((domain) ? "; domain=" + domain : "")
				+ ((secure) ? "; secure" : "");
		document.cookie = curCookie;
	}

	/*
	 name - nome do Cookie definido
	 retorna uma string contendo o valor do Cookie ou null
	 se o Cookie n�o existe
	 */

	function getCookie(name) {
		var dc = document.cookie;
		var prefix = name + "=";
		var begin = dc.indexOf("; " + prefix);
		if (begin == -1) {
			begin = dc.indexOf(prefix);
			if (begin != 0)
				return null;
		} else
			begin += 2;
		var end = document.cookie.indexOf(";", begin);
		if (end == -1)
			end = dc.length;
		return unescape(dc.substring(begin + prefix.length, end));
	}

	/*
	 name - nome do Cookie
	 [path] - path do Cookie (usada somente se o cookie foi criado em um path diferente)
	 [domain] - dom�nio do cookie (mesmo dom�nio onde foi definido na fun��o setCookie)
	 */

	function deleteCookie(name, path, domain) {
		if (getCookie(name)) {
			document.cookie = name + "=" + ((path) ? "; path=" + path : "")
					+ ((domain) ? "; domain=" + domain : "")
					+ "; expires=Thu, 01-Jan-70 00:00:01 GMT";
		}
	}

	// date - qualquer inst�ncia do Date object
	function fixDate(date) {
		var base = new Date(0);
		var skew = base.getTime();
		if (skew > 0)
			date.setTime(date.getTime() - skew);
	}

	function delphiStringToJavaStringSingleLine(str) {
		var quoted = false;

		if (str == null || typeof str == "undefined" || str == '\'\'') {
			return "";
		}

		for (var i = 0; i < str.length; i++) {
			if (str.charAt(i) == '\'') {
				quoted = !quoted;
			}

			if (!quoted) {
				if (str.charAt(i) == '#') {
					var j = i + 1;
					var code = '';

					while (j < str.length && str.charAt(j) != ' '
							&& !isNaN(str.charAt(j))) {
						code = code + str.charAt(j);
						j++;
					}

					if (code.length > 0) {
						str = str.substring(0, i) + String.fromCharCode(code)
								+ str.substring(j, str.length);
					}
				}
			}
		}

		str = str.replace(/''/g, "#@#@#");
		str = str.replace(/'/g, "");
		str = str.replace(/#@#@#/g, "'");

		return str;
	}

	function delphiStringToJavaString(str) {
		var result = "";

		var lines = str.split("\n");
		for (var i = 0; i < lines.length; i++) {
			var line = trim(lines[i]);
			line = line.replace(/\r/g, "").replace(/'?\s*\+$/, "");
			line = delphiStringToJavaStringSingleLine(line);

			result += line;
		}

		return result;
	}

	var webrun = new function() {

		function mknamespace(module) {

			var path = '';
			var rest = new String(module);
			var name = '';
			var temp = window;
			var i = rest.indexOf('.');
			while (i != -1) {
				name = rest.substring(0, i);
				path += name + '/';
				if (!temp[name]) {
					temp[name] = {};
				}
				temp = temp[name];
				rest = rest.substring(i + 1);
				i = rest.indexOf('.');
			}
			path += rest + '.js';
			return {
				parent : temp,
				name : rest,
				path : path
			}
		}

		this.included = new Array();

		this.include = function(url) {
			if (!this.included[url]) {
				var content = getContent(url, false, true);
				if (isIE) {
					var newScript = document.createElement("<script>");
					newScript.type = 'text/javascript';
					newScript.language = 'javascript';
					newScript.text = content;
					document.getElementsByTagName('head')[0]
							.appendChild(newScript);
				} else {
					window_eval(content);
				}
				this.included[url] = url;
			}
			return this.included[url];
		}

		this.construct = function(className, params) {
			var clazz = null;

			try {
				clazz = eval(className);
			} catch (e) {
			}
			if (!clazz) {
				alert(mknamespace(className).path)
				this.include(mknamespace(className).path);
				clazz = eval(className);
			}

			if (clazz) {
				return this.create(clazz, Array.prototype.slice.apply(
						arguments, [1]));
			} else {
				throw 'Class not found';
			}
		}

		this.create = function() {
			return this.newClass.apply(this, arguments);
		}

		this.newClass = function(constructor, arguments) {
			for (var i = 0, j = (arguments ? arguments.length : 0), Instance = new Array(
					j); i < j; i++)
				Instance[i] = "arguments[".concat(i, "]");
			return new Function("constructor, arguments",
					"return new constructor(".concat(Instance.join(","), ")"))(
					constructor, arguments);
		}
	}

	function window_eval(code) {
		var context = this;
		if (window.execScript) {
			window.execScript(code);
			return null;
		}
		return context.eval ? context.eval(code) : eval(code);
	}

	function IframeTransporter(url) {
		var id = 'IFRAME' + parseInt((Math.random() * 9999999));
		var iframe;
		if (IE) {
			iframe = $mainform().document.createElement("<iframe name='" + id
					+ "' id='" + id + "'>");
		} else {
			iframe = $mainform().document.createElement("iframe");
			iframe.name = id;
			iframe.id = id;
		}
		iframe.frameBorder = 0;
		iframe.setAttribute("frameborder", "no");
		iframe.setAttribute("border", 0);
		iframe.setAttribute("marginwidth", 0);
		iframe.setAttribute("marginheight", 0);
		iframe.width = 0;
		iframe.height = 0;
		iframe.src = url;
		$mainform().document.body.appendChild(iframe);
	}

	function arrayIndexRemove(arr, idx) {
		var newArray = new Array();
		for (var i = 0; i < arr.length; i++) {
			if (i != idx)
				newArray.push(arr[i]);
		}
		return newArray;
	}

	function WebrunMap() {
		this.keys = new Array();
		this.values = new Array();
		this.__mapInstance = true;
	}

	WebrunMap.prototype.getInstance = function(json) {
		var newMap = new WebrunMap();
		for ( var key in json) {
			if (typeof key == "function") {
				continue;
			}
			newMap.add(key, json[key]);
		}
		return newMap;
	}

	WebrunMap.prototype.getKeys = function() {
		return this.keys;
	}

	WebrunMap.prototype.getValues = function() {
		return this.values;
	}

	WebrunMap.prototype.size = function() {
		return this.keys.length;
	}

	WebrunMap.prototype.add = function(key, value) {
		if (this.validateKey(key)) {
			var foundKey = this.findKey(key);

			// Se a chave n�o for encontrada, apenas adiciona o novo valor
			// Caso contr�rio, atualiza o valor
			if (foundKey == -1) {
				this.keys.push(key);
				this.values.push(value);
			} else {
				this.values[foundKey] = value;
			}
		}
		return this;
	}

	WebrunMap.prototype.get = function(key) {
		if (this.validateKey(key)) {
			var foundKey = this.findKey(key);
			if (foundKey != -1) {
				return this.values[foundKey];
			}
		}
		return null;
	}

	WebrunMap.prototype.remove = function(key) {
		if (this.validateKey(key)) {
			var foundKey = this.findKey(key);
			if (foundKey != -1) {
				var size = this.size();

				var keys1 = this.keys.slice(0, foundKey);
				var keys2 = this.keys.slice(foundKey + 1, size);
				this.keys = new Array();
				this.keys = this.keys.concat(keys1, keys2);

				var values1 = this.values.slice(0, foundKey);
				var values2 = this.values.slice(foundKey + 1, size);
				this.values = new Array();
				this.values = this.values.concat(values1, values2);

				return true;
			}
		}

		return false;
	}

	WebrunMap.prototype.findKey = function(key) {
		if (this.validateKey(key)) {
			var keys = this.getKeys();
			for (var i = 0; i < keys.length; i++) {
				if (this.keys[i] == key) {
					return i;
				}
			}
		}
		return -1;
	}

	WebrunMap.prototype.validateKey = function(key) {
		return (key != null && typeof key != "undefined");
	}

	WebrunMap.prototype.flush = function(execFunction) {
		if (this.size() > 0) {
			if (execFunction) {
				var keys = this.getKeys();
				for (var i = 0; i < keys.length; i++) {
					var key = keys[i];
					var value = this.get(key);
					execFunction(key, value);
				}
			}
			this.keys = null;
			this.values = null;
		}
	}

	WebrunMap.prototype.toString = function() {
		var result = "{";
		if (this.size() > 0) {
			var keys = this.getKeys();
			for (var i = 0; i < keys.length; i++) {
				var key = keys[i];
				if (!isNullable(key)) {
					var value = this.get(key);
					result += ((i > 0 ? ", " : "") + key.toString() + "=" + (isNullable(value)
							? ""
							: value.toString()));
				}
			}
		}
		result += "}";
		return result;
	}

	WebrunMap.prototype.toStringSerialized = function(recursive) {
		var result = "{";
		if (this.size() > 0) {
			var keys = this.getKeys();
			for (var i = 0; i < keys.length; i++) {
				var key = keys[i];
				if (!isNullable(key)) {
					var value = this.get(key);
					result += ((i > 0 ? ", " : "")
							+ serialize(key, true, recursive) + ": " + serialize(
							value, true, recursive));
				}
			}
		}
		result += "}";
		return result;
	}

	function HTTPPool() {
		this.semaphore = false;
	}

	HTTPPool.prototype.get = function() {
		var http = getHTTPObject();
		return http;
	}

	HTTPPool.prototype.isInUse = function(http) {
		return false;
	}

	HTTPPool.prototype.leave = function(http) {
		if (http != null) {
			try {
				lastReceivedContent = convertNonUnicodeChars(http.responseText);
			} catch (e) {
			}
		}
	}

	HTTPPool.prototype.processAsyncGet = function(url, throwsException) {
		var http = httpPool.get();
		var pool = this;
		try {
			http.open('GET', url, true);
			http.send(null);

			http.onreadystatechange = function() {
				if (http.readyState == 4) {
					pool.leave(http);
				}
			}

		} catch (e) {
			if (throwsException)
				throw e;
			else
				this.processAsyncGet(url, true);
		}
	}

	HTTPPool.prototype.free = function() {
	}

	var httpPool = new HTTPPool();

	function getInstalledWebrunReports(version) {
		try {
			var control = new ActiveXObject('WebrunReports.WebrunReportsX');
			var controlVersion = '1,0,0,14';
			try {
				controlVersion = control.version;
			} catch (e) {
			}
			if (controlVersion != version)
				return null;
			else
				return control;
		} catch (e) {
		}
		return null;
	}

	var _session_variables = new WebrunMap();

	function addComponentDependences(code, value) {
		var dep = d.t.dependences;
		var depExist = dep[code];
		if (!depExist)
			dep[code] = new Array();
		dep[code].push(value);
	}

	function defineComponentDependences() {
		var dep = d.t.dependences;

		for ( var componentUpdateCode in dep) {
			if (typeof componentUpdateCode == "function") {
				continue;
			}

			var componentUpdate = eval("$mainform().d.c_" + componentUpdateCode);
			if ((componentUpdate == null)
					|| (typeof componentUpdate == "undefined")) {
				continue;
			}

			for ( var index in dep[componentUpdateCode]) {
				if (typeof index == "function") {
					continue;
				}

				var componentCode = dep[componentUpdateCode][index];
				if (isNumeric(componentCode)) {
					var component = eval("$mainform().d.c_" + componentCode);

					if ((component == null)
							|| (typeof component == "undefined")) {
						continue;
					}

					component.addComponentDependence(componentUpdate);
				}
			}
		}
	}

	function shortcutReloadSystem(sys) {
		if (sys != null && typeof sys != "undefined" && trim(sys) != ""
				&& confirm(getLocaleMessage("INFO.CONFIRM_RELOAD_SYSTEM"))) {
			window.location.href = "reloadSystem.do?sys=" + sys;
		} else {
			return false;
		}
	}

	function setFocusFormOnLoad() {
		var lMainForm = $mainform();
		if (lMainForm && lMainForm.focus) {
			lMainForm.focus();
		}
		return true;
	}

	function formatText(text) {
		if (arguments.length > 1) {
			for (var i = 1; i < arguments.length; i++) {
				var param = arguments[i];
				if (param != null && typeof param != "undefined") {
					var regexp = new RegExp("\\{" + (i - 1) + "\\}", "g");
					text = text.replace(regexp, param);
				}
			}
		}
		return text;
	}

	function testRegularExpression(value, regularExpression) {
		if (isNullable(value)) {
			return true;
		}
		return (value.search(regularExpression) == 0);
	}

	function processFilter(allFilters) {
		var result = allFilters;

		var filters = allFilters.split(";");
		if (filters.length > 0) {
			var objRegExp = /^\(\{.+?\}\)$/;
			result = "";

			for (var i = 0; i < filters.length; i++) {
				var filter = filters[i];
				var filterValues = filter.split("=");

				var key = filterValues[0];
				var filterValue = filter;
				if (filterValues.length > 1) {
					var strValue = filterValues[1];

					if (objRegExp.test(strValue)) {
						var objValue = eval(strValue);
						if (objValue && objValue.name) {
							var component = eval("$c('" + objValue.name
									+ "', '" + objValue.guid + "')");
							filterValue = (key + "=" + component.getValue());
							if (objValue.type && objValue.type.length > 0) {
								filterValue += ("@" + objValue.type);
							}
						}
					}
				}

				if (result.length > 0) {
					result += ";";
				}

				result += filterValue;
			}
		}

		return result;
	}

	function recursiveFlush(obj, depth, parent) {
		try {
			if (!depth)
				depth = 1;

			if ((typeof (obj) != 'string' && typeof (obj) != 'number' && depth <= 3)
					|| obj.isObject) {

				var aux;
				for ( var i in obj) {
					if (obj[i]) {
						aux = obj[i];
						obj[i] = null;
						recursiveFlush(aux, depth + 1, obj);
					}
				}
			}
		} catch (e) {
			//Abafa	  
		}
	}

	function clearReferences(o) {
		if (o.childNodes != undefined) {
			for (var i = o.childNodes.length - 1; i >= 0; i--) {
				if (o.childNodes[i].contentDocument)
					clearReferences(o.childNodes[i].contentDocument);
				clearReferences(o.childNodes[i]);
			}
		}
		if (o.onload)
			o.onload = null;
		if (o.onunload)
			o.onunload = null;
		if (o.onclick)
			o.onclick = null;
		if (o.ondblclick)
			o.ondblclick = null;
		if (o.onmousedown)
			o.onmousedown = null;
		if (o.onmouseup)
			o.onmouseup = null;
		if (o.onmouseover)
			o.onmouseover = null;
		if (o.onmousemove)
			o.onmousemove = null;
		if (o.onmouseout)
			o.onmouseout = null;
		if (o.onfocus)
			o.onfocus = null;
		if (o.onblur)
			o.onblur = null;
		if (o.onkeypress)
			o.onkeypress = null;
		if (o.onkeydown)
			o.onkeydown = null;
		if (o.onkeyup)
			o.onkeyup = null;
		if (o.onsubmit)
			o.onsubmit = null;
		if (o.onreset)
			o.onreset = null;
		if (o.onselect)
			o.onselect = null;
		if (o.onchange)
			o.onchange = null;

		o = null;
	}

	function generateContainerDiv() {
		var doc;
		try {
			doc = mainform.document;
		} catch (e) {
			doc = document;
		}

		if (!doc.body) {
			doc.writeln("<body></body>");
		}

		if (!doc.zoomCount)
			doc.zoomCount = 10000;
		if (!doc.divCount)
			doc.divCount = 1;

		doc.zoomCount = doc.zoomCount - 2;
		doc.divCount++;

		var divBckInteraction = doc.createElement("div");
		divBckInteraction.id = "divBckInteraction_" + doc.divCount;
		divBckInteraction.style.width = doc.body.clientWidth + 'px';
		divBckInteraction.style.height = '100%';
		divBckInteraction.style.left = doc.body.scrollLeft + "px";
		divBckInteraction.style.top = doc.body.scrollTop + "px";
		divBckInteraction.style.zIndex = doc.zoomCount;
		divBckInteraction.style.position = "absolute";
		divBckInteraction.style.backgroundColor = "#FFFFFF";
		divBckInteraction.innerHTML = "<table width=100% height=100%><tr><td>&nbsp;</td></tr></table>";
		divBckInteraction.style.filter = "alpha(opacity=30)";
		divBckInteraction.style.opacity = .3;

		doc.body.appendChild(divBckInteraction);

		var divInteraction = doc.createElement("div");
		divInteraction.id = "divInteraction_" + doc.divCount;
		divInteraction.style.width = doc.body.clientWidth + 'px';
		divInteraction.style.height = '100%';
		divInteraction.style.zIndex = doc.zoomCount + 1;
		divInteraction.style.left = doc.body.scrollLeft + "px";
		divInteraction.style.top = doc.body.scrollTop + "px";
		divInteraction.style.position = "absolute";
		divInteraction.style.display = "block";
		divInteraction.backDiv = divBckInteraction;

		doc.body.appendChild(divInteraction);

		return divInteraction;
	}

	function removeContainerDiv(div) {
		var doc;
		try {
			doc = mainform.document;
		} catch (e) {
			doc = document;
		}

		if (!doc.body) {
			doc.writeln("<body></body>");
		}

		if (div.backDiv) {
			div.backDiv.parentNode.removeChild(div.backDiv);
			div.backDiv = null;
		}

		div.parentNode.removeChild(div);
	}

	function containsNode(element, node) {
		if (element && element.childNodes) {
			for (var i = 0; i < element.childNodes.length; i++) {
				if (element.childNodes[i] == node) {
					return true;
				}
			}
		}
		return false;
	}

	function updateNotification() {
		var count = $jq("#notificationBody").children().size();

		if (count > 1) {
			$jq("#notificationResume").html(
					"Voc&#234; tem " + count + " notifica&#231;&#245;es");
		} else {
			$jq("#notificationResume").html(
					"Voc&#234; tem " + count + " notifica&#231;&#227;o");
		}

		if (count > 0) {
			$jq("#notification").show();
		} else {
			$jq("#notification").hide();
		}

		$jq("#notificationCount").html(count);
	}

	function newNotification(id, text, callbackApply, callbackClose) {
		$notification = $jq("#notification-" + id);
		$notificationBody = $jq("#notificationBody");

		if ($notification.size() == 0) {
			var html = "<li id=\"notification-" + id
					+ "\" class=\"notification-item\">";
			html += "<a href=\"#\">";
			html += "<div class=\"notification-text\">" + text + "</div>";
			html += "<div class=\"notification-close\"><i class=\"fa fa-close\"></i></div>";
			html += "</a>";
			html += "</li>";

			$notification = $jq(html);

			$notification.find("a").click(function(event) {
				callbackApply(dataId, "apply");
				event.stopPropagation();
				$notification.remove();
				updateNotification();
			});

			var dataId = id;

			$notification.find(".notification-close").click(function(event) {
				callbackClose(dataId, "close");
				event.stopPropagation();
				$notification.remove();
				updateNotification();
			});

			$notificationBody.append($notification);
		} else {
			$notification.find(".notification-text").html(text);
		}

		updateNotification();
	}

	function arrayConcat(arr1, arr2) {
		var r = new Array();
		if (arr1) {
			for (var i = 0; i < arr1.length; i++)
				r.push(arr1[i]);
		}
		if (arr2) {
			for (var i = 0; i < arr2.length; i++)
				r.push(arr2[i]);
		}
		return r;
	}

	function findPosX(obj) {
		var curleft = 0;
		if (obj.offsetParent) {
			while (obj.offsetParent) {
				curleft += obj.offsetLeft - obj.scrollLeft;
				obj = obj.offsetParent;
			}
		} else if (obj.x)
			curleft += obj.x;
		return curleft;
	}

	function findPosY(obj) {
		var curtop = 0;
		if (obj.offsetParent) {
			while (obj.offsetParent) {
				curtop += obj.offsetTop - obj.scrollTop;
				obj = obj.offsetParent;
			}
		} else if (obj.y)
			curtop += obj.y;
		return curtop;
	}

	function denyDrop(event) {
		return false;
	}

	Date.$VERSION = 1.02;
	Date.LZ = function(x) {
		return (x < 0 || x > 9 ? "" : "0") + x
	};
	Date.monthNames = new Array('January', 'February', 'March', 'April', 'May',
			'June', 'July', 'August', 'September', 'October', 'November',
			'December');
	Date.monthAbbreviations = new Array('Jan', 'Feb', 'Mar', 'Apr', 'May',
			'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec');
	Date.dayNames = new Array('Sunday', 'Monday', 'Tuesday', 'Wednesday',
			'Thursday', 'Friday', 'Saturday');
	Date.dayAbbreviations = new Array('Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri',
			'Sat');
	Date.preferAmericanFormat = true;
	if (!Date.prototype.getFullYear) {
		Date.prototype.getFullYear = function() {
			var yy = this.getYear();
			return (yy < 1900 ? yy + 1900 : yy);
		}
	}
	Date.parseString = function(val, format) {
		if (typeof (format) == "undefined" || format == null || format == "") {
			var generalFormats = new Array('y-M-d', 'MMM d, y', 'MMM d,y',
					'y-MMM-d', 'd-MMM-y', 'MMM d', 'MMM-d', 'd-MMM');
			var monthFirst = new Array('M/d/y', 'M-d-y', 'M.d.y', 'M/d', 'M-d');
			var dateFirst = new Array('d/M/y', 'd-M-y', 'd.M.y', 'd/M', 'd-M');
			var checkList = new Array(generalFormats, Date.preferAmericanFormat
					? monthFirst
					: dateFirst, Date.preferAmericanFormat
					? dateFirst
					: monthFirst);
			for (var i = 0; i < checkList.length; i++) {
				var l = checkList[i];
				for (var j = 0; j < l.length; j++) {
					var d = Date.parseString(val, l[j]);
					if (d != null) {
						return d;
					}
				}
			}
			return null;
		}
		this.isInteger = function(val) {
			for (var i = 0; i < val.length; i++) {
				if ("1234567890".indexOf(val.charAt(i)) == -1) {
					return false;
				}
			}
			return true;
		};
		this.getInt = function(str, i, minlength, maxlength) {
			for (var x = maxlength; x >= minlength; x--) {
				var token = str.substring(i, i + x);
				if (token.length < minlength) {
					return null;
				}
				if (this.isInteger(token)) {
					return token;
				}
			}
			return null;
		};
		val = val + "";
		format = format + "";
		var i_val = 0;
		var i_format = 0;
		var c = "";
		var token = "";
		var token2 = "";
		var x, y;
		var year = new Date().getFullYear();
		var month = 1;
		var date = 1;
		var hh = 0;
		var mm = 0;
		var ss = 0;
		var ampm = "";
		while (i_format < format.length) {
			c = format.charAt(i_format);
			token = "";
			while ((format.charAt(i_format) == c) && (i_format < format.length)) {
				token += format.charAt(i_format++);
			}
			if (token == "yyyy" || token == "yy" || token == "y") {
				if (token == "yyyy") {
					x = 4;
					y = 4;
				}
				if (token == "yy") {
					x = 2;
					y = 2;
				}
				if (token == "y") {
					x = 2;
					y = 4;
				}
				year = this.getInt(val, i_val, x, y);
				if (year == null) {
					return null;
				}
				i_val += year.length;
				if (year.length == 2) {
					if (year > 70) {
						year = 1900 + (year - 0);
					} else {
						year = 2000 + (year - 0);
					}
				}
			} else if (token == "MMM" || token == "NNN") {
				month = 0;
				var names = (token == "MMM"
						? (Date.monthNames.concat(Date.monthAbbreviations))
						: Date.monthAbbreviations);
				for (var i = 0; i < names.length; i++) {
					var month_name = names[i];
					if (val.substring(i_val, i_val + month_name.length)
							.toLowerCase() == month_name.toLowerCase()) {
						month = (i % 12) + 1;
						i_val += month_name.length;
						break;
					}
				}
				if ((month < 1) || (month > 12)) {
					return null;
				}
			} else if (token == "EE" || token == "E") {
				var names = (token == "EE"
						? Date.dayNames
						: Date.dayAbbreviations);
				for (var i = 0; i < names.length; i++) {
					var day_name = names[i];
					if (val.substring(i_val, i_val + day_name.length)
							.toLowerCase() == day_name.toLowerCase()) {
						i_val += day_name.length;
						break;
					}
				}
			} else if (token == "MM" || token == "M") {
				month = this.getInt(val, i_val, token.length, 2);
				if (month == null || (month < 1) || (month > 12)) {
					return null;
				}
				i_val += month.length;
			} else if (token == "dd" || token == "d") {
				date = this.getInt(val, i_val, token.length, 2);
				if (date == null || (date < 1) || (date > 31)) {
					return null;
				}
				i_val += date.length;
			} else if (token == "hh" || token == "h") {
				hh = this.getInt(val, i_val, token.length, 2);
				if (hh == null || (hh < 1) || (hh > 12)) {
					return null;
				}
				i_val += hh.length;
			} else if (token == "HH" || token == "H") {
				hh = this.getInt(val, i_val, token.length, 2);
				if (hh == null || (hh < 0) || (hh > 23)) {
					return null;
				}
				i_val += hh.length;
			} else if (token == "KK" || token == "K") {
				hh = this.getInt(val, i_val, token.length, 2);
				if (hh == null || (hh < 0) || (hh > 11)) {
					return null;
				}
				i_val += hh.length;
				hh++;
			} else if (token == "kk" || token == "k") {
				hh = this.getInt(val, i_val, token.length, 2);
				if (hh == null || (hh < 1) || (hh > 24)) {
					return null;
				}
				i_val += hh.length;
				hh--;
			} else if (token == "mm" || token == "m") {
				mm = this.getInt(val, i_val, token.length, 2);
				if (mm == null || (mm < 0) || (mm > 59)) {
					return null;
				}
				i_val += mm.length;
			} else if (token == "ss" || token == "s") {
				ss = this.getInt(val, i_val, token.length, 2);
				if (ss == null || (ss < 0) || (ss > 59)) {
					return null;
				}
				i_val += ss.length;
			} else if (token == "a") {
				if (val.substring(i_val, i_val + 2).toLowerCase() == "am") {
					ampm = "AM";
				} else if (val.substring(i_val, i_val + 2).toLowerCase() == "pm") {
					ampm = "PM";
				} else {
					return null;
				}
				i_val += 2;
			} else {
				if (val.substring(i_val, i_val + token.length) != token) {
					return null;
				} else {
					i_val += token.length;
				}
			}
		}
		if (i_val != val.length) {
			return null;
		}
		if (month == 2) {
			if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
				if (date > 29) {
					return null;
				}
			} else {
				if (date > 28) {
					return null;
				}
			}
		}
		if ((month == 4) || (month == 6) || (month == 9) || (month == 11)) {
			if (date > 30) {
				return null;
			}
		}
		if (hh < 12 && ampm == "PM") {
			hh = hh - 0 + 12;
		} else if (hh > 11 && ampm == "AM") {
			hh -= 12;
		}
		return new Date(year, month - 1, date, hh, mm, ss);
	}
	Date.isValid = function(val, format) {
		return (Date.parseString(val, format) != null);
	}
	Date.prototype.isBefore = function(date2) {
		if (date2 == null) {
			return false;
		}
		return (this.getTime() < date2.getTime());
	}
	Date.prototype.isAfter = function(date2) {
		if (date2 == null) {
			return false;
		}
		return (this.getTime() > date2.getTime());
	}
	Date.prototype.equals = function(date2) {
		if (date2 == null) {
			return false;
		}
		return (this.getTime() == date2.getTime());
	}
	Date.prototype.equalsIgnoreTime = function(date2) {
		if (date2 == null) {
			return false;
		}
		var d1 = new Date(this.getTime()).clearTime();
		var d2 = new Date(date2.getTime()).clearTime();
		return (d1.getTime() == d2.getTime());
	}
	Date.prototype.format = function(format) {
		format = format + "";
		var result = "";
		var i_format = 0;
		var c = "";
		var token = "";
		var y = this.getYear() + "";
		var M = this.getMonth() + 1;
		var d = this.getDate();
		var E = this.getDay();
		var H = this.getHours();
		var m = this.getMinutes();
		var s = this.getSeconds();
		var yyyy, yy, MMM, MM, dd, hh, h, mm, ss, ampm, HH, H, KK, K, kk, k;
		var value = new Object();
		if (y.length < 4) {
			y = "" + (+y + 1900);
		}
		value["y"] = "" + y;
		value["yyyy"] = y;
		value["yy"] = y.substring(2, 4);
		value["M"] = M;
		value["MM"] = Date.LZ(M);
		value["MMM"] = Date.monthNames[M - 1];
		value["NNN"] = Date.monthAbbreviations[M - 1];
		value["d"] = d;
		value["dd"] = Date.LZ(d);
		value["E"] = Date.dayAbbreviations[E];
		value["EE"] = Date.dayNames[E];
		value["H"] = H;
		value["HH"] = Date.LZ(H);
		if (H == 0) {
			value["h"] = 12;
		} else if (H > 12) {
			value["h"] = H - 12;
		} else {
			value["h"] = H;
		}
		value["hh"] = Date.LZ(value["h"]);
		value["K"] = value["h"] - 1;
		value["k"] = value["H"] + 1;
		value["KK"] = Date.LZ(value["K"]);
		value["kk"] = Date.LZ(value["k"]);
		if (H > 11) {
			value["a"] = "PM";
		} else {
			value["a"] = "AM";
		}
		value["m"] = m;
		value["mm"] = Date.LZ(m);
		value["s"] = s;
		value["ss"] = Date.LZ(s);
		while (i_format < format.length) {
			c = format.charAt(i_format);
			token = "";
			while ((format.charAt(i_format) == c) && (i_format < format.length)) {
				token += format.charAt(i_format++);
			}
			if (typeof (value[token]) != "undefined") {
				result = result + value[token];
			} else {
				result = result + token;
			}
		}
		return result;
	}
	Date.prototype.getDayName = function() {
		return Date.dayNames[this.getDay()];
	}
	Date.prototype.getDayAbbreviation = function() {
		return Date.dayAbbreviations[this.getDay()];
	}
	Date.prototype.getMonthName = function() {
		return Date.monthNames[this.getMonth()];
	}
	Date.prototype.getMonthAbbreviation = function() {
		return Date.monthAbbreviations[this.getMonth()];
	}
	Date.prototype.clearTime = function() {
		this.setHours(0);
		this.setMinutes(0);
		this.setSeconds(0);
		this.setMilliseconds(0);
		return this;
	}
	Date.prototype.add = function(interval, number) {
		if (typeof (interval) == "undefined" || interval == null
				|| typeof (number) == "undefined" || number == null) {
			return this;
		}
		number = +number;
		if (interval == 'y') {
			this.setFullYear(this.getFullYear() + number);
		} else if (interval == 'M') {
			this.setMonth(this.getMonth() + number);
		} else if (interval == 'd') {
			this.setDate(this.getDate() + number);
		} else if (interval == 'w') {
			var step = (number > 0) ? 1 : -1;
			while (number != 0) {
				this.add('d', step);
				while (this.getDay() == 0 || this.getDay() == 6) {
					this.add('d', step);
				}
				number -= step;
			}
		} else if (interval == 'h') {
			this.setHours(this.getHours() + number);
		} else if (interval == 'm') {
			this.setMinutes(this.getMinutes() + number);
		} else if (interval == 's') {
			this.setSeconds(this.getSeconds() + number);
		}
		return this;
	}

}).bind(window)();
