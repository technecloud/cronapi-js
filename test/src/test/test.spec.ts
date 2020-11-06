const chai = require('chai');
chai.should();
const cronapi = require('../../../cronapi');


describe('Array', function() {
  describe('#indexOf()', function() {
    it('should return -1 when the value is not present', function() {
      [1,2,3].indexOf(4).should.equal(-1);
    });
  });

  // describe('cronapi', function() {
  //   it('cronapi', function() {
  //     assert.equal(cronapi.conversion.toBoolean(true), true );
  //   });
  // });


});