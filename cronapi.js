(function() {
	'use strict';

	this.cronapi = {};

    this.doEval = function(arg) {
        return arg;
    }

    this.evalInContext = function(js) {
        return eval('doEval('+js+')');
    }

	/**
	 * @category CategoryType.CONVERSION
	 * @categoryTags Conversão|Convert
	 */
	this.cronapi.conversion = {};

	/**
	 * @type function
	 * @name {{textToTextBinary}}
	 * @nameTags asciiToBinary
	 * @description {{functionToConvertTextInTextBinary}}
	 * @param {ObjectType.STRING} astring {{contentInAscii}}
	 * @returns {ObjectType.STRING}
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
	 * @name {{toLogic}}
	 * @nameTags toBoolean
	 * @description {{functionConvertToLogic}}
	 * @param {ObjectType.STRING} value {{content}}
	 * @returns {ObjectType.BOOLEAN}
	 */
	this.cronapi.conversion.toBoolean = function(value) {
		return parseBoolean(value);
	};

	/**
	 * @type function
	 * @name {{convertToBytes}}
	 * @nameTags toBytes
	 * @description {{functionToConvertTextBinaryToText}}
	 * @param {ObjectType.OBJECT} obj {{contentInTextBinary}}
	 * @returns {ObjectType.STRING}
	 */
	this.cronapi.conversion.toBytes = function(obj) {
		return obj ? obj.toString() : "";
	};

	/**
	 * @type function
	 * @name {{convertToAscii}}
	 * @nameTags chrToAscii|convertToAscii
	 * @description {{functionToConvertToAscii}}
	 * @param {ObjectType.STRING} value {{content}}
	 * @returns {ObjectType.STRING}
	 */
	this.cronapi.conversion.chrToAscii = function(value) {
		if (!value) {
			return null;
		} else {
			return (value.charCodeAt(0));
		}
	};

	/**
	 * @type function
	 * @name {{convertStringToJs}}
	 * @nameTags stringToJs
	 * @description {{functionToConvertStringToJs}}
	 * @param {ObjectType.STRING} value {{content}}
	 * @returns {ObjectType.STRING}
	 */
	this.cronapi.conversion.stringToJs = function(value) {
		return stringToJs(value);
	};

	/**
	 * @type function
	 * @name {{convertStringToDate}}
	 * @nameTags stringToDate
	 * @description {{functionToConvertStringToDate}}
	 * @param {ObjectType.STRING} value {{content}}
	 * @returns {ObjectType.DATETIME}
	 */
	this.cronapi.conversion.stringToDate = function(value) {
		var pattern = /^\s*(\d+)[\/\.-](\d+)[\/\.-](\d+)(\s(\d+):(\d+):(\d+))?\s*$/;
		if (value) {
			if (value instanceof Date)
				return value;
			else if (pattern.test(value)) {
				var splited = pattern.exec(value);
				var userLang = (navigator.language || navigator.userLanguage)
						.split("-")[0];

				if (userLang == "pt" || userLang == "en") {
					var functionToCall = eval(userLang + "Date");
					return functionToCall(splited);
				} else
					return new Date(value);
			} else
				return new Date(value);
		}
		return null;
	};

	/**
	 * @type function
	 * @name {{convertIntToHex}}
	 * @nameTags intToHex
	 * @description {{functionToConvertIntToHex}}
	 * @param {ObjectType.STRING} value {{content}}
	 * @returns {ObjectType.STRING}
	 */
	this.cronapi.conversion.intToHex = function(value) {
		return Number(value).toString(16).toUpperCase();
	};

	/**
	 * @type function
	 * @name {{convertToLong}}
	 * @nameTags toLong
	 * @description {{functionToConvertToLong}}
	 * @param {ObjectType.OBJECT} value {{content}}
	 * @returns {ObjectType.LONG}
	 */
	this.cronapi.conversion.toLong = function(value) {
		return parseInt(value);
	};

	/**
	 * @type function
	 * @name {{convertToString}}
	 * @nameTags toString
	 * @description {{functionToConvertToString}}
	 * @param {ObjectType.OBJECT} value {{content}}
	 * @returns {ObjectType.STRING}
	 */
	this.cronapi.conversion.toString = function(value) {
		if (value)
			return new String(value)
		return "";
	};
	
	/**
	 * @category CategoryType.UTIL
	 * @categoryTags Util
	 */
	this.cronapi.util = {};

  /**
   * @type internal
   * @name {{callServerBlocklyAsync}}
   * @nameTags callServerBlocklyAsync
   * @description {{functionToCallServerBlocklyAsync}}
   * @param {ObjectType.STRING} classNameWithMethod {{classNameWithMethod}}
   * @param {ObjectType.OBJECT} callbackSuccess {{callbackSuccess}}
   * @param {ObjectType.OBJECT} callbackError {{callbackError}}
   * @param {ObjectType.OBJECT} params {{params}}
   * @arbitraryParams true
   */
  this.cronapi.util.callServerBlocklyAsync = function(classNameWithMethod, callbackSuccess, callbackError) {
    var serverUrl = 'api/cronapi/call/#classNameWithMethod#/'.replace('#classNameWithMethod#', classNameWithMethod);
    var http = cronapi.$scope.http;
    var params = [];
    $(arguments).each(function() {
      params.push(this);
    });

    var token = "";
    if (window.uToken)
      token = window.uToken;

    var resultData = $.ajax({
      type: 'POST',
      url: serverUrl,
      dataType: 'html',
      data : JSON.stringify(params.slice(3)),
      headers : {
        'Content-Type' : 'application/json',
        'X-AUTH-TOKEN' : token,
        'toJS' : true
      },
      success : callbackSuccess,
      error : callbackError
    });

  };

  /**
   * @type internal
   * @name {{makeCallServerBlocklyAsync}}
   * @nameTags makeCallServerBlocklyAsync
   * @description {{functionToMakeCallServerBlocklyAsync}}
   * @param {ObjectType.STRING} blocklyWithFunction {{blocklyWithFunction}}
   * @param {ObjectType.STRING} callbackBlocklySuccess {{callbackBlocklySuccess}}
   * @param {ObjectType.STRING} callbackBlocklyError {{callbackBlocklyError}}
   * @param {ObjectType.OBJECT} params {{params}}
   * @arbitraryParams true
   */
  this.cronapi.util.makeCallServerBlocklyAsync = function(blocklyWithFunction, callbackSuccess, callbackError) {
    var paramsApply = [];
    paramsApply.push(blocklyWithFunction);
    paramsApply.push(function(data) {
      if (typeof callbackSuccess == "string") {
        eval(callbackSuccess)(evalInContext(data));
      } else if (callbackSuccess) {
        callbackSuccess(evalInContext(data));
      }
    });
    paramsApply.push(function(data, status, errorThrown) {
      if (typeof callbackError == "string") {
        eval(callbackError)(errorThrown);
      }
      else if (callbackError) {
        callbackError(errorThrown);
      }
      else {
        var message = 'Unknown error';
        if (errorThrown)
          message = errorThrown;
        cronapi.$scope.Notification.error(message);
      }
    });
    $(arguments).each(function(idx) {
      if (idx >= 3)
        paramsApply.push(this);
    });
    cronapi.util.callServerBlocklyAsync.apply(this, paramsApply);
  };

  /**
   * @type function
   * @name {{callServerBlockly}}
   * @nameTags callServerBlockly
   * @description {{functionToCallServerBlockly}}
   * @param {ObjectType.STRING} classNameWithMethod {{classNameWithMethod}}
   * @param {ObjectType.OBJECT} params {{params}}
   * @arbitraryParams true
   * @wizard procedures_callblockly_callnoreturn
   */
  this.cronapi.util.callServerBlocklyNoReturn = function() {
    cronapi.util.callServerBlockly.apply(this, arguments);
  }

  /**
   * @type function
   * @name {{callServerBlockly}}
   * @nameTags callServerBlockly
   * @description {{functionToCallServerBlockly}}
   * @param {ObjectType.STRING} classNameWithMethod {{classNameWithMethod}}
   * @param {ObjectType.OBJECT} params {{params}}
   * @arbitraryParams true
   * @wizard procedures_callblockly_callreturn
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.util.callServerBlockly = function(classNameWithMethod) {
    var serverUrl = 'api/cronapi/call/#classNameWithMethod#/'.replace('#classNameWithMethod#', classNameWithMethod);
    var params = [];

    for (var i = 1; i<arguments.length; i++)
      params.push(arguments[i]);

    var token = "";
    if (window.uToken)
      token = window.uToken;

    var resultData = $.ajax({
      type: 'POST',
      url: serverUrl,
      dataType: 'html',
      data : JSON.stringify(params),
      async: false,
      headers : {
        'Content-Type' : 'application/json',
        'X-AUTH-TOKEN' : token,
        'toJS' : true
      }
    });

    var result;
    if (resultData.status == 200) {
      if (resultData.responseJSON)
        result = resultData.responseJSON;
      else
        result = evalInContext(resultData.responseText);
    }
    else {
      cronapi.$scope.Notification.error(resultData.statusText);
    }
    return result;
  };
  
  /**
	 * @category CategoryType.SCREEN
	 * @categoryTags Screen|Tela
	 */
	this.cronapi.screen = {};
	
	/**
	 * @type function
	 * @name {{fieldNameFromScreen}}
	 * @nameTags fieldNameFromScreen
	 * @description {{functionToGetFieldNameFromScreen}}
	 * @param {ObjectType.STRING} field {{field}}
	 * @returns {ObjectType.OBJECT}
	 * @wizard field_from_screen
	 */
	this.cronapi.screen.fieldFromScreen = function(field) {
	  return field;
	};
	
	/**
	 * @type function
	 * @name {{changeValueOfField}}
	 * @nameTags changeValueOfField|changeFieldValue
	 * @description {{functionToChangeValueOfField}}
	 * @param {ObjectType.STRING} field {{field}}
	 * @param {ObjectType.STRING} value {{value}}
	 */
	this.cronapi.screen.changeValueOfField = function(/** @type {ObjectType.BLOCK} @blockType field_from_screen*/ field, /** @type {ObjectType.STRING} */value) {
	  try {
	    cronapi.$scope.__tempValue = value;
	    var func = new Function('cronapi.$scope.' + field + ' = cronapi.$scope.__tempValue;');
	    cronapi.$scope.safeApply(func.bind(cronapi.$scope));
	  }
	  catch (e) {
	    alert(e);
	  }
	};
	
	/**
	 * @type function
	 * @name {{getValueOfField}}
	 * @nameTags getValueOfField|getFieldValue
	 * @description {{functionToGetValueOfField}}
	 * @param {ObjectType.STRING} field {{field}}
	 * @returns {ObjectType.OBJECT}
	 * @displayInline true
	 */
	this.cronapi.screen.getValueOfField = function(/** @type {ObjectType.BLOCK} @blockType field_from_screen*/ field) {
	  try {
	    if (field && field.length > 0) {
	      if (field.indexOf('vars.') > -1)
	        return eval('cronapi.$scope.'+field);
	      else
	       return eval(field);
	    }
	    return '';
	  }
	  catch (e) {
	    alert(e);
	  }
	};
  
	/**
	 * @category CategoryType.DATETIME
	 * @categoryTags Date|Datetime|Data|Hora
	 */
	this.cronapi.dateTime = {};

	/**
	 * @type function
	 * @name {{getSecondFromDate}}
	 * @nameTags getSecond
	 * @description {{functionToGetSecondFromDate}}
	 * @param {ObjectType.DATETIME} value {{ObjectType.DATETIME}}
	 * @returns {ObjectType.LONG}
	 */
	this.cronapi.dateTime.getSecond = function(value) {
		var date = cronapi.conversion.stringToDate(value);
		if (date)
			return date.getSeconds();
		return 0;
	};

	/**
	 * @type function
	 * @name {{getMinuteFromDate}}
	 * @nameTags getMinute
	 * @description {{functionToGetMinuteFromDate}}
	 * @param {ObjectType.DATETIME} value {{ObjectType.DATETIME}}
	 * @returns {ObjectType.LONG}
	 */
	this.cronapi.dateTime.getMinute = function(value) {
		var date = cronapi.conversion.stringToDate(value);
		if (date)
			return date.getMinutes();
		return 0;
	};

	/**
	 * @type function
	 * @name {{getHourFromDate}}
	 * @nameTags getHour
	 * @description {{functionToGetHourFromDate}}
	 * @param {ObjectType.DATETIME} value {{ObjectType.DATETIME}}
	 * @returns {ObjectType.LONG}
	 */
	this.cronapi.dateTime.getHour = function(value) {
		var date = cronapi.conversion.stringToDate(value);
		if (date)
			return date.getHours();
		return 0;
	};

	/**
	 * @type function
	 * @name {{getYearFromDate}}
	 * @nameTags getYear
	 * @description {{functionToGetYearFromDate}}
	 * @param {ObjectType.DATETIME} value {{ObjectType.DATETIME}}
	 * @returns {ObjectType.LONG}
	 */
	this.cronapi.dateTime.getYear = function(value) {
		var date = cronapi.conversion.stringToDate(value);
		if (date)
			return date.getFullYear();
		return 0;
	};

	/**
	 * @type function
	 * @name {{getMonthFromDate}}
	 * @nameTags getMonth
	 * @description {{functionToGetMonthFromDate}}
	 * @param {ObjectType.DATETIME} value {{ObjectType.DATETIME}}
	 * @returns {ObjectType.LONG}
	 */
	this.cronapi.dateTime.getMonth = function(value) {
		var date = cronapi.conversion.stringToDate(value);
		if (date)
			return date.getMonth() + 1;
		return 0;
	};

	/**
	 * @type function
	 * @name {{getDayFromDate}}
	 * @nameTags getDay
	 * @description {{functionToGetDayFromDate}}
	 * @param {ObjectType.DATETIME} value {{ObjectType.DATETIME}}
	 * @returns {ObjectType.LONG}
	 */
	this.cronapi.dateTime.getDay = function(value) {
		var date = cronapi.conversion.stringToDate(value);
		if (date)
			return date.getDate();
		return 0;
	};

	/**
	 * @type function
	 * @name {{getDaysBetweenDates}}
	 * @nameTags getDaysBetweenDates|getDaysDiffDate|diffDatesDays
	 * @description {{functionToGetDaysBetweenDates}}
	 * @param {ObjectType.DATETIME} date {{largerDateToBeSubtracted}}
	 * @param {ObjectType.DATETIME} date2 {{smallerDateToBeSubtracted}}
	 * @returns {ObjectType.LONG}
	 */
	this.cronapi.dateTime.getDaysBetweenDates = function(date, date2) {
		var DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
		var dateVar = cronapi.conversion.stringToDate(date);
		var date2Var = cronapi.conversion.stringToDate(date2);
		var daysBetween = Math.round((dateVar.getTime() - date2Var.getTime())
				/ DAY_IN_MILLIS);
		return daysBetween;
	};

	/**
	 * @type function
	 * @name {{getMonthsBetweenDates}}
	 * @nameTags getMonthsBetweenDates|getMonthsDiffDate|diffDatesMonths
	 * @description {{functionToGetMonthsBetweenDates}}
	 * @param {ObjectType.DATETIME} date {{largerDateToBeSubtracted}}
	 * @param {ObjectType.DATETIME} date2 {{smallerDateToBeSubtracted}}
	 * @returns {ObjectType.LONG}
	 */
	this.cronapi.dateTime.getMonthsBetweenDates = function(date, date2) {
		var monthBetween = 0;
		var yearBetween = 0;
		var dateVar = cronapi.conversion.stringToDate(date);
		var date2Var = cronapi.conversion.stringToDate(date2);
		if (dateVar && date2Var) {
			yearBetween = (dateVar.getFullYear() - date2Var.getFullYear()) * 12;
			monthBetween = dateVar.getMonth() - date2Var.getMonth();
			monthBetween += yearBetween;
			if (date2Var < dateVar && dateVar.getDate() < date2Var.getDate())
				monthBetween--;
			else if (date2Var > dateVar
					&& dateVar.getDate() > date2Var.getDate())
				monthBetween++;
		}
		return monthBetween;
	};

	/**
	 * @type function
	 * @name {{getYearsBetweenDates}}
	 * @nameTags getYearsBetweenDates|getYearsDiffDate|diffDatesYears
	 * @description {{functionToGetYearsBetweenDates}}
	 * @param {ObjectType.DATETIME} date {{largerDateToBeSubtracted}}
	 * @param {ObjectType.DATETIME} date2 {{smallerDateToBeSubtracted}}
	 * @returns {ObjectType.LONG}
	 */
	this.cronapi.dateTime.getYearsBetweenDates = function(date, date2) {
		var yearBetween = 0;
		var dateVar = cronapi.conversion.stringToDate(date);
		var date2Var = cronapi.conversion.stringToDate(date2);
		if (dateVar && date2Var) {
			yearBetween = (dateVar.getFullYear() - date2Var.getFullYear());
			if (date2Var < dateVar
					&& (dateVar.getDate() < date2Var.getDate() || dateVar
							.getMonth() < date2Var.getMonth()))
				yearBetween--;
			else if (date2Var > dateVar
					&& (dateVar.getDate() > date2Var.getDate() || dateVar
							.getMonth() > date2Var.getMonth()))
				yearBetween++;
		}
		return yearBetween;
	};

	/**
	 * @type function
	 * @name {{incDay}}
	 * @nameTags incDay|increaseDay
	 * @description {{functionToIncDay}}
	 * @param {ObjectType.DATETIME} date {{ObjectType.DATETIME}}
	 * @param {ObjectType.LONG} day {{daysToIncrement}}
	 * @returns {ObjectType.DATETIME}
	 */
	this.cronapi.dateTime.incDay = function(date, day) {
		var dateVar = cronapi.conversion.stringToDate(date);
		dateVar.setDate(dateVar.getDate() + day);
		return dateVar;
	};

	/**
	 * @type function
	 * @name {{incMonth}}
	 * @nameTags incMonth|increaseMonth
	 * @description {{functionToIncMonth}}
	 * @param {ObjectType.DATETIME} date {{ObjectType.DATETIME}}
	 * @param {ObjectType.LONG} month {{monthsToIncrement}}
	 * @returns {ObjectType.DATETIME}
	 */
	this.cronapi.dateTime.incMonth = function(date, month) {
		var dateVar = cronapi.conversion.stringToDate(date);
		dateVar.setMonth(dateVar.getMonth() + month);
		return dateVar;
	};

	/**
	 * @type function
	 * @name {{incYear}}
	 * @nameTags incYear|increaseYear
	 * @description {{functionToIncYear}}
	 * @param {ObjectType.DATETIME} date {{ObjectType.DATETIME}}
	 * @param {ObjectType.LONG} year {{yearsToIncrement}}
	 * @returns {ObjectType.DATETIME}
	 */
	this.cronapi.dateTime.incYear = function(date, year) {
		var dateVar = cronapi.conversion.stringToDate(date);
		dateVar.setFullYear(dateVar.getFullYear() + year);
		return dateVar;
	};

	/**
	 * @type function
	 * @name {{getNow}}
	 * @nameTags getNow|now|getDate
	 * @description {{functionToGetNow}}
	 * @returns {ObjectType.DATETIME}
	 */
	this.cronapi.dateTime.getNow = function() {
		return new Date();
	};

	/**
	 * @type function
	 * @name {{formatDateTime}}
	 * @nameTags formatDateTime
	 * @description {{functionToFormatDateTime}}
	 * @param {ObjectType.DATETIME} date {{ObjectType.DATETIME}}
	 * @param {ObjectType.STRING} format {{format}}
	 * @returns {ObjectType.STRING}
	 */
	this.cronapi.dateTime.formatDateTime = function(date, format) {
		var dateVar = cronapi.conversion.stringToDate(date);
		var dd = dateVar.getDate();
		var mm = dateVar.getMonth() + 1;
		var yyyy = dateVar.getFullYear();
		var	separator = '';
		var maskChars = 'dmy';
		for (var i = 0; i < format.length; i++) {
			if (!maskChars.includes(format.toLowerCase().charAt(i))) {
				separator = format.toLowerCase().charAt(i);
			  var formatLower = replaceAll(format.toLowerCase(), separator, '+separator+');
			  return eval(formatLower);
		  }
		}
		return '';
	};
	
	/**
	 * @type function
	 * @name {{newDate}}
	 * @nameTags newDate|createDate
	 * @description {{functionToNewDate}}
	 * @param {ObjectType.LONG} year {{year}}
	 * @param {ObjectType.LONG} month {{month}}
	 * @param {ObjectType.LONG} hour {{hour}}
	 * @param {ObjectType.LONG} minute {{minute}}
	 * @param {ObjectType.LONG} second {{second}}
	 * @returns {ObjectType.STRING}
	 */
	this.cronapi.dateTime.newDate = function(year, month, day, hour, minute, second) {
	  var date = new Date();
    date.setYear(year);
    date.setMonth(month - 1);
    date.setDate(day);
    date.setHours(hour);
    date.setMinutes(minute);
    date.setSeconds(second);
    return date;
	};
	
	/**
	 * @type function
	 * @name {{getValueIsNotNumber}}
	 * @nameTags getValueIsNotNumber
	 * @description {{functionToGetValueIsNotNumber}}
	 * @param {ObjectType.STRING} str {{content}}
	 * @returns {ObjectType.STRING}
	 */
	this.cronapi.dateTime.getValueIsNotNumber = function(str) {
		var numbers = '0123456789';
		for (var i = 0; i < str.length; i++)
			if (!numbers.includes(str.charAt(i)))
				return str.charAt(i);
		return '';
	};

	/**
	 * @category CategoryType.XML
	 * @categoryTags XML|xml
	 */
	this.cronapi.xml = {};

	/**
	 * @type function
	 * @name Obtém valor do elemento
	 * @nameTags XMLGetElementValue
	 * @description Função que retorna o valor de um elemento
	 * @param {ObjectType.OBJECT} node Elemento passado para obter-se o valor;
	 * @returns {ObjectType.STRING}
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
	 * @nameTags XMLGetChildElement
	 * @description Função para retornar o nó
	 * @param {ObjectType.OBJECT} node Elemento passado para obter-se o valor;
	 * @param {ObjectType.STRING} childName Filho a ser obtido do elemento;
	 * @returns {ObjectType.STRING}
	 */
	this.cronapi.xml.XMLGetChildElement = function(node, childName) {
		var c = node.getElementsByTagName(childName);
		if (c.length > 0)
			return c[0];
	};

	/**
	 * @type function
	 * @name Obtém a raiz do elemento
	 * @nameTags XMLGetRoot
	 * @description Função que retorna o elemento raiz a partir de um elemento
	 * @param {ObjectType.OBJECT} element Elemento passado para obter-se a raiz
	 * @returns {ObjectType.OBJECT}
	 */
	this.cronapi.xml.XMLGetRoot = function(element) {
		if (element)
			return doc.documentElement;
	};

	/**
	 * @type function
	 * @name Obtém o atributo do elemento
	 * @nameTags XMLGetAttribute
	 * @description Função que retorna o elemento raiz a partir de um elemento
	 * @param {ObjectType.OBJECT} element - Elemento passado para obter-se a raiz
	 * @param {ObjectType.OBJECT} attribute - Atributo a ser obtido
	 * @returns {ObjectType.STRING}
	 */
	this.cronapi.xml.XMLGetAttribute = function(element, attribute) {
		return node.getAttribute(attribute);
	};

	/**
	 * @type function
	 * @name Cria Document
	 * @nameTags XMLOpen
	 * @description Função que cria um objeto Document a partir de uma String
	 * @param {ObjectType.OBJECT} XMLText - Elemento passado para obter-se a raiz
	 * @returns {ObjectType.OBJECT}
	 */
	this.cronapi.xml.XMLOpen = function(XMLText) {
		var doc = null;
		if (document.implementation && document.implementation.createDocument) { //Mozzila
			var domParser = new DOMParser();
			doc = domParser.parseFromString(XMLText, 'application/xml');
			fixXMLDocument(doc);
			return doc;
		} else {
			doc = new ActiveXObject("MSXML2.DOMDocument");
			doc.loadXML(XMLText);
		}
		return doc;
	};

	/**
	 * @type function
	 * @name Busca filhos do elemento
	 * @nameTags XMLGetChildrenElement
	 * @description Função que retorna os filhos do tipo de um determinado elemento
	 * @param {ObjectType.OBJECT} node - Elemento passado para buscar os filhos
	 * @param {ObjectType.OBJECT} childName - Elemento do tipo a ser buscado
	 * @returns {ObjectType.OBJECT}
	 */
	this.cronapi.xml.XMLGetChildrenElement = function(node, childName) {
		if (childName) {
			return node.getElementsByTagName(childName);
		} else {
			return node.childNodes;
		}
	};

	/**
	 * @type function
	 * @name Retorna o elemento pai
	 * @nameTags XMLGetParentElement
	 * @description Função que retorna o pai de um elemento
	 * @param {ObjectType.OBJECT} node - Elemento a ser buscado o pai
	 * @returns {ObjectType.OBJECT}
	 */
	this.cronapi.xml.XMLGetParentElement = function XMLGetParentElement(node) {
		return node.parentNode;
	};

	/**
	 * @type function
	 * @name Retorna a tag do elemento
	 * @nameTags XMLGetElementTagName
	 * @description Função que retorna o nome da tag do elemento
	 * @param {ObjectType.OBJECT} node - Elemento a ser buscado a tag
	 * @returns {ObjectType.STRING}
	 */
	this.cronapi.xml.XMLGetElementTagName = function XMLGetElementTagName(node) {
		return node.tagName;
	};

	//Private variables and functions
	var ptDate = function(varray) {
		var date;
		var day = varray[1];
		var month = varray[2];
		var year = varray[3];
		var hour = varray[5];
		var minute = varray[6];
		var second = varray[7];
		if (hour)
			date = new Date(year, month - 1, day, hour, minute, second);
		else
			date = new Date(year, month - 1, day, 0, 0, 0);
		return date;
	};

	var enDate = function(varray) {
		var date;
		var month = varray[1];
		var day = varray[2];
		var year = varray[3];
		var hour = varray[5];
		var minute = varray[6];
		var second = varray[7];
		if (hour)
			date = new Date(year, month - 1, day, hour, minute, second);
		else
			date = new Date(year, month - 1, day, 0, 0, 0);
		return date;
	};

	var parseBoolean = function(value) {
		if (!value)
			return false;
		if (typeof value == "boolean")
			return value;
		value = value.toString().toLowerCase().trim();
		return value == "1" || value == "true";
	};

	var removeAccents = function(value) {
		withAccents = 'áàãâäéèêëíìîïóòõôöúùûüçÁÀÃÂÄÉÈÊËÍÌÎÏÓÒÕÖÔÚÙÛÜÇ';
		withoutAccents = 'aaaaaeeeeiiiiooooouuuucAAAAAEEEEIIIIOOOOOUUUUC';
		newValue = '';
		for (i = 0; i < value.length; i++) {
			if (withAccents.search(value.substr(i, 1)) >= 0) {
				newValue += withoutAccents.substr(withAccents.search(value
						.substr(i, 1)), 1);
			} else {
				newValue += value.substr(i, 1);
			}
		}
		return newValue;
	};

	var arrayRemove = function(array, value) {
		var i = arrayIndexOf(array, value);
		if (i != -1) {
			array.splice(i, 1);
		}
	};

	var arrayIndexOf = function(array, value) {
	  var index = -1;
	  $(array).each(function(idx) {
	    if (value == this) {
	      index = idx;
	    }
	  });
	  return index;
	};

	var replaceAll = function(str, value, newValue) {
		return str.toString().split(value).join(newValue);
	};

	var getWindowHeight = function() {
		$(window).height();
	};

	var getWindowWidth = function() {
		$(window).width();
	};

	/**
  *
  *  URL encode / decode
  *  http://www.webtoolkit.info/
  *
  **/
  
  var Url = {
      // public method for url encoding
      encode : function (string) {
          return escape(this._utf8_encode(string));
      },
      // public method for url decoding
      decode : function (string) {
          return this._utf8_decode(unescape(string));
      },
      // private method for UTF-8 encoding
      _utf8_encode : function (string) {
          string = string.replace(/\r\n/g,"\n");
          var utftext = "";
          for (var n = 0; n < string.length; n++) {
              var c = string.charCodeAt(n);
              if (c < 128) {
                  utftext += String.fromCharCode(c);
              }
              else if((c > 127) && (c < 2048)) {
                  utftext += String.fromCharCode((c >> 6) | 192);
                  utftext += String.fromCharCode((c & 63) | 128);
              }
              else {
                  utftext += String.fromCharCode((c >> 12) | 224);
                  utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                  utftext += String.fromCharCode((c & 63) | 128);
              }
          }
          return utftext;
      },
      // private method for UTF-8 decoding
      _utf8_decode : function (utftext) {
          var string = "";
          var i = 0;
          var c = c1 = c2 = 0;
          while ( i < utftext.length ) {
              c = utftext.charCodeAt(i);
              if (c < 128) {
                  string += String.fromCharCode(c);
                  i++;
              }
              else if((c > 191) && (c < 224)) {
                  c2 = utftext.charCodeAt(i+1);
                  string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
                  i += 2;
              }
              else {
                  c2 = utftext.charCodeAt(i+1);
                  c3 = utftext.charCodeAt(i+2);
                  string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
                  i += 3;
              }
          }
          return string;
      }
  };

	var stringToJs = function(str) {
    return (str + '').replace(/[\\"']/g, '\\$&').replace(/\u0000/g, '\\0');
	};

}).bind(window)();
