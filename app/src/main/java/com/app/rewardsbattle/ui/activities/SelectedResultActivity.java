//For description about completed match
package com.app.rewardsbattle.ui.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.net.Uri;
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
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import android.widget.RelativeLayout;

public class SelectedResultActivity extends AppCompatActivity {

    TextView matchresulttitle;
    TextView winnertitle;
    TextView playernametitle;
    TextView killstitle;
    TextView winningtitle;
    TextView fullresulttitle;
    TextView playrnametitle;
    TextView killtitle;
    TextView winngtitle;
    ImageView back;
    RequestQueue mQueue;
    TextView resultTitleAndNumber;
    TextView resultTime;
    TextView resultPrizePool;
    TextView resultPerKill;
    TextView resultEntryFee;
    LinearLayout winResultLl;
    LinearLayout fullResultLl;
    LinearLayout resultNotificationCardview;
    TextView resultNotification;
    LoadingDialog loadingDialog;
    CardView imageViewSelectedCardview;
    ImageView imgeviewSelected;
    SwipeRefreshLayout pullToRefresh;
    UserLocalStore userLocalStore;
    CurrentUser user;
    int N = 0;

    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_selected_result);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        //check baner ads enable or not
        SharedPreferences spb = getSharedPreferences("SMINFO", MODE_PRIVATE);
        if (TextUtils.equals(spb.getString("baner", "no"), "yes")) {

            RelativeLayout bannerLayout = findViewById(R.id.banner_container);
            if (bannerLayout != null) {
                BannerView bannerView = new BannerView(this, getString(R.string.unity_banner_id), new UnityBannerSize(320, 50));
                bannerLayout.addView(bannerView);
                bannerView.load();
            }
        }

        context = LocaleHelper.setLocale(SelectedResultActivity.this);
        resources = context.getResources();

        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();

        pullToRefresh = findViewById(R.id.pullToRefreshselectresult);
        pullToRefresh.setOnRefreshListener(() -> {
            pullToRefresh.setRefreshing(true);
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
            pullToRefresh.setRefreshing(false);
        });


        matchresulttitle = findViewById(R.id.matchresulttitleid);
        winnertitle = findViewById(R.id.winnertitleid);
        playernametitle = findViewById(R.id.playernametitleid);
        killstitle = findViewById(R.id.killstitleid);
        winningtitle = findViewById(R.id.winnningtitleid);
        fullresulttitle = findViewById(R.id.fullresulttitleid);
        playrnametitle = findViewById(R.id.playrnametitleid);
        killtitle = findViewById(R.id.killtitleid);
        winngtitle = findViewById(R.id.winnninggtitleid);

        matchresulttitle.setText(resources.getString(R.string.match_result));
        winnertitle.setText(resources.getString(R.string.winner));
        playernametitle.setText(resources.getString(R.string.player_name));
        killstitle.setText(resources.getString(R.string.kills));
        winningtitle.setText(resources.getString(R.string.winning));
        fullresulttitle.setText(resources.getString(R.string.match_result));
        playrnametitle.setText(resources.getString(R.string.player_name));
        killtitle.setText(resources.getString(R.string.kills));
        winngtitle.setText(resources.getString(R.string.winning));


        resultTitleAndNumber = findViewById(R.id.resulttitleandnumber);
        resultTime = findViewById(R.id.resulttime);
        resultPrizePool = findViewById(R.id.resultprizepool);
        resultPerKill = findViewById(R.id.resultperkill);
        resultEntryFee = findViewById(R.id.resultentryfee);
        resultNotificationCardview = findViewById(R.id.resultnotificationll);
        resultNotification = findViewById(R.id.resultnotification);
        winResultLl = findViewById(R.id.winresultll);
        fullResultLl = findViewById(R.id.fullresultll);
        back = findViewById(R.id.backfromselectedresult);
        back.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), SelectedGameActivity.class);
            intent.putExtra("N", "2");
            startActivity(intent);
        });

        Intent intent = getIntent();
        String mid = intent.getStringExtra("M_ID");
        String baner = intent.getStringExtra("BANER");

        imageViewSelectedCardview = findViewById(R.id.imageviewselectedresultcardview);
        imgeviewSelected = findViewById(R.id.imageviewselectedresult);
        if (!TextUtils.equals(baner, "")) {
            imageViewSelectedCardview.setVisibility(View.VISIBLE);
            Picasso.get().load(Uri.parse(baner)).placeholder(R.drawable.default_battlemania).fit().into(imgeviewSelected);
        } else {
            imageViewSelectedCardview.setVisibility(View.GONE);
            //imgeviewSelected.setImageDrawable(getDrawable(R.drawable.default_battlemania));
        }
        SharedPreferences sp = getSharedPreferences("currencyinfo", Context.MODE_PRIVATE);
        // final String selectedcurrency = sp.getString("currency", "₹");
        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();
        userLocalStore = new UserLocalStore(getApplicationContext());
        user = userLocalStore.getLoggedInUser();

        //call single_game_result for detail about result tab tournament
        String url = resources.getString(R.string.api) + "single_game_result/" + mid;

        final JsonObjectRequest request = new JsonObjectRequest(url, null,
                response -> {
                    try {
                        JSONObject obj = response.getJSONObject("match_deatils");
                        resultTitleAndNumber.setText(obj.getString("match_name") + " - " + resources.getString(R.string.match) + " #" + obj.getString("m_id"));
                        resultTime.setText(Html.fromHtml(resources.getString(R.string.organised_on) + " <b>" + obj.getString("match_time") + "</b>"));
                        resultTime.setClickable(true);
                        resultTime.setMovementMethod(LinkMovementMethod.getInstance());

                        SpannableStringBuilder builder = new SpannableStringBuilder();
                        builder.append(Html.fromHtml(resources.getString(R.string.winning_prize) + " : "))
                                /*.append(" ", new ImageSpan(getApplicationContext(), R.drawable.resize_coin1617,ImageSpan.ALIGN_BASELINE), 0)
                                .append(" ")*/
                                .append(Html.fromHtml("<b>" + obj.getString("win_prize")))
                                .append("(%)");
                        resultPrizePool.setText(builder);

                        builder = new SpannableStringBuilder();
                        builder.append(Html.fromHtml(resources.getString(R.string.per_kill) + " : "))
                                /* .append(" ", new ImageSpan(getApplicationContext(), R.drawable.resize_coin1617,ImageSpan.ALIGN_BASELINE), 0)
                                 .append(" ")*/
                                .append(Html.fromHtml("<b>" + obj.getString("per_kill")))
                                .append("(%)");
                        resultPerKill.setText(builder);

                        builder = new SpannableStringBuilder();
                        builder.append(Html.fromHtml(resources.getString(R.string.entry_fee) + " : "))
                                .append(" ", new ImageSpan(getApplicationContext(), R.drawable.resize_coin1617, ImageSpan.ALIGN_BASELINE), 0)
                                .append(" ")
                                .append(Html.fromHtml("<b>" + obj.getString("entry_fee")));
                        resultEntryFee.setText(builder);

                        if (TextUtils.equals(obj.getString("result_notification"), "null") || TextUtils.equals(obj.getString("result_notification"), "")) {
                            resultNotificationCardview.setVisibility(View.GONE);
                        } else {
                            resultNotificationCardview.setVisibility(View.VISIBLE);
                            resultNotification.setText(obj.getString("result_notification"));
                        }
                        JSONArray winarr = response.getJSONArray("match_winner");
                        JSON_PARSE_DATA_AFTER_WEBCALL(winarr);
                        JSONArray fullarr = response.getJSONArray("full_result");
                        JSON_PARSE_DATA_AFTER_WEBCALL_FULL(fullarr);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    loadingDialog.dismiss();
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
        request.setShouldCache(false);
        mQueue.add(request);
    }

    @SuppressLint("DefaultLocale")
    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array) {

        for (int i = 0; i < array.length(); i++) {
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);
                View view1 = getLayoutInflater().inflate(R.layout.selectedresultdata, null);
                TextView winno = view1.findViewById(R.id.no);
                TextView winpname = view1.findViewById(R.id.pname);
                TextView winkills = view1.findViewById(R.id.kills);
                TextView winwining = view1.findViewById(R.id.wining);

                winno.setText(String.valueOf(i + 1));
                winpname.setText(json.getString("pubg_id"));
                winkills.setText(json.getString("killed"));
                if (isDouble(json.getString("total_win"))) {
                    winwining.setText(String.format("%.2f", Double.parseDouble(json.getString("total_win"))));
                } else {
                    winwining.setText(json.getString("total_win"));
                }
                winResultLl.addView(view1);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALL_FULL(JSONArray array) {

        for (int i = 0; i < array.length(); i++) {
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);

                View view2 = getLayoutInflater().inflate(R.layout.selectedresultdata, null);
                TextView winno = view2.findViewById(R.id.no);
                TextView winpname = view2.findViewById(R.id.pname);
                TextView winkills = view2.findViewById(R.id.kills);
                TextView winwining = view2.findViewById(R.id.wining);

                winno.setText(String.valueOf(i + 1));
                winpname.setText(json.getString("pubg_id"));
                winkills.setText(json.getString("killed"));
                winwining.setText(json.getString("total_win"));

                if (TextUtils.equals(json.getString("user_name"), user.getUsername())) {
                    if (N == 0) {
                        fullResultLl.addView(view2, 0);
                    } else if (N == 1) {
                        fullResultLl.addView(view2, 1);
                    } else if (N == 2) {
                        fullResultLl.addView(view2, 2);
                    } else if (N == 3) {
                        fullResultLl.addView(view2, 3);
                    } else if (N == 4) {
                        fullResultLl.addView(view2, 4);
                    }
                    N++;
                } else {
                    fullResultLl.addView(view2);
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
