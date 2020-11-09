

describe('Test suit for category Util from Cronapi.js', function() {

const chai = require('chai');
let {window} = require('../../../../../cronapi');
const cronapi = window["cronapi"];
chai.should();


  it('createPromise', function() {
    let value = cronapi.util.createPromise();
    (typeof value.then).should.equal('function');
  });

  it('handleValueToPromise ', function() {
    const promise = cronapi.util.createPromise();
    let result;
    promise.__functionToResolve = (value)=> result = value;
    promise.__functionToReject = (value)=> result = value;
    
    cronapi.util.handleValueToPromise( 'resolve', promise , "100");
    result.should.equal("100");

    cronapi.util.handleValueToPromise( 'reject', promise , "0");
    result.should.equal("0");
  });
});