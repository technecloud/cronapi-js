
describe('Test suit for category conversion from Cronapi.js', function() {

const ch = require('chai');
let {window} = require('../../../../../cronapi');
ch.should();
const cronapi = window["cronapi"];


  it('asciiToBinary', function() {
    let value = cronapi.conversion.asciiToBinary("");
    value.should.equal("");
  });

  it('toBoolean', function() {
    let value = cronapi.conversion.toBoolean.bind(window)(true);
    value.should.equal(true);

    value = cronapi.conversion.toBoolean.bind(window)(null);
    value.should.equal(false);

    value = cronapi.conversion.toBoolean.bind(window)("1");
    value.should.equal(true);

    value = cronapi.conversion.toBoolean.bind(window)("true");
    value.should.equal(true);
  });

});