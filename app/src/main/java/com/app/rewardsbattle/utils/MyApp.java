//For onesignal, check internet availability and font family
package com.app.rewardsbattle.utils;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import com.app.rewardsbattle.ui.activities.NoInternetActivity;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;
import com.app.rewardsbattle.R;

import java.util.List;

public class MyApp extends Application {

    private static Context mContext;
    boolean internet = false;
    Context context;
    Resources resources;

    public static Context getContext() {
        return mContext;
    }

    public static boolean isAppRunning(final Context context, final String packageName) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null) {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        //for change font family
        FontsOverride.setDefaultFont(this, "MONOSPACE", "font/Poppins-Regular.ttf");
        context = LocaleHelper.setLocale(mContext);
        resources = context.getResources();

        //check internet availability
        final Handler tipsHanlder = new Handler();
        Runnable tipsRunnable = new Runnable() {
            @Override
            public void run() {
                tipsHanlder.postDelayed(this, 1000);

                if (isAppRunning(getApplicationContext(), getPackageName())) {

                    // App is running
                    if (isAppOnForeground(getApplicationContext(), getPackageName())) {

                        //run in foreground
                        if (!isNetworkAvailable()) {
                            if (!internet) {
                                internet = true;
                                Intent intent = new Intent(getApplicationContext(), NoInternetActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        } else {
                            internet = false;
                            //internet available
                        }
                    }  //run in background

                }  // App is not running

            }
        };
        tipsHanlder.post(tipsRunnable);

        UnityAds.initialize(getApplicationContext(), getString(R.string.unity_game_id), true, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                // initialization complete
            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                // initialization failed
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean isAppOnForeground(Context context, String appPackageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(appPackageName)) {
                return true;
            }
        }
        return false;
    }
}
