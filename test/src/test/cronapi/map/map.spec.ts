
describe('Test suit for category map from Cronapi.js',
function() {

  const ch = require('chai');
  let {window} = require('../../../../../cronapi');
  ch.should();
  const cronapi = window["cronapi"];

  let mapCreated;

  beforeAll(() => {
    let map = [
      {"param1" : {"name":"John", "age":30, "car":null}},
      {"param2" : {"name":"Marie"}},
      {"param3" : "Chic"},
    ]
    mapCreated = cronapi.map.createMap.bind(window)(map);
  });

  it('createMap', function() {
    (mapCreated[0].param1.car === null).should.equal(true);
    (mapCreated[0].param1.age === 30).should.equal(true);
    (mapCreated[1].param2.name === "Marie").should.equal(true);
    (mapCreated[2].param3 === "Chic").should.equal(true);
  });

  it('setMapValueByKey', function() {
    cronapi.map.setMapValueByKey.bind(window)(mapCreated, "param4", "Brasil");
    (mapCreated[3].param4 === "Brasil").should.equal(true);
  });

  it('setMapValueByPath', function() {
    cronapi.map.setMapValueByPath.bind(window)(mapCreated, "param5.lowcode.platform", "Cronapp");
    (mapCreated[4].param5 !== null).should.equal(true);
    (mapCreated[4].param5.lowcode !== null).should.equal(true);
    (mapCreated[4].param5.lowcode.platform === "Cronapp").should.equal(true);
  });

  it('getMapValueByPath', function() {
    const result = cronapi.map.getMapValueByPath.bind(window)(mapCreated, "param5.lowcode.platform");
    (result !== null).should.equal(true);
    (result === "Cronapp").should.equal(true);
  });

  it('getMapValueByKey', function() {
    const result = cronapi.map.getMapValueByKey.bind(window)(mapCreated, "param3");
    (result !== null).should.equal(true);
    (result === "Chic").should.equal(true);
  });

});