

function handleLists(resultData) {
    let genreList = $("#genre_list");
    let characterList = $("#character_list");

    for (let i = 0; i < resultData["genres"].length; i++) {
        genreList.append(
            "<a href='movies_index.html?genre_id=" + resultData["genres"][i]["genre_id"] + "' class='btn btn-outline-primary m-2'>" +
            resultData["genres"][i]["genre_name"] + "</a>")
    }

    for (let i = 0; i < resultData["alphanumerics"].length; i++) {
        characterList.append(
            "<a href='movies_index.html?title_start=" + resultData["alphanumerics"].charAt(i) + "' class='btn btn-outline-primary m-2'>" +
            resultData["alphanumerics"].charAt(i) + "</a>")
    }

}

$.ajax({
    dataType: "JSON",
    url: "api/search",
    method: "GET",
    success: (resultData) => handleLists(resultData)

})