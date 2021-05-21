describe('Test suit for category Notification from Cronapi.js', function () {

    const chai = require('chai');
    let { window } = require('../../../../../cronapi');
    const cronapi = window["cronapi"];
    chai.should();

    it('confimDialogAlert', () =>{
        cronapi.screen.confimDialogAlert.bind(window)();
    });

    it('buttonConfirmDialogAlert', () =>{
        cronapi.screen.confimDialogAlert.bind(window)();
    }); 

    it('destroyConfirmDialogAlert', () =>{
        cronapi.screen.confimDialogAlert.bind(window)();
    });  


});