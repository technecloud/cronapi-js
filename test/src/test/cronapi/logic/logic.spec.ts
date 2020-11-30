describe('Test suit for category logic from Cronapi.js', function() {
  const ch = require('chai');
  ch.should();

  let {window} = require('../../../../../cronapi');
  const cronapi = window["cronapi"];

  it('isNull', function() {
    let value = cronapi.logic.isNull.bind(window)(null);
    value.should.eql(true);

    value = cronapi.logic.isNull.bind(window)('undefined');
    value.should.eql(false);
  });

  it('isEmpty', function() {
    let value = cronapi.logic.isEmpty.bind(window)('');
    value.should.eql(true);
  });

  it('isNullOrEmpty', function() {
    let value = cronapi.logic.isNullOrEmpty.bind(window)(null);
    value.should.eql(true);

    value = cronapi.logic.isNullOrEmpty.bind(window)('undefined');
    value.should.eql(false);

    value = cronapi.logic.isNullOrEmpty.bind(window)('');
    value.should.eql(true);
  });

  it('typeOf', function() {
    let value = cronapi.logic.typeOf.bind(window)(null, 'array');
    value.should.eql(false);

    value = cronapi.logic.typeOf.bind(window)(new Array(), 'object');
    value.should.eql(false);

    value = cronapi.logic.typeOf.bind(window)(null, 'object');
    value.should.eql(false);
  });

});
