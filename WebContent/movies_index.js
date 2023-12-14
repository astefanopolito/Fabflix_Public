
function handleMovieResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let starTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {

        const genreList = []
        for (let j = 0; j < resultData[i]["movie_genres"].length; j++) {
            let genreLink = "<a href='movies_index.html?genre_id=" + resultData[i]["movie_genres"][j]["genre_id"] + "'>" +
                resultData[i]["movie_genres"][j]["genre_name"] + "</a>";
            genreList.push(genreLink);
        }

        const starList = []
        for (let j = 0; j < resultData[i]["movie_stars"].length; j++) {
            let starLink = "<a href='single-star.html?id=" + resultData[i]["movie_stars"][j]["star_id"] + "'>" +
                resultData[i]["movie_stars"][j]["star_name"] + "</a>";
                starList.push(starLink);
        }

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            '<a href="single-movie.html?id=' + resultData[i]["movie_id"] + '">'
        + resultData[i]["movie_title"] + '</a></th>';
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "<th>" + genreList + "</th>";
        rowHTML += "<th>" + starList + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
}




$.ajax({
    url: "api/movies" + window.location.search,
    response: "JSON",
    type: "GET",
    success: (resultData) => handleMovieResult(resultData)
})