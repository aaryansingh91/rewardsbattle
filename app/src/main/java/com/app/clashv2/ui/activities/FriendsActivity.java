package com.app.clashv2.ui.activities;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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

public class FriendsActivity extends AppCompatActivity {

    ImageView back;
    LoadingDialog loadingDialog;
    LinearLayout ll,nofrdslist;
    CurrentUser user;
    ScrollView frdlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_friends);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        loadingDialog=new LoadingDialog(this);
        loadingDialog.show();

        back= findViewById(R.id.backinfriends);
        back.setOnClickListener(v -> onBackPressed());

        ll= findViewById(R.id.friendsll);

        frdlist = findViewById(R.id.frdlist);
        nofrdslist = findViewById(R.id.nofrdslist);

        RequestQueue jQueue = Volley.newRequestQueue(this);
        jQueue.getCache().clear();

        final UserLocalStore userLocalStore = new UserLocalStore(this);
        user = userLocalStore.getLoggedInUser();

        SharedPreferences sp = getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
        String gameid = sp.getString("gameid", "");

        Log.d("id",gameid);

        String url = getResources().getString(R.string.api) + "budy_list/" + gameid;

        final JsonObjectRequest request = new JsonObjectRequest(url, null,
                response -> {
                    loadingDialog.dismiss();

                    try {
                        JSONArray arr = response.getJSONArray("member_list");
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

    @SuppressLint("SetTextI18n")
    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array) {

        if (TextUtils.equals(String.valueOf(array.length()), "0")) {
            frdlist.setVisibility(View.GONE);
            nofrdslist.setVisibility(View.VISIBLE);
        } else {
            for (int i = 0; i < array.length(); i++) {
                JSONObject json;
                try {
                    json = array.getJSONObject(i);

                    @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.friends_layout, null);
                    TextView name = view.findViewById(R.id.name_fl);

                    ImageView iv = view.findViewById(R.id.iv_fl);
                    CardView requestToPlay = view.findViewById(R.id.requesttoplay_fl);

                    name.setText(json.getString("first_name") + " " + json.getString("last_name"));


                    if (TextUtils.equals(json.getString("profile_image"), "") || TextUtils.equals(json.getString("profile_image"), "null")) {

                    } else {
                        Picasso.get().load(json.getString("profile_image")).placeholder(R.drawable.battlemanialogo).fit().into(iv);
                    }

                    JSONObject finalJson = json;
                    requestToPlay.setOnClickListener(v -> {
                        try {
                            sendRequest(finalJson.getString("member_id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                    ll.addView(view);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendRequest(String memberId){

        loadingDialog.show();

        RequestQueue jQueue = Volley.newRequestQueue(this);
        jQueue.getCache().clear();

        final UserLocalStore userLocalStore = new UserLocalStore(this);
        user = userLocalStore.getLoggedInUser();

        SharedPreferences sp = getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
        String gameid = sp.getString("gameid", "");

        Log.d("id",gameid);

        String url = getResources().getString(R.string.api) + "budy_play_request/"+memberId + "/" + gameid;

        final JsonObjectRequest request = new JsonObjectRequest(url, null,
                response -> {
                    //Log.d("notification----", response.toString());

                    loadingDialog.dismiss();
                    try {
                        Toast.makeText(getApplicationContext(),response.getString("message"),Toast.LENGTH_SHORT).show();
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
}