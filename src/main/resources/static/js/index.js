window.onload = function func1() {
    settings()
};


function getDocument() {
    $.safeGet("frws/document", function (fragment) {
        $("#content").empty().append(fragment);
    })
}

function getDocumentById() {
    var val = $("#document-number").val();
    if (val != null && val !== "") {
        $.safeGet("frws/document/" + val, function (fragment) {
            $("#document-body").html(JSON.stringify(JSON.parse(fragment), null, 3));
        })
    }
}

function settings() {
    $.safeGet("frws/settings", function (fragment) {
        $("#content").empty().append(fragment);
    })
}


function management() {
    $.safeGet("frws/management", function (fragment) {
        $("#content").empty().append(fragment);
    })
}

function openFRWS() {
    $.safeGet("frws/management/open", function (fragment) {
        $("#response-body").html(JSON.stringify(fragment, null, 3));
    })
}

function closeFRWS() {
    $.safeGet("frws/management/close", function (fragment) {
        $("#response-body").html(JSON.stringify(fragment, null, 3));
    })
}

function sendStatus() {
    $.safeGet("frws/backend/status", function (fragment) {
        $("#content").empty().append("<pre id=\"document-body\"><code>" + JSON.stringify(fragment, null, 3) + "</code></pre>");
    })
}

function registerFrws() {
    $.safeGet("frws/register", function (fragment) {
        $("#content").empty().append(fragment);
    })
}

function registerLayout() {
    $.safeGet("frws/registerLayout", function (fragment) {
        $("#register-body").empty().append(fragment);
    })
}

function reRegisterLayout() {
    $.safeGet("frws/reRegisterLayout", function (fragment) {
        $("#register-body").empty().append(fragment);
    })
}

function postRegisterData() {
    var data = collectRegisterData();
    if (data === null) {
        return;
    }
    $.postJSON("frws/postRegisterData", data, function (fragment) {
        $("#response-body").html(JSON.stringify(JSON.parse(fragment), null, 3));
        if ($("#check-mode").is(':checked')) {
            $(".info-label").show();
        } else {
            $(".info-label").hide();
        }
    })
}

function postReRegisterData() {
    var data = collectRegisterData();
    if (data === null) {
        return;
    }

    console.log();
    data.document.data.fiscprops.push(
        {
            tag: 1057,
            value: 1 << $('#param-update-reason option:selected').val()
        },
        {
            tag: 1205,
            value: 1 << $('#param-update-reason-bit option:selected').val()
        });

    $.postJSON("frws/postReRegisterData", data, function (fragment) {
        $("#response-body").html(JSON.stringify(JSON.parse(fragment), null, 3));
        if ($("#check-mode").is(':checked')) {
            $(".info-label").show();
        } else {
            $(".info-label").hide();
        }
    })
}

function collectRegisterData() {
    var registerLayout = $('#register-layout input');
    registerLayout.each(function (ind, itm) {
        $(itm).removeClass("require-field");
    });

    var payment = $("#payment-address");
    if (!$.trim(payment.val()).length) {
        payment.addClass("require-field")
    }

    var inn = $("#inn");
    if (!$.trim(inn.val()).length) {
        inn.addClass("require-field")
    }

    var cashier = $("#cashier");
    if (!$.trim(cashier.val()).length) {
        cashier.addClass("require-field")
    }

    var userName = $("#user-name");
    if (!$.trim(userName.val()).length) {
        userName.addClass("require-field")
    }

    var paymentPlace = $("#payment-place");
    if (!$.trim(paymentPlace.val()).length) {
        paymentPlace.addClass("require-field")
    }

    var autoMode = $("#auto-mode").is(':checked');
    var offlineMode = $("#offline-mode").is(':checked');
    var encryption = $("#encryption").is(':checked');
    var paymentOnlyInternet = $("#payment-only-internet").is(':checked');
    var paymentForServices = $("#payment-for-services").is(':checked');
    var asBso = $("#as-bso").is(':checked');
    var lottery = $("#lottery").is(':checked');
    var gambling = $("#gambling").is(':checked');
    var excisableGoods = $("#excisable-goods").is(':checked');
    var printerInMachine = $("#printer-in-machine").is(':checked');
    var checkMode = $("#check-mode").is(':checked');

    var innOfd = $("#inn-ofd");
    if (!$.trim(innOfd.val()).length && !offlineMode) {
        innOfd.addClass("require-field")
    }

    var ofdName = $("#ofd-name");
    if (!$.trim(ofdName.val()).length && !offlineMode) {
        ofdName.addClass("require-field")
    }

    var fnsSite = $("#fns-site");
    if (!$.trim(fnsSite.val()).length && !offlineMode) {
        fnsSite.addClass("require-field")
    }

    var emailSender = $("#email-sender");
    if (!$.trim(emailSender.val()).length && !offlineMode) {
        emailSender.addClass("require-field")
    }

    var kktRegisterNumber = $("#kkt-register-number");
    if (!$.trim(kktRegisterNumber.val()).length) {
        kktRegisterNumber.addClass("require-field")
    }

    var flag = false;
    registerLayout.each(function (ind, itm) {
        if ($(itm).hasClass("require-field")) {
            flag = true;
        }
    });

    if (flag) {
        return null;
    }

    return {
        document: {
            printOnly: checkMode ? 1 : 0,
            data: {
                fiscprops: [
                    {
                        tag: 1009,
                        value: payment.val()
                    },
                    {
                        tag: 1018,
                        value: inn.val()
                    },
                    {
                        tag: 1021,
                        value: cashier.val()
                    },
                    {
                        tag: 1048,
                        value: userName.val()
                    },
                    {
                        tag: 1187,
                        value: paymentPlace.val()
                    },
                    {
                        tag: 1001,
                        value: autoMode ? 1 : 0
                    },
                    {
                        tag: 1002,
                        value: offlineMode ? 1 : 0
                    },
                    {
                        tag: 1017,
                        value: innOfd.val()
                    },
                    {
                        tag: 1046,
                        value: ofdName.val()
                    },
                    {
                        tag: 1056,
                        value: encryption ? 1 : 0
                    },
                    {
                        tag: 1060,
                        value: fnsSite.val()
                    },
                    {
                        tag: 1108,
                        value: paymentOnlyInternet ? 1 : 0
                    },
                    {
                        tag: 1109,
                        value: paymentForServices ? 1 : 0
                    },
                    {
                        tag: 1110,
                        value: asBso ? 1 : 0
                    },
                    {
                        tag: 1117,
                        value: emailSender.val()
                    },
                    {
                        tag: 1126,
                        value: lottery ? 1 : 0
                    },
                    {
                        tag: 1193,
                        value: gambling ? 1 : 0
                    },
                    {
                        tag: 1207,
                        value: excisableGoods ? 1 : 0
                    },
                    {
                        tag: 1221,
                        value: printerInMachine ? 1 : 0
                    },
                    {
                        tag: 1037,
                        value: kktRegisterNumber.val()
                    },
                    {
                        tag: 1057,
                        value: 1 << $('#reason-for-assigning option:selected').val()
                    },
                    {
                        tag: 1062,
                        value: 1 << $('#taxation-systems option:selected').val()
                    }
                ]
            }
        }
    }
}
