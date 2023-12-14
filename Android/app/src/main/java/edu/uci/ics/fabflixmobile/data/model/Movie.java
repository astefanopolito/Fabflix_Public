package edu.uci.ics.fabflixmobile.data.model;

import java.util.ArrayList;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private final String id;
    private final String name;
    private final String director;
    private final String year;
    private final ArrayList<String> genreList;
    private final ArrayList<String> starList;

    public Movie(String id, String name, String director, String year, ArrayList<String> genreList, ArrayList<String> starList) {
        this.id = id;
        this.name = name;
        this.director = director;
        this.year = year;
        this.genreList = genreList;
        this.starList = starList;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public String getDirector() {return director;}
    public String getYear() {
        return year;
    }

    public ArrayList<String> getGenreList() {return genreList;}

    public ArrayList<String> getStarList() {return starList;}





}