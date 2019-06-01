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