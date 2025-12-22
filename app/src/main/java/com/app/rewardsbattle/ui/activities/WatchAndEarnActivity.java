//for watch and earn system
package com.app.rewardsbattle.ui.activities;

import static com.android.volley.Request.Method.GET;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.net.ParseException;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.utils.AnalyticsUtil;
import com.app.rewardsbattle.utils.LoadingDialog;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.app.rewardsbattle.utils.UserLocalStore;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import android.widget.RelativeLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WatchAndEarnActivity extends AppCompatActivity {

    TextView watchAndEarnTitle;
    TextView completeTaskTitle;
    // private RewardedInterstitialAd rewardedInterstitialAd;
    Button watchNow;
    RequestQueue mQueue;
    LoadingDialog loadingDialog;
    UserLocalStore userLocalStore;
    CurrentUser user;
    int total = 0;
    int current = 0;
    TextView watchDesc;
    TextView watchNote;
    TextView watchCount;
    Context context;
    Resources resources;
    boolean isLoadingAds;
    String TAG = "AD Status";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.watchmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.myrewarded) {
            Intent intent = new Intent(getApplicationContext(), MyRewardedActivity.class);
            intent.putExtra("FROM", "WATCHNEARN");
            startActivity(intent);
            return true;
        } else if (itemId == R.id.leaderboardwatch) {
            Intent intent = new Intent(getApplicationContext(), LeaderboardActivity.class);
            intent.putExtra("FROM", "WATCHNEARN");
            startActivity(intent);
            return true;
        } else if (itemId == R.id.tandcwatch) {
            Intent intent = new Intent(getApplicationContext(), TermsAndConditionActivity.class);
            intent.putExtra("FROM", "WATCHNEARN");
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_watch_and_earn);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        //check baner ads enable or not
        SharedPreferences sp = getSharedPreferences("SMINFO", MODE_PRIVATE);
        if (TextUtils.equals(sp.getString("baner", "no"), "yes")) {

            RelativeLayout bannerLayout = findViewById(R.id.banner_container);
            if (bannerLayout != null) {
                BannerView bannerView = new BannerView(this, getString(R.string.unity_banner_id), new UnityBannerSize(320, 50));
                bannerLayout.addView(bannerView);
                bannerView.load();
            }
        }

        context = LocaleHelper.setLocale(WatchAndEarnActivity.this);
        resources = context.getResources();

        watchAndEarnTitle = findViewById(R.id.watchandearntitleid);
        completeTaskTitle = findViewById(R.id.completetasktitleid);

        watchAndEarnTitle.setText(resources.getString(R.string.watch_more_to_earn_more));
        completeTaskTitle.setText(resources.getString(R.string.complete_tast));

        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        userLocalStore = new UserLocalStore(this);
        user = userLocalStore.getLoggedInUser();

        watchDesc = findViewById(R.id.watchdesc);
        watchNote = findViewById(R.id.watchnote);
        watchCount = findViewById(R.id.watchcount);
        watchNow = findViewById(R.id.watchnow);
        watchNow.setText(resources.getString(R.string.please_wait));
        watchNow.setEnabled(false);

        /*watch api call start*/
        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();
        String surl = resources.getString(R.string.api) + "watch_earn/" + user.getMemberid();
        UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());
        final JsonObjectRequest srequest = new JsonObjectRequest(GET, surl, null,
                response -> {
                    loadingDialog.dismiss();
                    try {
                        Log.d("watch 1", response.toString());
                        JSONObject obj = response.getJSONObject("watch_earn");

                        current = Integer.parseInt(obj.getString("total_watch_ads"));
                        total = Integer.parseInt(obj.getString("watch_ads_per_day"));
                        watchDesc.setText(obj.getString("watch_earn_description"));
                        watchNote.setText(obj.getString("watch_earn_note"));
                        watchCount.setText(current + "/" + total);
                        if (current < total) {
                            watchNow.setText(resources.getString(R.string.watch_now));
                            watchNow.setEnabled(true);
                        } else {
                            //finish all video
                            watchNow.setEnabled(false);
                            watchNow.setText(resources.getString(R.string.task_completed));
                            countdown();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    loadingDialog.dismiss();
                }, error -> Log.e("**VolleyError", "error" + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return super.getParams();
            }

            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> headers = new HashMap<>();
                CurrentUser user = userLocalStore.getLoggedInUser();
                String token = "Bearer " + user.getToken();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                headers.put("x-localization", LocaleHelper.getPersist(context));
                return headers;
            }
        };
        srequest.setShouldCache(false);
        mQueue.add(srequest);
        watchNow.setOnClickListener(view -> {
            loadRewardedInterstitialAd();
        });
    }

    private void loadRewardedInterstitialAd() {
        watchNow.setText(resources.getString(R.string.please_wait));
        watchNow.setEnabled(false);
        isLoadingAds = true;
        UnityAds.load(getString(R.string.unity_rewarded_id), new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                Log.d(TAG, "onAdLoaded");
                showRewardedVideo();
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                Log.d(TAG, "onAdFailedToLoad: " + message);
                watchNow = findViewById(R.id.watchnow);
                watchNow.setText(resources.getString(R.string.unable_to_load_video));
                watchNow.setEnabled(false);
            }
        });
    }

    public void countdown() {

        //count down start
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd/mm/yyyy hh:mm:ss a");
        String dateToStr = format.format(today);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/mm/yyyy hh:mm:ss a");

        try {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, 1);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            Date date2 = simpleDateFormat.parse(day + "/" + month + "/" + year + " 12:00:00 am");
            Date date1 = simpleDateFormat.parse(dateToStr);

            final long diff = DateDifference(date1, date2);
            new CountDownTimer(diff - 1000, 1000) {
                public void onTick(long millisUntilFinished) {

                    long secondsInMilli = 1000;
                    long minutesInMilli = secondsInMilli * 60;
                    long hoursInMilli = minutesInMilli * 60;
                    long daysInMilli = hoursInMilli * 24;

                    long elapsedDays = millisUntilFinished / daysInMilli;
                    millisUntilFinished = millisUntilFinished % daysInMilli;

                    long elapsedHours = millisUntilFinished / hoursInMilli;
                    millisUntilFinished = millisUntilFinished % hoursInMilli;

                    long elapsedMinutes = millisUntilFinished / minutesInMilli;
                    millisUntilFinished = millisUntilFinished % minutesInMilli;

                    long elapsedSeconds = millisUntilFinished / secondsInMilli;

                    if (elapsedDays == 0 && elapsedHours == 0 && elapsedMinutes == 0 && elapsedSeconds == 0) {
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                    }
                    watchCount.setText(elapsedHours + ":" + elapsedMinutes + ":" + elapsedSeconds);
                }

                public void onFinish() {
                }
            }.start();

        } catch (ParseException | java.text.ParseException e) {
            e.printStackTrace();
        }
        //count down end

    }

    public long DateDifference(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        return different;
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
        intent.putExtra("N", "0");
        startActivity(intent);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
        intent.putExtra("N", "0");
        startActivity(intent);
    }

    private void showRewardedVideo() {
        UnityAds.show(this, getString(R.string.unity_rewarded_id), new IUnityAdsShowListener() {
            @Override
            public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                 watchNow = findViewById(R.id.watchnow);
                 watchNow.setText(resources.getString(R.string.unable_to_load_video));
                 watchNow.setEnabled(false);
            }

            @Override
            public void onUnityAdsShowStart(String placementId) {
                 watchNow.setEnabled(false);
            }

            @Override
            public void onUnityAdsShowClick(String placementId) {
            }

            @Override
            public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                if (state.equals(UnityAds.UnityAdsShowCompletionState.COMPLETED)) {
                    watchNow.setText(resources.getString(R.string.watch_now));
                    watchNow.setEnabled(true);
                    // Handle the reward.
                    Log.d(TAG, "The user earned the reward.");
                    current++;
                    watchCount.setText(current + "/" + total);
                    if (current < total) {
                        //remain to watch video
                        watchNow.setEnabled(true);
                        watchNow.setText(resources.getString(R.string.watch_now));
                    } else {
                        //finish all video
                        watchNow.setEnabled(false);
                        watchNow.setText(resources.getString(R.string.task_completed));
                        countdown();
                    }

                    /*after watch api call start*/
                    loadingDialog.show();
                    mQueue = Volley.newRequestQueue(getApplicationContext());
                    mQueue.getCache().clear();
                    String surl = resources.getString(R.string.api) + "watch_earn2/" + user.getMemberid();
                    UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());
                    final JsonObjectRequest srequest = new JsonObjectRequest(GET, surl, null,
                            response -> loadingDialog.dismiss(), error -> Log.e("**VolleyError", "error" + error.getMessage())) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            return super.getParams();
                        }

                        @Override
                        public Map<String, String> getHeaders() {

                            Map<String, String> headers = new HashMap<>();
                            CurrentUser user = userLocalStore.getLoggedInUser();
                            String token = "Bearer " + user.getToken();
                            headers.put("Content-Type", "application/json");
                            headers.put("Authorization", token);
                            headers.put("x-localization", LocaleHelper.getPersist(context));
                            return headers;
                        }
                    };
                    srequest.setShouldCache(false);
                    mQueue.add(srequest);
                } else {
                     if (current < total) {
                        watchNow.setEnabled(true);
                        watchNow.setText(resources.getString(R.string.watch_now));
                    } else {
                        watchNow.setEnabled(false);
                         watchNow.setText(resources.getString(R.string.task_completed));
                        countdown();
                    }
                }
            }
        });
    }
}