fetch('navbar.html')
    .then(function(response) {
        return response.text()
    }).then(function(body) {
    document.querySelector('#navbar').innerHTML = body;
    createLogout();
})


function createLogout() {
    fetch('api/session', {
        method: 'GET'
    })
        .then(function(response) {
            return response.json()
        }).then(function(json) {
        if (json["username"] != null) {
            document.querySelector("#navbar-login").innerHTML += "<a class='nav-link' id='orders_button' href='orders.html'>Orders</a>"
            document.querySelector("#navbar-login").innerHTML += "<a class='nav-link' id='cart_button' href='cart.html'>Cart</a>"
            document.querySelector("#navbar-login").innerHTML += "<a class='nav-link' id='logout_button' href=''>Logout</a>"
            const logoutButton = document.querySelector('#logout_button');
            logoutButton.addEventListener('click', () => logout());
        }
        else {
            document.querySelector("#navbar-login").innerHTML = "<a class='nav-link' href='login.html'>Login</a>"
        }

    })
}



function logout() {
    fetch('api/logout', {
        method: 'GET'
    })
        .then(function(response) {
            return response.json();
        }).then(function(json) {
            console.log(json["status"])
            location.reload(true);
        })
}










