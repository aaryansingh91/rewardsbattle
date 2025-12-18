//for forgot password otp
package com.app.rewardsbattle.ui.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.utils.AnalyticsUtil;
import com.app.rewardsbattle.utils.LoadingDialog;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mukesh.OtpView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class FpOtpVerificationActivity extends AppCompatActivity {

    String ep = "";
    String memberid = "";
    String apiotp = "";
    OtpView otpView;
    TextView resend;
    TextView verificationtitle;
    TextView otpnote;
    TextView otpnote2;
    LoadingDialog loadingDialog;
    RequestQueue mQueue;

    Context context;
    Resources resources;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_fp_otp_verification);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        context = LocaleHelper.setLocale(FpOtpVerificationActivity.this);
        resources = context.getResources();

        countdown();
        loadingDialog=new LoadingDialog(this);

        Intent intent = getIntent();
        ep = intent.getStringExtra("EP");
        memberid = intent.getStringExtra("MID");
        apiotp = intent.getStringExtra("OTP");


        verificationtitle = findViewById(R.id.verificationtitleid);
        otpnote = findViewById(R.id.otpnoteid);
        otpnote2 = findViewById(R.id.otpnote2id);

        otpnote2.setText(resources.getString(R.string.otp_note_2));
        otpnote.setText(resources.getString(R.string.otp_note));
        verificationtitle.setText(resources.getString(R.string.verification));

        otpView = findViewById(R.id.otp_view_ft);
        resend = findViewById(R.id.resend_ft);
        resend.setClickable(false);
        resend.setEnabled(false);
        resend.setOnClickListener(v -> {
            resend.setClickable(false);
            resend.setEnabled(false);
            sendotp(ep);
        });

        otpView.setShowSoftInputOnFocus(true);
        otpView.setOtpCompletionListener(otp -> {
            if (TextUtils.equals(otp, apiotp)) {

                createnewpass(memberid);

            } else {
                Toast.makeText(getApplicationContext(), resources.getString(R.string.wrong_otp), Toast.LENGTH_SHORT).show();
                otpView.setText("");
            }
        });
    }

    public void countdown() {
        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                resend.setText(String.valueOf(millisUntilFinished / 1000));
                //here you can have your logic to set text to edittext
            }
            public void onFinish() {
                resend.setText(resources.getString(R.string.resend));
                resend.setClickable(true);
                resend.setEnabled(true);
            }

        }.start();
    }

    public void sendotp(String ep) {

        //sendOTP api call
        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();
        String url = resources.getString(R.string.api) + "sendOTP";
        HashMap<String, String> params = new HashMap<>();
        params.put("email_mobile", ep);

        final JsonObjectRequest request = new JsonObjectRequest(url, new JSONObject(params),
                response -> {
                    loadingDialog.dismiss();

                    try {
                        Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                        if (TextUtils.equals(response.getString("status"), "true")) {

                            apiotp = response.getString("otp");

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    loadingDialog.dismiss();

                    NetworkResponse response = error.networkResponse;
                    if (response != null && response.data != null) {
                        String errorString = new String(response.data);
                        try {
                            JSONObject obj = new JSONObject(errorString);
                            String message = obj.getString("message");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                        } catch (JSONException ignored) {

                        }
                    }
                }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return super.getParams();
            }

            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> headers = new HashMap<>();

                headers.put("x-localization", LocaleHelper.getPersist(context));
                return headers;
            }
        };
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, 0));
        mQueue.add(request);

    }

    public void createnewpass(final String memberid) {

        final Dialog builder = new Dialog(FpOtpVerificationActivity.this);
        builder.setCancelable(false);
        builder.setContentView(R.layout.final_resetpassword_layout);
        final EditText pass = builder.findViewById(R.id.resetnewpass);
        final EditText cpass = builder.findViewById(R.id.resetcnewpass);
        final Button submit = builder.findViewById(R.id.resetsubmit);
        Button cancel = builder.findViewById(R.id.resetcancel);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(pass.getText().toString().trim())) {
                    pass.setError(resources.getString(R.string.enter_new_password));
                    return;
                }
                if (TextUtils.isEmpty(cpass.getText().toString().trim())) {
                    cpass.setError(resources.getString(R.string.retype_new_password));
                    return;
                }
                if (!TextUtils.equals(pass.getText().toString().trim(), cpass.getText().toString().trim())) {
                    cpass.setError(resources.getString(R.string.password_not_match));
                    return;
                }

                loadingDialog.show();
                builder.dismiss();

                //forgotpassword api call
                mQueue = Volley.newRequestQueue(getApplicationContext());
                mQueue.getCache().clear();

                String url = resources.getString(R.string.api) + "forgotpassword";


                HashMap<String, String> params = new HashMap<>();
                params.put("member_id", memberid);
                params.put("submit", "forgotpass");
                params.put("password", pass.getText().toString().trim());
                params.put("cpassword", cpass.getText().toString().trim());

                final JsonObjectRequest request = new JsonObjectRequest(url, new JSONObject(params),
                        response -> {
                            loadingDialog.dismiss();

                            try {
                                Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                                if (TextUtils.equals(response.getString("status"), "true")) {
                                    Intent intent = new Intent(FpOtpVerificationActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }, error -> {

                            loadingDialog.dismiss();

                            NetworkResponse response = error.networkResponse;
                            if (response != null && response.data != null) {
                                String errorString = new String(response.data);
                                try {
                                    JSONObject obj = new JSONObject(errorString);
                                    String message = obj.getString("message");
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                                } catch (JSONException ignored) {

                                }
                            }
                        }){

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        return super.getParams();
                    }

                    @Override
                    public Map<String, String> getHeaders() {

                        Map<String, String> headers = new HashMap<>();

                        headers.put("x-localization", LocaleHelper.getPersist(context));
                        return headers;
                    }
                };
                request.setShouldCache(false);
                request.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, 0));
                mQueue.add(request);

            }
        });

        cancel.setOnClickListener(view -> {
            builder.dismiss();
            startActivity(new Intent(FpOtpVerificationActivity.this,MainActivity.class));
        });
        builder.show();
    }
}