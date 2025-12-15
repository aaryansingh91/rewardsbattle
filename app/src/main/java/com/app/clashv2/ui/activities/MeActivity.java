package com.app.clashv2.ui.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.clashv2.R;
import com.app.clashv2.models.CurrentUser;
import com.app.clashv2.utils.LoadingDialog;
import com.app.clashv2.utils.LocaleHelper;
import com.app.clashv2.utils.UserLocalStore;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MeActivity extends AppCompatActivity {

    TextView usernametitle;
    TextView balancetitle;
    TextView matchestitle;
    TextView playedtitle;
    TextView totaltitle;
    TextView killedtitle;
    TextView amounttitle;
    TextView wontitle;
    TextView usernametitleid;
    TextView myWallet;
    TextView myProfile;
    TextView aboutUs;
    TextView customerSupport;
    TextView logOut;
    TextView shareApp;
    TextView myStatistics;
//    TextView topPlayer;
    TextView matchesPlayed;
    TextView totalKilled;
    TextView amountWon;
    TextView appVersion;
    TextView appTutorial;
    TextView myReff;
    TextView myRewards;
    TextView leaderboard;
    TextView termAndCondition;
    TextView chooselangvage;
    TextView mymatches;
    TextView myOrder;
    TextView announcetv;
    LinearLayout staticResult;
    RequestQueue mQueue, vQueue;
    UserLocalStore userLocalStore;
    LoadingDialog loadingDialog;
    String userNameforlogout = "";
    SharedPreferences sp;
    String shareBody = "";
    Switch notification;
    TextView pushText;
    Context context;
    Resources resources;
    CurrentUser user;
    RequestQueue uQueue;
    String profile_image;
    ImageView profileImage;
    ImageView backfromprofile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        context = LocaleHelper.setLocale(MeActivity.this);
        resources = context.getResources();

        userLocalStore = new UserLocalStore(context);
        user = userLocalStore.getLoggedInUser();

        loadingDialog = new LoadingDialog(context);
        sp = this.getSharedPreferences("tabinfo", Context.MODE_PRIVATE);
        String selectedtab = sp.getString("selectedtab", "");
        if (TextUtils.equals(selectedtab, "2")) {
            loadingDialog.show();
        }

        usernametitle = findViewById(R.id.usernametitleid);
        balancetitle = findViewById(R.id.balancetitleid);
        matchestitle = findViewById(R.id.matcestitleid);
        playedtitle = findViewById(R.id.playedtitleid);
        totaltitle = findViewById(R.id.totaltitleid);
        killedtitle = findViewById(R.id.killdtitleid);
        amounttitle = findViewById(R.id.amounttitleid);
        wontitle = findViewById(R.id.wontitleid);

        usernametitleid = findViewById(R.id.usernametitleid);
        myWallet = findViewById(R.id.mywallet);
        myProfile = findViewById(R.id.myprofile);
        aboutUs = findViewById(R.id.aboutus);
        customerSupport = findViewById(R.id.customersupport);
        logOut = findViewById(R.id.logout);
        appTutorial = findViewById(R.id.howto);
        shareApp = findViewById(R.id.shareapp);
        myStatistics = findViewById(R.id.mystatisics);
//        topPlayer = findViewById(R.id.topplayer);
        myReff = findViewById(R.id.myreff);
        myRewards = findViewById(R.id.myreward);
        leaderboard = findViewById(R.id.leaderboard);
        termAndCondition = findViewById(R.id.tandc);
        chooselangvage = findViewById(R.id.langid);
        staticResult = findViewById(R.id.staticsresult);
        matchesPlayed = findViewById(R.id.matchesplayed);
        totalKilled = findViewById(R.id.totalkilled);
        amountWon = findViewById(R.id.amountwon);
        appVersion = findViewById(R.id.appversion);
        mymatches = findViewById(R.id.mymatch);
        myOrder = findViewById(R.id.myorder);
        announcetv = findViewById(R.id.announcetv);
        pushText = findViewById(R.id.pushtext);
        notification = findViewById(R.id.notification);

        profileImage = findViewById(R.id.test);
        backfromprofile = findViewById(R.id.backfromprofile);


        amounttitle.setText(resources.getString(R.string.Amount));
        wontitle.setText(resources.getString(R.string.won));
        playedtitle.setText(resources.getString(R.string.played));
        killedtitle.setText(resources.getString(R.string.killed));
        totaltitle.setText(resources.getString(R.string.total));
        matchestitle.setText(resources.getString(R.string.matches));

        myProfile.setText(resources.getString(R.string.my_profile));
        aboutUs.setText(resources.getString(R.string.about_us));
        customerSupport.setText(resources.getString(R.string.customer_support));
        logOut.setText(resources.getString(R.string.logout));
        appTutorial.setText(resources.getString(R.string.app_tutorial));
        shareApp.setText(resources.getString(R.string.share_app));
        myStatistics.setText(resources.getString(R.string.my_statistics));
        myWallet.setText(resources.getString(R.string.my_wallet));
//        topPlayer.setText(resources.getString(R.string.top_players));
        myReff.setText(resources.getString(R.string.my_referrals));
        myRewards.setText(resources.getString(R.string.my_reward));
        leaderboard.setText(resources.getString(R.string.leaderboard));
        termAndCondition.setText(resources.getString(R.string.terms_and_condition));
        chooselangvage.setText(resources.getString(R.string.change_language));
        mymatches.setText(resources.getString(R.string.my_matches));
        myOrder.setText(resources.getString(R.string.my_order));
        announcetv.setText(resources.getString(R.string.Announcement));
        pushText.setText(resources.getString(R.string.push_notification));
        myProfile.setText(resources.getString(R.string.my_profile));

        uQueue = Volley.newRequestQueue(this);
        uQueue.getCache().clear();

        SharedPreferences sp = this.getSharedPreferences("Notification", Context.MODE_PRIVATE);
        final String switchstatus = sp.getString("switch", "on");
        if (TextUtils.equals(switchstatus, "on")) {
            notification.setChecked(true);
        } else {
            notification.setChecked(false);
        }


        announcetv.setOnClickListener(view -> startActivity(new Intent(this, AnnouncementActivity.class)));

        notification.setOnCheckedChangeListener((buttonView, isChecked) -> {

            SharedPreferences sp12 = this.getSharedPreferences("Notification", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp12.edit();
            // Toast.makeText(context, String.valueOf(isChecked), Toast.LENGTH_SHORT).show();
            String notistatus;
            if (isChecked) {
                editor.putString("switch", "on");
                notistatus = "1";
            } else {
                notistatus = "0";
                editor.putString("switch", "off");
            }
            String uurl = resources.getString(R.string.api) + "update_myprofile";
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("member_id", user.getMemberid());
            params.put("push_noti", notistatus);
            params.put("submit", "submit_push_noti");

            Log.d("save", new JSONObject(params).toString());

            final JsonObjectRequest urequest = new JsonObjectRequest(uurl, new JSONObject(params),
                    response -> {
                        //loadingDialog.dismiss();
                        try {
                            if (response.getString("status").matches("true")) {

                            } else {
                                //Toast.makeText(MyProfileActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, error -> Log.e("**VolleyError", "error" + error.getMessage())) {
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
            urequest.setShouldCache(false);
            uQueue.add(urequest);
            editor.apply();
        });

        userLocalStore = new UserLocalStore(context);
        final CurrentUser user = userLocalStore.getLoggedInUser();
        userNameforlogout = user.getUsername();

        //dashboard api call start
        dashBoard();
        //dashboard api call end

        mymatches.setOnClickListener(view -> {
            Intent intent = new Intent(this, SelectedGameActivity.class);
            SharedPreferences sp1 = this.getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp1.edit();
            editor.putString("gametitle", resources.getString(R.string.my_matches));
            editor.putString("gameid", "not");
            editor.apply();
            startActivity(intent);
        });

        myOrder.setOnClickListener(view -> {
            Intent intent = new Intent(this, MyOrderActivity.class);
            startActivity(intent);
        });

        staticResult.setOnClickListener(view -> startActivity(new Intent(this, MyStatisticsActivity.class)));

        myWallet.setOnClickListener(view -> startActivity(new Intent(this, MyWalletActivity.class)));

        myProfile.setOnClickListener(view -> startActivity(new Intent(this, MyProfileActivity.class)));

        backfromprofile.setOnClickListener(view -> {
            finish();
        });

        shareApp.setOnClickListener(view -> {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody + resources.getString(R.string.referral_code) + " : " + user.getUsername());
            startActivity(Intent.createChooser(sharingIntent, resources.getString(R.string.share_using)));
        });

        myStatistics.setOnClickListener(view -> startActivity(new Intent(this, MyStatisticsActivity.class)));

//        topPlayer.setOnClickListener(view -> startActivity(new Intent(context, TopPlayerActivity.class)));

        aboutUs.setOnClickListener(view -> startActivity(new Intent(context, AboutUsActivity.class)));

        customerSupport.setOnClickListener(view -> startActivity(new Intent(context, CustomerSupportActivity.class)));

        myReff.setOnClickListener(v -> startActivity(new Intent(this, MyReferralsActivity.class)));
        myRewards.setOnClickListener(v -> startActivity(new Intent(this, MyRewardedActivity.class)));

        leaderboard.setOnClickListener(v -> startActivity(new Intent(this, LeaderboardActivity.class)));

        termAndCondition.setOnClickListener(v -> startActivity(new Intent(this, TermsAndConditionActivity.class)));

        chooselangvage.setOnClickListener(v -> startActivity(new Intent(this, ChooseLanguageActivity.class)));

        logOut.setOnClickListener(view -> logoutall());

        appTutorial.setOnClickListener(view -> startActivity(new Intent(this, HowtoActivity.class)));

        /* version api call start*/

        vQueue = Volley.newRequestQueue(context);
        vQueue.getCache().clear();
        String vurl = resources.getString(R.string.api) + "version/android";
        JsonObjectRequest vrequest = new JsonObjectRequest(Request.Method.GET, vurl, null, response -> {
            try {
                appVersion.setText(resources.getString(R.string.version) + " : " + response.getString("version"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.e("error", error.toString() + "*******************************************" + LocaleHelper.getPersist(context))) {
            @Override
            protected Map<String, String> getParams() throws
                    AuthFailureError {
                return super.getParams();
            }

            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> headers = new HashMap<>();


                headers.put("x-localization", LocaleHelper.getPersist(context));
                return headers;
            }
        };

        vrequest.setShouldCache(false);
        vQueue.add(vrequest);
        /*version api call end*/
    }

    public void dashBoard() {
        mQueue = Volley.newRequestQueue(this);
        mQueue.getCache().clear();

        String url = resources.getString(R.string.api) + "dashboard/" + user.getMemberid();

        @SuppressLint("DefaultLocale") final JsonObjectRequest request = new JsonObjectRequest(url, null,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response.getString("web_config"));
                        shareBody = obj.getString("share_description");
                        SharedPreferences sp111 = context.getSharedPreferences("sharedesc", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp111.edit();
                        editor.putString("sharedescription", shareBody);
                        editor.apply();

                        JSONObject memobj = new JSONObject(response.getString("member"));

                        String winMoney = memobj.getString("wallet_balance");
                        String joinMoney = memobj.getString("join_money");

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

                        usernametitleid.setText(memobj.getString("user_name"));
                        profile_image = memobj.getString("profile_image");
                        if (!profile_image.equals("null") && !profile_image.isEmpty()) {
                            Picasso.get().load(Uri.parse(profile_image)).placeholder(R.drawable.logo_space).fit().into(profileImage);
                        }

                        JSONObject totplayobj = new JSONObject(response.getString("tot_match_play"));
                        if (TextUtils.equals(totplayobj.getString("total_match"), "null")) {
                            matchesPlayed.setText("0");
                        } else {
                            matchesPlayed.setText(totplayobj.getString("total_match"));
                        }
                        JSONObject totkillobj = new JSONObject(response.getString("tot_kill"));

                        if (TextUtils.equals(totkillobj.getString("total_kill"), "null")) {
                            totalKilled.setText("0");
                        } else {
                            totalKilled.setText(totkillobj.getString("total_kill"));
                        }

                        JSONObject totwinobj = new JSONObject(response.getString("tot_win"));

                        if (TextUtils.equals(totwinobj.getString("total_win"), "null")) {
                            amountWon.setText("0");
                        } else {
                            if (isDouble(totwinobj.getString("total_win"))) {
                                amountWon.setText(String.format("%.2f", Double.parseDouble(totwinobj.getString("total_win"))));
                            } else {
                                amountWon.setText(totwinobj.getString("total_win"));
                            }
                        }
                    } catch (JSONException e) {
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

    boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void logoutall() {

        SharedPreferences sp = getSharedPreferences("tabitem", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor1111 = sp.edit();
        editor1111.putString("TAB", "0");
        editor1111.apply();

        userLocalStore.clearUserData();
        FirebaseAuth.getInstance().signOut();
        FirebaseMessaging.getInstance().deleteToken();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignIn.getClient(this, gso).signOut();

        Toast.makeText(this, resources.getString(R.string.log_out_successfully), Toast.LENGTH_SHORT).show();

        startActivity(new Intent(this, MainActivity.class));
    }
    
}