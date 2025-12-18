//for show all order
package com.app.rewardsbattle.ui.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.models.OrderData;
import com.app.rewardsbattle.ui.adapters.OrderAdapter;
import com.app.rewardsbattle.utils.AnalyticsUtil;
import com.app.rewardsbattle.utils.LoadingDialog;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.app.rewardsbattle.utils.UserLocalStore;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyOrderActivity extends AppCompatActivity {

    TextView myordertitle;
    ImageView back;
    RecyclerView rv;
    OrderAdapter myAdapter;
    List<OrderData> mData;
    RequestQueue mQueue;
    LoadingDialog loadingDialog;
    TextView noorder;
    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_my_order);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        //check baner ads enable or not
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

        context = LocaleHelper.setLocale(MyOrderActivity.this);
        resources = context.getResources();

        myordertitle = findViewById(R.id.myordertitleid);

        myordertitle.setText(resources.getString(R.string.my_order));


        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();

        back = findViewById(R.id.backinmyorder);
        back.setOnClickListener(view -> onBackPressed());

        rv = findViewById(R.id.orderrv);
        noorder = findViewById(R.id.noorder);
        mData = new ArrayList<>();

        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        /*rv.setHasFixedSize(true);*/
        rv.setLayoutManager(manager);

        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();
        final UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());
        final CurrentUser user = userLocalStore.getLoggedInUser();

        //my_order api call
        String url = resources.getString(R.string.api) + "my_order/" + user.getMemberid();

        final JsonObjectRequest request = new JsonObjectRequest(url, null,
                response -> {

                    loadingDialog.dismiss();

                    try {

                        JSONArray arr = response.getJSONArray("my_orders");
                        JSON_PARSE_DATA_AFTER_WEBCALL(arr);

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
                String credentials = user.getUsername() + ":" + user.getPassword();
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
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

    public void JSON_PARSE_DATA_AFTER_WEBCALL(final JSONArray array) {

        if (TextUtils.equals(String.valueOf(array.length()), "0")) {
            rv.setVisibility(View.GONE);
            noorder.setVisibility(View.VISIBLE);
        } else {
            for (int i = 0; i < array.length(); i++) {
                try {
                    final JSONObject json = array.getJSONObject(i);
                    Log.d("order", json.toString());
                    OrderData data = new OrderData(json.getString("orders_id"), json.getString("order_no"), json.getString("product_name"), json.getString("product_image"), json.getString("product_price"), json.getString("address"), json.getString("order_status"), json.getString("courier_id"), json.getString("tracking_id"), json.getString("created_date"), json.getString("courier_link"), json.getString("name"), json.getString("add_info"));
                    mData.add(data);
                    myAdapter = new OrderAdapter(MyOrderActivity.this, mData);
                    myAdapter.notifyDataSetChanged();
                    rv.setAdapter(myAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}