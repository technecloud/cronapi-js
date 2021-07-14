describe('Test suit for category dateTime from Cronapi.js', function() {
  const ch = require('chai');
  ch.should();
  
  const moment = require('moment');
  
  let {window} = require('../../../../../cronapi');
  const cronapi = window["cronapi"];

  window.moment = moment;

  it('formats', function() {
    window.cronapi.$translate = {use: ()=> ""}
    let value = cronapi.dateTime.formats.bind(window)();
    value.should.eql(['MM/DD/YYYY HH:mm:ss', 'MM/DD/YYYY', 'MM-DD-YYYY HH:mm:ss', 'MM-DD-YYYY', 'YYYY-MM-DDTHH:mm:ss', 'HH:mm:ss', 'MMMM']);

    window.cronapi.$translate = {use: ()=> "pt_br"}
    value = cronapi.dateTime.formats.bind(window)();
    value.should.eql(['DD/MM/YYYY HH:mm:ss', 'DD/MM/YYYY', 'DD-MM-YYYY HH:mm:ss', 'DD-MM-YYYY', 'YYYY-MM-DDTHH:mm:ss', 'HH:mm:ss', 'MMMM']);
  });

  it('getMomentObj', function() {
    
  });

  it('getSecond', function() {
    
  });

  it('getMinute', function() {
    
  });

  it('getHour', function() {
    
  });

  it('getYear', function() {
    
  });

  it('getMonth', function() {
    
  });

  it('getDay', function() {
    
  });

  it('getSecondsBetweenDates', function() {
    
  });

  it('getMinutesBetweenDates', function() {
    
  });

  it('getHoursBetweenDates', function() {
    
  });

  it('getDaysBetweenDates', function() {
    
  });

  it('getMonthsBetweenDates', function() {
    
  });

  it('getYearsBetweenDates', function() {
    
  });

  it('incSecond', function() {
    
  });

  it('incMinute', function() {
    
  });

  it('incHour', function() {
    
  });

  it('incDay', function() {
    
  });

  it('incMonth', function() {
    
  });

  it('incYear', function() {
    
  });

  it('getNow', function() {
    
  });

  it('formatDateTime', function() {
    
  });

  it('newDate', function() {
    
  });

  it('updateDate', function() {
    
  });

  it('updateNewDate', function() {
    
  });

});
