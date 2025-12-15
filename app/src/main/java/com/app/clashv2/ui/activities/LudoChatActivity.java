package com.app.clashv2.ui.activities;

import static com.android.volley.Request.Method.POST;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.clashv2.R;
import com.app.clashv2.models.ChatData;
import com.app.clashv2.models.CurrentUser;
import com.app.clashv2.utils.AnalyticsUtil;
import com.app.clashv2.utils.UserLocalStore;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LudoChatActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference myRef;
    String ludoID = "";

    LinearLayout ll;

    List<ChatData> list = new ArrayList<>();

    String senderImg = "";
    String receiverImg = "";

    CurrentUser user;
    UserLocalStore userLocalStore;

    EditText msgEt;
    LinearLayout send;

    ScrollView scrollview;

    ImageView back;
    ImageView receiverIvTitle;
    TextView receiverNameTitle;

    String senderName = "";
    String receiverPlayerId;
    RequestQueue mQueue;
    SharedPreferences fbkey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_ludo_chat);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        Intent intent = getIntent();
        ludoID = intent.getStringExtra("AUTO_ID");
        senderImg = intent.getStringExtra("SENDER_IMG");
        receiverImg = intent.getStringExtra("RECEIVER_IMG");
        senderName = intent.getStringExtra("SENDER_NAME");
        receiverPlayerId = intent.getStringExtra("RECEIVER_PLAYER_ID");

        fbkey = getSharedPreferences("SMINFO", MODE_PRIVATE);

        userLocalStore = new UserLocalStore(this);
        user = userLocalStore.getLoggedInUser();

        back = findViewById(R.id.backinchat);
        back.setOnClickListener(v -> onBackPressed());

        receiverIvTitle = findViewById(R.id.receiver_iv_title);
        receiverNameTitle = findViewById(R.id.receiver_name_title);

        receiverNameTitle.setText("Chat - " + intent.getStringExtra("RECEIVER_NAME"));

        if (TextUtils.equals(receiverImg, "") || TextUtils.equals(receiverImg, "null")) {
        } else {
            Picasso.get().load(receiverImg).placeholder(R.drawable.battlemanialogo).fit().into(receiverIvTitle);
        }
        scrollview = findViewById(R.id.chat_sv);

        ll = findViewById(R.id.chatll);
        msgEt = findViewById(R.id.msgEt);
        send = findViewById(R.id.sendll);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("chat").child(ludoID).child("messages");

        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                try {

                    ll.removeAllViews();

                    list = new ArrayList<>();

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Log.d("Register", "Value is: " + postSnapshot.getChildren());
                        ChatData data = postSnapshot.getValue(ChatData.class);
                        list.add(data);
                    }
                    //Collections.reverse(subList);
                    addToLL(list);


                } catch (Exception e) {
                    e.printStackTrace();

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("chat", "Failed to read value.", error.toException());
            }
        });

        send.setOnClickListener(v -> {
            //////////////////////////////////////////////
            String msg = msgEt.getText().toString().trim();

            if (TextUtils.equals(msg, "")) {
                return;
            }
            String id = myRef.push().getKey();
            ChatData data = new ChatData(id, msg, user.getMemberid());
            myRef.child(id).setValue(data);

            //API CALLING
            SharedPreferences sp = getApplicationContext().getSharedPreferences("Notification", Context.MODE_PRIVATE);
            final String switchstatus = sp.getString("switch", "on");
            if (TextUtils.equals(switchstatus, "on")) {
                fireabse_push_noti(receiverPlayerId);
            } else {

            }

            msgEt.setText("");
        });

    }

    public void fireabse_push_noti(String receiverPlayerId) {
        String url = getApplicationContext().getString(R.string.api) + "sendNotification";

        Log.d("FIREBASE TOKEN : ", receiverPlayerId);
        String msg = msgEt.getText().toString().trim();

        msgEt.setText("");
        JSONObject params = new JSONObject();

        try {
            params.put("title", getResources().getString(R.string.app_name) + " " + ludoID);
            params.put("message", msg);
            params.put("token", receiverPlayerId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("PARAMS", params.toString());
        final JsonObjectRequest request = new JsonObjectRequest(POST, url, params,
                response -> {
                    Log.d("RESPONCE DATA", response.toString());

                }, error -> {

            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                String errorString = new String(response.data);
                try {
                    JSONObject obj = new JSONObject(errorString);
                    String message = obj.getString("failure");
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {

                }
            }
        }) {

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
        request.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, 0));
        mQueue.add(request);
    }

    public void addToLL(List<ChatData> list) {

        ll.removeAllViews();

        for (int i = 0; i < list.size(); i++) {
            ChatData data = list.get(i);

            View view = getLayoutInflater().inflate(R.layout.chat_layout, null);

            TextView tv = view.findViewById(R.id.msgtv);
            ImageView senderiv = view.findViewById(R.id.sender_iv);
            ImageView receiveriv = view.findViewById(R.id.receiver_iv);
            CardView sendercv = view.findViewById(R.id.sender_iv_cv);
            CardView receivercv = view.findViewById(R.id.receiver_iv_cv);
            LinearLayout mainChatLl = view.findViewById(R.id.main_chat_ll);

            if (TextUtils.equals(data.getSenderId(), user.getMemberid())) {
                mainChatLl.setGravity(Gravity.RIGHT);
                receivercv.setVisibility(View.INVISIBLE);
                if (TextUtils.equals(senderImg, "") || TextUtils.equals(senderImg, "null")) {

                } else {
                    Picasso.get().load(senderImg).placeholder(R.drawable.battlemanialogo).fit().into(senderiv);
                }
            } else {
                mainChatLl.setGravity(Gravity.LEFT);

                sendercv.setVisibility(View.INVISIBLE);
                if (TextUtils.equals(receiverImg, "") || TextUtils.equals(receiverImg, "null")) {

                } else {
                    Picasso.get().load(receiverImg).placeholder(R.drawable.battlemanialogo).fit().into(receiveriv);
                }
            }

            tv.setText(data.getMsg());

            ll.addView(view);

            scrollview.post(() -> scrollview.fullScroll(ScrollView.FOCUS_DOWN));
        }

    }
}