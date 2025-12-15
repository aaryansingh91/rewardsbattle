//For description about selected match or tournament
package com.app.clashv2.ui.activities;

import static java.lang.Integer.parseInt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.clashv2.R;
import com.app.clashv2.models.CurrentUser;
import com.app.clashv2.ui.adapters.TabAdapter;
import com.app.clashv2.ui.fragments.FragmentSelectedTournamentDescription;
import com.app.clashv2.ui.fragments.FragmentSelectedTournamentJoinedeMember;
import com.app.clashv2.utils.AnalyticsUtil;
import com.app.clashv2.utils.LoadingDialog;
import com.app.clashv2.utils.LocaleHelper;
import com.app.clashv2.utils.UserLocalStore;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SelectedTournamentActivity extends AppCompatActivity {

    TextView matchTitleBar;
    ImageView back;
    Button joinNow;
    LoadingDialog loadingDialog;
    CardView imageViewSelectedCardview;
    ImageView imgeViewSelected;
    String mId, matchName, matchTime, winPrize, perKill, entryFee, type, MAP, matchType, matchDesc, noOfPlayer, numberOfPosition, memberId, matchUrl, roomId, roomPassword, matchprivateDesc;
    String joinStatus = null;
    String gameName = "";
    String packagename = "";
    RequestQueue mQueue;

    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_selected_tournament);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        context = LocaleHelper.setLocale(SelectedTournamentActivity.this);
        resources = context.getResources();

        ViewPager viewPager = findViewById(R.id.viewPagernewinselectedtournament);
        TabLayout tabLayout = findViewById(R.id.tabLayoutnewinselectedtournament);
        TabAdapter adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentSelectedTournamentDescription(), resources.getString(R.string.description));
        adapter.addFragment(new FragmentSelectedTournamentJoinedeMember(), resources.getString(R.string.joined_member));

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabTextColors(Color.WHITE, getResources().getColor(R.color.orange));

        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();

        back = findViewById(R.id.backfromselectedmatch);

        joinNow = findViewById(R.id.joinnow);
        matchTitleBar = findViewById(R.id.matchtitlebar);

        back.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), SelectedGameActivity.class);
            startActivity(intent);
        });

        Intent intent = getIntent();
        final String mid = intent.getStringExtra("M_ID");
        final String from = intent.getStringExtra("FROM");
        String baner = intent.getStringExtra("BANER");
        gameName = intent.getStringExtra("GAME_NAME");

        imageViewSelectedCardview = findViewById(R.id.imageviewselectedcardview);
        imgeViewSelected = findViewById(R.id.imageviewselected);

        if (!TextUtils.equals(baner, "")) {
            imageViewSelectedCardview.setVisibility(View.VISIBLE);
            Picasso.get().load(Uri.parse(baner)).placeholder(R.drawable.default_battlemania).fit().into(imgeViewSelected);
        } else {
            imageViewSelectedCardview.setVisibility(View.GONE);
        }

        SharedPreferences sp = getSharedPreferences("currencyinfo", Context.MODE_PRIVATE);

        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();
        final UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());
        final CurrentUser user = userLocalStore.getLoggedInUser();

        //for detail about any match
        String url = resources.getString(R.string.api) + "single_match/" + mid + "/" + user.getMemberid();

        final JsonObjectRequest request = new JsonObjectRequest(url, null,
                response -> {

                    Log.d("--------------", response.toString());
                    try {
                        JSONObject obj = response.getJSONObject("match");

                        mId = obj.getString("m_id");
                        matchName = obj.getString("match_name");
                        matchTime = obj.getString("match_time");
                        winPrize = obj.getString("win_prize");
                        perKill = obj.getString("per_kill");
                        entryFee = obj.getString("entry_fee");
                        type = obj.getString("type");
                        //version = obj.getString("version");
                        MAP = obj.getString("MAP");
                        matchType = obj.getString("match_type");
                        matchDesc = obj.getString("match_desc");
                        noOfPlayer = obj.getString("no_of_player");
                        numberOfPosition = obj.getString("number_of_position");
                        memberId = obj.getString("member_id");
                        matchUrl = obj.getString("match_url");
                        roomId = obj.getString("room_description");
                        /*roomPassword = obj.getString("room_password");*/
                        matchprivateDesc = obj.getString("match_private_desc");
                        joinStatus = obj.getString("join_status");
                        packagename = obj.getString("package_name");

                        matchTitleBar.setText(matchName);
                        if (TextUtils.equals(noOfPlayer, numberOfPosition) || parseInt(noOfPlayer) >= Integer.parseInt(numberOfPosition)) {

                            if (!TextUtils.equals(from, "LIVE")) {

                                joinNow.setText(resources.getString(R.string.match_full));
                                joinNow.setEnabled(false);
                                joinNow.setBackgroundColor(getResources().getColor(R.color.newblack));

                                joinNow.setTextColor(Color.WHITE);
                            }

                        }

                        if (joinStatus.matches("true") == true) {

                            JSONArray joinarr = response.getJSONArray("join_position");


                            if (!TextUtils.equals(from, "LIVE")) {

                                joinNow.setBackgroundColor(getResources().getColor(R.color.newred));
                                joinNow.setTextColor(Color.WHITE);
                                joinNow.setText(resources.getString(R.string.already_joined));
                                joinNow.setEnabled(false);
                            }


                        } else {
                            if (TextUtils.equals(noOfPlayer, numberOfPosition) || parseInt(noOfPlayer) >= Integer.parseInt(numberOfPosition)) {

                            } else {
                                joinNow.setEnabled(true);
                            }
                        }


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


        if (TextUtils.equals(from, "LIVE")) {

            joinNow.setText(resources.getString(R.string.spectate));
            joinNow.setOnClickListener(view -> {

                Intent intent12 = new Intent(Intent.ACTION_VIEW, Uri.parse(matchUrl));
                startActivity(intent12);

            });
        } else {

            joinNow.setOnClickListener(view -> {
                Intent intent1 = new Intent(getApplicationContext(), SelectMatchPositionActivity.class);
                intent1.putExtra("MATCH_ID", mId);
                intent1.putExtra("MATCH_NAME", matchName);
                intent1.putExtra("TYPE", type);
                intent1.putExtra("TOTAL", numberOfPosition);
                intent1.putExtra("JOINSTATUS", joinStatus);
                intent1.putExtra("GAME_NAME", gameName);
                startActivity(intent1);
            });
        }


    }
}
