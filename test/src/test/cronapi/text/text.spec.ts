
describe('Test suit for category text from Cronapi.js',
function() {
  const ch = require('chai');
  let {window} = require('../../../../../cronapi');
  ch.should();
  const cronapi = window["cronapi"];

  it('prompt', function() {
    let value = cronapi.text.prompt.bind(window)('abc');
    (value === null).should.equal(true);
    
    value = cronapi.text.prompt.bind(window)(123);
    (value === null).should.equal(true);
    
    value = cronapi.text.prompt.bind(window)(null);
    (value === null).should.equal(true);
    
    value = cronapi.text.prompt.bind(window)(true);
    (value === null).should.equal(true);
  });

  it('newline', function() {
    let value = cronapi.text.newline.bind(window)();
    value.should.equal('\n');
  });
  
  it('replaceAll', function() {
    let value = cronapi.text.replaceAll.bind(window)(null, 'n', '-', 'c');
    (value === null).should.equal(true);

    value = cronapi.text.replaceAll.bind(window)('banana', null, '-', 'c');
    (value === null).should.equal(true);

    // Erro - Quando o typeReplace é passado como null, ocorre SyntaxError
    // ao instanciar um RegExp com flag nula
    // value = cronapi.text.replaceAll.bind(window)('banana', 'n', null, 'c');
    // (value === null).should.equal(true);

    value = cronapi.text.replaceAll.bind(window)('banana', 'n', '-', null);
    (value === null).should.equal(true);

    value = cronapi.text.replaceAll.bind(window)('banana', 'n', 'i', 'c');
    value.should.equal('bacana');

    // Erro - Quando o typeReplace é passado como '-', é dispara
    // a função mais interna replace em vez de replaceAll
    value = cronapi.text.replaceAll.bind(window)('banana', 'n', '-', 'c');
    value.should.equal('bacaca');
  });

  it('formatTextWithReplacement', function() {
    let value = cronapi.text.formatTextWithReplacement.bind(window)("Hoje é {0} do mês {1} e do ano {2}", "31", "12", "2050");
    value.should.equal("Hoje é 31 do mês 12 e do ano 2050");
  });

});