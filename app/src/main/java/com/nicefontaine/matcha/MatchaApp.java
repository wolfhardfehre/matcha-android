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

package com.nicefontaine.matcha;


import android.app.Application;

import com.nicefontaine.matcha.injection.components.AppComponent;
import com.nicefontaine.matcha.injection.components.DaggerAppComponent;
import com.nicefontaine.matcha.injection.modules.AppModule;
import com.nicefontaine.matcha.injection.modules.NetModule;

import timber.log.Timber;


public class MatchaApp extends Application {

    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        initLogging();
        initComponents();
    }

    private void initLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    public void initComponents() {
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .netModule(new NetModule())
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}