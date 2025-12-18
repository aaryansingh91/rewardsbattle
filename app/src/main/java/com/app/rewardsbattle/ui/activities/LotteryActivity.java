//for lottery
package com.app.rewardsbattle.ui.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.ui.adapters.TabAdapter;
import com.app.rewardsbattle.ui.fragments.OngoingLotteryFragment;
import com.app.rewardsbattle.ui.fragments.ResultLotteryFragment;
import com.app.rewardsbattle.utils.AnalyticsUtil;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.app.rewardsbattle.utils.UserLocalStore;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class  LotteryActivity extends AppCompatActivity {

    int n = 0;
    ImageView back;
    CardView balance;
    TextView balInPlay;
    UserLocalStore userLocalStore;
    CurrentUser user;
    RequestQueue dQueue;
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_lottery);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        SharedPreferences sp=getSharedPreferences("SMINFO",MODE_PRIVATE);
        if(TextUtils.equals(sp.getString("baner","no"),"yes")) {

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

        context = LocaleHelper.setLocale(LotteryActivity.this);
        resources = context.getResources();

       // lotterytitle.setText(resources.getString(R.string.lottery));
        viewPager = findViewById(R.id.viewPagerlottry);
        tabLayout = findViewById(R.id.tabLayoutlottery);
        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new OngoingLotteryFragment(), resources.getString(R.string.ONGOING));
        adapter.addFragment(new ResultLotteryFragment(), resources.getString(R.string.RESULTS));
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabTextColors(Color.WHITE, getResources().getColor(R.color.newblack));

        try {
            Intent intent = getIntent();
            String N = intent.getStringExtra("N");
            if (N != null) {
                n = Integer.parseInt(N);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        viewPager.setCurrentItem(n);
        back = findViewById(R.id.backfromlottery);
        back.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
            intent.putExtra("N", "0");
            startActivity(intent);
        });

        userLocalStore = new UserLocalStore(getApplicationContext());
        user = userLocalStore.getLoggedInUser();

        balInPlay = findViewById(R.id.balinlottery);
        balance = findViewById(R.id.balanceinlattery);
        balance.setOnClickListener(view -> {

            if(tabLayout.getSelectedTabPosition()==0){
                Intent intent=new Intent(getApplicationContext(),MyWalletActivity.class);
                intent.putExtra("FROM","ONGOINGLOTTERY");
                startActivity(intent);
            }else if(tabLayout.getSelectedTabPosition()==1){
                Intent intent=new Intent(getApplicationContext(),MyWalletActivity.class);
                intent.putExtra("FROM","RESULTLOTTERY");
                startActivity(intent);
            }
        });


        //dashboard api call
        dQueue = Volley.newRequestQueue(getApplicationContext());
        dQueue.getCache().clear();

        String durl = resources.getString(R.string.api) + "dashboard/" + user.getMemberid();

        @SuppressLint("DefaultLocale") final JsonObjectRequest drequest = new JsonObjectRequest(durl, null,
                response -> {

                    try {
                        JSONObject memobj = new JSONObject(response.getString("member"));

                        String winMoney = memobj.getString("wallet_balance");
                        String joinMoney = memobj.getString("join_money");
                        if (TextUtils.equals(winMoney, "null")) {
                            winMoney = "0";
                        }
                        if (TextUtils.equals(joinMoney, "null")) {
                            joinMoney = "0";
                        }

                        String win = "";
                        if (isDouble(winMoney)) {
                            win = String.format("%.2f", Double.parseDouble(winMoney));
                        } else {
                            win = String.valueOf(Integer.parseInt(winMoney));
                        }

                        String joinm = "";
                        if (isDouble(joinMoney)) {
                            joinm = String.format("%.2f", Double.parseDouble(joinMoney));
                        } else {
                            joinm = String.valueOf(Integer.parseInt(joinMoney));
                        }

                        String totalMoney = "";
                        if(win.startsWith("0")){
                            if (isDouble(joinm)) {
                                totalMoney = String.valueOf(Double.parseDouble(joinm));
                            } else {
                                totalMoney = String.valueOf(Integer.parseInt(joinm));
                            }
                        }else if(joinm.startsWith("-")){
                            if (isDouble(win)) {
                                totalMoney = String.valueOf(Double.parseDouble(win));
                            } else {
                                totalMoney = String.valueOf(Integer.parseInt(win));
                            }
                        }else {
                            if (isDouble(win) && isDouble(joinm)) {
                                totalMoney = String.valueOf(Double.parseDouble(win) + Double.parseDouble(joinm));
                            } else {
                                totalMoney = String.valueOf(Integer.parseInt(win) + Integer.parseInt(joinm));
                            }
                        }
                        if (isDouble(totalMoney)) {
                            totalMoney = String.format("%.2f", Double.parseDouble(totalMoney));
                        }
                        balInPlay.setText(totalMoney);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("**VolleyError", "error" + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return super.getParams();
            }
            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> headers = new HashMap<>();
                CurrentUser user = userLocalStore.getLoggedInUser();
                String token="Bearer "+user.getToken();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                headers.put("x-localization", LocaleHelper.getPersist(context));

                return headers;
            }
        };
        drequest.setShouldCache(false);
        dQueue.add(drequest);
    }

    boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
        intent.putExtra("N", "0");
        startActivity(intent);
    }
}