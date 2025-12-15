//For after successfully complete payment
package com.app.clashv2.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.clashv2.R;
import com.app.clashv2.utils.AnalyticsUtil;
import com.app.clashv2.utils.CustomTypefaceSpan;
import com.app.clashv2.utils.LocaleHelper;
import com.google.firebase.analytics.FirebaseAnalytics;

public class TransactionSuccessActivity extends AppCompatActivity {

    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_tansaction_success);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        context = LocaleHelper.setLocale(TransactionSuccessActivity.this);
        resources = context.getResources();

        Button home = findViewById(R.id.successthome);
        TextView successTid = findViewById(R.id.successtid);
        TextView successTAmount = findViewById(R.id.successtamount);
        TextView successStatus = findViewById(R.id.succesststatus);

        successStatus.setText(resources.getString(R.string.your_transaction_is_success));


        Intent intent = getIntent();
        String tid = intent.getStringExtra("TID");
        String tamount = intent.getStringExtra("TAMOUNT");
        String selectedcurrency=intent.getStringExtra("selected");

        successTid.setText(resources.getString(R.string.transaction_id)+" : #" + tid);

        successTAmount.setText(resources.getString(R.string.transaction_amount_is)+" : " + selectedcurrency + tamount);
        if (TextUtils.equals(selectedcurrency, "₹")) {
            Typeface font = Typeface.DEFAULT_BOLD;
            SpannableStringBuilder SS = new SpannableStringBuilder(resources.getString(R.string.Rs));
            SS.setSpan(new CustomTypefaceSpan("", font), 0, 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            successTAmount.setText(TextUtils.concat(resources.getString(R.string.transaction_amount_is)+" : ", SS, tamount));
        }

        home.setText(resources.getString(R.string.home));
        home.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), MyWalletActivity.class)));

        Intent intent1 = new Intent();
        intent1.setAction("reload");
        sendBroadcast(intent1);
    }
}
