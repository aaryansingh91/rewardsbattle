//For game wise top player list
package com.app.clashv2.ui.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.clashv2.R;
import com.app.clashv2.models.CurrentUser;
import com.app.clashv2.models.TopplayerData;
import com.app.clashv2.ui.adapters.TopplayerAdapter;
import com.app.clashv2.utils.AnalyticsUtil;
import com.app.clashv2.utils.LoadingDialog;
import com.app.clashv2.utils.LocaleHelper;
import com.app.clashv2.utils.UserLocalStore;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopPlayerActivity extends AppCompatActivity {

    TextView topplayerstitle;
    ImageView back;
    RecyclerView recyclerView;
    TopplayerAdapter myAdapter;
    List<TopplayerData> mData;
    RequestQueue mQueue;
    LoadingDialog loadingDialog;
    LinearLayout logoll;
    Boolean firsttime = true;
    TextView notoplayer;

    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_top_player);

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

        context = LocaleHelper.setLocale(TopPlayerActivity.this);
        resources = context.getResources();

        topplayerstitle = findViewById(R.id.topplayerstitleid);

        topplayerstitle.setText(resources.getString(R.string.top_players));

        notoplayer = findViewById(R.id.notoplayer);
        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();

        back = findViewById(R.id.backfromtopplayer);
        back.setOnClickListener(view -> onBackPressed());

        logoll = findViewById(R.id.logoll);
        recyclerView = findViewById(R.id.topplayerrecyclerview);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(false);
        layoutManager.setReverseLayout(false);
        /*recyclerView.setHasFixedSize(true);*/
        recyclerView.setLayoutManager(layoutManager);

        mData = new ArrayList<>();

        mQueue = Volley.newRequestQueue(TopPlayerActivity.this);
        mQueue.getCache().clear();

        // for top player
        String url = resources.getString(R.string.api) + "top_players";

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            loadingDialog.dismiss();
            Log.d("response", response.toString());
            try {
                JSONObject jsnobject = new JSONObject(response.getString("top_players"));
                JSONArray arrgame = response.getJSONArray("game");
                JSON_PARSE_DATA_AFTER_WEBCALLgame(arrgame, jsnobject);
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

    public void JSON_PARSE_DATA_AFTER_WEBCALLgame(JSONArray array, final JSONObject object) {

        if (TextUtils.equals(String.valueOf(array.length()), "0")) {
            logoll.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            notoplayer.setVisibility(View.VISIBLE);
        } else {
            for (int i = 0; i < array.length(); i++) {
                JSONObject json;
                try {
                    json = array.getJSONObject(i);
                    View view = getLayoutInflater().inflate(R.layout.topplayer_logo_layout, null);
                    final ImageView gamelogo = view.findViewById(R.id.topplayerlogoiv);
                    Log.d("game_logo", json.getString("game_logo"));
                    Picasso.get().load(Uri.parse(json.getString("game_logo"))).placeholder(R.drawable.battlemanialogo).fit().into(gamelogo);

                    gamelogo.setTag(json.getString("game_name"));
                    final JSONObject finalJson = json;

                    if (firsttime) {
                        firsttime = false;
                        JSONArray arr = null;
                        try {
                            arr = object.getJSONArray(finalJson.getString("game_name"));
                            JSON_PARSE_DATA_AFTER_WEBCALL(arr, finalJson.getString("game_name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    gamelogo.setOnClickListener(v -> {
                        mData.clear();
                        JSONArray arr = null;
                        try {
                            arr = object.getJSONArray(finalJson.getString("game_name"));
                            JSON_PARSE_DATA_AFTER_WEBCALL(arr, finalJson.getString("game_name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    });
                    logoll.addView(view);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array, String gamename) {

        for (int i = 0; i < array.length(); i++) {
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);
                if (i != 0) {
                    gamename = "";
                }
                TopplayerData data = new TopplayerData(gamename, json.getString("winning"), json.getString("user_name"), json.getString("member_id"), json.getString("pubg_id"));
                Log.e("winning", json.getString("winning"));
                mData.add(data);
                myAdapter = new TopplayerAdapter(TopPlayerActivity.this, mData);
                myAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(myAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
