package com.app.clashv2.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.clashv2.R;
import com.app.clashv2.utils.AnalyticsUtil;
import com.app.clashv2.utils.LocaleHelper;
import com.google.firebase.analytics.FirebaseAnalytics;

public class SuccessOrderActivity extends AppCompatActivity {

    Button home;

    Context context;
    Resources resources;
    TextView ordersucessfulytitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_success_order);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        context = LocaleHelper.setLocale(SuccessOrderActivity.this);
        resources = context.getResources();

        ordersucessfulytitle = findViewById(R.id.ordersucessfulytitleid);

        ordersucessfulytitle.setText(resources.getString(R.string.order_placed_successfully));

        home=(Button)findViewById(R.id.ordersuccesshome);
        home.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(),PlayActivity.class)));
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(),PlayActivity.class));
    }
}