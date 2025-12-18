//For add money, withdraw and transaction
package com.app.rewardsbattle.ui.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
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
import java.util.Objects;

import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;
import me.toptas.fancyshowcase.listener.DismissListener;


public class MyWalletActivity extends AppCompatActivity {

    TextView mywallettitle;
    ImageView back;
    TextView balance;
    TextView winMoneyTv;
    TextView joinMoneyTv;
    RequestQueue dQueue;
    UserLocalStore userLocalStore;
    LoadingDialog loadingDialog;
    String winMoney = "";
    String joinMoney = "";
    LinearLayout addBtn;
    LinearLayout withdrawBtn;
    LinearLayout transaction;
    TextView earnings;
    TextView payouts;
    String from = "";

    Context context;
    Resources resources;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_my_wallet);

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


        context = LocaleHelper.setLocale(MyWalletActivity.this);
        resources = context.getResources();

        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();


        mywallettitle = findViewById(R.id.mywallettitleid);

        balance = findViewById(R.id.balanceinwallet);
        winMoneyTv = findViewById(R.id.winmoneyinwallet);
        joinMoneyTv = findViewById(R.id.joinmoneyinwallet);
        earnings = findViewById(R.id.earnings);
        payouts = findViewById(R.id.payouts);


        mywallettitle.setText(resources.getString(R.string.my_wallet));


        Intent intent = getIntent();
        from = intent.getStringExtra("FROM");

        back = findViewById(R.id.backfromwallet);
        back.setOnClickListener(view -> onBackPressed());
        addBtn = findViewById(R.id.addbtn);
        withdrawBtn = findViewById(R.id.withdrawbtn);
        transaction = findViewById(R.id.transaction);
        addBtn.setOnClickListener(v -> {
            Intent intent1 = new Intent(getApplicationContext(), AddMoneyActivity.class);
            startActivity(intent1);
        });
        withdrawBtn.setOnClickListener(v -> {
            Intent intent12 = new Intent(getApplicationContext(), WithdrawMoneyActivity.class);
            startActivity(intent12);
        });
        transaction.setOnClickListener(v -> {
            Intent intent12 = new Intent(getApplicationContext(), TransactionActivity.class);
            startActivity(intent12);
        });

        IntentFilter intentFilter = new IntentFilter("reload");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(broadcastReceiver, intentFilter, RECEIVER_NOT_EXPORTED);
        }


        userLocalStore = new UserLocalStore(getApplicationContext());

        //dashboard api call
        dashBoard();

        new FancyShowCaseQueue()
                .add(addView(addBtn, resources.getString(R.string.show_case_4), "4"))
                .add(addView(withdrawBtn, resources.getString(R.string.show_case_5), "5"))
                .show();
    }

    public void dashBoard() {

        dQueue = Volley.newRequestQueue(getApplicationContext());
        dQueue.getCache().clear();

        userLocalStore = new UserLocalStore(getApplicationContext());
        final CurrentUser user = userLocalStore.getLoggedInUser();

        String durl = resources.getString(R.string.api) + "dashboard/" + user.getMemberid();

        @SuppressLint("DefaultLocale") final JsonObjectRequest drequest = new JsonObjectRequest(durl, null,
                response -> {
                    loadingDialog.dismiss();
                    try {
                        JSONObject memobj = new JSONObject(response.getString("member"));
                        Log.d("MEMOBJ", String.valueOf(memobj));
                        winMoney = memobj.getString("wallet_balance");
                        joinMoney = memobj.getString("join_money");
                        if (TextUtils.equals(winMoney, "null")) {
                            winMoney = "0";
                        }
                        if (TextUtils.equals(joinMoney, "null")) {
                            joinMoney = "0";
                        }


                        String win = "";
                        if (isDouble(winMoney)) {
                            win = String.format("%.2f", Double.parseDouble(winMoney));
                        } else {
                            win = String.valueOf(Integer.parseInt(winMoney));
                        }


                        String joinm = "";
                        if (isDouble(joinMoney)) {
                            joinm = String.format("%.2f", Double.parseDouble(joinMoney));
                        } else {
                            joinm = String.valueOf(Integer.parseInt(joinMoney));
                        }

                        String totalMoney = "";
                        if (win.startsWith("0")) {
                            if (isDouble(joinm)) {
                                totalMoney = String.valueOf(Double.parseDouble(joinm));
                            } else {
                                totalMoney = String.valueOf(Integer.parseInt(joinm));
                            }
                        } else if (joinm.startsWith("-")) {
                            if (isDouble(win)) {
                                totalMoney = String.valueOf(Double.parseDouble(win));
                            } else {
                                totalMoney = String.valueOf(Integer.parseInt(win));
                            }
                        } else {
                            if (isDouble(win) && isDouble(joinm)) {
                                totalMoney = String.valueOf(Double.parseDouble(win) + Double.parseDouble(joinm));
                            } else {
                                totalMoney = String.valueOf(Integer.parseInt(win) + Integer.parseInt(joinm));
                            }
                        }
                        if (isDouble(totalMoney)) {
                            totalMoney = String.format("%.2f", Double.parseDouble(totalMoney));
                        }

                        balance.setText(totalMoney);

                        SpannableStringBuilder builder = new SpannableStringBuilder();
                        builder.append(Html.fromHtml(win));
                        winMoneyTv.setText(builder);

                        builder = new SpannableStringBuilder();
                        builder.append(Html.fromHtml(joinm));
                        joinMoneyTv.setText(builder);

                        JSONObject totwinobj = new JSONObject(response.getString("tot_win"));
                        if (TextUtils.equals(totwinobj.getString("total_win"), "null")) {
                            earnings.setText("0");
                        } else {
                            if (isDouble(totwinobj.getString("total_win"))) {
                                earnings.setText(String.format("%.2f", Double.parseDouble(totwinobj.getString("total_win"))));
                            } else {
                                earnings.setText(totwinobj.getString("total_win"));
                            }
                        }

                        JSONObject totwithobj = new JSONObject(response.getString("tot_withdraw"));

                        if (TextUtils.equals(totwithobj.getString("tot_withdraw"), "null")) {
                            payouts.setText("0");
                        } else {
                            if (isDouble(totwithobj.getString("tot_withdraw"))) {
                                payouts.setText(String.format("%.2f", Double.parseDouble(totwithobj.getString("tot_withdraw"))));
                            } else {
                                payouts.setText(totwithobj.getString("tot_withdraw"));
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("**VolleyError", "error" + error.getMessage())) {
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
        drequest.setShouldCache(false);
        dQueue.add(drequest);
    }

    boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void refresh() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (TextUtils.equals(from, "EARN")) {
            Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
            intent.putExtra("N", "0");
            startActivity(intent);
        } else if (TextUtils.equals(from, "PLAY")) {
            finish();
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

    public FancyShowCaseView addView(View view, String title, String n) {
        FancyShowCaseView view1 = new FancyShowCaseView.Builder(this)
                .focusOn(view)
                .title(title)
                //.titleGravity(Gravity.BOTTOM)
                .titleSize(20, 1)
                .focusShape(FocusShape.CIRCLE)
                .enableAutoTextPosition()
                .roundRectRadius(10)
                .focusBorderSize(1)
                .showOnce(n)
                .focusBorderColor(getResources().getColor(R.color.newblue))
                .dismissListener(new DismissListener() {
                    @Override
                    public void onDismiss(String s) {

                    }

                    @Override
                    public void onSkipped(String s) {

                    }
                })
                .build();

        return view1;
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
