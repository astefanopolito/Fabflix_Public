let paymentForm = document.querySelector("#payment_form");
let checkoutButton = document.querySelector("#checkout_button");
let demoButton = document.querySelector("#demo_button");

function submitPaymentForm(formData) {
    console.log("submit payment form");
    let data = new URLSearchParams(formData);

    fetch (
        "api/payment", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            body: data
        }
    )
        .then(function (response) {
            return response.json();
        })
        .then(function (json) {
            alert(json["status"]);
        })
}

// Bind the submit action of the form to a handler function
checkoutButton.addEventListener('click', () => submitPaymentForm(new FormData(paymentForm)));
demoButton.addEventListener('click', () => {
    let data = new FormData();
    data.append("firstname", "demo");
    submitPaymentForm(data);
});
