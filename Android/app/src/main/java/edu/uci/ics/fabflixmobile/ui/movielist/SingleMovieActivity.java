package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.databinding.ActivitySinglemovieBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SingleMovieActivity extends AppCompatActivity {
    private final String host = "122bbread.com";
    private final String port = "8443";
    private final String domain = "project4";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    TextView titleView;
    TextView yearView;
    TextView directorView;
    TextView genresView;
    TextView starsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySinglemovieBinding binding = ActivitySinglemovieBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // TODO: this should be retrieved from the backend server
        Bundle extras = getIntent().getExtras();
        titleView = binding.movieTitle;
        yearView = binding.movieYear;
        directorView = binding.movieDirector;
        genresView = binding.movieGenres;
        starsView = binding.movieStars;
        pullMovie(extras.getString("movie_id"));

    }

    public void pullMovie(String movieId) {
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        final StringRequest moviesRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/single-movie?id=" + movieId,
                response -> {
                    Log.d("Response", response);
                    fillMovie(response);
                },
                error -> {
                    Log.d("Error", error.toString());
                }) {
        };

        queue.add(moviesRequest);

    }

    public void fillMovie(String response) {
        JsonArray moviesArray = new Gson().fromJson(response, JsonArray.class);
        for (int i = 0; i < moviesArray.size(); i++) {
            JsonObject movieObj = moviesArray.get(i).getAsJsonObject();
            JsonArray genres = movieObj.get("movie_genres").getAsJsonArray();
            JsonArray stars = movieObj.get("movie_stars").getAsJsonArray();
            ArrayList<String> genreList = new ArrayList<>();
            ArrayList<String> starList = new ArrayList<>();
            for (int j = 0; j < genres.size(); j++) {
                genreList.add(genres.get(j).getAsJsonObject().get("genre_name").getAsString());
            }
            for (int k = 0; k < stars.size(); k++) {
                starList.add(stars.get(k).getAsJsonObject().get("star_name").getAsString());
            }

            titleView.setText(movieObj.get("movie_title").getAsString());
            yearView.setText(movieObj.get("movie_year").getAsString());
            directorView.setText(movieObj.get("movie_director").getAsString());
            genresView.setText(String.join(", ", genreList));
            starsView.setText(String.join(", ", starList));
        }
    }
}