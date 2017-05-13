/*
 * Copyright 2017, Wolfhard Fehre
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nicefontaine.matcha.injection.modules;


import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nicefontaine.matcha.data.sources.LocationDataSource;
import com.nicefontaine.matcha.data.sources.LocationRemoteDataSource;
import com.nicefontaine.matcha.data.sources.TicketDataSource;
import com.nicefontaine.matcha.data.sources.TicketRemoteDataSource;
import com.nicefontaine.matcha.network.MatchaService;
import com.nicefontaine.matcha.network.NominatimService;

import java.io.File;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


@Module
public class NetModule {

    private static final String MATCHA_ENDPOINT = "http://10.230.251.250:5000";
    private static final String NOMINATIM_ENDPOINT = "http://nominatim.openstreetmap.org/";
    private static final int CACHE_SIZE = 31457280;

    @Provides
    @Singleton
    Cache provideOkHttpCache(Application application) {
        return new Cache(new File(application.getCacheDir(), "retrofit-cache"), CACHE_SIZE);
    }

    @Provides
    @Singleton
    Gson provideGson() {
        return new GsonBuilder().create();
    }

    @Provides
    @Singleton
    HttpLoggingInterceptor provideLoggingInterceptor() {
        return new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Cache cache, HttpLoggingInterceptor log) {
        return new OkHttpClient.Builder()
                .addInterceptor(log)
                .cache(cache)
                .build();
    }

    @Provides
    @Singleton
    @Named("Matcha")
    public Retrofit provideMatchaRetrofit(Gson gson, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(MATCHA_ENDPOINT)
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    @Named("Nominatim")
    Retrofit provideSensorRetrofit(Gson gson, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(NOMINATIM_ENDPOINT)
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    MatchaService provideMatchaService(@Named("Matcha")Retrofit retrofit) {
        return retrofit.create(MatchaService.class);
    }

    @Provides
    @Singleton
    NominatimService provideNominatimService(@Named("Nominatim")Retrofit retrofit) {
        return retrofit.create(NominatimService.class);
    }

    @Provides
    @Singleton
    TicketDataSource provideTicketDataSource(MatchaService matchaService) {
        return TicketRemoteDataSource.getInstance(matchaService);
    }

    @Provides
    @Singleton
    LocationDataSource provideLocationDataSource(NominatimService nominatimService) {
        return LocationRemoteDataSource.getInstance(nominatimService);
    }
}
