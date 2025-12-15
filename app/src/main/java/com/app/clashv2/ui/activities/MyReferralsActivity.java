//For show logins user's referral list
package com.app.clashv2.ui.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
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

public class MyReferralsActivity extends AppCompatActivity {

    TextView myreferalstitle;
    TextView myreferalssumarytitle;
    TextView myreferalliststitle;
    TextView playernametitle;
    TextView datetitle;
    TextView statustitle;
    RequestQueue mQueue;
    UserLocalStore userLocalStore;
    CurrentUser user;
    LinearLayout refLl;
    TextView refTv;
    ImageView back;
    LoadingDialog loadingDialog;
    String from ="";

    Context context;
    Resources resources;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_my_referrals);

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

        context = LocaleHelper.setLocale(MyReferralsActivity.this);
        resources = context.getResources();
        myreferalssumarytitle = findViewById(R.id.myreferralssumarytitleid);
        myreferalstitle = findViewById(R.id.myreferralstitleid);
        myreferalliststitle = findViewById(R.id.myreferralslisttitleid);
        playernametitle = findViewById(R.id.playernametitleid);
        statustitle = findViewById(R.id.statustitleid);
        datetitle = findViewById(R.id.datetitleid);

        myreferalliststitle.setText(resources.getString(R.string.my_referrals_list));
        playernametitle.setText(resources.getString(R.string.player_name));
        statustitle.setText(resources.getString(R.string.status));
        datetitle.setText(resources.getString(R.string.date));
        myreferalstitle.setText(resources.getString(R.string.my_referrals));
        myreferalssumarytitle.setText(resources.getString(R.string.my_referrals_summary));

        loadingDialog = new LoadingDialog(MyReferralsActivity.this);
        loadingDialog.show();

        Intent intent=getIntent();
        from=intent.getStringExtra("FROM");
        back = findViewById(R.id.backfrommyreff);
        back.setOnClickListener(view -> onBackPressed());

        final TextView refnumber = findViewById(R.id.refnumber);
        final TextView earnings = findViewById(R.id.earnings);
        refTv = findViewById(R.id.reftv);
        refLl = findViewById(R.id.refll);

        refTv.setText(resources.getString(R.string.no_referrals_found));

        userLocalStore = new UserLocalStore(getApplicationContext());
        user = userLocalStore.getLoggedInUser();

        //For list of all referrals
        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();

        String url = resources.getString(R.string.api) + "my_refrrrals/" + user.getMemberid();

        final JsonObjectRequest request = new JsonObjectRequest(url, null,
                response -> {
                    try {
                        JSONObject totrefobj = new JSONObject(response.getString("tot_referrals"));
                        refnumber.setText(Html.fromHtml(resources.getString(R.string.referrals)+"<br><b>" + totrefobj.getString("total_ref")));
                        refnumber.setClickable(true);
                        refnumber.setMovementMethod(LinkMovementMethod.getInstance());

                        JSONObject totearnobj = new JSONObject(response.getString("tot_earnings"));

                        SharedPreferences sp1 = getSharedPreferences("currencyinfo", Context.MODE_PRIVATE);
                        SpannableStringBuilder builder = new SpannableStringBuilder();

                        if ((TextUtils.equals(totearnobj.getString("total_earning"), "null"))) {
                            builder = new SpannableStringBuilder();
                            builder.append(Html.fromHtml(resources.getString(R.string.earnings)+"<br><b>"))
                                    .append(" ", new ImageSpan(getApplicationContext(), R.drawable.resize_coin1617,ImageSpan.ALIGN_BASELINE), 0)
                                    .append(" ")
                                    .append(Html.fromHtml(""+"<b>0</b>"));
                            earnings.setText(builder);
                        } else {
                            builder = new SpannableStringBuilder();
                            builder.append(Html.fromHtml(resources.getString(R.string.earnings)+"<br><b>"))
                                    .append(" ", new ImageSpan(getApplicationContext(), R.drawable.resize_coin1617,ImageSpan.ALIGN_BASELINE), 0)
                                    .append(" ")
                                    .append(Html.fromHtml("<b>"+ totearnobj.getString("total_earning")));
                            earnings.setText(builder);
                        }
                        JSONArray arr = response.getJSONArray("my_referrals");
                        if (!TextUtils.equals(response.getString("my_referrals"), "[]")) {
                            refTv.setVisibility(View.GONE);
                        }
                        JSON_PARSE_DATA_AFTER_WEBCALL(arr);
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
    }
    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array) {

        for (int i = 0; i < array.length(); i++) {
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);

                View view = getLayoutInflater().inflate(R.layout.referral_data, null);
                TextView rdate = view.findViewById(R.id.rdate);
                TextView rplayername = view.findViewById(R.id.rplayername);
                TextView rstatus = view.findViewById(R.id.rstatus);

                rdate.setText(json.getString("date"));
                rplayername.setText(json.getString("user_name"));
                rstatus.setText(json.getString("status"));
                if (TextUtils.equals(json.getString("status"), "Rewarded")) {
                    rstatus.setTextColor(getResources().getColor(R.color.newgreen));
                } else {
                    rstatus.setTextColor(getResources().getColor(R.color.newblack));
                }
                refLl.addView(view);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
