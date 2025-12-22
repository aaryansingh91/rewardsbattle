package com.app.rewardsbattle.ui.activities;

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

import androidx.annotation.NonNull;
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
import com.app.rewardsbattle.ui.fragments.OnGoingFragment;
import com.app.rewardsbattle.ui.fragments.ResultFragment;
import com.app.rewardsbattle.ui.fragments.UpcomingFragment;
import com.app.rewardsbattle.utils.AnalyticsUtil;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.app.rewardsbattle.utils.UserLocalStore;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import android.widget.RelativeLayout;

public class SelectedGameActivity extends AppCompatActivity {

    int n = 1;
    RequestQueue dQueue;
    ImageView back;
    CardView balance;
    TextView balInPlay;
    UserLocalStore userLocalStore;
    TextView gameTitle;
    CurrentUser user;
    private TabLayout tabLayout;
    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_selected_game);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        //check baner ads enable or not
        SharedPreferences spb = getSharedPreferences("SMINFO", MODE_PRIVATE);
        if (TextUtils.equals(spb.getString("baner", "no"), "yes")) {

            RelativeLayout bannerLayout = findViewById(R.id.banner_container);
            if (bannerLayout != null) {
                BannerView bannerView = new BannerView(this, getString(R.string.unity_banner_id), new UnityBannerSize(320, 50));
                bannerLayout.addView(bannerView);
                bannerView.load();
            }
        }

        context = LocaleHelper.setLocale(SelectedGameActivity.this);
        resources = context.getResources();

        ViewPager viewPager = findViewById(R.id.viewPagernew);
        tabLayout = findViewById(R.id.tabLayoutnew);
        TabAdapter adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new OnGoingFragment(), resources.getString(R.string.ongoing));
        adapter.addFragment(new UpcomingFragment(), resources.getString(R.string.upcoming));
        adapter.addFragment(new ResultFragment(), resources.getString(R.string.results));
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabTextColors(Color.WHITE, getResources().getColor(R.color.red));

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
        back = findViewById(R.id.backfromselectedgame);
        back.setOnClickListener(view -> {
            finish();
        });

        SharedPreferences sp = getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
        String gamename = sp.getString("gametitle", "");


        gameTitle = findViewById(R.id.gametitle);
        gameTitle.setText(gamename);

        userLocalStore = new UserLocalStore(getApplicationContext());
        user = userLocalStore.getLoggedInUser();

        balInPlay = findViewById(R.id.balinplay);
        balance = findViewById(R.id.balanceinplay);
        balance.setOnClickListener(view -> {

            if (tabLayout.getSelectedTabPosition() == 0) {
                Intent intent = new Intent(getApplicationContext(), MyWalletActivity.class);
                intent.putExtra("FROM", "ONGOING");
                startActivity(intent);
            } else if (tabLayout.getSelectedTabPosition() == 1) {
                Intent intent = new Intent(getApplicationContext(), MyWalletActivity.class);
                intent.putExtra("FROM", "UPCOMING");
                startActivity(intent);
            } else if (tabLayout.getSelectedTabPosition() == 2) {
                Intent intent = new Intent(getApplicationContext(), MyWalletActivity.class);
                intent.putExtra("FROM", "RESULT");
                startActivity(intent);
            }
        });

        //dashboard api call
        dQueue = Volley.newRequestQueue(getApplicationContext());
        dQueue.getCache().clear();

        String url = resources.getString(R.string.api) + "dashboard/" + user.getMemberid();
        final JsonObjectRequest drequest = new JsonObjectRequest(url, null,
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

                        String win;
                        if (isDouble(winMoney)) {
                            win = String.format("%.2f", Double.parseDouble(winMoney));
                        } else {
                            win = String.valueOf(Integer.parseInt(winMoney));
                        }

                        String joinm;
                        if (isDouble(joinMoney)) {
                            joinm = String.format("%.2f", Double.parseDouble(joinMoney));
                        } else {
                            joinm = String.valueOf(Integer.parseInt(joinMoney));
                        }

                        String totalMoney;
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
                String token = "Bearer " + user.getToken();
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
        finish();
    }
}
