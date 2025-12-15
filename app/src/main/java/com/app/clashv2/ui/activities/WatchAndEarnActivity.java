//for watch and earn system
package com.app.clashv2.ui.activities;

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
import com.app.clashv2.R;
import com.app.clashv2.models.CurrentUser;
import com.app.clashv2.utils.AnalyticsUtil;
import com.app.clashv2.utils.LoadingDialog;
import com.app.clashv2.utils.LocaleHelper;
import com.app.clashv2.utils.UserLocalStore;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
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
    private RewardedInterstitialAd rewardedInterstitialAd;
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

            AdView mAdView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);

            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    mAdView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(LoadAdError adError) {
                    // Code to be executed when an ad request fails.
                    mAdView.setVisibility(View.GONE);
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }

                @Override
                public void onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                }


                @Override
                public void onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                }
            });
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
            rewardedInterstitialAd = null;
            loadRewardedInterstitialAd();
        });
    }

    private void loadRewardedInterstitialAd() {
        watchNow.setText(resources.getString(R.string.please_wait));
        watchNow.setEnabled(false);

        if (rewardedInterstitialAd == null) {
            isLoadingAds = true;
            AdRequest adRequest = new AdRequest.Builder().build();
            // Use the test ad unit ID to load an ad.
            RewardedInterstitialAd.load(
                    WatchAndEarnActivity.this,
                    resources.getString(R.string.admob_reward),
                    adRequest,
                    new RewardedInterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull RewardedInterstitialAd ad) {
                            Log.d(TAG, "onAdLoaded");
                            rewardedInterstitialAd = ad;
                            showRewardedVideo(); // Move this line inside the onAdLoaded callback
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.d(TAG, "onAdFailedToLoad: " + loadAdError.getMessage());

                            // Handle the error.
                            watchNow = findViewById(R.id.watchnow);
                            watchNow.setText(resources.getString(R.string.unable_to_load_video));
                            watchNow.setEnabled(false);
                        }
                    });
        }
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
        rewardedInterstitialAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        watchNow.setEnabled(false);
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        watchNow = findViewById(R.id.watchnow);
                        watchNow.setText(resources.getString(R.string.unable_to_load_video));
                        watchNow.setEnabled(false);
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
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
                    }
                });

        Activity activityContext = WatchAndEarnActivity.this;
        rewardedInterstitialAd.show(
                activityContext,
                rewardItem -> {
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
                });
    }
}