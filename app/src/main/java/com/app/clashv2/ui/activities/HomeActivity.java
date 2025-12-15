//For show tab in main screen
package com.app.clashv2.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.app.clashv2.R;
import com.app.clashv2.ui.adapters.SectionsPagerAdapter;
import com.app.clashv2.utils.AnalyticsUtil;
import com.app.clashv2.utils.LocaleHelper;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    Boolean doubleBackToExitPressedOnce = false;
    int n = 1;
    TabLayout tabs;
    SharedPreferences sp;
    View view1;
    View view2;
    View view5;
    View view6;
    View view3;
    ImageView tabImageView1;
    TextView tabtextview1;
    ImageView tabImageView2;
    ImageView tabImageView5;
    ImageView tabImageView6;
    TextView tabtextview2;
    ImageView tabImageView3;
    TextView tabtextview3;
    TextView tabtextview5;
    TextView tabtextview6;
    TextView tabImagetext;

    // Add these variables to prevent rapid tab switching
    private boolean isTabSwitching = false;
    private static final int TAB_SWITCH_DELAY = 300; // milliseconds

    private final int[] tabIcons = {
            R.drawable.ic_discount_symbol,
            R.drawable.ic_podium,
            R.drawable.ic_home,
            R.drawable.ic_wallet,
            R.drawable.ic_setting
    };
    final int PERMISSION_REQUEST_CODE = 112;
    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_home);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        context = LocaleHelper.setLocale(HomeActivity.this);
        resources = context.getResources();

        if (Build.VERSION.SDK_INT > 32) {
            if (!shouldShowRequestPermissionRationale("112")) {
                SharedPreferences sp12 = getSharedPreferences("Notification", Context.MODE_PRIVATE);
                String status = sp12.getString("switch", "");
                if (!status.equals("off")) {
                    getNotificationPermission();
                }
            }
        }

        SharedPreferences sp = getSharedPreferences("SMINFO", MODE_PRIVATE);
        if (TextUtils.equals(sp.getString("baner", "no"), "yes")) {

            MobileAds.initialize(this, initializationStatus -> {

            });

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

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);

        SharedPreferences preferences1 = getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor1 = preferences1.edit();
        editor1.clear();
        editor1.apply();

        viewPager.setAdapter(sectionsPagerAdapter);
        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        tabs.setSelectedTabIndicatorHeight(0);

        setupTabIcons();

        try {
            Intent intent = getIntent();
            String N = intent.getStringExtra("N");
            if (N != null) {
                n = Integer.parseInt(N);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        // Increase offscreen page limit to keep more fragments in memory
        viewPager.setOffscreenPageLimit(5);
        viewPager.setCurrentItem(n);

        if (Objects.requireNonNull(tabs.getTabAt(0)).isSelected()) {
            TextView tv = Objects.requireNonNull(Objects.requireNonNull(tabs.getTabAt(0)).getCustomView()).findViewById(R.id.tabtextview);
            tv.setTextColor(getResources().getColor(R.color.newblack));
            TextView tviv = Objects.requireNonNull(Objects.requireNonNull(tabs.getTabAt(0)).getCustomView()).findViewById(R.id.tabimageviewtext);
            tviv.setTextColor(getResources().getColor(R.color.newblack));
            ImageView iv = Objects.requireNonNull(Objects.requireNonNull(tabs.getTabAt(0)).getCustomView()).findViewById(R.id.tabimageview);
            //iv.getDrawable().setColorFilter(getResources().getColor(R.color.newblack), PorterDuff.Mode.SRC_IN);
        }
    }

    @SuppressLint({"InflateParams", "UseCompatLoadingForDrawables"})
    private void setupTabIcons() {

        view1 = getLayoutInflater().inflate(R.layout.custom_tab_layout, null);
        tabImageView1 = view1.findViewById(R.id.tabimageview);
        tabtextview1 = view1.findViewById(R.id.tabtextview);
        tabImagetext = view1.findViewById(R.id.tabimageviewtext);
        tabtextview1.setText(resources.getString(R.string.earn));
        tabImageView1.setImageDrawable(getDrawable(tabIcons[0]));
        tabImageView1.setPadding(5, 5, 5, 5);

        view5 = getLayoutInflater().inflate(R.layout.custom_tab_layout, null);
        tabImageView5 = view5.findViewById(R.id.tabimageview);
        tabtextview5 = view5.findViewById(R.id.tabtextview);
        tabtextview5.setText(resources.getString(R.string.rank));
        tabImageView5.setImageDrawable(getDrawable(tabIcons[1]));

        view2 = getLayoutInflater().inflate(R.layout.custom_tab_layout, null);
        tabImageView2 = view2.findViewById(R.id.tabimageview);
        tabtextview2 = view2.findViewById(R.id.tabtextview);
        tabtextview2.setText(resources.getString(R.string.home));
        tabImageView2.setImageDrawable(getDrawable(tabIcons[2]));

        view6 = getLayoutInflater().inflate(R.layout.custom_tab_layout, null);
        tabImageView6 = view6.findViewById(R.id.tabimageview);
        tabtextview6 = view6.findViewById(R.id.tabtextview);
        tabtextview6.setText(resources.getString(R.string.wallet));
        tabImageView6.setImageDrawable(getDrawable(tabIcons[3]));

        view3 = getLayoutInflater().inflate(R.layout.custom_tab_layout, null);
        tabImageView3 = view3.findViewById(R.id.tabimageview);
        tabtextview3 = view3.findViewById(R.id.tabtextview);
        tabtextview3.setText(resources.getString(R.string.account));
        tabImageView3.setImageDrawable(getDrawable(tabIcons[4]));

        Objects.requireNonNull(tabs.getTabAt(0)).setCustomView(view1);
        Objects.requireNonNull(tabs.getTabAt(1)).setCustomView(view5);
        Objects.requireNonNull(tabs.getTabAt(2)).setCustomView(view2);
        Objects.requireNonNull(tabs.getTabAt(3)).setCustomView(view6);
        Objects.requireNonNull(tabs.getTabAt(4)).setCustomView(view3);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Prevent rapid tab switching
                if (isTabSwitching) {
                    return;
                }

                isTabSwitching = true;

                // Reset the flag after delay
                new Handler().postDelayed(() -> isTabSwitching = false, TAB_SWITCH_DELAY);

                TextView tv = Objects.requireNonNull(tab.getCustomView()).findViewById(R.id.tabtextview);
                tv.setTextColor(getResources().getColor(R.color.newblack));
                ImageView iv = tab.getCustomView().findViewById(R.id.tabimageview);
                if (tab.getPosition() != 0) {
                    iv.getDrawable().setColorFilter(getResources().getColor(R.color.newblack), PorterDuff.Mode.SRC_IN);
                }
                if (tab.getPosition() == 0) {
                    TextView ivtv = tab.getCustomView().findViewById(R.id.tabimageviewtext);
                    ivtv.setTextColor(getResources().getColor(R.color.newblack));
                }

                sp = getSharedPreferences("tabinfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("selectedtab", String.valueOf(tab.getPosition()));
                editor.apply();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView tv = Objects.requireNonNull(tab.getCustomView()).findViewById(R.id.tabtextview);
                tv.setTextColor(Color.WHITE);
                ImageView iv = tab.getCustomView().findViewById(R.id.tabimageview);
                if (tab.getPosition() != 0) {
                    iv.getDrawable().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
                }

                if (tab.getPosition() == 0) {
                    TextView ivtv = tab.getCustomView().findViewById(R.id.tabimageviewtext);
                    ivtv.setTextColor(Color.WHITE);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                TextView tv = Objects.requireNonNull(tab.getCustomView()).findViewById(R.id.tabtextview);
                tv.setTextColor(getResources().getColor(R.color.newblack));
                ImageView iv = tab.getCustomView().findViewById(R.id.tabimageview);
                if (tab.getPosition() != 0) {
                    iv.getDrawable().setColorFilter(getResources().getColor(R.color.newblack), PorterDuff.Mode.SRC_IN);
                }

                if (tab.getPosition() == 0) {
                    TextView ivtv = tab.getCustomView().findViewById(R.id.tabimageviewtext);
                    ivtv.setTextColor(getResources().getColor(R.color.newblack));
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void getNotificationPermission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            PERMISSION_REQUEST_CODE);
                }
            }
        } catch (Exception ignored) {
            // Handle any exceptions that may occur during the permission check or request
        }
    }


    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAffinity();
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, resources.getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 1500);
    }
}