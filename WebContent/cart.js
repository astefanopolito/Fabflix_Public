fetch('api/cart' + window.location.search, {
    method: 'GET'
})
    .then((response) => response.json())
    .then((responseJson) => {
        this.populateCart(responseJson);
    })

document.querySelector("#checkout_button").addEventListener('click', function () {
    window.location.replace("payment.html");
})

function increment(id) {
    let data = new URLSearchParams();
    data.append('id', id);
    data.append('action', 'add');
    fetch('api/editcart', {
        method: 'POST',
        body: data
    })
        .then(function() {
            window.location.reload();
        })
}

function decrement(id) {
    let data = new URLSearchParams();
    data.append('id', id);
    data.append('action', 'remove');
    fetch('api/editcart', {
        method: 'POST',
        body: data
    })
        .then(function() {
            window.location.reload();
        })
}

function populateCart(response) {
    let itemTableBody = document.querySelector("#item_table_body");
    let grandTotal = document.querySelector("#grand_total");

    for (let i = 0; i < response["items"].length; i++) {
        let movieId = response["items"][i]["id"];
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + response["items"][i]["title"] + "</th>";
        rowHTML += "<th>" +
            "<input type='button' class='btn btn-primary' value='-' onclick='decrement(\"" + movieId + "\")'>" +
            " " + response["items"][i]["quantity"] + " " +
            "<input type='button' class='btn btn-primary' value='+' onclick='increment(\"" + movieId + "\")'>" +
            "</th>";
        rowHTML += "<th>" + response["items"][i]["price"] + "</th>";
        rowHTML += "</tr>";

        itemTableBody.innerHTML += rowHTML;
    }

    grandTotal.append(parseFloat(response["total"]).toFixed(2));
}
