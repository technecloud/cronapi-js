describe('Test suit for category regex from Cronapi.js', function() {

  const chai = require('chai');
  chai.should();
  let expect = chai.expect;
  let {window} = require('../../../../../cronapi');
  const cronapi = window["cronapi"];
  
  it('extractTextByRegex', function() {
    let result;

    result = cronapi.regex.extractTextByRegex.bind(window)(null, '[0-9]', 'g');
    expect(result).to.be.empty;

    result = cronapi.regex.extractTextByRegex.bind(window)('abcde1234', null, 'g');
    expect(result).to.be.empty;

    result = cronapi.regex.extractTextByRegex.bind(window)('20/dec/2018', '$[0-9]{4}', null);
    expect(result).to.be.empty;

    // For sem a declaração correta da variável
    // A Interação com o matches.length só pega a partir da segunda sequencia
    // O retorno vem dentro de 2 Arrays sem necessidade
    result = cronapi.regex.extractTextByRegex.bind(window)('cdbbdbsbz', 'd(b+)d', 'g');
    expect(result).to.be.an.instanceof(Array);
    expect(result).to.be.an('array').that.includes('bb');
  });

  it('validateTextWithRegex', function() {
    let result;
    result = cronapi.regex.validateTextWithRegex.bind(window)(null, '[0-9]', 'g');
    expect(result).to.be.false;

    result = cronapi.regex.validateTextWithRegex.bind(window)('12345', null, 'g');
    expect(result).to.be.false;

    result = cronapi.regex.validateTextWithRegex.bind(window)('Olá Cronapp', '^Ol', null);
    expect(result).to.be.true;

    result = cronapi.regex.validateTextWithRegex.bind(window)('Cronapp is the BEST low code', 'best', 'gi');
    expect(result).to.be.true;
  });

});