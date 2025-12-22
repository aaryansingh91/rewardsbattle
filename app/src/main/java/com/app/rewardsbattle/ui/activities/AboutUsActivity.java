//For aboutus
package com.app.rewardsbattle.ui.activities;

import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.utils.AnalyticsUtil;
import com.app.rewardsbattle.utils.LoadingDialog;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.app.rewardsbattle.utils.UserLocalStore;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import android.widget.RelativeLayout;

public class AboutUsActivity extends AppCompatActivity {
    RequestQueue mQueue;
    LoadingDialog loadingDialog;
    TextView aboutUs;
    TextView noAbout;
    ScrollView noAboutScroll;
    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_aboutus);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        //check baner ads enable or not
        SharedPreferences sp = getSharedPreferences("SMINFO", MODE_PRIVATE);
        if (TextUtils.equals(sp.getString("baner", "no"), "yes")) {

            RelativeLayout bannerLayout = findViewById(R.id.banner_container);
            if (bannerLayout != null) {
                BannerView bannerView = new BannerView(this, getString(R.string.unity_banner_id), new UnityBannerSize(320, 50));
                bannerLayout.addView(bannerView);
                bannerView.load();
            }
        }

        context = LocaleHelper.setLocale(AboutUsActivity.this);
        resources = context.getResources();

        noAbout = findViewById(R.id.noabout);
        noAboutScroll = findViewById(R.id.noaboutscroll);

        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();

        aboutUs = findViewById(R.id.aboutusid);
        final TextView aboutView = findViewById(R.id.aboutview);
        ImageView back = findViewById(R.id.backfromaboutus);
        back.setOnClickListener(view -> onBackPressed());

        aboutUs.setText(resources.getString(R.string.about_us));

        //call aboutus api
        mQueue = Volley.newRequestQueue(this);
        mQueue.getCache().clear();

        String url = resources.getString(R.string.api) + "about_us";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            loadingDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(response.getString("about_us"));
                String about = jsonObject.getString("aboutus");
                if (TextUtils.equals(about, "")) {
                    noAbout.setVisibility(View.VISIBLE);
                    noAboutScroll.setVisibility(View.GONE);
                } else {
                    aboutView.setText(Html.fromHtml(about));
                    aboutView.setClickable(true);
                    aboutView.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.e("error", error.toString())) {
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
}
