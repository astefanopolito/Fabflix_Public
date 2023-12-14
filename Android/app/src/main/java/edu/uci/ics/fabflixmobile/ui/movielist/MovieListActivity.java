package edu.uci.ics.fabflixmobile.ui.movielist;

import android.content.Intent;
import android.widget.Button;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import edu.uci.ics.fabflixmobile.databinding.ActivityMovielistBinding;
import org.json.*;
import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MovieListActivity extends AppCompatActivity {
    private final String host = "122bbread.com";
    private final String port = "8443";
    private final String domain = "project4";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;
    final ArrayList<Movie> movies = new ArrayList<>();
    String fulltext = "";
    String page = "";
    MovieListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        ActivityMovielistBinding binding = ActivityMovielistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final Button next = binding.nextButton;
        final Button prev = binding.prevButton;

        if (extras.getString("fulltext") != null) {
            fulltext = extras.getString("fulltext");
        }
        // TODO: this should be retrieved from the backend server
        pullMovies(page);
        adapter = new MovieListViewAdapter(this, movies);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);
        next.setOnClickListener(view -> pullMovies("switch=next"));
        prev.setOnClickListener(view -> pullMovies("switch=prev"));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Movie movie = movies.get(position);
            Intent SingleMoviePage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
            SingleMoviePage.putExtra("movie_id", movie.getId());
            startActivity(SingleMoviePage);
        });
    }

    public void pullMovies(String page) {
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        final StringRequest moviesRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movies?" + fulltext + "&" + page,
                response -> {
                    Log.d("Response", response);
                    fillMovies(response);
                },
                error -> {
                    Log.d("Error", error.toString());
                }) {
        };

        queue.add(moviesRequest);

    }

    public void fillMovies(String response) {
        movies.clear();
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
            Movie m = new Movie(
                    movieObj.get("movie_id").getAsString(),
                    movieObj.get("movie_title").getAsString(),
                    movieObj.get("movie_director").getAsString(),
                    movieObj.get("movie_year").getAsString(),
                    genreList,
                    starList);

            this.movies.add(m);

        }
        adapter.notifyDataSetChanged();
    }
}