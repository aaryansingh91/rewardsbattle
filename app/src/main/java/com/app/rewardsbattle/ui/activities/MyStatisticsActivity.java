//For login user's statics
package com.app.rewardsbattle.ui.activities;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.models.MyStatisticsData;
import com.app.rewardsbattle.ui.adapters.MyStatisticsAdapter;
import com.app.rewardsbattle.utils.AnalyticsUtil;
import com.app.rewardsbattle.utils.LoadingDialog;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.app.rewardsbattle.utils.UserLocalStore;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import android.widget.RelativeLayout;

public class MyStatisticsActivity extends AppCompatActivity {

    ImageView back;
    MyStatisticsAdapter myAdapter;
    List<MyStatisticsData> mData;
    RequestQueue mQueue;
    RecyclerView recyclerView;
    LoadingDialog loadingDialog;
    TextView nostatics;
    LinearLayout staticsdata;
    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_my_statistics);

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


        context = LocaleHelper.setLocale(MyStatisticsActivity.this);
        resources = context.getResources();

        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();

        back = findViewById(R.id.backfromstatistics);
        back.setOnClickListener(view -> onBackPressed());

        recyclerView = findViewById(R.id.mystatisticsrecyclerview);
        nostatics = findViewById(R.id.nostatics);
        staticsdata = findViewById(R.id.staticsdata);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        /*recyclerView.setHasFixedSize(true);*/
        recyclerView.setLayoutManager(layoutManager);

        mData = new ArrayList<>();

        final UserLocalStore userLocalStore = new UserLocalStore(MyStatisticsActivity.this);
        final CurrentUser user = userLocalStore.getLoggedInUser();

        //For get all statics
        mQueue = Volley.newRequestQueue(MyStatisticsActivity.this);
        mQueue.getCache().clear();
        String url = resources.getString(R.string.api) + "my_statistics/" + user.getMemberid();

        final JsonObjectRequest request;
        request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            loadingDialog.dismiss();
            try {
                JSONArray arr = response.getJSONArray("my_statistics");
                JSON_PARSE_DATA_AFTER_WEBCALL(arr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VolleyError", "error" + error.getMessage() + "   " + error.fillInStackTrace());
            }
        }) {
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

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                if (response.data == null || response.data.length == 0) {
                    return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
                } else {
                    return super.parseNetworkResponse(response);
                }
            }
        };
        request.setShouldCache(false);
        mQueue.add(request);
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array) {
        if (TextUtils.equals(String.valueOf(array.length()), "0")) {
            staticsdata.setVisibility(View.GONE);
            nostatics.setVisibility(View.VISIBLE);
        } else {
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = null;
                try {
                    json = array.getJSONObject(i);

                    MyStatisticsData data = new MyStatisticsData(json.getString("match_name"), json.getString("m_id"), json.getString("match_time"), json.getString("paid"), json.getString("won"));
                    mData.add(data);
                    myAdapter = new MyStatisticsAdapter(MyStatisticsActivity.this, mData);
                    myAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(myAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
