/**
 *
 * File: cronapi-test.js
 * Author: Wesley Rover
 * Description: arquivo responsável por executar as 4 operações: + - * /
 * Date: 07/10/2019
 *
 */

/* eslint import/no-extraneous-dependencies: ["error", {"peerDependencies": true}] */
const assert = require('chai');
const cronapi = require('../../cronapi');

// eslint-disable-next-line no-undef
describe('Diferentes Tipos de Testes com Mocha & Chai:', () => {
  // eslint-disable-next-line no-undef
  it('Teste: Deve retornar a frase: "Mocha & Chai são legais!"', () => {
    assert.equal(cronapi(), 'Mocha & Chai são legais!');
  });
});
