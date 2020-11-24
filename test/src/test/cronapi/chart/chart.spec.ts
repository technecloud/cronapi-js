describe('Test suit for category chart from Cronapi.js', function() {
  const ch = require('chai');
  ch.should();

  let {window} = require('../../../../../cronapi');
  const cronapi = window["cronapi"];

  it('createChart', function() {
  });

  it('createDataset', function() {
    var element = document.querySelector('#user');
    let value = cronapi.chart.createDataset(element, element, element);
    value.should.eql({"data": [], "label": "", "options": null});
  });

});
