package com.app.clashv2.utils;

import android.app.Activity;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class Analytics {
    private static FirebaseAnalytics mFirebaseAnalytics;

    public static void initialize(FirebaseAnalytics firebaseAnalytics) {
        mFirebaseAnalytics = firebaseAnalytics;
    }

    public static void recordScreenView(Activity activity) {
        if (mFirebaseAnalytics != null && activity != null) {
            String screenName = activity.getClass().getSimpleName();
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName);
            bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
        }
    }
}

