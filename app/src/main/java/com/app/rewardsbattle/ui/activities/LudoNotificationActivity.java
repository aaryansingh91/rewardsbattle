package com.app.rewardsbattle.ui.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.utils.AnalyticsUtil;
import com.app.rewardsbattle.utils.LoadingDialog;
import com.app.rewardsbattle.utils.UserLocalStore;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LudoNotificationActivity extends AppCompatActivity {


    ImageView back;
    LoadingDialog loadingDialog;
    LinearLayout ll, nonotification;
    CurrentUser user;
    ScrollView notilist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_ludo_notification);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();
        back = findViewById(R.id.backinnotification);
        back.setOnClickListener(v -> onBackPressed());
        ll = findViewById(R.id.notificationll);

        notilist = findViewById(R.id.notilist);
        nonotification = findViewById(R.id.nonotification);

        RequestQueue jQueue = Volley.newRequestQueue(this);
        jQueue.getCache().clear();
        final UserLocalStore userLocalStore = new UserLocalStore(this);
        user = userLocalStore.getLoggedInUser();
        SharedPreferences sp = getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
        String gameid = sp.getString("gameid", "");
        Log.d("id", gameid);
        String url = getResources().getString(R.string.api) + "notification_list/" + gameid;

        final JsonObjectRequest request = new JsonObjectRequest(url, null,
                response -> {
                    loadingDialog.dismiss();
                    try {
                        JSONArray arr = response.getJSONArray("notifications");
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
            notilist.setVisibility(View.GONE);
            nonotification.setVisibility(View.VISIBLE);
        } else {
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = null;
                try {
                    json = array.getJSONObject(i);

                    View view = getLayoutInflater().inflate(R.layout.notification_layout, null);
                    TextView title = view.findViewById(R.id.notification_title);
                    TextView msg = view.findViewById(R.id.notification_msg);
                    TextView time = view.findViewById(R.id.notification_time);

                    title.setText(json.getString("heading"));
                    msg.setText(json.getString("content"));
                    time.setText(json.getString("date_created"));
                    ll.addView(view);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}