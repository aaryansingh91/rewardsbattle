//For show participated player in selected tournament or match in tab layout
package com.app.rewardsbattle.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.app.rewardsbattle.utils.UserLocalStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FragmentSelectedTournamentJoinedeMember extends Fragment {

    LinearLayout ll;
    RequestQueue pQueue;
    TextView noMember;

    Context context;
    Resources resources;

    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_selectedtournament_joinedmember, container, false);

        context = LocaleHelper.setLocale(getContext());
        resources = context.getResources();

        ll = root.findViewById(R.id.ll);
        noMember = root.findViewById(R.id.nomember);

        Intent intent = getActivity().getIntent();
        final String mid = intent.getStringExtra("M_ID");
        final String from = intent.getStringExtra("FROM");

        final UserLocalStore userLocalStore = new UserLocalStore(getActivity());

        //for participated member list
        pQueue = Volley.newRequestQueue(getActivity());
        pQueue.getCache().clear();

        String purl = resources.getString(R.string.api) + "match_participate/" + mid;

        final JsonObjectRequest prequest = new JsonObjectRequest(purl, null,
                response -> {

                    try {
                        JSONArray arr = response.getJSONArray("match_participate");
                        if (TextUtils.equals(arr.toString(), "[]")) {
                            noMember.setVisibility(View.VISIBLE);
                        } else {
                            noMember.setVisibility(View.GONE);
                            JSON_PARSE_DATA_AFTER_WEBCALL(arr);
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
                String token="Bearer "+user.getToken();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                headers.put("x-localization", LocaleHelper.getPersist(context));
                return headers;
            }
        };
        prequest.setShouldCache(false);
        pQueue.add(prequest);

        return root;
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array) {
        int count = 1;
        for (int i = 0; i < array.length(); i++) {

            JSONObject json = null;
            try {
                json = array.getJSONObject(i);

                LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                View view = getLayoutInflater().inflate(R.layout.mystatisticsdata, null);
                TextView tvno = view.findViewById(R.id.no_mystatistics);
                TextView tvname = view.findViewById(R.id.title_mystatistics);
                TextView tv1 = view.findViewById(R.id.time_mystatistics);
                TextView tv2 = view.findViewById(R.id.paid);
                TextView tv3 = view.findViewById(R.id.won);
                tv1.setVisibility(View.GONE);
                tv2.setVisibility(View.GONE);
                tv3.setVisibility(View.GONE);
                tvno.setText("  " + String.valueOf(count) + ".   ");
                tvname.setText(json.getString("pubg_id"));
                ll.addView(view);
                count++;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
