package com.firza.i42movfinder.model.api;

import com.firza.i42movfinder.model.response.MovieResponse;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Api {
    @GET("3/discover/movie")
    Call<MovieResponse> discoverMovie();
}
