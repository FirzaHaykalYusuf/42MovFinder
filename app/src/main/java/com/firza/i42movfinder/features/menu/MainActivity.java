package com.firza.i42movfinder.features.menu;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firza.i42movfinder.R;
import com.firza.i42movfinder.model.api.Api;
import com.firza.i42movfinder.model.api.RetrofitInstance;
import com.firza.i42movfinder.model.response.MovieResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fetchMovies();
    }

    private void fetchMovies() {
        Api apiService = RetrofitInstance.getApi();
        Call<MovieResponse> call = apiService.discoverMovie();

        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieResponse movieResponse = response.body();
                    for (int i = 0; i < movieResponse.getResults().size(); i++) {
                        Log.e("RespMovieList", " " + movieResponse.getResults().get(i).getVoteCount());
                    }
                    // Lakukan sesuatu dengan data yang diterima, misalnya:
                    // updateUI(movieResponse);
                } else {
                    Toast.makeText(MainActivity.this, "Error Nih", Toast.LENGTH_SHORT).show();
                    // Tampilkan error atau pesan ke user
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                // Tampilkan error atau pesan ke user
            }
        });
    }
}