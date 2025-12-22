package com.app.rewardsbattle.ui.activities;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.rewardsbattle.R;
import com.app.rewardsbattle.ui.fragments.EarnFragment;
import com.app.rewardsbattle.ui.fragments.MeFragment;
import com.app.rewardsbattle.ui.fragments.PlayFragment;
import com.app.rewardsbattle.ui.fragments.RankFragment;
import com.app.rewardsbattle.ui.fragments.WalletFragment;
import com.app.rewardsbattle.utils.AnalyticsUtil;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import android.widget.RelativeLayout;
import android.widget.ImageView;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.utils.UserLocalStore;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class PlayActivity extends AppCompatActivity {

    Boolean doubleBackToExitPressedOnce = false;
    final int PERMISSION_REQUEST_CODE = 112;
    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_play);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        context = LocaleHelper.setLocale(PlayActivity.this);
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

        // Subscribe to topic "all"
        FirebaseMessaging.getInstance().subscribeToTopic("all")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed to topic: all";
                        if (!task.isSuccessful()) {
                            msg = "Subscription failed";
                            //   Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                        //  Log.d(TAG, msg);
                        //    Toast.makeText(CreateNewAccount.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

        SharedPreferences sp = getSharedPreferences("SMINFO", MODE_PRIVATE);
        if (TextUtils.equals(sp.getString("baner", "no"), "yes")) {

                        RelativeLayout bannerLayout = findViewById(R.id.banner_container);
            if (bannerLayout != null) {
                BannerView bannerView = new BannerView(this, getString(R.string.unity_banner_id), new UnityBannerSize(320, 50));
                bannerLayout.addView(bannerView);
                bannerView.load();
            }
        }


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (bottomNavigationView == null) {
            throw new IllegalStateException("BottomNavigationView is not found in the layout");
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new PlayFragment())
                .commit();

        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.nav_earn:
                    selectedFragment = new EarnFragment();
                    break;
                case R.id.nav_rank:
                    selectedFragment = new RankFragment();
                    break;
                case R.id.nav_home:
                    selectedFragment = new PlayFragment();
                    break;
                case R.id.nav_wallet:
                    selectedFragment = new WalletFragment();
                    break;
                case R.id.nav_profile:
                    selectedFragment = new MeFragment();
                    break;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        // Initialize WhatsApp button
        ImageView whatsappButton = findViewById(R.id.whatsapp_button);
        fetchWhatsAppNumber(whatsappButton);
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

    private void fetchWhatsAppNumber(ImageView whatsappButton) {
        RequestQueue mQueue = Volley.newRequestQueue(this);
        mQueue.getCache().clear();

        String url = resources.getString(R.string.api) + "customer_support";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response.getString("customer_support"));
                String customerPhone = jsonObject.getString("comapny_phone");
                String customerCode = jsonObject.getString("comapny_country_code");
                
                String whatsappNumber = customerCode + customerPhone;
                
                whatsappButton.setOnClickListener(view -> openWhatsApp(whatsappNumber));
                whatsappButton.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
                whatsappButton.setVisibility(View.GONE);
            }
        }, error -> {
            Log.e("WhatsApp", "Failed to fetch WhatsApp number: " + error.toString());
            whatsappButton.setVisibility(View.GONE);
        }) {
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

    private void openWhatsApp(String number) {
        try {
            number = number.replace(" ", "").replace("+", "");

            Intent sendIntent = new Intent("android.intent.action.SEND");
            sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
            sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators(number) + "@s.whatsapp.net");
            startActivity(sendIntent);

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), resources.getString(R.string.whatsapp_not_found), Toast.LENGTH_SHORT).show();
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
