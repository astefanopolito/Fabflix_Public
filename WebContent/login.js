let login_form = document.querySelector("#login_form");
let login_button = document.querySelector("#login_button");
let demo_button = document.querySelector("#demo_button");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */

function handleLoginResult(resultDataJson) {

    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") {
        window.location.replace("homepage.html");
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        window.grecaptcha.reset();
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#login_error_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formData) {
    console.log("submit login form");
    let data = new URLSearchParams(formData);
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    fetch (
        "api/login", {
            method: "POST",
            body: data
        }
    )
        .then(function (response) {
            return response.json();
        })
        .then(function (json) {
            handleLoginResult(json);
        })

}

login_button.addEventListener('click', () => submitLoginForm(new FormData(login_form)));
demo_button.addEventListener('click', () => {
    let data = new FormData();
    data.append("username", "demo");
    data.append("g-recaptcha-response", window.grecaptcha.getResponse());
    submitLoginForm(data);
});
