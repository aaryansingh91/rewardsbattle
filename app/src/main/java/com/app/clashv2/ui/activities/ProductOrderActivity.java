//for buy any product or place order
package com.app.clashv2.ui.activities;

import static com.android.volley.Request.Method.POST;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProductOrderActivity extends AppCompatActivity {

    TextInputLayout productorder_information_textinput, productorder_fullname_textinput, productorder_address_textinput;
    ImageView back;
    TextView title;
    TextView current;
    TextView total;
    EditText name;
    EditText address;
    EditText additional;
    Button cancel;
    Button buy;
    LoadingDialog loadingDialog;
    UserLocalStore userLocalStore;
    CurrentUser user;
    RequestQueue dQueue, mQueue;
    String id = "";

    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_product_order);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        //check baner ads enable or not
        SharedPreferences spb = getSharedPreferences("SMINFO", MODE_PRIVATE);
        if (TextUtils.equals(spb.getString("baner", "no"), "yes")) {

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

        context = LocaleHelper.setLocale(ProductOrderActivity.this);
        resources = context.getResources();

        loadingDialog = new LoadingDialog(this);
        userLocalStore = new UserLocalStore(this);
        user = userLocalStore.getLoggedInUser();

        back = findViewById(R.id.backfromorder);
        back.setOnClickListener(view -> onBackPressed());


        productorder_fullname_textinput = findViewById(R.id.productorder_fullname_textinput_id);
        productorder_information_textinput = findViewById(R.id.productorder_information_textinput_id);
        productorder_address_textinput = findViewById(R.id.productorder_address_textinput_id);
        title = findViewById(R.id.ordertitle);
        current = findViewById(R.id.ordercurrent);
        total = findViewById(R.id.ordertotal);
        name = findViewById(R.id.orderfullname);
        address = findViewById(R.id.orderadd);
        additional = findViewById(R.id.orderadditionnal);
        cancel = findViewById(R.id.ordercancel);
        buy = findViewById(R.id.orderbuy);


        productorder_fullname_textinput.setHint(resources.getString(R.string.full_name));
        productorder_address_textinput.setHint(resources.getString(R.string.address));
        productorder_information_textinput.setHint(resources.getString(R.string.additional));
        cancel.setText(resources.getString(R.string.cancel));
        buy.setText(resources.getString(R.string.buy_now));

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        title.setText(intent.getStringExtra("name"));

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(Html.fromHtml(resources.getString(R.string.total_payable_amount__)))
                .append(" ", new ImageSpan(getApplicationContext(), R.drawable.resize_coin1617, ImageSpan.ALIGN_BASELINE), 0)
                .append(" ")
                .append(Html.fromHtml("<b>" + intent.getStringExtra("price")));
        total.setText(builder);
        total.setClickable(true);
        total.setMovementMethod(LinkMovementMethod.getInstance());

        dQueue = Volley.newRequestQueue(getApplicationContext());
        dQueue.getCache().clear();

        String durl = resources.getString(R.string.api) + "dashboard/" + user.getMemberid();

        @SuppressLint("DefaultLocale") final JsonObjectRequest drequest = new JsonObjectRequest(durl, null,
                response -> {
                    loadingDialog.dismiss();
                    //Log.d("respons",response.toString());

                    try {
                        JSONObject memobj = new JSONObject(response.getString("member"));
                        String winMoney = memobj.getString("wallet_balance");
                        String joinMoney = memobj.getString("join_money");
                        if (TextUtils.equals(winMoney, "null")) {
                            winMoney = "0";
                        }
                        if (TextUtils.equals(joinMoney, "null")) {
                            joinMoney = "0";
                        }

                        String win;
                        if (isDouble(winMoney)) {
                            win = String.format("%.2f", Double.parseDouble(winMoney));
                        } else {
                            win = String.valueOf(Integer.parseInt(winMoney));
                        }

                        String joinm;
                        if (isDouble(joinMoney)) {
                            joinm = String.format("%.2f", Double.parseDouble(joinMoney));
                        } else {
                            joinm = String.valueOf(Integer.parseInt(joinMoney));
                        }

                        String totalMoney;
                        if(win.startsWith("0")){
                            if (isDouble(joinm)) {
                                totalMoney = String.valueOf(Double.parseDouble(joinm));
                            } else {
                                totalMoney = String.valueOf(Integer.parseInt(joinm));
                            }
                        }else if(joinm.startsWith("-")){
                            if (isDouble(win)) {
                                totalMoney = String.valueOf(Double.parseDouble(win));
                            } else {
                                totalMoney = String.valueOf(Integer.parseInt(win));
                            }
                        }else {
                            if (isDouble(win) && isDouble(joinm)) {
                                totalMoney = String.valueOf(Double.parseDouble(win) + Double.parseDouble(joinm));
                            } else {
                                totalMoney = String.valueOf(Integer.parseInt(win) + Integer.parseInt(joinm));
                            }
                        }
                        if (isDouble(totalMoney)) {
                            totalMoney = String.format("%.2f", Double.parseDouble(totalMoney));
                        }

                        SpannableStringBuilder builder1 = new SpannableStringBuilder();
                        builder1.append(Html.fromHtml(resources.getString(R.string.your_current_balance__)))
                                .append(" ", new ImageSpan(getApplicationContext(), R.drawable.resize_coin1617, ImageSpan.ALIGN_BASELINE), 0)
                                .append(" ")
                                .append(Html.fromHtml("<b>" + totalMoney));
                        current.setText(builder1);

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
                String token = "Bearer " + user.getToken();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                return headers;
            }
        };
        drequest.setShouldCache(false);
        dQueue.add(drequest);

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.equals(name.getText().toString().trim(), "")) {
                    Toast.makeText(ProductOrderActivity.this, resources.getString(R.string.please_enter_your_name), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.equals(address.getText().toString().trim(), "")) {
                    Toast.makeText(ProductOrderActivity.this, resources.getString(R.string.please_enter_your_address), Toast.LENGTH_SHORT).show();
                    return;
                }
                /*product order api call start*/
                loadingDialog.show();
                mQueue = Volley.newRequestQueue(getApplicationContext());
                mQueue.getCache().clear();
                String surl = resources.getString(R.string.api) + "product_order";
                final UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());

                JSONObject addobj = new JSONObject();
                JSONObject postobj = new JSONObject();
                try {
                    addobj.put("name", name.getText().toString().trim());
                    addobj.put("address", address.getText().toString().trim());
                    addobj.put("add_info", additional.getText().toString().trim());
                    postobj.put("submit", "order");
                    postobj.put("product_id", id);
                    postobj.put("member_id", user.getMemberid());
                    postobj.put("shipping_address", addobj);
                    Log.d("AAAAAA", postobj.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final JsonObjectRequest srequest = new JsonObjectRequest(POST, surl, postobj,
                        response -> {
                            loadingDialog.dismiss();

                            try {
                                String status = response.getString("status");
                                if (TextUtils.equals(status, "true")) {
                                    Intent intent1 = new Intent(getApplicationContext(), SuccessOrderActivity.class);
                                    startActivity(intent1);
                                } else {
                                    Toast.makeText(ProductOrderActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                                }
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
                        String token = "Bearer " + user.getToken();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", token);
                        return headers;
                    }
                };
                srequest.setShouldCache(false);
                mQueue.add(srequest);
                /*product order api call end*/
            }
        });


        cancel.setOnClickListener(v -> onBackPressed());
    }

    boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}