package com.app.rewardsbattle.ui.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.ui.adapters.TabAdapter;
import com.app.rewardsbattle.ui.fragments.MyContestLudoFragment;
import com.app.rewardsbattle.ui.fragments.OnGoingLudoFragment;
import com.app.rewardsbattle.ui.fragments.ResultLudoFragment;
import com.app.rewardsbattle.utils.AnalyticsUtil;
import com.app.rewardsbattle.utils.LoadingDialog;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.app.rewardsbattle.utils.UserLocalStore;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LudoActivity extends AppCompatActivity {

    TextView createcontest, home, friends, refersh, ludotitle, follow;
    RequestQueue jQueue, mQueue, rQueue;
    String gameid, page;
    int n = 0;
    ImageView back;
    CurrentUser user;
    UserLocalStore userLocalStore;
    JsonObjectRequest request;
    LoadingDialog loadingDialog;
    LinearLayout notification;
    Context context;
    Resources resources;
    LinearLayout ludoactivitytool;
    LinearLayout noti, searchbtn, searchtab, closesearch;
    ImageView searchicon;
    EditText searchludogame;

    @Override
    protected void onResume() {
        super.onResume();

        loadingDialog = new LoadingDialog(this);

        context = LocaleHelper.setLocale(LudoActivity.this);
        resources = context.getResources();

        SharedPreferences searchsp = getApplicationContext().getSharedPreferences("ludosearch", Context.MODE_PRIVATE);
        SharedPreferences.Editor spedit = searchsp.edit();
        spedit.putString("searchdata", "***********");
        spedit.putString("btnstatus", "off");
        spedit.apply();

        back = findViewById(R.id.backinludo);
        back.setOnClickListener(v -> onBackPressed());
        ludoactivitytool = findViewById(R.id.ludoactivitytool);
        userLocalStore = new UserLocalStore(this);
        user = userLocalStore.getLoggedInUser();
        searchicon = findViewById(R.id.searchicon);
        mQueue = Volley.newRequestQueue(this);
        mQueue.getCache().clear();

        rQueue = Volley.newRequestQueue(this);
        rQueue.getCache().clear();
        searchludogame = findViewById(R.id.searchludogame);
        searchludogame.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchludogame.getText().toString().length() > 0) {
                    spedit.putString("searchdata", searchludogame.getText().toString());
                } else {
                    spedit.putString("searchdata", "***********");
                }
                spedit.apply();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        SharedPreferences sp = getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
        gameid = sp.getString("gameid", "");
        page = sp.getString("SHAREPAGE", "");

        followunfollowapi();

        notification = findViewById(R.id.notificationinludo);
        notification.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), LudoNotificationActivity.class)));
        follow = findViewById(R.id.follow);
        searchbtn = findViewById(R.id.searchbtn);
        searchtab = findViewById(R.id.searchtab);
        searchbtn.setOnClickListener(v -> {
            if (searchtab.getVisibility() == View.VISIBLE) {
                searchtab.setVisibility(View.GONE);
                searchicon.setImageResource(R.drawable.ic_baseline_search_24);
                searchludogame.setText("");
                spedit.putString("searchdata", "***********");
                spedit.putString("btnstatus", "on");
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchludogame.getWindowToken(), 0);
            } else {
                spedit.putString("searchdata", "***********");
                spedit.putString("btnstatus", "off");
                searchtab.setVisibility(View.VISIBLE);
                searchicon.setImageResource(R.drawable.ic_baseline_search_off_24);
                searchludogame.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchludogame, InputMethodManager.SHOW_IMPLICIT);
            }
            spedit.apply();
        });
        closesearch = findViewById(R.id.closesearch);
        closesearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchtab.setVisibility(View.GONE);
                searchludogame.setText("");
                searchicon.setImageResource(R.drawable.ic_baseline_search_24);
                spedit.putString("searchdata", "***********");
                spedit.putString("btnstatus", "off");
                spedit.apply();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchludogame.getWindowToken(), 0);
            }
        });
        ViewPager viewPager = findViewById(R.id.viewPagernew);
        TabLayout tabLayout = findViewById(R.id.tabLayoutnew);
        noti = findViewById(R.id.noti);
        TabAdapter adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new OnGoingLudoFragment(), getResources().getString(R.string.ongoingludo));
        adapter.addFragment(new MyContestLudoFragment(), getResources().getString(R.string.mycontest));
        adapter.addFragment(new ResultLudoFragment(), getResources().getString(R.string.resultsludo));
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabTextColors(Color.WHITE, getResources().getColor(R.color.newblack));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                n = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                n = tab.getPosition();
            }
        });

        try {
            Intent intent = getIntent();
            String N = intent.getStringExtra("N");
            if (N != null) {
                n = Integer.parseInt(N);
            }
        } catch (NumberFormatException e) {
            if (page.matches("SHARE")) {
                n = 1;
                SharedPreferences sp111 = context.getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor1111 = sp111.edit();
                editor1111.putString("SHAREPAGE", "FIRST");
                editor1111.apply();
            }
            e.printStackTrace();
        }

        viewPager.setCurrentItem(n);
        viewPager.setOffscreenPageLimit(3);
        ludotitle = findViewById(R.id.ludotitleid);
        home = findViewById(R.id.homeid);
        createcontest = findViewById(R.id.creatcontestid);
        friends = findViewById(R.id.friendid);
        refersh = findViewById(R.id.refershid);

        SharedPreferences sp1 = getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
        String gamename = sp1.getString("gametitle", "");

        ludotitle.setText(gamename);

        refersh.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
            Intent intent = getIntent();
            intent.putExtra("N", String.valueOf(n));
            startActivity(intent);
            overridePendingTransition(0, 0);
        });


        home.setOnClickListener(v -> {
            searchdisable();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchludogame.getWindowToken(), 0);
            Intent intent = new Intent(getApplicationContext(), LudoLeaderBoardActivity.class);
            startActivity(intent);
        });


        friends.setOnClickListener(view -> {
            searchdisable();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchludogame.getWindowToken(), 0);
            Intent intent = new Intent(getApplicationContext(), FriendsActivity.class);
            startActivity(intent);
        });

        noti.setOnClickListener(v -> {
            searchdisable();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchludogame.getWindowToken(), 0);
            final Dialog builder = new Dialog(LudoActivity.this);
            builder.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            builder.setContentView(R.layout.notification_alert_dialog);

            ImageView spincancel = builder.findViewById(R.id.spinnercancel1);
            TextView notititle = builder.findViewById(R.id.noti_title);
            @SuppressLint("UseSwitchCompatOrMaterialCode") Switch notitoggle = builder.findViewById(R.id.notitoggle);

            notitoggle.setChecked(follow.getText().toString().trim().matches("Follow"));
            notitoggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String rurl = resources.getString(R.string.api) + "follow_unfollow_game";
                String status;
                if (follow.getText().toString().trim().matches("Follow")) {
                    status = "0";
                } else {
                    status = "1";
                }

                HashMap<String, String> params = new HashMap<>();
                params.put("status", status);
                params.put("game_id", gameid);
                params.put("member_id", user.getMemberid());

                final JsonObjectRequest rrequest = new JsonObjectRequest(rurl, new JSONObject(params),
                        response -> {
                            //loadingDialog.dismiss();
                            try {
                                if (response.getString("status").matches("true")) {
                                    if (response.getString("message").matches("Game Follow Successfully")) {
                                        follow.setText("Follow");
                                    } else {
                                        follow.setText("Unfollow");
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }, error -> Log.e("**VolleyError", "error" + error.getMessage())) {

                    @Override
                    public Map<String, String> getHeaders() {

                        Map<String, String> headers = new HashMap<>();
                        CurrentUser user = userLocalStore.getLoggedInUser();

                        String credentials = user.getUsername() + ":" + user.getPassword();
                        String auth = "Basic "
                                + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                        Log.d("token", user.token);
                        String token = "Bearer " + user.getToken();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", token);
                        headers.put("x-localization", LocaleHelper.getPersist(context));

                        return headers;
                    }
                };

                rrequest.setShouldCache(false);
                rQueue.add(rrequest);
            });
            notititle.setText("Remind me for Lineup\nAnnouncement");
            spincancel.setOnClickListener(v12 -> builder.dismiss());
            builder.show();
        });

        follow.setOnClickListener(v -> {
            //loadingDialog.show();
            searchdisable();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchludogame.getWindowToken(), 0);
            String rurl = resources.getString(R.string.api) + "follow_unfollow_game";
            String status;
            if (follow.getText().toString().trim().matches("Follow")) {
                status = "0";
            } else {
                status = "1";
            }

            HashMap<String, String> params = new HashMap<String, String>();
            params.put("status", status);
            params.put("game_id", gameid);
            params.put("member_id", user.getMemberid());

            final JsonObjectRequest rrequest = new JsonObjectRequest(rurl, new JSONObject(params),
                    response -> {
                        //loadingDialog.dismiss();
                        try {
                            if (response.getString("status").matches("true")) {
                                if (response.getString("message").matches("Game Follow Successfully")) {
                                    follow.setText("Follow");
                                } else {
                                    follow.setText("Unfollow");
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }, error -> Log.e("**VolleyError", "error" + error.getMessage())) {

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    CurrentUser user = userLocalStore.getLoggedInUser();
                    Log.d("token", user.token);
                    String token = "Bearer " + user.getToken();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", token);
                    headers.put("x-localization", LocaleHelper.getPersist(context));
                    return headers;
                }
            };

            rrequest.setShouldCache(false);
            rQueue.add(rrequest);
        });

        createcontest.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v1) {
                searchdisable();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchludogame.getWindowToken(), 0);
                final Dialog builder = new Dialog(LudoActivity.this);
                builder.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                builder.setContentView(R.layout.create_challenge);
                final EditText newplayernameuser = builder.findViewById(R.id.usernameid);
                SharedPreferences sp = getSharedPreferences("LUDO", Context.MODE_PRIVATE);
                newplayernameuser.setText(sp.getString("ludoname", ""));
                final EditText newplayernamecoin = builder.findViewById(R.id.coinsid);
                TextInputLayout pnamehintusername = builder.findViewById(R.id.textinputlayoutforaddinfousername);
                TextInputLayout pnamehintcoin = builder.findViewById(R.id.textinputlayoutforaddinfocoin);
                pnamehintusername.setHint(gamename + " " + getResources().getString(R.string.Username));
                pnamehintcoin.setHint("Amount");
                newplayernameuser.setCompoundDrawablesWithIntrinsicBounds(R.drawable.gameicongreen, 0, 0, 0);
                newplayernamecoin.setCompoundDrawablesWithIntrinsicBounds(R.drawable.resize_coin, 0, 0, 0);
                Button newcancel = builder.findViewById(R.id.newcancel1);
                RadioGroup pwradiogroup_ed = builder.findViewById(R.id.pwradiogroup_ed);
                ImageView privateinfo = builder.findViewById(R.id.privateinfo);
                privateinfo.setOnClickListener(v -> {
                    final Dialog builderinfo = new Dialog(LudoActivity.this);
                    builderinfo.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    builderinfo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    builderinfo.setContentView(R.layout.private_alert_dialog);
                    ImageView cancelbtn = builderinfo.findViewById(R.id.cancelbtn);
                    cancelbtn.setOnClickListener(v2 -> {
                        builderinfo.dismiss();
                    });
                    builderinfo.show();
                });
                RadioButton pw1 = builder.findViewById(R.id.pw1);
                RadioButton pw2 = builder.findViewById(R.id.pw2);
                TextInputLayout passwordtextinputlayout = builder.findViewById(R.id.passwordtextinputlayout);
                EditText passwordid = builder.findViewById(R.id.passwordid);
                TextView maintitle1 = builder.findViewById(R.id.main_title);

                pw2.setChecked(true);

                pwradiogroup_ed.setOnCheckedChangeListener((group, checkedId) -> {
                    if (checkedId == R.id.pw2) {
                        passwordtextinputlayout.setVisibility(View.GONE);
                    } else {
                        passwordtextinputlayout.setVisibility(View.VISIBLE);
                    }
                });

                newcancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int rdbtn = 1;

                        if (passwordtextinputlayout.getVisibility() == View.VISIBLE) {
                            rdbtn = 1;
                        } else {
                            rdbtn = 0;
                        }

                        String cludouser = newplayernameuser.getText().toString().trim();
                        String ccoin = newplayernamecoin.getText().toString().trim();
                        String password = passwordid.getText().toString().trim();

                        String regex = "[a-zA-Z0-9]+";
                        Pattern pattern = Pattern.compile(regex);

                        if (TextUtils.isEmpty(cludouser)) {
                            newplayernameuser.setError("Required Field");
                            return;
                        }

                        if (!pattern.matcher(cludouser).matches()) {
                            newplayernameuser.setError("Invalid format");
                            return;
                        }

                        if (TextUtils.isEmpty(ccoin)) {
                            newplayernamecoin.setError("Required Field");
                            return;
                        }

                        if (Integer.parseInt(ccoin) < 10) {
                            newplayernamecoin.setError("Minimum 10 coin");
                            return;
                        }

                        if (Integer.parseInt(ccoin) % 10 != 0) {
                            newplayernamecoin.setError("Contest amount should be multiple of 10,like 10 20 50 100");
                            return;
                        }

                        if (String.valueOf(rdbtn).matches("1")) {
                            if (password.length() < 6) {
                                passwordid.setError("Password is too short");
                                return;
                            }

                            if (!pattern.matcher(password).matches()) {
                                passwordid.setError("Invalid format");
                                return;
                            }
                        }

                        loadingDialog.show();

                        SharedPreferences sp = getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
                        String gameid = sp.getString("gameid", "");

                        jQueue = Volley.newRequestQueue(getApplicationContext());

                        String jurl = getResources().getString(R.string.api) + "add_challenge";

                        final UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("member_id", user.getMemberid());
                            jsonObject.put("ludo_king_username", cludouser);
                            jsonObject.put("coin", ccoin);
                            jsonObject.put("submit", "addChallenge");
                            jsonObject.put("game_id", gameid);
                            jsonObject.put("with_password", String.valueOf(rdbtn));
                            try {
                                if (String.valueOf(rdbtn).equals("0")) {
                                    jsonObject.put("challenge_password", "");
                                } else {
                                    jsonObject.put("challenge_password", passwordid.getText().toString());
                                }
                            } catch (Exception e) {
                                if (String.valueOf(rdbtn).equals("0")) {
                                    jsonObject.put("challenge_password", "");
                                } else {
                                    jsonObject.put("challenge_password", passwordid.getText().toString());
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.e(jurl, jsonObject.toString());
                        final JsonObjectRequest jrequest = new JsonObjectRequest(jurl, jsonObject,
                                response -> {

                                    Log.d("nk", response.toString());

                                    try {
                                        loadingDialog.dismiss();
                                        if (TextUtils.equals(response.getString("status"), "true")) {
                                            builder.dismiss();
                                            passwordid.setText("");
                                            final Dialog bbuilder = new Dialog(LudoActivity.this);
                                            bbuilder.setContentView(R.layout.create_challenge);
                                            TextView maintitle = bbuilder.findViewById(R.id.main_title);
                                            maintitle.setText("Wow\nContest create successfully");
                                            //maintitle.setText(response.getString("message"));
                                            TextView rules = bbuilder.findViewById(R.id.rulesid);
                                            rules.setText("1. You can see your contest in my contest section\n2. Now wait for someone who accept your contest.\n3. After that create room in " + gamename + "  app  and update it.\n4. When your match complete take winning screen shot and upload.");
                                            final EditText newplayername = bbuilder.findViewById(R.id.usernameid);
                                            final EditText newplayername1 = bbuilder.findViewById(R.id.coinsid);
                                            TextInputLayout pnamehintusername1 = bbuilder.findViewById(R.id.textinputlayoutforaddinfousername);
                                            TextInputLayout pnamehintcoin1 = bbuilder.findViewById(R.id.textinputlayoutforaddinfocoin);
                                            RadioGroup pwradiogroup = bbuilder.findViewById(R.id.pwradiogroup_ed);
                                            TextInputLayout passwordtext = bbuilder.findViewById(R.id.passwordtextinputlayout);
                                            ImageView privateinfo1 = bbuilder.findViewById(R.id.privateinfo);
                                            newplayername.setVisibility(View.GONE);
                                            pnamehintusername1.setVisibility(View.GONE);
                                            newplayername1.setVisibility(View.GONE);
                                            pnamehintcoin1.setVisibility(View.GONE);
                                            pwradiogroup.setVisibility(View.GONE);
                                            passwordtext.setVisibility(View.GONE);
                                            privateinfo1.setVisibility(View.GONE);
                                            newplayername.setCompoundDrawablesWithIntrinsicBounds(R.drawable.gameicongreen, 0, 0, 0);
                                            LinearLayout add_player_ll1 = bbuilder.findViewById(R.id.create_challage_ll);
                                            Button newcancel1 = bbuilder.findViewById(R.id.newcancel1);
                                            newcancel1.setText("OK");
                                            newcancel1.setOnClickListener(view1 -> {
                                                bbuilder.dismiss();
                                                finish();
                                                overridePendingTransition(0, 0);
                                                Intent intent = getIntent();
                                                intent.putExtra("N", String.valueOf(1));
                                                startActivity(intent);
                                                overridePendingTransition(0, 0);
                                            });
                                            bbuilder.create();
                                            bbuilder.show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
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
                                String credentials = user.getUsername() + ":" + user.getPassword();
                                String auth = "Basic "
                                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                                String token = "Bearer " + user.getToken();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", token);
                                return headers;
                            }
                        };
                        jQueue.add(jrequest);

                    }
                });
                builder.create();
                builder.show();
            }
            ////////////////////////////////////////////dialog end////////////////////////////////////////////////////////////
        });

    }

    public void searchdisable() {
        SharedPreferences searchsp = getApplicationContext().getSharedPreferences("ludosearch", Context.MODE_PRIVATE);
        SharedPreferences.Editor sp = searchsp.edit();
        searchtab.setVisibility(View.GONE);
        searchludogame.setText("");
        searchicon.setImageResource(R.drawable.ic_baseline_search_24);
        sp.putString("searchdata", "***********");
        sp.putString("btnstatus", "off");
        sp.apply();
    }

    public void followunfollowapi() {
        loadingDialog.show();
        SharedPreferences sp = getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
        String gameid = sp.getString("gameid", "");
        CurrentUser user = userLocalStore.getLoggedInUser();

        String url = resources.getString(R.string.api) + "get_game_follow_status/" + gameid + "/" + user.memberId;
        request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    loadingDialog.dismiss();
                    try {
                        if (TextUtils.equals(response.getString("status"), "true")) {

                            if (TextUtils.equals(response.getString("is_follower"), "true")) {
                                follow.setText("Follow");
                            } else {
                                follow.setText("Unfollow");
                            }
                        } else {

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            Log.e("**VolleyError", "error" + error.getMessage());

            if (error instanceof TimeoutError) {

                request.setShouldCache(false);
                mQueue.add(request);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return super.getParams();
            }

            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> headers = new HashMap<>();
                userLocalStore = new UserLocalStore(getApplicationContext());
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_ludo);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
        intent.putExtra("N", "1");
        startActivity(intent);
    }
}