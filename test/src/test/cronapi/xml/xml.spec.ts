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
    let value = cronapi.xml.addXMLElement(null, $.parseXML('<?xml version="1.0" encoding="UTF-8"?><root></root>'));
    value.should.eql(false);
  });

  it('XMLHasRootElement', function() {
    let value = cronapi.xml.XMLHasRootElement($.parseXML('<?xml version="1.0" encoding="UTF-8"?><root></root>'));
    value.should.eql(true);

    value = cronapi.xml.XMLHasRootElement(null);
    value.should.eql(false);
  });

  it('XMLGetRootElement', function() {
    let value = cronapi.xml.XMLGetRootElement($.parseXML('<?xml version="1.0" encoding="UTF-8"?><root></root>'));
    (value === undefined || value === null || value === '').should.equal(false);
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
