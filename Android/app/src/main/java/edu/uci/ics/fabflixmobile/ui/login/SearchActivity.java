package edu.uci.ics.fabflixmobile.ui.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivitySearchBinding;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;

import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private EditText username;
    private EditText searchbar;
    private TextView message;

    /*
      In Android, localhost is the address of the device or the emulator.
      To connect to your machine, you need to use the below IP address
     */
    //changed url
    private final String host = "122bbread.com";
    private final String port = "8443";
    private final String domain = "project4";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySearchBinding binding = ActivitySearchBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        searchbar = binding.searchBar;
        message = binding.message;
        final Button loginButton = binding.login;

        //assign a listener to call a function to handle the user request when clicking a button
        loginButton.setOnClickListener(view -> search());
    }

    @SuppressLint("SetTextI18n")
    public void search() {
        message.setText("Searching");
        String fulltextQuery = "search_type=fulltext&fulltext=" + searchbar.getText().toString();
        Intent MovieListPage = new Intent(SearchActivity.this, MovieListActivity.class);
        MovieListPage.putExtra("fulltext", fulltextQuery);
        startActivity(MovieListPage);
    }
}