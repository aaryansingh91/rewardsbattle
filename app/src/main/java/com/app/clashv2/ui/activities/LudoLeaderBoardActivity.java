package com.app.clashv2.ui.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import com.app.clashv2.utils.UserLocalStore;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LudoLeaderBoardActivity extends AppCompatActivity {


    ImageView back;
    LoadingDialog loadingDialog;
    LinearLayout ll, noleaderdataludo;
    CurrentUser user;
    ScrollView leaderludo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_ludo_leader_board);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();

        back = findViewById(R.id.backinludoleaderboard);
        back.setOnClickListener(v -> onBackPressed());

        ll = findViewById(R.id.ludoleaderboardll);
        leaderludo = findViewById(R.id.leaderludo);
        noleaderdataludo = findViewById(R.id.noleaderdataludo);

        RequestQueue jQueue = Volley.newRequestQueue(this);
        jQueue.getCache().clear();

        final UserLocalStore userLocalStore = new UserLocalStore(this);
        user = userLocalStore.getLoggedInUser();


        SharedPreferences sp = getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
        String gameid = sp.getString("gameid", "");

        Log.d("id", gameid);

        String url = getResources().getString(R.string.api) + "ludo_leader_board/" + gameid;

        final JsonObjectRequest request = new JsonObjectRequest(url, null,
                response -> {
                    loadingDialog.dismiss();
                    try {
                        JSONArray arr = response.getJSONArray("list");
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
                String token = "Bearer " + user.getToken();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                return headers;
            }
        };
        request.setShouldCache(false);
        jQueue.add(request);

    }

    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array) {
        if (TextUtils.equals(String.valueOf(array.length()), "0")) {
            leaderludo.setVisibility(View.GONE);
            noleaderdataludo.setVisibility(View.VISIBLE);
        } else {
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = null;
                try {
                    json = array.getJSONObject(i);

                    View view = getLayoutInflater().inflate(R.layout.ludo_leaderboard_layout, null);
                    TextView name = view.findViewById(R.id.name_lll);
                    TextView wonamount = view.findViewById(R.id.wonamount_lll);
                    TextView wonchallenge = view.findViewById(R.id.won_challenge_lll);
                    ImageView iv = view.findViewById(R.id.iv_lll);
                    TextView number = view.findViewById(R.id.number_lll);

                    name.setText(json.getString("first_name") + " " + json.getString("last_name"));

                    SpannableStringBuilder builder = new SpannableStringBuilder();
                    builder.append("Won: ")
                            .append(" ", new ImageSpan(getApplicationContext(), R.drawable.resize_coin1618, ImageSpan.ALIGN_BASELINE), 0)
                            .append(" ")
                            .append(json.getString("total_amount"));
                    wonamount.setText(builder);

                    wonchallenge.setText("Won : " + json.getString("total_challenge") + " Challenges");

                    number.setText("#" + (i + 1));

                    if (TextUtils.equals(json.getString("profile_image"), "") || TextUtils.equals(json.getString("profile_image"), "null")) {

                    } else {
                        Picasso.get().load(json.getString("profile_image")).placeholder(R.drawable.battlemanialogo).fit().into(iv);
                    }
                    ll.addView(view);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}