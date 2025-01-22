$.postJSON = function (url, data, successCallback) {
    return jQuery.ajax({
        type: "POST",
        url: url,
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: successCallback,
        error: function (httpObj, textStatus) {
            if (httpObj.status === 401){
                window.location = "/auth";
            }
        }
    });
};

$.safeGet = function (url, successCallback) {
    return jQuery.ajax({
        type: "GET",
        url: url,
        contentType: 'application/json',
        success: successCallback,
        error: function (httpObj, textStatus) {
            if (httpObj.status === 401){
                window.location = "/auth";
            }
        }
    });
};