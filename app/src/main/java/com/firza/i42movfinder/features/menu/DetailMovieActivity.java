package com.firza.i42movfinder.features.menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.firza.i42movfinder.R;
import com.firza.i42movfinder.database.AppDatabase;
import com.firza.i42movfinder.database.MovieEntity;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DetailMovieActivity extends AppCompatActivity {
    private AppDatabase db;
    ImageView ivMovie;
    TextView tvMovieTitle, tvMovieRating, tvMovieVoteCount, tvMoviePopularity, tvMovieOverview;
    RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_movie);
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "movies-db").build();

        ivMovie = findViewById(R.id.ivMovie);
        tvMovieTitle = findViewById(R.id.tvMovieTitle);
        tvMovieRating = findViewById(R.id.tvMovieRating);
        tvMovieVoteCount = findViewById(R.id.tvMovieVoteCount);
        tvMoviePopularity = findViewById(R.id.tvMoviePopularity);
        tvMovieOverview = findViewById(R.id.tvMovieOverview);
        ratingBar = findViewById(R.id.ratingBar);


        int movieId = getIntent().getIntExtra("movieId", -1);
        if (movieId != -1) {
            new GetMovieTask().execute(movieId);
        }
    }

    private class GetMovieTask extends AsyncTask<Integer, Void, MovieEntity> {
        @Override
        protected MovieEntity doInBackground(Integer... movieIds) {
            return db.movieDao().getMovieById(movieIds[0]);
        }

        @Override
        protected void onPostExecute(MovieEntity movie) {
            super.onPostExecute(movie);
            if (movie != null) {
                String baseUrl = "https://image.tmdb.org/t/p/w500";
                String imageUrl = baseUrl + movie.getPosterPath();
                Picasso.get()
                        .load(imageUrl)
                        .into(ivMovie);

                tvMovieTitle.setText(movie.getTitle());
                tvMovieRating.setText(String.valueOf(movie.getVoteAverage()));
                tvMovieVoteCount.setText(String.valueOf(movie.getVoteCount()));
                tvMovieOverview.setText(movie.getOverview());
                ratingBar.setRating((float) (movie.getVoteAverage() / 2));

                double popularity = movie.getPopularity();

                DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.getDefault());
                formatSymbols.setGroupingSeparator('.');
                formatSymbols.setDecimalSeparator(',');

                DecimalFormat formatter = new DecimalFormat("#,###.###", formatSymbols);
                String formattedPopularity = formatter.format(popularity);

                tvMoviePopularity.setText(formattedPopularity);

            }
        }
    }
}