describe('Test suit for category conversion from Cronapi.js',
function() {
  const ch = require('chai');
  let {window} = require('../../../../../cronapi');
  ch.should();
  const cronapi = window["cronapi"];

  it('asciiToBinary', function() {
    let value = cronapi.conversion.asciiToBinary.bind(window)('hello');
    value.should.equal('0110100001100101011011000110110001101111');
    
    value = cronapi.conversion.asciiToBinary.bind(window)('');
    value.should.equal('');
  });

  it('toBoolean', function() {
    let value = cronapi.conversion.toBoolean.bind(window)(true);
    value.should.equal(true);

    value = cronapi.conversion.toBoolean.bind(window)(false);
    value.should.equal(false);

    value = cronapi.conversion.toBoolean.bind(window)(null);
    value.should.equal(false);

    value = cronapi.conversion.toBoolean.bind(window)('1');
    value.should.equal(true);

    value = cronapi.conversion.toBoolean.bind(window)('true');
    value.should.equal(true);
  });

it('toBytes', function() {
  let value = cronapi.conversion.toBytes.bind(window)();
  value.should.equal('');

  value = cronapi.conversion.toBytes.bind(window)(null);
  value.should.equal('');

  value = cronapi.conversion.toBytes.bind(window)(undefined);
  value.should.equal('');

  value = cronapi.conversion.toBytes.bind(window)(10);
  value.should.equal('10');
});

  it('chrToAscii ', function() {
    let value = cronapi.conversion.chrToAscii.bind(window)();
    (value === null).should.equal(true);
    
    value = cronapi.conversion.chrToAscii.bind(window)(null);
    (value === null).should.equal(true);
    
    value = cronapi.conversion.chrToAscii.bind(window)(undefined);
    (value === null).should.equal(true);
    
    value = cronapi.conversion.chrToAscii.bind(window)('h');
    value.should.equal(104);
    
    value = cronapi.conversion.chrToAscii.bind(window)('hello');
    value.should.equal(104);
  });

  it('stringToJs', function() {
    let value = cronapi.conversion.stringToJs.bind(window)('{"usename": "admin", "password": 123}');
    value.should.equal('{\\"usename\\": \\"admin\\", \\"password\\": 123}');
  });

  it('stringToDate', function() {
    const validDate = new Date(2020, 10, 13);

    let value = cronapi.conversion.stringToDate.bind(window)(validDate);
    value.should.equal(validDate);

    value = cronapi.conversion.stringToDate.bind(window)(null);
    (value === null).should.equal(true);
    
    value = cronapi.conversion.stringToDate.bind(window)(undefined);
    (value === null).should.equal(true);

    value = cronapi.conversion.stringToDate.bind(window)('2020-11');
    (value.toUTCString()).should.equal(new Date("Sun Nov 01 2020 00:00:00 GMT-0000").toUTCString());
    
    Object.defineProperty(window.navigator, 'language', { value: 'es-Es', writable: true, configurable: true });
    value = cronapi.conversion.stringToDate.bind(window)('2020-11-13');
    (value.toUTCString()).should.equal(new Date("Sun Nov 13 2020 00:00:00 GMT-0000").toUTCString());

    // Os três casos abaixo contemplariam o restante da cobertura, não ficou totalmente
    // funcional porque não foi possível mockar as funções internas chamadas
    // pelo eval: cronapi.internal.enDate e cronapi.internal.enDate

    Object.defineProperty(window.navigator, 'language', { value: 'en-Us' });
    // value = cronapi.conversion.stringToDate.bind(window)('10-13-2020');
    // value.should.equal(validDate);

    // Object.defineProperty(window.navigator, 'language', { value: 'pt-BR', writable: true });
    // value = cronapi.conversion.stringToDate.bind(window)('13-10-2020');
    // value.should.equal(validDate);
    
    // Object.defineProperty(window.navigator, 'language', { value: null });
    // Object.defineProperty(window.navigator, 'userLanguage', { value: 'pt-BR', writable: true });
    // value = cronapi.conversion.stringToDate.bind(window)('13-10-2020');
    // value.should.equal(validDate);
  });

  it('intToHex', function() {
    let value = cronapi.conversion.intToHex.bind(window)(10);
    value.should.equal('A');
  });

  it('toLong', function() {
    let value = cronapi.conversion.toLong.bind(window)('10');
    value.should.equal(10);
  });

  it('toString', function() {
    let value = cronapi.conversion.toString.bind(window)(10);
    value.should.equal('10');
    
    value = cronapi.conversion.toString.bind(window)(null);
    value.should.equal('');
    
    value = cronapi.conversion.toString.bind(window)(undefined);
    value.should.equal('');
  });

});