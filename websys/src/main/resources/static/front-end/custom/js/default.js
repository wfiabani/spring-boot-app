String.prototype.validDate = function(){
    var regex = /[0-9]{2}\/[0-9]{2}\/[0-9]{4}/;
    return regex.test(this);
}
String.prototype.validMonth = function(){
    var regex = /[0-9]{2}\/[0-9]{4}/;
    return regex.test(this);
}


String.prototype.validDayOfMonth = function(){
    return (Number(this)>0 && Number(this)<32);
}

Number.prototype.round = function(decimals) {
    return Number(Math.round(this+'e'+decimals)+'e-'+decimals);
}

Vue.prototype.$eventHub = new Vue();


var validation = function( prop, validationType ){
    var validClassName = '';
    var invalidClassName = 'is-invalid';
    switch(validationType){
        case 'required':
        return (prop!==null && prop.length > 0) ? validClassName : invalidClassName;
        break;
        
        case 'date':
        return prop.validDate() ? validClassName : invalidClassName;
        break;
        
        case 'month':
        return prop.validMonth() ? validClassName : invalidClassName;
        break;
        
        case 'dayOfMonth':
        return prop.toString().validDayOfMonth() ? validClassName : invalidClassName;
        break;
        
        case 'dateNotRequired':
        return (prop.validDate() || prop=="") ? validClassName : invalidClassName;
        break;
        
        case 'money':
        return (typeof prop)==="number";// && prop>0;
        break;
        
        default:
        return prop.length > 0 ? validClassName : invalidClassName;
        break;
    }
}




Vue.component('add-skus', {
    template: '#add-skus',
    props: ['pid']
});

Vue.component('add-cadastros', {
    template: '#add-cadastros'
});

Vue.component('add-products', {
    template: '#add-products'
});

Vue.component('add-movements', {
    template: '#add-movements',
    props: ['transaction', 'sale']
});

Vue.component('add-servicos', {
    template: '#add-servicos'
});







Vue.component('estoque', {
    template: '#estoque',
    data: function(){
        return {
            skus: [],
            
            sending: false,
        };
    },
    created: function(){
        this.load();
    },
    methods: {
        
        load: function(){
            $.ajax({
                type: "POST",
                url: BASEURL+"skus/deficit-stock",
                dataType: 'json',
                success: function(data){
                    this.skus = data;
                }.bind(this),
            });
        },
    }
});



Vue.component('home', {
    template: '#home',
    data: function(){
        return {
            contasPagar: [],
            contasReceber: [],
            resumo: {},
            open: true,
            paid: true,
            
            month: '',
            
            sending: false,
        };
    },
    created: function(){
        this.load();
    },
    methods: {
        
        load: function(){
            $.ajax({
                type: "POST",
                url: BASEURL+'service/accounts',
                dataType: 'json',
                data:{
                    date: this.month
                },
                success: function(data){
                    this.contasPagar = data['contasPagar'];
                    this.contasReceber = data['contasReceber'];
                    this.resumo = data['resumo'];
                    this.month = data['date'];
                    this.sending = false;
                }.bind(this),
            });
            
        },
        
        getClassValidation: validation.bind(this),
        
        validForm: function(){
            return (
                this.month.validMonth()
                );
            },
            
            checkForm: function(event){
                event.preventDefault();
                
                if( !this.sending && this.validForm() ){
                    this.sending = true;
                    this.load();
                }
            },
            
            
            getFinancesReport: function(event){
                event.preventDefault();
                
                /*
                var obj = {
                    contasPagar: this.contasPagar,
                    contasReceber: this.contasReceber,
                    open: this.open,
                    paid: this.paid,
                    month: this.month,
                }*/
                
                if( !this.sending && this.validForm() ){
                    this.sending = true;
                    
                    $.ajax({
                        type: "POST",
                        url: BASEURL+'service/get-finances-report/',
                        dataType: 'json',
                        data: {
                            scope: this.month,
                        },
                        success: this.handleResult,
                        error: this.handleResult
                    });
                }
                
                
            },
            
            handleResult: function(data){
                this.sending = false;
                window.open( BASEURL+'service/download-report/'+data.fileName );
            },
            
            
            editAccount: function(id){
                popup('Editar Conta', '<add-account v-bind:pk="'+id+'"></add-account>', this.checkForm);
            },
            
            editFixedAccount: function(fixedAccountId, scope){
                popup('Adicionar Pagamento', '<add-payment v-bind:fixed-account-id="'+fixedAccountId+'" v-bind:scope="\''+scope+'\'"></add-payment>', this.checkForm);
            },
            
            buttonText: function(){
                return this.sending ? 'Aguarde...' : 'Aplicar';
            }
        }
    });
    
    
    
    
    
    
    
    Vue.component('xml-loader', {
        template: '#xml-loader',
        data: function(){
            return {
                objNFe: [],
                sending: false,
            };
        },
        created: function(){
            this.load();
        },
        methods: {
            
            load: function(){
                /*
                $.ajax({
                    type: "POST",
                    url: BASEURL+"skus/deficit-stock",
                    dataType: 'json',
                    success: function(data){
                        this.skus = data;
                    }.bind(this),
                });
                */
            },
            
            validForm: function(){
                return true;
            },
            
            checkForm: function(event){
                event.preventDefault();
                
                var files = document.getElementById("xmlFile").files;
                var formData = new FormData();
                for (var i = 0; i < files.length; i++) {
                    var file = files[i];
                    /*
                    // Check the file type.
                    if (!file.type.match('image.*')) {
                        continue;
                    }
                    */
                    // Add the file to the request.
                    formData.append('file', file, file.name);
                    console.log(file.name)
                    break;
                }
                var xhr = new XMLHttpRequest();
                xhr.responseType = 'json';
                xhr.open('POST', BASEURL+"nfe/parse-xml", true);
                xhr.onload = function () {
                    this.sending = false;
                    if (xhr.status === 200) {
                        var obj = xhr.response;
                        document.infosNFe = obj.infos;
                        document.itemsNFe = obj.items;
                        popup('Ajustar Estoque', '<ajustar-estoque></ajustar-estoque>', function(){});
                    }
                }.bind(this);
                xhr.send(formData);
                
                /*
                if( !this.sending && this.validForm() ){
                    this.sending = true;
                }*/
            },
            
            buttonText: function(){
                return this.sending ? 'Aguarde...' : 'Importar XML';
            }
        }
    });



    Vue.component('resumo-financeiro-sintetico', {
            template: '#resumo-financeiro-sintetico',
            data: function(){
                return {
                    months: [],
                    competences: [],
                    maxValue: 0,
                    sending: false,
                    initDate: '',
                    endDate: ''
                };
            },

            created: function(){
                //this.load();
            },
            methods: {

                getClassValidation: validation.bind(this),

                load: function(){
                    this.months = [];
                    var initDate = this.getDate(this.initDate);
                    console.log(initDate)
                    var endDate = this.getDate(this.endDate);
                    while(initDate < endDate){
                        console.log(this.getFormattedMonth(initDate))
                        this.months.push( this.getFormattedMonth(initDate) )
                        initDate.setMonth(initDate.getMonth() + 1);
                    }
                    this.months.push( this.getFormattedMonth(endDate) )

                    this.competences = [];
                    this.maxValue = 0;
                    this.loadCompetence();
                },

                loadCompetence: function(){
                    if(this.competences.length < this.months.length){
                        $.ajax({
                            type: "POST",
                            url: BASEURL+'service/accounts',
                            dataType: 'json',
                            data:{ date: this.months[this.competences.length] },
                            success: function(data){
                                var totalR = parseFloat(data['resumo'].totalReceber.replace("," , "."));
                                var totalP = parseFloat(data['resumo'].totalPagar.replace("," , "."));
                                this.maxValue = totalR>this.maxValue ? totalR : this.maxValue;
                                this.maxValue = totalP>this.maxValue ? totalP : this.maxValue;
                                this.competences.push({
                                    month: data['date'],
                                    resumo: data['resumo'],
                                    percentP: 0,
                                    percentR: 0
                                });
                                this.loadCompetence();
                            }.bind(this),
                        });
                    }else{
                        this.competences.forEach(function(value, index, array){
                            var nValueR = parseFloat(value.resumo.totalReceber.replace("," , "."));
                            var nValueP = parseFloat(value.resumo.totalPagar.replace("," , "."));
                            this.competences[index].percentP = (nValueP*100)/this.maxValue
                            this.competences[index].percentR = (nValueR*100)/this.maxValue
                        }.bind(this));
                        this.sending = false;
                        console.log(this.competences)
                    }
                },

                validForm: function(){
                    var initDate = this.getDate(this.initDate);
                    var endDate = this.getDate(this.endDate);
                    return initDate < endDate;
                },

                getDate: function(month){
                    var date = new Date();
                    date.setDate(1);
                    date.setMonth( parseInt(month.split("/")[0])-1 );
                    date.setYear( month.split("/")[1] );
                    return date;
                },

                getFormattedMonth: function(date){
                    console.log("-----------")
                    console.log(date)
                    var month = (date.getMonth()+1).toString();
                    month = month.length==2 ? month : "0"+month;
                    var year = date.getFullYear();
                    console.log(month+"/"+year);
                    console.log("-----------")
                    return month+"/"+year;
                },

                checkForm: function(event){
                    event.preventDefault();
                    if( !this.sending && this.validForm() ){
                        this.sending = true;
                        this.load();
                    }
//
//                    var files = document.getElementById("xmlFile").files;
//                    var formData = new FormData();
//                    for (var i = 0; i < files.length; i++) {
//                        var file = files[i];
//                        /*
//                        // Check the file type.
//                        if (!file.type.match('image.*')) {
//                            continue;
//                        }
//                        */
//                        // Add the file to the request.
//                        formData.append('file', file, file.name);
//                        console.log(file.name)
//                        break;
//                    }
//                    var xhr = new XMLHttpRequest();
//                    xhr.responseType = 'json';
//                    xhr.open('POST', BASEURL+"nfe/parse-xml", true);
//                    xhr.onload = function () {
//                        this.sending = false;
//                        if (xhr.status === 200) {
//                            var obj = xhr.response;
//                            document.infosNFe = obj.infos;
//                            document.itemsNFe = obj.items;
//                            popup('Ajustar Estoque', '<ajustar-estoque></ajustar-estoque>', function(){});
//                        }
//                    }.bind(this);
//                    xhr.send(formData);
//
                    /*
                    if( !this.sending && this.validForm() ){
                        this.sending = true;
                    }*/
                },

                buttonText: function(){
                    return this.sending ? 'Aguarde...' : 'Aplicar';
                }
            }
        });
    
    
    
    Vue.component('ajustar-estoque', {
        template: '#ajustar-estoque',
        data: function(){
            return {
                infos: {},
                items: [],
                action: 0, // 0:entrada. 1:saída. 2:nenhuma ação
                skuTempData: [],
                actions: [
                    { text: 'Adicionar ao Estoque', value: 0 },
                    { text: 'Diminuir do Estoque', value: 1 },
                    { text: 'Não Alterar Estoque', value: 2 },
                ],
                sending: false,
            };
        },
        created: function(){
            
            
            //configura produtos events
            this.$eventHub.$off('select-sku');
            this.$eventHub.$on('select-sku', function(id){
                
                $.ajax({
                    type: "POST",
                    url: BASEURL+'skus/get',
                    dataType: 'json',
                    data: {id: id},
                    success: function(data){
                        this.sending = false;
                        this.skuTempData = data;
                        closeLastModalOpen();
                    }.bind(this),
                    error: function(){
                        this.sending = false;
                    }.bind(this)
                });
                
            }.bind(this));
            
            
            this.infos = document.infosNFe;
            this.items = document.itemsNFe;
            delete document.infos;
            delete document.items;
            this.load();
        },
        methods: {
            
            load: function(){
                /*
                $.ajax({
                    type: "POST",
                    url: BASEURL+"skus/deficit-stock",
                    dataType: 'json',
                    success: function(data){
                        this.skus = data;
                    }.bind(this),
                });
                */
            },
            
            //popup para selecionar sku
            changeSKU: function(index){
                console.log(index);
                popup('Produtos', '<list-skus v-bind:movement="true"></list-skus>', function(){
                    this.items[index].skuId = this.skuTempData.id
                    this.items[index].skuExternalId = this.skuTempData.externalId
                    this.items[index].skuName = this.skuTempData.name
                    this.items[index].productName = this.skuTempData.productName
                }.bind(this));
            },
            
            onChangeDefaultAction: function(){
                for(var i=0 ; i<this.items.length ; i++){
                    this.items[i].action = this.action;
                }
            },
            
            validForm: function(){
                return true;
            },
            
            checkForm: function(event){
                event.preventDefault();
                
                var obj = {
                    infos: this.infos,
                    items: this.items,
                }
                
                if( !this.sending && this.validForm() ){
                    this.sending = true;
                    
                    $.ajax({
                        type: "POST",
                        url: BASEURL+'skus/ajustar-estoque/',
                        dataType: 'json',
                        data: {obj: JSON.stringify(obj)},
                        success: this.handleResult,
                        error: this.handleResult
                    });
                }
            },
            
            handleResult: function(data){
                this.sending = false;
                console.log(data);
                closeLastModalOpen();
                alert("Ações executadas com sucesso!");
            },
            
            buttonText: function(){
                return this.sending ? 'Aguarde...' : 'Executar Ações';
            }
        }
    });
    
    
    
    
    
    
    
    
    
    
    Vue.component('add-payment', {
        template: '#add-payment',
        props: ['fixedAccountId', 'scope'],
        data: function(){
            return {
                id: 0,
                
                value: 0.0, //valor pago, pode ser diferente do original
                accountValue: 0.0, //valor original da conta
                
                sending: false,
                paid: false, //controla se a conta pode ou não ser paga
            };
        },
        created: function(){
            this.load();
        },
        methods: {
            
            load: function(){
                $.ajax({
                    type: "POST",
                    url: BASEURL+'fixed-accounts/get',
                    dataType: 'json',
                    data: {
                        id: this.fixedAccountId,
                    },
                    success: function(data){
                        this.value = data.value;
                        if(this.accountValue==0){
                            this.accountValue = data.value;
                        }
                        $.ajax({
                            type: "POST",
                            url: BASEURL+'fixed-accounts/verify-payment',
                            dataType: 'json',
                            data: {
                                fixedAccountId: this.fixedAccountId,
                                scope: this.scope,
                            },
                            success: function(data){
                                
                                this.sending = false;
                                
                                if(data.paid=="true" || data.paid=="parcial"){
                                    this.id = data.id;
                                    this.paid = true;
                                    this.value = data.value;
                                    this.scope = data.scope;
                                    this.fixedAccountId = data.fixedAccountId;
                                }else if(data.paid=="false"){
                                    this.paid = false;
                                }else if(data.paid=="error"){
                                    alert("Já existem "+data.size+" pagamentos para esta conta!");
                                }
                            }.bind(this),
                        });
                        
                    }.bind(this),
                });
                
                
            },
            
            
            getClassValidation: validation.bind(this),
            
            validForm: function(){
                return (
                    this.value>0
                    );
                },
                
                removePayment: function(){
                    if(this.id>0 && !this.sending){
                        this.sending = true;
                        $.ajax({
                            type: "POST",
                            url: BASEURL+'fixed-accounts/remove-payment',
                            dataType: 'text',
                            data: {
                                id: this.id
                            },
                            success: function(data){
                                this.id = 0;
                                this.sending = false;
                                this.paid = false;
                                this.load();
                            }.bind(this),
                        });
                    }
                },
                
                checkForm: function(event){
                    event.preventDefault();
                    
                    if( !this.sending && this.validForm() ){
                        this.sending = true;
                        $.ajax({
                            type: "POST",
                            url: BASEURL+'fixed-accounts/add-payment',
                            dataType: 'json',
                            data: {
                                id: this.id,
                                value: this.value,
                                scope: this.scope,
                                fixedAccountId: this.fixedAccountId,
                            },
                            success: function(data){
                                this.id = data.id;
                                this.paid = true;
                                this.sending = false;
                            }.bind(this),
                            error: function(){
                                this.sending = false;
                            }.bind(this),
                        });
                    }
                },
                
                buttonText: function(){
                    return this.sending ? 'Aguarde...' : (this.id > 0 ? 'Salvar' : 'Adicionar');
                }
            }
        });
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        Vue.component('add-product', {
            template: '#add-product',
            props: ['pk'],
            data: function(){
                return {
                    id: 0,
                    price: 0, //truque para ativar a máscara via js
                    cost: 0, //truque para ativar a máscara via js
                    priceUnTrib: 0,
                    name: '',
                    externalId: '',
                    grupoTrib: '',
                    ean: '',
                    eanTrib: '',
                    ncm: '',
                    cest: '',
                    uCom: null,
                    uTrib: null,
                    trackStock: true,
                    productCategoryId: 1,
                    productCategories: [],
                    unidadesComerciais: [
                        { text: 'Selecione', value: null },
                        { text: 'Unidade', value: 'Un' },
                        { text: 'Kilograma', value: 'Kg' },
                        { text: 'Litro', value: 'Lt' },
                        { text: 'Caixa', value: 'Cx' },
                    ],
                    
                    sending: false,
                };
            },
            created: function(){
                this.load(this.pk);
                try{
                    $(".form-group:first-child input[type=text]").get(0).focus();
                }catch(e){}
            },
            methods: {
                
                load: function(id){
                    $.ajax({
                        type: "POST",
                        url: BASEURL+'service/categories',
                        dataType: 'json',
                        success: function(data){
                            this.productCategories = data;
                            if(id > 0){
                                $.ajax({
                                    type: "POST",
                                    url: BASEURL+'products/get',
                                    dataType: 'json',
                                    data: {
                                        id: id,
                                    },
                                    success: function(data){
                                        this.id = data.id;
                                        this.price = data.price;
                                        this.cost = data.cost;
                                        this.priceUnTrib = data.priceUnTrib;
                                        this.name = data.name;
                                        this.externalId = data.externalId;
                                        this.grupoTrib = data.grupoTrib;
                                        this.ean = data.ean;
                                        this.eanTrib = data.eanTrib;
                                        this.ncm = data.ncm;
                                        this.cest = data.cest;
                                        this.uCom = data.uCom;
                                        this.uTrib = data.uTrib;
                                        this.productCategoryId = data.productCategoryId;
                                        this.trackStock = data.trackStock;
                                    }.bind(this),
                                });
                            }
                        }.bind(this),
                    });
                    
                    
                },
                
                getClassValidation: validation.bind(this),
                
                validForm: function(){
                    return (
                        this.name.length>0 &&
                        this.uCom!==null
                        );
                    },
                    
                    checkForm: function(event){
                        event.preventDefault();
                        
                        if( !this.sending && this.validForm() ){
                            this.sending = true;
                            $.ajax({
                                type: "POST",
                                url: BASEURL+'products/add/',
                                dataType: 'json',
                                data: {
                                    id: this.id,
                                    price: this.price,
                                    cost: this.cost,
                                    priceUnTrib: this.priceUnTrib,
                                    name: this.name,
                                    externalId: this.externalId,
                                    grupoTrib: this.grupoTrib,
                                    ean: this.ean,
                                    eanTrib: this.eanTrib,
                                    ncm: this.ncm,
                                    cest: this.cest,
                                    uCom: this.uCom,
                                    uTrib: this.uTrib,
                                    productCategoryId: this.productCategoryId,
                                    trackStock: this.trackStock,
                                },
                                success: function(data){
                                    this.id = data.id;
                                    this.price = data.price;
                                    this.cost = data.cost;
                                    this.priceUnTrib = data.priceUnTrib;
                                    this.name = data.name;
                                    this.externalId = data.externalId;
                                    this.grupoTrib = data.grupoTrib;
                                    this.ean = data.ean;
                                    this.eanTrib = data.eanTrib;
                                    this.ncm = data.ncm;
                                    this.cest = data.cest;
                                    this.uCom = data.uCom;
                                    this.uTrib = data.uTrib;
                                    this.productCategoryId = data.productCategoryId;
                                    this.trackStock = data.trackStock;
                                    this.sending = false;
                                }.bind(this),
                                error: function(){
                                    alert("O código informado já está sendo usado em outro produto.");
                                    this.sending = false;
                                }
                            });
                        }
                    },
                    
                    buttonText: function(){
                        return this.sending ? 'Aguarde...' : (this.id > 0 ? 'Salvar' : 'Adicionar');
                    }
                }
            });
            
            
            
            Vue.component('add-account', {
                template: '#add-account',
                props: ['pk', 'mid', 'mtype', 'morcamento'],
                data: function(){
                    return {
                        id: 0,
                        movementId: 0,
                        orcamento: false,
                        name: '',
                        externalId: '',
                        value: 0, //truque para ativar a máscara via js
                        paid: false,
                        expirationDate: '',
                        type: null,
                        types: [
                            { text: 'Selecione', value: null },
                            { text: 'Pagar', value: 'P' },
                            { text: 'Receber', value: 'R' }
                        ],
                        
                        sending: false,
                        
                        lock: false, //controla a exibição do tipo
                    };
                },
                created: function(){
                    if(this.mid>0){
                        this.movementId = this.mid;
                        $.ajax({
                            type: "POST",
                            url: BASEURL+'movements/get',
                            dataType: 'json',
                            data: {
                                id: this.mid,
                            },
                            success: function(data){
                                if(this.name==''){
                                    this.name = "Ref. à "+(data.type=='C' ? 'compra' : 'venda')+" cód "+data.id+" - "+data.cadastroName;
                                }
                                if(this.value==0){
                                    this.value = data.total;
                                }
                                if(this.externalId==''){
                                    this.externalId = data.externalId;
                                }
                            }.bind(this),
                        });
                        
                        this.lock = true;
                    }
                    if(this.mtype=='C' ){
                        this.type = 'P';
                    }else if(this.mtype=='V'){
                        this.type = 'R';
                    }
                    if( (typeof this.morcamento)=="boolean" ){
                        this.orcamento = this.morcamento;
                    }
                    this.load(this.pk);
                    try{
                        $(".form-group:first-child input[type=text]").get(0).focus();
                    }catch(e){}
                },
                methods: {
                    
                    load: function(id){
                        if(id > 0){
                            $.ajax({
                                type: "POST",
                                url: BASEURL+'accounts/get',
                                dataType: 'json',
                                data: {
                                    id: id,
                                },
                                success: function(data){
                                    this.id = data.id;
                                    this.movementId = data.movementId;
                                    this.orcamento = data.orcamento;
                                    this.name = data.name;
                                    this.externalId = data.externalId;
                                    this.value = data.value;
                                    this.expirationDate = data.expirationDate;
                                    this.type = data.type;
                                    this.paid = data.paid;
                                }.bind(this),
                            });
                        }
                    },
                    
                    
                    getClassValidation: validation.bind(this),
                    
                    validForm: function(){
                        return (
                            this.type!==null &&
                            this.expirationDate.validDate() &&
                            this.value>0 
                            );
                        },
                        
                        checkForm: function(event){
                            event.preventDefault();
                            
                            if( !this.sending && this.validForm() ){
                                this.sending = true;
                                $.ajax({
                                    type: "POST",
                                    url: BASEURL+'accounts/add/',
                                    dataType: 'json',
                                    data: {
                                        id: this.id,
                                        movementId: this.movementId,
                                        orcamento: this.orcamento,
                                        name: this.name,
                                        externalId: this.externalId,
                                        value: this.value,
                                        expirationDate: this.expirationDate,
                                        type: this.type,
                                        paid: this.paid,
                                    },
                                    success: function(data){
                                        this.id = data.id;
                                        this.movementId = data.movementId;
                                        this.orcamento = data.orcamento;
                                        this.name = data.name;
                                        this.externalId = data.externalId;
                                        this.value = data.value;
                                        this.expirationDate = data.expirationDate;
                                        this.type = data.type;
                                        this.paid = data.paid;
                                        this.sending = false;
                                    }.bind(this),
                                    error: function(){
                                        //alert("O código informado já está sendo usado em outro produto.");
                                        this.sending = false;
                                    }.bind(this),
                                });
                            }
                        },
                        
                        buttonText: function(){
                            return this.sending ? 'Aguarde...' : (this.id > 0 ? 'Salvar' : 'Adicionar');
                        }
                    }
                });
                
                
                
                Vue.component('add-fixed-account', {
                    template: '#add-fixed-account',
                    props: ['pk'],
                    data: function(){
                        return {
                            id: 0,
                            name: '',
                            externalId: '',
                            value: 0, //truque para ativar a máscara via js
                            expirationDay: 10,
                            enabledDate: '',
                            disabledDate: '',
                            type: null,
                            repeatJAN: true,
                            repeatFEV: true,
                            repeatMAR: true,
                            repeatAPR: true,
                            repeatMAY: true,
                            repeatJUN: true,
                            repeatJUL: true,
                            repeatAGO: true,
                            repeatSET: true,
                            repeatOCT: true,
                            repeatNOV: true,
                            repeatDEC: true,
                            types: [
                                { text: 'Selecione', value: null },
                                { text: 'Pagar', value: 'P' },
                                { text: 'Receber', value: 'R' }
                            ],
                            
                            sending: false,
                        };
                    },
                    created: function(){
                        this.load(this.pk);
                        try{
                            $(".form-group:first-child input[type=text]").get(0).focus();
                        }catch(e){}
                    },
                    methods: {
                        
                        load: function(id){
                            $.ajax({
                                type: "POST",
                                url: BASEURL+'service/categories',
                                dataType: 'json',
                                success: function(data){
                                    //this.productCategories = data;
                                    if(id > 0){
                                        $.ajax({
                                            type: "POST",
                                            url: BASEURL+'fixed-accounts/get',
                                            dataType: 'json',
                                            data: {
                                                id: id,
                                            },
                                            success: function(data){
                                                this.id = data.id;
                                                this.name = data.name;
                                                this.externalId = data.externalId;
                                                this.value = data.value;
                                                this.expirationDay = data.expirationDay;
                                                this.enabledDate = data.enabledDate;
                                                this.disabledDate = data.disabledDate;
                                                this.repeatJAN = data.repeatJAN;
                                                this.repeatFEV = data.repeatFEV;
                                                this.repeatMAR = data.repeatMAR;
                                                this.repeatAPR = data.repeatAPR;
                                                this.repeatMAY = data.repeatMAY;
                                                this.repeatJUN = data.repeatJUN;
                                                this.repeatJUL = data.repeatJUL;
                                                this.repeatAGO = data.repeatAGO;
                                                this.repeatSET = data.repeatSET;
                                                this.repeatOCT = data.repeatOCT;
                                                this.repeatNOV = data.repeatNOV;
                                                this.repeatDEC = data.repeatDEC;
                                                this.type = data.type;
                                            }.bind(this),
                                        });
                                    }
                                }.bind(this),
                            });
                            
                        },
                        
                        
                        getClassValidation: validation.bind(this),
                        
                        validForm: function(){
                            return (
                                this.value>0 && 
                                this.type!=null &&
                                this.enabledDate.validDate() &&
                                (this.disabledDate.validDate || this.disabledDate=="") &&
                                (this.expirationDay.toString().validDayOfMonth())
                                );
                            },
                            
                            checkForm: function(event){
                                event.preventDefault();
                                
                                if( !this.sending && this.validForm() ){
                                    this.sending = true;
                                    $.ajax({
                                        type: "POST",
                                        url: BASEURL+'fixed-accounts/add/',
                                        dataType: 'json',
                                        data: {
                                            id: this.id,
                                            name: this.name,
                                            externalId: this.externalId,
                                            value: this.value,
                                            expirationDay: this.expirationDay,
                                            enabledDate: this.enabledDate,
                                            disabledDate: this.disabledDate,
                                            repeatJAN: this.repeatJAN,
                                            repeatFEV: this.repeatFEV,
                                            repeatMAR: this.repeatMAR,
                                            repeatAPR: this.repeatAPR,
                                            repeatMAY: this.repeatMAY,
                                            repeatJUN: this.repeatJUN,
                                            repeatJUL: this.repeatJUL,
                                            repeatAGO: this.repeatAGO,
                                            repeatSET: this.repeatSET,
                                            repeatOCT: this.repeatOCT,
                                            repeatNOV: this.repeatNOV,
                                            repeatDEC: this.repeatDEC,
                                            type: this.type,
                                        },
                                        success: function(data){
                                            this.id = data.id;
                                            this.name = data.name;
                                            this.externalId = data.externalId;
                                            this.value = data.value;
                                            this.expirationDay = data.expirationDay;
                                            this.enabledDate = data.enabledDate;
                                            this.disabledDate = data.disabledDate;
                                            this.repeatJAN = data.repeatJAN;
                                            this.repeatFEV = data.repeatFEV;
                                            this.repeatMAR = data.repeatMAR;
                                            this.repeatAPR = data.repeatAPR;
                                            this.repeatMAY = data.repeatMAY;
                                            this.repeatJUN = data.repeatJUN;
                                            this.repeatJUL = data.repeatJUL;
                                            this.repeatAGO = data.repeatAGO;
                                            this.repeatSET = data.repeatSET;
                                            this.repeatOCT = data.repeatOCT;
                                            this.repeatNOV = data.repeatNOV;
                                            this.repeatDEC = data.repeatDEC;
                                            
                                            this.sending = false;
                                        }.bind(this),
                                        error: function(){
                                            //alert("O código informado já está sendo usado em outro produto.");
                                            this.sending = false;
                                        }.bind(this),
                                    });
                                }
                            },
                            
                            buttonText: function(){
                                return this.sending ? 'Aguarde...' : (this.id > 0 ? 'Salvar' : 'Adicionar');
                            }
                        }
                    });
                    
                    
                    
                    
                    Vue.component('view-nfe', {
                        template: '#view-nfe',
                        props: ['chave', 'protocolo'],
                        data: function(){
                            return {
                                
                                xNome: '',
                                dhEmi: '',
                                nNF: '',
                                natOp: '',
                                model: '',
                                chNFe: '',
                                nProt: '',
                                just: '',
                                xMotivo: '',
                                cStat: '',
                                
                                to: '',
                                
                                sending: false,
                            };
                        },
                        created: function(){
                            this.load();
                        },
                        methods: {
                            
                            load: function(){
                                
                                $.ajax({
                                    type: "POST",
                                    url: BASEURL+'nfe/get-nfe',
                                    dataType: 'json',
                                    data: {
                                        'chave': this.chave
                                    },
                                    success: function(data){
                                        try{
                                            this.xNome = data.NFe.infNFe.dest.xNome;
                                        }catch(e){
                                            this.xNome = "Consumidor não identificado";
                                        }
                                        this.dhEmi = data.NFe.infNFe.ide.dhEmi;
                                        this.nNF   = data.NFe.infNFe.ide.nNF;
                                        this.natOp = data.NFe.infNFe.ide.natOp;
                                        this.model = data.NFe.infNFe.ide.mod;
                                        this.chNFe = data.protNFe.infProt.chNFe;
                                        this.nProt = data.protNFe.infProt.nProt;
                                        this.cStat = data.protNFe.infProt.cStat;
                                        this.xMotivo = data.protNFe.infProt.xMotivo;
                                    }.bind(this),
                                });
                                
                            },
                            
                            getClassValidation: validation.bind(this),
                            
                            validForm: function(){
                                return this.to.length>0;
                            },
                            
                            checkForm: function(event){
                                event.preventDefault();
                                
                                if( !this.sending && this.validForm() ){
                                    this.sending = true;
                                    $.ajax({
                                        type: "POST",
                                        url: BASEURL+'nfe/send-mail/',
                                        dataType: 'text',
                                        data: {
                                            to: this.to,
                                            chave: this.chave
                                        },
                                        success: function(data){
                                            this.sending = false;
                                        }.bind(this),
                                        error: function(){
                                            this.sending = false;
                                        }
                                    });
                                }
                            },
                            
                            downloadDanfe: function(){
                                window.open( BASEURL+"nfe/download/"+this.chave+"/danfe" );
                            },
                            
                            downloadNotaXml: function(){
                                window.open( BASEURL+"nfe/download/"+this.chave+"/nota" );
                            },
                            
                            buttonText: function(){
                                return this.sending ? 'Aguarde...' : 'Enviar por E-mail';
                            }
                        }
                    });
                    
                    
                    
                    
                    Vue.component('cancel-nfe', {
                        template: '#cancel-nfe',
                        props: ['chave'],
                        data: function(){
                            return {
                                xNome: '',
                                dhEmi: '',
                                nNF: '',
                                natOp: '',
                                model: '',
                                chNFe: '',
                                nProt: '',
                                just: '',
                                xMotivo: '',
                                cStat: '',
                                sending: false,
                            };
                        },
                        created: function(){
                            this.load();
                        },
                        methods: {
                            load: function(){
                                $.ajax({
                                    type: "POST",
                                    url: BASEURL+'nfe/get-nfe',
                                    dataType: 'json',
                                    data: { 'chave': this.chave },
                                    success: function(data){
                                        try{
                                            this.xNome = data.NFe.infNFe.dest.xNome;
                                        }catch(e){
                                            this.xNome = "Consumidor não identificado";
                                        }
                                        this.dhEmi = data.NFe.infNFe.ide.dhEmi;
                                        this.nNF   = data.NFe.infNFe.ide.nNF;
                                        this.natOp = data.NFe.infNFe.ide.natOp;
                                        this.model = data.NFe.infNFe.ide.mod;
                                        this.chNFe = data.protNFe.infProt.chNFe;
                                        this.nProt = data.protNFe.infProt.nProt;
                                        this.cStat = data.protNFe.infProt.cStat;
                                        this.xMotivo = data.protNFe.infProt.xMotivo;
                                    }.bind(this),
                                });
                            },
                            getClassValidation: validation.bind(this),
                            validForm: function(){
                                return this.just.length>0 && this.chNFe.length>0 && this.nProt.length>0 && this.model.length>0;
                            },
                            handleChangeChave: function(){
                                this.chave = this.chNFe;
                                this.load();
                            },
                            checkForm: function(event){
                                event.preventDefault();
                                if( !this.sending && this.validForm() ){
                                    this.sending = true;
                                    $.ajax({
                                        type: "POST",
                                        url: BASEURL+'movements/cancela-nfe/',
                                        dataType: 'json',
                                        data: {
                                            just: this.just,
                                            chave: this.chNFe,
                                            protocolo: this.nProt,
                                            model: this.model,
                                        },
                                        success: function(data){
                                            this.sending = false;
                                            var cStat = data.retEvento.infEvento.cStat;
                                            if(cStat=='101' || cStat=='135' || cStat=='155'){
                                                closeLastModalOpen();
                                                alert("Nota fiscal cancelada com sucesso!");
                                            }else{
                                                alert('Erro código '+cStat+": "+data.retEvento.infEvento.xMotivo);
                                            }
                                        }.bind(this),
                                        error: function(){
                                            this.sending = false;
                                        }
                                    });
                                }
                            },
                            buttonText: function(){
                                return this.sending ? 'Aguarde...' : 'Cancelar NF-e';
                            }
                        }
                    });
                    
                    
                    
                    
                    
                    
                    Vue.component('add-nfe', {
                        template: '#add-nfe',
                        props: ['mid'],
                        data: function(){
                            return {
                                
                                movementId: '',
                                
                                //se deve ou não converter orçamento -> transação depois de autorizada a NF
                                convert: false,
                                
                                //dados da NF
                                nf: {
                                    natOp:'Venda de Mercadoria',
                                    tpNF: 1, //saida
                                    tpImp: 1,
                                    mod: 55,
                                    indPres: 1,
                                    tpEmis: 1,
                                    modFrete: 9,
                                    finNFe: 1,
                                    tPag: '01',
                                    idDest: 1,
                                    indFinal: 1, //verificar
                                    vFrete: 0.00,
                                    vOutro: 0.00,
                                    vNF: 0.00,
                                    vPag: 0.00, //verificar
                                    vTroco: 0.00,
                                    refNFe: '',
                                    infCpl: '',
                                    indAdFisco: ''
                                },
                                
                                //produtos da NF
                                items: [],
                                
                                //destinatário
                                dest: {
                                    xLgr:  '',
                                    nro:  '',
                                    xBairro:  '',
                                    cMun:  '',
                                    xMun:  '',
                                    UF:  '',
                                    CEP:  '',
                                    cPais:  '',
                                    xPais:  '',
                                    xNome:  '',
                                    IE:  '',
                                    CNPJ:  '',
                                    CPF:  '',
                                    indIEDest:  9,
                                },
                                
                                
                                
                                
                                unidadesComerciais: [
                                    { text: 'Unidade', value: 'Un' },
                                    { text: 'Kilograma', value: 'Kg' },
                                    { text: 'Litro', value: 'Lt' },
                                    { text: 'Caixa', value: 'Cx' },
                                ],
                                
                                indIEDestOptions: [
                                    { text: 'Contribuinte de ICMS', value: 1 },
                                    { text: 'Contribuinte Isento de Inscrição', value: 2 },
                                    { text: 'Não Contribuinte', value: 9 },
                                ],
                                
                                tpNFOptions: [
                                    { text: 'Entrada', value: 0 },
                                    { text: 'Saída', value: 1 },
                                ],
                                
                                tpImpOptions: [
                                    { text: 'Sem geração de DANFE', value: 0 },
                                    { text: 'DANFE normal, Retrato', value: 1 },
                                    { text: 'DANFE normal, Paisagem', value: 2 },
                                    { text: 'DANFE Simplificado', value: 3 },
                                    { text: 'DANFE NFC-e', value: 4 },
                                ],
                                
                                modOptions: [
                                    { text: 'NF-e (55)', value: 55 },
                                    { text: 'NFC-e (65)', value: 65 },
                                ],
                                
                                indPresOptions: [
                                    { text: 'Não se aplica (por exemplo, para a Nota Fiscal complementar ou de ajuste)', value: 0 },
                                    { text: 'Operação presencial', value: 1 },
                                    { text: 'Operação não presencial, pela Internet', value: 2 },
                                    { text: 'Operação não presencial, Teleatendimento', value: 3 },
                                    { text: 'NFC-e em operação com entrega em domicílio', value: 4 },
                                    { text: 'Operação não presencial, outros', value: 9 },
                                ],
                                
                                modFreteOptions: [
                                    { text: 'Contratação do frete por conta do remetente (CIF)', value: 0 },
                                    { text: 'Contratação do frete por conta do destinatário (FOB)', value: 1 },
                                    { text: 'Por conta de terceiros', value: 2 },
                                    { text: 'Transporte próprio por conta do remetente', value: 3 },
                                    { text: 'Transporte próprio por conta do destinatário', value: 4 },
                                    { text: 'Sem frete', value: 9 }
                                ],
                                
                                
                                finNFeOptions: [
                                    { text: 'NF-e normal', value: 1 },
                                    { text: 'NF-e complementar', value: 2 },
                                    { text: 'NF-e de ajuste', value: 3 },
                                    { text: 'Devolução de Mercadoria', value: 4 },
                                ],
                                
                                tPagOptions: [
                                    { text: 'Dinheiro', value: '01' },
                                    { text: 'Cheque', value: '02' },
                                    { text: 'Cartão de Crédito', value: '03' },
                                    { text: 'Cartão de Débito', value: '04' },
                                    { text: 'Crédito Loja', value: '05' },
                                    { text: 'Vale Alimentação', value: '10' },
                                    { text: 'Vale Refeição', value: '11' },
                                    { text: 'Vale Presente', value: '12' },
                                    { text: 'Vale Combustível', value: '13' },
                                    { text: 'Boleto Bancário', value: '15' },
                                    { text: 'Sem Pagamento (NFe de Ajuste ou Devolução)', value: '90' },
                                    { text: 'Outros', value: '99' },
                                ],
                                
                                idDestOptions: [
                                    { text: 'Operação Interna', value: 1 },
                                    { text: 'Operação Interestadual', value: 2 },
                                    { text: 'Operação Internacional', value: 3 },
                                ],
                                
                                sending: false,
                            };
                        },
                        created: function(){
                            
                            //configura cadastros events
                            this.$eventHub.$off('select-cadastro');
                            this.$eventHub.$on('select-cadastro', function(id){
                                closeLastModalOpen();
                                //this.sending = true;
                                $.ajax({
                                    type: "POST",
                                    url: BASEURL+'movements/get-cadastro/',
                                    dataType: 'json',
                                    data: { id: id },
                                    success: function(data){
                                        closeLastModalOpen();
                                        //this.sending = false;
                                        this.dest = data;
                                    }.bind(this),
                                    error: function(){
                                        this.sending = false;
                                    }
                                });
                            }.bind(this));
                            
                            
                            //configura produtos events
                            this.$eventHub.$off('select-sku');
                            this.$eventHub.$on('select-sku', function(id){
                                closeLastModalOpen();
                                //this.sending = true;
                                $.ajax({
                                    type: "POST",
                                    url: BASEURL+'movements/get-sku/',
                                    dataType: 'json',
                                    data: { id: id },
                                    success: function(data){
                                        closeLastModalOpen();
                                        //this.sending = false;
                                        this.items.push(data);
                                    }.bind(this),
                                    error: function(){
                                        this.sending = false;
                                    }
                                });
                            }.bind(this));
                            
                            
                            if(this.mid!=''){
                                this.movementId = this.mid.toString();
                                this.convert = true;
                            }
                            this.load();
                            
                            //gambiarra mestre
                            try{clearInterval(document.nfeCalcTotalTimer)}catch(e){}
                            document.nfeCalcTotalTimer = setInterval(this.calc, 800);
                        },
                        
                        methods: {
                            
                            load: function(){
                                
                                if( !this.sending && this.movementId!='' ){
                                    this.sending = true;
                                    
                                    $.ajax({
                                        type: "POST",
                                        url: BASEURL+'movements/get-movement/',
                                        dataType: 'json',
                                        data: {
                                            id: this.movementId,
                                        },
                                        success: function(data){
                                            this.sending = false;
                                            this.items = data.items;
                                            this.dest = data.dest;
                                            this.nf = data.nf;
                                        }.bind(this),
                                        error: function(){
                                            this.sending = false;
                                        }
                                    });
                                    
                                }
                                
                            },
                            
                            //calcula totais da NF e afins
                            calc: function () {
                                var totalNF = 0.0;
                                var pag = 0.0;
                                var troco = 0.0;
                                $.each(this.items, function(index, item){
                                    var prodSubTotal = (parseInt(item.qCom) * parseFloat(item.vUnCom)).round(2);
                                    this.items[index].vProd = prodSubTotal+""; 
                                    totalNF += prodSubTotal;
                                }.bind(this));
                                
                                //if( this.nf.modFrete==0 ){ //frete por conta do emitente
                                totalNF += parseFloat(this.nf.vFrete);
                                //}
                                
                                totalNF += parseFloat(this.nf.vOutro); //outros custos
                                this.nf.vNF = totalNF.round(2);
                                if(this.nf.tPag=='90'){ //sem pagamento
                                    this.nf.vPag = this.nf.vTroco = 0.00;
                                }else{
                                    this.nf.vTroco = (this.nf.vPag - this.nf.vNF).round(2);
                                    if(!$("#nfvPag").is(":focus")){
                                        this.nf.vPag = totalNF.round(2);
                                    }
                                }
                                
                            },
                            
                            getClassValidation: validation.bind(this),
                            
                            duplicateItem: function(idx){
                                var item = this.items[idx];
                                this.items.push(item);
                            },
                            
                            removeItem: function(idx){
                                this.items.splice(idx, 1);
                            },
                            
                            validForm: function(){
                                return true;
                            },
                            
                            
                            
                            checkForm: function(event){
                                event.preventDefault();
                                
                                var obj = {
                                    nf: this.nf,
                                    dest: this.dest,
                                    items: this.items,
                                }
                                
                                if( !this.sending && this.validForm() ){
                                    this.sending = true;
                                    
                                    $.ajax({
                                        type: "POST",
                                        url: BASEURL+'movements/send-nfe/',
                                        dataType: 'json',
                                        data: {
                                            obj: JSON.stringify(obj),
                                            movementId: this.movementId=='' ? 0 : parseInt(this.movementId),
                                            convert: this.convert
                                        },
                                        success: this.handleResult,
                                        error: this.handleResult
                                    });
                                }
                            },
                            
                            handleResult: function(data){
                                this.sending = false;
                                if(data.protNFe.infProt.cStat=='100'){
                                    closeLastModalOpen();
                                    popup('Visualizar Nota Fiscal', '<view-nfe v-bind:chave="\''+data.protNFe.infProt.chNFe+'\'" v-bind:protocolo="\''+data.protNFe.infProt.nProt+'\'" ></view-nfe>', this.load);
                                    alert("Nota Fiscal emitida com sucesso!");
                                }else{
                                    alert("Erro "+data.protNFe.infProt.cStat+": "+data.protNFe.infProt.xMotivo);
                                }
                            },
                            
                            
                            
                            
                            loadMovement: function(event){
                                event.preventDefault();
                                this.load();
                            },
                            
                            //popup para selecionar cadastro
                            listCadastros: function(){
                                popup('Cadastros', '<list-cadastros v-bind:movement="true"></list-cadastros>', function(){}.bind(this));
                            },
                            
                            //popup para selecionar sku
                            listSKUs: function(){
                                popup('Produtos', '<list-skus v-bind:movement="true"></list-skus>', function(){}.bind(this));
                            },
                            
                            
                            buttonText: function(){
                                return this.sending ? 'Aguarde...' : 'Emitir Nota Fiscal';
                            },
                            
                            buttonSearchMovText: function(){
                                return this.sending ? 'Aguarde...' : 'Buscar Movimento';
                            }
                        }
                    });
                    
                    
                    
                    Vue.component('add-servico', {
                        template: '#add-servico',
                        props: ['pk'],
                        data: function(){
                            return {
                                id: 0,
                                price: 0, //truque para ativar a máscara via js
                                name: '',
                                //externalId: '',
                                
                                sending: false,
                            };
                        },
                        created: function(){
                            this.load(this.pk);
                            try{
                                $(".form-group:first-child input[type=text]").get(0).focus();
                            }catch(e){}
                        },
                        methods: {
                            
                            load: function(id){
                                $.ajax({
                                    type: "POST",
                                    url: BASEURL+'service/categories',
                                    dataType: 'json',
                                    success: function(data){
                                        //this.productCategories = data;
                                        if(id > 0){
                                            $.ajax({
                                                type: "POST",
                                                url: BASEURL+'servicos/get',
                                                dataType: 'json',
                                                data: {
                                                    id: id,
                                                },
                                                success: function(data){
                                                    this.id = data.id;
                                                    this.price = data.price;
                                                    this.name = data.name;
                                                    //this.externalId = data.externalId;
                                                    
                                                }.bind(this),
                                            });
                                        }
                                    }.bind(this),
                                });
                                
                                
                            },
                            
                            getClassValidation: validation.bind(this),
                            
                            validForm: function(){
                                return (
                                    this.name.length>0 &&
                                    this.price > 0
                                    );
                                },
                                
                                checkForm: function(event){
                                    event.preventDefault();
                                    
                                    if( !this.sending && this.validForm() ){
                                        this.sending = true;
                                        $.ajax({
                                            type: "POST",
                                            url: BASEURL+'servicos/add/',
                                            dataType: 'json',
                                            data: {
                                                id: this.id,
                                                price: this.price,
                                                name: this.name,
                                                //externalId: this.externalId,
                                                
                                            },
                                            success: function(data){
                                                this.id = data.id;
                                                this.price = data.price;
                                                this.name = data.name;
                                                //this.externalId = data.externalId;
                                                
                                                this.sending = false;
                                            }.bind(this),
                                            error: function(){
                                                //alert("O código informado já está sendo usado em outro produto.");
                                                this.sending = false;
                                            }
                                        });
                                    }
                                },
                                
                                buttonText: function(){
                                    return this.sending ? 'Aguarde...' : (this.id > 0 ? 'Salvar' : 'Adicionar');
                                }
                            }
                        });
                        
                        
                        
                        
                        
                        
                        Vue.component('add-movement', {
                            template: '#add-movement',
                            props: ['pk'],
                            data: function(){
                                return {
                                    
                                    lock: true, //variavel importante controla se o movimento pode ou não ser alterado
                                    
                                    id: 0,
                                    name: '',
                                    externalId: '',
                                    type: 'V',
                                    orcamento: true,
                                    freight: 0, //truque para ativar a máscara via js
                                    otherCosts: 0, //truque para ativar a máscara via js
                                    total: 0, //truque para ativar a máscara via js
                                    enableCosts: false,
                                    cadastroId: 0,
                                    nfeChave: '',
                                    cadastros: [],
                                    
                                    sending: false,
                                    
                                    // [PRODUTOS]
                                    //form add item ao orcamento
                                    showFormAddProduct: false,
                                    productSkus: [],
                                    selectedProduct: Object,
                                    selectedSKU: Object,
                                    qtd: 1,
                                    unitPrice: 0, //pode ser tanto valor de compra quanto de venda, ficar atento à esse número
                                    subTotal: 0, //se ligar aqui também
                                    itemId: 0, //usado para alterar
                                    
                                    //items adicionados ao orçamento/venda
                                    items: [],
                                    
                                    
                                    // [SERVICOS]
                                    //servicos adicionados ao orçamento/venda
                                    showFormAddService: false,
                                    selectedService: Object,
                                    serviceSubTotal: 0,
                                    serviceItemId: 0,
                                    
                                    //seriços adicionados ao orçamento/venda
                                    serviceItems: [],
                                    
                                    //parcelas
                                    accounts: [],
                                };
                            },
                            
                            
                            
                            
                            computed: {
                                calcTotal: function () {
                                    var total = 0.0;
                                    $.each(this.serviceItems, function(index, service){
                                        total += service.subTotal;
                                    }.bind(this));
                                    $.each(this.items, function(index, item){
                                        total += item.subTotal;
                                    }.bind(this));
                                    //console.log(total);
                                    if(this.enableCosts){
                                        total += parseFloat(this.freight);
                                        total += parseFloat(this.otherCosts);
                                    }
                                    //essa linha faz atualizar o total
                                    this.total = total.round(2);
                                    return this.total;
                                }
                            },
                            
                            
                            
                            
                            created: function(){
                                
                                //configura cadastros events
                                this.$eventHub.$off('select-cadastro');
                                this.$eventHub.$on('select-cadastro', function(id){
                                    if(this.lock){ return; } //bloqueio
                                    closeLastModalOpen();
                                    this.cadastroId = id;
                                }.bind(this));
                                
                                //configura produtos events
                                this.$eventHub.$off('select-product');
                                this.$eventHub.$on('select-product', function(id){
                                    if(this.lock){ return; } //bloqueio
                                    closeLastModalOpen();
                                    console.log("Product id: "+id);
                                    this.loadProductById(id, function(data){
                                        this.selectedProduct = data;
                                        this.unitPrice = (this.type=='V' ? data.price : data.cost);
                                        this.loadSKUsFromProductId(data.id, function(data){
                                            this.showFormAddProduct = true;
                                            this.productSkus = data;
                                            this.selectedSKU = data[0];
                                            this.calcSubTotal();
                                        }.bind(this));
                                    }.bind(this));
                                }.bind(this));
                                
                                
                                //configura servicos events
                                this.$eventHub.$off('select-service');
                                this.$eventHub.$on('select-service', function(id){
                                    if(this.lock){ return; } //bloqueio
                                    closeLastModalOpen();
                                    console.log("Service id: "+id);
                                    this.loadServiceById(id, function(data){
                                        this.selectedService = data;
                                        //this.unitPrice = data.price;
                                        this.serviceSubTotal = data.price;
                                        this.showFormAddService = true;
                                    }.bind(this));
                                }.bind(this));
                                
                                
                                try{
                                    this.orcamento = !this.$parent.transaction;
                                    this.type = this.$parent.sale ? 'V' : 'C';
                                }catch(e){}
                                
                                this.load(this.pk);
                                
                                try{
                                    $(".form-group:first-child input[type=text]").get(0).focus();
                                }catch(e){}
                            },
                            methods: {
                                
                                load: function(id){
                                    this.loadInitData(
                                        function(data){
                                            this.cadastros =  _.orderBy(data, ['name'], ['asc']);
                                            this.cadastroId = data[0].id;
                                            if(id > 0){
                                                $.ajax({
                                                    type: "POST",
                                                    url: BASEURL+'movements/get',
                                                    dataType: 'json',
                                                    data: {
                                                        id: id,
                                                    },
                                                    success: function(data){
                                                        this.id = data.id;
                                                        this.name = data.name;
                                                        this.externalId = data.externalId;
                                                        this.type = data.type;
                                                        this.orcamento = data.orcamento;
                                                        this.enableCosts = data.enableCosts;
                                                        this.cadastroId = data.cadastroId;
                                                        this.freight = data.freight;
                                                        this.otherCosts = data.otherCosts;
                                                        this.total = data.total;
                                                        this.lock = !this.orcamento;
                                                        this.nfeChave = data.nfeChave;
                                                        
                                                        this.loadItems();
                                                        this.loadServiceItems();
                                                        this.loadAccounts();
                                                        
                                                    }.bind(this),
                                                });
                                            }else{
                                                this.lock = false;
                                            }
                                        }.bind(this)
                                        
                                        );
                                    },
                                    
                                    
                                    //calcula qtd x custo unitário alterado
                                    calcSubTotal: function(){
                                        this.subTotal = (this.qtd * this.unitPrice).round(2);
                                    },
                                    
                                    
                                    //carrega os cadastros via ajax
                                    loadInitData: function(success){
                                        $.ajax({
                                            type: "POST",
                                            url: BASEURL+'service/cadastros',
                                            dataType: 'json',
                                            success: success
                                        });
                                    },
                                    
                                    
                                    //busca os items de um movimento
                                    loadItems: function(){
                                        if(this.id > 0){
                                            $.ajax({
                                                type: "POST",
                                                url: BASEURL+'movements/get-items/'+this.id,
                                                dataType: 'json',
                                                success: function(data){
                                                    this.items = data;
                                                }.bind(this)
                                            });
                                        }
                                    },
                                    
                                    
                                    
                                    //busca os servicos de um movimento
                                    loadServiceItems: function(){
                                        if(this.id > 0){
                                            $.ajax({
                                                type: "POST",
                                                url: BASEURL+'movements/get-service-items/'+this.id,
                                                dataType: 'json',
                                                success: function(data){
                                                    this.serviceItems = data;
                                                }.bind(this)
                                            });
                                        }
                                    },
                                    
                                    
                                    
                                    
                                    //busca as parcelas
                                    loadAccounts: function(){
                                        if(this.id > 0){
                                            $.ajax({
                                                type: "POST",
                                                url: BASEURL+'accounts/get-accounts/'+this.id,
                                                dataType: 'json',
                                                success: function(data){
                                                    this.accounts = data;
                                                }.bind(this)
                                            });
                                        }
                                    },
                                    
                                    addNFe: function(){
                                        popup('Nova Nota Fiscal', '<add-nfe v-bind:mid="\''+this.id+'\'"></add-nfe>', function(){
                                            this.load(this.pk);
                                            try{clearInterval(document.nfeCalcTotalTimer)}catch(e){}
                                        }.bind(this));
                                    },
                                    
                                    addAccounts: function(){
                                        popup('Adicionar Parcelas', '<add-account v-bind:pk="0" v-bind:mid="'+this.id+'" v-bind:mtype="\''+this.type+'\'" v-bind:morcamento="'+this.orcamento+'"></add-account>', this.loadAccounts);
                                    },
                                    
                                    editAccount: function(id){
                                        popup('Editar Parcela', '<add-account v-bind:pk="'+id+'" v-bind:mid="'+this.id+'" v-bind:mtype="\''+this.type+'\'" v-bind:morcamento="'+this.orcamento+'"></add-account>', this.loadAccounts);
                                    },
                                    
                                    
                                    duplicateAccount: function(id){
                                        $.ajax({
                                            type: "POST",
                                            url: BASEURL+'accounts/duplicate/',
                                            dataType: 'text',
                                            data: {id: id},
                                            success: function(data){
                                                this.loadAccounts();
                                            }.bind(this),
                                        });
                                    },
                                    
                                    
                                    removeAccount: function(id){
                                        popup(
                                            "Atenção",
                                            "Confirmar remover parcela?",
                                            function(){}.bind(this),
                                            function(){
                                                $.ajax({
                                                    type: "POST",
                                                    url: BASEURL+'accounts/remove/',
                                                    dataType: 'text',
                                                    data: {id: id},
                                                    success: function(data){
                                                        closeLastModalOpen();
                                                        this.loadAccounts();
                                                    }.bind(this),
                                                });
                                            }.bind(this),
                                            )
                                        },
                                        
                                        
                                        
                                        //
                                        addMovementItem: function( ajustarEstoque ){
                                            //event.preventDefault();
                                            
                                            if(this.lock){ return; } //bloqueio
                                            
                                            validFormTeste = true;            
                                            if( !this.sending && validFormTeste){
                                                this.sending = true;
                                                $.ajax({
                                                    type: "POST",
                                                    url: BASEURL+'movements/add-item',
                                                    dataType: 'json',
                                                    data: {
                                                        id: this.itemId,
                                                        productId: this.selectedProduct.id,
                                                        skuId: this.selectedSKU.id,
                                                        movementId: this.id,
                                                        qtd: this.qtd,
                                                        unitPrice: this.unitPrice,
                                                        subTotal: this.subTotal,
                                                        ajustarEstoque: ajustarEstoque,
                                                    },
                                                    success: function(data){
                                                        this.showFormAddProduct = false;
                                                        console.log("movement item added with success!");
                                                        console.log(data);
                                                        this.sending = false;
                                                        this.loadItems();
                                                        this.loadServiceItems();
                                                        
                                                    }.bind(this),
                                                    error: function(){
                                                        this.showFormAddProduct = false;
                                                        //alert("O código informado já está sendo usado em outro produto.");
                                                        this.sending = false;
                                                    }.bind(this)
                                                });
                                            }
                                        },
                                        
                                        
                                        
                                        //
                                        addServiceItem: function(){
                                            //event.preventDefault();
                                            
                                            if(this.lock){ return; } //bloqueio
                                            
                                            validFormTeste = true;            
                                            if( !this.sending && validFormTeste){
                                                this.sending = true;
                                                $.ajax({
                                                    type: "POST",
                                                    url: BASEURL+'movements/add-service-item',
                                                    dataType: 'json',
                                                    data: {
                                                        id: this.serviceItemId,
                                                        movementId: this.id,
                                                        serviceId: this.selectedService.id,
                                                        name: this.selectedService.name,
                                                        subTotal: this.serviceSubTotal,
                                                    },
                                                    success: function(data){
                                                        this.showFormAddService = false;
                                                        console.log("service item added with success!");
                                                        console.log(data);
                                                        this.sending = false;
                                                        this.loadItems();
                                                        this.loadServiceItems();
                                                        
                                                    }.bind(this),
                                                    error: function(){
                                                        this.showFormAddService = false;
                                                        //alert("O código informado já está sendo usado em outro produto.");
                                                        this.sending = false;
                                                    }.bind(this)
                                                });
                                            }
                                        },
                                        
                                        
                                        //carrega um item no form para editar
                                        editItem: function( id ){
                                            
                                            if(this.lock){ return; } //bloqueio
                                            
                                            $.ajax({
                                                type: "POST",
                                                url: BASEURL+'movements/get-item',
                                                dataType: 'json',
                                                data: {
                                                    id: id,
                                                },
                                                success: function(data){
                                                    var productId = data.productId;
                                                    var skuId = data.skuId;
                                                    
                                                    this.itemId = data.id;
                                                    this.subTotal = data.subTotal;
                                                    this.qtd = data.qtd;
                                                    this.unitPrice = data.unitPrice;
                                                    this.loadProductById(productId, function(data){
                                                        this.selectedProduct = data;
                                                        this.loadSKUsFromProductId(productId, function(data){
                                                            this.showFormAddProduct = true;
                                                            this.productSkus = data;
                                                            $.each(data, function(index, sku){
                                                                if(sku.id==skuId){
                                                                    this.selectedSKU = sku;
                                                                }
                                                            }.bind(this));
                                                        }.bind(this));
                                                    }.bind(this));
                                                }.bind(this)
                                            });
                                        },
                                        
                                        
                                        
                                        
                                        //carrega um servico no form para editar
                                        editServiceItem: function( id ){
                                            
                                            if(this.lock){ return; } //bloqueio
                                            
                                            $.ajax({
                                                type: "POST",
                                                url: BASEURL+'movements/get-service-item',
                                                dataType: 'json',
                                                data: {
                                                    id: id,
                                                },
                                                success: function(data){
                                                    var serviceId = data.serviceId;
                                                    this.serviceItemId = data.id;
                                                    this.serviceSubTotal = data.subTotal;
                                                    this.loadServiceById(serviceId, function(data){
                                                        this.selectedService = data;
                                                        this.showFormAddService = true;
                                                    }.bind(this));
                                                }.bind(this)
                                            });
                                        },
                                        
                                        
                                        
                                        //remove um produto
                                        removeItem: function(id){
                                            
                                            if(this.lock){ return; } //bloqueio
                                            
                                            var msg = "Confirmar remover este item?";
                                            var msgMovement = "Você está prestes a remover um item de uma transação.<br />Se você não tem pleno conhecimento sobre esta operação, recomendamos antes que consulte um contador.<br />Confirmar remover este item?";
                                            popup(
                                                "Atenção",
                                                msg,//this.orcamento ? msg : msgMovement,
                                                function(){}.bind(this),
                                                function(){
                                                    $.ajax({
                                                        type: "POST",
                                                        url: BASEURL+'movements/remove-item',
                                                        dataType: 'text',
                                                        data: {id: id},
                                                        success: function(data){
                                                            closeLastModalOpen();
                                                            this.loadItems();
                                                            if(!this.orcamento){
                                                                popup(
                                                                    "Atenção",
                                                                    "Item excluído com sucesso.<br />Deseja ajustar o estoque?",
                                                                    function(){}.bind(this),
                                                                    function(){
                                                                        //função que ajusta o estoque
                                                                    }.bind(this)
                                                                    );
                                                                }
                                                            }.bind(this),
                                                        });
                                                    }.bind(this),
                                                    );
                                                },
                                                
                                                
                                                
                                                //remove um serviço
                                                removeServiceItem: function(id){
                                                    
                                                    if(this.lock){ return; } //bloqueio
                                                    
                                                    popup(
                                                        "Atenção",
                                                        "Confirmar remover este item?",
                                                        function(){}.bind(this),
                                                        function(){
                                                            $.ajax({
                                                                type: "POST",
                                                                url: BASEURL+'movements/remove-service-item',
                                                                dataType: 'text',
                                                                data: {id: id},
                                                                success: function(data){
                                                                    closeLastModalOpen();
                                                                    this.loadServiceItems();
                                                                }.bind(this),
                                                            });
                                                        }.bind(this),
                                                        )
                                                    },
                                                    
                                                    
                                                    
                                                    loadServiceById: function(id, success){
                                                        $.ajax({
                                                            type: "POST",
                                                            url: BASEURL+'servicos/get',
                                                            dataType: 'json',
                                                            data: {
                                                                id: id,
                                                            },
                                                            success: success,
                                                        });
                                                    },
                                                    
                                                    
                                                    loadProductById: function(id, success){
                                                        $.ajax({
                                                            type: "POST",
                                                            url: BASEURL+'products/get',
                                                            dataType: 'json',
                                                            data: {
                                                                id: id,
                                                            },
                                                            success: success,
                                                        });
                                                    },
                                                    
                                                    loadSKUsFromProductId: function(productId, success){
                                                        $.ajax({
                                                            type: "GET",
                                                            url: BASEURL+'skus/list-all/'+productId,
                                                            dataType: 'json',
                                                            data: {},
                                                            success: success,
                                                        });
                                                    },
                                                    
                                                    getClassValidation: validation.bind(this),
                                                    
                                                    validForm: function(){
                                                        return true;
                                                    },
                                                    
                                                    
                                                    //popup para selecionar cadastro
                                                    listCadastros: function(){
                                                        popup('Cadastros', '<list-cadastros v-bind:movement="true"></list-cadastros>', function(){
                                                            this.loadInitData(function(data){
                                                                this.cadastros =  _.orderBy(data, ['name'], ['asc']);
                                                            }.bind(this));
                                                        }.bind(this));
                                                    },
                                                    
                                                    
                                                    //popup para selecionar produto
                                                    addProduct: function(){
                                                        
                                                        if(this.lock){ return; } //bloqueio
                                                        
                                                        this.itemId = 0;
                                                        this.qtd = 1;
                                                        this.subTotal = 0;
                                                        this.unitPrice = 0;
                                                        this.selectedSKU = null;
                                                        
                                                        popup('Produtos', '<list-products v-bind:movement="true"></list-products>', function(){
                                                            this.loadInitData(function(data){
                                                                this.cadastros =  _.orderBy(data, ['name'], ['asc']);
                                                            }.bind(this));
                                                        }.bind(this));
                                                    },
                                                    
                                                    
                                                    
                                                    //popup para selecionar servico
                                                    addService: function(){
                                                        
                                                        if(this.lock){ return; } //bloqueio
                                                        
                                                        this.serviceItemId = 0;
                                                        this.serviceSubTotal = 0;
                                                        
                                                        popup('Serviços', '<list-servicos v-bind:movement="true"></list-servicos>', function(){
                                                            this.loadInitData(function(data){
                                                                this.cadastros =  _.orderBy(data, ['name'], ['asc']);
                                                            }.bind(this));
                                                        }.bind(this));
                                                    },
                                                    
                                                    
                                                    checkForm: function(event){
                                                        event.preventDefault();
                                                        
                                                        if(this.lock){ return; } //bloqueio
                                                        
                                                        if( !this.sending && this.validForm() && !this.showFormAddProduct  && !this.showFormAddService){
                                                            this.sending = true;
                                                            
                                                            //seta uma descr padrão, para facilitar
                                                            if(this.name==''){
                                                                cadastro = _.find(this.cadastros, { 'id': this.cadastroId });
                                                                this.name = (this.type=='V' ? 'Venda' : 'Compra') + ' - ' + cadastro.name;
                                                                if(cadastro.nameFant!=''){
                                                                    this.name += ' ('+cadastro.nameFant+')';
                                                                }
                                                            }
                                                            
                                                            $.ajax({
                                                                type: "POST",
                                                                url: BASEURL+'movements/add/',
                                                                dataType: 'json',
                                                                data: {
                                                                    id: this.id,
                                                                    name: this.name,
                                                                    //externalId: this.externalId,
                                                                    type: this.type,
                                                                    orcamento: this.orcamento,
                                                                    otherCosts: this.otherCosts,
                                                                    freight: this.freight,
                                                                    total: this.total,
                                                                    enableCosts: this.enableCosts,
                                                                    cadastroId: this.cadastroId,
                                                                },
                                                                success: function(data){
                                                                    this.id = data.id;
                                                                    this.name = data.name;
                                                                    this.externalId = data.externalId;
                                                                    this.type = data.type;
                                                                    this.orcamento = data.orcamento;
                                                                    this.enableCosts = data.enableCosts;
                                                                    this.cadastroId = data.cadastroId;
                                                                    this.freight = data.freight;
                                                                    this.otherCosts = data.otherCosts;
                                                                    this.total = data.total;
                                                                    
                                                                    
                                                                    this.sending = false;
                                                                }.bind(this),
                                                                error: function(){
                                                                    //alert("O código informado já está sendo usado em outro produto.");
                                                                    this.sending = false;
                                                                }
                                                            });
                                                        }
                                                    },
                                                    
                                                    
                                                    
                                                    convertToTransaction: function(){
                                                        if(this.lock){ return; } //bloqueio
                                                        
                                                        if( !this.sending && this.validForm() && !this.showFormAddProduct && !this.showFormAddService ){
                                                            this.sending = true;
                                                            
                                                            $.ajax({
                                                                type: "POST",
                                                                url: BASEURL+'movements/convert-to-transaction',
                                                                dataType: 'text',
                                                                data: {
                                                                    id: this.id
                                                                },
                                                                success: function(data){
                                                                    this.load(this.id);
                                                                    this.sending = false;
                                                                    alert("Transação realizada com sucesso!");
                                                                }.bind(this),
                                                                error: function(){
                                                                    //alert("O código informado já está sendo usado em outro produto.");
                                                                    this.sending = false;
                                                                }.bind(this)
                                                            });
                                                            
                                                        }
                                                    },
                                                    
                                                    
                                                    cancelTransaction: function(){
                                                        if( !this.sending){
                                                            this.sending = true;
                                                            $.ajax({
                                                                type: "POST",
                                                                url: BASEURL+'movements/cancel-transaction',
                                                                dataType: 'text',
                                                                data: {
                                                                    id: this.id
                                                                },
                                                                success: function(data){
                                                                    this.load(this.id);
                                                                    this.sending = false;
                                                                }.bind(this),
                                                                error: function(){
                                                                    //alert("O código informado já está sendo usado em outro produto.");
                                                                    this.sending = false;
                                                                }.bind(this)
                                                            });
                                                        }
                                                    },
                                                    
                                                    
                                                    
                                                    buttonText: function(){
                                                        return this.sending ? 'Aguarde...' : (this.id > 0 ? 'Salvar' : 'Adicionar');
                                                    }
                                                }
                                            });
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            Vue.component('list-products', {
                                                template: '#list-products',
                                                props:['movement'],
                                                data: function(){
                                                    return {
                                                        items: [],
                                                        filteredItems: [],
                                                        searchParam: '',
                                                    };
                                                },
                                                
                                                created: function(){
                                                    this.load();
                                                },
                                                
                                                beforeDestroy() {
                                                    this.$eventHub.$off('select-product');
                                                },
                                                
                                                methods: {
                                                    load: function(){
                                                        $.ajax({
                                                            type: "POST",
                                                            url: BASEURL+'products/list-all',
                                                            dataType: 'json',
                                                            data: {},
                                                            success: function(data){
                                                                this.items = data;
                                                                this.filter();
                                                            }.bind(this),
                                                        });
                                                    },
                                                    
                                                    filter: function(event){
                                                        if(event != null){
                                                            event.preventDefault();
                                                        }
                                                        var searchParam = this.searchParam;
                                                        this.filteredItems = searchParam.length==0 ? this.items : _.filter(this.items, function(item) { 
                                                            return item.name.toString().toLowerCase().indexOf(searchParam.toString().toLowerCase())>-1 || item.externalId==searchParam; 
                                                        });
                                                        this.filteredItems = _.orderBy(this.filteredItems, ['name'], ['asc']);
                                                    },
                                                    
                                                    
                                                    select: function(item){
                                                        this.$eventHub.$emit('select-product', item.id);
                                                    },
                                                    
                                                    
                                                    addProducts: function(){
                                                        popup('Adicionar Produtos', '<add-products></add-products>', this.load);
                                                    },
                                                    
                                                    edit: function(id){
                                                        popup('Editar Produtos', '<add-product v-bind:pk="'+id+'"></add-product>', this.load);
                                                    },
                                                    
                                                    duplicate: function(id){
                                                        $.ajax({
                                                            type: "POST",
                                                            url: BASEURL+'products/duplicate/',
                                                            dataType: 'text',
                                                            data: {id: id},
                                                            success: function(data){
                                                                this.load();
                                                            }.bind(this),
                                                        });
                                                    },
                                                    
                                                    openSKUS: function(id){
                                                        popup('Variações do Produto', '<list-skus v-bind:movement="false" v-bind:id="'+id+'"></list-skus>', this.load);
                                                    },
                                                    
                                                    remove: function(id){
                                                        
                                                        popup(
                                                            "Atenção",
                                                            "Confirmar remover este registro e todos os seus itens relacionados?",
                                                            function(){}.bind(this),
                                                            function(){
                                                                $.ajax({
                                                                    type: "POST",
                                                                    url: BASEURL+'products/remove/',
                                                                    dataType: 'text',
                                                                    data: {id: id},
                                                                    success: function(data){
                                                                        closeLastModalOpen();
                                                                        this.load();
                                                                    }.bind(this),
                                                                });
                                                            }.bind(this),
                                                            )
                                                            
                                                            
                                                        },
                                                    }
                                                    
                                                });
                                                
                                                
                                                Vue.component('list-movements', {
                                                    template: '#list-movements',
                                                    //props: ['orc', 'mtype'],
                                                    props: ['mtype'],
                                                    data: function(){
                                                        return {
                                                            items: [],
                                                            filteredItems: [],
                                                            searchParam: '',
                                                            sale:true,
                                                            
                                                            //contorle de exibição
                                                            showOrcamentos: true,
                                                            showTransactions: true,
                                                        };
                                                    },
                                                    
                                                    created: function(){
                                                        this.sale = this.mtype=='v';
                                                        this.load();
                                                    },
                                                    
                                                    methods: {
                                                        load: function(){
                                                            $.ajax({
                                                                type: "POST",
                                                                //url: BASEURL+'movements/list-all/'+this.orc+'/'+this.mtype,
                                                                url: BASEURL+'movements/list-all/'+this.mtype,
                                                                dataType: 'json',
                                                                data: {},
                                                                success: function(data){
                                                                    this.items = data;
                                                                    this.filter();
                                                                }.bind(this),
                                                            });
                                                        },
                                                        
                                                        filter: function(event){
                                                            if(event != null){
                                                                event.preventDefault();
                                                            }
                                                            var searchParam = this.searchParam;
                                                            this.filteredItems = searchParam.length==0 ? this.items : _.filter(this.items, function(item) { 
                                                                return item.name.toString().toLowerCase().indexOf(searchParam.toString().toLowerCase())>-1 || item.externalId==searchParam; 
                                                            });
                                                            this.filteredItems = _.orderBy(this.filteredItems, ['id'], ['desc']);
                                                        },
                                                        
                                                        addMovements: function(){
                                                            popup('Adicionar Movimentos', '<add-movements v-bind:transaction="false" v-bind:sale="'+this.sale+'"></add-movements>', this.load);
                                                        },
                                                        
                                                        edit: function(id){
                                                            popup('Editar Movimentos', '<add-movement v-bind:pk="'+id+'"></add-movement>', this.load);
                                                        },
                                                        
                                                        cancel: function(chave){
                                                            popup('Cancelar Nota Fiscal', '<cancel-nfe v-bind:chave="\''+chave+'\'"></cancel-nfe>', this.load);
                                                        },
                                                        
                                                        
                                                        remove: function(id){
                                                            
                                                            popup(
                                                                "Atenção",
                                                                "Confirmar remover este registro e todos os seus itens relacionados?",
                                                                function(){}.bind(this),
                                                                function(){
                                                                    $.ajax({
                                                                        type: "POST",
                                                                        url: BASEURL+'movements/remove/',
                                                                        dataType: 'text',
                                                                        data: {id: id},
                                                                        success: function(data){
                                                                            closeLastModalOpen();
                                                                            this.load();
                                                                        }.bind(this),
                                                                    });
                                                                }.bind(this),
                                                                )
                                                                
                                                                
                                                            },
                                                        }
                                                        
                                                    });
                                                    
                                                    
                                                    
                                                    Vue.component('list-accounts', {
                                                        template: '#list-accounts',
                                                        data: function(){
                                                            return {
                                                                items: [],
                                                                filteredItems: [],
                                                                searchParam: '',
                                                            };
                                                        },
                                                        
                                                        created: function(){
                                                            this.load();
                                                        },
                                                        
                                                        methods: {
                                                            load: function(){
                                                                $.ajax({
                                                                    type: "POST",
                                                                    url: BASEURL+'accounts/list-all',
                                                                    dataType: 'json',
                                                                    data: {},
                                                                    success: function(data){
                                                                        this.items = data;
                                                                        this.filter();
                                                                    }.bind(this),
                                                                });
                                                            },
                                                            
                                                            filter: function(event){
                                                                if(event != null){
                                                                    event.preventDefault();
                                                                }
                                                                var searchParam = this.searchParam;
                                                                this.filteredItems = searchParam.length==0 ? this.items : _.filter(this.items, function(item) { 
                                                                    return item.name.toString().toLowerCase().indexOf(searchParam.toString().toLowerCase())>-1 || item.externalId==searchParam; 
                                                                });
                                                                this.filteredItems = _.orderBy(this.filteredItems, ['id'], ['desc']);
                                                            },
                                                            
                                                            addAccounts: function(){
                                                                popup('Adicionar Conta Única', '<add-account></add-account>', this.load);
                                                            },
                                                            
                                                            edit: function(id){
                                                                popup('Editar Conta Única', '<add-account v-bind:pk="'+id+'"></add-account>', this.load);
                                                            },
                                                            
                                                            duplicate: function(id){
                                                                $.ajax({
                                                                    type: "POST",
                                                                    url: BASEURL+'accounts/duplicate/',
                                                                    dataType: 'text',
                                                                    data: {id: id},
                                                                    success: function(data){
                                                                        this.load();
                                                                    }.bind(this),
                                                                });
                                                            },
                                                            
                                                            remove: function(id){
                                                                
                                                                popup(
                                                                    "Atenção",
                                                                    "Confirmar remover este registro e todos os seus itens relacionados?",
                                                                    function(){}.bind(this),
                                                                    function(){
                                                                        $.ajax({
                                                                            type: "POST",
                                                                            url: BASEURL+'accounts/remove/',
                                                                            dataType: 'text',
                                                                            data: {id: id},
                                                                            success: function(data){
                                                                                closeLastModalOpen();
                                                                                this.load();
                                                                            }.bind(this),
                                                                        });
                                                                    }.bind(this),
                                                                    )
                                                                    
                                                                    
                                                                },
                                                            }
                                                            
                                                        });
                                                        
                                                        
                                                        Vue.component('list-fixed-accounts', {
                                                            template: '#list-fixed-accounts',
                                                            data: function(){
                                                                return {
                                                                    items: [],
                                                                    filteredItems: [],
                                                                    searchParam: '',
                                                                };
                                                            },
                                                            
                                                            created: function(){
                                                                this.load();
                                                            },
                                                            
                                                            methods: {
                                                                load: function(){
                                                                    $.ajax({
                                                                        type: "POST",
                                                                        url: BASEURL+'fixed-accounts/list-all',
                                                                        dataType: 'json',
                                                                        data: {},
                                                                        success: function(data){
                                                                            this.items = data;
                                                                            this.filter();
                                                                        }.bind(this),
                                                                    });
                                                                },
                                                                
                                                                filter: function(event){
                                                                    if(event != null){
                                                                        event.preventDefault();
                                                                    }
                                                                    var searchParam = this.searchParam;
                                                                    this.filteredItems = searchParam.length==0 ? this.items : _.filter(this.items, function(item) { 
                                                                        return item.name.toString().toLowerCase().indexOf(searchParam.toString().toLowerCase())>-1 || item.externalId==searchParam; 
                                                                    });
                                                                    this.filteredItems = _.orderBy(this.filteredItems, ['id'], ['desc']);
                                                                },
                                                                
                                                                addAccounts: function(){
                                                                    popup('Adicionar Conta Recorrente', '<add-fixed-account></add-fixed-account>', this.load);
                                                                },
                                                                
                                                                edit: function(id){
                                                                    popup('Editar Conta Recorrente', '<add-fixed-account v-bind:pk="'+id+'"></add-fixed-account>', this.load);
                                                                },
                                                                
                                                                duplicate: function(id){
                                                                    $.ajax({
                                                                        type: "POST",
                                                                        url: BASEURL+'fixed-accounts/duplicate/',
                                                                        dataType: 'text',
                                                                        data: {id: id},
                                                                        success: function(data){
                                                                            this.load();
                                                                        }.bind(this),
                                                                    });
                                                                },
                                                                
                                                                remove: function(id){
                                                                    
                                                                    popup(
                                                                        "Atenção",
                                                                        "Confirmar remover este registro e todos os seus itens relacionados?",
                                                                        function(){}.bind(this),
                                                                        function(){
                                                                            $.ajax({
                                                                                type: "POST",
                                                                                url: BASEURL+'fixed-accounts/remove/',
                                                                                dataType: 'text',
                                                                                data: {id: id},
                                                                                success: function(data){
                                                                                    closeLastModalOpen();
                                                                                    this.load();
                                                                                }.bind(this),
                                                                            });
                                                                        }.bind(this),
                                                                        )
                                                                        
                                                                        
                                                                    },
                                                                }
                                                                
                                                            });
                                                            
                                                            
                                                            
                                                            
                                                            
                                                            
                                                            Vue.component('add-cadastro', {
                                                                template: '#add-cadastro',
                                                                props: ['pk'],
                                                                data: function(){
                                                                    return {
                                                                        id: 0,
                                                                        name: '',
                                                                        nameFant: '',
                                                                        telefone: '',
                                                                        email: '',
                                                                        cpf: '',
                                                                        cnpj: '',
                                                                        ie: '',
                                                                        lgr: '',
                                                                        num: '',
                                                                        bairro: '',
                                                                        codMun: '',
                                                                        municipio: '',
                                                                        cep: '',
                                                                        pais: '',
                                                                        cPais: '',
                                                                        indIEDest: 9,
                                                                        uf: 'RS',
                                                                        estados: [],
                                                                        
                                                                        indIEDestOptions: [
                                                                            { text: 'Contribuinte de ICMS', value: 1 },
                                                                            { text: 'Contribuinte Isento de Inscrição', value: 2 },
                                                                            { text: 'Não Contribuinte', value: 9 },
                                                                        ],
                                                                        
                                                                        sending: false,
                                                                    };
                                                                },
                                                                created: function(){
                                                                    this.load(this.pk);
                                                                    try{
                                                                        $(".form-group:first-child input[type=text]").get(0).focus();
                                                                    }catch(e){}
                                                                },
                                                                methods: {
                                                                    
                                                                    load: function(id){
                                                                        $.ajax({
                                                                            type: "POST",
                                                                            url: BASEURL+'service/estados',
                                                                            dataType: 'json',
                                                                            success: function(data){
                                                                                this.estados = data;
                                                                                if(id > 0){
                                                                                    $.ajax({
                                                                                        type: "POST",
                                                                                        url: BASEURL+'cadastros/get',
                                                                                        dataType: 'json',
                                                                                        data: {
                                                                                            id: id,
                                                                                        },
                                                                                        
                                                                                        success: function(data){
                                                                                            this.id = data.id;
                                                                                            this.name = data.name;
                                                                                            this.nameFant = data.nameFant;
                                                                                            this.cpf = data.cpf;
                                                                                            this.cnpj = data.cnpj;
                                                                                            this.ie = data.ie;
                                                                                            this.lgr = data.lgr;
                                                                                            this.num = data.num;
                                                                                            this.bairro = data.bairro;
                                                                                            this.codMun = data.codMun;
                                                                                            this.municipio = data.municipio;
                                                                                            this.uf = data.uf;
                                                                                            this.email = data.email;
                                                                                            this.telefone = data.telefone;
                                                                                            this.cep = data.cep;
                                                                                            this.pais = data.pais;
                                                                                            this.cPais = data.cPais;
                                                                                            this.indIEDest = data.indIEDest;
                                                                                        }.bind(this),
                                                                                    });
                                                                                }
                                                                            }.bind(this),
                                                                        });
                                                                        
                                                                        
                                                                    },
                                                                    
                                                                    getClassValidation: validation.bind(this),
                                                                    
                                                                    validForm: function(){
                                                                        return (
                                                                            this.name.length>0
                                                                            );
                                                                        },
                                                                        
                                                                        checkForm: function(event){
                                                                            event.preventDefault();
                                                                            
                                                                            if( !this.sending && this.validForm() ){
                                                                                this.sending = true;
                                                                                $.ajax({
                                                                                    type: "POST",
                                                                                    url: BASEURL+'cadastros/add/',
                                                                                    dataType: 'json',
                                                                                    data: {
                                                                                        id: this.id,
                                                                                        name: this.name,
                                                                                        nameFant: this.nameFant,
                                                                                        cpf: this.cpf,
                                                                                        cnpj: this.cnpj,
                                                                                        ie: this.ie,
                                                                                        lgr: this.lgr,
                                                                                        num: this.num,
                                                                                        bairro: this.bairro,
                                                                                        codMun: this.codMun,
                                                                                        municipio: this.municipio,
                                                                                        uf: this.uf,
                                                                                        email: this.email,
                                                                                        telefone: this.telefone,
                                                                                        cep: this.cep,
                                                                                        pais: this.pais,
                                                                                        cPais: this.cPais,
                                                                                        indIEDest: this.indIEDest,
                                                                                    },
                                                                                    success: function(data){
                                                                                        this.id = data.id;
                                                                                        this.name = data.name;
                                                                                        this.nameFant = data.nameFant;
                                                                                        this.cpf = data.cpf;
                                                                                        this.cnpj = data.cnpj;
                                                                                        this.ie = data.ie;
                                                                                        this.lgr = data.lgr;
                                                                                        this.num = data.num;
                                                                                        this.bairro = data.bairro;
                                                                                        this.codMun = data.codMun;
                                                                                        this.municipio = data.municipio;
                                                                                        this.uf = data.uf;
                                                                                        this.email = data.email;
                                                                                        this.telefone = data.telefone;
                                                                                        this.cep = data.cep;
                                                                                        this.pais = data.pais;
                                                                                        this.cPais = data.cPais;
                                                                                        this.indIEDest = data.indIEDest;
                                                                                        
                                                                                        this.sending = false;
                                                                                    }.bind(this),
                                                                                    error: function(){
                                                                                        //alert("O código informado já está sendo usado em outro produto.");
                                                                                        this.sending = false;
                                                                                    }
                                                                                });
                                                                            }
                                                                        },
                                                                        
                                                                        
                                                                        buttonText: function(){
                                                                            return this.sending ? 'Aguarde...' : (this.id > 0 ? 'Salvar' : 'Adicionar');
                                                                        }
                                                                    }
                                                                });
                                                                
                                                                
                                                                
                                                                
                                                                
                                                                
                                                                
                                                                Vue.component('list-cadastros', {
                                                                    template: '#list-cadastros',
                                                                    props:['movement'],
                                                                    data: function(){
                                                                        return {
                                                                            items: [],
                                                                            filteredItems: [],
                                                                            searchParam: '',
                                                                        };
                                                                    },
                                                                    
                                                                    created: function(){
                                                                        this.load();
                                                                    },
                                                                    
                                                                    methods: {
                                                                        load: function(){
                                                                            $.ajax({
                                                                                type: "POST",
                                                                                url: BASEURL+'cadastros/list-all',
                                                                                dataType: 'json',
                                                                                data: {},
                                                                                success: function(data){
                                                                                    this.items = data;
                                                                                    this.filter();
                                                                                }.bind(this),
                                                                            });
                                                                        },
                                                                        
                                                                        filter: function(event){
                                                                            if(event != null){
                                                                                event.preventDefault();
                                                                            }
                                                                            var searchParam = this.searchParam;
                                                                            this.filteredItems = searchParam.length==0 ? this.items : _.filter(this.items, function(item) { 
                                                                                return item.name.toString().toLowerCase().indexOf(searchParam.toString().toLowerCase())>-1 || item.cnpj==searchParam || item.cpf==searchParam; 
                                                                            });
                                                                            this.filteredItems = _.orderBy(this.filteredItems, ['name'], ['asc']);
                                                                            
                                                                        },
                                                                        
                                                                        select: function(item){
                                                                            this.$eventHub.$emit('select-cadastro', item.id);
                                                                        },
                                                                        
                                                                        addCadastros: function(){
                                                                            popup('Adicionar Cadastros', '<add-cadastros></add-cadastros>', this.load);
                                                                        },
                                                                        
                                                                        edit: function(id){
                                                                            popup('Editar Cadastros', '<add-cadastro v-bind:pk="'+id+'"></add-cadastro>', this.load);
                                                                        },
                                                                        
                                                                        duplicate: function(id){
                                                                            $.ajax({
                                                                                type: "POST",
                                                                                url: BASEURL+'cadastros/duplicate/',
                                                                                dataType: 'text',
                                                                                data: {id: id},
                                                                                success: function(data){
                                                                                    this.load();
                                                                                }.bind(this),
                                                                            });
                                                                        },
                                                                        
                                                                        remove: function(id){
                                                                            
                                                                            popup(
                                                                                "Atenção",
                                                                                "Confirmar remover este registro e todos os seus itens relacionados?",
                                                                                function(){}.bind(this),
                                                                                function(){
                                                                                    $.ajax({
                                                                                        type: "POST",
                                                                                        url: BASEURL+'cadastros/remove/',
                                                                                        dataType: 'text',
                                                                                        data: {id: id},
                                                                                        success: function(data){
                                                                                            closeLastModalOpen();
                                                                                            this.load();
                                                                                        }.bind(this),
                                                                                    });
                                                                                }.bind(this),
                                                                                )
                                                                                
                                                                                
                                                                            },
                                                                        }
                                                                        
                                                                    });
                                                                    
                                                                    
                                                                    
                                                                    
                                                                    Vue.component('list-nfe', {
                                                                        template: '#list-nfe',
                                                                        data: function(){
                                                                            return {
                                                                                emitidas: [],
                                                                                filteredEmitidas: [],
                                                                                canceladas: [],
                                                                                filteredCanceladas: [],
                                                                                searchParam: '',
                                                                            };
                                                                        },
                                                                        
                                                                        created: function(){
                                                                            this.load();
                                                                        },
                                                                        
                                                                        
                                                                        
                                                                        methods: {
                                                                            load: function(){
                                                                                $.ajax({
                                                                                    type: "POST",
                                                                                    url: BASEURL+'nfe/list-all',
                                                                                    dataType: 'json',
                                                                                    data: {},
                                                                                    success: function(data){
                                                                                        this.emitidas = data.emitidas;
                                                                                        this.canceladas = data.canceladas;
                                                                                        this.filter();
                                                                                    }.bind(this),
                                                                                });
                                                                            },
                                                                            
                                                                            filter: function(event){
                                                                                if(event != null){
                                                                                    event.preventDefault();
                                                                                }
                                                                                var searchParam = this.searchParam;
                                                                                this.filteredEmitidas = searchParam.length==0 ? this.emitidas : _.filter(this.emitidas, function(item) { 
                                                                                    return item.xNome.toString().toLowerCase().indexOf(searchParam.toString().toLowerCase())>-1 || item.nNF==searchParam.toString().toLowerCase(); 
                                                                                });
                                                                                this.filteredEmitidas = _.orderBy(this.filteredEmitidas, ['nNF'], ['desc']);
                                                                                this.filteredCanceladas = searchParam.length==0 ? this.canceladas : _.filter(this.canceladas, function(item) { 
                                                                                    return item.xNome.toString().toLowerCase().indexOf(searchParam.toString().toLowerCase())>-1 || item.nNF==searchParam.toString().toLowerCase();
                                                                                });
                                                                                this.filteredCanceladas = _.orderBy(this.filteredCanceladas, ['nNF'], ['desc']);
                                                                            },
                                                                            
                                                                            view: function(chave, protocolo){
                                                                                console.log(chave);
                                                                                console.log(protocolo);
                                                                                popup('Visualizar Nota Fiscal', '<view-nfe v-bind:chave="\''+chave+'\'" v-bind:protocolo="\''+protocolo+'\'" ></view-nfe>', this.load);
                                                                            },
                                                                            
                                                                            cancel: function(chave){
                                                                                popup('Cancelar Nota Fiscal', '<cancel-nfe v-bind:chave="\''+chave+'\'"></cancel-nfe>', this.load);
                                                                            },
                                                                            cancelNFe: function(){
                                                                                popup('Cancelar Nota Fiscal', '<cancel-nfe v-bind:chave="\'\'"></cancel-nfe>', this.load);
                                                                            },
                                                                            
                                                                            addNFe: function(){
                                                                                popup('Nova Nota Fiscal', '<add-nfe v-bind:mid="0"></add-nfe>', function(){
                                                                                    this.load();
                                                                                    try{clearInterval(document.nfeCalcTotalTimer)}catch(e){}
                                                                                }.bind(this));
                                                                            },
                                                                            
                                                                        }
                                                                        
                                                                    });
                                                                    
                                                                    
                                                                    
                                                                    
                                                                    
                                                                    Vue.component('list-servicos', {
                                                                        template: '#list-servicos',
                                                                        props:['movement'],
                                                                        data: function(){
                                                                            return {
                                                                                items: [],
                                                                                filteredItems: [],
                                                                                searchParam: '',
                                                                            };
                                                                        },
                                                                        
                                                                        created: function(){
                                                                            this.load();
                                                                        },
                                                                        
                                                                        
                                                                        beforeDestroy() {
                                                                            this.$eventHub.$off('select-service');
                                                                        },
                                                                        
                                                                        
                                                                        methods: {
                                                                            load: function(){
                                                                                $.ajax({
                                                                                    type: "POST",
                                                                                    url: BASEURL+'servicos/list-all',
                                                                                    dataType: 'json',
                                                                                    data: {},
                                                                                    success: function(data){
                                                                                        this.items = data;
                                                                                        this.filter();
                                                                                    }.bind(this),
                                                                                });
                                                                            },
                                                                            
                                                                            filter: function(event){
                                                                                if(event != null){
                                                                                    event.preventDefault();
                                                                                }
                                                                                var searchParam = this.searchParam;
                                                                                this.filteredItems = searchParam.length==0 ? this.items : _.filter(this.items, function(item) { 
                                                                                    return item.name.toString().toLowerCase().indexOf(searchParam.toString().toLowerCase())>-1 || item.cnpj==searchParam || item.cpf==searchParam; 
                                                                                });
                                                                                this.filteredItems = _.orderBy(this.filteredItems, ['name'], ['asc']);
                                                                            },
                                                                            
                                                                            
                                                                            select: function(item){
                                                                                this.$eventHub.$emit('select-service', item.id);
                                                                            },
                                                                            
                                                                            
                                                                            addServicos: function(){
                                                                                popup('Adicionar Serviços', '<add-servicos></add-servicos>', this.load);
                                                                            },
                                                                            
                                                                            edit: function(id){
                                                                                popup('Editar Serviços', '<add-servico v-bind:pk="'+id+'"></add-servico>', this.load);
                                                                            },
                                                                            
                                                                            remove: function(id){
                                                                                
                                                                                popup(
                                                                                    "Atenção",
                                                                                    "Confirmar remover este registro e todos os seus itens relacionados?",
                                                                                    function(){}.bind(this),
                                                                                    function(){
                                                                                        $.ajax({
                                                                                            type: "POST",
                                                                                            url: BASEURL+'servicos/remove/',
                                                                                            dataType: 'text',
                                                                                            data: {id: id},
                                                                                            success: function(data){
                                                                                                closeLastModalOpen();
                                                                                                this.load();
                                                                                            }.bind(this),
                                                                                        });
                                                                                    }.bind(this),
                                                                                    )
                                                                                    
                                                                                    
                                                                                },
                                                                            }
                                                                            
                                                                        });
                                                                        
                                                                        
                                                                        
                                                                        
                                                                        
                                                                        
                                                                        
                                                                        
                                                                        
                                                                        Vue.component('add-sku', {
                                                                            template: '#add-sku',
                                                                            props: ['pk', 'pid'],
                                                                            data: function(){
                                                                                return {
                                                                                    id: 0,
                                                                                    name: '',
                                                                                    externalId: '',
                                                                                    productId: 0,
                                                                                    estoque: 0,
                                                                                    estoqueMin: 0,
                                                                                    products: [],
                                                                                    
                                                                                    sending: false,
                                                                                };
                                                                            },
                                                                            created: function(){
                                                                                
                                                                                this.$eventHub.$off('select-product');
                                                                                this.$eventHub.$on('select-product', function(id){
                                                                                    closeLastModalOpen();
                                                                                    console.log("Product id: "+id);
                                                                                    this.productId = id;
                                                                                }.bind(this));
                                                                                
                                                                                
                                                                                this.load(this.pk);
                                                                                try{
                                                                                    $(".form-group:first-child input[type=text]").get(0).focus();
                                                                                }catch(e){}
                                                                            },
                                                                            methods: {
                                                                                
                                                                                
                                                                                
                                                                                load: function(id){
                                                                                    this.loadInitData(
                                                                                        
                                                                                        function(data){
                                                                                            this.products = data;
                                                                                            if((typeof this.pid)!="number"){
                                                                                                this.pid = this.$parent.pid;
                                                                                            }
                                                                                            this.productId = this.pid>0 ? this.pid : this.products[0].id;
                                                                                            if(id > 0){
                                                                                                $.ajax({
                                                                                                    type: "POST",
                                                                                                    url: BASEURL+'skus/get',
                                                                                                    dataType: 'json',
                                                                                                    data: {
                                                                                                        id: id,
                                                                                                    },
                                                                                                    success: function(data){
                                                                                                        this.id = data.id;
                                                                                                        this.name = data.name;
                                                                                                        this.externalId = data.externalId;
                                                                                                        this.productId = data.productId;
                                                                                                        this.productName = data.productName;
                                                                                                        this.estoque = data.estoque;
                                                                                                        this.estoqueMin = data.estoqueMin;
                                                                                                    }.bind(this),
                                                                                                });
                                                                                            }
                                                                                        }.bind(this),
                                                                                        
                                                                                        
                                                                                        );
                                                                                    },
                                                                                    
                                                                                    
                                                                                    
                                                                                    
                                                                                    loadInitData: function(success){
                                                                                        $.ajax({
                                                                                            type: "POST",
                                                                                            url: BASEURL+'products/list-all',
                                                                                            dataType: 'json',
                                                                                            success: success
                                                                                        });
                                                                                        
                                                                                    },
                                                                                    
                                                                                    
                                                                                    
                                                                                    getClassValidation: validation.bind(this),
                                                                                    
                                                                                    validForm: function(){
                                                                                        return this.name.length;
                                                                                    },
                                                                                    
                                                                                    
                                                                                    selectProduct: function(){
                                                                                        popup('Produtos', '<list-products v-bind:movement="true"></list-products>', function(){
                                                                                            this.loadInitData(function(data){
                                                                                                this.cadastros = data;
                                                                                            }.bind(this));
                                                                                        }.bind(this));
                                                                                    },
                                                                                    
                                                                                    
                                                                                    checkForm: function(event){
                                                                                        event.preventDefault();
                                                                                        
                                                                                        if( !this.sending && this.validForm() ){
                                                                                            this.sending = true;
                                                                                            $.ajax({
                                                                                                type: "POST",
                                                                                                url: BASEURL+'skus/add/',
                                                                                                dataType: 'json',
                                                                                                data: {
                                                                                                    id: this.id,
                                                                                                    name: this.name,
                                                                                                    externalId: this.externalId,
                                                                                                    productId: this.productId,
                                                                                                    estoque: this.estoque,
                                                                                                    estoqueMin: this.estoqueMin,
                                                                                                },
                                                                                                success: function(data){
                                                                                                    this.id = data.id;
                                                                                                    this.name = data.name;
                                                                                                    this.externalId = data.externalId;
                                                                                                    this.productId = data.productId;
                                                                                                    this.estoque = data.estoque;
                                                                                                    this.estoqueMin = data.estoqueMin;
                                                                                                    
                                                                                                    this.sending = false;
                                                                                                }.bind(this),
                                                                                            });
                                                                                        }
                                                                                    },
                                                                                    
                                                                                    buttonText: function(){
                                                                                        return this.sending ? 'Aguarde...' : (this.id > 0 ? 'Salvar' : 'Adicionar');
                                                                                    }
                                                                                }
                                                                            });
                                                                            
                                                                            
                                                                            Vue.component('list-skus', {
                                                                                template: '#list-skus',
                                                                                props: ['id', 'movement'],
                                                                                data: function(){
                                                                                    return {
                                                                                        items: [],
                                                                                        filteredItems: [],
                                                                                        searchParam: '',
                                                                                    };
                                                                                },
                                                                                
                                                                                created: function(){
                                                                                    this.load();
                                                                                },
                                                                                
                                                                                beforeDestroy() {
                                                                                    this.$eventHub.$off('select-sku');
                                                                                },
                                                                                
                                                                                methods: {
                                                                                    load: function(){
                                                                                        if(this.id>0){
                                                                                            var endPoint = 'skus/list-all/'+this.id;
                                                                                        }else{
                                                                                            var endPoint = 'skus/list-all';
                                                                                        }
                                                                                        
                                                                                        $.ajax({
                                                                                            type: "GET",
                                                                                            url: BASEURL+endPoint,
                                                                                            dataType: 'json',
                                                                                            data: {},
                                                                                            success: function(data){
                                                                                                this.items = data;
                                                                                                this.filter();
                                                                                            }.bind(this),
                                                                                        });
                                                                                    },
                                                                                    
                                                                                    getReport: function(event){
                                                                                        event.preventDefault();
                                                                                        if( !this.sending ){
                                                                                            this.sending = true;
                                                                                            $.ajax({
                                                                                                type: "POST",
                                                                                                url: BASEURL+'service/get-skus-report/',
                                                                                                dataType: 'json',
                                                                                                data: { obj: JSON.stringify(this.items) },
                                                                                                success: this.handleResult,
                                                                                                error: this.handleResult
                                                                                            });
                                                                                        }
                                                                                    },
                                                                                    
                                                                                    handleResult: function(data){
                                                                                        this.sending = false;
                                                                                        window.open( BASEURL+'service/download-report/'+data.fileName );
                                                                                    },
                                                                                    
                                                                                    filter: function(event){
                                                                                        if(event != null){
                                                                                            event.preventDefault();
                                                                                        }
                                                                                        var searchParam = this.searchParam;
                                                                                        this.filteredItems = searchParam.length==0 ? this.items : _.filter(this.items, function(item) { 
                                                                                            return item.name.toString().toLowerCase().indexOf(searchParam.toString().toLowerCase())>-1 || item.externalId==searchParam; 
                                                                                        });
                                                                                        this.filteredItems = _.orderBy(this.filteredItems, ['name'], ['asc']);
                                                                                    },
                                                                                    
                                                                                    
                                                                                    addSKUS: function(){
                                                                                        if(this.id>0){
                                                                                            popup('Adicionar Variações', '<add-skus v-bind:pid="'+this.id+'"></add-skus>', this.load);
                                                                                        }else{
                                                                                            popup('Adicionar Variações', '<add-skus v-bind:pid="0"></add-skus>', this.load);
                                                                                        }
                                                                                    },
                                                                                    
                                                                                    edit: function(id){
                                                                                        popup('Editar Variações', '<add-sku v-bind:pk="'+id+'"></add-sku>', this.load);
                                                                                    },
                                                                                    
                                                                                    duplicate: function(id){
                                                                                        $.ajax({
                                                                                            type: "POST",
                                                                                            url: BASEURL+'skus/duplicate/',
                                                                                            dataType: 'text',
                                                                                            data: {id: id},
                                                                                            success: function(data){
                                                                                                this.load();
                                                                                            }.bind(this),
                                                                                        });
                                                                                    },
                                                                                    
                                                                                    select: function(item){
                                                                                        this.$eventHub.$emit('select-sku', item.id);
                                                                                    },
                                                                                    
                                                                                    
                                                                                    remove: function(id){
                                                                                        
                                                                                        popup(
                                                                                            "Atenção",
                                                                                            "Confirmar remover este registro e todos os seus itens relacionados?",
                                                                                            function(){}.bind(this),
                                                                                            function(){
                                                                                                $.ajax({
                                                                                                    type: "POST",
                                                                                                    url: BASEURL+'skus/remove/',
                                                                                                    dataType: 'text',
                                                                                                    data: {id: id},
                                                                                                    success: function(data){
                                                                                                        closeLastModalOpen();
                                                                                                        this.load();
                                                                                                    }.bind(this),
                                                                                                });
                                                                                            }.bind(this),
                                                                                            )
                                                                                            
                                                                                            
                                                                                        },
                                                                                        
                                                                                    }
                                                                                    
                                                                                });
                                                                                
                                                                                
                                                                                
                                                                                $(document).on('show.bs.modal', '.modal', function () {
                                                                                    var zIndex = 1040 + (10 * $('.modal:visible').length);
                                                                                    $(this).css('z-index', zIndex);
                                                                                    setTimeout(function() {
                                                                                        $('.modal-backdrop').not('.modal-stack').css('z-index', zIndex - 1).addClass('modal-stack');
                                                                                    }, 0);
                                                                                });
                                                                                
                                                                                var popup = function(title, content, onExit, onConfirm){
                                                                                    var elementId = "#modal-"+$(".modal:visible").length;
                                                                                    var modal = $(elementId);
                                                                                    modal.css('z-index', 1040 + (10 * $('.modal:visible').length) );
                                                                                    if(typeof onConfirm == 'function'){
                                                                                        $(elementId+" > .modal-dialog").removeClass('modal-lg');
                                                                                        $(elementId+" * .btn-secondary").html("Cancelar");
                                                                                    }else{
                                                                                        $(elementId+" > .modal-dialog").addClass('modal-lg');
                                                                                        $(elementId+" * .btn-secondary").html("Fechar");
                                                                                    }
                                                                                    
                                                                                    $(elementId+' * .modal-body').html(content);
                                                                                    $(elementId+' * .modal-title').html(title);
                                                                                    setTimeout(function(){
                                                                                        new Vue({
                                                                                            el: elementId+' * .modal-body'
                                                                                        });
                                                                                        init();
                                                                                        modal.modal("toggle");
                                                                                    }, 0); //cuidado com esse numero em pcs mais lentos, não vacile meu bruxo
                                                                                    modal.unbind("hidden.bs.modal");
                                                                                    (typeof onExit == 'function') ? modal.on("hidden.bs.modal", onExit.bind(this)) : null;
                                                                                    (typeof onConfirm == 'function') ? $(elementId+" * .btn-primary").show().unbind().bind('click', onConfirm.bind(this)) : $(elementId+" * .btn-primary").hide();
                                                                                }
                                                                                
                                                                                
                                                                                var closeLastModalOpen = function(){
                                                                                    var elementId = "#modal-"+($(".modal:visible").length-1);
                                                                                    $(elementId).modal("toggle");
                                                                                }
                                                                                
                                                                                
                                                                                
                                                                                
                                                                                var loadUrl = function(event){
                                                                                    
                                                                                    if(  event==null  ){
                                                                                        var url = parent.location.toString();
                                                                                    }else if((typeof event)=="string"){
                                                                                        var url = event;
                                                                                    }else{
                                                                                        event.preventDefault();
                                                                                        var url = event.currentTarget.href;
                                                                                    }
                                                                                    window.history.pushState(null, null, url);
                                                                                    
                                                                                    $( "#content" ).load( url, function() {
                                                                                        var  app = new Vue({
                                                                                            el: '#content'
                                                                                        });
                                                                                        setTimeout(init, 800);
                                                                                    });
                                                                                }
                                                                                
                                                                                
                                                                                
                                                                                
                                                                                
                                                                                var init = function(){
                                                                                    $(function () {
                                                                                        $('[data-toggle="tooltip"]').tooltip()
                                                                                    })
                                                                                    $('a').each(function(element){
                                                                                        if($(this).data('spa') == true ){
                                                                                            $(this).data('spa', false).click( loadUrl );
                                                                                        }
                                                                                    });
                                                                                    $("[date]").mask('00/00/0000',  {selectOnFocus: true});
                                                                                    $("[month]").mask('00/0000', {selectOnFocus: true});
                                                                                    $('[money]').mask("0000000000.00", {reverse: true, selectOnFocus: true});
                                                                                    
                                                                                }
                                                                                
                                                                                
                                                                                var logout = function(){
                                                                                    window.location = "/security/logout";
                                                                                }
                                                                                
                                                                                $(window).on("popstate", function(e) {
                                                                                    if(e.originalEvent.state == null){
                                                                                        loadUrl();
                                                                                    }
                                                                                });
                                                                                
                                                                                loadUrl();
                                                                                
                                                                                
                                                                                