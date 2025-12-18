//for announcement
package com.app.rewardsbattle.ui.activities;

import static com.android.volley.Request.Method.GET;

import android.annotation.SuppressLint;
import android.content.Context;
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

public class AnnouncementActivity extends AppCompatActivity {

    RequestQueue mQueue;
    LoadingDialog loadingDialog;
    LinearLayout ll;
    TextView announcementtitle;
    TextView noannouncement;


    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_announcement);

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
                    mAdView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(LoadAdError adError) {
                    mAdView.setVisibility(View.GONE);
                }

                @Override
                public void onAdOpened() {
                }

                @Override
                public void onAdClicked() {
                }


                @Override
                public void onAdClosed() {
                }
            });
        }

        context = LocaleHelper.setLocale(AnnouncementActivity.this);
        resources = context.getResources();

        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();

        announcementtitle = findViewById(R.id.announcementtitleid);
        ImageView back = findViewById(R.id.backfromannouncement);
        noannouncement = findViewById(R.id.noannouncement);
        back.setOnClickListener(view -> onBackPressed());

        announcementtitle.setText(resources.getString(R.string.Announcement));
        ll = findViewById(R.id.announcementll);
        Announcement();
    }

    public void Announcement() {
        //announcement api call
        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();
        String url = resources.getString(R.string.api) + "announcement";
        final UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());

        final JsonObjectRequest request = new JsonObjectRequest(GET, url, null,
                response -> {
                    try {
                        JSONArray arr = response.getJSONArray("announcement");
                        JSON_PARSE_DATA_AFTER_WEBCALLannounccement(arr);
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
        request.setShouldCache(false);
        mQueue.add(request);
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALLannounccement(JSONArray array) {
        if (TextUtils.equals(String.valueOf(array.length()), "0")) {
            ll.setVisibility(View.GONE);
            noannouncement.setVisibility(View.VISIBLE);
        } else {
            for (int i = 0; i < array.length(); i++) {
                JSONObject json;
                try {
                    json = array.getJSONObject(i);

                    @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.announcement_data, null);
                    TextView announce = view.findViewById(R.id.announcementtext);
                    announce.setText(json.getString("announcement_desc"));

                    ll.addView(view);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}