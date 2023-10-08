package com.firza.i42movfinder.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firza.i42movfinder.R;
import com.firza.i42movfinder.features.menu.DetailMovieActivity;
import com.firza.i42movfinder.model.response.MovieResponse;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieViewHolder> {

    private List<MovieResponse.Result> movies;
    private Context context;

    public MovieListAdapter(List<MovieResponse.Result> movies) {
        this.movies = movies;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        MovieResponse.Result movie = movies.get(position);
        holder.tvMovieTitle.setText(movie.getTitle());
        holder.tvMovieAdult.setText("R13+");

        String baseUrl = "https://image.tmdb.org/t/p/w500";
        String imageUrl = baseUrl + movie.getPosterPath();
        Picasso.get()
                .load(imageUrl)
                .into(holder.ivMovie);

//        holder.llItem.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MovieResponse.Result selectedMovie = movies.get(position);
//                Gson gson = new Gson();
//                String movieJson = gson.toJson(selectedMovie);
//
//                Intent detailIntent = new Intent(v.getContext(), DetailMovieActivity.class);
//                detailIntent.putExtra("movie", movieJson);
//
//                v.getContext().startActivity(detailIntent);
//            }
//        });

        holder.llItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MovieResponse.Result selectedMovie = movies.get(position);
                Intent detailIntent = new Intent(v.getContext(), DetailMovieActivity.class);
                detailIntent.putExtra("movieId", selectedMovie.getId());
                v.getContext().startActivity(detailIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public void updateData(List<MovieResponse.Result> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieTitle, tvMovieAdult;
        ImageView ivMovie;
        LinearLayout llItem;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvMovieAdult = itemView.findViewById(R.id.tvMovieAdult);
            ivMovie = itemView.findViewById(R.id.ivMovie);
            llItem = itemView.findViewById(R.id.llItem);
        }
    }
}
