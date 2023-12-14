fetch('api/orders' + window.location.search, {
    method: 'GET'
})
    .then((response) => response.json())
    .then((responseJson) => {
        this.populateOrders(responseJson);
    })


function populateOrders(response) {
    let itemTableBody = document.querySelector("#item_table_body");

    for (let i = 0; i < response["items"].length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + response["items"][i]["sale_id"] + "</th>";
        rowHTML += "<th>" + response["items"][i]["movie_id"] + "</th>";
        rowHTML += "<th>" + response["items"][i]["date"] + "</th>";
        rowHTML += "<th>" + response["items"][i]["quantity"] + "</th>";
        rowHTML += "</tr>";

        itemTableBody.innerHTML += rowHTML;
    }

}
