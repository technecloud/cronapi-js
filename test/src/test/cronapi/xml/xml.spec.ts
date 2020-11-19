describe('Test suit for category xml from Cronapi.js', function() {
  const ch = require('chai');
  ch.should();

  const $ = require('jquery');

  let {window} = require('../../../../../cronapi');
  const cronapi = window["cronapi"];

  window.$ = $;

  it('newXMLEmpty', function() {
    let value = cronapi.xml.newXMLEmpty.bind(window)();
    (value === undefined || value === null || value === '').should.equal(false);
  });

  it('newXMLEmptyWithRoot', function() {
  });

  it('newXMLElement', function() {
  });

  it('addXMLElement', function() {
    let value = cronapi.xml.addXMLElement(null, new Object());
    value.should.eql(false);
  });

  it('XMLHasRootElement', function() {
  });

  it('XMLGetRootElement', function() {
  });

  it('XMLDocumentToText', function() {
  });

  it('getChildren', function() {
  });

  it('setAttribute', function() {
  });

  it('getAttributeValue', function() {
    let value = cronapi.xml.getAttributeValue(null, false);
    value.should.eql('');
  });

  it('getParentNode', function() {
  });

  it('setElementContent', function() {
  });

  it('getElementContent', function() {
  });

  it('removeElement', function() {
  });

  it('getElementName', function() {
  });

  it('renameElement', function() {
  });

});
