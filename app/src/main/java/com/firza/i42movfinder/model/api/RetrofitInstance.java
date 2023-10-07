package com.firza.i42movfinder.model.api;

import com.firza.i42movfinder.model.util.Constant;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {
    private static Retrofit retrofit = null;

    public static Api getApi() {
        if (retrofit == null) {
//            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

//            OkHttpClient client = new OkHttpClient.Builder()
//                    .addInterceptor(logging)
//                    .connectTimeout(5, TimeUnit.MINUTES)
//                    .readTimeout(5, TimeUnit.MINUTES)
//                    .build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.MINUTES)
                    .readTimeout(5, TimeUnit.MINUTES)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request originalRequest = chain.request();
                            HttpUrl originalHttpUrl = originalRequest.url();

                            HttpUrl url = originalHttpUrl.newBuilder()
                                    .addQueryParameter("api_key", "f7b67d9afdb3c971d4419fa4cb667fbf")
                                    .build();

                            Request.Builder requestBuilder = originalRequest.newBuilder().url(url);
                            Request request = requestBuilder.build();
                            return chain.proceed(request);
                        }
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constant.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }

        return retrofit.create(Api.class);
    }

}
