describe('Test suit for category json from Cronapi.js',
function() {
  const ch = require('chai');
  let {window} = require('../../../../../cronapi');
  ch.should();
  const cronapi = window["cronapi"];

  it('createObjectFromString', function() {
    let value = cronapi.json.createObjectFromString.bind(window)('{"user": {"username": "admin"}}');
    (JSON.stringify(value)).should.equal('{"user":{"username":"admin"}}');
  });

  it('setProperty', function() {
    let user = {};
    user['username'] = 'admin';
    user['password'] = '123';
    user['info'] = {name: 'Administrator', birthday: '01/01/2000'};
    
    cronapi.json.setProperty.bind(window)(user, 'username', 'john');
    let value = cronapi.json.getProperty.bind(window)(user, 'username');
    value.should.equal('john');

    cronapi.json.setProperty.bind(window)(user, 'info.name', 'John');
    value = cronapi.json.getProperty.bind(window)(user, 'info.name');
    value.should.equal('John');
    
    user['info'] = {name: 'Administrator', birthday: undefined};
    cronapi.json.setProperty.bind(window)(user, 'info.birthday', undefined);
    value = cronapi.json.getProperty.bind(window)(user, 'info.birthday');
    (JSON.stringify(value)).should.equal('{}');
  });

  it('deleteProperty', function() {
    let user = {};
    user['username'] = 'admin';
    user['password'] = '123';
    user['info'] = {name: 'Administrator', birthday: '01/01/2000'};

    cronapi.json.deleteProperty.bind(window)(user, "info");
    let value = cronapi.json.getProperty.bind(window)(user, "info");
    (value === undefined).should.equal(true);
  });

  it('getProperty', function() {
    let user = {};
    user['username'] = 'admin';
    user['password'] = '123';
    user['info'] = {name: 'Administrator', birthday: undefined};
    
    let value = cronapi.json.getProperty.bind(window)(user, 'username');
    value.should.equal('admin');
    
    value = cronapi.json.getProperty.bind(window)(user, 'info.name');
    value.should.equal('Administrator');

    value = cronapi.json.getProperty.bind(window)(user, 'info.birthday');
    (JSON.stringify(value)).should.equal('{}');
  });
});