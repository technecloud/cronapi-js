
describe('Test suit for category conversion from Cronapi.js', function() {

const ch = require('chai');
const wd = require('../../../../../cronapi');
const cronapi = wd['cronapi']; 
ch.should();



  it('asciiToBinary', function() {
    let value = cronapi.conversion.asciiToBinary.bind(wd)("");
    value.should.equal("");
  });

  it('toBoolean', function() {
    let value = cronapi.conversion.toBoolean.bind(wd)(true);
    value.should.equal(true);

    value = cronapi.conversion.toBoolean.bind(wd)(null);
    value.should.equal(false);

    value = cronapi.conversion.toBoolean.bind(wd)("1");
    value.should.equal(true);

    value = cronapi.conversion.toBoolean.bind(wd)("true");
    value.should.equal(true);
  });

});