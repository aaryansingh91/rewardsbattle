package com.app.clashv2.ui.activities;

import static com.android.volley.Request.Method.GET;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.clashv2.R;
import com.app.clashv2.models.CurrentUser;
import com.app.clashv2.models.GameData;
import com.app.clashv2.ui.adapters.GameAdapter;
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

public class SubGameActivity extends AppCompatActivity {
    RequestQueue mQueue;
    Context context;
    Resources resources;
    UserLocalStore userLocalStore;
    GameAdapter gameAdapter;
    CurrentUser user;
    List<GameData> gameData;
    LoadingDialog loadingDialog;
    TextView noUpcoming;
    ProgressBar loadingProgressBar;
    private StaggeredGridLayoutManager manager;
    RecyclerView gameRv;
    JSONArray arr;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_game);

        context = LocaleHelper.setLocale(SubGameActivity.this);
        resources = context.getResources();

        SharedPreferences sp = getSharedPreferences("homegameinfo", Context.MODE_PRIVATE);
        String gameName = sp.getString("gametitle", "");
        String gameId = sp.getString("gameid", "");

        loadingDialog = new LoadingDialog(context);
        userLocalStore = new UserLocalStore(context);
        user = userLocalStore.getLoggedInUser();
        noUpcoming = findViewById(R.id.noupcominginplay1);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        gameData = new ArrayList<>();
        gameRv = findViewById(R.id.allgamerv);
        manager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        gameRv.setLayoutManager(manager);

        viewallgame(gameId);
    }

    public void viewallgame(String gameId) {
        // Show loading, hide other views
        loadingProgressBar.setVisibility(View.VISIBLE);
        gameRv.setVisibility(View.GONE);
        noUpcoming.setVisibility(View.GONE);

        mQueue = Volley.newRequestQueue(context);
        mQueue.getCache().clear();
        String jurl = resources.getString(R.string.api) + "all_game/" + gameId;
        final UserLocalStore userLocalStore = new UserLocalStore(context);

        final JsonObjectRequest request = new JsonObjectRequest(GET, jurl, null,
                response -> {
                    try {
                        arr = response.getJSONArray("all_game");
                        JSON_PARSE_DATA_AFTER_WEBCALL(arr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // Hide loading on error
                        loadingProgressBar.setVisibility(View.GONE);
                        noUpcoming.setVisibility(View.VISIBLE);
                    }
                    loadingDialog.dismiss();
                }, error -> {
            Log.e("**VolleyError", "error" + error.getMessage());
            // Hide loading on error
            loadingProgressBar.setVisibility(View.GONE);
            noUpcoming.setVisibility(View.VISIBLE);
        }) {
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
                Log.d("sx", token);
                headers.put("x-localization", LocaleHelper.getPersist(context));
                return headers;
            }
        };
        request.setShouldCache(false);
        mQueue.add(request);
    }

    private void checkgame() {
        // Hide loading first
        loadingProgressBar.setVisibility(View.GONE);

        noUpcoming.setVisibility(View.GONE);
        gameRv.setVisibility(View.VISIBLE);
        List<String> issues = new ArrayList<>();
        int list = arr.length();
        for (int i = 0; i < list; i++) {
            try {
                JSONObject json = arr.getJSONObject(i);
                issues.add(String.valueOf(i));
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
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject json;
                json = array.getJSONObject(i);
                final GameData data = new GameData(json.getString("game_id"), json.getString("game_name"), json.getString("game_image"), json.getString("status"), json.getString("total_upcoming_match"), json.getString("game_type"), json.getString("total_upcoming_challenge"), json.getString("package_name"), json.getString("coming_soon"));
                Log.d(data.getGameName(), data.getGameImage());
                gameData.add(data);
                gameAdapter = new GameAdapter(this, gameData);
                gameAdapter.notifyDataSetChanged();
                gameRv.setAdapter(gameAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        checkgame();
    }
}