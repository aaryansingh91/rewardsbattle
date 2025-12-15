package com.app.clashv2.ui.activities;

import android.content.Context;
import android.content.Intent;
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

import com.app.clashv2.R;
import com.app.clashv2.utils.CustomTypefaceSpan;
import com.app.clashv2.utils.LocaleHelper;

public class TansactionSuccessActivity extends AppCompatActivity {
    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tansaction_success2);
        context = LocaleHelper.setLocale(TansactionSuccessActivity.this);
        resources = context.getResources();

        Button home = findViewById(R.id.successthome);
        TextView successtid = findViewById(R.id.successtid);
        TextView successtamount = findViewById(R.id.successtamount);
        TextView successstatus = findViewById(R.id.succesststatus);

        successstatus.setText(resources.getString(R.string.your_transaction_is_success));


        Intent intent = getIntent();
        String tid = intent.getStringExtra("TID");
        String tamount = intent.getStringExtra("TAMOUNT");
        String selectedcurrency = intent.getStringExtra("selected");

        successtid.setText(resources.getString(R.string.transaction_id) + " : #" + tid);

        successtamount.setText(resources.getString(R.string.transaction_amount_is) + " : " + selectedcurrency + tamount);
        if (TextUtils.equals(selectedcurrency, "₹")) {
            Typeface font = Typeface.DEFAULT_BOLD;
            SpannableStringBuilder SS = new SpannableStringBuilder(resources.getString(R.string.Rs));
            SS.setSpan(new CustomTypefaceSpan("", font), 0, 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            successtamount.setText(TextUtils.concat(resources.getString(R.string.transaction_amount_is) + " : ", SS, tamount));
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