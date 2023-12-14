fetch('/api/session', {
    method: 'GET'
})
    .then(function(response) {
        return response.json()
    }).then(function(json) {
        if (json["username"] == "null") {
            document.querySelectorAll("#navbar-login").innerHTML = "<a class='nav-link' href='login.html'>Login</a>"
        }
        else {
            document.querySelectorAll("#navbar-login").innerHTML = "<a class='nav-link' href='#'>Logout</a>"
        }

})
