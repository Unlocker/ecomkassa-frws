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
            $("#document-body").html(JSON.stringify(fragment, null, 3));
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
