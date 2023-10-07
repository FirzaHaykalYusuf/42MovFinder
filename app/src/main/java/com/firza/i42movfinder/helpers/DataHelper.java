package com.firza.i42movfinder.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.firza.i42movfinder.model.response.MovieResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class DataHelper {

    public static final String PREFERENCE_KEY = "42MovFinder";
    public static final String MOVIE_LIST_KEY = "MOVIE_LIST";

    public SharedPreferences sp;
    public Activity activity;
    public Context context;
    private Gson gson;

    public DataHelper(Activity activity) {
        sp = activity.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public DataHelper(Context context) {
        sp = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveMovieList(List<MovieResponse.Result> movieList) {
        SharedPreferences.Editor editor = sp.edit();
        String json = gson.toJson(movieList);
        editor.putString(MOVIE_LIST_KEY, json);
        editor.apply();
    }

    public List<MovieResponse.Result> getMovieList() {
        String json = sp.getString(MOVIE_LIST_KEY, null);
        Type type = new TypeToken<List<MovieResponse.Result>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void clearData() {
        sp.edit().clear().commit();
    }
}
