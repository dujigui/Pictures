package me.pheynix.pictures.utils;

import android.app.Application;
import android.content.Context;

/**
 * application
 * Created by pheynix on 4/2/16.
 */
public class ApplicationLoader extends Application {
    public static Context applicationContext;

    @Override public void onCreate() {
        super.onCreate();
        applicationContext = this;
    }
}
