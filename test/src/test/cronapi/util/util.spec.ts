describe('Test suit for category Util from Cronapi.js', function() {

const ch = require('chai');
const wd = require('../../../../../cronapi');
const cronapi = wd['cronapi']; 
ch.should();


  it('createPromise', function() {
    let value = cronapi.util.createPromise.bind(wd)();
    (typeof value.then).should.equal('function');
  });

  it('handleValueToPromise ', function() {
    const promise = cronapi.util.createPromise.bind(wd)();
    let result;
    promise.__functionToResolve = (value)=> result = value;
    promise.__functionToReject = (value)=> result = value;
    
    cronapi.util.handleValueToPromise.bind(wd)( 'resolve', promise , "100");
    result.should.equal("100");

    cronapi.util.handleValueToPromise.bind(wd)( 'reject', promise , "0");
    result.should.equal("0");
  });
});