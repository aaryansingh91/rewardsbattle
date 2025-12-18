//For Me tab at home page
package com.app.rewardsbattle.ui.fragments;


import static android.content.Context.RECEIVER_NOT_EXPORTED;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.ui.activities.AboutUsActivity;
import com.app.rewardsbattle.ui.activities.AnnouncementActivity;
import com.app.rewardsbattle.ui.activities.ChooseLanguageActivity;
import com.app.rewardsbattle.ui.activities.CustomerSupportActivity;
import com.app.rewardsbattle.ui.activities.HowtoActivity;
import com.app.rewardsbattle.ui.activities.LeaderboardActivity;
import com.app.rewardsbattle.ui.activities.MainActivity;
import com.app.rewardsbattle.ui.activities.MeActivity;
import com.app.rewardsbattle.ui.activities.MyOrderActivity;
import com.app.rewardsbattle.ui.activities.MyProfileActivity;
import com.app.rewardsbattle.ui.activities.MyReferralsActivity;
import com.app.rewardsbattle.ui.activities.MyRewardedActivity;
import com.app.rewardsbattle.ui.activities.MyStatisticsActivity;
import com.app.rewardsbattle.ui.activities.MyWalletActivity;
import com.app.rewardsbattle.ui.activities.SelectedGameActivity;
import com.app.rewardsbattle.ui.activities.TermsAndConditionActivity;
import com.app.rewardsbattle.utils.LoadingDialog;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.app.rewardsbattle.utils.UserLocalStore;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MeFragment extends Fragment {

    TextView usernametitle;
    TextView balancetitle;
    TextView matchestitle;
    TextView playedtitle;
    TextView totaltitle;
    TextView killedtitle;
    TextView amounttitle;
    TextView wontitle;
    TextView userName;
    String fullName;
    String fullEmail;
    TextView usernametitleid;
    TextView playCoin;
    TextView myWallet;
    TextView myProfile;
    TextView aboutUs;
    TextView customerSupport;
    TextView logOut;
    TextView shareApp;
    TextView myStatistics;
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
    ImageView visit_web, visit_support;
    CircleImageView profile;
    LinearLayout visit_wallet;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        context = LocaleHelper.setLocale(getContext());
        resources = context.getResources();

        userLocalStore = new UserLocalStore(requireActivity());
        user = userLocalStore.getLoggedInUser();

        loadingDialog = new LoadingDialog(getContext());
        sp = getActivity().getSharedPreferences("tabinfo", Context.MODE_PRIVATE);
        String selectedtab = sp.getString("selectedtab", "");
        if (TextUtils.equals(selectedtab, "2")) {
            loadingDialog.show();
        }
        View root = inflater.inflate(R.layout.me_home, container, false);

        usernametitle = root.findViewById(R.id.usernametitleid);
        balancetitle = root.findViewById(R.id.balancetitleid);
        matchestitle = root.findViewById(R.id.matcestitleid);
        playedtitle = root.findViewById(R.id.playedtitleid);
        totaltitle = root.findViewById(R.id.totaltitleid);
        killedtitle = root.findViewById(R.id.killdtitleid);
        amounttitle = root.findViewById(R.id.amounttitleid);
        wontitle = root.findViewById(R.id.wontitleid);

        userName = root.findViewById(R.id.username);
        usernametitleid = root.findViewById(R.id.usernametitleid);
        playCoin = root.findViewById(R.id.playcoin);
        myWallet = root.findViewById(R.id.mywallet);
        myProfile = root.findViewById(R.id.myprofile);
        aboutUs = root.findViewById(R.id.aboutus);
        customerSupport = root.findViewById(R.id.customersupport);
        logOut = root.findViewById(R.id.logout);
        appTutorial = root.findViewById(R.id.howto);
        shareApp = root.findViewById(R.id.shareapp);
        myStatistics = root.findViewById(R.id.mystatisics);
        myReff = root.findViewById(R.id.myreff);
        myRewards = root.findViewById(R.id.myreward);
        leaderboard = root.findViewById(R.id.leaderboard);
        termAndCondition = root.findViewById(R.id.tandc);
        chooselangvage = root.findViewById(R.id.langid);
        staticResult = root.findViewById(R.id.staticsresult);
        matchesPlayed = root.findViewById(R.id.matchesplayed);
        totalKilled = root.findViewById(R.id.totalkilled);
        amountWon = root.findViewById(R.id.amountwon);
        appVersion = root.findViewById(R.id.appversion);
        mymatches = root.findViewById(R.id.mymatch);
        myOrder = root.findViewById(R.id.myorder);
        announcetv = root.findViewById(R.id.announcetv);
        pushText = root.findViewById(R.id.pushtext);
        notification = root.findViewById(R.id.notification);

        profileImage = root.findViewById(R.id.test);

        visit_web = root.findViewById(R.id.visit_web);
        visit_support = root.findViewById(R.id.visit_support);
        profile = root.findViewById(R.id.profile_image);
        visit_wallet = root.findViewById(R.id.visit_wallet);

        visit_web.setOnClickListener(view -> {
            String url = resources.getString(R.string.url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));

            try {
                startActivity(intent);
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(getActivity(), "No browser app found to open the link", Toast.LENGTH_SHORT).show();
            }
        });

        visit_support.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), CustomerSupportActivity.class);
            startActivity(intent);
        });

        profile.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), MeActivity.class);
            startActivity(intent);
        });

        visit_wallet.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MyWalletActivity.class);
            intent.putExtra("FROM", "PLAY");
            startActivity(intent);
        });

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

        uQueue = Volley.newRequestQueue(getActivity());
        uQueue.getCache().clear();

        SharedPreferences sp = getActivity().getSharedPreferences("Notification", Context.MODE_PRIVATE);
        final String switchstatus = sp.getString("switch", "on");
        if (TextUtils.equals(switchstatus, "on")) {
            notification.setChecked(true);
        } else {
            notification.setChecked(false);
        }


        announcetv.setOnClickListener(view -> startActivity(new Intent(getActivity(), AnnouncementActivity.class)));

        notification.setOnCheckedChangeListener((buttonView, isChecked) -> {

            SharedPreferences sp12 = getActivity().getSharedPreferences("Notification", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp12.edit();
            // Toast.makeText(getContext(), String.valueOf(isChecked), Toast.LENGTH_SHORT).show();
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

        userLocalStore = new UserLocalStore(getContext());
        final CurrentUser user = userLocalStore.getLoggedInUser();
        userNameforlogout = user.getUsername();

        IntentFilter intentFilter = new IntentFilter("reloadprofile");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().registerReceiver(broadcastReceiver, intentFilter, RECEIVER_NOT_EXPORTED);
        }

        //dashboard api call start
        dashBoard();
        //dashboard api call end

        mymatches.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), SelectedGameActivity.class);
            SharedPreferences sp1 = getActivity().getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp1.edit();
            editor.putString("gametitle", resources.getString(R.string.my_matches));
            editor.putString("gameid", "not");
            editor.apply();
            startActivity(intent);
        });

        myOrder.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), MyOrderActivity.class);
            startActivity(intent);
        });
        staticResult.setOnClickListener(view -> startActivity(new Intent(getActivity(), MyStatisticsActivity.class)));

        myWallet.setOnClickListener(view -> startActivity(new Intent(getActivity(), MyWalletActivity.class)));

        myProfile.setOnClickListener(view -> startActivity(new Intent(getActivity(), MyProfileActivity.class)));

        shareApp.setOnClickListener(view -> {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody + resources.getString(R.string.referral_code) + " : " + user.getUsername());
            startActivity(Intent.createChooser(sharingIntent, resources.getString(R.string.share_using)));
        });

        myStatistics.setOnClickListener(view -> startActivity(new Intent(getActivity(), MyStatisticsActivity.class)));

        aboutUs.setOnClickListener(view -> startActivity(new Intent(getContext(), AboutUsActivity.class)));

        customerSupport.setOnClickListener(view -> startActivity(new Intent(getContext(), CustomerSupportActivity.class)));

        myReff.setOnClickListener(v -> startActivity(new Intent(getActivity(), MyReferralsActivity.class)));
        myRewards.setOnClickListener(v -> startActivity(new Intent(getActivity(), MyRewardedActivity.class)));

        leaderboard.setOnClickListener(v -> startActivity(new Intent(getActivity(), LeaderboardActivity.class)));

        termAndCondition.setOnClickListener(v -> startActivity(new Intent(getActivity(), TermsAndConditionActivity.class)));

        chooselangvage.setOnClickListener(v -> startActivity(new Intent(getActivity(), ChooseLanguageActivity.class)));

        logOut.setOnClickListener(view -> logoutall());

        appTutorial.setOnClickListener(view -> startActivity(new Intent(getActivity(), HowtoActivity.class)));

        /* version api call start*/

        vQueue = Volley.newRequestQueue(getContext());
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

        return root;
    }

    public void dashBoard() {
        mQueue = Volley.newRequestQueue(getContext());
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

                        userName.setText(memobj.getString("user_name"));

                        fullName = memobj.getString("user_name");
                        fullEmail = memobj.getString("email_id");

                        usernametitleid.setText(memobj.getString("user_name"));
                        profile_image = memobj.getString("profile_image");
                        if (!profile_image.equals("null") && !profile_image.isEmpty()) {
                            Picasso.get().load(Uri.parse(profile_image)).placeholder(R.drawable.logo_space).fit().into(profileImage);
                        }
                        playCoin.setText(totalMoney);

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

        SharedPreferences sp = getActivity().getSharedPreferences("tabitem", Context.MODE_PRIVATE);
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

        GoogleSignIn.getClient(getContext(), gso).signOut();

        Toast.makeText(getActivity(), resources.getString(R.string.log_out_successfully), Toast.LENGTH_SHORT).show();

        startActivity(new Intent(getActivity(), MainActivity.class));
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && Objects.equals(intent.getAction(), "reloadprofile")) {
                dashBoard();
            }
        }
    };
}
