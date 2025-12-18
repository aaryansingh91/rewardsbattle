package com.app.rewardsbattle.ui.activities;

import static com.android.volley.Request.Method.GET;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.models.TransactionDetails;
import com.app.rewardsbattle.ui.adapters.TransactionAdapter;
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
import java.util.Objects;

public class TransactionActivity extends AppCompatActivity {

    Context context;
    Resources resources;
    LoadingDialog loadingDialog;
    String from = "";
    ImageView back;
    RecyclerView transactionRv;
    TransactionAdapter myAdapter;
    List<TransactionDetails> mData;
    SwipeRefreshLayout pullToRefresh;
    RequestQueue dQueue, mQueue;
    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_transaction);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

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
                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
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

        context = LocaleHelper.setLocale(TransactionActivity.this);
        resources = context.getResources();

        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();

        Intent intent = getIntent();
        from = intent.getStringExtra("FROM");

        userLocalStore = new UserLocalStore(getApplicationContext());

        pullToRefresh = findViewById(R.id.pullToRefreshtransaction);
        pullToRefresh.setOnRefreshListener(() -> {
            pullToRefresh.setRefreshing(true);
            refresh();
            pullToRefresh.setRefreshing(false);
        });

        transactionRv = findViewById(R.id.transactionrv);
        back = findViewById(R.id.backfromtransaction);
        back.setOnClickListener(view -> onBackPressed());
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(false);
        layoutManager.setReverseLayout(false);
        /*transactionRv.setHasFixedSize(true);*/
        transactionRv.setLayoutManager(layoutManager);
        mData = new ArrayList<>();

        //dashboard api call
        dashBoard();

    }

    public void dashBoard() {

        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();

        userLocalStore = new UserLocalStore(getApplicationContext());
        final CurrentUser user = userLocalStore.getLoggedInUser();

        String url = resources.getString(R.string.api) + "transaction";

        final JsonObjectRequest request = new JsonObjectRequest(GET, url, null,
                response -> {
                    try {
                        JSONArray arr = response.getJSONArray("transaction");
                        JSON_PARSE_DATA_AFTER_WEBCALL(arr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    loadingDialog.dismiss();
                }, error -> Log.e("VolleyError", "error" + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() throws
                    AuthFailureError {
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
        request.setShouldCache(false);
        mQueue.add(request);
    }

    boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array) {

        mData.clear();
        transactionRv.removeAllViews();

        for (int i = 0; i < array.length(); i++) {
            JSONObject json;
            try {
                json = array.getJSONObject(i);
                Log.d("Array", json.toString());
                if (!(TextUtils.equals(json.getString("deposit"), "0") && TextUtils.equals(json.getString("withdraw"), "0"))) {
                    TransactionDetails data = new TransactionDetails(json.getString("transaction_id"), json.getString("note"), json.getString("match_id"), json.getString("note_id"), json.getString("date"), json.getString("join_money"), json.getString("win_money"), json.getString("deposit"), json.getString("withdraw"));
                    mData.add(data);
                    myAdapter = new TransactionAdapter(TransactionActivity.this, mData);
                    myAdapter.notifyDataSetChanged();
                    transactionRv.setAdapter(myAdapter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void refresh() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
        pullToRefresh.setRefreshing(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (TextUtils.equals(from, "EARN")) {
            Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
            intent.putExtra("N", "0");
            startActivity(intent);
        } else if (TextUtils.equals(from, "PLAY")) {
            Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
            intent.putExtra("N", "1");
            startActivity(intent);
        } else if (TextUtils.equals(from, "ME")) {
            Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
            intent.putExtra("N", "2");
            startActivity(intent);
        } else if (TextUtils.equals(from, "ONGOING")) {
            Intent intent = new Intent(getApplicationContext(), SelectedGameActivity.class);
            intent.putExtra("N", "0");
            startActivity(intent);
        } else if (TextUtils.equals(from, "UPCOMING")) {
            Intent intent = new Intent(getApplicationContext(), SelectedGameActivity.class);
            intent.putExtra("N", "1");
            startActivity(intent);
        } else if (TextUtils.equals(from, "RESULT")) {
            Intent intent = new Intent(getApplicationContext(), SelectedGameActivity.class);
            intent.putExtra("N", "2");
            startActivity(intent);
        } else if (TextUtils.equals(from, "ONGOINGLOTTERY")) {
            Intent intent = new Intent(getApplicationContext(), LotteryActivity.class);
            intent.putExtra("N", "0");
            startActivity(intent);
        } else if (TextUtils.equals(from, "RESULTLOTTERY")) {
            Intent intent = new Intent(getApplicationContext(), LotteryActivity.class);
            intent.putExtra("N", "1");
            startActivity(intent);
        } else {
            finish();
        }

    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && Objects.equals(intent.getAction(), "reload")) {
                dashBoard();
            }
        }
    };
}