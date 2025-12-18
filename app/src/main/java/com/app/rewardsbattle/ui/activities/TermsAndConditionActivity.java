//For terms and conditions
package com.app.rewardsbattle.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TermsAndConditionActivity extends AppCompatActivity {

    TextView termsAndConditionTitle;
    ImageView back;
    RequestQueue mQueue;
    LoadingDialog loadingDialog;
    TextView tcview;
    String from="";
    TextView noTerms;
    ScrollView scrollTermsCondition;

    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_termsand_condition);

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

        context = LocaleHelper.setLocale(TermsAndConditionActivity.this);
        resources = context.getResources();

        termsAndConditionTitle = findViewById(R.id.termandconditiontitleid);

        termsAndConditionTitle.setText(resources.getString(R.string.terms_and_condition));

        Intent intent=getIntent();
        from=intent.getStringExtra("FROM");
        back = findViewById(R.id.backfromtandc);
        noTerms = findViewById(R.id.noterms);
        back.setOnClickListener(view -> {
            if(TextUtils.equals(from,"REFERNEARN")){
                Intent intent1 = new Intent(getApplicationContext(), ReferandEarnActivity.class);
                startActivity(intent1);
            } else  if(TextUtils.equals(from,"WATCHNEARN")){
                Intent intent1 = new Intent(getApplicationContext(), WatchAndEarnActivity.class);
                startActivity(intent1);
            }else {
                Intent intent1 = new Intent(getApplicationContext(), PlayActivity.class);
                intent1.putExtra("N", "2");
                startActivity(intent1);
            }
        });

        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();
        tcview = findViewById(R.id.tcview);
        scrollTermsCondition = findViewById(R.id.scrolltermscondition);
        // for terms and conditions
        mQueue = Volley.newRequestQueue(this);
        mQueue.getCache().clear();

        String url = resources.getString(R.string.api) + "terms_conditions";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response.getString("terms_conditions"));
                    String about = jsonObject.getString("terms_conditions");
                    if (TextUtils.equals(about, "")) {
                        noTerms.setVisibility(View.VISIBLE);
                        scrollTermsCondition.setVisibility(View.GONE);
                    } else {
                        tcview.setText(Html.fromHtml(about));
                        tcview.setClickable(true);
                        tcview.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.e("error", error.toString())) {
            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> headers = new HashMap<>();
                UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(TextUtils.equals(from,"REFERNEARN")){
            Intent intent = new Intent(getApplicationContext(), ReferandEarnActivity.class);
            startActivity(intent);
        } else  if(TextUtils.equals(from,"WATCHNEARN")){
            Intent intent = new Intent(getApplicationContext(), WatchAndEarnActivity.class);
            startActivity(intent);
        }else {
            Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
            intent.putExtra("N", "2");
            startActivity(intent);
        }
    }
}
