// For after failed transaction
package com.app.rewardsbattle.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.rewardsbattle.R;
import com.app.rewardsbattle.utils.AnalyticsUtil;
import com.app.rewardsbattle.utils.CustomTypefaceSpan;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.google.firebase.analytics.FirebaseAnalytics;

public class TransactionFailActivity extends AppCompatActivity {

    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_transaction_fail);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        context = LocaleHelper.setLocale(TransactionFailActivity.this);
        resources = context.getResources();

        Button home = findViewById(R.id.failthome);
        TextView failtid = findViewById(R.id.failtid);
        TextView failtamount = findViewById(R.id.failtamount);
        TextView failstatus = findViewById(R.id.failtstatus);

        failstatus.setText(resources.getString(R.string.your_transaction_is_fail));

        Intent intent = getIntent();
        String tid = intent.getStringExtra("TID");
        String tamount = intent.getStringExtra("TAMOUNT");
        String selectedcurrency = intent.getStringExtra("selected");

        failtid.setText(resources.getString(R.string.transaction_id) + " : #" + tid);
        failtamount.setText(resources.getString(R.string.transaction_amount_is) + " : " + selectedcurrency + tamount);
        if (TextUtils.equals(selectedcurrency, "₹")) {
            Typeface font = Typeface.DEFAULT_BOLD;
            SpannableStringBuilder SS = new SpannableStringBuilder(resources.getString(R.string.Rs));
            SS.setSpan(new CustomTypefaceSpan("", font), 0, 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            failtamount.setText(TextUtils.concat(resources.getString(R.string.transaction_amount_is) + " : ", SS, tamount));
        }
        home.setText(resources.getString(R.string.home));
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(getApplicationContext(), PlayActivity.class));
            }
        });

        Intent intent1 = new Intent();
        intent1.setAction("reload");
        sendBroadcast(intent1);
    }

}
