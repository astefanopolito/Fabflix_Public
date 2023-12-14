/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating movie info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let movieInfoElement = jQuery("#movie_info");
    let genreListElement = jQuery("#genre_list");
    let starListElement = jQuery("#star_list");

    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append("<h1 class='display-3 text-center'>" + resultData[0]["movie_title"] + "</h1>" +
        "<h6 class='text-center'>Year: " + resultData[0]["movie_year"] + "</h6>" +
        "<h6 class='text-center'>Director: " + resultData[0]["movie_director"] + "</h1>" +
        "<h6 class='text-center'>Rating: " + resultData[0]["movie_rating"] + "</h1>" +
        "<button id='add_to_cart' name='add' class='btn btn-primary mx-auto' value='" + resultData[0]["movie_id"] + "'>" +
        "Add To Cart" +
        "</button>");

    document.querySelector("#add_to_cart").addEventListener("click", () => addToCart());

    for (let i in resultData[0].movie_genres) {
        genreListElement.append(
            '<a class="btn btn-outline-primary m-2" href="movies_index.html?genre_id=' + resultData[0].movie_genres[i]["genre_id"] + '">'
            + resultData[0].movie_genres[i]["genre_name"] +
            '<a/>')
    }

    for (let i in resultData[0].movie_stars) {
        starListElement.append(
            '<a class="btn btn-outline-primary m-2" href="single-star.html?id=' + resultData[0].movie_stars[i]["star_id"] + '">'
            + resultData[0].movie_stars[i]["star_name"] +
            '<a/>')
    }

    console.log("handleResult: populating movie table from resultData");

}

function addToCart() {
    let data = new URLSearchParams();
    data.append('id', movieId);
    data.append('action', 'add');
    fetch('api/editcart', {
        method: 'POST',
        body: data
    })
        .then(function(response) {
            return response.json();
        }).then(function(json) {
        console.log(json["status"])
        alert("Successfully added movie to cart.");
    })
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});