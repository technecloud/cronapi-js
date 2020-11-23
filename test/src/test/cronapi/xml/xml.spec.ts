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
    let value = cronapi.xml.getParentNode($.parseXML('<?xml version="1.0" encoding="UTF-8"?><root></root>'));
    (value === undefined || value === null || value === '').should.equal(true);
  });

  it('setElementContent', function() {
    let value = cronapi.xml.setElementContent($.parseXML('<?xml version="1.0" encoding="UTF-8"?><root></root>'), $.parseXML('<?xml version="1.0" encoding="UTF-8"?><root></root>'));
    (value === undefined || value === null || value === '').should.equal(true);
  });

  it('getElementContent', function() {
    let value = cronapi.xml.getElementContent($.parseXML('<?xml version="1.0" encoding="UTF-8"?><root></root>'));
    (value === undefined || value === null || value === '').should.equal(true);
  });

  it('removeElement', function() {
    let value = cronapi.xml.removeElement($.parseXML('<?xml version="1.0" encoding="UTF-8"?><root></root>'), true);
    (value === undefined || value === null || value === '').should.equal(true);

    value = cronapi.xml.removeElement($.parseXML('<?xml version="1.0" encoding="UTF-8"?><root></root>'), false);
    (value === undefined || value === null || value === '').should.equal(true);
  });

  it('getElementName', function() {
    let value = cronapi.xml.getElementName($.parseXML('<?xml version="1.0" encoding="UTF-8"?><root></root>'));
    (value === undefined || value === null || value === '').should.equal(true);
  });

  it('renameElement', function() {
  });

});
