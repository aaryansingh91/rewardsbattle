//For after successfully join match or tournament
package com.app.rewardsbattle.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.rewardsbattle.R;
import com.app.rewardsbattle.utils.AnalyticsUtil;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.google.firebase.analytics.FirebaseAnalytics;

public class SuccessJoinActivity extends AppCompatActivity {

    Context context;
    Resources resources;
    TextView joinedSuccessfulTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_success_join);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        context = LocaleHelper.setLocale(SuccessJoinActivity.this);
        resources = context.getResources();

        joinedSuccessfulTitle = findViewById(R.id.joinedsuccesfullytitleid);
        TextView nameid = findViewById(R.id.successjoinmatchnameid);
        Button home = findViewById(R.id.joinsuccesshome);

        Intent intent = getIntent();
        String matchId = intent.getStringExtra("MATCH_ID");
        String matchName = intent.getStringExtra("MATCH_NAME");

        joinedSuccessfulTitle.setText(resources.getString(R.string.you_have_joined_successfully));

        nameid.setText(matchName + " - "+resources.getString(R.string.match)+" #" + matchId);
        home.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), PlayActivity.class)));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), PlayActivity.class));
    }
}
