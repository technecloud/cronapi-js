describe('Test suit for category Screen from Cronapi.js', function() {

    const chai = require('chai');
    chai.should();
    let {window} = require('../../../../../cronapi');
    const cronapi = window["cronapi"];
    let dataSource;
    window.$ = require('jquery');
    const $ = window.$;

    afterEach(() => {
        jest.resetAllMocks();
    });

    beforeAll(() => {
        dataSource = { 
            data: [],
            $apply: function (fun) {
                if (fun instanceof Function) {
                    fun();
                }
            },
            startInserting: () => { dataSource.data.push({ company: 'Techne', category: 'Cronapp' }); },
            startEditing: () => { dataSource.data.company = 'Bahia'; },
            previous: jest.fn(),
            next: jest.fn(),
            removeSilent: jest.fn(),
            refreshActive: jest.fn(),
            hasNext: () => { return true },
            postSilent: () => { return dataSource.data[0].category; },
            isOData: () => { return true },
            search: jest.fn(),
            fetch: jest.fn(),
            hasNextPage: () => { return true },
            nextPage: jest.fn()
        }
        return dataSource;
    });

    it('isInsertingMode', () => {
        let datasource = {};

        (cronapi.screen.isInsertingMode(datasource) == undefined).should.equal(true);

        datasource['inserting'] = true;
        cronapi.screen.isInsertingMode(datasource).should.equal(true);

        datasource['inserting'] = false;
        cronapi.screen.isInsertingMode(datasource).should.equal(false);
    });

    it('isEditingMode', () => {
        let datasource = {};

        (cronapi.screen.isEditingMode(datasource) == undefined).should.equal(true);

        datasource['editing'] = true;
        cronapi.screen.isEditingMode(datasource).should.equal(true);

        datasource['editing'] = false;
        cronapi.screen.isEditingMode(datasource).should.equal(false);
    });

    it('changeTitleScreen', () => {
        (window.document.title).should.equal('');

        cronapi.screen.changeTitleScreen();
        (window.document.title).should.equal('undefined');

        cronapi.screen.changeTitleScreen('Cronapp Low Code');
        (window.document.title).should.equal('Cronapp Low Code');
    });

    it('fieldFromScreen', () => {
        (cronapi.screen.fieldFromScreen() == undefined).should.equal(true);
        (cronapi.screen.fieldFromScreen(null) == null).should.equal(true);
        cronapi.screen.fieldFromScreen('Cronapp').should.equal('Cronapp');
    });

    it('changeValueOfField', () => {
        window.__name = 'Cronapp';
        window.__name.should.equal('Cronapp');
        cronapi.screen.changeValueOfField.bind(window)('cronapi.__name', 'Bahia');
        /**
         * O valor do Campo não foi trocado
         */
        window.__name.should.equal('Bahia');
    });

    it('getValueOfField', () => {
        (cronapi.screen.getValueOfField.bind(window)() == '').should.equal(true);
        
        (cronapi.screen.getValueOfField.bind(window)('cronapi.__name') == 'Cronapp').should.equal(true);

        /** 
         * Ao passar um field não encontrado, a lógica não segue o fluxo correto do método
         * if(fieldValue !== undefined || fieldValue !== null)
         * Como não foi encontrado a chave, a condição vai ficar FALSE || TRUE = TRUE, logo
         * será sempre retornado UNDEFINED
         */
        (cronapi.screen.getValueOfField.bind(window)('cronapi.__city') == '').should.equal(true);
    });

    it('createScopeVariable', () => {
        cronapi['$scope'] = {}
        cronapi.$scope['vars'] = {};
        cronapi.screen.createScopeVariable.bind(window)('cronapp', 'low code');
        JSON.stringify(cronapi.$scope.vars).should.equal('{"cronapp":"low code"}');

        cronapi.screen.createScopeVariable.bind(window)('hello', 'world');
        cronapi.$scope.vars.hello.should.equal('world');
    });

    it('getScopeVariable', () => {
        cronapi.screen.getScopeVariable.bind(window)('cronapp').should.equal('low code');

        cronapi.screen.getScopeVariable.bind(window)('hello').should.equal('world');

        (cronapi.screen.getScopeVariable.bind(window)('teste') == undefined).should.equal(true);

        cronapi.screen.createScopeVariable.bind(window)('logico', false);
        cronapi.screen.getScopeVariable.bind(window)('logico').should.equal(false);
    });

    it('notify', () => {
        cronapi['$scope'] = {};
        cronapi.$scope['Notification'] = function (json, type) {
            this.json = json;
            this.type = type;
        }
        cronapi.screen.notify.bind(window)();
        JSON.stringify(cronapi.$scope.json).should.equal('{"message":""}');
        cronapi.screen.notify.bind(window)('Successful', 'Cronapp is The Best!');
        JSON.stringify(cronapi.$scope.json).should.equal('{"message":"Cronapp is The Best!"}');
        cronapi.$scope.type.should.equal('Successful');
    });

    it('datasourceFromScreen', () => {
        cronapi.screen.startInsertingMode.bind(window)(dataSource);
        cronapi.screen.datasourceFromScreen(dataSource).should.equal(dataSource);
        cronapi.screen.datasourceFromScreen(dataSource.data[0].company).should.equal('Techne');
    });

    it('startInsertingMode', () => {
        cronapi.screen.startInsertingMode.bind(window)(dataSource);
        dataSource.data[0].company.should.equal('Techne');
        dataSource.data[0].category.should.equal('Cronapp');
    });

    it('startEditingMode', () => {
        cronapi.screen.startEditingMode.bind(window)(dataSource);
        dataSource.data[0].company.should.equal('Techne');
    });

    it('previusRecord', () => {
        cronapi.screen.previusRecord.bind(window)(dataSource);
        expect(dataSource.previous).toHaveBeenCalledTimes(1);
    });

    it('nextRecord', () => {
        cronapi.screen.nextRecord.bind(window)(dataSource);
        expect(dataSource.next).toHaveBeenCalledTimes(1);
    });

    it('firstRecord', () => {
        cronapi.screen.firstRecord.bind(window)(dataSource);
        expect(dataSource.next).toHaveBeenCalledTimes(1);
    });

    it('lastRecord', () => {
        cronapi.screen.lastRecord.bind(window)(dataSource);
        expect(dataSource.next).toHaveBeenCalledTimes(1);
    });

    it('removeRecord', () => {
        cronapi.screen.removeRecord.bind(window)(dataSource);
        expect(dataSource.removeSilent).toHaveBeenCalledTimes(1);
    });

    it('refreshActiveRecord', () => {
        cronapi.screen.refreshActiveRecord.bind(window)(dataSource);
        expect(dataSource.refreshActive).toHaveBeenCalledTimes(1);
    });

    it('hasNextRecord', () => {
        cronapi.screen.hasNextRecord.bind(window)(dataSource).should.equal(true);
    });

    it('quantityRecords', () => {
        cronapi.screen.quantityRecords.bind(window)(dataSource).should.equal(2);
    });

    it('post', () => {
        cronapi.screen.post.bind(window)(dataSource).should.equal('Cronapp');
    });

    it('filter', () => {
        cronapi.screen.filter.bind(window)(dataSource, 'oData');
        expect(dataSource.search).toBeCalledTimes(1);
    
        let data = {
            isOData: () => {
                return false;
            },
            filter: jest.fn()
        }
        cronapi.screen.filter.bind(window)(data, 'any');
        expect(data.filter).toBeCalledTimes(1);
    });

    it('changeView', () => {
        const { location } = window;
        delete window.location;
        window.location = { reload: jest.fn() };
        window.alert = jest.fn();

        let listParams = [
            { company: 'Cronapp' }, { category: 'LowCode' }, { date: new Date() }
        ];

        cronapi.$scope['$state'] = {
            get() {
                return [ { templateUrl: 'views/login.view.html', url: 'login.view.html' } ];
            },
        };

        cronapi.screen.changeView.bind(window)('view/user/login', listParams);

        expect(window.location.reload).not.toHaveBeenCalled();

        cronapi.$scope['$state'] = {
            get() {
                return [ { templateUrl: () => {
                    'views/login.view.html'
                }, url: 'login.view.html' } ];
            },
        };

        cronapi.screen.changeView.bind(window)('view/user/login', listParams);

        expect(window.location.reload).toBeCalledTimes(1);

        cronapi.screen.changeView.bind(window)('view/user/login');

        expect(window.location.reload).toBeCalledTimes(2);

        cronapi.$scope['$state'] = {
            get() {
                return [ { templateUrl: 'none', url: 'none' } ];
            },
        };

        cronapi.screen.changeView.bind(window)('view/user/login');

        expect(window.location.reload).toBeCalledTimes(2);

        cronapi.screen.changeView.bind(window)('view/user/login?');

        expect(window.location.reload).toBeCalledTimes(3);

        expect(window.alert).toBeCalledTimes(1);

        window.location = location;
    });

    it('openUrl', () => {
        window.open = jest.fn();

        cronapi.screen.openUrl.bind(window)('google.com.br', true, 500, 500);
        expect(window.open).toHaveBeenCalled();

        cronapi.screen.openUrl.bind(window)('google.com.br', 'TRUE', 500, 500);
        expect(window.open).toHaveBeenCalledTimes(2);

        jest.clearAllMocks();

        cronapi.screen.openUrl('google.com.br', 'TRUE', 'Cronapp', 500);
    });

    it('getParam', () => {
        const { location } = window;
        delete window.location;

        (cronapi.screen.getParam() == null).should.equal(true);
        
        Object.defineProperty(window, 'location', {
            value: {
                href: 'http://example.com/path?name=Techne&products=[Cronapp,LowCode,Dev]&state=Bahia%20Brasil'
            }
        });

        cronapi.screen.getParam('state').should.equal('Bahia Brasil');

        expect(cronapi.screen.getParam('products')).toContain('Cronapp');

        window.location = location;
    });

    it('confimDialog', () => {
        window.confirm = jest.fn();

        cronapi.screen.confimDialog('Cronapp');
        expect(window.confirm).toHaveBeenCalled();
        expect(window.confirm).toHaveBeenCalledTimes(1);
    })

    it('createDefaultModal', () => {

    })

    it('showModal', () => {

    });

    it('setActiveTab', () => {

    });

    it('hideModal', () => {
        document.body.innerHTML =
        '<div id="myModal" class="modal">'
            '<div class="modal-content">'
                '<p>Some text in the Modal..</p>'
            '</div>'
        '</div>'

        cronapi.screen.hideModal('myModal');
        
    });

    it('showIonicModal', () => {

    });

    it('hideIonicModal', () => {

    });

    it('isShownIonicModal', () => {

    });

    it('showLoading', () => {

    });

    it('hide', () => {

    });

    it('getHostapp', () => {
        (cronapi.screen.getHostapp() == undefined).should.equal(true);
        window.hostApp = 'Cronapp';
        cronapi.screen.getHostapp().should.equal('Cronapp');
    });

    it('searchIds', () => {

    });

    it('showComponent', () => {
        document.body.innerHTML = '<div id="cronapp">Low Code</div>';
        $("#cronapp").get(0).style.getPropertyValue('display').should.equal('');
        cronapi.screen.hideComponent('cronapp');
        $("#cronapp").get(0).style.getPropertyValue('display').should.equal('none');
        cronapi.screen.showComponent('cronapp');
        $("#cronapp").get(0).style.getPropertyValue('display').should.equal('block');
    });

    it('hideComponent', () => {
        document.body.innerHTML = '<div id="cronapp">Low Code</div>';
        $("#cronapp").get(0).style.getPropertyValue('display').should.equal('');
        cronapi.screen.showComponent('cronapp');
        $("#cronapp").get(0).style.getPropertyValue('display').should.equal('block');
        cronapi.screen.hideComponent('cronapp');
        $("#cronapp").get(0).style.getPropertyValue('display').should.equal('none');
    });

    it('disableComponent', () => {

    });

    it('enableComponent', () => {

    });

    it('focusComponent', () => {
        
    });

    it('changeAttrValue', () => {
        document.body.innerHTML = '<img id="cronapp" width="300" height="300">';
        cronapi.screen.changeAttrValue('cronapp', 'width', 500);
        $('#cronapp').attr('width').should.equal('500');
    });

    it('changeContent', () => {
        
    });

    it('logout', () => {
        cronapi.$scope['logout'] = jest.fn();
        cronapi.screen.logout.bind(window)();
        expect(cronapi.$scope.logout).toHaveBeenCalledTimes(1);
    });

    it('refreshDatasource', () => {

    });

    it('loadMore', () => {
        cronapi.screen.loadMore(dataSource);
        expect(dataSource.nextPage).toHaveBeenCalledTimes(1);
    });

    it('hasNextPage', () => {
        cronapi.screen.hasNextPage(dataSource).should.equal(true);
    });

    it('load', () => {
        cronapi.screen.load(dataSource);
        expect(dataSource.fetch).toHaveBeenCalledTimes(1);
    });

}); 