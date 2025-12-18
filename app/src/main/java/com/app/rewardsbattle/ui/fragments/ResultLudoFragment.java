package com.app.rewardsbattle.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.models.LudoLivematchData;
import com.app.rewardsbattle.ui.adapters.LudoResultAdapter;
import com.app.rewardsbattle.utils.LoadingDialog;
import com.app.rewardsbattle.utils.UserLocalStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultLudoFragment extends Fragment {

    RequestQueue jQueue;
    RecyclerView recyclerView;
    LudoResultAdapter myAdapter;
    List<LudoLivematchData> mData;
    CurrentUser user;
    UserLocalStore userLocalStore;
    EditText resulttext;
    LoadingDialog loadingDialog;
    JSONArray arr;
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 700;
    Handler handlerbtn = new Handler();
    Runnable runnablebtn;
    int delay2 = 2500;
    String recent = "***********";
    LinearLayout resultludoempty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_result_ludo, container, false);

        loadingDialog = new LoadingDialog(getContext());
        loadingDialog.show();

        userLocalStore = new UserLocalStore(getContext());

        recyclerView = view.findViewById(R.id.ludoresultrecyclerview);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(false);
        layoutManager.setReverseLayout(false);
        /*recyclerView.setHasFixedSize(true);*/
        recyclerView.setLayoutManager(layoutManager);
        mData = new ArrayList<>();

        resulttext = view.findViewById(R.id.resulttext);
        resultludoempty = view.findViewById(R.id.resultludoempty);
        SharedPreferences searchdataaa = getActivity().getSharedPreferences("ludosearch", Context.MODE_PRIVATE);

        handlerbtn.postDelayed(runnablebtn = new Runnable() {
            public void run() {
                handlerbtn.postDelayed(runnablebtn, delay2);
                if (TextUtils.equals(searchdataaa.getString("btnstatus", ""), "on")) {
                    delay2 = arr.length() == 0 ? 86400 : arr.length() < 5 ? 1000 : 700;
                } else {
                    delay2 = 86400;
                }
            }
        }, delay2);

        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delay);
                if (!TextUtils.equals(recent, searchdataaa.getString("searchdata", ""))) {
                    resulttext.setText(searchdataaa.getString("searchdata", ""));
                }
            }
        }, delay);

        resulttext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mData.clear();
                recyclerView.removeAllViews();
                recent = resulttext.getText().toString();
                if(resultludoempty.getVisibility() == View.VISIBLE){
                    resultludoempty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
                try {
                    if (arr.length() > 0) {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject result_json = null;
                            try {
                                result_json = arr.getJSONObject(i);
                                LudoLivematchData data1 = new LudoLivematchData(result_json.getString("ludo_challenge_id"), result_json.getString("auto_id"), result_json.getString("member_id"), result_json.getString("accepted_member_id"), result_json.getString("ludo_king_username"), result_json.getString("accepted_ludo_king_username"), result_json.getString("coin"), result_json.getString("winning_price"), result_json.getString("room_code"), result_json.getString("accept_status"), result_json.getString("challenge_status"), result_json.getString("canceled_by"), result_json.getString("winner_id"), result_json.getString("date_created"), result_json.getString("first_name"), result_json.getString("last_name"), result_json.getString("profile_image"), result_json.getString("accepted_member_name"), result_json.getString("accepted_profile_image"), result_json.getString("added_result"), result_json.getString("accepted_result"), result_json.getString("player_id"), result_json.getString("accepted_player_id"), result_json.getString("with_password"), result_json.getString("challenge_password"), result_json.getString("notification_status"));
                                if (resulttext.getText().toString().contains("***********")) {
                                    mData.add(data1);
                                    myAdapter = new LudoResultAdapter(getContext(), mData);
                                    myAdapter.notifyDataSetChanged();
                                    recyclerView.setAdapter(myAdapter);
                                } else {
                                    if (data1.getAutoId().toUpperCase().startsWith(resulttext.getText().toString().toUpperCase()) || data1.getAutoId().toUpperCase().contains(resulttext.getText().toString().toUpperCase())) {
                                        mData.add(data1);
                                        myAdapter = new LudoResultAdapter(getContext(), mData);
                                        myAdapter.notifyDataSetChanged();
                                        recyclerView.setAdapter(myAdapter);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if (mData.isEmpty()) {
                            resultludoempty.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        jQueue = Volley.newRequestQueue(getContext());
        jQueue.getCache().clear();

        final UserLocalStore userLocalStore = new UserLocalStore(getContext());
        user = userLocalStore.getLoggedInUser();

        SharedPreferences sp = getActivity().getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
        String gameid = sp.getString("gameid", "");

        Log.d("id", gameid);
        // for get info about blank space in room for showing check box checked or unchecked
        String url = getResources().getString(R.string.api) + "challenge_result_list/" + gameid;

        final JsonObjectRequest request = new JsonObjectRequest(url, null,
                response -> {
                    loadingDialog.dismiss();
                    try {
                        arr = response.getJSONArray("challenge_list");
                        resultludoempty.setVisibility(arr.length() == 0 ? View.VISIBLE : View.GONE);
                        recyclerView.setVisibility(arr.length() > 0 ? View.VISIBLE : View.GONE);
                        JSON_PARSE_DATA_AFTER_WEBCALL();
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
                String token = "Bearer " + user.getToken();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                return headers;
            }
        };
        request.setShouldCache(false);
        jQueue.add(request);

        return view;
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALL() {
        Log.d("RESULT ARRAY DATA", String.valueOf(arr));
        for (int i = 0; i < arr.length(); i++) {
            JSONObject json = null;
            try {
                json = arr.getJSONObject(i);
                LudoLivematchData data = new LudoLivematchData(json.getString("ludo_challenge_id"), json.getString("auto_id"), json.getString("member_id"), json.getString("accepted_member_id"), json.getString("ludo_king_username"), json.getString("accepted_ludo_king_username"), json.getString("coin"), json.getString("winning_price"), json.getString("room_code"), json.getString("accept_status"), json.getString("challenge_status"), json.getString("canceled_by"), json.getString("winner_id"), json.getString("date_created"), json.getString("first_name"), json.getString("last_name"), json.getString("profile_image"), json.getString("accepted_member_name"), json.getString("accepted_profile_image"), json.getString("added_result"), json.getString("accepted_result"), json.getString("player_id"), json.getString("accepted_player_id"), json.getString("with_password"), json.getString("challenge_password"), json.getString("notification_status"));
                mData.add(data);
                myAdapter = new LudoResultAdapter(getContext(), mData);
                myAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(myAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void onBackPressed() {
        delay = 1000000;
    }
}