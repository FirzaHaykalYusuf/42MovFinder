package com.firza.i42movfinder.features.menu;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.firza.i42movfinder.R;
import com.firza.i42movfinder.adapter.BannerAdapter;
import com.firza.i42movfinder.adapter.MovieListAdapter;
import com.firza.i42movfinder.database.AppDatabase;
import com.firza.i42movfinder.database.MovieDao;
import com.firza.i42movfinder.database.MovieEntity;
import com.firza.i42movfinder.helpers.DataHelper;
import com.firza.i42movfinder.helpers.UpdateService;
import com.firza.i42movfinder.helpers.UpdateWorker;
import com.firza.i42movfinder.model.api.Api;
import com.firza.i42movfinder.model.api.RetrofitInstance;
import com.firza.i42movfinder.model.response.MovieResponse;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    DataHelper dataHelper;
    Gson gson;
    ViewPager2 bannerViewPager;
    int[] bannerDrawables = {
            R.drawable.img_banner_app,
            R.drawable.img_banner_app_2
    };

    EditText etSearch;

    List<MovieResponse.Result> movieList = new ArrayList<>();
    private int currentPage = 0;
    private final long DELAY = 4000;
    private final long PERIOD = 8000;
    private Handler handler;
    private Timer timer;
    private final int NUM_PAGES = bannerDrawables.length;
    private boolean isReversed = false;
    private MovieListAdapter movieListAdapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "movies-db").build();
        createNotificationChannel();

        PeriodicWorkRequest updateRequest = new PeriodicWorkRequest.Builder(UpdateWorker.class, 15, TimeUnit.MINUTES)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueue(updateRequest);

        fetchMovies();
        setup();

        Intent serviceIntent = new Intent(this, UpdateService.class);
        startService(serviceIntent);
    }

    private void setup() {
        dataHelper = new DataHelper(this);
        gson = new Gson();
        etSearch = findViewById(R.id.etSearch);

        BannerAdapter bannerAdapter = new BannerAdapter(bannerDrawables);
        bannerViewPager = findViewById(R.id.bannerViewPager);
        bannerViewPager.setAdapter(bannerAdapter);

        RecyclerView recyclerView = findViewById(R.id.moviesRecyclerView);
        movieListAdapter = new MovieListAdapter(new ArrayList<>()); // Mulai dengan list kosong
        recyclerView.setAdapter(movieListAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2); // 2 berarti dua kolom
        recyclerView.setLayoutManager(gridLayoutManager);

        startAutoSlide();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchMovies(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAutoSlide();
        fetchMovies();
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver,
                new IntentFilter(UpdateService.ACTION_UPDATE_COMPLETED));

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);

        PeriodicWorkRequest updateRequest = new PeriodicWorkRequest.Builder(UpdateWorker.class, 15, TimeUnit.MINUTES)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueue(updateRequest);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoSlide();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver);
        unregisterReceiver(networkReceiver);
    }

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isConnectedToInternet()) {
                Snackbar.make(findViewById(R.id.root_layout), "Data success to update!", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(findViewById(R.id.root_layout), "Data failed update!", Snackbar.LENGTH_LONG).show();
            }
        }
    };

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isConnectedToInternet()) {
                fetchMovies();
                Snackbar.make(findViewById(R.id.root_layout), "Data success to update!", Snackbar.LENGTH_LONG).show();
            }
        }
    };

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "42MovFinder",
                    "Network Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public void fetchMovies() {
        movieList.clear();
        if (isConnectedToInternet()) {
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
                        movieList.addAll(movieResponse.getResults());

                        // Update the adapter
                        movieListAdapter.updateData(movieResponse.getResults());

                        List<MovieEntity> movieEntities = convertToMovieEntities(response.body().getResults());
                        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "movies-db").build();
                        new InsertMoviesTask(db.movieDao()).execute(movieEntities);

                    } else {
                        Toast.makeText(MainActivity.this, "Connection lost, please check your Internet!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
//                    Toast.makeText(MainActivity.this, "Connection lost, please check your Internet!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Connection lost, please check your Internet!", Toast.LENGTH_SHORT).show();
        }
    }

    private List<MovieEntity> convertToMovieEntities(List<MovieResponse.Result> movieResults) {
        List<MovieEntity> movieEntities = new ArrayList<>();

        for (MovieResponse.Result movieResult : movieResults) {
            MovieEntity movieEntity = new MovieEntity();

            movieEntity.setId(movieResult.getId());
            movieEntity.setTitle(movieResult.getTitle());
            movieEntity.setPosterPath(movieResult.getPosterPath());
            movieEntity.setOverview(movieResult.getOverview());
            movieEntity.setVoteAverage(movieResult.getVoteAverage());
            movieEntity.setVoteCount(movieResult.getVoteCount());
            movieEntity.setPopularity(movieResult.getPopularity());

            movieEntities.add(movieEntity);
        }

        return movieEntities;
    }

    private static class InsertMoviesTask extends AsyncTask<List<MovieEntity>, Void, Void> {
        private MovieDao movieDao;

        public InsertMoviesTask(MovieDao movieDao) {
            this.movieDao = movieDao;
        }

        @Override
        protected Void doInBackground(List<MovieEntity>... lists) {
            movieDao.insertAll(lists[0]);
            return null;
        }
    }

    public boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void searchMovies(String query) {
        if (query.isEmpty()) {
            movieListAdapter.updateData(movieList);
        } else {
            List<MovieResponse.Result> filteredList = new ArrayList<>();
            for (MovieResponse.Result movie : movieList) {
                if (movie.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(movie);
                }
            }
            movieListAdapter.updateData(filteredList);
        }
    }

    private void startAutoSlide() {
        final Runnable update = new Runnable() {
            @Override
            public void run() {
                if (!isReversed) {
                    if (currentPage < NUM_PAGES - 1) {
                        currentPage++;
                    } else {
                        isReversed = true;
                        currentPage--;
                    }
                } else {
                    if (currentPage > 0) {
                        currentPage--;
                    } else {
                        isReversed = false;
                        currentPage++;
                    }
                }

                bannerViewPager.setCurrentItem(currentPage, true);
                Log.d("AutoSlide", "Page changed to: " + currentPage);
            }
        };

        handler = new Handler();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(update);
            }
        }, DELAY, PERIOD);
    }

    private void stopAutoSlide() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

}