//for offline payment
package com.app.clashv2.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.clashv2.R;
import com.app.clashv2.utils.AnalyticsUtil;
import com.app.clashv2.utils.LocaleHelper;
import com.app.clashv2.utils.URLImageParser;
import com.google.firebase.analytics.FirebaseAnalytics;

public class OfflinePaymentActivity extends AppCompatActivity {

    ImageView back;
    TextView paymentDesc;
    TextView paymentdesctitle;

    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_offline_payment);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        context = LocaleHelper.setLocale(OfflinePaymentActivity.this);
        resources = context.getResources();

        paymentdesctitle = findViewById(R.id.payment_desctitleid);

        paymentdesctitle.setText(resources.getString(R.string.payment_desc));

        back = findViewById(R.id.backfromoffline);
        back.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), AddMoneyActivity.class);
            startActivity(intent);
        });

        Intent intent=getIntent();
        String payDesc=intent.getStringExtra("paymentdesc");
        paymentDesc= findViewById(R.id.payment_desc);
        Log.d("paydesc",payDesc);

        paymentDesc.setText(Html.fromHtml(payDesc,new URLImageParser(paymentDesc,this),null));
        paymentDesc.setClickable(true);
        paymentDesc.setMovementMethod(LinkMovementMethod.getInstance());

        Intent intent1 = new Intent();
        intent1.setAction("reload");
        sendBroadcast(intent1);
    }
}
