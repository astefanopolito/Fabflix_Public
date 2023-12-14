let create_star_form = $("#insert_star_form");
let create_movie_form= $("#insert_movie_form");

function handleFormResult(resultData) {
    window.alert(resultData["message"]);
}


function handleResultData(resultData) {
    let metadataDiv = $("#metadata");

    let inString = "";
    for (let i = 0; i < resultData.length; i++) {
        inString += "<h2>" + resultData[i]["table"] + "</h2><br>";
        for (let j = 0; j < resultData[i]["contents"].length; j++) {
            inString += "<p>" + resultData[i]["contents"][j]["field"] +
                " " + resultData[i]["contents"][j]["type"] + "</p>"
        }
    }
    metadataDiv.append(inString);
}


function submitCreateStarForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/dashboard", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: create_star_form.serialize(),
            success: handleFormResult
        }
    );
}

function submitCreateMovieForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/dashboard", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: create_movie_form.serialize(),
            success: handleFormResult
        }
    );
}

$.ajax({
    url: "api/dashdata",
    method: "GET",
    type: "json",
    success: (resultData) => handleResultData(resultData)
})

// Bind the submit action of the form to a handler function
create_star_form.submit(submitCreateStarForm);
create_movie_form.submit(submitCreateMovieForm);
