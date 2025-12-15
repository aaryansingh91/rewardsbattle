//For leaderboard
package com.app.clashv2.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.clashv2.R;
import com.app.clashv2.models.CurrentUser;
import com.app.clashv2.utils.AnalyticsUtil;
import com.app.clashv2.utils.LoadingDialog;
import com.app.clashv2.utils.LocaleHelper;
import com.app.clashv2.utils.UserLocalStore;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {

    TextView leaderboardtitle;
    ImageView back;
    RequestQueue mQueue;
    LoadingDialog loadingDialog;
    LinearLayout leaderboardLl, l1;
    String from = "";
    TextView noleaderboard;

    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_leaderboard);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

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

        context = LocaleHelper.setLocale(LeaderboardActivity.this);
        resources = context.getResources();

        leaderboardtitle = findViewById(R.id.leaderboardtitleid);
        leaderboardtitle.setText(resources.getString(R.string.leaderboard));

        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();
        Intent intent = getIntent();
        from = intent.getStringExtra("FROM");
        back = findViewById(R.id.backfromleaderboard);
        back.setOnClickListener(view -> {
            if (TextUtils.equals(from, "REFERNEARN")) {
                Intent intent1 = new Intent(getApplicationContext(), ReferandEarnActivity.class);
                startActivity(intent1);
            } else if (TextUtils.equals(from, "WATCHNEARN")) {
                Intent intent1 = new Intent(getApplicationContext(), WatchAndEarnActivity.class);
                startActivity(intent1);
            } else {
                Intent intent1 = new Intent(getApplicationContext(), PlayActivity.class);
                intent1.putExtra("N", "2");
                startActivity(intent1);
            }

        });
        leaderboardLl = findViewById(R.id.leaderboardll);
        noleaderboard = findViewById(R.id.noleaderboard);
        l1 = findViewById(R.id.l1);
        // leader_board api call
        mQueue = Volley.newRequestQueue(LeaderboardActivity.this);
        mQueue.getCache().clear();

        String url = resources.getString(R.string.api) + "leader_board";

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            loadingDialog.dismiss();
            JSONObject player = null;
            try {
                JSONArray arr = response.getJSONArray("leader_board");
                JSON_PARSE_DATA_AFTER_WEBCALL(arr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.e("VolleyError", "error" + error.getMessage())) {
            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> headers = new HashMap<>();
                UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());
                CurrentUser user = userLocalStore.getLoggedInUser();
                String token = "Bearer " + user.getToken();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                headers.put("x-localization", LocaleHelper.getPersist(context));

                return headers;
            }
        };
        request.setShouldCache(false);
        mQueue.add(request);
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array) {
        if (TextUtils.equals(String.valueOf(array.length()), "0")) {
            l1.setVisibility(View.GONE);
            noleaderboard.setVisibility(View.VISIBLE);
        } else {
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = null;
                try {
                    json = array.getJSONObject(i);

                    View view = getLayoutInflater().inflate(R.layout.leaderboard_data, null);
                    TextView userNamel = view.findViewById(R.id.usernamel);
                    TextView totalReff = view.findViewById(R.id.totalref);
                    userNamel.setText(json.getString("user_name"));
                    totalReff.setText(json.getString("tot_referral"));

                    leaderboardLl.addView(view);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (TextUtils.equals(from, "REFERNEARN")) {
            Intent intent = new Intent(getApplicationContext(), ReferandEarnActivity.class);
            startActivity(intent);
        } else if (TextUtils.equals(from, "WATCHNEARN")) {
            Intent intent = new Intent(getApplicationContext(), WatchAndEarnActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
            intent.putExtra("N", "2");
            startActivity(intent);
        }

    }
}
