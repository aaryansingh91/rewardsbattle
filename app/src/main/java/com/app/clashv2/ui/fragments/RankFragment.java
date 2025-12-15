package com.app.clashv2.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.clashv2.R;
import com.app.clashv2.models.CurrentUser;
import com.app.clashv2.models.TopplayerData;
import com.app.clashv2.ui.activities.CustomerSupportActivity;
import com.app.clashv2.ui.activities.MeActivity;
import com.app.clashv2.ui.activities.MyWalletActivity;
import com.app.clashv2.ui.adapters.TopplayerAdapter;
import com.app.clashv2.utils.AnalyticsUtil;
import com.app.clashv2.utils.LoadingDialog;
import com.app.clashv2.utils.LocaleHelper;
import com.app.clashv2.utils.UserLocalStore;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RankFragment extends Fragment {
    RecyclerView recyclerView;
    TopplayerAdapter myAdapter;
    List<TopplayerData> mData;
    RequestQueue mQueue;
    LoadingDialog loadingDialog;
    LinearLayout logoll;
    Boolean firsttime = true;
    TextView notoplayer;
    LinearLayout balance;
    RequestQueue dQueue;
    UserLocalStore userLocalStore;
    String shareBody = "";
    TextView balInEarn;
    TextView username;
    CurrentUser user;
    Context context;
    Resources resources;
    ImageView visit_web, visit_support;
    CircleImageView profile;
    TextView tvRank1, tvRank2, tvRank3;
    TextView tvWin1, tvWin2, tvWin3;

    public RankFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_rank, container, false);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(requireActivity());

        context = LocaleHelper.setLocale(requireActivity());
        resources = context.getResources();

        notoplayer = root.findViewById(R.id.notoplayer);
        loadingDialog = new LoadingDialog(requireActivity());
        loadingDialog.show();

        logoll = root.findViewById(R.id.logoll);
        recyclerView = root.findViewById(R.id.topplayerrecyclerview);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(false);
        layoutManager.setReverseLayout(false);
        /*recyclerView.setHasFixedSize(true);*/
        recyclerView.setLayoutManager(layoutManager);

        mData = new ArrayList<>();

        mQueue = Volley.newRequestQueue(requireActivity());
        mQueue.getCache().clear();

        // for top player
        String url = resources.getString(R.string.api) + "top_players";

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            loadingDialog.dismiss();
            Log.d("response", response.toString());
            try {
                JSONObject jsnobject = new JSONObject(response.getString("top_players"));
                JSONArray arrgame = response.getJSONArray("game");
                JSON_PARSE_DATA_AFTER_WEBCALLgame(arrgame, jsnobject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.e("VolleyError", "error" + error.getMessage())) {
            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> headers = new HashMap<>();
                UserLocalStore userLocalStore = new UserLocalStore(requireContext());
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

        balInEarn = root.findViewById(R.id.balinearn);
        username = root.findViewById(R.id.username);

        visit_web = root.findViewById(R.id.visit_web);
        visit_support = root.findViewById(R.id.visit_support);
        profile = root.findViewById(R.id.profile_image);

        tvRank1 = root.findViewById(R.id.tvRank1);
        tvRank2 = root.findViewById(R.id.tvRank2);
        tvRank3 = root.findViewById(R.id.tvRank3);

        tvWin1 = root.findViewById(R.id.tvWin1);
        tvWin2 = root.findViewById(R.id.tvWin2);
        tvWin3 = root.findViewById(R.id.tvWin3);

        visit_web.setOnClickListener(view -> {
            String weburl = resources.getString(R.string.url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(weburl));

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

        userLocalStore = new UserLocalStore(requireContext());
        user = userLocalStore.getLoggedInUser();

        //dashboard api call start
        dQueue = Volley.newRequestQueue(requireContext());
        dQueue.getCache().clear();

        String durl = resources.getString(R.string.api) + "dashboard/" + user.getMemberid();

        final JsonObjectRequest drequest = new JsonObjectRequest(durl, null,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response.getString("web_config"));
                        if (TextUtils.equals(obj.getString("active_referral"), "1")) {
                            //noReferEarn.setVisibility(View.GONE);
                        } else {
                            //noReferEarn.setVisibility(View.VISIBLE);
                        }
                        shareBody = obj.getString("share_description");
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
                        balInEarn.setText(totalMoney);
                        username.setText(memobj.getString("user_name"));

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

        drequest.setShouldCache(false);
        dQueue.add(drequest);
        // dashboard api call end

//        share.setOnClickListener(view12 -> {
//            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//            sharingIntent.setType("text/plain");
//            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody + " Referral Code : " + user.getUsername());
//            startActivity(Intent.createChooser(sharingIntent, "Share using"));
//        });

        balance = root.findViewById(R.id.balanceinearn);
        balance.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), MyWalletActivity.class);
            intent.putExtra("FROM", "EARN");
            startActivity(intent);
        });

        return root;
    }

    boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALLgame(JSONArray array, final JSONObject object) {

        if (TextUtils.equals(String.valueOf(array.length()), "0")) {
            logoll.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            notoplayer.setVisibility(View.VISIBLE);
        } else {
            for (int i = 0; i < array.length(); i++) {
                JSONObject json;
                try {
                    json = array.getJSONObject(i);
                    View view = getLayoutInflater().inflate(R.layout.topplayer_logo_layout, null);
                    final TextView gamelogo = view.findViewById(R.id.topplayerlogoiv);
                    Log.d("game_logo", json.getString("game_logo"));
                    gamelogo.setText(json.getString("game_name"));
                    gamelogo.setTag(json.getString("game_name"));
                    final JSONObject finalJson = json;

                    if (firsttime) {
                        firsttime = false;
                        JSONArray arr = null;
                        try {
                            arr = object.getJSONArray(finalJson.getString("game_name"));
                            JSON_PARSE_DATA_AFTER_WEBCALL(arr, finalJson.getString("game_name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    gamelogo.setOnClickListener(v -> {
                        mData.clear();
                        JSONArray arr = null;
                        try {
                            arr = object.getJSONArray(finalJson.getString("game_name"));
                            JSON_PARSE_DATA_AFTER_WEBCALL(arr, finalJson.getString("game_name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    });
                    logoll.addView(view);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array, String gamename) {

        for (int i = 0; i < array.length(); i++) {
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);

                if (i == 0) {
                    String userName = json.optString("user_name", "--");
                    String winning = json.optString("winning", "--");
                    tvRank1.setText(userName);
                    tvWin1.setText("WIN - " + winning);
                }

                if (i == 1) {
                    String userName = json.optString("user_name", "--");
                    String winning = json.optString("winning", "--");
                    tvRank2.setText(userName);
                    tvWin2.setText("WIN - " + winning);
                }

                if (i == 2) {
                    String userName = json.optString("user_name", "--");
                    String winning = json.optString("winning", "--");
                    tvRank3.setText(userName);
                    tvWin3.setText("WIN - " + winning);
                }

                if (i != 0) {
                    gamename = "";
                }
                TopplayerData data = new TopplayerData(gamename, json.getString("winning"), json.getString("user_name"), json.getString("member_id"), json.getString("pubg_id"));
                Log.e("winning", json.getString("winning"));
                mData.add(data);
                myAdapter = new TopplayerAdapter(requireActivity(), mData);
                myAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(myAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}