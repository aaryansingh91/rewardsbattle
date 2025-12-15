//For refer and earn page
package com.app.clashv2.ui.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReferandEarnActivity extends AppCompatActivity {

    TextView referAndEarnTitle;
    TextView yourReferralCodeTitle;
    Button referNow;
    TextView promo;
    TextView referDetail;
    UserLocalStore userLocalStore;
    CurrentUser user;
    LoadingDialog loadingDialog;
    RequestQueue mQueue;
    String shareBody = "";

    Context context;
    Resources resources;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refermenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        switch (item.getItemId()) {

            case R.id.myref:
                Intent intent=new Intent(getApplicationContext(),MyReferralsActivity.class);
                intent.putExtra("FROM","REFERNEARN");
                startActivity(intent);
                return true;
//            case R.id.leaderboard:
//                intent=new Intent(getApplicationContext(),LeaderboardActivity.class);
//                intent.putExtra("FROM","REFERNEARN");
//                startActivity(intent);
//                return true;
            case R.id.tandc:
                intent=new Intent(getApplicationContext(), TermsAndConditionActivity.class);
                intent.putExtra("FROM","REFERNEARN");
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_referand_earn);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        context = LocaleHelper.setLocale(ReferandEarnActivity.this);
        resources = context.getResources();

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

        loadingDialog = new LoadingDialog(ReferandEarnActivity.this);
        loadingDialog.show();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sp = getSharedPreferences("fragmentinfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("fraginfo", "0");
        editor.apply();

        userLocalStore = new UserLocalStore(getApplicationContext());
        user = userLocalStore.getLoggedInUser();

        yourReferralCodeTitle = findViewById(R.id.yourreferralcodetitleid);
        referAndEarnTitle = findViewById(R.id.referandearntitleid);

        yourReferralCodeTitle.setText(resources.getString(R.string.your_referral_code));
        referAndEarnTitle.setText(resources.getString(R.string.refer_more_to_earn_more));


        referDetail = findViewById(R.id.referdetail);
        referNow = findViewById(R.id.refernow);
        referNow.setText(resources.getString(R.string.refer_now));
        promo = findViewById(R.id.promo);
        promo.setText(user.getUsername());
        promo.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(resources.getString(R.string.promo_code), user.getUsername());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), resources.getString(R.string.promo_code_copied_successfully), Toast.LENGTH_SHORT).show();
        });

        // dashboard api call
        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();

        String url = resources.getString(R.string.api) + "dashboard/" + user.getMemberid();

        final JsonObjectRequest request = new JsonObjectRequest(url, null,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response.getString("web_config"));
                        referDetail.setText(obj.getString("referandearn_description"));
                        shareBody = obj.getString("share_description");
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
                String token="Bearer "+user.getToken();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                headers.put("x-localization", LocaleHelper.getPersist(context));
                return headers;
            }
        };
        request.setShouldCache(false);
        mQueue.add(request);

        referNow.setOnClickListener(v -> {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody + resources.getString(R.string.referral_code)+" : " + user.getUsername());
            startActivity(Intent.createChooser(sharingIntent, resources.getString(R.string.share_using)));
        });
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
        Intent intent=new Intent(getApplicationContext(),PlayActivity.class);
        intent.putExtra("N","0");
        startActivity(intent);
    }
}
