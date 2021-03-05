
if (window.fixedTimeZone === undefined || window.fixedTimeZone === null) {
  window.fixedTimeZone = true;
}

if (window.timeZone === undefined || window.timeZone === null) {
  window.timeZone = "UTC";
}

if (window.timeZoneOffset === undefined || window.timeZoneOffset === null) {
  window.timeZoneOffset = 0;
}

window.systemTimeZoneOffset = window.timeZoneOffset;

if (!window.fixedTimeZone) {
  window.timeZoneOffset = moment().utcOffset();
}

function cronapi() {
  'use strict';

  this.$evt = function(str) {
    var self = this;
    if (!self.$eval) {
      self = angular.element(event.target).scope();
    }

    self.$eval(str);
  }.bind(this);

  this.cronapi = {};

  this.cronapi.toDate = function(value) {
    return new Date(value);
  }

  var getDatasource = function(ds) {
    if (typeof ds == 'string') {
      return window[ds];
    } else {
      return ds;
    }
  }

  this.cronapi.doEval = function(arg) {
    return arg;
  }

  this.cronapi.evalInContext = async function(js) {
    var result = eval('this.cronapi.doEval('+js+')');
    if (result) {
      if (result.commands) {
        for (var i = 0; i < result.commands.length; i++) {
          var func = eval(result.commands[i].function);
          await func.apply(this, result.commands[i].params);
        }
      }
      return result.value;
    }
  }

  let clientMap = {};

  this.cronapi.client = function(pack) {
    let attr = false;
    return {
      attr: function() {
        attr = true;
        return this;
      },
      run: function() {
        var key = pack;

        for (var i = 0;i <arguments.length;i++) {
          key += String(arguments[i]);
        }

        var bk;
        try {
          bk = eval('blockly.'+pack);
        } catch(e) {
          //
        }

        if (!bk) {
          bk = eval(pack);
        }

        if (attr) {
          let result = bk.apply(this, arguments);
          if (result !== undefined && result !== null && result.then && typeof result.then === 'function') {
            result.then(value => {
              if (clientMap[key] !== value) {
                this.safeApply(() => {
                  clientMap[key] = value;
                });
              }
            }).catch((error) => {
                if (clientMap[key] !== error) {
                this.safeApply(() => {
                  clientMap[key] = error;
                });
              }
            });

            return clientMap[key];
          } else {
            return result;
          }
        } else {
          let isAsync = bk.constructor.name === "AsyncFunction";
          if (isAsync)
            return bk.apply(this, arguments).catch((error) => this.cronapi.$scope.Notification.error(error));
          else
            return bk.apply(this, arguments);
        }
      }.bind(this)
    }
  };

  var serverMap = {};

  this.cronapi.server = function(pack) {
    var attr = false;
    var toPromise = false;
    var async = true;
    var notificationOnError = true;
    return {
      attr: function() {
        attr = true;
        return this;
      },
      toPromise: function() {
        toPromise = true;
        return this;
      },
      notAsync: function() {
        async = false;
        return this;
      },
      disableNotification: function() {
        notificationOnError = false;
        return this;
      },
      run: function() {
        var key = pack;

        for (var i = 0;i <arguments.length;i++) {
          key += String(arguments[i]);
        }

        if (attr) {
          if (serverMap.hasOwnProperty(key)) {
            if (serverMap[key] != "$$loading") {
              return serverMap[key];
            } else {
              return "";
            }
          }
          serverMap[key] = "$$loading";
        }

        var parts = pack.split(".");
        var func = parts[parts.length-1];
        parts.pop();
        var namespace = parts.join(".");

        var blocklyName = namespace + ":" + func;

        var resolveForPromise;
        var rejectForPromise;
        var promise = new Promise((resolve, reject) => {
          resolveForPromise = resolve;
          rejectForPromise = reject;
        });

        var success = function(data) {
          this.safeApply(function() {
            if (attr) {
              serverMap[key] = data;
            }
            resolveForPromise(data);
          });
        }.bind(this);

        var error = function(error) {
          this.safeApply(function() {
            if (attr) {
              serverMap[key] = error;
            }
            rejectForPromise(error);
          });
          if (error && notificationOnError)
            this.cronapi.$scope.Notification.error(error);
        }.bind(this);

        var args = [blocklyName];

        if (async) {
          args.push(success);
          args.push(error);
          for (var i = 0;i <arguments.length;i++) {
            args.push(arguments[i]);
          }

          this.cronapi.util.makeCallServerBlocklyAsync.apply(this, args);
          if (toPromise)
            return promise;
        }
        else {
          for (var i = 0;i <arguments.length;i++) {
            args.push(arguments[i]);
          }

          let resultData = this.cronapi.util.callServerBlockly.apply(this, args);
          if (attr) {
            serverMap[key] = resultData;
          }
          return resultData;
        }

      }.bind(this)
    }
  };

  this.cronapi.callFunction = function(name) {
    return {
      call: function() {
        var ref;
        try {
          ref = eval(name);
        } catch(e) {
          //
        }

        if (ref) {
          return ref.apply(this, arguments)
        }

        return undefined;
      }.bind(this)
    }
  };


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
    return this.cronapi.internal.parseBoolean(value);
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
    return this.cronapi.internal.stringToJs(value);
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
          var functionToCall = eval("cronapi.internal." + userLang + "Date");
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
   * @type function
   * @name {{setRequestToken}}
   * @nameTags token
   * @description {{setRequestTokenDescription}}
   * @param {ObjectType.STRING} token {{setRequesTokenParam}}
   */
  this.cronapi.util.setToken = function(token) {
    let currentSession = {};
    if (localStorage.getItem("_u")) {
      currentSession = JSON.parse(localStorage.getItem("_u"));
      currentSession.token = token;
    }
    localStorage.setItem("_u", JSON.stringify(currentSession));
    window.uToken = token;
  };

  /**
   * @type function
   * @name {{getApplicationName}}
   * @nameTags getApplicationName
   * @description {{functionToGetApplicationName}}
   * @returns {ObjectType.STRING}
   */
  this.cronapi.util.getApplicationName = function() {
    return $('#projectName').length ? $('#projectName').val() : $('h1:first').length && $('h1:first').text().trim().length ? $('h1:first').text().trim() : '';
  };

  /**
   * @type function
   * @name {{createPromiseName}}
   * @nameTags createPromiseName
   * @description {{functioncreatePromise}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.util.createPromise = function () {
    var functionToResolve;
    var functionToReject;
    var promise = new Promise((resolve, reject) => {
      functionToResolve = resolve;
      functionToReject = reject;
    });
    promise.__functionToResolve = functionToResolve;
    promise.__functionToReject = functionToReject;
    return promise;
  }

  /**
   * @type function
   * @name {{handleValueToPromise}}
   * @nameTags handleValueToPromise
   * @description {{functionToHandleValueToPromise}}
   * @param {ObjectType.STRING} type {{type}}
   * @param {ObjectType.OBJECT} promise {{promise}}
   * @param {ObjectType.OBJECT} value {{value}}
   */
  this.cronapi.util.handleValueToPromise = function (/** @type {ObjectType.STRING} @description {{type}} @blockType util_dropdown @keys resolve|reject @values resolve|reject  */ type, promise, value) {
    if(type === 'resolve') {
      promise.__functionToResolve(value);
    }else{
      promise.__functionToReject(value);
    }
  }

  /**
   * @type function
   * @name {{sleep}}
   * @nameTags sleep, dormir, wait, interval
   * @description {{sleepDescription}}
   * @param {ObjectType.LONG} interval {{sleepInterval}}
   */
  this.cronapi.util.sleep = async function (interval) {
    var promise = new Promise((resolve) => {
      setInterval(() => {
        resolve(interval);
      }, interval)
    });
    return promise;
  }

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

    const getCircularReplacer = () => {
      const seen = new WeakSet();
      return (key, value) => {
        if (typeof value === "object" && value !== null) {
          if (seen.has(value)) {
            return;
          }
          seen.add(value);
        }
        return value;
      };
    };

    var serverUrl = 'api/cronapi/call/body/#classNameWithMethod#/'.replace('#classNameWithMethod#', classNameWithMethod);
    var http = this.cronapi.$scope.http;
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

    var finalUrl = this.cronapi.internal.getAddressWithHostApp(serverUrl);
    let contentData = undefined;
    try {
      contentData = JSON.stringify(dataCall);
    }
    catch (e) {
      contentData = JSON.stringify(dataCall, getCircularReplacer());
    }

    var resultData = $.ajax({
      type: 'POST',
      url: finalUrl,
      dataType: 'html',
      data : contentData,
      headers : {
        'Content-Type' : 'application/json',
        'X-AUTH-TOKEN' : token,
        'toJS' : true,
        'timezone': moment().utcOffset()
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

    for (var key in this.cronapi.$scope) {
      if (this.cronapi.$scope[key] && this.cronapi.$scope[key].constructor && this.cronapi.$scope[key].constructor.name=="DataSet") {
        fields[key] = {};
        fields[key].active = this.cronapi.$scope[key].active;
      }
    }

    var scope = this.cronapi.$scope;
    var recursiveLookup = function(scope) {
      var fieldValue;
      try {
        fieldValue = eval(scope.vars);
      }
      catch (e) {
      }
      if(fieldValue && Object.keys(fieldValue).length !== 0) {
        var keys = Object.keys(fieldValue);
        keys.forEach(function(key){
          if (fieldValue[key] !== undefined && fieldValue[key] !== null) {
            if (!fields.vars) {
              fields.vars = {};
            }
            fields.vars[key] = fieldValue[key];
          }
        });
      }
      else if(scope && scope.$parent ) {
        return recursiveLookup(scope.$parent);
      }
      return;
    };
    recursiveLookup(scope);

    for (var key in this.cronapi.$scope.params) {
      if (this.cronapi.$scope.params[key]) {
        if (!fields.params) {
          fields.params = {};
        }
        fields.params[key] = this.cronapi.$scope.params[key];
      }
    }

    return fields;
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
   * @arbitraryParams   if (window.event.target && window.event.target) {
      window.cronapi.$scope = angular.element(window.event.target).scope();
    }true
   */
  this.cronapi.util.makeCallServerBlocklyAsync = function(blocklyWithFunction, callbackSuccess, callbackError) {
    var fields = this.cronapi.util.getScreenFields();

    var paramsApply = [];
    paramsApply.push(blocklyWithFunction);
    paramsApply.push(fields);
    paramsApply.push(function(data) {
      this.cronapi.evalInContext(data).then((result) => {
        if (typeof callbackSuccess == "string") {
          eval(callbackSuccess)(result);
        } else if (callbackSuccess) {
          callbackSuccess(result);
        }
      });
    }.bind(this));
    paramsApply.push(function(data, status, errorThrown) {
      var message = this.cronapi.internal.getErrorMessage(data.responseText, errorThrown);
      if (typeof callbackError == "string") {
        eval(callbackError)(message);
      }
      else if (callbackError) {
        callbackError(message);
      }
      else {
        this.cronapi.$scope.Notification.error(message);
      }
    }.bind(this));
    $(arguments).each(function(idx) {
      if (idx >= 3)
        paramsApply.push(this);
    });
    this.cronapi.util.callServerBlocklyAsync.apply(this, paramsApply);
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
  this.cronapi.util.callServerBlocklyNoReturn = async function() {
    return this.cronapi.util.callServerBlockly.apply(this, arguments);
  };

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
   * @name {{language}}
   * @nameTags language, i18n, idioma, linguagem, locale
   * @description {{languageDescription}}
   * @returns {ObjectType.STRING}
   */
  this.cronapi.util.language = function() {
    var locale = (window.navigator.userLanguage || window.navigator.language || 'pt_br').replace('-', '_');
    return locale;
  };

  /**
   * @type function
   * @name {{share}}
   * @nameTags share, compartilhar, enviar, abrir como
   * @description {{shareDescription}}
   * @param {ObjectType.STRING} title {{shareParam0}}
   * @param {ObjectType.STRING} text {{shareParam1}}
   * @param {ObjectType.STRING} url {{shareParam2}}
   * @returns {ObjectType.STRING}
   */
  this.cronapi.util.share = function(title, text, url) {
    navigator.share({
      title: title,
      text: text,
      url: url
    }).then(() => console.log('Successful share'))
        .catch(error => console.log('Error sharing:', error));
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
  this.cronapi.util.callServerBlockly = async function(classNameWithMethod) {
    let params = []
    params.push(classNameWithMethod);
    params.push(null); // This argument will be used as the resolve method callback
    params.push(null); // This argument will be used as the reject method callback
    let idx = 1; // idx should be 1 to ignore the declared argument 'classNameWithMethod'
    for(idx; idx < arguments.length ; idx ++){
      params.push(arguments[idx]);
    }

    return  new Promise(((resolve, reject) => {
      params[1] = ((data) => {
        resolve(data);
      }).bind(this);
      params[2] = ((error) => {
        reject(error);
      }).bind(this);
      this.cronapi.util.makeCallServerBlocklyAsync.apply(this, params);
    }).bind(this));
  };

    /**
     * @type function
     * @name {{callServerBlocklyAsync}}
     * @nameTags callServerBlocklyAsync
     * @description {{callServerBlocklyAsync}}
     * @param {ObjectType.STRING} classNameWithMethod {{classNameWithMethod}}
     * @param {ObjectType.OBJECT} callback {{callbackFinish}}
     * @param {ObjectType.LIST} params {{params}}
     * @wizard procedures_callblockly_callreturn_async
     * @returns {ObjectType.OBJECT}
     */
  this.cronapi.util.callServerBlocklyAsynchronous = function(classNameWithMethod , callback , params) {
    if(classNameWithMethod != '' && typeof callback == 'function'){
      var params = [];
      params.push(classNameWithMethod);
      params.push(callback);
      params.push(callback);
      var idx = 2;
      for(idx; idx < arguments.length ; idx ++){
        params.push(arguments[idx]);
      };
      this.cronapi.util.makeCallServerBlocklyAsync.apply(this,params);
    }
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
   * @name {{downloadFileName}}
   * @nameTags downloadFile
   * @description {{downloadFileDescription}}
   * @param {ObjectType.STRING} url {{downloadFileParam0}}
   * @multilayer true
   */
  this.cronapi.util.downloadFile = function(url) {

    var finalUrl = this.cronapi.internal.getAddressWithHostApp(url);
    this.cronapi.screen.openUrl(finalUrl, '_blank' ,0,0 );
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
   * @type function
   * @name {{openReport}}
   * @nameTags openReport|abrirrelatorio
   * @description {{openReportDescription}}
   * @param {ObjectType.STRING} value {{report}}
   * @multilayer true
   * @returns {ObjectType.VOID}
   * @wizard procedures_openreport_callnoreturn
   */
  this.cronapi.util.openReport = function(/** @type {ObjectType.STRING} @blockType util_report_list */ name) {
    this.cronapi.$scope.getReport(name);
  };

  /**
   * @type internal
   */
  this.cronapi.util.handleCallback = function(ref) {
    if (ref) {
      let isAsync = ref.constructor.name === "AsyncFunction";
      if (isAsync) {
        return function() {
          (async ()=> await ref.apply(this, arguments))()
        }.bind(this);
      } else {
        return ref.bind(this);
      }
    }
  }

  /**
   * @type function
   * @name {{getURLFromOthersName}}
   * @description {{getURLFromOthersDescription}}
   * @nameTags URL|API|Content|Download|Address|Endereco|Conteudo
   * @param {ObjectType.STRING} method {{HTTPMethod}}
   * @param {ObjectType.STRING} contentType {{contentType}}
   * @param {ObjectType.STRING} url {{URLAddress}}
   * @param {ObjectType.STRING} params {{paramsHTTP}}
   * @param {ObjectType.STRING} headers {{headers}}
   * @param {ObjectType.STRING} success {{success}}
   * @param {ObjectType.STRING} error {{error}}
   */
  this.cronapi.util.getURLFromOthers = function(/** @type {ObjectType.STRING} @description {{HTTPMethod}} @blockType util_dropdown @keys GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS|TRACE @values GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS|TRACE  */  method , /** @type {ObjectType.STRING} @description {{HTTPMethod}} @blockType util_dropdown @keys application/x-www-form-urlencoded|application/json @values application/x-www-form-urlencoded|application/json  */  contentType , /** @type {ObjectType.STRING} @description {{URLAddress}} */ url, /** @type {ObjectType.OBJECT} @description {{paramsHTTP}} */ params, /** @type {ObjectType.OBJECT} @description {{headers}} */ headers, /** @type {ObjectType.STATEMENTSENDER} @description {{success}} */ success, /** @type {ObjectType.STATEMENTSENDER} @description {{error}} */  error ) {

    if (params && contentType === "application/x-www-form-urlencoded") {
      for (var key in params) {
        if (params[key] instanceof Array) {
          var arrayContent = params[key].toString();
          params[key] = `[${arrayContent}]`;
        }
      }
    }
    else if (params && contentType === "application/json") {
      params = JSON.stringify(params);
    }

    var header = Object.create(headers);
    header["Content-Type"] = contentType;
    // Angular has a .run that inject X-AUTH-TOKEN, so we use JQuery
    $.ajax({
      method : method,
      url : url,
      data: params,
      headers: header
    }).done(this.cronapi.util.handleCallback(success).bind(this)).fail(this.cronapi.util.handleCallback(error.bind(this)));

  };


  /**
   * @type function
   * @name {{getUserToken}}
   * @nameTags token | auth | autenticaçào | armazenamento
   * @description {{getUserTokenDesc}}
   * @returns {ObjectType.STRING}
   */
  this.cronapi.util.getUserToken = function() {
    return JSON.parse(window.localStorage.getItem('_u')).token;
  };

  /**
   * @type function
   * @name {{setSessionStorage}}
   * @nameTags storage | session | sessão | armazenamento
   * @description {{setSessionStorageDesc}}
   * @param {ObjectType.STRING} key {{key}}
   * @param {ObjectType.STRING} value {{value}}
   */
  this.cronapi.util.setSessionStorage = function(key, value) {
    window.sessionStorage.setItem(key, value);
  };

  /**
   * @type function
   * @name {{getSessionStorage}}
   * @nameTags storage | session | sessão | armazenamento
   * @description {{getSessionStorageDesc}}
   * @param {ObjectType.STRING} key {{key}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.util.getSessionStorage = function(key) {
    return window.sessionStorage.getItem(key);
  };

  /**
   * @type function
   * @name {{setLocalStorage}}
   * @nameTags storage | session | sessão | armazenamento
   * @description {{setLocalStorageDesc}}
   * @param {ObjectType.STRING} key {{key}}
   * @param {ObjectType.STRING} value {{value}}
   */
  this.cronapi.util.setLocalStorage = function(key, value) {
    window.localStorage.setItem(key, value);
  };

  /**
   * @type function
   * @name {{getLocalStorage}}
   * @nameTags storage | session | sessão | armazenamento
   * @description {{getLocalStorageDesc}}
   * @param {ObjectType.STRING} key {{key}}
   * @returns {ObjectType.STRING}
   */
  this.cronapi.util.getLocalStorage = function(key) {
    return window.localStorage.getItem(key);
  };


  /**
   * @type function
   * @name {{executeAsynchronousName}}
   * @nameTags Executar|Assíncrono|Execute| Asynchronous
   * @description {{executeAsynchronousDescription}}
   * @param {ObjectType.STATEMENT} statement {{statement}}
   *
   */
  this.cronapi.util.executeAsynchronous = function( /** @type {ObjectType.STATEMENT} @description {{statement}} */ statement) {
    setTimeout(statement , 0 );
  };

  /**
   * @type function
   * @name {{scheduleExecutionName}}
   * @nameTags Executar|Agenda|Agendar|Agendamento|Execução|Execute|Execution|Schedule|Scheduled
   * @description {{scheduleExecutionDescription}}
   * @param {ObjectType.STATEMENT} statement {{statement}}
   * @param {ObjectType.LONG} initial_time {{scheduleExecutionParam1}}
   * @param {ObjectType.LONG} interval_time {{scheduleExecutionParam2}}
   * @param {ObjectType.STRING} measurement_unit {{scheduleExecutionParam3}}
   * @param {ObjectType.BOOLEAN} stopExecutionAfterScopeDestroy {{stopExecutionAfterScopeDestroyLabel}}
   */
  this.cronapi.util.scheduleExecution = function( /** @type {ObjectType.STATEMENT} @description {{statement}} */ statements ,  /** @type {ObjectType.LONG} */  initial_time ,  /** @type {ObjectType.LONG} */  interval_time , /** @type {ObjectType.STRING} @description {{scheduleExecutionParam3}} @blockType util_dropdown @keys seconds|milliseconds|minutes|hours @values {{seconds}}|{{millisecondss}}|{{minutes}}|{{hours}}  */ measurement_unit, /** @type {ObjectType.BOOLEAN} @description {{stopExecutionAfterScopeDestroyLabel}} @blockType util_dropdown @keys true|false @values {{true}}|{{false}}  */  stopExecutionAfterScopeDestroy ) {

    stopExecutionAfterScopeDestroy = stopExecutionAfterScopeDestroy || true;
    stopExecutionAfterScopeDestroy = (stopExecutionAfterScopeDestroy === 'true' || stopExecutionAfterScopeDestroy === true);

    var factor = 1;

    if (measurement_unit === 'seconds') {
      factor = 1000;
    } else if(measurement_unit ==='minutes') {
      factor = 60000;
    } else if(measurement_unit ==='hours') {
      factor = 3600000;
    }

    initial_time = initial_time * factor;
    interval_time = interval_time * factor;

    var intervalId = -1;

    var timeoutId = setTimeout(function() {
      statements();
      intervalId = setInterval(statements , interval_time) ;
    }.bind(this), initial_time);

    if(stopExecutionAfterScopeDestroy){
      this.$on('$destroy', function() {
        try { clearTimeout(timeoutId); } catch(e) {}
        try { clearInterval(intervalId); } catch(e) {}
      });
    }

  };

  /**
   * @type internal
   */
  this.cronapi.util.openReport = function(name, params, config) {
    this.cronapi.$scope.getReport(name, params, config);
  };

  /**
   * @type function
   * @name {{getCEPName}}
   * @description {{getCEPDescription}}
   * @param {ObjectType.STRING} cep {{CEP}}
   * @param {ObjectType.STRING} success {{success}}
   */
  this.cronapi.util.getCEP = function(/** @type {ObjectType.STRING} @description {{CEP}} */ cep, /** @type {ObjectType.STATEMENTSENDER} @description {{success}} */ success) {

    if(this.cronapi.logic.isNullOrEmpty(cep)) throw new Error("Informe o CEP");

    cep = cep.replace(/\.|\-/g, '').split(' ').join('');

    if(cep.length < 8) throw new Error("CEP inválido");

    let url = "https://viacep.com.br/ws/" + cep + "/json/?callback=?";

    $.getJSON(url, this.cronapi.util.handleCallback( success.bind(this) ));

  };

  /**
   * @category CategoryType.SCREEN
   * @categoryTags Screen|Tela
   */
  this.cronapi.screen = {};

  /**
   * @type function
   * @name {{isInsertingMode}}
   * @nameTags isInsertingMode
   * @description {{functionToIsInsertingMode}}
   * @param {ObjectType.OBJECT} datasource {{datasource}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.screen.isInsertingMode = function(datasource) {
    return datasource.inserting;
  };

  /**
   * @type function
   * @name {{isEditingMode}}
   * @nameTags isEditingMode
   * @description {{functionToIsEditingMode}}
   * @param {ObjectType.OBJECT} datasource {{datasource}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.screen.isEditingMode = function(datasource) {
    return datasource.editing;
  };

  /**
   * @type function
   * @name {{changeTitleScreen}}
   * @nameTags changeTitleScreen
   * @description {{functionToChangeTitleScreen}}
   * @param {ObjectType.STRING} title {{title}}
   */
  this.cronapi.screen.changeTitleScreen = function(title) {
    window.document.title = title;
  };

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
      this.__tempValue = value;
      var func = new Function('this.' + field + ' = this.__tempValue;');
      this.safeApply(func.bind(this));
    }
    catch (e) {
      // NO COMMAND
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
        if (field.indexOf('.active.') > -1)
          return eval(field);
        else{
            var scope = eval('this');
            var recursiveLookup = function(scope) {
              var fieldValue;
              try {
                fieldValue = eval("scope." + field);
              }
              catch (e) {
              }
              if(fieldValue !== undefined || fieldValue !== null){
                return fieldValue;
              }
              else if(scope && scope.$parent ) {
                return recursiveLookup(scope.$parent);
              }
              return '';
            };
            return recursiveLookup(scope);
        }
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
    this.cronapi.$scope.vars[name] = value;
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
    return this.cronapi.$scope.vars[name];
  };

  /**
   * @type function
   * @name {{screenNotifyName}}
   * @description {{screenNotifyDescription}}
   * @nameTags show | exibir | exibe | notification | notificação
   * @param {ObjectType.STRING} type {{screenNotifyParam0}}
   * @param {ObjectType.STRING} message {{screenNotifyParam1}}
   * @wizard notify_type
   * @multilayer true
   */
  this.cronapi.screen.notify = function(/** @type {ObjectType.STRING} */ type, /** @type {ObjectType.STRING} */  message) {
    if (message == null || message == undefined) {
      message = '';
    }

    this.cronapi.$scope.Notification({'message':message.toString() },type);
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
  this.cronapi.screen.startInsertingMode = async function(/** @type {ObjectType.OBJECT} @blockType datasource_from_screen*/ datasource) {
    return new Promise((resolve, reject) =>{
      getDatasource(datasource).$apply(() => {
        try {
          getDatasource(datasource).startInserting(null, ()=> {
            resolve();
          });
        }
        catch(e) {
          reject(e);
        }
      });
    });
  };

  /**
   * @type function
   * @name {{startEditingModeName}}
   * @nameTags startEditingMode
   * @description {{startEditingModeDescription}}
   * @param {ObjectType.STRING} datasource {{startEditingModeParam0}}
   * @multilayer true
   */
  this.cronapi.screen.startEditingMode = async function(/** @type {ObjectType.OBJECT} @blockType datasource_from_screen*/ datasource) {
    return new Promise((resolve, reject) =>{
      getDatasource(datasource).$apply(() => {
        try {
          getDatasource(datasource).startEditing(null, ()=> {
            resolve();
          });
        }
        catch(e) {
          reject(e);
        }
      });
    });
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
    getDatasource(datasource).$apply( new function(){getDatasource(datasource).previous();} );
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
    getDatasource(datasource).$apply( new function(){getDatasource(datasource).next();} );
  };

  /**
   * @type function
   * @name {{firstRecordName}}
   * @nameTags firstRecord
   * @description {{firstRecordDescription}}
   * @param {ObjectType.STRING} datasource {{firstRecordParam0}}
   * @multilayer true
   */

  this.cronapi.screen.firstRecord = function(/** @type {ObjectType.OBJECT} @blockType datasource_from_screen*/ datasource) {
    getDatasource(datasource).$apply( new function(){
      var ds = getDatasource(datasource);
      ds.cursor = -1;
      ds.next();
    } );
  };

  /**
   * @type function
   * @name {{lastRecordName}}
   * @nameTags lastRecord
   * @description {{lastRecordDescription}}
   * @param {ObjectType.STRING} datasource {{lastRecordParam0}}
   * @multilayer true
   */

  this.cronapi.screen.lastRecord = function(/** @type {ObjectType.OBJECT} @blockType datasource_from_screen*/ datasource) {
    getDatasource(datasource).$apply( new function(){
      var ds = getDatasource(datasource);
      ds.cursor = ds.data.length-2;
      ds.next();
    } );
  };

  /**
   * @type function
   * @name {{removeRecordName}}
   * @nameTags removeRecord
   * @description {{removeRecordDescription}}
   * @param {ObjectType.STRING} datasource {{removeRecordParam0}}
   * @multilayer true
   */
  this.cronapi.screen.removeRecord = async function(/** @type {ObjectType.OBJECT} @blockType datasource_from_screen*/ datasource) {
    return new Promise((resolve, reject) =>{
      getDatasource(datasource).$apply(() => {
        try {
          getDatasource(datasource).removeSilent(null, () => {
            resolve();
          }, (e) => {
            reject(e);
          });
        }
        catch(e) {
          reject(e);
        }
      });
    });
  };

  /**
   * @type function
   * @name {{refreshActiveRecordName}}
   * @nameTags hasNextRecord
   * @description {{refreshActiveRecordDescription}}
   * @param {ObjectType.STRING} datasource {{refreshActiveRecordParam0}}
   * @multilayer true
   */
  this.cronapi.screen.refreshActiveRecord = async function(/** @type {ObjectType.OBJECT} @blockType datasource_from_screen*/ datasource) {
    return new Promise((resolve, reject) =>{
      try {
        getDatasource(datasource).refreshActive(() => {
          resolve();
        }, (e) => {
          reject(e);
        });
      }
      catch(e) {
        reject(e);
      }
    });
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
    return getDatasource(datasource).hasNext();
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
    return getDatasource(datasource).data.length;
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
    return getDatasource(datasource).postSilent();
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
    if(getDatasource(datasource).isOData()){
      getDatasource(datasource).search(path);
    }
    else{
      getDatasource(datasource).filter('/' + path);
    }
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
      var queryString = '';

      var paramsStopEncode = {};
      paramsStopEncode['%24'] = '$';

      function decodeCharParam(value) {
        if (value) {
          for (var param in paramsStopEncode) {
            var regex = eval('/' + param + '/g' );
            value = value.replace(regex, paramsStopEncode[param]);
          }
        }
        return value;
      }

      if (typeof params != 'undefined') {
        for (var i in Object.keys(params)) {
          var k = Object.keys(params[i])[0];

          var paramValue = Object.values(params[i])[0];

          if (paramValue instanceof Date) {
            paramValue = paramValue.toISOString();
          }

          var v = String(paramValue);
          if (queryString) {
            queryString += "&";
          }
          queryString += decodeCharParam(encodeURIComponent(k)) + "=" + encodeURIComponent(v);
        }
      }

      let existRoute = (view) => {
        if (this.cronapi.$scope && this.cronapi.$scope.$state) {
          let $states = this.cronapi.$scope.$state.get();
          let viewSplited = view.split("/").slice(2);
          let templateToFind = "views/" + viewSplited.join("/") + ".view.html";

          $states.forEach((s)=> {
            let templateUrl  = s.templateUrl;
            if (templateUrl instanceof Function)  {
              let regexExecuted = /('|")[a-z0-9/.]+('|")/gim.exec(templateUrl.toString());
              if (regexExecuted !== null && regexExecuted !== undefined && regexExecuted[0])
                templateUrl = regexExecuted[0].replace(/'/g, "");
            }
            if (templateUrl === templateToFind)
              view = "#" + s.url;
          })
        }
        return view;
      };

      var oldHash = window.location.hash;
      view = existRoute(view);
      window.location.hash = view + (queryString?"?"+queryString:"");

      var oldHashToCheck = oldHash + (oldHash.indexOf("?") > -1 ? "": "?");
      var viewToCheck = view + (view.indexOf("?") > -1 ? "": "?");

      this.cronapi.forceCloseAllModal();

      if(oldHashToCheck.indexOf(viewToCheck) >= 0){
        window.location.reload();
      }

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

      var vars = [], hash;
      var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
      for(var i = 0; i < hashes.length; i++)
      {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
      }

      if (vars[paramName] !== undefined)
        return decodeURIComponent(vars[paramName]);
    }
    catch (e) {
      //
    }

    return null;
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
    this.cronapi.screen.showModal('modalTemplate');

  };

  /**
   * @type function
   * @name {{showModal}}
   * @nameTags Show| Modal| Exibir| Mostrar
   * @platform W
   * @description {{showModalDesc}}
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @multilayer true
   */
  this.cronapi.screen.showModal = function(/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id) {
      let modalToShow = `#${id}`;
      let focused = $(':focus');
      let allHover = $(':hover');

      try{
          $(modalToShow).one('shown.bs.modal', function (e) {

              if (focused.length) {
                  $(this).data('lastFocused', focused);
                  $(this).data('lastFocusedClass', '.' + focused.attr('class').split(' ').join('.'));
              }
              if (allHover.length) {
                  $(this).data('lastHovers', allHover);
              }
              let firstInputVisible = $(this).find('input:not(:hidden)')[0];
              if (firstInputVisible)
                  firstInputVisible.focus();

          }).one('hidden.bs.modal', function(e) {

              let lastFocusedClass = undefined;
              let lastFocused = $(modalToShow).data('lastFocused');
              let lastFocusedIsVisible = false;

              if (lastFocused && lastFocused.length) {
                  //Verifica se o item que foi clicado ainda existe no documento (A grade remove o botao e adiciona novamente, perde a referencia)
                  lastFocusedClass = $($(this).data('lastFocusedClass'));
                  if ($('html').has(lastFocused).length) {
                      //Se existir, verifica se o mesmo está visivel
                      lastFocusedIsVisible = lastFocused.is(':visible');
                  }
                  else {
                      //Se nao existir, e foi readicionado verifica se está visivel pelas classes
                      lastFocusedIsVisible = lastFocusedClass.is(':visible');
                  }
              }

              let findLastLink = function(element) {
                return $(element[element.length - 1]).closest('li:visible').find('a:first');
              };

              let lastHovers = $(modalToShow).data('lastHovers');

              if (lastFocusedIsVisible) {
                  lastFocusedClass.focus();
                  lastFocused.focus();
              }
              //Tenta achar o mais proximo do ultimo click (link clicado)
              else if ($('html').has(lastFocused).length) {
                let lastLink = findLastLink(lastFocused);
                lastLink.focus();
              }
              else if (lastHovers && lastHovers.length) {
                let lastLink = findLastLink(lastHovers);
                lastLink.focus();
              }


          }).modal({backdrop: 'static', keyboard: false});
      }catch(e){
          $(modalToShow).show();
      }
      $(modalToShow).css('overflow-y', 'auto');
  };

  /**
   * @type function
   * @name {{setActiveTab}}
   * @nameTags Show| Tab| Exibir| Mostrar | Ativar |  Aba
   * @platform W
   * @description {{setActiveTablDesc}}
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @multilayer true
   */
  this.cronapi.screen.setActiveTab = function(/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id) {
    this.cronapi.$scope.safeApply( function(){
      if( $('#'+id).attr('data-target') === undefined){
        $( '[data-target="#'+ id + '"]' ).tab('show');
      }
      else{
        $('#'+id).tab('show');
      }

    });
  };

  /**
   * @type function
   * @name {{hideModal}}
   * @nameTags Hide| Modal| Esconder | Fechar
   * @description {{hideModalDesc}}
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @multilayer true
   */
  this.cronapi.screen.hideModal = function(/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id) {
    try{
      $('#'+id).modal('hide');
    }catch(e){
      $('#'+id).hide();
    }
  };


  /**
   * @type function
   * @name {{showMobileModal}}
   * @nameTags Show| Modal| Exibir| Mostrar
   * @description {{showMobileModalDesc}}
   * @platform M
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @multilayer true
   */
  this.cronapi.screen.showIonicModal = function(/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id) {
    if($('#'+id).data('cronapp-modal') ) $('#'+id).data('cronapp-modal').remove();
    this.cronapi.$scope.$ionicModal.fromTemplateUrl(id, {
      scope: this.cronapi.$scope,
      animation: 'slide-in-up'
    }).then(function(modal){
      $('#'+id).data('cronapp-modal', modal);
      modal.show();
    })
  };


  /**
   * @type function
   * @name {{hideMobileModal}}
   * @nameTags Hide| Modal| Esconder | Fechar
   * @description {{hideMobileModalDesc}}
   * @platform M
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @multilayer true
   */
  this.cronapi.screen.hideIonicModal = function(/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id) {
    if($('#'+id).data('cronapp-modal')) {
      var modal = $('#'+id).data('cronapp-modal');
      modal.remove();
      $('#'+id).data('cronapp-modal', null);
    }
  };

  /**
   * @type function
   * @name {{isShownIonicModal}}
   * @nameTags isShown| Modal| Exibido
   * @description {{isShownIonicModallDesc}}
   * @platform M
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @returns {ObjectType.BOOLEAN}
   */
  this.cronapi.screen.isShownIonicModal = function(/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id) {
    if($('#'+id).data('cronapp-modal')) {
      var modal = $('#'+id).data('cronapp-modal');
      return modal.isShown();
    }
    return false;
  };

  /**
   * @type function
   * @name {{showLoading}}
   * @nameTags Show| Loading| Exibir | Carregamento
   * @description {{showLoadingDesc}}
   * @platform M
   */
  this.cronapi.screen.showLoading = function() {
    this.cronapi.$scope.$ionicLoading.show({
      content : 'Loading',
      animation : 'fade-in',
      showBackdrop : true,
      maxWidth : 200,
      showDelay : 0
    });
  };

  /**
   * @type function
   * @name {{hideLoading}}
   * @nameTags Hide| Loading| Esconder | Carregamento
   * @description {{hideLoadingDesc}}
   * @platform M
   */
  this.cronapi.screen.hide = function() {
    this.cronapi.$scope.$ionicLoading.hide();
  };

  /**
   * @type function
   * @name {{getHostapp}}
   * @nameTags Hostapp
   * @description {{getHostappDesc}}
   * @platform M
   * @returns {ObjectType.String}
   */
  this.cronapi.screen.getHostapp = function() {
    return window.hostApp;
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

      let $scope = undefined;
      if (window.cordova) {
          $scope = this.cronapi.$scope;
      } else {
          let injector = window.angular.element('body').injector();
          $scope = injector.get('$rootScope');
      }

      let waitAngularReady = () => {
          if ($scope.$$phase !== '$apply' && $scope.$$phase !== '$digest') {
              if ($('#'+id).data("kendoComboBox")) {
                  $('#'+id).data("kendoComboBox").enable(false);
              } else if ($('#'+id).parent().parent().find('input.cronSelect[data-role=combobox]').data('kendoComboBox')) {
                  $('#'+id).parent().parent().find('input.cronSelect[data-role=combobox]').data('kendoComboBox').enable(false);
              } else if ($('#'+id).data("kendoDropDownList")) {
                  $('#'+id).data("kendoDropDownList").enable(false);
              } else if ($('#'+id).find('[data-role=grid]').data('kendoGrid')) {
                  this.cronapi.internal.coverElement($('#'+id));
              } else {
                  $.each( $('#'+id).find('*').addBack(), function(index, value){ $(value).prop('disabled',true); });
              }
          } else {
              setTimeout( () => waitAngularReady(), 200);
          }
      };
      waitAngularReady();
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

      let $scope = undefined;
      if (window.cordova) {
          $scope = this.cronapi.$scope;
      } else {
          let injector = window.angular.element('body').injector();
          $scope = injector.get('$rootScope');
      }

      let waitAngularReady = () => {
          if ($scope.$$phase !== '$apply' && $scope.$$phase !== '$digest') {
              if($('#'+id).data("kendoComboBox")){
                  $('#'+id).data("kendoComboBox").enable(true);
              } else if ($('#'+id).parent().parent().find('input.cronSelect[data-role=combobox]').data('kendoComboBox')) {
                  $('#'+id).parent().parent().find('input.cronSelect[data-role=combobox]').data('kendoComboBox').enable(true);
              } else if ($('#'+id).data("kendoDropDownList")) {
                  $('#'+id).data("kendoDropDownList").enable(true);
              } else if ($('#'+id).find('[data-role=grid]').data('kendoGrid')) {
                  this.cronapi.internal.discoverElement($('#'+id));
              } else {
                  $.each( $('#'+id).find('*').addBack(), function(index, value){ $(value).prop('disabled',false); });
              }
          } else {
              setTimeout( () => waitAngularReady(), 200);
          }
      };
      waitAngularReady();
  };

  /**
   * @type function
   * @name {{focusComponent}}
   * @nameTags focus
   * @description {{focusComponentDesc}}*
   * @multilayer true
   */
  this.cronapi.screen.focusComponent = function(/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id) {
    this.cronapi.$scope.safeApply( function() {
      if( tinyMCE && tinyMCE.get(id) !== undefined) {
        tinyMCE.get(id).focus();
      }else{
        $('#'+id).find('*').addBack().focus();
      }
    });
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
   * @type function
   * @name {{changeContent}}
   * @nameTags change|content|conteudo|moficiar
   * @description {{changeContentDesc}}
   * @param {ObjectType.STRING} id {{idsFromScreen}}
   * @param {ObjectType.STRING} content {{content}}
     * @param {ObjectType.BOOLEAN} compile {{compile}}
   * @multilayer true
   */
    this.cronapi.screen.changeContent = function(/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id , /** @type {ObjectType.STRING} */ content, /** @type {ObjectType.BOOLEAN} @blockType util_dropdown @keys false|true @values {{false}}|{{true}}*/ compile) {
    $('#'+id).html(content);
      if(compile === true || compile === 'true'){
        var $injector = angular.injector(['ng']);
        var that = this;
        $injector.invoke(['$compile', function($compile) {
          $compile(document.querySelector('#'+id))(that.cronapi.$scope);
        }]);
      }
  };

  /**
   * @type function
   * @name {{logoutName}}
   * @nameTags logout
   * @description {{logoutDescription}}
   * @multilayer true
   */
  this.cronapi.screen.logout = function() {
    if(this.cronapi.$scope.logout != undefined)
      this.cronapi.$scope.logout();
  };

  /**
   * @type function
   * @name {{refreshDatasource}}
   * @nameTags refresh|datasource|atualizar|fonte
   * @description {{refreshDatasourceDescription}}
   * @param {ObjectType.STRING} datasource {{datasource}}
   * @multilayer true
   */
  this.cronapi.screen.refreshDatasource = function(/** @type {ObjectType.OBJECT} @blockType datasource_from_screen*/ datasource , /** @type {ObjectType.BOOLEAN} @description {{keepFilters}} @blockType util_dropdown @keys true|false @values {{true}}|{{false}}  */  keepFilters ) {
    if(keepFilters == true || keepFilters == 'true' ){
      this[datasource].search(this[datasource].terms , this[datasource].caseInsensitive);
    }else
      this[datasource].search("", this[datasource].caseInsensitive);
  };



    /**
     * @type function
     * @name {{loadMoreName}}
     * @nameTags load|datasource|next|page
     * @description {{loadMoreNameDescription}}
     * @param {ObjectType.STRING} datasource {{datasource}}
     */
    this.cronapi.screen.loadMore = function(/** @type {ObjectType.OBJECT} @blockType datasource_from_screen*/ datasource) {
        getDatasource(datasource).$apply( function() { getDatasource(datasource).nextPage();});
    };


    /**
     * @type function
     * @name {{hasNextPageName}}
     * @nameTags load|datasource|next|page
     * @description {hasNextPageDescription}}
     * @param {ObjectType.STRING} datasource {{datasource}}
     * @returns {ObjectType.BOOLEAN}
     */
    this.cronapi.screen.hasNextPage = function(/** @type {ObjectType.OBJECT} @blockType datasource_from_screen*/ datasource) {
        return getDatasource(datasource).hasNextPage();
    };

    /**
     * @type function
     * @name {{datasourceLoadName}}
     * @nameTags load|datasource
     * @description {{datasourceLoadDescription}}
     * @param {ObjectType.STRING} datasource {{datasource}}
     * @multilayer true
     */
    this.cronapi.screen.load = function(/** @type {ObjectType.OBJECT} @blockType datasource_from_screen*/ datasource) {
        getDatasource(datasource).fetch({ params: {} }, undefined, undefined, { origin: "button" });
    };

  /**
   * @category CategoryType.DATETIME
   * @categoryTags Date|Datetime|Data|Hora
   */
  this.cronapi.dateTime = {};

  this.cronapi.dateTime.formats = function() {
    var formats = [];

    if (this.cronapi.$translate.use() == 'pt_br')
      formats = ['DD/MM/YYYY HH:mm:ss', 'DD/MM/YYYY', 'DD-MM-YYYY HH:mm:ss', 'DD-MM-YYYY'];
    else
      formats = ['MM/DD/YYYY HH:mm:ss', 'MM/DD/YYYY', 'MM-DD-YYYY HH:mm:ss', 'MM-DD-YYYY'];

    formats.push('YYYY-MM-DDTHH:mm:ss');
    formats.push('HH:mm:ss')
    formats.push('MMMM');

    return formats;
  };

  this.cronapi.dateTime.getMomentObj = function(value) {
    var currentMoment = moment;

    if (value  instanceof moment) {
      return value;
    }
    else if (value instanceof Date) {
      return currentMoment(value).utcOffset(window.timeZoneOffset);
    }
    else  {
      if (value) {
        let dateString = new RegExp("\/Date\((.*?)\)/");
        let regexExecution = dateString.exec(value);
        if (regexExecution && regexExecution.length > 1) {
          let date = eval(`new Date${regexExecution[1]}`);
          return currentMoment(date).utcOffset(window.timeZoneOffset);
        }
      }

      var formats = this.cronapi.dateTime.formats();
      var momentObj = null;
      for (var ix in formats) {
        momentObj = currentMoment(value, formats[ix]);
        if (momentObj.isValid())
          break;
      }
      return momentObj;
    }
  };

  /**
   * @type function
   * @name {{getSecondFromDate}}
   * @nameTags getSecond
   * @description {{functionToGetSecondFromDate}}
   * @param {ObjectType.DATETIME} value {{ObjectType.DATETIME}}
   * @returns {ObjectType.LONG}
   */
  this.cronapi.dateTime.getSecond = function(value) {
    var date = this.cronapi.dateTime.getMomentObj(value);
    if (date)
      return date.get('second');
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
    var date = this.cronapi.dateTime.getMomentObj(value);
    if (date)
      return date.get('minute');
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
    var date = this.cronapi.dateTime.getMomentObj(value);
    if (date)
      return date.get('hour');
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
    var date = this.cronapi.dateTime.getMomentObj(value);
    if (date)
      return date.get('year');
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
    var date = this.cronapi.dateTime.getMomentObj(value);
    if (date)
      return date.get('month') + 1;
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
    var date = this.cronapi.dateTime.getMomentObj(value);
    if (date)
      return date.get('date');
    return 0;
  };

  /**
   * @type function
   * @name {{getSecondsBetweenDates}}
   * @nameTags getSecondsBetweenDates|getSecondsDiffDate|diffDatesSeconds
   * @description {{functionToGetSecondsBetweenDates}}
   * @param {ObjectType.DATETIME} date {{largerDateToBeSubtracted}}
   * @param {ObjectType.DATETIME} date2 {{smallerDateToBeSubtracted}}
   * @returns {ObjectType.LONG}
   */
  this.cronapi.dateTime.getSecondsBetweenDates = function(date, date2) {
    var dateVar = this.cronapi.dateTime.getMomentObj(date);
    var date2Var = this.cronapi.dateTime.getMomentObj(date2);
    return dateVar.diff(date2Var, 'seconds');
  };

  /**
   * @type function
   * @name {{getMinutesBetweenDates}}
   * @nameTags getMinutesBetweenDates|getMinutesDiffDate|diffDatesMinutes
   * @description {{functionToGetMinutesBetweenDates}}
   * @param {ObjectType.DATETIME} date {{largerDateToBeSubtracted}}
   * @param {ObjectType.DATETIME} date2 {{smallerDateToBeSubtracted}}
   * @returns {ObjectType.LONG}
   */
  this.cronapi.dateTime.getMinutesBetweenDates = function(date, date2) {
    var dateVar = this.cronapi.dateTime.getMomentObj(date);
    var date2Var = this.cronapi.dateTime.getMomentObj(date2);
    return dateVar.diff(date2Var, 'minutes');
  };

  /**
   * @type function
   * @name {{getHoursBetweenDates}}
   * @nameTags getHoursBetweenDates|getHoursDiffDate|diffDatesHours
   * @description {{functionToGetHoursBetweenDates}}
   * @param {ObjectType.DATETIME} date {{largerDateToBeSubtracted}}
   * @param {ObjectType.DATETIME} date2 {{smallerDateToBeSubtracted}}
   * @returns {ObjectType.LONG}
   */
  this.cronapi.dateTime.getHoursBetweenDates = function(date, date2) {
    var dateVar = this.cronapi.dateTime.getMomentObj(date);
    var date2Var = this.cronapi.dateTime.getMomentObj(date2);
    return dateVar.diff(date2Var, 'hours');
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
    var dateVar = this.cronapi.dateTime.getMomentObj(date);
    var date2Var = this.cronapi.dateTime.getMomentObj(date2);
    return dateVar.diff(date2Var, 'days');
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
    var dateVar = this.cronapi.dateTime.getMomentObj(date);
    var date2Var = this.cronapi.dateTime.getMomentObj(date2);
    return dateVar.diff(date2Var, 'months');
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
    var dateVar = this.cronapi.dateTime.getMomentObj(date);
    var date2Var = this.cronapi.dateTime.getMomentObj(date2);
    return dateVar.diff(date2Var, 'years');
  };

  /**
   * @type function
   * @name {{incSecond}}
   * @nameTags incSecond|increaseSecond
   * @description {{functionToIncSecond}}
   * @param {ObjectType.DATETIME} date {{ObjectType.DATETIME}}
   * @param {ObjectType.LONG} second {{secondsToIncrement}}
   * @returns {ObjectType.DATETIME}
   */
  this.cronapi.dateTime.incSecond = function(date, second) {
    var dateVar = this.cronapi.dateTime.getMomentObj(date);
    return dateVar.add('seconds', second).toDate();
  };

  /**
   * @type function
   * @name {{incMinute}}
   * @nameTags incMinute|increaseMinute
   * @description {{functionToIncMinute}}
   * @param {ObjectType.DATETIME} date {{ObjectType.DATETIME}}
   * @param {ObjectType.LONG} minute {{minutesToIncrement}}
   * @returns {ObjectType.DATETIME}
   */
  this.cronapi.dateTime.incMinute = function(date, minute) {
    var dateVar = this.cronapi.dateTime.getMomentObj(date);
    return dateVar.add('minutes', minute).toDate();
  };

  /**
   * @type function
   * @name {{incHour}}
   * @nameTags incHour|increaseHour
   * @description {{functionToIncHour}}
   * @param {ObjectType.DATETIME} date {{ObjectType.DATETIME}}
   * @param {ObjectType.LONG} hour {{hoursToIncrement}}
   * @returns {ObjectType.DATETIME}
   */
  this.cronapi.dateTime.incHour = function(date, hour) {
    var dateVar = this.cronapi.dateTime.getMomentObj(date);
    return dateVar.add('hours', hour).toDate();
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
    var dateVar = this.cronapi.dateTime.getMomentObj(date);
    return dateVar.add('days', day).toDate();
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
    var dateVar = this.cronapi.dateTime.getMomentObj(date);
    return dateVar.add('months', month).toDate();
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
    var dateVar = this.cronapi.dateTime.getMomentObj(date);
    return dateVar.add('years', year).toDate();
  };

  /**
   * @type function
   * @name {{getNow}}
   * @nameTags getNow|now|getDate
   * @description {{functionToGetNow}}
   * @returns {ObjectType.DATETIME}
   */
  this.cronapi.dateTime.getNow = function() {
    var momentDate = moment();
    return momentDate.toDate();
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
    return this.cronapi.dateTime.getMomentObj(date).format(format);
  };

  /**
   * @type function
   * @name {{newDate}}
   * @nameTags newDate|createDate
   * @description {{functionToNewDate}}
   * @param {ObjectType.LONG} year {{year}}
   * @param {ObjectType.LONG} month {{month}}
   * @param {ObjectType.LONG} day {{day}}
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
    return this.cronapi.dateTime.getMomentObj(date).toDate();
  };

  this.cronapi.dateTime.updateDate = function(value, year, month, day, hour, minute, second, millisecond) {
    var date = this.cronapi.dateTime.getMomentObj(value).toDate();
    if (date && !isNaN(date.getTime())) {
      date.setYear(year);
      date.setMonth(month - 1);
      date.setDate(day);
      date.setHours(hour);
      date.setMinutes(minute);
      date.setSeconds(second);
      date.setMilliseconds(millisecond);
    }
    else{
      this.cronapi.screen.notify('error',this.cronapi.i18n.translate("InvalidDate",[  ]));
      return;
    }
    return this.cronapi.dateTime.getMomentObj(date).toDate();
  };

  /**
   * @type function
   * @name {{updateDate}}
   * @nameTags setDate|updateDate
   * @description {{functionToUpdateDate}}
   * @param {ObjectType.DATETIME} date {{ObjectType.DATETIME}}
   * @param {ObjectType.STRING} type {{attribute}}
   * @param {ObjectType.LONG} value {{value}}
   * @returns {ObjectType.DATETIME}
   */
  this.cronapi.dateTime.updateNewDate = function(date, /** @type {ObjectType.STRING} @description {{attribute}} @blockType util_dropdown @keys year|month|day|hour|minute|second|millisecond  @values {{year}}|{{month}}|{{day}}|{{hour}}|{{minute}}|{{second}}|{{millisecond}}  */ type, value ) {
    var updatedDate = this.cronapi.dateTime.getMomentObj(date).toDate();
    if (updatedDate && !isNaN(updatedDate.getTime())) {
      switch(type){
        case "year":
          updatedDate.setYear(value);
          break;
        case "month":
          updatedDate.setMonth(value - 1);
          break;
        case "day":
          updatedDate.setDate(value);
          break;
        case "hour":
          updatedDate.setHours(value);
          break;
        case "minute":
          updatedDate.setMinutes(value);
          break;
        case "second":
          updatedDate.setSeconds(value);
          break;
        case "millisecond":
          updatedDate.setMilliseconds(value);
          break;
      }
    }
    else{
      this.cronapi.screen.notify('error',this.cronapi.i18n.translate("InvalidDate",[  ]));
      return;
    }
    return this.cronapi.dateTime.getMomentObj(updatedDate).toDate();
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
   * @type function
   * @name {{newline}}
   * @description {{newlineDescription}}
   * @returns {ObjectType.STRING}
   */
  this.cronapi.text.newline = function() {
    return "\n";
  }

  /**
   * @type function
   * @name {{replaceName}}
   * @nameTags text|replace
   * @description {{replaceDescription}}
   * @param {ObjectType.STRING} textReplace {{textReplaceElement}}
   * @param {ObjectType.STRING} textReplaceTargetRegex {{textReplaceTargetRegexElement}}
   * @param {ObjectType.STRING} typeReplace {{typeReplaceElement}}
   * @param {ObjectType.STRING} textReplaceReplacement {{textReplaceReplacementElement}}
   * @returns {ObjectType.STRING}
   */
  this.cronapi.text.replaceAll = function(/** @type {ObjectType.STRING} @defaultValue Xmas.*/ textReplace, /** @type {ObjectType.STRING} @defaultValue X*/ textReplaceTargetRegex, /** @type {ObjectType.STRING} @description {{typeReplaceElement}} @defaultValue - @blockType util_dropdown @keys -|g|i|m|gi|gim|gm  @values {{-}}|{{g}}|{{i}}|{{m}}|{{gi}}|{{gim}}|{{gm}}  */ typeReplace, /** @type {ObjectType.STRING} @defaultValue Christmas*/ textReplaceReplacement){
    if (this.cronapi.logic.isNull(textReplace) || this.cronapi.logic.isNull(textReplaceTargetRegex) || this.cronapi.logic.isNull(textReplaceReplacement))
      return null;
    if (typeReplace !== '-')
      return textReplace.replace(new RegExp(textReplaceTargetRegex, typeReplace), textReplaceReplacement);
    return textReplace.replace(textReplaceTargetRegex, textReplaceReplacement);
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
    return (value === null || typeof value  == 'undefined'  || value == undefined);
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
    return (this.cronapi.logic.isNull(value) || this.cronapi.logic.isEmpty(value));
  }


  /**
   * @type function
   * @name {{}}
   * @nameTags typeOf
   * @description {{typeOfDescription}}
   * @returns {ObjectType.OBJECT}
   * @displayInline true
   */
  this.cronapi.logic.typeOf = function(/** @type {ObjectType.OBJECT} @description {{value}} */ value, /** @type {ObjectType.OBJECT} @description {{typeOf}} @blockType util_dropdown @keys string|number|undefined|object|function|array  @values {{string}}|{{number}}|{{undefined}}|{{object}}|{{function}}|{{array}}  */ type) {
    if(type==='array') return Array.isArray(value);
    if(type==='object' && Array.isArray(value)) return false;
    return (typeof(value) === type);
  }

  this.cronapi.i18n = {};

  this.cronapi.i18n.translate = function(value , params) {
    if (value) {
      var text = this.cronapi.$translate.instant(value);
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

  this.cronapi.internal.coverElement = function($element) {

    let cover = (id, obj) => {
      let width = obj.eq(0).outerWidth();
      let height = obj.eq(0).outerHeight();
      obj.each( function(index){
        let position = $(this).offset();
        $(`<div class='cover-${id}'>`).css({
          "width": width,
          "height": height,
          "position": "absolute",
          "top": position.top,
          "left": position.left,
          "z-index": 999999
        }).appendTo($("body"));
      });
    };

    let id = $element.attr('id');
    cover(id, $element);
  };

  this.cronapi.internal.discoverElement = function($element) {
    let id = $element.attr('id');
    $(`.cover-${id}`).remove();
  };

  this.cronapi.internal.focusFormInput = function () {
    let $firstForm = $($('form')[0]);
    let $firstInput = $($firstForm.find('input')[0]);
    if ($firstInput && $firstInput.length) {
      let waitBecomeVisible = setInterval(()=> {
        if ($firstInput.is(':visible')) {
          $firstInput.focus();
          clearInterval(waitBecomeVisible);
        }
      }, 100);
    }
  };

  this.cronapi.forceCloseAllModal = function() {
    var modals = $('.modal.fade.in');
    if (modals) {
      modals.each((idx, obj) => this.cronapi.screen.hideModal(obj.id));
    }
    $('.modal-backdrop.fade.in').remove();
  };

  this.cronapi.internal.setFile = function(field, file) {
    this.cronapi.internal.fileToBase64(file, function(base64) { this.cronapi.screen.changeValueOfField(field, base64); }.bind(this));
  };

  this.cronapi.internal.fileToBase64 = function(file, cb) {
    var fileReader = new FileReader();
    fileReader.readAsDataURL(file);
    fileReader.onload = function(e) {
      var base64Data = e.target.result.substr(e.target.result.indexOf('base64,') + 'base64,'.length);
      cb(base64Data);
    };
  };

  this.cronapi.internal.startCamera = function(field, quality, allowEdit, targetWidth, targetHeight) {
    //verify if user is on Browser or not
    if(window.cordova && window.cordova.platformId && window.cordova.platformId !== 'browser') {
      // If in mobile devices use native camera cordova plugin
      var that = this;
      navigator.camera.getPicture(function (result) {
        that.cronapi.screen.changeValueOfField(field, result);
      }, function (error) {
        console.error(error);
        that.cronapi.$scope.Notification.error(message);
      }, {
        quality: parseInt(quality), //Mobile images are very big to be stored into database, so reducing their quality (same as whatsapp images) improve performance and reduce db size
        destinationType: Camera.DestinationType.DATA_URL,
        encodingType: Camera.EncodingType.JPEG,
        correctOrientation: true,
        allowEdit: (allowEdit == 'true'),
        targetWidth: parseInt(targetWidth),
        targetHeight: parseInt(targetHeight)
      });
    }else{
      var cameraContainer =   '<div class="camera-container" style="margin-left:-$marginleft$;margin-top:-$margintop$">\
                                      <button class="btn btn-success button button-balanced" id="cronapiVideoCaptureOk" style="position: absolute; z-index: 999999999;">\
                                          <span class="glyphicon glyphicon-ok icon ion-checkmark-round"></span>\
                                          <span class="sr-only">{{"Upload.camera" | translate}}</span>\
                                      </button>\
                                      <button class="btn btn-danger button button-assertive button-cancel-capture" id="cronapiVideoCaptureCancel" style="position: absolute; margin-left: 42px; z-index: 999999999;">\
                                          <span class="glyphicon glyphicon-remove icon ion-android-close"></span>\
                                          <span class="sr-only">{{"Cancel" | translate}}</span>\
                                      </button>\
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
          }.bind(this));

          cronapiVideoCapture.find('#cronapiVideoCaptureOk').on('click',function() {
            this.cronapi.internal.captureFromCamera(field, res.width, res.height);
            if (streaming!= null && streaming.getTracks().length > 0)
              streaming.getTracks()[0].stop();
            $(cronapiVideoCapture).remove();
          }.bind(this));

          videoDOM.srcObject = stream;
          videoDOM.onloadedmetadata = function(e) {
            videoDOM.play();
          };
        }.bind(this));
      }
    }
  };

  this.cronapi.internal.downloadFileEntityMobile = function(datasource, field, indexData, fileInfo) {

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
      var finalUrl = this.cronapi.internal.getAddressWithHostApp('/api/cronapi/filePreview/');
      window.open(finalUrl + tempJsonFileUploaded.path, '_system');
    }
    else if (valueContent.startsWith('https://') || valueContent.startsWith('http://')) {
      window.open(valueContent, '_system');
    }
    else {
      if (datasource.isOData()) {
        cronapi.internal.makeDownloadFromBytes(valueContent, fileInfo);
      }
      else {
        var url = '/api/cronapi/downloadFile';
        var splited = datasource.entity.split('/');

        var entity = splited[splited.length - 1];
        if (entity.indexOf(":") > -1) {
          //Siginifica que é relacionamento, pega a entidade do relacionamento
          var entityRelation = '';
          var splitedDomainBase = splited[3].split('.');
          for (var i = 0; i < splitedDomainBase.length - 1; i++)
            entityRelation += splitedDomainBase[i] + '.';
          var entityRelationSplited = entity.split(':');
          entity = entityRelation + entityRelationSplited[entityRelationSplited.length - 1];
        }
        url += '/' + entity;
        url += '/' + field;
        var object = itemActive;
        var ids = datasource.getKeyValues(object);
        var currentIdxId = 0;
        for (var attr in ids) {
          if (currentIdxId == 0)
            url = url + '/' + object[attr];
          else
            url = url + ':' + object[attr];
          currentIdxId++;
        }
        var finalUrl = this.cronapi.internal.getAddressWithHostApp(url);
        window.open(finalUrl, '_system');
      }
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
    this.cronapi.screen.changeValueOfField(field, base64);
  };

  this.cronapi.internal.castBinaryStringToByteArray = function(binary_string) {
    var len = binary_string.length;
    var bytes = new Uint8Array( len );
    for (var i = 0; i < len; i++)        {
      bytes[i] = binary_string.charCodeAt(i);
    }
    return bytes;
  };

  this.cronapi.internal.castBase64ToByteArray = function(base64) {
    var binary_string = window.atob(base64);
    return this.cronapi.internal.castBinaryStringToByteArray(binary_string);
  };

  this.cronapi.internal.castByteArrayToString = function(bytes) {
    return String.fromCharCode.apply(null, new Uint16Array(bytes));
  };

  this.cronapi.internal.generatePreviewDescriptionByte = function(data, fileInfo) {
    var json;
    let fileInfoContent = eval(fileInfo);
    try {
      if (fileInfoContent) {
        json = JSON.parse(fileInfoContent);
      }
      else {
        //Verificando se é JSON Uploaded file
        json = JSON.parse(data);
      }
    }
    catch (e) {
      try {
        //Tenta pegar do header
        json = JSON.parse(window.atob(data));
      }
      catch (e) {
        if (data && data.match(/__odataFile_/g)) {
          var file = eval(data);
          json = cronapi.internal.getJsonDescriptionFromFile(file);
        }
        //Verifica se é url
        else if (data && (data.startsWith('http://') || data.startsWith('https://'))) {
          json = {};
          var urlSplited = data.split('/');
          var fullName = urlSplited[urlSplited.length - 1].replace('?dl=0','');
          var fullNameSplited = fullName.split('.')
          var extension = '.' + fullNameSplited[fullNameSplited.length - 1];
          json.fileExtension = extension;
          json.name = fullName.replace(extension, '');
          json.contentType = 'file/'+extension.replace('.','');
        }
        else if (data && this.cronapi.internal.isBase64(data)) {
          var fileName = 'download';
          var fileExtesion = this.cronapi.internal.getExtensionBase64(data);
          var contentType = this.cronapi.internal.getContentTypeFromExtension(fileExtesion);
          fileName += fileExtesion;
          json = {};
          json.fileExtension = fileExtesion;
          json.name = fileName;
          json.contentType = contentType;
        }
      }
    }
    if (json) {
      if (json.name.length > 25)
        json.name = json.name.substr(0,22)+'...';

      var result = (this.cronapi.$translate.use() == 'pt_br' ? "<b>Nome:</b> <br/>" : "<b>Name:</b> <br/>") + (json.contentType !== undefined ? json.name +"<br/>" : "");
      result += json.contentType !== undefined ? "<b>Content-Type:</b> <br/>" + json.contentType +"<br/>" : "";
      result += json.fileExtension !== "" ? this.cronapi.$translate.use() == 'pt_br' ? "<b>Extensão:</b> <br/>" + json.fileExtension +"<br/>" : "<b>Extension:</b> <br/>" + json.fileExtension +"<br/>" : "";
      return result;
    }
  };

  this.cronapi.internal.getContentTypeFromExtension = function(extension) {
    if (extension) {
      switch (extension.toLowerCase()) {
        case '.png':
          return 'image/png';
        case '.jpg':
          return 'image/jpeg';
        case '.mp4':
          return 'video/mp4';
        case '.pdf':
          return 'application/pdf';
        case '.ico':
          return 'image/vnd.microsoft.icon';
        case '.rar':
          return 'application/x-rar-compressed';
        case '.rtf':
          return 'application/rtf';
        case '.txt':
          return 'text/plain';
        case '.zip':
          return 'application/zip';
        case '.srt':
          return 'text/srt';
        default:
          return 'unknown';
      }
    }
  };

  this.cronapi.internal.getExtensionBase64 = function(base64) {
    if (base64) {
      var data = base64.substr(0, 5);
      switch (data.toLocaleUpperCase())
      {
        case "IVBOR":
          return ".png";
        case "/9J/4":
          return ".jpg";
        case "AAAAF":
          return ".mp4";
        case "JVBER":
          return ".pdf";
        case "AAABA":
          return ".ico";
        case "UMFYI":
          return ".rar";
        case "E1XYD":
          return ".rtf";
        case "U1PKC":
          return ".txt";
        case "UESDB":
          return ".zip";
        case "MQOWM":
        case "77U/M":
          return ".srt";
        default:
          return "";
      }
    }
    return "";
  };

  this.cronapi.internal.isBase64 = function(str) {
    try {
      return btoa(atob(str)) == str;
    } catch (err) {
      return false;
    }
  };

  this.cronapi.internal.downloadUrl = function(url, fileName) {
    let link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute("download", fileName);
    let event = document.createEvent('MouseEvents');
    event.initMouseEvent('click', true, true, window, 1, 0, 0, 0, 0, false, false, false, false, 0, null);
    link.dispatchEvent(event);
  };

  this.cronapi.internal.makeDownloadFromBytes = function(valueContent, fileInfo) {
    var urlCreator = window.URL || window.webkitURL || window.mozURL || window.msURL;
    var bytesOrFileInput;
    var fileName = 'download';

    if (valueContent.match(/__odataFile_/g)) {
      bytesOrFileInput = eval(valueContent);
      fileName = bytesOrFileInput.name
    }
    else {
      fileName += cronapi.internal.getExtensionBase64(valueContent);
      try {
        valueContent = window.atob(valueContent);
      } catch (e) {
        //NoCommand
      }
      bytesOrFileInput = cronapi.internal.castBinaryStringToByteArray(valueContent);
    }
    let fileInfoDescription = eval(fileInfo);
    if (fileInfoDescription) {
      let fileInfoJson = JSON.parse(fileInfoDescription);
      fileName = fileInfoJson.name;
    }
    var url = urlCreator.createObjectURL(new Blob([bytesOrFileInput],{type: 'application/octet-stream'}));
    cronapi.internal.downloadUrl(url, fileName);
  };

  this.cronapi.internal.downloadFileEntity = function(datasource, field, indexData, fileInfo) {

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
    else if (valueContent.startsWith('https://') || valueContent.startsWith('http://')) {
      window.open(valueContent);
    }
    else {

      if (datasource.isOData()) {
        cronapi.internal.makeDownloadFromBytes(valueContent, fileInfo);
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
        var _u = JSON.parse(localStorage.getItem('_u')) || {};
        var object = itemActive;

        var finalUrl = this.cronapi.internal.getAddressWithHostApp(url);

        this.$promise = this.cronapi.$scope.$http({
          method: 'POST',
          url: finalUrl,
          data: (object) ? JSON.stringify(object) : null,
          responseType: 'blob',
          headers: {
            'Content-Type': 'application/json',
            'X-AUTH-TOKEN': _u.token,
          }
        }).success(function(data, status, headers, config) {
          headers = headers();
          var filename = headers['x-filename'] || 'download.bin';
          var urlCreator = window.URL || window.webkitURL || window.mozURL || window.msURL;
          try
          {
            var url = urlCreator.createObjectURL(data);
            cronapi.internal.downloadUrl(url, filename);
          } catch(ex) {
            console.log('Error downloading file');
            console.log(ex);
          }
        }.bind(this)).error(function(data, status, headers, config) {
          console.log('Error downloading file');
        }.bind(this));
      }

    }

  };

  this.cronapi.internal.uploadFileAjax = function(field, file, progressId) {
    var uploadUrl = '/api/cronapi/uploadFile';
    var formData = new FormData();
    formData.append("file", file);
    var _u = JSON.parse(localStorage.getItem('_u')) || {};

    var finalUrl = this.cronapi.internal.getAddressWithHostApp(uploadUrl);

    this.$promise = this.cronapi.$scope.$http({
      method: 'POST',
      url: finalUrl,
      data: formData,
      headers:  {
        'Content-Type': undefined,
        'X-AUTH-TOKEN': _u.token
      },
      onProgress: function(event) {
        if (event.lengthComputable) {
          var complete = (event.loaded / event.total * 100 | 0);
          var $progressId = $('#'+progressId);
          if ($progressId.length > 0) {
            if ($progressId.data('type') == 'bootstrapProgress') {
              if (complete < 100) {
                $progressId.show();
                $progressId.find('.progress-bar').css('width', complete+'%');
              }
              else {
                $progressId.hide();
                $progressId.find('.progress-bar').css('width', '0%');
              }
            }
            else {
              var progress = document.getElementById(progressId);
              progress.value = progress.innerHTML = complete;
            }
          }

        }
      }
    }).success(function(data, status, headers, config) {
      this.cronapi.screen.changeValueOfField(field, data.jsonString);
    }.bind(this)).error(function(data, status, headers, config) {
      alert('Error uploading file');
    }.bind(this));

  };

  this.cronapi.internal.getFieldFromActiveString = function(rawActive) {
    var regexForField = /.active.([a-zA-Z0-9_-]*)/g;
    var groupField = regexForField.exec(rawActive);
    var fieldName = groupField[1];
    return fieldName;
  };

  this.cronapi.internal.getJsonDescriptionFromFile = function(file) {
    let json = {};
    if (file) {
      let fullNameSplited = file.name.split('.');
      let extension = '.' + fullNameSplited[fullNameSplited.length - 1];
      json.fileExtension = extension;
      json.name = file.name;
      json.contentType = file.type || 'unknown';
    }
    return json;
  };
  
  this.cronapi.internal.uploadFile = function(field, file, progressId, fileInfo, errorFile) {

    if (errorFile && errorFile.length) {
      if (errorFile[0].$errorMessages && errorFile[0].$errorMessages.maxSize) {
        let error = this.cronapi.$translate.instant("maxFileSize");
        showErrorNotification.bind(this)(`${error} ${errorFile[0].$errorParam}`);
      }
    }

    if (!file)
      return;

    var regexForDatasource = /(.*?).active./g;
    var groupDatasource = regexForDatasource.exec(field);
    //Verificar se é campo de um datasource
    if (groupDatasource) {
      var datasource = eval(groupDatasource[1]);
      if (datasource.isOData()) {
        let fieldName = cronapi.internal.getFieldFromActiveString(field);
        let schemaField = datasource.getFieldSchema(fieldName);
        if (schemaField && schemaField.type == 'Binary') {
          datasource.active['__odataFile_' + fieldName] = file;
          datasource.active[fieldName] = datasource.name + '.active.__odataFile_' +  fieldName;
          if (fileInfo) {
            let json = cronapi.internal.getJsonDescriptionFromFile(file);
            let fieldFileInfo = cronapi.internal.getFieldFromActiveString(fileInfo);
            datasource.active[fieldFileInfo] = JSON.stringify(json);
          }
          return;
        }
      }
    }
    this.cronapi.internal.uploadFileAjax(field, file, progressId);
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

    var splited = property.split('.');
    if(splited.length > 1 ){
      var recursiva = function(object, params , idx) {
        if (!idx) idx = 0;
        if(object[params[idx]] === undefined)
          object[params[idx]] = {};
        idx++;
        if (idx < params.length)
          return recursiva(object[params[idx -1]], params , idx);
        else return object[params[idx-1]];
      };
      return recursiva(object , splited , 0);
    }else{
      return object[property];
    }
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
    var splited = property.split('.');
    if(splited.length > 1 ){
      var recursiva = function(object, params,value , idx) {
        if (!idx) idx = 0;
        if(object[params[idx]] === undefined)
          object[params[idx]] = {};
        idx++;
        if (idx < params.length)
          recursiva(object[params[idx -1]], params, value , idx);
        else object[params[idx-1]] = value;
      };
      recursiva(object , splited , value, 0);
    }else{
      object[property] = value;
    }
  };

  /**
   * @type function
   * @name {{createObject}}
   * @description {{createObjectDescription}}
   * @nameTags object
   * @param {ObjectType.STRING} string {{string}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.object.createObjectFromString = function(string) {
    return JSON.parse(string);
  };

   /**
   * @type function
   * @name {{createObjectLoginJson}}
   * @description {{createObjectLoginJsonDescription}}
   * @nameTags object
   * @param {ObjectType.STRING} string {{Login}}
   * @param {ObjectType.STRING} string {{Password}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.object.createObjectLoginFromString = function(login, password) {
    let json = {};
    json["username"] = login;
    json["password"] = password;
    return JSON.parse(JSON.stringify(json));
  };

  /**
   * @type function
   * @name {{serializeObject}}
   * @description {{serializeObjectDescription}}
   * @nameTags object
   * @param {ObjectType.STRING} string {{string}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.object.serializeObject = function(obj) {
    return JSON.stringify(obj);
  };

  /**
   * @type function
   * @name {{deleteProperty}}
   * @description {{deletePropertyDescription}}
   * @nameTags object
   * @param {ObjectType.OBJECT} object {{object}}
   * @param {ObjectType.STRING} key {{key}}
   */
  this.cronapi.object.deleteProperty = function(obj, key) {
    delete obj[key];
  };

  /**
   * @type function
   * @name {{createNewObject}}
   * @nameTags createNewObject
   * @description {{functionToCreateNewObject}}
   * @arbitraryParams true
   * @wizard procedures_createnewobject_callreturn
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.object.newObject = function() {
    var result = {};

    if (arguments && arguments.length > 0) {
      for (var i = 0; i < arguments.length; i++) {
        var param = arguments[i];
        if (param.name)
          result[param.name] = param.value;
      }
    }
    return result;
  };

  /**
   * @type function
   * @name {{getObjectField}}
   * @nameTags getObjectField
   * @description {{functionToGetObjectField}}
   * @param {ObjectType.OBJECT} obj {{obj}}
   * @param {ObjectType.STRING} field {{field}}
   * @wizard procedures_get_field
   */
  this.cronapi.object.getObjectField = function(/** @type {ObjectType.OBJECT} @blockType variables_get */ obj, /** @type {ObjectType.STRING} @blockType procedures_get_field_object */ field) {
    var result = undefined;
    if (obj && field)
      result = obj[field];
    return result;
  };

  /**
   * @category CategoryType.JSON
   * @categoryTags JSON|json
   */
  this.cronapi.json = {};

  /**
   * @type function
   * @name {{createObjectJson}}
   * @description {{createObjectJsonDescription}}
   * @nameTags object
   * @param {ObjectType.STRING} string {{string}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.json.createObjectFromString = function(string) {
    return this.cronapi.object.createObjectFromString(string);
  };

  /**
   * @type function
   * @name {{setProperty}}
   * @nameTags setProperty
   * @param {ObjectType.OBJECT} object {{json}}
   * @param {ObjectType.STRING} property {{property}}
   * @param {ObjectType.OBJECT} value {{value}}
   * @description {{setPropertyDescription}}
   * @returns {ObjectType.VOID}
   */
  this.cronapi.json.setProperty = function(object, property, value) {
    this.cronapi.object.setProperty(object, property, value)
  };

  /**
   * @type function
   * @name {{deleteProperty}}
   * @description {{deletePropertyDescription}}
   * @nameTags object
   * @param {ObjectType.OBJECT} object {{json}}
   * @param {ObjectType.STRING} key {{key}}
   */
  this.cronapi.json.deleteProperty = function(obj, key) {
    this.cronapi.object.deleteProperty(obj, key);
  };

  /**
   * @type function
   * @name {{getProperty}}
   * @nameTags getProperty
   * @param {ObjectType.OBJECT} object {{json}}
   * @param {ObjectType.STRING} property {{property}}
   * @description {{getPropertyDescription}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.json.getProperty = function(object, property) {
    return this.cronapi.object.getProperty(object, property);
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

  this.cronapi.cordova.device = {};

  /**
   *  @type function
   * @platform M
   * @name {{getFirebaseToken}}
   * @nameTags firebase|token|push|notification
   * @param {ObjectType.STATEMENTSENDER} success {{success}}
   * @param {ObjectType.STATEMENTSENDER} error {{error}}
   * @description {{getFirebaseTokenDescription}}
   * @returns {ObjectType.VOID}
   */
  this.cronapi.cordova.device.getFirebaseToken = function (success, error) {
    function onDeviceReady() {
      try {
        window.FirebasePlugin.getToken(this.cronapi.util.handleCallback(success), this.cronapi.util.handleCallback(error));
      } catch (e) {
        console.error(e);
        error(e);
      }
    }
    // The deviceready event fires once Cordova has fully loaded. Once the event fires, you can safely make calls to Cordova APIs.
    document.addEventListener("deviceready", onDeviceReady, false);
  };


  /**
   *  @type function
   * @platform M
   * @name {{getFirebaseNotificationData}}
   * @nameTags firebase|token|push|notification
   * @param {ObjectType.STATEMENTSENDER} success {{success}}
   * @param {ObjectType.STATEMENTSENDER} error {{error}}
   * @description {{getFirebaseNotificationDataDesc}}
   * @returns {ObjectType.VOID}
   */
  this.cronapi.cordova.device.getFirebaseNotificationData = function (success, error) {
    // Full documentation on https://github.com/dpa99c/cordova-plugin-firebasex
    // onNotificationOpen() renamed to onMessageReceived() on cordova-plugin-firebasex
    function onDeviceReady() {
      try {
        // Available on cronapi-firebase-push@2.0.0
        window.FirebasePlugin.onMessageReceived(function (notification) {
          success(notification);
        }, function (err) {
          error(err);
        });
      } catch (e) {
        console.warn(e);
      }

      // Keep old code if user is using deprecated cordova-plugin-firebase that still works on android
      try {
        // @deprecated
        // Available on cronapi-firebase-push@1.0.0
        window.FirebasePlugin.onNotificationOpen(function (notification) {
          success(notification);
        }, function (err) {
          error(err);
        });
      } catch (e) {
        console.warn(e);
      }
    }

    document.addEventListener("deviceready", onDeviceReady, false);
  };


  /**
   *  @type function
   * @platform M
   * @name {{getDeviceInfo}}
   * @nameTags device|dispositivo|info
   * @param {ObjectType.STRING} type {{type}}
   * @description {{getDeviceInfoDescription}}
   * @returns {ObjectType.STRING}
   */
  this.cronapi.cordova.device.getDeviceInfo = function( /** @type {ObjectType.STRING} @description {{type}} @blockType util_dropdown @keys uuid|model|platform|version|manufacturer|isVirtual|serial @values uuid|model|platform|version|manufacturer|isVirtual|serial  */ type){
    return window.device[type];
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
    return navigator.geolocation.watchPosition(success, error, { maximumAge: maximumAge, timeout: timeout, enableHighAccuracy: enableHighAccuracy });
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

  this.cronapi.cordova.camera.getPicture = function(/** @type {ObjectType.STATEMENTSENDER} @description {{success}} */ success, /** @type {ObjectType.STATEMENTSENDER} @description {{error}} */  error, /** @type {ObjectType.LONG} @description {{destinationType}} @blockType util_dropdown @keys 0|1|2 @values DATA_URL|FILE_URI|NATIVE_URI  */  destinationType, /** @type {ObjectType.LONG} @description {{pictureSourceType}} @blockType util_dropdown @keys 0|1|2 @values PHOTOLIBRARY|CAMERA|SAVEDPHOTOALBUM  */ pictureSourceType, /** @type {ObjectType.LONG} @description {{mediaType}} @blockType util_dropdown @keys 0|1|2 @values PICTURE|VIDEO|ALLMEDIA  */ mediaType, /** @type {ObjectType.BOOLEAN} @description {{allowEdit}} @blockType util_dropdown @keys false|true @values {{false}}|{{true}}  */ allowEdit) {
    if(mediaType === undefined || mediaType === null) mediaType = 0 ;
    allowEdit = (allowEdit === true || allowEdit === 'true');
    navigator.camera.getPicture(this.cronapi.util.handleCallback(success), this.cronapi.util.handleCallback(error), { destinationType: Number(destinationType) , sourceType : Number(pictureSourceType) , mediaType: Number(mediaType) , allowEdit: allowEdit});
  };

  /**
   * @type function
   * @platform M
   * @name {{qrCodeScanner}}
   * @nameTags QRCODE|QR|BAR|Scanner|BARCODE
   * @param {ObjectType.STRING} format {{formatQRCode}}
   * @param {ObjectType.STRING} message {{messageQRCode}}
   * @description {{qrCodeScannerDescription}}
   * @returns {ObjectType.VOID}
   */
  this.cronapi.cordova.camera.qrCodeScanner = function(/** @type {ObjectType.STRING} @description {{formatQRCode}} @blockType util_dropdown @keys QR_CODE|DATA_MATRIX|UPC_A|UPC_E|EAN_8|EAN_13|CODE_39|CODE_128 @values QR_CODE|DATA_MATRIX|UPC_A|UPC_E|EAN_8|EAN_13|CODE_39|CODE_128  */  format,/** @type {ObjectType.STRING} @description {{messageQRCode}} */ message, /** @type {ObjectType.STATEMENTSENDER} @description {{success}} */ success, /** @type {ObjectType.STATEMENTSENDER} @description {{error}} */  error ) {
    cordova.plugins.barcodeScanner.scan(
        function (result) {
          success(result.text);
        },
        function (errorMsg) {
          if (errorMsg !== 'Scan is already in progress') {
            // Verification in order to avoid issue: https://github.com/phonegap/phonegap-plugin-barcodescanner/issues/660
            error(errorMsg);
          }
        },
        {
          preferFrontCamera : false,
          showFlipCameraButton : true,
          showTorchButton : true,
          saveHistory: true,
          prompt : message,
          resultDisplayDuration: 500,
          formats : format,
          orientation : "portrait",
          disableAnimations : true,
          disableSuccessBeep: false
        }
    );
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
      }.bind(this),this.cronapi.util.handleCallback(error));
    }.bind(this),this.cronapi.util.handleCallback(error));
  };

  /**
   * @type function
   * @platform M
   * @name {{readFile}}
   * @nameTags file|arquivo|readFile|lerarquivo
   * @description {{readFileDescription}}
   * @returns {ObjectType.VOID}
   */
  this.cronapi.cordova.file.readFile = function( /** @type {ObjectType.STRING} @description {{fileName}} */ fileName,  /** @type {ObjectType.STATEMENTSENDER} @description {{success}} */ success,  /** @type {ObjectType.STATEMENTSENDER} @description {{error}} */ error, /** @type {ObjectType.STRING} @description {{returnType}} @blockType util_dropdown @keys ARRAYBUFFER|TEXT|BINARYSTRING|DATAURL @values {{ARRAYBUFFER}}|{{TEXT}}|{{BINARYSTRING}}|{{DATAURL}}   */  returnType) {
    window.resolveLocalFileSystemURL(fileName, function (fileEntry) {
      fileEntry.file(function (file) {
        var reader = new FileReader();
        reader.onloadend = function (e) {
          success(this.result);
        };
        switch (returnType) {
          case 'ARRAYBUFFER': {
            reader.readAsArrayBuffer(file);
            break;
          }
          case 'TEXT': {
            reader.readAsText(file);
            break;
          }
          case 'BINARYSTRING': {
            reader.readAsBinaryString(file);
            break;
          }
          case 'DATAURL': {
            reader.readAsDataURL(file);
            break;
          }
          default: {
            reader.readAsText(file);
          }
        };
      }.bind(this),this.cronapi.util.handleCallback(error));
    }.bind(this),this.cronapi.util.handleCallback(error));
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
    const path = (dirEntry || getDirectory(0));
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
            }.bind(this),500);
          }
        }.bind(this), this.cronapi.util.handleCallback(error) );
      }.bind(this), this.cronapi.util.handleCallback(error) );
    }.bind(this), this.cronapi.util.handleCallback(error) );
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
      directoryEntry.getDirectory(dirChildrenName, { create: true }, function (childrenEntry) {
        if (success)
          success(childrenEntry);
      }.bind(this),this.cronapi.util.handleCallback(error));
    }.bind(this), this.cronapi.util.handleCallback(error));
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

  this.cronapi.cordova.database.DatabaseModule = class DatabaseModule {
    constructor(){
      this.DEFAULT_DATABASE_NAME = "cronappDB";
    }

    connect(dbName  = this.DEFAULT_DATABASE_NAME){
      let myOpenDatabaseMethod = window.openDatabase;
      // If in mobile environment use native sqlite
      if (window.sqlitePlugin) myOpenDatabaseMethod = window.sqlitePlugin.openDatabase;
      return myOpenDatabaseMethod(dbName, "1.0", dbName, 1000000);
    }

    async executeSQL(dbName  = this.DEFAULT_DATABASE_NAME, rawSQL = "", params = []){
      const dbInstance = this.connect(dbName);
      const extractMultipleSQLQueries = (command)=> command.trim().split(';').filter(str => str ? str : null);
      try{
        const splitedRawSQL = extractMultipleSQLQueries(rawSQL);
        return this.handleExecuteTransaction(dbInstance, splitedRawSQL, params);
      }catch(err){
        console.log(err);
        throw err;
      }
  }

  extractSQLResult(resultQuery){
    return resultQuery._array ?  resultQuery._array : resultQuery;
  }

  async handleExecuteSQL(transaction, raw, params){
    return new Promise( (resolve, reject) =>{
      transaction.executeSql(raw, params, (tx, resultSet) => {
        try{
          resolve(resultSet.rows);
        }catch(err){
          reject(err);
        }
      });
    });
  }

  async handleExecuteTransaction(dbInstance, splitedRawSQL, params){
    return new Promise((resultResolve, resultReject) => {
      try{
        dbInstance.transaction(async (transaction) =>{
        let partial;
        for (let raw of splitedRawSQL) {
          partial = await this.handleExecuteSQL(transaction, raw, params);       
        }
        resultResolve(this.extractSQLResult(partial));
      });
      }catch(err){
        resultReject(err);
      }
    })
    
  }
}


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
    const database = new this.cronapi.cordova.database.DatabaseModule();
    return database.connect(dbName);
  };

    /**
   * @type function
   * @platform M
   * @name {{executeSql}}
   * @nameTags executeSQL
   * @param {ObjectType.STRING} dbName {{dbName}}
   * @param {ObjectType.STRING} text SQL
   * @param {ObjectType.OBJECT} array {{arrayParams}}
   * @description {{executeSqlDescription}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.cordova.database.executeSQL = async function(dbName,rawSQL, params){
    let dbModule = new this.cronapi.cordova.database.DatabaseModule(); 
    return dbModule.executeSQL(dbName,rawSQL,params);
  };

  /**
   * @deprecated true
   * @platform M
   * @nameTags executesql
   * @param {ObjectType.STRING} dbName {{dbName}}
   * @param {ObjectType.STRING} text SQL
   * @param {ObjectType.OBJECT} array {{arrayParams}}
   * @description {{executeSqlDescription}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.cordova.database.executeSql = function(dbName,text, array, success , error){

    // exist DB
    var db = this.cronapi.cordova.database.openDatabase(dbName); // create DB

    // open transaction
    db.transaction(function(connect){
      // execute SQL
      connect.executeSql(text,array, function(a,b){success.call(this,Object.values(b.rows))}.bind(this));
    }.bind(this), function(e){
      // error
      console.log(e);
      error(e);
    }.bind(this));

  };


  /**
   * @deprecated true
   */
  this.cronapi.cordova.database.executeMultipleSql = function (dbName, text, success, error) {

    // exist DB
    var db = this.cronapi.cordova.database.openDatabase(dbName); // create DB
    let promises = [];
    let statements = text.split(';');

    for (let stmIdx in statements) {
      let statement = statements[stmIdx];
      if (!statement) { break; }
      let promise = new Promise((resolve, reject) => {
        db.transaction(function (txn) {
          txn.executeSql(statement.trim(), [], function (transaction, result) {
            resolve(result);
          }.bind(this),
            // callback de erro, função anônima que recebe um objeto SQLTransaction e um SQLError
            function (transaction, error) {
              reject(error)
            }.bind(this));
        }.bind(this));
      });

      promises.push(promise);
    }

    Promise.all(promises).then(function (values) {
      success(values);
    }.bind(this)).catch(function (e) {
      error(e);
    }.bind(this));
  }


  /**
   * @type function
   * @platform M
   * @name {{openInAppBrowser}}
   * @nameTags openInAppBrowser
   * @param {ObjectType.STRING} url {{url}}
   * @description {{openInAppBrowserDescription}}
   * @returns {ObjectType.VOID}
   */
  this.cronapi.cordova.database.openInAppBrowser = function(url) {
    if(cordova.InAppBrowser){
      cordova.InAppBrowser.open(url, '_blank', 'location=no');
    }
  };


  //Private variables and functions
  this.cronapi.internal.ptDate = function(varray) {
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

  this.cronapi.internal.enDate = function(varray) {
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

  this.cronapi.internal.parseBoolean = function(value) {
    if (!value)
      return false;
    if (typeof value == "boolean")
      return value;
    value = value.toString().toLowerCase().trim();
    return value == "1" || value == "true";
  };

  this.cronapi.internal.removeAccents = function(value) {
    var withAccents = 'áàãâäéèêëíìîïóòõôöúùûüçÁÀÃÂÄÉÈÊËÍÌÎÏÓÒÕÖÔÚÙÛÜÇ';
    var withoutAccents = 'aaaaaeeeeiiiiooooouuuucAAAAAEEEEIIIIOOOOOUUUUC';
    var newValue = '';
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

  this.cronapi.internal.arrayRemove = function(array, value) {
    var i = this.cronapi.internal.arrayIndexOf(array, value);
    if (i != -1) {
      array.splice(i, 1);
    }
  };

  this.cronapi.internal.arrayIndexOf = function(array, value) {
    var index = -1;
    $(array).each(function(idx) {
      if (value == this) {
        index = idx;
      }
    });
    return index;
  };

  this.cronapi.internal.replaceAll = function(str, value, newValue) {
    return str.toString().split(value).join(newValue);
  };

  this.cronapi.internal.getWindowHeight = function() {
    $(window).height();
  };

  this.cronapi.internal.getWindowWidth = function() {
    $(window).width();
  };

  /**
   * @type internal
   */
  this.cronapi.internal.getAddressWithHostApp = function(address) {
    var urlWithoutEndSlash = window.hostApp || "";
    urlWithoutEndSlash = urlWithoutEndSlash.endsWith('/') ? urlWithoutEndSlash.substr(0, urlWithoutEndSlash.length - 1): urlWithoutEndSlash;
    if (address) {
      var addressWithoutStartSlash = address.startsWith('/') ? address.substr(1) : address;
      urlWithoutEndSlash === "" ? urlWithoutEndSlash = addressWithoutStartSlash : urlWithoutEndSlash = urlWithoutEndSlash + '/' + addressWithoutStartSlash;
    }
    return urlWithoutEndSlash;
  };

  /**
   *
   *  URL encode / decode
   *  http://www.webtoolkit.info/
   *
   **/

  this.cronapi.internal.Url = {
    // public method for url encoding
    encode : function (string) {
      if (string)
        return escape(this.cronapi.internal.Url._utf8_encode(string));
      return '';
    },
    // public method for url decoding
    decode : function (string) {
      if (string)
        return this.cronapi.internal.Url._utf8_decode(unescape(string));
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

  this.cronapi.internal.stringToJs = function(str) {
    return (str + '').replace(/[\\"']/g, '\\$&').replace(/\u0000/g, '\\0');
  };

  this.cronapi.internal.getErrorMessage = function (data, message) {
    try {
      var json = null;
      if (typeof data === 'object') {
        if (data.data) {
          if (typeof data.data === 'object') {
            json = data.data;
          } else if (typeof data.data === 'string') {
            json = JSON.parse(data.data);
          }
        }
      } else if (typeof data === 'string') {
        json = JSON.parse(data);
      }
      if (json && json.error) {
        return json.error;
      }
    } catch(e) {
      //Abafa
    }

    return message;
  };

  /**
   * @type internal
   */
  this.cronapi.util.upload = function(id, description, filter, maxSize, multiple) {
    this.UploadService.upload({'description': description, 'id' : id, 'filter' : filter, 'maxSize': maxSize, 'multiple': multiple, 'scope': this});
  };

  /**
   * @type function
   * @name {{getBaseUrlName}}
   * @nameTags getBaseUrl
   * @description {{getBaseUrlDescription}}
   * @returns {ObjectType.STRING}
   */
  this.cronapi.util.getBaseUrl = function() {
    return window.location.origin;
  };

  /**
   * @category CategoryType.CHART
   * @categoryTags Gráfico|Chart
   */
  this.cronapi.chart = {};

  /**
   * @type function
   * @name {{createChartName}}
   * @nameTags chart|series|serie
   * @description {{createChartDescription}}
   * @arbitraryParams true
   */
  this.cronapi.chart.createChart = function(/** @type {ObjectType.OBJECT} @description {{createChartId}} @blockType ids_from_screen*/ chartId,  /** @type {ObjectType.STRING} @description {{createChartType}} @blockType util_dropdown @keys line|bar|horizontalBar|doughnut|pie|polarArea  @values line|bar|horizontalBar|doughnut|pie|polarArea  */ type, /** @type {ObjectType.LIST} @description {{createChartLegends}} */  chartLegends, /** @type {ObjectType.LIST} @description {{createChartOptions}} */ options, /** @type {ObjectType.LIST}  @description {{createChartSeries}}  */ series) {

    var CSS_COLOR_NAMES = ["#FF5C00","#0E53A7","#48DD00","#FFD500","#7309AA","#CD0074","#00AF64","#BF8230","#F16D95","#A65000","#A65000","#AF66D5"];
    var colorIndex = 0;

    function nextColor(){
      if(colorIndex < CSS_COLOR_NAMES.length )
        colorIndex++;
      else  colorIndex = 0;
      return colorIndex;
    }

    function getColumn(position, datasets){
      var column = [];
      $.each(datasets , function(index,value){
        if(value.data[position] != undefined) column.push(value.data[position]);
      });
      return column;
    }

    function displayLegend(){
      if(json.data.datasets[0].label ==""){
        if(json.options.legend == undefined){
          json.options.legend ={};
          json.options.legend.display = false;
        }else
          json.options.legend.display = false;
      }
    }

    function getDataset(args){
      var ds = [];
      var size = 4;
      if(Array.isArray(args[4])
      && typeof args[4][0] === 'object'
      && 'label' in args[4][0]
      && 'data' in args[4][0]){
        args = args[4];
        size = 0;
      }
      for(size ; size <  args.length ; size++){
        if(args[size].label){
          if(args[size].options){
            if(args[size].data) ds.push(cronapi.chart.createDataset(args[size].label,args[size].data,args[size].options) );
            else  ds.push(cronapi.chart.createDataset(args[size].label,args[size],null) );
            ds.push(cronapi.chart.createDataset(args[size].label,args[size].data,args[size].options) );
          }else{
            ds.push(cronapi.chart.createDataset(args[size].label,args[size].data,null) );
          }
        }else
        {
          if(args[size].options){
            if(args[size].data)  ds.push(cronapi.chart.createDataset(null,args[size].data, args[size].options) );
            else   ds.push(cronapi.chart.createDataset(null,args[size], args[size].options) );
            ds.push(cronapi.chart.createDataset(null,args[size].data, args[size].options) );
          }else{
            if(args[size].data)  ds.push(cronapi.chart.createDataset(null,args[size].data, null) );
            else   ds.push(cronapi.chart.createDataset(null,args[size], null) );
          }
        }
      }
      return ds;
    }
    function beginAtZero(){
      if(json.options == undefined){ json.options = {};
        json.options.scales={};
        json.options.scales.yAxes = [{ticks: {beginAtZero:true}}];
      }else if(json.options.scales == undefined) { json.options.scales= {}; json.options.scales.yAxes = [{ticks: {beginAtZero:true}}] };
    }

    var ctx = document.getElementById(chartId);
    if (!ctx)
      return;
    if (ctx._chart) {
      ctx._chart.destroy();
    }
    ctx.getContext('2d');
    var json = {};
    json.type = type;
    json.data = [];
    json.options= {};
    if(Array.isArray(chartLegends)){
      json.data.labels = chartLegends;
    }else
      json.data.labels = JSON.parse(chartLegends);
    json.data.datasets = [];
    if(Array.isArray(options)) json.options = options;
    else if(options != "" && options != null) {
      try {
        json.options = JSON.parse(options);

      }catch(e){
        json.options={};
        console.log(e);
      }
    }else {
      json.options= {};
    }


    switch(type){
      case 'line':{
        json.data.datasets = getDataset(arguments);
        //Applying configs in Datasets
        $.each(json.data.datasets, function(index,value){
          value.fill = false;
          value.backgroundColor = CSS_COLOR_NAMES[nextColor()];
          value.borderColor = value.backgroundColor;
          beginAtZero();
          displayLegend();
        });

        break;
      }
      case 'bar':
      case 'horizontalBar': {
        json.data.datasets = getDataset(arguments);
        //Applying configs in Datasets
        $.each(json.data.datasets, function(index,value){
          value.backgroundColor = CSS_COLOR_NAMES[nextColor()];
          value.borderColor = value.backgroundColor;
        });
        beginAtZero();
        displayLegend();
        break;
      }
      case 'doughnut':
      case 'pie':
      case 'polarArea':
      {
        var ds = getDataset(arguments);
        $.each(ds, function(index, value){
          var dtset = {};
          dtset = ds[index];
          dtset.backgroundColor = [];
          dtset.borderColor = [];
          $.each(dtset.data, function(indexx,valuee){
            dtset.backgroundColor.push( CSS_COLOR_NAMES[nextColor()] );

          });
          dtset.borderColor =  dtset.backgroundColor;
          json.data.datasets.push(dtset);
          colorIndex = 0;
        });
        break;
      }

      default :{
      }
    }
    var chart = new Chart(ctx, json);
    ctx._chart = chart;

  }

  /**
   * @type function
   * @name {{createSerieName}}
   * @nameTags chart|graficos|series|serie|dados
   * @description {{createSerieDescription}}
   * @returns {ObjectType.LIST}
   */
  this.cronapi.chart.createDataset = function(/** @type {ObjectType.STRING} @description {{createSerieParamName}} */ name, /** @type {ObjectType.LIST}  @description {{createSerieParamData}} */ data , /** @type {ObjectType.LIST}  @description {{createSerieParamOptions}} */ options  ) {
    var dataset = {};
    if(name)  dataset.label = name;  else  dataset.label ="";
    if(Array.isArray(data))  dataset.data = data;  else {   if(data){ dataset.data = JSON.parse(data);}else dataset.data = [];}
    if(Array.isArray(options)){   dataset.options = options;} else  dataset.options = JSON.parse(options);
    return dataset;
  }



  /**
   * @category CategoryType.SOCIAL
   * @categoryTags login|social|network|facebook|github|google|linkedin
   */
  this.cronapi.social = {};


  this.cronapi.social.gup = function(name,url){
    if (!url) url = location.href;
    name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
    var regexS = "[\\?&]"+name+"=([^&#]*)";
    var regex = new RegExp( regexS );
    var results = regex.exec( url );
    return results == null ? null : results[1];
  }

  this.cronapi.social.login = function(login,password,options){
    var item;
    this.cronapi.screen.showLoading();
    if (!this.cronapi.logic.isNullOrEmpty(this.cronapi.screen.getHostapp())) {
      this.cronapi.util.getURLFromOthers('POST', 'application/x-www-form-urlencoded', String(this.cronapi.screen.getHostapp()) + String('auth'), this.cronapi.object.createObjectFromString(['{ \"username\": \"',login,'\" , \"password\": \"',password,'\" } '].join('')), this.cronapi.object.createObjectFromString(['{ \"X-AUTH-TOKEN\": \"',options,'\" } '].join('')), function(sender_item) {
        item = sender_item;
        this.cronapi.screen.hide();
        this.cronapi.util.setLocalStorage('_u', this.cronapi.object.serializeObject(item));
        this.cronapi.screen.changeView("#/app/logged/home",[  ]);
      }.bind(this), function(sender_item) {
        item = sender_item;
        if (this.cronapi.object.getProperty(item, 'status') == '403' || this.cronapi.object.getProperty(item, 'status') == '401') {
          this.cronapi.screen.notify('error',this.cronapi.i18n.translate("LoginViewInvalidpassword",[  ]));
        } else {
          this.cronapi.screen.notify('error',this.cronapi.object.getProperty(item, 'responseJSON.message'));
        }
        this.cronapi.screen.hide();
      }.bind(this));
    } else {
      this.cronapi.screen.hide();
      this.cronapi.screen.notify('error','HostApp is Required');
    }
  }

  /**
   * @type function
   * @name {{socialLogin}}
   * @nameTags login|social|network|facebook|github|google|linkedin
   * @description {{socialLoginDescription}}
   * @param {ObjectType.STRING} socialNetwork {{socialNetwork}}
   * @param {ObjectType.BOOLEAN} clearCacheBeforeOpen {{clearCacheBeforeOpen}}
   * @returns {ObjectType.VOID}
   */
  this.cronapi.social.sociaLogin = function(/** @type {ObjectType.STRING} @description socialNetwork @blockType util_dropdown @keys facebook|github|google|linkedin @values facebook|github|google|linkedin  */ socialNetwork, /** @type {ObjectType.BOOLEAN} @blockType util_dropdown @keys false|true*/ clearCache) {
    var that = this;
    var u = window.hostApp+"signin/"+socialNetwork+"/";
    if(window.cordova && cordova.InAppBrowser){
      var clearCacheString = '';
      if(clearCache === true || clearCache === 'true'){
        clearCacheString = ',clearcache=yes';
      }
      var cref = cordova.InAppBrowser.open(u, '_blank', 'location=no' + clearCacheString);
      cref.addEventListener('loadstart', function(event) {
        if (event.url.indexOf("_ctk") > -1) {
          cref.close();
          that.cronapi.social.login.bind(that)('#OAUTH#', '#OAUTH#', that.cronapi.social.gup('_ctk',event.url));
        }
      });
    }else{
      window.location.href = "signin/"+socialNetwork+"/";
    }
  }

  /**
   * @type function
   * @name {{getSelectedRowsGrid}}
   * @nameTags getSelectedRowsGrid|Obter linhas selecionadas da grade
   * @description {{functionToGetSelectedRowsGrid}}
   * @param {ObjectType.STRING} field {{field}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.screen.getSelectedRowsGrid = function(/** @type {ObjectType.STRING} @blockType field_from_screen*/ field) {
    var result = [];
    var grid = $('[ng-model="'+ field  +'"]').children().data('kendoGrid');
    if (grid) {
      var selected = grid.select();
      selected.each(function() {
        var dataItem = grid.dataItem(this);
        result.push(dataItem);
      });
    }
    return result;
  };

  /**
   * @type function
   * @name {{back}}
   * @nameTags back|voltar|retroceder|history
   * @description {{backDescription}}
   */
  this.cronapi.screen.back = function() {
    history.back();
  }

  /**
   * @category CategoryType.REGEX
   * @categoryTags REGEX|regex
   */

  this.cronapi.regex = {};

   /**
   * @type function
   * @name {{extractTextByRegex}}
   * @nameTags extractTextByRegex
   * @description {{extractTextByRegexDescription}}
   * @param {ObjectType.STRING} text {{text}}
   * @param {ObjectType.STRING} regex {{regex}}
   * @param {ObjectType.STRING} flag {{flag}}
   * @returns {ObjectType.LIST}
   */
  this.cronapi.regex.extractTextByRegex = function(/** @type {ObjectType.STRING} @defaultValue */ text, /** @type {ObjectType.STRING} */ regex, /** @type {ObjectType.STRING} @description {{flag}} @defaultValue - @blockType util_dropdown @keys -|g|i|m|gi|gim|gm  @values {{-}}|{{g}}|{{i}}|{{m}}|{{gi}}|{{gim}}|{{gm}}  */ flag){
    if (this.cronapi.logic.isNull(text) || this.cronapi.logic.isNull(regex))
      return [];

    let regexInstance = null;

    if (flag != null && flag !== '-'){
      regexInstance = new RegExp(regex, flag);
    }else{
       regexInstance = new RegExp(regex);
    }

    let matches, output = [];
    let idx = null;

    while (matches = regexInstance.exec(text)) {

        if(idx != null && idx == matches.index) break;

        let occurrence =  [];

        for(i = 1; i < matches.length ; i++){
          occurrence.push(matches[i]);
        }

        idx = matches.index;

        output.push(occurrence);

    }

    return output;
  }

  /**
   * @type function
   * @name {{validateTextWithRegex}}
   * @nameTags validateTextWithRegex
   * @description {{validateTextWithRegexDescription}}
   * @param {ObjectType.STRING} text {{text}}
   * @param {ObjectType.STRING} regex {{regex}}
   * @param {ObjectType.STRING} flag {{flag}}
   * @returns {ObjectType.BOOLEAN}
   */
  this.cronapi.regex.validateTextWithRegex = function(/** @type {ObjectType.STRING} */ text, /** @type {ObjectType.STRING} */ regex, /** @type {ObjectType.STRING} @description {{flag}} @defaultValue - @blockType util_dropdown @keys -|g|i|m|gi|gim|gm  @values {{-}}|{{g}}|{{i}}|{{m}}|{{gi}}|{{gim}}|{{gm}}  */ flag){
    if (this.cronapi.logic.isNull(text) || this.cronapi.logic.isNull(regex))
      return false;
    if (flag != null && flag !== '-')
      return new RegExp(regex, flag).test(text);

    return new RegExp(regex).test(text);
  }

  /**
   * @category Calendar
   * @categoryTags calendar|calendário
   */
  this.cronapi.calendar = {};

  function getCalendar(id) {
    let start = Date.now();
    let timeout = 10000;
    let waitForChatData = (resolve, reject) => {
      let calendar = $('#' + id + ' cron-calendar').data("kendoCalendar");
      if (calendar) {
        resolve(calendar);
      } else if (timeout && (Date.now() - start) >= timeout) {
        resolve();
      } else {
        setTimeout(() => waitForChatData(resolve, reject), 200);
      }
    };
    return new Promise(waitForChatData);
  }

  function showErrorNotification(errorMsgKey) {
    const errorMsg = this.cronapi.$translate.instant(errorMsgKey);
    this.cronapi.$scope.Notification.error(errorMsg);
  }

  /**
   * @type function
   * @name {{getCalendarValue}}
   * @nameTags Calendar
   * @platform W
   * @description {{getCalendarValueDesc}}
   * @param {ObjectType.STRING} id {{id}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.calendar.getCalendarValue = async function (/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id) {

    const calendar = await getCalendar(id);
    if (calendar) {
      return calendar.value();
    } else {
      showErrorNotification("calendarElementNotPresent");
      return null;
    }
  };

  /**
   * @type function
   * @name {{setCalendarValue}}
   * @nameTags Calendar
   * @platform W
   * @description {{setCalendarValueDesc}}
   * @param {ObjectType.STRING} id {{id}}
   * @param {ObjectType.OBJECT} value {{value}}
   */
  this.cronapi.calendar.setCalendarValue = async function (/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id, /** @type {ObjectType.OBJECT} */ value) {

    const calendar = await getCalendar(id);
    if (calendar) {
      calendar.value(value);
    } else {
      showErrorNotification("calendarElementNotPresent");
    }
  };

  /**
   * @type function
   * @name {{getCalendarMin}}
   * @nameTags Calendar
   * @platform W
   * @description {{getCalendarMinDesc}}
   * @param {ObjectType.STRING} id {{id}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.calendar.getCalendarMin = async function (/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id) {

    const calendar = await getCalendar(id);
    if (calendar) {
      return calendar.min();
    } else {
      showErrorNotification("calendarElementNotPresent");
      return null;
    }
  };

  /**
   * @type function
   * @name {{setCalendarMin}}
   * @nameTags Calendar
   * @platform W
   * @description {{setCalendarMinDesc}}
   * @param {ObjectType.STRING} id {{id}}
   * @param {ObjectType.OBJECT} value {{value}}
   */
  this.cronapi.calendar.setCalendarMin = async function (/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id, /** @type {ObjectType.OBJECT} */ value) {

    const calendar = await getCalendar(id);
    if (calendar) {
      calendar.min(value);
    } else {
      showErrorNotification("calendarElementNotPresent");
    }
  };

  /**
   * @type function
   * @name {{getCalendarMax}}
   * @nameTags Calendar
   * @platform W
   * @description {{getCalendarMaxDesc}}
   * @param {ObjectType.STRING} id {{id}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.calendar.getCalendarMax = async function (/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id) {

    const calendar = await getCalendar(id);
    if (calendar) {
      return calendar.max();
    } else {
      showErrorNotification("calendarElementNotPresent");
      return null;
    }
  };

  /**
   * @type function
   * @name {{setCalendarMax}}
   * @nameTags Calendar
   * @platform W
   * @description {{setCalendarMaxDesc}}
   * @param {ObjectType.STRING} id {{id}}
   * @param {ObjectType.OBJECT} value {{value}}
   */
  this.cronapi.calendar.setCalendarMax = async function (/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id, /** @type {ObjectType.OBJECT} */ value) {

    const calendar = await getCalendar(id);
    if (calendar) {
      calendar.max(value);
    } else {
      showErrorNotification("calendarElementNotPresent");
    }
  };


  /**
   * @type function
   * @name {{getCalendarSelectDates}}
   * @nameTags Calendar
   * @platform W
   * @description {{getCalendarSelectDatesDesc}}
   * @param {ObjectType.STRING} id {{id}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.calendar.getCalendarSelectDates = async function (/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id) {

    const calendar = await getCalendar(id);
    if (calendar) {
      return calendar.selectDates();
    } else {
      showErrorNotification("calendarElementNotPresent");
      return null;
    }
  };

  /**
   * @type function
   * @name {{setCalendarSelectDates}}
   * @nameTags Calendar
   * @platform W
   * @description {{setCalendarSelectDatesDesc}}
   * @param {ObjectType.STRING} id {{id}}
   * @param {ObjectType.OBJECT} value {{value}}
   */
  this.cronapi.calendar.setCalendarSelectDates = async function (/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id, /** @type {ObjectType.OBJECT} */ value) {

    const calendar = await getCalendar(id);
    if (calendar) {
      calendar.selectDates((Array.isArray(value) ? value : [value]));
    } else {
      showErrorNotification("calendarElementNotPresent");
    }
  };

  /**
   * @type function
   * @name {{navigateCalendarTo}}
   * @nameTags Calendar
   * @platform W
   * @description {{navigateCalendarToDesc}}
   * @param {ObjectType.STRING} id {{id}}
   * @param {ObjectType.OBJECT} value {{value}}
   * @param {ObjectType.STRING} view {{calendarView}}
   */
  this.cronapi.calendar.navigateCalendarTo = async function (/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id, /** @type {ObjectType.OBJECT} */ value, /** @type {ObjectType.STRING} @description {{calendarView}} @blockType util_dropdown @keys month|year|decade|century @values {{month}}|{{year}}|{{decade}}|{{century}}  */ view) {

    const calendar = await getCalendar(id);
    if (calendar) {
      calendar.navigate(value, view);
    } else {
      showErrorNotification("calendarElementNotPresent");
    }
  };

  /**
   * @category Chat
   * @categoryTags chat|chatbot|message
   */
  this.cronapi.chat = {};

  function getChat(id) {
    let start = Date.now();
    let timeout = 10000;
    let waitForChatData = (resolve, reject) => {
      let chat = $('#' + id + ' .k-chat').data("kendoChat");
      if (chat) {
        resolve(chat);
      } else if (timeout && (Date.now() - start) >= timeout) {
        resolve();
      } else {
        setTimeout(() => waitForChatData(resolve, reject), 200);
      }
    };
    return new Promise(waitForChatData);
  }

  /**
   * @type function
   * @name {{chatUserObj}}
   * @nameTags Chat
   * @platform M
   * @description {{chatUserObjDesc}}
   * @param {ObjectType.STRING} id {{id}}
   * @param {ObjectType.STRING} name {{name}}
   * @param {ObjectType.STRING} iconUrl {{iconUrl}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.chat.chatUserObj = function (/** @type {ObjectType.STRING} */ id, /** @type {ObjectType.STRING} */ name, /** @type {ObjectType.STRING} */ iconUrl) {
    return {
      id: id,
      name: name,
      iconUrl: iconUrl
    };
  };

  /**
   * @type function
   * @name {{chatAttachmentObj}}
   * @nameTags Chat
   * @platform M
   * @description {{chatAttachmentObjDesc}}
   * @param {ObjectType.STRING} attachmentTitle {{attachmentTitle}}
   * @param {ObjectType.STRING} attachmentSubtitle {{attachmentSubtitle}}
   * @param {ObjectType.STRING} attachmentText {{attachmentText}}
   * @param {ObjectType.OBJECT} attachmentImage {{attachmentImage}}
   * @param {ObjectType.STRING} attachmentImageAlt {{attachmentImageAlt}}
   * @param {ObjectType.OBJECT} attachmentButtons {{chatSuggestedActions}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.chat.chatAttachmentObj = function ( /** @type {ObjectType.STRING} */ attachmentTitle, /** @type {ObjectType.STRING} */ attachmentSubtitle, /** @type {ObjectType.STRING} */ attachmentText,  /** @type {ObjectType.STRING} */ attachmentImage, /** @type {ObjectType.STRING} */ attachmentImageAlt, /** @type {ObjectType.OBJECT} */ attachmentButtons) {
    return {
      contentType: 'heroCard',
      content: {
        title: attachmentTitle,
        subtitle: attachmentSubtitle,
        text: attachmentText,
        images: [{
          url: attachmentImage,
          alt: attachmentImageAlt
        }],
        buttons: (Array.isArray(attachmentButtons) ? attachmentButtons : [attachmentButtons])
      }
    };
  };

  /**
   * @type function
   * @name {{chatSuggestedActionObj}}
   * @nameTags Chat
   * @platform M
   * @description {{chatSuggestedActionObjDesc}}
   * @param {ObjectType.STRING} suggestedActionTitle {{suggestedActionTitle}}
   * @param {ObjectType.STRING} suggestedActionValue {{suggestedActionValue}}
   * @returns {ObjectType.OBJECT}
   */
  this.cronapi.chat.chatSuggestedActionObj = function (/** @type {ObjectType.STRING} */ suggestedActionTitle, /** @type {ObjectType.STRING} */ suggestedActionValue) {
    return {
      title: suggestedActionTitle,
      value: suggestedActionValue
    };
  };

  /**
   * @type function
   * @name {{getChatUser}}
   * @nameTags Chat
   * @platform M
   * @description {{getChatUserDesc}}
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @type {ObjectType.STATEMENTSENDER} @description {{success}}
   * @type {ObjectType.STATEMENTSENDER} @description {{error}}
   */
  this.cronapi.chat.getChatUser = async function (/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id, /** @type {ObjectType.STATEMENTSENDER} @description {{success}} */ success, /** @type {ObjectType.STATEMENTSENDER} @description {{error}} */  error) {

    getChat(id).then((chat) => {
      if (chat) {
        success(chat.getUser());
      } else {
        let errorMsg = this.cronapi.$translate.instant("chatElementNotPresent");
        this.cronapi.$scope.Notification.error(errorMsg);
        error(errorMsg);
      }
    });

  };

  /**
   * @type function
   * @name {{postChatMessage}}
   * @nameTags Chat
   * @platform M
   * @description {{postChatMessageDesc}}
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @param {ObjectType.STRING} textMessage {{textMessage}}
   * @type {ObjectType.STATEMENTSENDER} @description {{success}}
   * @type {ObjectType.STATEMENTSENDER} @description {{error}}
   */
  this.cronapi.chat.postChatMessage = function (/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id, /** @type {ObjectType.STRING} */ textMessage, /** @type {ObjectType.STATEMENTSENDER} @description {{success}} */ success, /** @type {ObjectType.STATEMENTSENDER} @description {{error}} */  error) {

    getChat(id).then((chat) => {
      if (chat) {
        chat.postMessage(textMessage);
        success();
      } else {
        let errorMsg = this.cronapi.$translate.instant("chatElementNotPresent");
        this.cronapi.$scope.Notification.error(errorMsg);
        error(errorMsg);
      }
    });

  };

  /**
   * @type function
   * @name {{renderChatMessage}}
   * @nameTags Chat
   * @platform M
   * @description {{renderChatMessageDesc}}
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @param {ObjectType.STRING} chatMessage {{chatMessage}}
   * @param {ObjectType.OBJECT} chatUser {{chatUser}}
   * @type {ObjectType.STATEMENTSENDER} @description {{success}}
   * @type {ObjectType.STATEMENTSENDER} @description {{error}}
   */
  this.cronapi.chat.renderChatMessage = function (/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id, /** @type {ObjectType.STRING} */ chatMessage, /** @type {ObjectType.OBJECT} */ chatUser, /** @type {ObjectType.STATEMENTSENDER} @description {{success}} */ success, /** @type {ObjectType.STATEMENTSENDER} @description {{error}} */  error) {

    getChat(id).then((chat) => {
      if (chat) {
        chat.renderMessage({'type': 'text', 'text': chatMessage}, chatUser);
        success();
      } else {
        let errorMsg = this.cronapi.$translate.instant("chatElementNotPresent");
        this.cronapi.$scope.Notification.error(errorMsg);
        error(errorMsg);
      }
    });

  };

  /**
   * @type function
   * @name {{renderChatAttachments}}
   * @nameTags Chat
   * @platform M
   * @description {{renderChatAttachmentsDesc}}
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @param {ObjectType.OBJECT} chatUser {{chatUser}}
   * @param {ObjectType.OBJECT} chatAttachments {{chatAttachments}}
   * @param {ObjectType.STRING} chatAttachmentLayout {{chatAttachmentLayout}}
   * @type {ObjectType.STATEMENTSENDER} @description {{success}}
   * @type {ObjectType.STATEMENTSENDER} @description {{error}}
   */
  this.cronapi.chat.renderChatAttachments = function (/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id, /** @type {ObjectType.OBJECT} */ chatUser, /** @type {ObjectType.OBJECT} */ chatAttachments, /** @type {ObjectType.STRING} @description {{chatAttachmentLayoutDesc}} @blockType util_dropdown @keys list|carousel @values {{chatAttachmentList}}|{{chatAttachmentCarousel}}  */ chatAttachmentLayout, /** @type {ObjectType.STATEMENTSENDER} @description {{success}} */ success, /** @type {ObjectType.STATEMENTSENDER} @description {{error}} */  error) {

    getChat(id).then((chat) => {
      if (chat) {
        chat.renderAttachments({
          attachments: (Array.isArray(chatAttachments) ? chatAttachments : [chatAttachments]),
          attachmentLayout: chatAttachmentLayout
        }, chatUser);
        success();
      } else {
        let errorMsg = this.cronapi.$translate.instant("chatElementNotPresent");
        this.cronapi.$scope.Notification.error(errorMsg);
        error(errorMsg);
      }
    });

  };

  /**
   * @type function
   * @name {{renderChatHtml}}
   * @nameTags Chat
   * @platform M
   * @description {{renderChatHtmlDesc}}
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @param {ObjectType.OBJECT} chatUser {{chatUser}}
   * @param {ObjectType.OBJECT} html Html
   * @param {ObjectType.STRING} chatAttachmentLayout {{chatAttachmentLayout}}
   * @type {ObjectType.STATEMENTSENDER} @description {{success}}
   * @type {ObjectType.STATEMENTSENDER} @description {{error}}
   */
  this.cronapi.chat.renderChatHtml = function (/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id, /** @type {ObjectType.OBJECT} */ chatUser, /** @type {ObjectType.OBJECT} */ html, /** @type {ObjectType.STRING} @description {{chatAttachmentLayoutDesc}} @blockType util_dropdown @keys list|carousel @values {{chatAttachmentList}}|{{chatAttachmentCarousel}}  */ chatAttachmentLayout, /** @type {ObjectType.STATEMENTSENDER} @description {{success}} */ success, /** @type {ObjectType.STATEMENTSENDER} @description {{error}} */  error) {

    // Remove any script in the html message
    const stripScripts = function (s) {
      let div = document.createElement('div');
      div.innerHTML = s;
      let scripts = div.getElementsByTagName('script');
      let i = scripts.length;
      while (i--) {
        scripts[i].parentNode.removeChild(scripts[i]);
      }
      return div.innerHTML;
    };

    const kendoChatHtmlTemplate = kendo.template(`
              <div class="#=styles.card# #=styles.cardRich#">
                  <div class="#=styles.cardBody#">
                    <div>
                    #=text#
                    </div>
                  </div>
              </div>`);

    if(!kendo.chat.Templates.kendoChatHtmlTemplate){
      kendo.chat.registerTemplate("kendoChatHtmlTemplate", kendoChatHtmlTemplate);
    }

    let attachments = [];

    if (Array.isArray(html)) {
      for(let index in html){
        let attachment = {
          contentType: "kendoChatHtmlTemplate",
          content: {
            "text": stripScripts(html[index])
          }
        };
        attachments.push(attachment)
      }
    } else {
      let attachment = {
        contentType: "kendoChatHtmlTemplate",
        content: {
          "text": stripScripts(html)
        }
      };
      attachments.push(attachment)
    }

    getChat(id).then((chat) => {
      if (chat) {
        chat.renderAttachments({
          attachments: attachments,
          attachmentLayout: chatAttachmentLayout
        }, chatUser);
        success();
      } else {
        let errorMsg = this.cronapi.$translate.instant("chatElementNotPresent");
        this.cronapi.$scope.Notification.error(errorMsg);
        error(errorMsg);
      }
    });

  };

  /**
   * @type function
   * @name {{renderChatSuggestedActions}}
   * @nameTags Chat
   * @platform M
   * @description {{renderChatSuggestedActionsDesc}}
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @param {ObjectType.OBJECT} chatSuggestedActions {{chatSuggestedActions}}
   * @type {ObjectType.STATEMENTSENDER} @description {{success}}
   * @type {ObjectType.STATEMENTSENDER} @description {{error}}
   */
  this.cronapi.chat.renderChatSuggestedActions = function (/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id, /** @type {ObjectType.OBJECT} */ chatSuggestedActions, /** @type {ObjectType.STATEMENTSENDER} @description {{success}} */ success, /** @type {ObjectType.STATEMENTSENDER} @description {{error}} */  error) {

    getChat(id).then((chat) => {
      if (chat) {
        chat.renderSuggestedActions((Array.isArray(chatSuggestedActions) ? chatSuggestedActions : [chatSuggestedActions]));
        success();
      } else {
        let errorMsg = this.cronapi.$translate.instant("chatElementNotPresent");
        this.cronapi.$scope.Notification.error(errorMsg);
        error(errorMsg);
      }
    });

  };

  /**
   * @type function
   * @name {{renderChatUserTypingIndicator}}
   * @nameTags Chat
   * @platform M
   * @description {{renderChatUserTypingIndicatorDesc}}
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @param {ObjectType.OBJECT} chatUser {{chatUser}}
   * @type {ObjectType.STATEMENTSENDER} @description {{success}}
   * @type {ObjectType.STATEMENTSENDER} @description {{error}}
   */
  this.cronapi.chat.renderChatUserTypingIndicator = function (/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id, /** @type {ObjectType.OBJECT} */ chatUser, /** @type {ObjectType.STATEMENTSENDER} @description {{success}} */ success, /** @type {ObjectType.STATEMENTSENDER} @description {{error}} */  error) {

    getChat(id).then((chat) => {
      if (chat) {
        chat.renderUserTypingIndicator(chatUser);
        success();
      } else {
        let errorMsg = this.cronapi.$translate.instant("chatElementNotPresent");
        this.cronapi.$scope.Notification.error(errorMsg);
        error(errorMsg);
      }
    });

  };

  /**
   * @type function
   * @name {{clearChatUserTypingIndicator}}
   * @nameTags Chat
   * @platform M
   * @description {{clearChatUserTypingIndicatorDesc}}
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @param {ObjectType.OBJECT} chatUser {{chatUser}}
   * @type {ObjectType.STATEMENTSENDER} @description {{success}}
   * @type {ObjectType.STATEMENTSENDER} @description {{error}}
   */
  this.cronapi.chat.clearChatUserTypingIndicator = function (/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id, /** @type {ObjectType.OBJECT} */ chatUser, /** @type {ObjectType.STATEMENTSENDER} @description {{success}} */ success, /** @type {ObjectType.STATEMENTSENDER} @description {{error}} */  error) {

    getChat(id).then((chat) => {
      if (chat) {
        chat.clearUserTypingIndicator(chatUser);
        success();
      } else {
        let errorMsg = this.cronapi.$translate.instant("chatElementNotPresent");
        this.cronapi.$scope.Notification.error(errorMsg);
        error(errorMsg);
      }
    });

  };

  /**
   * @type function
   * @name {{removeChatTypingIndicator}}
   * @nameTags Chat
   * @platform M
   * @description {{removeChatTypingIndicatorDesc}}
   * @param {ObjectType.STRING} component {{ComponentParam}}
   * @param {ObjectType.OBJECT} chatUser {{chatUser}}
   * @type {ObjectType.STATEMENTSENDER} @description {{success}}
   * @type {ObjectType.STATEMENTSENDER} @description {{error}}
   */
  this.cronapi.chat.removeChatTypingIndicator = function (/** @type {ObjectType.OBJECT} @blockType ids_from_screen*/ id, /** @type {ObjectType.STATEMENTSENDER} @description {{success}} */ success, /** @type {ObjectType.STATEMENTSENDER} @description {{error}} */  error) {

    getChat(id).then((chat) => {
      if (chat) {
        chat.removeTypingIndicator();
        success();
      } else {
        let errorMsg = this.cronapi.$translate.instant("chatElementNotPresent");
        this.cronapi.$scope.Notification.error(errorMsg);
        error(errorMsg);
      }
    });

  }
}

(cronapi).bind(window)();


// This is only for test purpose
try{
if(module){
  module.exports = {
    window
  };
}
}catch(err){}
