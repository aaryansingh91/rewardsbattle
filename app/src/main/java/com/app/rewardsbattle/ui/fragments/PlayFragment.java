//For Play tab at selected home page
package com.app.rewardsbattle.ui.fragments;

import static com.android.volley.Request.Method.GET;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.BanerData;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.models.HomeGameData;
import com.app.rewardsbattle.ui.activities.AnnouncementActivity;
import com.app.rewardsbattle.ui.activities.CustomerSupportActivity;
import com.app.rewardsbattle.ui.activities.MainActivity;
import com.app.rewardsbattle.ui.activities.MeActivity;
import com.app.rewardsbattle.ui.activities.MyWalletActivity;
import com.app.rewardsbattle.ui.activities.SelectedGameActivity;
import com.app.rewardsbattle.ui.adapters.HomeGameAdapter;
import com.app.rewardsbattle.ui.adapters.TestFragmentAdapter;
import com.app.rewardsbattle.utils.KKViewPager;
import com.app.rewardsbattle.utils.LoadingDialog;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.app.rewardsbattle.utils.UserLocalStore;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;
import me.toptas.fancyshowcase.listener.DismissListener;

public class PlayFragment extends Fragment {
    RequestQueue mQueue, dQueue, sQueue;
    TextView balInPlay;
    UserLocalStore userLocalStore;
    TextView noUpcoming;
    LoadingDialog loadingDialog;
    ShimmerFrameLayout shimer;
    CurrentUser user;
    LinearLayout allGameLl;
    SwipeRefreshLayout pullToRefresh;
    TextView announcement;
    CardView announcecv;
    Context context;
    Resources resources;
    KKViewPager mPager;
    RecyclerView gameRv;
    TextView username;
    HomeGameAdapter gameAdapter;
    List<HomeGameData> gameData;
    LinearLayout visiblelinear;
    LinearLayout upcoming, ongoing, completed;
    private StaggeredGridLayoutManager manager;
    int currentPage = 0;
    Timer timer;
    TabLayout tabLayout;
    List<BanerData> banerData;
    JSONArray arr;
    SharedPreferences sp;
    LinearLayout share, whatsappShare;
    ImageView instagram_share, telegram_share, whatsapp_share;
    String shareBody = "";
    ImageView visit_web, visit_support;
    CircleImageView profile;

    // Add this constant for request tag
    private static final String REQUEST_TAG = "PlayFragmentRequests";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize request queues in onCreate to avoid null issues
        if (getContext() != null) {
            mQueue = Volley.newRequestQueue(getContext());
            dQueue = Volley.newRequestQueue(getContext());
            sQueue = Volley.newRequestQueue(getContext());
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        View root = inflater.inflate(R.layout.play_home, container, false);

        // Check if fragment is attached before accessing activity
        if (!isAdded() || getActivity() == null) {
            return root;
        }

        sp = requireActivity().getSharedPreferences("tabitem", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor1111 = sp.edit();
        String pagenumber = sp.getString("TAB", "0");

        share = root.findViewById(R.id.share);
        whatsappShare = root.findViewById(R.id.whatsappShare);

        instagram_share = root.findViewById(R.id.instagram_share);
        telegram_share = root.findViewById(R.id.telegram_share);
        whatsapp_share = root.findViewById(R.id.whatsapp_share);

        gameData = new ArrayList<>();
        gameRv = root.findViewById(R.id.allgamerv);
        manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        /*gameRv.setHasFixedSize(true);*/
        gameRv.setLayoutManager(manager);

        context = LocaleHelper.setLocale(getContext());
        resources = context.getResources();
        tabLayout = root.findViewById(R.id.tablayoutmycontest);
        username = root.findViewById(R.id.username);
        visiblelinear = root.findViewById(R.id.visiblelinear);
        visit_web = root.findViewById(R.id.visit_web);
        visit_support = root.findViewById(R.id.visit_support);
        profile = root.findViewById(R.id.profile_image);

        visit_web.setOnClickListener(view -> {
            if (!isAdded() || getActivity() == null) return;

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
            if (!isAdded() || getActivity() == null) return;
            Intent intent = new Intent(getActivity(), CustomerSupportActivity.class);
            startActivity(intent);
        });

        profile.setOnClickListener(view -> {
            if (!isAdded() || getActivity() == null) return;
            Intent intent = new Intent(getActivity(), MeActivity.class);
            startActivity(intent);
        });

        if (TextUtils.equals(pagenumber, "1")) {
            Objects.requireNonNull(tabLayout.getTabAt(1)).select();
        } else {
            Objects.requireNonNull(tabLayout.getTabAt(0)).select();
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (!isAdded() || getActivity() == null) return;

                gameRv.removeAllViews();
                editor1111.putString("TAB", String.valueOf(tab.getPosition()));
                editor1111.apply();
                try {
                    gameAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                checkgame();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d("tab contest re", String.valueOf(tab.getPosition()));
            }
        });

        pullToRefresh = root.findViewById(R.id.pullToRefreshplay);
        pullToRefresh.setOnRefreshListener(this::refresh);

        mPager = root.findViewById(R.id.kk_pager);

        banerData = new ArrayList<>();

        loadingDialog = new LoadingDialog(getContext());
        shimer = root.findViewById(R.id.shimmerplay);
        userLocalStore = new UserLocalStore(getContext());
        user = userLocalStore.getLoggedInUser();

        // Log.d("sony", user.getToken());
        allGameLl = root.findViewById(R.id.allgamell);
        balInPlay = root.findViewById(R.id.balinplay);
        balInPlay.setOnClickListener(v -> {
            if (!isAdded() || getActivity() == null) return;
            Intent intent = new Intent(getContext(), MyWalletActivity.class);
            intent.putExtra("FROM", "PLAY");
            startActivity(intent);
        });

        announcement = root.findViewById(R.id.announce);
        announcecv = root.findViewById(R.id.announccv);
        announcecv.setVisibility(View.GONE);
        announcement.setOnClickListener(v -> {
            if (!isAdded() || getActivity() == null) return;
            startActivity(new Intent(getActivity(), AnnouncementActivity.class));
        });
        Announcement();

        noUpcoming = root.findViewById(R.id.noupcominginplay1);

        Objects.requireNonNull(tabLayout.getTabAt(0)).setText(resources.getString(R.string.tournament));
        Objects.requireNonNull(tabLayout.getTabAt(1)).setText(resources.getString(R.string.solo));

        /*dashboard start*/
        if (dQueue == null && getContext() != null) {
            dQueue = Volley.newRequestQueue(getContext());
        }
        if (dQueue != null) {
            dQueue.getCache().clear();
        }

        String url = resources.getString(R.string.api) + "dashboard/" + user.getMemberid();
        @SuppressLint("DefaultLocale") final JsonObjectRequest drequest = new JsonObjectRequest(url, null,
                response -> {
                    // CRITICAL: Check if fragment is still attached
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }

                    try {
                        Log.d("dash", response.toString());
                        JSONObject memobj = new JSONObject(response.getString("member"));
                        JSONObject obj = new JSONObject(response.getString("web_config"));
                        shareBody = obj.getString("share_description");
                        if (TextUtils.equals(memobj.getString("member_status"), "0")) {
                            if (!user.getUsername().isEmpty() && !user.getPassword().isEmpty()) {
                                userLocalStore.clearUserData();
                                Toast.makeText(getActivity(), resources.getString(R.string.your_account_is_blocked_by_admin), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getActivity(), MainActivity.class));
                            }
                            return;
                        }

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
                        /// Assigning the bigdecimal value of ln to
                        balInPlay.setText(totalMoney);
                        username.setText(memobj.getString("user_name"));
                    } catch (JSONException e) {
                        e.printStackTrace();
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
                Log.d("sony", token);
                headers.put("x-localization", LocaleHelper.getPersist(context));
                return headers;
            }
        };

        drequest.setShouldCache(false);
        drequest.setTag(REQUEST_TAG);
        if (dQueue != null) {
            dQueue.add(drequest);
        }

        share.setOnClickListener(v -> {
            if (!isAdded() || getActivity() == null) return;
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody + resources.getString(R.string.referral_code)+" : " + user.getUsername());
            startActivity(Intent.createChooser(sharingIntent, resources.getString(R.string.share_using)));
        });

        whatsappShare.setOnClickListener(v -> {
            if (!isAdded() || getActivity() == null) return;
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody + resources.getString(R.string.referral_code) + " : " + user.getUsername());
            sharingIntent.setPackage("com.whatsapp");

            try {
                startActivity(sharingIntent);
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(getActivity(), "WhatsApp is not installed on your device", Toast.LENGTH_SHORT).show();
            }
        });

        telegram_share.setOnClickListener(v -> {
            if (!isAdded() || getActivity() == null) return;
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody + resources.getString(R.string.referral_code) + " : " + user.getUsername());
            sharingIntent.setPackage("org.telegram.messenger");

            try {
                startActivity(sharingIntent);
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(getActivity(), "Telegram is not installed on your device", Toast.LENGTH_SHORT).show();
            }
        });

        instagram_share.setOnClickListener(v -> {
            if (!isAdded() || getActivity() == null) return;
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody + resources.getString(R.string.referral_code) + " : " + user.getUsername());
            sharingIntent.setPackage("com.instagram.android");

            try {
                startActivity(sharingIntent);
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(getActivity(), "Instagram is not installed on your device", Toast.LENGTH_SHORT).show();
            }
        });

        whatsapp_share.setOnClickListener(v -> {
            if (!isAdded() || getActivity() == null) return;
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody + resources.getString(R.string.referral_code) + " : " + user.getUsername());
            sharingIntent.setPackage("com.whatsapp");

            try {
                startActivity(sharingIntent);
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(getActivity(), "WhatsApp is not installed on your device", Toast.LENGTH_SHORT).show();
            }
        });


        /*dashboard end*/
        viewallslider();

        upcoming = root.findViewById(R.id.upcoming);
        ongoing = root.findViewById(R.id.ongoing);
        completed = root.findViewById(R.id.completed);

        upcoming.setOnClickListener(view -> {
            if (!isAdded() || getActivity() == null) return;
            Intent intent = new Intent(getActivity(), SelectedGameActivity.class);
            SharedPreferences sp1 = getActivity().getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp1.edit();
            editor.putString("gametitle", resources.getString(R.string.my_matches));
            editor.putString("gameid", "not");
            intent.putExtra("N", "1");
            editor.apply();
            startActivity(intent);
        });

        ongoing.setOnClickListener(view -> {
            if (!isAdded() || getActivity() == null) return;
            Intent intent = new Intent(getActivity(), SelectedGameActivity.class);
            SharedPreferences sp1 = getActivity().getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp1.edit();
            editor.putString("gametitle", resources.getString(R.string.my_matches));
            editor.putString("gameid", "not");
            intent.putExtra("N", "0");
            editor.apply();
            startActivity(intent);
        });

        completed.setOnClickListener(view -> {
            if (!isAdded() || getActivity() == null) return;
            Intent intent = new Intent(getActivity(), SelectedGameActivity.class);
            SharedPreferences sp1 = getActivity().getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp1.edit();
            editor.putString("gametitle", resources.getString(R.string.my_matches));
            editor.putString("gameid", "not");
            intent.putExtra("N", "2");
            editor.apply();
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

    public void viewallgame() {
        // Check if fragment is attached
        if (!isAdded() || getContext() == null) {
            return;
        }

        /*all_game api call start*/
        if (mQueue == null) {
            mQueue = Volley.newRequestQueue(getContext());
        }
        mQueue.getCache().clear();
        String jurl = resources.getString(R.string.api) + "home_game";
        final UserLocalStore userLocalStore = new UserLocalStore(getContext());

        final JsonObjectRequest request = new JsonObjectRequest(GET, jurl, null,
                response -> {
                    // CRITICAL: Check if fragment is still attached
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }

                    try {
                        arr = response.getJSONArray("all_game");
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
                Log.d("sx", token);
                headers.put("x-localization", LocaleHelper.getPersist(context));
                return headers;
            }
        };
        request.setShouldCache(false);
        request.setTag(REQUEST_TAG);
        mQueue.add(request);
        /*all_game api call end*/
    }

    private void checkgame() {
        // Check if fragment is attached
        if (!isAdded() || getActivity() == null) {
            return;
        }

        noUpcoming.setVisibility(View.GONE);
        gameRv.setVisibility(View.VISIBLE);
        List<String> issues = new ArrayList<>();
        int list = arr.length();
        for (int i = 0; i < list; i++) {
            try {
                JSONObject json = arr.getJSONObject(i);
                if (TextUtils.equals(sp.getString("TAB", "0"), json.getString("game_type"))) {
                    issues.add(String.valueOf(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (issues.isEmpty()) {
            gameRv.setVisibility(View.GONE);
            if (noUpcoming.getVisibility() != View.VISIBLE) {
                noUpcoming.setVisibility(View.VISIBLE);
            }
        }
        issues.clear();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array) {
        // Check if fragment is attached
        if (!isAdded() || getActivity() == null) {
            return;
        }

        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject json;
                json = array.getJSONObject(i);
                final HomeGameData data = new HomeGameData(json.getString("game_id"), json.getString("game_name"), json.getString("game_image"), json.getString("status"), json.getString("game_type"));
                Log.d(data.getGameName(), data.getGameImage());
                gameData.add(data);
                gameAdapter = new HomeGameAdapter(getActivity(), gameData);
                gameAdapter.notifyDataSetChanged();
                gameRv.setAdapter(gameAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        checkgame();
    }

    public void refresh() {
        if (!isAdded() || getActivity() == null) {
            return;
        }

        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
            activity.overridePendingTransition(0, 0);
            Intent intent = activity.getIntent();
            intent.putExtra("N", "1");
            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0);
        }
    }

    public void Announcement() {
        // Check if fragment is attached
        if (!isAdded() || getContext() == null) {
            return;
        }

        //for announcement api
        if (mQueue == null) {
            mQueue = Volley.newRequestQueue(getContext());
        }
        mQueue.getCache().clear();
        String vurl = resources.getString(R.string.api) + "announcement";
        final UserLocalStore userLocalStore = new UserLocalStore(getContext());

        final JsonObjectRequest request = new JsonObjectRequest(GET, vurl, null,
                response -> {
                    // CRITICAL: Check if fragment is still attached
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }

                    try {
                        Log.d("announce", response.toString());
                        JSONArray arr = response.getJSONArray("announcement");

                        if (!TextUtils.equals(response.getString("announcement"), "[]")) {
                            announcecv.setVisibility(View.GONE);
                        } else {
                            announcecv.setVisibility(View.GONE);
                            announcement.setText(Html.fromHtml(resources.getString(R.string.no_announcement_available)));
                        }
                        announcement.setClickable(true);
                        announcement.setMovementMethod(LinkMovementMethod.getInstance());
                        JSON_PARSE_DATA_AFTER_WEBCALLannounccement(arr);
                    } catch (JSONException e) {
                        e.printStackTrace();
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
    }

    public void viewallslider() {
        // Check if fragment is attached
        if (!isAdded() || getActivity() == null || getContext() == null) {
            return;
        }

        /*slider api call start*/
        if (sQueue == null) {
            sQueue = Volley.newRequestQueue(getContext());
        }
        sQueue.getCache().clear();
        String kurl = resources.getString(R.string.api) + "slider";
        final UserLocalStore userLocalStore = new UserLocalStore(getContext());

        final JsonObjectRequest srequest = new JsonObjectRequest(GET, kurl, null,
                response -> {
                    // CRITICAL: Check if fragment is still attached
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }

                    try {
                        Log.d("slider", response.toString());
                        JSONArray arr = response.getJSONArray("slider");

                        if (TextUtils.equals(response.getString("slider"), "[]")) {
                            mPager.setVisibility(View.GONE);
                            visiblelinear.setVisibility(View.VISIBLE);
                        } else {
                            mPager.setVisibility(View.VISIBLE);
                            visiblelinear.setVisibility(View.GONE);
                        }

                        JSON_PARSE_DATA_AFTER_WEBCALLslider(arr);
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
                headers.put("x-localization", "en");
                return headers;
            }
        };
        srequest.setShouldCache(false);
        srequest.setTag(REQUEST_TAG);
        sQueue.add(srequest);
        /*slider api call end*/
        viewallgame();
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALLslider(JSONArray array) {
        // CRITICAL: Check if fragment is still attached
        if (!isAdded() || getActivity() == null) {
            return;
        }

        banerData.clear();
        for (int i = 0; i < array.length(); i++) {
            JSONObject json;
            try {
                json = array.getJSONObject(i);
                BanerData data = new BanerData(json.getString("slider_id"), json.getString("slider_title"), json.getString("slider_image"), json.getString("slider_link_type"), json.getString("slider_link"), json.getString("link_id"), json.getString("game_name"));
                banerData.add(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Check again before setting adapter
        Activity activity = getActivity();
        if (activity == null || !isAdded()) {
            return;
        }

        mPager.setAdapter(new TestFragmentAdapter(getChildFragmentManager(),
                activity, banerData));

        mPager.setPageMargin(40);
        mPager.setAnimationEnabled(true);
        mPager.setFadeEnabled(true);
        mPager.setFadeFactor(0.6f);

        /*After setting the adapter use the timer */
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {

            public void run() {
                if (!isAdded() || getActivity() == null) {
                    return;
                }
                if (currentPage == banerData.size()) {
                    currentPage = 0;
                }
                mPager.setCurrentItem(currentPage++, true);
            }
        };

        // Cancel existing timer if any
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        timer = new Timer(); // This will create a new Thread
        timer.schedule(new TimerTask() { // task to be scheduled
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 2000, 2000);

    }

    public void JSON_PARSE_DATA_AFTER_WEBCALLannounccement(JSONArray array) {
        // Check if fragment is attached
        if (!isAdded() || getActivity() == null) {
            return;
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject json;
            announcecv.setVisibility(View.VISIBLE);
            try {
                json = array.getJSONObject(i);
                announcement.setText(Html.fromHtml(json.getString("announcement_desc")));
                announcement.setClickable(true);
                announcement.setMovementMethod(LinkMovementMethod.getInstance());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public FancyShowCaseView addView(View view, String title, String n) {
        if (!isAdded() || getActivity() == null) {
            return null;
        }

        FancyShowCaseView view1 = new FancyShowCaseView.Builder(getActivity())
                .focusOn(view)
                .title(title)
                //.titleGravity(Gravity.BOTTOM)
                .titleSize(20, 1)
                .focusShape(FocusShape.CIRCLE)
                .enableAutoTextPosition()
                .roundRectRadius(10)
                .focusBorderSize(1)
                .showOnce(n)
                .focusBorderColor(getActivity().getResources().getColor(R.color.newblue))
                .dismissListener(new DismissListener() {
                    @Override
                    public void onDismiss(String s) {

                    }

                    @Override
                    public void onSkipped(String s) {

                    }
                })
                .build();

        return view1;
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
        if (sQueue != null) {
            sQueue.cancelAll(REQUEST_TAG);
        }

        // Cancel timer
        if (timer != null) {
            timer.cancel();
            timer = null;
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
        if (sQueue != null) {
            sQueue.cancelAll(REQUEST_TAG);
            sQueue = null;
        }
    }
}