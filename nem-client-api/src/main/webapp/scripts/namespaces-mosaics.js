"use strict";

define(['jquery', 'ncc', 'NccLayout', 'Utils'], function($, ncc, NccLayout, Utils) {
    return $.extend(true, {}, NccLayout, {
        name: 'namespaces-mosaics',
        url: 'namespaces-mosaics.html',
        template: 'rv!layout/namespaces-mosaics',
        parent: 'wallet',
        local: {
        	scrollBottomTolerance: 100
        },
        initOnce: function() {
            /**
             * @param {string} type load type: 'reload' | 'update' | 'append', default is 'reload'
             */
		},
    	setupEverytime: function() {
            var remoteServer = ncc.get('settings.remoteServer.protocol') + "://" + ncc.get('settings.remoteServer.host') + ":" + ncc.get('settings.remoteServer.port');

            //namespace root
            var url = remoteServer + '/namespace/root/page?';
            var outputUrls = [];
            ncc.set('namespaces.all', null);
            ncc.set('namespaces.urls', null);
            outputUrls.push({
                "url":url
            });

            // order is as follows:
            // 1. take owners of root namespaces
            // 2. take all namespaces owned by rootOwners (chain the requests... and collect results)
            // 3. take all mosaics from above namespaces
            $.getJSON(url, function getRootNamespaces(rootNamespaces) {
                var output = [];
                var ownersList = [];
                var mosaicOutputs = [];

                var allOwnerNamespaces = [];
                var namespaceRequests = [];
                for (var rootNs of rootNamespaces.data) {
                    //namespaces owned by root owners
                    // http://127.0.0.1:7890/namespace/mosaic/definition/page?namespace=jabo38_ltd
                    //http://127.0.0.1:7890/account/namespace/page?address=TBGIMRE4SBFRUJXMH7DVF2IBY36L2EDWZ37GVSC4

                    // do we already know this owner account?
                    var hasOwner = $.inArray(rootNs.namespace.owner, ownersList);
                    if (hasOwner < 0) {
                        var url2 = remoteServer + '/account/namespace/page?address=' + rootNs.namespace.owner;
                        outputUrls.push({"url":url2});

                        namespaceRequests.push( $.getJSON(url2, function pushNamespaces(ownerNamespaces) {
                            allOwnerNamespaces.push(ownerNamespaces);
                        }) );
                    } //end check if we already know this owner.
                    ownersList.push(rootNs.namespace.owner);
                } //end namespace root for

                // wrapper to pass url into the closure
                var getMosaics = function getMosaics(url) {
                    return function getMosaicsReal(){
                        $.getJSON(url, function(ownerMosaics) {
                            for (var curMosaic of ownerMosaics.data) {
                                mosaicOutputs.push(curMosaic.mosaic);
                            }
                        });
                    };
                };
                $.when.apply($, namespaceRequests).then(function(){
                    var index = 0;
                    for (var ownerNamespaces of allOwnerNamespaces) {
                        for (var curNamespace of ownerNamespaces.data) {
                            //mosaics lookup
                            var url3 = remoteServer + '/namespace/mosaic/definition/page?namespace=' + curNamespace.fqn;
                            outputUrls.push({"url":url3});

                            // since we're using directly NIS API, we need to delay requests
                            // not to trigger DoS filter
                            setTimeout(getMosaics(url3), 30*index);
                            index++;

                            output.push(curNamespace);
                        } //end namespaces owned by root owners for
                    }
                });

                ncc.set('namespaces.all', output);
                ncc.set('mosaics.all', mosaicOutputs);
                ncc.set('namespaces.urls', outputUrls);
            }); //end namespace root


            //owned mosaics
            //http://127.0.0.1:7890/account/mosaic/owned?address=TD3RXTHBLK6J3UD2BH2PXSOFLPWZOTR34WCG4HXH
            var currAccount = ncc.get('activeAccount.address');
            var url4 = remoteServer + '/account/mosaic/owned?address=' + currAccount;
            var mosaicOwnedOutputs = [];
            $.getJSON(url4, function(data4) {
                for (var i4 in data4.data) {
                    mosaicOwnedOutputs.push({
                        "quantity":data4.data[i4].quantity,
                        "namespaceId":data4.data[i4].mosaicId.namespaceId,
                        "name":data4.data[i4].mosaicId.name,
                    });
                } //end owned mosaics for
            }); //end owned mosaics

            ncc.set('mosaics.owned', mosaicOwnedOutputs);
        },
    leave: [function() {}]
    });
});