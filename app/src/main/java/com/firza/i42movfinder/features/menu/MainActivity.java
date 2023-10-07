package com.firza.i42movfinder.features.menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firza.i42movfinder.R;
import com.firza.i42movfinder.helpers.DataHelper;
import com.firza.i42movfinder.helpers.UpdateService;
import com.firza.i42movfinder.model.api.Api;
import com.firza.i42movfinder.model.api.RetrofitInstance;
import com.firza.i42movfinder.model.response.MovieResponse;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    DataHelper dataHelper;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fetchMovies();
        setup();

        Intent serviceIntent = new Intent(this, UpdateService.class);
        startService(serviceIntent);
    }

    private void setup() {
        dataHelper = new DataHelper(this);
        gson = new Gson();
    }

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Lakukan apa yang Anda inginkan setelah menerima broadcast bahwa pembaruan telah selesai
            Toast.makeText(context, "Data telah diperbarui!", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver,
                new IntentFilter(UpdateService.ACTION_UPDATE_COMPLETED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver);
    }


    private void fetchMovies() {
        Api apiService = RetrofitInstance.getApi();
        Call<MovieResponse> call = apiService.discoverMovie();

        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieResponse movieResponse = response.body();
                    dataHelper.saveMovieList(movieResponse.getResults());
                    String jsonDHMovies = gson.toJson(dataHelper.getMovieList());
                    Log.e("RespMovieList", " " + jsonDHMovies);

                } else {
                    Toast.makeText(MainActivity.this, "Connection lost, please check your Internet!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Connection lost, please check your Internet!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}