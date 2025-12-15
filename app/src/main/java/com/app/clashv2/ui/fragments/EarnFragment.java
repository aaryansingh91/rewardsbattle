package com.app.clashv2.ui.fragments;

import static com.android.volley.Request.Method.GET;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.clashv2.R;
import com.app.clashv2.models.BanerData;
import com.app.clashv2.models.CurrentUser;
import com.app.clashv2.ui.activities.AboutUsActivity;
import com.app.clashv2.ui.activities.AnnouncementActivity;
import com.app.clashv2.ui.activities.CustomerSupportActivity;
import com.app.clashv2.ui.activities.HowtoActivity;
import com.app.clashv2.ui.activities.LeaderboardActivity;
import com.app.clashv2.ui.activities.LotteryActivity;
import com.app.clashv2.ui.activities.MeActivity;
import com.app.clashv2.ui.activities.MyProfileActivity;
import com.app.clashv2.ui.activities.MyReferralsActivity;
import com.app.clashv2.ui.activities.MyRewardedActivity;
import com.app.clashv2.ui.activities.MyStatisticsActivity;
import com.app.clashv2.ui.activities.MyWalletActivity;
import com.app.clashv2.ui.activities.ProductActivity;
import com.app.clashv2.ui.activities.ReferandEarnActivity;
import com.app.clashv2.ui.activities.SelectedGameActivity;
import com.app.clashv2.ui.activities.TermsAndConditionActivity;
import com.app.clashv2.ui.activities.TopPlayerActivity;
import com.app.clashv2.ui.activities.WatchAndEarnActivity;
import com.app.clashv2.utils.LoadingDialog;
import com.app.clashv2.utils.LocaleHelper;
import com.app.clashv2.utils.UserLocalStore;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EarnFragment extends Fragment {

    LinearLayout balance;
    RequestQueue dQueue;
    UserLocalStore userLocalStore;
    TextView balInEarn;
    TextView username;
    LoadingDialog loadingDialog;
    CurrentUser user;
    SharedPreferences sp;
    String shareBody = "";
    RequestQueue mQueue;
    LinearLayout banerll;

    Context context;
    Resources resources;
    ImageView visit_web, visit_support;
    CircleImageView profile;

    // Add request tag constant
    private static final String REQUEST_TAG = "EarnFragmentRequests";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize request queues in onCreate
        if (getContext() != null) {
            dQueue = Volley.newRequestQueue(getContext());
            mQueue = Volley.newRequestQueue(getContext());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.earn_home, container, false);

        // Check if fragment is attached
        if (!isAdded() || getActivity() == null || getContext() == null) {
            return view;
        }

        context = LocaleHelper.setLocale(getContext());
        resources = context.getResources();

        loadingDialog = new LoadingDialog(getActivity());
        sp = requireActivity().getSharedPreferences("tabinfo", Context.MODE_PRIVATE);
        String selectedtab = sp.getString("selectedtab", "");
        if (TextUtils.equals(selectedtab, "0")) {
            loadingDialog.show();
        }

        banerll = view.findViewById(R.id.banerll);
        balInEarn = view.findViewById(R.id.balinearn);
        username = view.findViewById(R.id.username);
        visit_web = view.findViewById(R.id.visit_web);
        visit_support = view.findViewById(R.id.visit_support);
        profile = view.findViewById(R.id.profile_image);

        visit_web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isAdded() || getActivity() == null) return;

                String url = resources.getString(R.string.url);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));

                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), "No browser app found to open the link", Toast.LENGTH_SHORT).show();
                }
            }
        });

        visit_support.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isAdded() || getActivity() == null) return;
                Intent intent = new Intent(getActivity(), CustomerSupportActivity.class);
                startActivity(intent);
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isAdded() || getActivity() == null) return;
                Intent intent = new Intent(getActivity(), MeActivity.class);
                startActivity(intent);
            }
        });

        userLocalStore = new UserLocalStore(requireContext());
        user = userLocalStore.getLoggedInUser();

        //dashboard api call start
        if (dQueue == null) {
            dQueue = Volley.newRequestQueue(requireContext());
        }
        dQueue.getCache().clear();

        String durl = resources.getString(R.string.api) + "dashboard/" + user.getMemberid();

        final JsonObjectRequest drequest = new JsonObjectRequest(durl, null,
                response -> {
                    // CRITICAL: Check if fragment is still attached
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }

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

                    if (loadingDialog != null) {
                        loadingDialog.dismiss();
                    }
                }, error -> {
            // CRITICAL: Check if fragment is still attached
            if (!isAdded() || getActivity() == null) {
                return;
            }
            Log.e("**VolleyError", "error" + error.getMessage());
        }) {
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
        drequest.setTag(REQUEST_TAG);
        dQueue.add(drequest);
        // dashboard api call end

//        share.setOnClickListener(view12 -> {
//            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//            sharingIntent.setType("text/plain");
//            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody + " Referral Code : " + user.getUsername());
//            startActivity(Intent.createChooser(sharingIntent, "Share using"));
//        });

        balance = view.findViewById(R.id.balanceinearn);
        balance.setOnClickListener(view1 -> {
            if (!isAdded() || getActivity() == null) return;
            Intent intent = new Intent(getContext(), MyWalletActivity.class);
            intent.putExtra("FROM", "EARN");
            startActivity(intent);
        });
        viewBaner();
        return view;
    }

    public void viewBaner() {
        // Check if fragment is attached
        if (!isAdded() || getContext() == null) {
            return;
        }

        /*banner api call start*/
        if (mQueue == null) {
            mQueue = Volley.newRequestQueue(getContext());
        }
        mQueue.getCache().clear();
        String url = resources.getString(R.string.api) + "banner";
        final UserLocalStore userLocalStore = new UserLocalStore(getContext());

        final JsonObjectRequest request = new JsonObjectRequest(GET, url, null,
                response -> {
                    // CRITICAL: Check if fragment is still attached
                    if (!isAdded() || getActivity() == null || getContext() == null) {
                        return;
                    }

                    try {
                        JSONArray arr = response.getJSONArray("banner");

                        JSON_PARSE_DATA_AFTER_WEBCALL(arr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (loadingDialog != null) {
                        loadingDialog.dismiss();
                    }
                }, error -> {
            // CRITICAL: Check if fragment is still attached
            if (!isAdded() || getActivity() == null) {
                return;
            }
            Log.e("**VolleyError", "error" + error.getMessage());
        }) {
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
        request.setTag(REQUEST_TAG);
        mQueue.add(request);
        /*banner api call end*/

    }

    boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array) {
        // CRITICAL: Check if fragment is still attached BEFORE using getLayoutInflater()
        if (!isAdded() || getActivity() == null || getContext() == null) {
            return;
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);
                BanerData data = new BanerData(json.getString("banner_id"), json.getString("banner_title"), json.getString("banner_image"), json.getString("banner_link_type"), json.getString("banner_link"), json.getString("link_id"), json.getString("game_name"));

                // Check again before inflating layout
                if (!isAdded() || getActivity() == null) {
                    return;
                }

                View view = getLayoutInflater().inflate(R.layout.baner_layout, null);
                CardView banercard = view.findViewById(R.id.banercard);
                ImageView baneriv = view.findViewById(R.id.baneriv);

                if (TextUtils.equals(data.getLink(), "Refer and Earn")) {
                    Picasso.get().load(Uri.parse(data.getImage())).placeholder(R.drawable.refer_and_earn).fit().into(baneriv);
                } else if (TextUtils.equals(data.getLink(), "Luckey Draw")) {
                    Picasso.get().load(Uri.parse(data.getImage())).placeholder(R.drawable.lucky_draw).fit().into(baneriv);
                } else if (TextUtils.equals(data.getLink(), "Watch and Earn")) {
                    Picasso.get().load(Uri.parse(data.getImage())).placeholder(R.drawable.watch_and_earn).fit().into(baneriv);
                } else if (TextUtils.equals(data.getLink(), "Buy Product")) {
                    Picasso.get().load(Uri.parse(data.getImage())).placeholder(R.drawable.buy_product).fit().into(baneriv);
                } else {
                    Picasso.get().load(Uri.parse(data.getImage())).placeholder(R.drawable.default_battlemania).fit().into(baneriv);
                }

                banercard.setOnClickListener(v -> {
                    // Check if fragment is attached before handling click
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }

                    if (TextUtils.equals(data.getLinkType(), "app")) {
                        if (TextUtils.equals(data.getLink(), "Refer and Earn")) {
                            Intent intent = new Intent(getActivity(), ReferandEarnActivity.class);
                            startActivity(intent);
                        } else if (TextUtils.equals(data.getLink(), "Luckey Draw")) {
                            Intent intent = new Intent(getActivity(), LotteryActivity.class);
                            startActivity(intent);
                        } else if (TextUtils.equals(data.getLink(), "Watch and Earn")) {
                            Intent intent = new Intent(getActivity(), WatchAndEarnActivity.class);
                            startActivity(intent);
                        } else if (TextUtils.equals(data.getLink(), "My Profile")) {
                            Intent intent = new Intent(getActivity(), MyProfileActivity.class);
                            startActivity(intent);
                        } else if (TextUtils.equals(data.getLink(), "My Wallet")) {
                            Intent intent = new Intent(getActivity(), MyWalletActivity.class);
                            startActivity(intent);
                        } else if (TextUtils.equals(data.getLink(), "My Matches")) {
                            Intent intent = new Intent(getActivity(), SelectedGameActivity.class);
                            SharedPreferences sp = getActivity().getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("gametitle", "My Matches");
                            editor.putString("gameid", "not");
                            editor.apply();
                            startActivity(intent);
                        } else if (TextUtils.equals(data.getLink(), "My Statics")) {
                            Intent intent = new Intent(getActivity(), MyStatisticsActivity.class);
                            startActivity(intent);
                        } else if (TextUtils.equals(data.getLink(), "My Referral")) {
                            Intent intent = new Intent(getActivity(), MyReferralsActivity.class);
                            startActivity(intent);
                        } else if (TextUtils.equals(data.getLink(), "My Rewards")) {
                            Intent intent = new Intent(getActivity(), MyRewardedActivity.class);
                            startActivity(intent);
                        } else if (TextUtils.equals(data.getLink(), "Announcement")) {
                            Intent intent = new Intent(getActivity(), AnnouncementActivity.class);
                            startActivity(intent);
                        } else if (TextUtils.equals(data.getLink(), "Top Players")) {
                            Intent intent = new Intent(getActivity(), TopPlayerActivity.class);
                            startActivity(intent);
                        } else if (TextUtils.equals(data.getLink(), "Leaderboard")) {
                            Intent intent = new Intent(getActivity(), LeaderboardActivity.class);
                            startActivity(intent);
                        } else if (TextUtils.equals(data.getLink(), "App Tutorials")) {
                            Intent intent = new Intent(getActivity(), HowtoActivity.class);
                            startActivity(intent);
                        } else if (TextUtils.equals(data.getLink(), "About us")) {
                            Intent intent = new Intent(getActivity(), AboutUsActivity.class);
                            startActivity(intent);
                        } else if (TextUtils.equals(data.getLink(), "Customer Support")) {
                            Intent intent = new Intent(getActivity(), CustomerSupportActivity.class);
                            startActivity(intent);
                        } else if (TextUtils.equals(data.getLink(), "Terms and Condition")) {
                            Intent intent = new Intent(getActivity(), TermsAndConditionActivity.class);
                            startActivity(intent);
                        } else if (TextUtils.equals(data.getLink(), "Game")) {
                            Activity activity = getActivity();
                            if (activity != null) {
                                Intent intent = new Intent(activity, SelectedGameActivity.class);
                                SharedPreferences sp = activity.getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("gametitle", data.getLinkname());
                                editor.putString("gameid", data.getLinkId());
                                editor.apply();
                                startActivity(intent);
                            }
                        } else if (TextUtils.equals(data.getLink(), "Buy Product")) {
                            Intent intent = new Intent(getActivity(), ProductActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.getLink()));
                        startActivity(browserIntent);
                    }
                });

                banerll.addView(view);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Cancel all pending network requests
        if (mQueue != null) {
            mQueue.cancelAll(REQUEST_TAG);
        }
        if (dQueue != null) {
            dQueue.cancelAll(REQUEST_TAG);
        }

        // Dismiss loading dialog
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Final cleanup
        if (mQueue != null) {
            mQueue.cancelAll(REQUEST_TAG);
            mQueue = null;
        }
        if (dQueue != null) {
            dQueue.cancelAll(REQUEST_TAG);
            dQueue = null;
        }
    }
}