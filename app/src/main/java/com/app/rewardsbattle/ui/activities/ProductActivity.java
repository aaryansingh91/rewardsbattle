//for show all available product for buy
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
import com.app.rewardsbattle.models.ProductData;
import com.app.rewardsbattle.ui.adapters.ProductAdapter;
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

public class ProductActivity extends AppCompatActivity {

    TextView shopnowtitle;
    ImageView back;
    RecyclerView rv;
    ProductAdapter myAdapter;
    List<ProductData> mData;
    RequestQueue mQueue;
    LoadingDialog loadingDialog;
    TextView noProduct;
    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_product);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        context = LocaleHelper.setLocale(ProductActivity.this);
        resources = context.getResources();

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


        shopnowtitle = findViewById(R.id.shpnowtitleid);

        shopnowtitle.setText(resources.getString(R.string.shop_now));

        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();

        back = findViewById(R.id.backinproduct);
        back.setOnClickListener(view -> onBackPressed());

        rv = findViewById(R.id.productrv);
        noProduct = findViewById(R.id.noproduct);
        mData = new ArrayList<>();

        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        /*rv.setHasFixedSize(true);*/
        rv.setLayoutManager(manager);

        //product api call
        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();

        String url = resources.getString(R.string.api) + "product";

        final JsonObjectRequest request = new JsonObjectRequest(url, null,
                response -> {

                    loadingDialog.dismiss();
                    try {
                        JSONArray arr = response.getJSONArray("product");
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
                UserLocalStore userLocalStore = new UserLocalStore(ProductActivity.this);
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

    public void JSON_PARSE_DATA_AFTER_WEBCALL(final JSONArray array) {
        if (TextUtils.equals(String.valueOf(array.length()), "0")) {
            rv.setVisibility(View.GONE);
            noProduct.setVisibility(View.VISIBLE);
        } else {
            for (int i = 0; i < array.length(); i++) {
                try {
                    final JSONObject json = array.getJSONObject(i);
                    ProductData data = new ProductData(json.getString("product_id"), json.getString("product_name"), json.getString("product_image"), json.getString("product_short_description"), json.getString("product_description"), json.getString("product_actual_price"), json.getString("product_selling_price"));
                    mData.add(data);
                    myAdapter = new ProductAdapter(ProductActivity.this, mData);
                    myAdapter.notifyDataSetChanged();
                    rv.setAdapter(myAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}