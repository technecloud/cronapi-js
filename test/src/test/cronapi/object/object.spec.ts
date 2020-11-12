describe('Test suit for category object from Cronapi.js',
function() {
  const ch = require('chai');
  let {window} = require('../../../../../cronapi');
  ch.should();
  const cronapi = window["cronapi"];

  it('getProperty', function() {
    let user = {};
    user['username'] = 'admin';
    user['password'] = '123';
    user['info'] = {name: 'Administrator', birthday: undefined};
    
    let value = cronapi.object.getProperty.bind(window)(user, 'username');
    value.should.equal('admin');
    
    value = cronapi.object.getProperty.bind(window)(user, 'info.name');
    value.should.equal('Administrator');

    value = cronapi.object.getProperty.bind(window)(user, 'info.birthday');
    (JSON.stringify(value)).should.equal('{}');
  });

  it('setProperty', function() {
    let user = {};
    user['username'] = 'admin';
    user['password'] = '123';
    user['info'] = {name: 'Administrator', birthday: '01/01/2000'};
    
    cronapi.object.setProperty.bind(window)(user, 'username', 'john');
    let value = cronapi.object.getProperty.bind(window)(user, 'username');
    value.should.equal('john');

    cronapi.object.setProperty.bind(window)(user, 'info.name', 'John');
    value = cronapi.object.getProperty.bind(window)(user, 'info.name');
    value.should.equal('John');
    
    user['info'] = {name: 'Administrator', birthday: undefined};
    cronapi.object.setProperty.bind(window)(user, 'info.birthday', undefined);
    value = cronapi.object.getProperty.bind(window)(user, 'info.birthday');
    (JSON.stringify(value)).should.equal('{}');
  });

  it('createObjectFromString', function() {
    let value = cronapi.object.createObjectFromString.bind(window)('{"user": {"username": "admin"}}');
    (JSON.stringify(value)).should.equal('{"user":{"username":"admin"}}');
  });

  it('createObjectLoginFromString', function() {
    let value = cronapi.object.createObjectLoginFromString.bind(window)("admin", 123);
    (JSON.stringify(value)).should.equal('{"username":"admin","password":123}');
  });

  it('serializeObject', function() {
    let value = cronapi.object.serializeObject.bind(window)({username:"admin", password: 123});
    value.should.equal('{"username":"admin","password":123}');
  });

  it('deleteProperty', function() {
    let user = {};
    user['username'] = 'admin';
    user['password'] = '123';
    user['info'] = {name: 'Administrator', birthday: '01/01/2000'};

    cronapi.object.deleteProperty.bind(window)(user, "info");
    let value = cronapi.object.getProperty(user, "info");
    (value === undefined).should.equal(true);
  });

  it('newObject', function() {
    let user = {};
    user['username'] = 'admin';
    user['password'] = '123';
    user['info'] = {name: 'Administrator', birthday: '01/01/2000'};

    let value = cronapi.object.newObject.bind(window)();
    (JSON.stringify(value)).should.equal('{}');

    value = cronapi.object.newObject.bind(window)({username: 'admin'});
    (JSON.stringify(value)).should.equal('{}');

    value = cronapi.object.newObject.bind(window)({name: 'username', value: 'admin'});
    (JSON.stringify(value)).should.equal('{"username":"admin"}');
  });

  it('getObjectField', function() {
    let user = {};
    user['username'] = 'admin';

    let value = cronapi.object.getObjectField.bind(window)(user, null);
    (value === undefined).should.equal(true);

    value = cronapi.object.getObjectField.bind(window)(user, undefined);
    (value === undefined).should.equal(true);

    value = cronapi.object.getObjectField.bind(window)(null, 'username');
    (value === undefined).should.equal(true);

    value = cronapi.object.getObjectField.bind(window)(undefined, 'username');
    (value === undefined).should.equal(true);

    value = cronapi.object.getObjectField.bind(window)(user, 'username');
    value.should.equal('admin');
  });

});