package me.pheynix.pictures.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Environment;
import android.util.TypedValue;
import android.view.WindowManager;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * utilities
 * Created by pheynix on 4/2/16.
 */
public class AndroidUtil {

    private AndroidUtil() {
        throw new UnsupportedOperationException();
    }

    public static int getScreenWidthInPixel() {
        Point point = new Point();
        getScreenSizeInPixel(point);
        return point.x;
    }

    public static int getScreenHeightInPixel() {
        Point point = new Point();
        getScreenSizeInPixel(point);
        return point.y;
    }

    public static void getScreenSizeInPixel(Point point) {
        WindowManager windowManager = (WindowManager) ApplicationLoader.applicationContext.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getSize(point);
    }

    public static int dpToPx(int dp) {
        Resources resources = ApplicationLoader.applicationContext.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    public static File getCacheDir(String type) {
        File cacheDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            cacheDir = ApplicationLoader.applicationContext.getExternalCacheDir();
        } else {
            cacheDir = ApplicationLoader.applicationContext.getCacheDir();
        }

        cacheDir = new File(cacheDir.getPath() + File.separator + type);
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }

        return cacheDir;
    }


    public static int getAppVersion() {
        int versionCode = 1;
        try {
            PackageInfo packageInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }


    public static String getKey(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(url.getBytes());
            byte[] bytes = md.digest();
            StringBuilder stringBuilder = new StringBuilder();

            for (byte aByte : bytes) {
                stringBuilder.append(String.format("%02x", aByte));
            }
            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return String.valueOf(url.hashCode());
    }
}
