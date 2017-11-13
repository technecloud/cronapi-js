(function() {
  'use strict';

  this.cronapi = {};

  this.doEval = function(arg) {
    return arg;
  }

  this.evalInContext = function(js) {
    var result = eval('doEval('+js+')');
    if (result && result.commands) {
      for (var i=0;i<result.commands.length;i++) {
        var func = eval(result.commands[i].function);
        func.apply(this, result.commands[i].params);
      }
    }
    if (result && result.value) {
      return result.value;
    }
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
    if (value){
      var result = new String(value);
      return result.toString();
    }
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
  this.cronapi.util.callServerBlocklyAsync = function(classNameWithMethod, fields, callbackSuccess, callbackError) {
    var serverUrl = 'api/cronapi/call/body/#classNameWithMethod#/'.replace('#classNameWithMethod#', classNameWithMethod);
    var http = cronapi.$scope.http;
    var params = [];
    $(arguments).each(function() {
      params.push(this);
    });

    var token = "";
    if (window.uToken)
      token = window.uToken;

    var dataCall = {
      "fields": fields,
      "inputs": params.slice(4)
    };

    var resultData = $.ajax({
      type: 'POST',
      url: (window.hostApp || "") + serverUrl,
      dataType: 'html',
      data : JSON.stringify(dataCall),
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
   */
  this.cronapi.util.getScreenFields = function() {
    var fields = {};

    for (var key in cronapi.$scope) {
      if (cronapi.$scope[key] && cronapi.$scope[key].constructor && cronapi.$scope[key].constructor.name=="DataSet") {
        fields[key] = {};
        fields[key].active = cronapi.$scope[key].active;
      }
    }

    for (var key in cronapi.$scope.vars) {
      if (cronapi.$scope.vars[key]) {
        if (!fields.vars) {
          fields.vars = {};
        }
        fields.vars[key] = cronapi.$scope.vars[key];
      }
    }

    for (var key in cronapi.$scope.params) {
      if (cronapi.$scope.params[key]) {
        if (!fields.params) {
          fields.params = {};
        }
        fields.params[key] = cronapi.$scope.params[key];
      }
    }

    return fields;
  }

  function getErrorMessage(data, message) {
    try {
      var json = JSON.parse(data);
      if (json && json.error) {
        return json.error;
      }
    } catch(e) {
      //Abafa
    }

    return message;
  }

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
    var fields = this.getScreenFields();

    var paramsApply = [];
    paramsApply.push(blocklyWithFunction);
    paramsApply.push(fields);
    paramsApply.push(function(data) {
      var result = evalInContext(data);
      if (typeof callbackSuccess == "string") {
        eval(callbackSuccess)(result);
      } else if (callbackSuccess) {
        callbackSuccess(result);
      }
    });
    paramsApply.push(function(data, status, errorThrown) {
      var message = getErrorMessage(data.responseText, errorThrown);
      if (typeof callbackError == "string") {
        eval(callbackError)(message);
      }
      else if (callbackError) {
        callbackError(message);
      }
      else {
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
   * @name {{throwExceptionName}}
   * @nameTags throwException
   * @description {{throwExceptionDescription}}
   * @param {ObjectType.OBJECT} value {{throwExceptionParam0}}
   */
  this.cronapi.util.throwException = function(value) {
    throw value;
  };



  /**
   * @type function
   * @name {{createExceptionName}}
   * @nameTags createException
   * @description {{createExceptionDescription}}
   * @param {ObjectType.STRING} value {{createExceptionParam0}}
   * @returns {ObjectType.STRING}
   */
  this.cronapi.util.createException = function(value) {
    return value;
  };

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
    var serverUrl = 'api/cronapi/call/body/#classNameWithMethod#/'.replace('#classNameWithMethod#', classNameWithMethod);
    var params = [];

    var fields = this.getScreenFields();

    var dataCall = {
      "fields": fields,
      "inputs": params
    };

    for (var i = 1; i<arguments.length; i++)
      params.push(arguments[i]);

    var token = "";
    if (window.uToken)
      token = window.uToken;

    var resultData = $.ajax({
      type: 'POST',
      url: (window.hostApp || "") + serverUrl,
      dataType: 'html',
      data : JSON.stringify(dataCall),
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
      var message = getErrorMessage(resultData.responseText, resultData.statusText);
      cronapi.$scope.Notification.error(message);
      throw message;
    }
    return result;
  };
  
  /**
   * @type function
   * @name {{executeJavascriptNoReturnName}}
   * @nameTags executeJavascriptNoReturn
   * @description {{executeJavascriptNoReturnDescription}}
   * @param {ObjectType.STRING} value {{executeJavascriptNoReturnParam0}}
   * @multilayer true
   */
  this.cronapi.util.executeJavascriptNoReturn = function(value) {
    eval( value );
  };
  
  /**
   * @type function
   * @name {{executeJavascriptNoReturnName}}
   * @nameTags executeJavascriptNoReturn
   * @description {{executeJavascriptNoReturnDescription}}
   * @param {ObjectType.STRING} value {{executeJavascriptNoReturnParam0}}
   * @returns {ObjectType.STRING}
   */
  this.cronapi.util.executeJavascriptNoReturn = function(value) {
     eval( value );
  };

  /**
   * @type function
   * @name {{executeJavascriptNoReturnName}}
   * @nameTags executeJavascriptReturn
   * @description {{executeJavascriptReturnDescription}}
   * @param {ObjectType.STRING} value {{executeJavascriptNoReturnParam0}}
   * @returns {ObjectType.STRING}
   */
  this.cronapi.util.executeJavascriptReturn = function(value) {
    return eval( value );
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
   * @multilayer true
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
   * @multilayer true
   */
  this.cronapi.screen.changeValueOfField = function(/** @type {ObjectType.STRING} @blockType field_from_screen*/ field, /** @type {ObjectType.STRING} */value) {
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
  this.cronapi.screen.getValueOfField = function(/** @type {ObjectType.STRING} @blockType field_from_screen*/ field) {
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
   * @type function
   * @name {{createScopeVariableName}}
   * @nameTags createScopeVariable
   * @description {{createScopeVariableDescription}}
   * @param {ObjectType.STRING} name {{createScopeVariableParam0}}
   * @param {ObjectType.STRING} value {{createScopeVariableParam1}}
   */
  this.cronapi.screen.createScopeVariable = function(name,value) {
    cronapi.$scope.vars[name] = value;
  };

  /**
   * @type function
   * @name {{getScopeVariableName}}
   * @nameTags getScopeVariable
   * @description {{getScopeVariableDescription}}
   * @param {ObjectType.STRING} name {{getScopeVariableParam0}}
   * @returns {ObjectType.STRING}
   */
  this.cronapi.screen.getScopeVariable = function(name) {
    return cronapi.$scope.vars[name];
  };

  /**
   * @type function
   * @name {{screenNotifyName}}
   * @platform W
   * @description {{screenNotifyDescription}}
   * @param {ObjectType.STRING} type {{screenNotifyParam0}}
   * @param {ObjectType.STRING} message {{screenNotifyParam1}}
   * @wizard notify_type
   * @multilayer true
   */
  this.cronapi.screen.notify = function(/** @type {ObjectType.STRING} */ type, /** @type {ObjectType.STRING} */  message) {
    cronapi.$scope.Notification({'message':message.toString() },type);
  };

  /**
   * @type function
   * @name {{datasourceFromScreenName}}
   * @nameTags datasourceFromScreen
   * @description {{datasourceFromScreenDescription}}
   * @param {ObjectType.STRING} datasource {{datasourceFromScreenParam0}}
   * @returns {ObjectType.STRING}
   * @wizard datasource_from_screen
   * @multilayer true
   */
  this.cronapi.screen.datasourceFromScreen = function(datasource) {
    return datasource;
  };

  /**
   * @type function
   * @name {{startInsertingModeName}}
   * @nameTags startInsertingMode
   * @description {{startInsertingModeDescription}}
   * @param {ObjectType.STRING} datasource {{startInsertingModeParam0}}
   * @multilayer true
   */
  this.cronapi.screen.startInsertingMode = function(/** @type {ObjectType.OBJECT} @blockType datasource_from_screen*/ datasource) {
    window[datasource].$apply( function() { window[datasource].startInserting();});
  };

  /**
   * @type function
   * @name {{startEditingModeName}}
   * @nameTags startEditingMode
   * @description {{startEditingModeDescription}}
   * @param {ObjectType.STRING} datasource {{startEditingModeParam0}}
   * @multilayer true
   */
  this.cronapi.screen.startEditingMode = function(/** @type {ObjectType.OBJECT} @blockType datasource_from_screen*/ datasource) {
    window[datasource].$apply( new function(){window[datasource].startEditing();} );
  };

  /**
   * @type function
   * @name {{previusRecordName}}
   * @nameTags previusRecord
   * @description {{previusRecordDescription}}
   * @param {ObjectType.STRING} datasource {{previusRecordParam0}}
   * @multilayer true
   */
  this.cronapi.screen.previusRecord = function(/** @type {ObjectType.OBJECT} @blockType datasource_from_screen*/ datasource) {
    window[datasource].$apply( new function(){window[datasource].previous();} );
  };

  /**
   * @type function
   * @name {{nextRecordName}}
   * @nameTags nextRecord
   * @description {{nextRecordDescription}}
   * @param {ObjectType.STRING} datasource {{nextRecordParam0}}
   * @multilayer true
   */
  this.cronapi.screen.nextRecord = function(/** @type {ObjectType.OBJECT} @blockType datasource_from_screen*/ datasource) {
    window[datasource].$apply( new function(){window[datasource].next();} );
  };

  /**
   * @type function
   * @name {{removeRecordName}}
   * @nameTags removeRecord
   * @description {{removeRecordDescription}}
   * @param {ObjectType.STRING} datasource {{removeRecordParam0}}
   * @multilayer true
   */
  this.cronapi.screen.removeRecord = function(/** @type {ObjectType.OBJECT} @blockType datasource_from_screen*/ datasource) {
    window[datasource].$apply( new function(){window[datasource].remove();} );
  };


  /**
   * @type function
   * @name {{hasNextRecordName}}
   * @nameTags hasNextRecord
   * @description {{hasNextRecordDescription}}
   * @param {ObjectType.STRING} datasource {{hasNextRecordParam0}}
   * @returns {ObjectType.BOOLEAN}
   */
  this.cronapi.screen.hasNextRecord = function(/** @type {ObjectType.OBJECT} @blockType datasource_from_screen*/ datasource) {
    return window[datasource].hasNext();
  };

  /**
   * @type function
   * @name {{quantityRecordsName}}
   * @nameTags quantityRecords
   * @description {{quantityRecordsDescription}}
   * @param {ObjectType.STRING} datasource {{quantityRecordsParam0}}
   * @returns {ObjectType.LONG}
   */
  this.cronapi.screen.quantityRecords = function(/** @type {ObjectType.OBJECT} @blockType datasource_from_screen*/ datasource) {
    return window[datasource].data.length;
  };

  /**
   * @type function
   * @name {{datasourcePostName}}
   * @nameTags post|datasource
   * @description {{datasourcePostDescription}}
   * @param {ObjectType.STRING} datasource {{datasource}}
   * @multilayer true
   */
  this.cronapi.screen.post = function(/** @type {ObjectType.OBJECT} @blockType datasource_from_screen*/ datasource) {
    return window[datasource].post();
  };

  /**
   * @type function
   * @name {{datasourceFilterName}}
   * @nameTags filter|datasource
   * @description {{datasourceFilterDescription}}
   * @param {ObjectType.STRING} datasource {{datasourceFilterParam0}}
   * @param {ObjectType.STRING} datasource {{datasourceFilterParam1}}
   * @multilayer true
   */
  this.cronapi.screen.filter = function(/** @type {ObjectType.OBJECT} @blockType datasource_from_screen*/ datasource,/** @type {ObjectType.STRING}*/ path) {
    window[datasource].filter("/"+path);
  };
  
    /**
   * @type function
   * @name {{changeView}}
   * @nameTags changeView|Mudar tela|Change form|Change screen|Mudar formulário
   * @description {{functionToChangeView}}
   * @param {ObjectType.STRING} view {{view}}
   * @param {ObjectType.LIST} params {{params}}
   * @wizard procedures_open_form_callnoreturn
   * @multilayer true
   */
  this.cronapi.screen.changeView = function(view, params) {
    try {
      var queryString = '?';
      var template = '#key#=#value#&';
      $(params).each(function(idx) {
        for (var key in this)
          queryString += template.replace('#key#', Url.encode(key)).replace('#value#', Url.encode(this[key]));
      });
      window.location.hash = view + queryString;
    }
    catch (e) {
      alert(e);
    }
  };

  /**
   * @type function
   * @name {{openUrl}}
   * @nameTags openUrl|Abrir url
   * @description {{functionToOpenUrl}}
   * @param {ObjectType.STRING} url {{url}}
   * @param {ObjectType.BOOLEAN} newTab {{newTab}}
   * @param {ObjectType.LONG} width {{width}}
   * @param {ObjectType.LONG} height {{height}}
   * @multilayer true
   */
  this.cronapi.screen.openUrl = function(url, newTab, width, height) {
    try {
      var target = '_self';
      var params = '';
      if (newTab && newTab.toString().toLowerCase() == 'true')
        target = '_blank';
      if (width)
        params += 'width=' + width + ',';
      if (height)
        params += 'height=' + height+ ',';
      window.open(url, target, params);
    }
    catch (e) {
      alert(e);
    }
  };

  /**
   * @type function
   * @name {{getParam}}
   * @nameTags getParam|Obter paramêtro
   * @description {{functionToGetParam}}
   * @returns {ObjectType.STRING}
   * @param {ObjectType.STRING} paramName {{paramName}}
   */
  this.cronapi.screen.getParam = function(paramName) {
    try {
      return cronapi.$scope.params[paramName];
    }
    catch (e) {
      alert(e);
    }
  };


  /**
   * @type function
   * @name {{confirmDialogName}}
   * @nameTags confirmDialog|Confirmar
   * @description {{confirmDialogDescription}}
   * @returns {ObjectType.BOOLEAN}
   * @param {ObjectType.STRING} msg {{confirmDialogParam0}}
   */
  this.cronapi.screen.confimDialog = function(msg) {

    var value = confirm(msg);
    return value;
  };

  /**
   * @type function
   * @name {{createDefaultModalName}}
   * @nameTags createModal|Criar Modal| Modal
   * @description {{createDefaultModalDescription}}
   * @param {ObjectType.STRING} title {{createDefaultModalParam1}}
   * @param {ObjectType.STRING} msg {{createDefaultModalParam2}}
   * @param {ObjectType.STRING} buttonCancelName {{createDefaultModalParam3}}
   * @param {ObjectType.STRING} buttonSaveName {{createDefaultModalParam4}}
   *
   */
  this.cronapi.screen.createDefaultModal = function(title, msg, buttonCancelName, buttonSaveName, /** @type {ObjectType.STATEMENT} @description {{createDefaultModalParam5}} */ onSuccess, /** @type {ObjectType.STATEMENT} @description {{createDefaultModalParam6}}*/ onError,/** @type {ObjectType.STATEMENT} @description {{createDefaultModalParam7}}*/ onClose ) {
   $('#modalTemplateTitle').text(title);
   $('#modalTemplateBody').text(msg);
   $('#modalTemplateCancel').text(buttonCancelName);
   $('#modalTemplateSave').text(buttonSaveName);
   $( "#modalTemplateClose").unbind( "click" );
   $('#modalTemplateClose').click(onClose);
   $( "#modalTemplateCancel").unbind( "click" );
   $('#modalTemplateCancel').click(onError);
   $( "#modalTemplateSave").unbind( "click" );   
   $('#modalTemplateSave').click(onSuccess);
   $('#modalTemplate').modal('show');
   
  };
  
  /**
   * @type function
   * @name {{showModal}}
   * @nameTags Show| Modal| Exibir| Mostrar
   * @description {{showModalDesc}}
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @multilayer true
   */
    this.cronapi.screen.showModal = function(/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id) {
        $('#'+id).modal('show');
  };

  /**
   * @type function
   * @name {{searchIds}}
   * @nameTags searchIds
   * @description {{searchIdsDescription}}
   * @wizard ids_from_screen
   * @multilayer true
   */
  this.cronapi.screen.searchIds = function() {
    
  };
  
  /**
   * @type function
   * @name {{showComponent}}
   * @nameTags showComponent
   * @description {{showComponentDesc}}
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @multilayer true
   */
  this.cronapi.screen.showComponent = function(/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id) {
    $("#"+id).get(0).style.setProperty("display", "block", "important");
    
  };
  
  /**
   * @type function
   * @name {{hideComponent}}
   * @nameTags hideComponent
   * @description {{hideComponentDesc}}
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @multilayer true
   */
  this.cronapi.screen.hideComponent = function(/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id) {
    $("#"+id).get(0).style.setProperty("display", "none", "important");
  };
  
  /**
   * @type function
   * @name {{disableComponent}}
   * @nameTags disableComponent
   * @description {{disableComponentDesc}}
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @multilayer true
   */
  this.cronapi.screen.disableComponent = function(/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id) {
   $.each( $('#'+id).find('*').addBack(), function(index, value){ $(value).prop('disabled',true); });
  };
  
  /**
   * @type function
   * @name {{enableComponent}}
   * @nameTags enableComponent
   * @description {{enableComponentDesc}}
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @multilayer true
   */
  this.cronapi.screen.enableComponent = function(/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id) {
    $.each( $('#'+id).find('*').addBack(), function(index, value){ $(value).prop('disabled',false); });
  };
  
  /**
   * @type function
   * @name {{changeAttrValueName}}
   * @nameTags changeAttrValue
   * @description {{changeAttrValueDesc}}
   * @param {ObjectType.STRING} id {{idsFromScreen}}
   * @param {ObjectType.STRING} attrName {{attrName}}
   * @param {ObjectType.STRING} attrValue {{attrValue}}
   * @multilayer true
   */
  this.cronapi.screen.changeAttrValue = function(/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id , /** @type {ObjectType.STRING} */ attrName, /** @type {ObjectType.STRING} */ attrValue ) {
    $('#'+id).attr(attrName , attrValue);
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
    var separator = '';
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
   * @param {ObjectType.LONG} month {{day}}
   * @param {ObjectType.LONG} hour {{hour}}
   * @param {ObjectType.LONG} minute {{minute}}
   * @param {ObjectType.LONG} second {{second}}
   * @returns {ObjectType.DATETIME}
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
  * @category CategoryType.TEXT
  * @categoryTags TEXT|text
  */
  this.cronapi.text = {};
  
   /**
  * @type function
  * @wizard text_prompt_ext
  */
  this.cronapi.text.prompt = function(/** @type {ObjectType.STRING} @defaultValue abc*/ value) {
    return null;
  }

  /**
   * @category CategoryType.XML
   * @categoryTags XML|xml
   */
  this.cronapi.xml = {};
  
  /**
   * @type function
   * @name {{newXMLEmptyName}}
   * @nameTags newXMLEmptyValue
   * @description {{newXMLEmptyDescription}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.xml.newXMLEmpty = function() {
  return $.parseXML('<?xml version="1.0" encoding="UTF-8"?><root></root>');
  };
  
  /**
   * @type function
   * @name {{newXMLEmptyWithRootName}}
   * @nameTags newXMLEmptyWithRoot
   * @description {{newXMLEmptyWithRootDescription}}
   * @param {ObjectType.OBJECT} rootElement {{rootElement}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.xml.newXMLEmptyWithRoot = function(rootElement) {
  var t__temp = $.parseXML('<?xml version="1.0" encoding="UTF-8"?><root></root>');
  t__temp.removeChild(t__temp.firstElementChild);
  t__temp.appendChild(rootElement);
  return t__temp;
  };
  
  
  /**
   * @type function
   * @name {{newXMLElementName}}
   * @nameTags newXMLElement
   * @description {{newXMLElementDescription}}
   * @param {ObjectType.STRING} elementName {{elementName}}
   * @param {ObjectType.STRING} value {{content}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.xml.newXMLElement = function(elementName, value) {
  var t__tempElement = document.createElement(elementName);
  t__tempElement.textContent = value;
  return t__tempElement;
  };
  
  /**
   * @type function
   * @name {{addXMLElementName}}
   * @nameTags addXMLElement
   * @description {{addXMLElementDescription}}
   * @param {ObjectType.OBJECT} parent {{parentElement}}
   * @param {ObjectType.OBJECT} value {{elementToAdd}}
   * @returns {ObjectType.BOOLEAN}
   */
  this.cronapi.xml.addXMLElement = function(parent, element) {
  try{
  var temp = element.cloneNode(true);
  parent.appendChild(temp);
  return true;
  }catch(e){
    return false;
  }
  };
  
  /**
   * @type function
   * @name {{XMLHasRootElementName}}
   * @nameTags XMLHasRootElement
   * @description {{XMLHasRootElementDescription}}
   * @param {ObjectType.OBJECT} element {{element}}
   * @returns {ObjectType.BOOLEAN}
   */
  this.cronapi.xml.XMLHasRootElement = function(element) {
    if(element  &&  element.getRootNode()) return true;
    return false;
  }
  
  
    /**
   * @type function
   * @name {{XMLGetRootElementName}}
   * @nameTags XMLGetRootElement
   * @description {{XMLGetRootElementDescription}}
   * @param {ObjectType.OBJECT} element {{element}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.xml.XMLGetRootElement = function(element) {
    if(element instanceof XMLDocument){
      return element.firstElementChild;
    }
    return element.getRootNode();
  }
  
  /**
   * @type function
   * @name {{XMLDocumentToTextName}}
   * @nameTags XMLDocumentToText
   * @description {{XMLDocumentToTextDescription}}
   * @param {ObjectType.OBJECT} xml {{element}}
   * @returns {ObjectType.STRING}
   */
  this.cronapi.xml.XMLDocumentToText = function(xml) {
    if(xml instanceof XMLDocument){
      return $($($(xml.firstElementChild).context.outerHTML).removeAttr('xmlns'))[0].outerHTML ;
    }
    if($(xml).size() > 1 ){
      var __v = '';
      $.each($(xml).toArray() , function(key , value){  __v += $($(value)[0].outerHTML).removeAttr('xmlns')[0].outerHTML  } );
      return __v;
    }
    return $($($(xml).context.outerHTML).removeAttr('xmlns'))[0].outerHTML ;
  }
  
  
  /**
   * @type function
   * @name {{getChildrenName}}
   * @nameTags getChildren
   * @description {{getChildrenDescription}}
   * @param {ObjectType.OBJECT} element {{element}}
   * @param {ObjectType.STRING} search {{getChildrenParam1}}
   * @returns {ObjectType.LIST}
   */
  this.cronapi.xml.getChildren = function(element, search) {
    if(element instanceof XMLDocument){
      return element.firstElementChild.toArray;
    }
    if(search){
      if(search.localName){
        return $(element).find(search.localName).toArray();
      }else {
        return $(element).find(search).toArray();
      }
    }
    return $(element).children().toArray();
  };
  
  /**
   * @type function
   * @name {{setAttributeName}}
   * @nameTags setAttribute
   * @description {{setAttributeDescription}}
   * @param {ObjectType.OBJECT} element {{element}}
   * @param {ObjectType.STRING} attributeName {{attributeName}}
   * @param {ObjectType.STRING} attributeValue {{attributeValue}}
   * @returns {ObjectType.BOOLEAN}
   */
  this.cronapi.xml.setAttribute = function(element, attributeName, attributeValue) {
    if(!attributeName){
      return false;
    }
    if(element instanceof XMLDocument){
      element.firstChild.setAttribute(attributeName, attributeValue);
      return true;
    }
    if(element){
      element.setAttribute(attributeName, attributeValue);
      return true;
    }
    return false;
  };
  
  
  /**
   * @type function
   * @name {{getAttributeValueName}}
   * @nameTags getAttributeValue
   * @description {{getAttributeValueDescription}}
   * @param {ObjectType.OBJECT} element {{element}}
   * @param {ObjectType.STRING} attributeName {{attributeName}}
   * @returns {ObjectType.STRING}
   */
  this.cronapi.xml.getAttributeValue = function(element, attributeName) {
    if(!attributeName){
        return '';
      }
    if(element instanceof XMLDocument){
      return element.firstChild.getAttribute(attributeName)  ? element.firstChild.getAttribute(attributeName) : '' ;
    }
    if(element && attributeName ){
      return element.getAttribute(attributeName) ;
    }
    return '';
  }
  
  
  /**
   * @type function
   * @name {{getParentNodeName}}
   * @nameTags getParentNode
   * @description {{getParentNodeDescription}}
   * @param {ObjectType.OBJECT} element {{element}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.xml.getParentNode = function(element){

    if(element instanceof XMLDocument){
      return element.firstChild;
    }
    return element.parentNode;
  }
  
  
  
  /**
   * @type function
   * @name {{setElementValueName}}
   * @nameTags setElementValue
   * @description {{setElementValueDescription}}
   * @param {ObjectType.OBJECT} element {{element}}
   * @param {ObjectType.STRING} content {{content}}
   */
  this.cronapi.xml.setElementContent = function(element, content) {
    
      if(element instanceof XMLDocument){
      element.firstChild.textContent = content;
    }
     element.textContent = content;
  }
  
  
  /**
   * @type function
   * @name {{getElementContentName}}
   * @nameTags getElementContent
   * @description {{getElementContentDescription}}
   * @param {ObjectType.OBJECT} element {{element}}
   * @returns {ObjectType.STRING}
   */
  this.cronapi.xml.getElementContent = function(element) {
      if(element instanceof XMLDocument){
      return element.firstChild.innerText;
    }
     return element.innerText;
  }
  
  /**
   * @type function
   * @name {{removeElementName}}
   * @nameTags removeElement
   * @description {{removeElementDescription}}
   * @param {ObjectType.OBJECT} parent {{parentElement}}
   * @param {ObjectType.STRING} element {{element}}
   */
  this.cronapi.xml.removeElement = function(parent, element) {
      if(parent instanceof XMLDocument)
      {
      if(element)
        {
        if( element instanceof HTMLUnknownElement ){
         element.remove();
        }else
          {
          $.each( $(parent.firstElementChild.children), function( key , value )
            {  
            if(value.localName == element)
            value.remove();
            });
        }
        }else
        {
      $.each( $(parent.firstElementChild.children), function( key , currentObject ){  currentObject.remove() });
        }
      }else
      {
      if(element)
        {
        if( element instanceof HTMLUnknownElement ){
         element.remove();
        }else
          {
          $.each( $(parent.children), function( key , value )
            {  
            if(value.localName == element)
            value.remove();
            });
        }
        }else
        {
      $.each( $(parent.children), function( key , currentObject ){  currentObject.remove() });
        }
      }
      
  }
  
  /**
   * @type function
   * @name {{getElementNameName}}
   * @nameTags getElementName
   * @description {{getElementNameDescription}}
   * @param {ObjectType.OBJECT} element {{element}}
   * @returns {ObjectType.STRING}
   */
  this.cronapi.xml.getElementName = function(element){
    
    if(element instanceof XMLDocument){
      return element.firstChild.localName;
    }
    return element.localName;
  }
  
  /**
   * @type function
   * @name {{renameElementName}}
   * @nameTags renameElement
   * @description {{renameElementDescription}}
   * @param {ObjectType.OBJECT} element {{element}}
   * @param {ObjectType.STRING} name {{name}}
   */
  this.cronapi.xml.renameElement = function(element, name){
    var newElement = element.outerHTML.replace(element.localName, name )
    newElement = newElement.replace('/'+ element.localName ,'/'+name);
    newElement = $(newElement).removeAttr('xmlns');
    element.replaceWith(newElement[0]);
  }
  
  /**
   * @category CategoryType.LOGIC
   * @categoryTags LOGIC|logic
   */
   this.cronapi.logic = {};
   
   /**
   * @type function
    * @name {{LogicIsNullName}}
    * @nameTags isNull
    * @description {{LogicIsNullDescription}}
    * @returns {ObjectType.BOOLEAN}
    * @displayInline true
   */
   this.cronapi.logic.isNull = function(/** @type {ObjectType.OBJECT} @description */ value) {
     return (value === null || typeof value  == 'undefined');
   }
   
   /**
   * @type function
    * @name {{LogicIsEmptyName}}
    * @nameTags isEmpty
    * @description {{LogicIsEmptyDescription}}
    * @returns {ObjectType.BOOLEAN}
    * @displayInline true
   */
   this.cronapi.logic.isEmpty = function(/** @type {ObjectType.OBJECT} @description */ value) {
     return (value  === '');
   }
  
   /**
   * @type function
    * @name {{LogicIsNullOrEmptyName}}
    * @nameTags isNullOrEmpty
    * @description {{LogicIsNullOrEmptyDescription}}
    * @returns {ObjectType.BOOLEAN}
    * @displayInline true
   */
   this.cronapi.logic.isNullOrEmpty = function(/** @type {ObjectType.OBJECT} @description */ value) {
     return (cronapi.logic.isNull(value) || cronapi.logic.isEmpty(value));
   }
  
  this.cronapi.i18n = {};

  this.cronapi.i18n.translate = function(value , params) {
    if (value) {
      var text = cronapi.$translate.instant(value);
      for (var i = 0; i < params.length; i++){
        var param = params[i];
        if (param != null && typeof param != "undefined") {
          var regexp = new RegExp("\\{" + (i) + "\\}", "g");
          text = text.replace(regexp, param);
        }
      }
      return text;
    }
    return;
  };

  this.cronapi.internal = {};
  
  this.cronapi.internal.setFile = function(field, file) {
    cronapi.internal.fileToBase64(file, function(base64) { cronapi.screen.changeValueOfField(field, base64); });
  };
  
  this.cronapi.internal.fileToBase64 = function(file, cb) {
      var fileReader = new FileReader();
      fileReader.readAsDataURL(file);
      fileReader.onload = function(e) {
          debugger;
          var base64Data = e.target.result.substr(e.target.result.indexOf('base64,') + 'base64,'.length);
          cb(base64Data);
      };
  };
  
  this.cronapi.internal.startCamera = function(field) {
    var cameraContainer =   '<div class="camera-container" style="margin-left:-$marginleft$;margin-top:-$margintop$">\
                                    <div class="btn btn-success" id="cronapiVideoCaptureOk" style="position: absolute; z-index: 999999999;">\
                                        <span class="glyphicon glyphicon-ok"></span>\
                                    </div>\
                                    <div class="btn btn-danger" id="cronapiVideoCaptureCancel" style="position: absolute; margin-left: 42px; z-index: 999999999;">\
                                        <span class="glyphicon glyphicon-remove"></span>\
                                    </div>\
                                    <video id="cronapiVideoCapture" style="height: $height$; width: $width$;" autoplay=""></video>\
                            </div>';
    
    
    function getMaxResolution(width, height) {
      var maxWidth = window.innerWidth; 
      var maxHeight = window.innerHeight; 
      var ratio = 0;

      ratio = maxWidth / width;   
      height = height * ratio;    
      width = width * ratio;

      if(width > maxWidth){
          ratio = maxWidth / width;   
          height = height * ratio;    
          width = width * ratio;
      }
      
      if(height > maxHeight){
          ratio = maxHeight / height; 
          width = width * ratio;
          height = height * ratio;
      }
      
      return { width: width, height: height };
    }
    
    var streaming = null;
    var mediaConfig =  { video: true };
    var errBack = function(e) {
    	console.log('An error has occurred!', e)
    };
    
    if(navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
        navigator.mediaDevices.getUserMedia(mediaConfig).then(function(stream) {
          streaming = stream;
          
          var res = getMaxResolution(stream.getTracks()[0].getSettings().width, stream.getTracks()[0].getSettings().height);
          var halfWidth = res.width;
          var halfHeight = res.height;
          try {
            halfWidth = parseInt(halfWidth/2);
            halfHeight = parseInt(halfHeight/2);
          }
          catch (e) { }
          
          cameraContainer = 
          cameraContainer
          .split('$height$').join(res.height+'px')
          .split('$width$').join(res.width+'px')
          .split('$marginleft$').join(halfWidth+'px')
          .split('$margintop$').join(halfHeight+'px')
          ;
          var cronapiVideoCapture = $(cameraContainer);
          cronapiVideoCapture.prependTo("body");
          var videoDOM = document.getElementById('cronapiVideoCapture');
          
          cronapiVideoCapture.find('#cronapiVideoCaptureCancel').on('click',function() {
             if (streaming!= null && streaming.getTracks().length > 0)
                streaming.getTracks()[0].stop();
             $(cronapiVideoCapture).remove();
          });
        
          cronapiVideoCapture.find('#cronapiVideoCaptureOk').on('click',function() {
             cronapi.internal.captureFromCamera(field, res.width, res.height);
             if (streaming!= null && streaming.getTracks().length > 0)
                streaming.getTracks()[0].stop();
             $(cronapiVideoCapture).remove();
          });
          
          videoDOM.src = window.URL.createObjectURL(stream);
          videoDOM.play();
        });
    }
  }; 
   
  this.cronapi.internal.captureFromCamera = function(field, width, height) {
    var canvas = document.createElement("canvas"); // create img tag
    canvas.width = width;
    canvas.height = height;
    var context = canvas.getContext('2d');
    var videoDOM = document.getElementById('cronapiVideoCapture');
		context.drawImage(videoDOM, 0, 0, width, height);
		var base64 = canvas.toDataURL().substr(22);
		cronapi.screen.changeValueOfField(field, base64);
  };
  
  this.cronapi.internal.castBase64ToByteArray = function(base64) {
    var binary_string =  window.atob(base64);
    var len = binary_string.length;
    var bytes = new Uint8Array( len );
    for (var i = 0; i < len; i++)        {
        bytes[i] = binary_string.charCodeAt(i);
    }
    return bytes;
  };
  
  this.cronapi.internal.castByteArrayToString = function(bytes) {
    return String.fromCharCode.apply(null, new Uint16Array(bytes));
  };
  
  this.cronapi.internal.generatePreviewDescriptionByte = function(data) {
    var json;
    try {
      //Verificando se é JSON Uploaded file
      json = JSON.parse(data);
    }
    catch (e) {
      try {
        //Tenta pegar do header
        json = JSON.parse(cronapi.internal.castByteArrayToString(cronapi.internal.castBase64ToByteArray(data)))
      }
      catch (e) {
        //Verifica se é drpobox
        if (data && data.indexOf('dropboxusercontent') > -1) {
          json = {};
          var urlSplited = data.split('/');
          var fullName = urlSplited[urlSplited.length - 1].replace('?dl=0','');
          var fullNameSplited = fullName.split('.')
          var extension = '.' + fullNameSplited[fullNameSplited.length - 1];
          json.fileExtension = extension;
          json.name = fullName.replace(extension, '');
          json.contentType = 'file/'+extension.replace('.','');
        }
      }
    }
    if (json) {
      if (json.name.length > 25)
        json.name = json.name.substr(0,22)+'...';
        
      var result = "<b>Nome:</b> <br/>" + json.name +"<br/>";
      result += "<b>Content-Type:</b> <br/>" + json.contentType +"<br/>";
      result += "<b>Extensão:</b> <br/>" + json.fileExtension +"<br/>";
      return result;
    }
  };
  
  this.cronapi.internal.downloadFileEntity = function(datasource, field, indexData) {
    var tempJsonFileUploaded = null;
    var valueContent;
    var itemActive;
    if (indexData) {
      valueContent = datasource.data[indexData][field];
      itemActive = datasource.data[indexData];
    }
    else {
      try {
        valueContent = datasource.active[field]; 
        itemActive = datasource.active;
      }
      catch (e) {
        valueContent = datasource[field]; 
        itemActive = datasource;
      }
    }
    //Verificando se é JSON Uploaded file
    try {
      var tempJsonFileUploaded = JSON.parse(valueContent);
    }
    catch(e) { }
    
    if (tempJsonFileUploaded) {
      window.open('/api/cronapi/filePreview/'+tempJsonFileUploaded.path);
    }
    else if (valueContent.indexOf('dropboxusercontent') > -1) {
      window.open(valueContent);
    }
    else {
      var url = '/api/cronapi/downloadFile';
      var splited = datasource.entity.split('/');
      
      var entity = splited[splited.length-1];
      if (entity.indexOf(":") > -1) {
        //Siginifica que é relacionamento, pega a entidade do relacionamento
        var entityRelation = '';
        var splitedDomainBase = splited[3].split('.');
        for (var i=0; i<splitedDomainBase.length-1;i++)
          entityRelation += splitedDomainBase[i]+'.';
        var entityRelationSplited = entity.split(':');
        entity = entityRelation + entityRelationSplited[entityRelationSplited.length-1];
      }
      
      url += '/' + entity;
      url += '/' + field;
      var _u = JSON.parse(sessionStorage.getItem('_u'));
      var object = itemActive;
      this.$promise = cronapi.$scope.$http({
          method: 'POST',
          url: (window.hostApp || "") + url,
          data: (object) ? JSON.stringify(object) : null,
          responseType: 'blob',
          headers: {
            'Content-Type': 'application/json', 
            'X-AUTH-TOKEN': _u.token,
          }
      }).success(function(data, status, headers, config) {
          var octetStreamMime = 'application/octet-stream';
          headers = headers();
          var filename = headers['x-filename'] || 'download.bin';
          var contentType = headers['content-type'] || octetStreamMime;
          var urlCreator = window.URL || window.webkitURL || window.mozURL || window.msURL;      
          try
          {
            var link = document.createElement('a');
            if('download' in link)
            {
              var url = urlCreator.createObjectURL(data);
              link.setAttribute('href', url);
              link.setAttribute("download", filename);
              var event = document.createEvent('MouseEvents');
              event.initMouseEvent('click', true, true, window, 1, 0, 0, 0, 0, false, false, false, false, 0, null);
              link.dispatchEvent(event);
            }
          } catch(ex) {
            console.log('Error downloading file');
            console.log(ex);
          }
      }).error(function(data, status, headers, config) {
          console.log('Error downloading file');
      });
    }
    
  };
  
  this.cronapi.internal.uploadFile = function(field, file, progressId) {
    var uploadUrl = '/api/cronapi/uploadFile';
    var formData = new FormData();
    formData.append("file", file);
    var _u = JSON.parse(sessionStorage.getItem('_u'));
    this.$promise = cronapi.$scope.$http({
        method: 'POST',
        url: (window.hostApp || "") + uploadUrl,
        data: formData,
        headers:  {
          'Content-Type': undefined, 
          'X-AUTH-TOKEN': _u.token 
        },
        onProgress: function(event) {
  				if (event.lengthComputable) {
            var complete = (event.loaded / event.total * 100 | 0);
            if (progressId) {
              var progress = document.getElementById(progressId);
              progress.value = progress.innerHTML = complete;
            }
          }
				}
    }).success(function(data, status, headers, config) {
        cronapi.screen.changeValueOfField(field, data.jsonString);
    }).error(function(data, status, headers, config) {
        alert('Error uploading file');
    });
  };
  
  /**
   * @category CategoryType.OBJECT
   * @categoryTags OBJECT|object
   */
  this.cronapi.object = {};

    /**
   *  @type function
    * @name {{getProperty}}
    * @nameTags getProperty
    * @param {ObjectType.OBJECT} object {{object}}
    * @param {ObjectType.STRING} property {{property}}
    * @description {{getPropertyDescription}}
    * @returns {ObjectType.OBJECT}
   */
   this.cronapi.object.getProperty = function(object, property) {
     var split = property.split('.');
     for (var i = 0; i < split.length; i++){ 
       object = object[split[i]];
     }
     return object;
   };
   
    /**
   *  @type function
    * @name {{setProperty}}
    * @nameTags setProperty
    * @param {ObjectType.OBJECT} object {{object}}
    * @param {ObjectType.STRING} property {{property}}
    * @param {ObjectType.OBJECT} value {{value}} 
    * @description {{setPropertyDescription}}
    * @returns {ObjectType.VOID}
   */
   this.cronapi.object.setProperty = function(object, property, value) {
     var split = property.split('.');
     for (var i = 0; i < split.length; i++){ 
       object = object[split[i]];
     }
     object = value;
   };  
    
   /**
    * @category CategoryType.DEVICE
    * @categoryTags CORDOVA|cordova|Dispositivos|device|Device
    */
   this.cronapi.cordova = {};

   /**
    *  @type function
     * @name {{vibrate}}
     * @platform M
     * @nameTags vibrate
     * @param {ObjectType.OBJECT} vibrateValue {{vibrateValue}}
     * @description {{vibrateDescription}}
     * @returns {ObjectType.VOID}
    */
   this.cronapi.cordova.vibrate = function(vibrateValue){
     navigator.vibrate(vibrateValue);
   };
   
   this.cronapi.cordova.geolocation = {};
   
     /**
    *  @type function
     * @platform M
     * @name {{getCurrentPosition}}
     * @nameTags geolocation|getCurrentPosition
     * @param {ObjectType.STATEMENTSENDER} success {{success}}
     * @param {ObjectType.STATEMENTSENDER} error {{error}}
     * @description {{getCurrentPositionDescription}}
     * @returns {ObjectType.VOID}
    */
   this.cronapi.cordova.geolocation.getCurrentPosition = function(success, error){
     navigator.geolocation.getCurrentPosition(success, error);
   };
   
       /**
    *  @type function
     * @platform M
     * @name {{watchPosition}}
     * @nameTags geolocation|watchPosition
     * @param {ObjectType.STATEMENTSENDER} success {{success}}
     * @param {ObjectType.STATEMENTSENDER} error {{error}}
     * @param {ObjectType.LONG} maximumAge {{maximumAge}}
     * @param {ObjectType.LONG} timeout {{timeout}}
     * @param {ObjectType.BOOLEAN} enableHighAccuracy {{enableHighAccuracy}}
     * @description {{watchPositionDescription}}
     * @returns {ObjectType.LONG}
    */
   this.cronapi.cordova.geolocation.watchPosition = function(success, error, maximumAge, timeout, enableHighAccuracy){
    return navigator.geolocation.watchPosition(callbackSuccess, callbackError, { maximumAge: maximumAge, timeout: timeout, enableHighAccuracy: enableHighAccuracy });
   };
   
       /**
    *  @type function
     * @platform M
     * @name {{clearWatchPosition}}
     * @nameTags geolocation|clearWatch
     * @param {ObjectType.LONG} watchID {{watchID}}
     * @description {{clearWatchPositionDescription}}
     * @returns {ObjectType.VOID}
    */
   this.cronapi.cordova.geolocation.clearWatchPosition = function(watchID){
     navigator.geolocation.clearWatch(watchID);
   };
   
   this.cronapi.cordova.camera = {};
   
   /**
     * @type function
     * @platform M
     * @name {{getPicture}}
     * @nameTags geolocation|getPicture
     * @description {{getPictureDescription}}
     * @returns {ObjectType.VOID}
    */
    
   this.cronapi.cordova.camera.getPicture = function(/** @type {ObjectType.STATEMENTSENDER} @description {{success}} */ success, /** @type {ObjectType.STATEMENTSENDER} @description {{error}} */  error, /** @type {ObjectType.LONG} @description {{destinationType}} @blockType util_dropdown @keys 0|1|2 @values DATA_URL|FILE_URI|NATIVE_URI  */  destinationType, /** @type {ObjectType.LONG} @description {{pictureSourceType}} @blockType util_dropdown @keys 0|1|2 @values PHOTOLIBRARY|CAMERA|SAVEDPHOTOALBUM  */ pictureSourceType) {
     navigator.camera.getPicture(success, error, { destinationType: destinationType , sourceType : pictureSourceType });
   };
   
   
    this.cronapi.cordova.file = {};
   
    /**
     * @type function
     * @platform M
     * @name {{getDirectory}}
     * @nameTags file|arquivo|directory|diretorio
     * @description {{getDirectoryDescription}}
     * @returns {ObjectType.STRING}
    */
   this.cronapi.cordova.file.getDirectory = function(/** @type {ObjectType.LONG} @description {{type}} @blockType util_dropdown @keys 0|1 @values {{INTERNAL}}|{{EXTERNAL}}  */  type) {
    var path;
    if (type == '0') {
      path = cordova.file.dataDirectory ;
    } else {
        path = cordova.file.externalApplicationStorageDirectory;
        if (!path) {
          path = cordova.file.externalDataDirectory;
        }
        if (!path) {
          path = cordova.file.syncedDataDirectory;
        }
    }
    return path;
   };
   
   
    /**
     * @type function
     * @platform M
     * @name {{removeFile}}
     * @nameTags file|arquivo|removeFile|remover
     * @param {ObjectType.STRING} fileName {{fileName}}
     * @param {ObjectType.STATEMENTSENDER} success {{success}}
     * @param {ObjectType.STATEMENTSENDER} error {{error}}
     * @description {{removeFileDescription}}
     * @returns {ObjectType.VOID}
    */
     this.cronapi.cordova.file.removeFile = function(fileName, success, error) {
        window.resolveLocalFileSystemURL(fileName, function (fileEntry) { 
          fileEntry.remove(function (entry) { 
          if (success)
            success(entry);
          },error);
        },error);
     };
   
     /**
     * @type function
     * @platform M
     * @name {{readFile}}
     * @nameTags file|arquivo|readFile|lerarquivo
     * @param {ObjectType.STRING} fileName {{fileName}}
     * @param {ObjectType.STATEMENTSENDER} success {{success}}
     * @param {ObjectType.STATEMENTSENDER} error {{error}}
     * @description {{readFileDescription}}
     * @returns {ObjectType.VOID}
    */
     this.cronapi.cordova.file.readFile = function(fileName, success, error) {
        window.resolveLocalFileSystemURL(fileName, function (fileEntry) { 
          fileEntry.file(function (file) { 
            var reader = new FileReader();
            reader.onloadend = function (e) {
                success(this.result);
            };
            reader.readAsText(file);
          },error);
        },error);
     };
   
     /**
     * @type function
     * @platform M
     * @name {{createFile}}
     * @nameTags file|arquivo|createFile|criararquivo
     * @param {ObjectType.STRING} dirEntry {{dirEntry}}
     * @param {ObjectType.STRING} fileName {{fileName}}
     * @param {ObjectType.STRING} content {{content}}
     * @param {ObjectType.STATEMENTSENDER} success {{success}}
     * @param {ObjectType.STATEMENTSENDER} error {{error}}
     * @description {{createFileDescription}}
     * @returns {ObjectType.VOID}
    */
   this.cronapi.cordova.file.createFile = function(dirEntry, fileName, content, success, error) {
     var path = (dirEntry || getDirectory(0));  
    window.resolveLocalFileSystemURL(dirEntry,  function(directoryEntry) {
      console.log(dirEntry);
        directoryEntry.getFile(fileName, {create: true }, function (fileEntry) {
           fileEntry.createWriter(function(fileWriter) {
             fileWriter.onwriteend = function (e) {
                  console.log('Write of file "' + fileName + '"" completed.');
              };
              fileWriter.onerror = function (e) {
                  console.log('Write failed: ' + e.toString());
              };
              var data = new Blob([content], { type: 'text/plain' });
              fileWriter.write(data);
              if (success) {
                setTimeout(function() {
                  success();   
                },500);
              }
           }, error);
        }, error);
    }, error);
   };
   
      /**
     * @type function
     * @platform M
     * @name {{createDirectory}}
     * @nameTags file|arquivo|criardiretorio
     * @param {ObjectType.STRING} dirParent {{dirParent}}
     * @param {ObjectType.STRING} dirChildrenName {{dirChildrenName}}
     * @param {ObjectType.STATEMENTSENDER} success {{success}}
     * @param {ObjectType.STATEMENTSENDER} error {{error}}
     * @description {{createDirectoryDescription}}
     * @returns {ObjectType.VOID}
    */
     this.cronapi.cordova.file.createDirectory = function(dirParent, dirChildrenName, success, error) {
        window.resolveLocalFileSystemURL(dirParent,  function(directoryEntry) {
          parentEntry.getDirectory(dirChildrenName, { create: true }, function (childrenEntry) {
            if (success)
                success(childrenEntry);
          },error);
        }, error);
     };

      this.cronapi.cordova.storage = {}; 

      /**
     * @type function
     * @platform M
     * @name {{setStorageItem}}
     * @nameTags storage
     * @param {ObjectType.STRING} key {{key}}
     * @param {ObjectType.OBJECT} value {{value}}
     * @description {{setStorageItemDescription}}
     * @returns {ObjectType.VOID}
    */
     this.cronapi.cordova.storage.setStorageItem = function(key, value) {
        var storage = window.localStorage;
        if (storage) {
          storage.setItem(key, value);
        } else {
          console.error('Local Storage not Found!');
        }
     };
     
      /**
     * @type function
     * @platform M
     * @name {{getStorageItem}}
     * @nameTags storage|getItem
     * @param {ObjectType.STRING} key {{key}}
     * @description {{getStorageItemDescription}}
     * @returns {ObjectType.OBJECT}
    */
     this.cronapi.cordova.storage.getStorageItem = function(key) {
        var storage = window.localStorage;
        if (storage) {
          return storage.getItem(key);
        } else {
          console.error('Local Storage not Found!');
        }
     };
   
       /**
     * @type function
     * @platform M
     * @name {{removeStorageItem}}
     * @nameTags storage|remove
     * @param {ObjectType.STRING} key {{key}}
     * @description {{removeStorageItemDescription}}
     * @returns {ObjectType.VOID}
    */
     this.cronapi.cordova.storage.removeStorageItem = function(key) {
        var storage = window.localStorage;
        if (storage) {
          storage.removeItem(key);
        } else {
          console.error('Local Storage not Found!');
        }
     };
     
     this.cronapi.cordova.connection = {};
     
     /**
      * @type function
      * @platform M
      * @name {{getConnection}}
      * @nameTags connection
      * @description {{getConnectionDescription}}
      * @returns {ObjectType.BOOLEAN}
     */
     this.cronapi.cordova.connection.getConnection = function() {
       return navigator.connection.type;
     };
     
       /**
      * @type function
      * @platform M
      * @name {{verifyConnection}}
      * @nameTags connection
      * @description {{verifyConnectionDescription}}
      * @returns {ObjectType.BOOLEAN}
     */
     this.cronapi.cordova.connection.verifyConnection = function(/** @type {ObjectType.STRING} @description {{type}} @blockType util_dropdown @keys Connection.UNKNOWN|Connection.ETHERNET|Connection.WIFI|Connection.CELL_2G|Connection.CELL_3G|Connection.CELL_4G|Connection.CELL|Connection.NONE @values {{UnknownConnection}}|{{EthernetConnection}}|{{WiFiConnection}}|{{Cell2GConnection}}|{{Cell3GConnection}}|{{Cell4GConnection}}|{{CellGenericConnection}}|{{NoNetworkConnection}}  */  type) {
       return (navigator.connection.type == type);
     };
 
     this.cronapi.cordova.database = {};
     this.cronapi.cordova.database.nameDefault = "cronappDB";

     /**
      * @type function
      * @platform M
      * @name {{openDatabase}}
      * @nameTags openDatabase
      * @param {ObjectType.STRING} name {{name}}
      * @description {{openDatabaseDescription}}
      * @returns {ObjectType.VOID}
     */
     this.cronapi.cordova.database.openDatabase = function(dbName) {
       if (!dbName) {
         this.cronapi.cordova.database.name = this.cronapi.cordova.database.nameDefault;
         if (window.BuildInfo) {
           this.cronapi.cordova.database.name = BuildInfo.packageName;
         }
       } 
       this.cronapi.cordova.database.object = window.openDatabase({ name : this.cronapi.cordova.database.name });
     };
     
     /**
      * @type function
      * @platform M
      * @name {{executeSql}}
      * @nameTags executesql
      * @param {ObjectType.STRING} text {{text}}
      * @param {ObjectType.OBJECT} array {{array}}
      * @param {ObjectType.STATEMENTSENDER} success {{success}}
      * @param {ObjectType.STATEMENTSENDER} error {{error}}
      * @description {{executeSqlDescription}}
      * @returns {ObjectType.VOID}
     */
     this.cronapi.cordova.database.executeSql = function(text, array, success , error){
       
       // exist DB
       if (!this.cronapi.cordova.database.object)
         this.cronapi.cordova.database.openDatabase(); // create DB
       
       // open transaction
       this.cronapi.cordova.database.object.transaction(function(connect){
         // execute SQL
         connect.executeSql(text,array, success);
       }.bind(this), function(e){
         // error
         console.log(e);
         error(e);
       }.bind(this));
       
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
      if (string)
        return escape(this._utf8_encode(string));
      return '';
    },
    // public method for url decoding
    decode : function (string) {
      if (string)
        return this._utf8_decode(unescape(string));
      return '';
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