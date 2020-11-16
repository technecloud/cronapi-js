describe('Test suit for category Util from Cronapi.js', function() {

  const chai = require('chai');
  let {window} = require('../../../../../cronapi');
  let should = chai.should();
  const cronapi = window["cronapi"];

  it('setToken', () => {
    cronapi.util.setToken();
    localStorage.getItem('_u').should.equal("{}");

    cronapi.util.setToken('Cronapp');
    let currentItem = localStorage.getItem('_u');
    JSON.parse(currentItem).should.have.property('token');
    JSON.parse(currentItem).token.should.have.lengthOf(7);
    JSON.parse(currentItem).token.should.equal('Cronapp');

    window.should.have.property('uToken');
    window.uToken.should.equal('Cronapp');
  });

  it('getApplicationName', () => {
    
  });

  it('createPromise', () => {
    let value = cronapi.util.createPromise();
    (typeof value.then).should.equal('function');
  });

  it('handleValueToPromise ', () => {
    let result;
    const promise = cronapi.util.createPromise();
    
    promise.__functionToResolve = (value) => result = value;
    promise.__functionToReject = (value) => result = value;
    
    cronapi.util.handleValueToPromise('resolve', promise , "100");
    result.should.equal("100");

    cronapi.util.handleValueToPromise('reject', promise , "0");
    result.should.equal("0");
  });

  it('sleep', async () => {
    let x = await cronapi.util.sleep(50);
    x.should.equal(50);
  });

  it('callServerBlocklyAsync', () => {

  });

  it('getScreenFields', () => {

  }); 

  it('makeCallServerBlocklyAsync', () => {

  }); 

  it('callServerBlocklyNoReturn', () => {

  });

  it('throwException', () => {
    let exception = new Error(cronapi.util.createException('argument Cronapp is null'));
    should.throw(() => cronapi.util.throwException(exception), 'argument Cronapp is null');
    should.throw(() => cronapi.util.throwException(new TypeError('')), '');
  });

  it('createException', () => {
    cronapi.util.createException('argument Cronapp is null').should.equal('argument Cronapp is null');
  });

  it('language', () => {
    Object.defineProperty(window.navigator, 'userLanguage', { value: 'en-US', writable: true, configurable: true });
    cronapi.util.language().should.equal('en_US')

    delete window.navigator.userLanguage;

    Object.defineProperty(window.navigator, 'language', { value: 'pt-BR', writable: true });
    cronapi.util.language().should.equal('pt_BR');

    Object.defineProperty(window.navigator, 'language', { value: null, writable: true });
    cronapi.util.language().should.equal('pt_br');
  });

  it('share', () => {

  }); 

  it('callServerBlockly', () => {

  }); 

  it('callServerBlocklyAsynchronous', () => {

  });

  it('executeJavascriptNoReturn', () => {
    window.eval = jest.fn();
    cronapi.util.executeJavascriptNoReturn('alert("Cronapp");');
    expect(window.eval).toBeCalled();
    expect(window.eval).toHaveBeenCalledTimes(1);
    expect(window.eval).toHaveBeenCalledWith('alert("Cronapp");');
  });
 
  it('downloadFile', () => {
    window.open = jest.fn();
    cronapi.util.downloadFile.bind(window)('/cronapp/jest');
    expect(window.open).toBeCalled();
    expect(window.open).toHaveBeenCalledTimes(1);
    expect(window.open).toHaveBeenCalledWith('cronapp/jest','_self', '');
  });

  it('executeJavascriptReturn', () => {
    let fun = {
      sayHiCronapp() {
        return 'Hi Cronapp';
      }
    };
    window.eval = jest.fn();
    cronapi.util.executeJavascriptReturn(fun.sayHiCronapp())
    expect(window.eval).toBeCalled();
    expect(window.eval).toHaveBeenCalledTimes(1);
    expect(window.eval).toHaveBeenCalledWith(fun.sayHiCronapp());
  });

  it('openReport', () => {

  });

  it('handleCallback', () => {
    let refWithoutAsyncFunction = { bind: () => { return 'Cronapp is the best' } };
    should.equal(cronapi.util.handleCallback(refWithoutAsyncFunction), 'Cronapp is the best');
  }); 

  it('getURLFromOthers', () => {

  });

  it('getUserToken', () => {
    should.equal(cronapi.util.getUserToken(), 'Cronapp');
  });

  it('setSessionStorage', () => {
    cronapi.util.setSessionStorage('business', 'Cronapp-BA');
    sessionStorage.getItem('business').should.equal('Cronapp-BA');
  });

  it('getSessionStorage', () => {
    cronapi.util.getSessionStorage('business').should.equal('Cronapp-BA');
    sessionStorage.removeItem('business');
  });

  it('setLocalStorage', () => {
    cronapi.util.setLocalStorage('client', 'Cronapp');
    localStorage.getItem('client').should.equal('Cronapp');
  });

  it('getLocalStorage', () => {
    cronapi.util.getLocalStorage('client').should.equal('Cronapp');
    localStorage.removeItem('client');
  });

  it('executeAsynchronous', () => {
    let fun = {
      sayHiCronapp() {
        return 'Hi Cronapp';
      }
    };
    window.setTimeout = jest.fn();
    cronapi.util.executeAsynchronous(fun.sayHiCronapp());
    expect(window.setTimeout).toBeCalled();
    expect(window.setTimeout).toHaveBeenCalledTimes(1);
    expect(window.setTimeout).toHaveBeenCalledWith(fun.sayHiCronapp(), 0);
  }); 

  it('scheduleExecution', () => {
    
  });

  it('openReport', () => {
    
  }); 

  it('getCEP', () => {
    
  });

});