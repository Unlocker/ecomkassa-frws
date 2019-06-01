function getDocument() {
    $.safeGet("frws/document", function (fragment) {
        $("#content").empty().append(fragment);
    })
}

function getDocumentById() {
    $.safeGet("frws/document/" + $("#document-number").val(), function (fragment) {
        $("#document-body").empty().append(fragment);
    })
}