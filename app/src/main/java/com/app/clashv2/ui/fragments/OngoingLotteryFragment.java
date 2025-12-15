package com.app.clashv2.ui.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.clashv2.R;
import com.app.clashv2.models.CurrentUser;
import com.app.clashv2.models.LotteryData;
import com.app.clashv2.ui.adapters.LotteryAdapter;
import com.app.clashv2.utils.LoadingDialog;
import com.app.clashv2.utils.LocaleHelper;
import com.app.clashv2.utils.UserLocalStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OngoingLotteryFragment extends Fragment {

    RecyclerView recyclerView;
    LotteryAdapter myAdapter;
    List<LotteryData> mData;
    RequestQueue mQueue;
    LoadingDialog loadingDialog;
    SwipeRefreshLayout pullToRefresh;
    UserLocalStore userLocalStore;
    CurrentUser user;
    TextView noOngoing;

    Context context;
    Resources resources;

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_ongoing_lottery,container,false);

        context = LocaleHelper.setLocale(getContext());
        resources = context.getResources();

        pullToRefresh = view.findViewById(R.id.pullToRefreshinongoinglottery);
        pullToRefresh.setOnRefreshListener(() -> {
            pullToRefresh.setRefreshing(true);
            refresh();
            pullToRefresh.setRefreshing(false);
        });

        loadingDialog = new LoadingDialog(getContext());
        loadingDialog.show();

        noOngoing= view.findViewById(R.id.noongoinglottery);
        recyclerView = view.findViewById(R.id.rvinongoinglottery);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(false);
        layoutManager.setReverseLayout(false);
        /*recyclerView.setHasFixedSize(true);*/
        recyclerView.setLayoutManager(layoutManager);

        mData = new ArrayList<>();

        userLocalStore=new UserLocalStore(getContext());
        user=userLocalStore.getLoggedInUser();

        mQueue = Volley.newRequestQueue(getContext());
        mQueue.getCache().clear();

        // for top player
        String url = resources.getString(R.string.api) + "lottery/"+user.getMemberid()+"/ongoing";

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            loadingDialog.dismiss();

            Log.d("response",response.toString());
            JSONObject player = null;
            try {
                JSONArray arr = response.getJSONArray("ongoing");

                if (!TextUtils.equals(response.getString("ongoing"), "[]")) {
                    noOngoing.setVisibility(View.GONE);
                } else {
                    noOngoing.setVisibility(View.VISIBLE);
                    noOngoing.setText(resources.getString(R.string.no_ongoing_lottery_found_));
                }
                JSON_PARSE_DATA_AFTER_WEBCALL(arr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.e("VolleyError", "error" + error.getMessage())) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> headers = new HashMap<>();
                UserLocalStore userLocalStore = new UserLocalStore(getContext());
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

        return view;
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array) {

        for (int i = 0; i < array.length(); i++) {
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);
                LotteryData data = new LotteryData(json.getString("lottery_id"), json.getString("lottery_title"), json.getString("lottery_image"), json.getString("lottery_time"), json.getString("lottery_rules"),json.getString("lottery_fees"),json.getString("lottery_prize"),json.getString("lottery_size"),json.getString("total_joined"),json.getString("join_status"),json.getString("won_by"),json.getString("join_member"));
                mData.add(data);
                myAdapter = new LotteryAdapter(getActivity(), mData);
                myAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(myAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
