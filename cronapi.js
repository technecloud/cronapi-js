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
	 * @param {string} astring
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
	/**
	 * @type function
	 * @name {{sdsds}}  Para lógico
	 * @nameSynonymous toBoolean
	 * @description Função para converter para lógico
	 * @description_en Função para converter para lógico
	 
	 * @param {string} value
	 */
	this.cronapi.conversion.toBoolean = function(value) {
    return parseBoolean(value);
  }

	/**
	 * @category XML
	 * @categorySynonymous XML|xml
	 */
	this.cronapi.xml = {};

	/**
	 * @type function
	 * @name Obtém valor do elemento
	 * @nameSynonymous XMLGetElementValue
	 * @description Função que retorna o valor de um elemento
	 * @param {string} node - Elemento passado para obter-se o valor;
	 */
	this.cronapi.xml.XMLGetElementValue = function(node) {
		if (node.firstChild)
			return node.firstChild.nodeValue;
		else
			return null;
	};

	/**
	 * @type function
	 * @name Obtém o primeiro filho do elemento
	 * @nameSynonymous XMLGetChildElement
	 * @description Função para retornar o nó
	 * @param {Object} node - Elemento passado para obter-se o valor;
	 * @param {string} childName - Filho a ser obtido do elemento;
	 */
	this.cronapi.xml.XMLGetChildElement = function(node, childName) {
		var c = node.getElementsByTagName(childName);
		if (c.length > 0)
			return c[0];
	};

	/**
	 * @type function
	 * @name Obtém a raiz do elemento
	 * @nameSynonymous XMLGetRoot
	 * @description Função que retorna o elemento raiz a partir de um elemento
	 * @param {Object} element - Elemento passado para obter-se a raiz
	 */
	this.cronapi.xml.XMLGetRoot = function(element) {
		if (element)
			return doc.documentElement;
	};

	/**
	 * @type function
	 * @name Obtém o atributo do elemento
	 * @nameSynonymous XMLGetAttribute
	 * @description Função que retorna o elemento raiz a partir de um elemento
	 * @param {Object} element - Elemento passado para obter-se a raiz
	 * @param {Object} attribute - Atributo a ser obtido
	 */
	this.cronapi.xml.XMLGetAttribute = function(element, attribute) {
		return node.getAttribute(attribute);
	}

	/**
	 * @type function
	 * @name Cria Document
	 * @nameSynonymous XMLOpen
	 * @description Função que cria um objeto Document a partir de uma String
	 * @param {Object} XMLText - Elemento passado para obter-se a raiz
	 */
	this.cronapi.xml.XMLOpen = function(XMLText) {
		var doc = null;
		if (document.implementation && document.implementation.createDocument) { //Mozzila
			var domParser = new DOMParser();
			doc = domParser.parseFromString(XMLText, 'application/xml');
			fixXMLDocument(doc);
			return doc;
		} else {//IE
			doc = new ActiveXObject("MSXML2.DOMDocument");
			doc.loadXML(XMLText);
		}
		return doc;
	}

	/**
	 * @type function
	 * @name Busca filhos do elemento
	 * @nameSynonymous XMLGetChildrenElement
	 * @description Função que retorna os filhos do tipo de um determinado elemento
	 * @param {Object} node - Elemento passado para buscar os filhos
	 * @param {Object} childName - Elemento do tipo a ser buscado
	 */
	this.cronapi.xml.XMLGetChildrenElement = function(node, childName) {
		if (childName) {
			return node.getElementsByTagName(childName);
		} else {
			return node.childNodes;
		}
	}

	/**
	 * @type function
	 * @name Retorna o elemento pai
	 * @nameSynonymous XMLGetParentElement
	 * @description Função que retorna o pai de um elemento
	 * @param {Object} node - Elemento a ser buscado o pai
	 */
	this.cronapi.xml.XMLGetParentElement = function XMLGetParentElement(node) {
		return node.parentNode;
	};

	/**
	 * @type function
	 * @name Retorna a tag do elemento
	 * @nameSynonymous XMLGetElementTagName
	 * @description Função que retorna o nome da tag do elemento
	 * @param {Object} node - Elemento a ser buscado a tag
	 */
	this.cronapi.xml.XMLGetElementTagName = function XMLGetElementTagName(node) {
		return node.tagName;
	};

	//Private variables and functions
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

	var removeAccents = function (value) {
		withAccents = 'áàãâäéèêëíìîïóòõôöúùûüçÁÀÃÂÄÉÈÊËÍÌÎÏÓÒÕÖÔÚÙÛÜÇ';
		withoutAccents = 'aaaaaeeeeiiiiooooouuuucAAAAAEEEEIIIIOOOOOUUUUC';
		newValue = '';
		for (i = 0; i < value.length; i++) {
			if (withAccents.search(value.substr(i, 1)) >= 0) {
				newValue += withoutAccents.substr(withAccents.search(value.substr(i, 1)), 1);
			} else {
				newValue += value.substr(i, 1);
			}
		}
		return newValue;
	}

	var fix = function(v) {
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
					//Abafa erro de conversão
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

	var visibility = function(obj, v) {
		if (obj.style) {
			obj = obj.style;
			v = (v == 'show') ? 'visible' : (v == 'hide') ? 'hidden' : v;
		}
		obj.visibility = v;
	}

	var getPath = function(url) {
		var regexS = "(.*/).*\\?.*";
		var regex = new RegExp(regexS);
		var results = regex.exec(url);
		if (results == null)
			return "";
		else
			return results[1];
		return url;
	}

	var getParameterByName = function(name, url) {
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

	var pt = function(v) {
		var r = parseInt(v.replace('px', '').replace('pt', ''));
		if (isNaN(r))
			return document.body.clientHeight;
		else
			return r;
	}

	var trim = function(str) {
		var result = "";
		if (str) {
			result = str.toString().replace(/^\s+|\s+$/g, '');
		}
		return result;
	}

	var replaceAll = function(str, token, newToken) {
		return str.toString().split(token).join(newToken);
	}

	var convertNonUnicodeChars = function(value) {
		if (IE && !isNullable(value) && value.length > 0) {
			return value.replace(/\x80/g, String.fromCharCode(8364));
		}
		return value;
	}

	var getAbsolutContextPath = function() {
		var indice = location.pathname.lastIndexOf('/');
		var path = location.pathname.substring(0, indice + 1);
		var url = location.protocol + "//" + location.host + path;
		return url;
	}

	var getXMLContent = function(url, successCB, errorCB) {
		$.ajax({
			type : "GET",
			url : url,
			success : successCB,
			error : errorCB,
			dataType : "xml"
		});
	}

	var getContent = function(url, successCB, errorCB) {
		$.ajax({
			type : "GET",
			url : url,
			success : successCB,
			error : errorCB,
			dataType : "xml"
		});
	}

	var setFocus = function() {
		try {
			$(window).focus()
		} catch (e) {
		}
	}

	var getWindowHeight = function() {
		$(window).height();
	}

	var getWindowWidth = function() {
		$(window).width();
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

	var URLEncode = function(plaintext, forceUTF8) {
		if (ENCODING == "UTF-8" || forceUTF8) {
			return Url.encode(plaintext);
		} else {
			return URLEncode2(plaintext);
		}
	}

	var URLEncode3 = function(plaintext) {
		if (plaintext == null || typeof (plaintext) == 'undefined'
				|| plaintext === '' || plaintext.toString() == 'NaN') {
			return "";
		}
		return encodeURIComponent(plaintext);
	}

	var URLEncode2 = function(plaintext) {
		if (plaintext == null || typeof (plaintext) == 'undefined'
				|| plaintext === '' || plaintext.toString() == 'NaN') {
			return "";
		}

		plaintext = plaintext.toString();
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

	var stringToHTMLString = function(value) {
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

	var stringToJs = function(value) {
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

	var isTypeOf = function(obj, clazz) {
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

	var serialize = function(_obj, useQuot, recursive, emptyIfNull) {
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

	var isNullable = function(value, dontCheckEmpty) {
		return (value == null || typeof value == 'undefined'
				|| (!dontCheckEmpty && value === '') || (value.toString && value
				.toString() == 'NaN'));
	}

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
	}

	var parseNumeric = function(value) {
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

	var removeZeroFromBeginString = function(value) {
		if (!isNullable(value)) {
			while (value.charAt(0) == "0" && value.length > 1) {
				value = value.substring(1);
			}
		}
		return value;
	}

	var firstToUpper = function(texto) {
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

	var removeEvents = function(obj) {
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

	var flushDocument = function() {
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

	var setCookie = function(name, value, expires, path, domain, secure) {
		var curCookie = name + "=" + escape(value)
				+ ((expires) ? "; expires=" + expires.toGMTString() : "")
				+ ((path) ? "; path=" + path : "")
				+ ((domain) ? "; domain=" + domain : "")
				+ ((secure) ? "; secure" : "");
		document.cookie = curCookie;
	}

	var getCookie = function(name) {
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

	var deleteCookie = function(name, path, domain) {
		if (getCookie(name)) {
			document.cookie = name + "=" + ((path) ? "; path=" + path : "")
					+ ((domain) ? "; domain=" + domain : "")
					+ "; expires=Thu, 01-Jan-71 00:00:01 GMT";
		}
	}

	var fixDate = function(date) {
		var base = new Date(0);
		var skew = base.getTime();
		if (skew > 0)
			date.setTime(date.getTime() - skew);
	}

	var arrayIndexRemove = function(arr, idx) {
		arr.splice(idx, 1);
		return arr;
	}

	var testRegularExpression = function(value, regularExpression) {
		if (isNullable(value)) {
			return true;
		}
		return (value.search(regularExpression) == 0);
	}

	var clearReferences = function(o) {
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

	var arrayConcat = function(arr1, arr2) {
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

}).bind(window)();
