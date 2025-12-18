//For Ongoing tab at selected game page
package com.app.rewardsbattle.ui.fragments;

import static com.android.volley.Request.Method.GET;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.AllOngoingMatchData;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.ui.adapters.AllOngoingMatchAdapter;
import com.app.rewardsbattle.utils.LoadingDialog;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.app.rewardsbattle.utils.UserLocalStore;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnGoingFragment extends Fragment {

    RecyclerView recyclerView;
    AllOngoingMatchAdapter myAdapter;
    List<AllOngoingMatchData> mData;
    RequestQueue mQueue;
    TextView noLive;
    LoadingDialog loadingDialog;
    ShimmerFrameLayout shimer;
    SwipeRefreshLayout pullToRefresh;
    CurrentUser user;
    UserLocalStore userLocalStore;

    Context context;
    Resources resources;

    public View onCreateView(
             LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.ongoing_home, container, false);

        context = LocaleHelper.setLocale(getContext());
        resources = context.getResources();

        loadingDialog = new LoadingDialog(getContext());
        shimer = root.findViewById(R.id.shimmerongoing);

        pullToRefresh = root.findViewById(R.id.pullToRefreshongoing);
        pullToRefresh.setOnRefreshListener(() -> {
            pullToRefresh.setRefreshing(true);
            refresh();
            pullToRefresh.setRefreshing(false);
        });

        userLocalStore = new UserLocalStore(getContext());
        user = userLocalStore.getLoggedInUser();

        recyclerView = root.findViewById(R.id.ongoingrecyclerview);
        noLive = root.findViewById(R.id.nolive);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(false);
        layoutManager.setReverseLayout(false);
        /*recyclerView.setHasFixedSize(true);*/
        recyclerView.setLayoutManager(layoutManager);

        mData = new ArrayList<>();

        /*all_ongoing_match api call start*/
        mQueue = Volley.newRequestQueue(getContext());
        mQueue.getCache().clear();

        SharedPreferences sp = getActivity().getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
        final String matchid = sp.getString("gameid", "");

        String url =null;
        if(TextUtils.equals(matchid,"not")){
            url= resources.getString(R.string.api) + "my_match/" + user.getMemberid();
        }else {
            url = resources.getString(R.string.api) + "all_ongoing_match/" + matchid + "/" + user.getMemberid();
        }

        final JsonObjectRequest request = new JsonObjectRequest(GET, url, null,
                response -> {
                    try {

                        if(TextUtils.equals(matchid,"not")){
                            JSONArray arr = response.getJSONArray("my_match");
                            noLive.setVisibility(View.GONE);
                            if (!TextUtils.equals(response.getString("my_match"), "[]")) {
                                noLive.setVisibility(View.GONE);
                            } else {
                                noLive.setVisibility(View.VISIBLE);
                                noLive.setText(resources.getString(R.string.no_live_match_found));
                            }
                            JSON_PARSE_DATA_AFTER_WEBCALL(matchid,arr);
                        }else {
                            JSONArray arr = response.getJSONArray("all_ongoing_match");
                            noLive.setVisibility(View.GONE);
                            if (!TextUtils.equals(response.getString("all_ongoing_match"), "[]")) {
                                noLive.setVisibility(View.GONE);
                            } else {
                                noLive.setVisibility(View.VISIBLE);
                                noLive.setText(resources.getString(R.string.no_live_match_found));
                            }
                            JSON_PARSE_DATA_AFTER_WEBCALL(matchid,arr);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    shimer.setVisibility(View.GONE);
                }, error -> Log.e("**VolleyError", "error" + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return super.getParams();
            }
            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> headers = new HashMap<>();
                CurrentUser user = userLocalStore.getLoggedInUser();
                String token="Bearer "+user.getToken();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                headers.put("x-localization", LocaleHelper.getPersist(context));

                return headers;
            }
        };

        request.setShouldCache(false);
        mQueue.add(request);
        /*all_ongoing_match api call end*/

        return root;
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALL(String matchid,JSONArray array) {

        int count=0;
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);

                if(TextUtils.equals(matchid,"not")) {

                    if (TextUtils.equals(json.getString("match_status"), "3")) {
                        count++;
                        AllOngoingMatchData data = new AllOngoingMatchData(json.getString("m_id"), json.getString("match_banner"), json.getString("match_name"), json.getString("match_time"), json.getString("win_prize"), json.getString("per_kill"), json.getString("entry_fee"), json.getString("type"), "", json.getString("MAP"), json.getString("match_url"), json.getString("member_id"), json.getString("match_type"), json.getString("room_description"), json.getString("join_status"));
                        mData.add(data);
                        myAdapter = new AllOngoingMatchAdapter(getContext(), mData,json.getString("result_type"));
                        myAdapter.notifyDataSetChanged();
                        recyclerView.setAdapter(myAdapter);
                    }
                }else {
                    AllOngoingMatchData data = new AllOngoingMatchData(json.getString("m_id"), json.getString("match_banner"), json.getString("match_name"), json.getString("match_time"), json.getString("win_prize"), json.getString("per_kill"), json.getString("entry_fee"), json.getString("type"), "", json.getString("MAP"), json.getString("match_url"), json.getString("member_id"), json.getString("match_type"), json.getString("room_description"),json.getString("join_status"));
                    mData.add(data);
                    myAdapter = new AllOngoingMatchAdapter(getContext(), mData,json.getString("result_type"));
                    myAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(myAdapter);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (TextUtils.equals(matchid,"not")&&count==0){
            noLive.setVisibility(View.VISIBLE);
        }
    }

    public void refresh() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requireActivity().getSupportFragmentManager().beginTransaction().detach(this).commitNow();
            requireActivity().getSupportFragmentManager().beginTransaction().attach(this).commitNow();
        } else {
            requireActivity().getSupportFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }
}
