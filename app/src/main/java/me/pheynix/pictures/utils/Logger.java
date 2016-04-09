package me.pheynix.pictures.utils;

import android.util.Log;

/**
 * log
 * Created by pheynix on 4/2/16.
 */
public class Logger {
    public static void log(String message) {
        Log.e("picture", System.currentTimeMillis() + " " + message);
    }
}
