package com.lasley.kts_provider;

import android.app.Application;

public class ProviderApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        Database.init(this);
    }
}
