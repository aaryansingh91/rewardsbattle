//for otp verify when user register
package com.app.rewardsbattle.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.utils.AnalyticsUtil;
import com.app.rewardsbattle.utils.LoadingDialog;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.app.rewardsbattle.utils.UserLocalStore;
import com.google.firebase.FirebaseException;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mukesh.OtpView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OtpVerifyActivity extends AppCompatActivity {

    OtpView otpView;
    TextView resend;

    TextView verificationtitle;
    TextView otpnote;
    TextView otpnote2;
    LoadingDialog loadingDialog;
    RequestQueue mQueue;
    String userName, mobileNo, emailId, pass, cpass, promoCode, apiOtp, countryCode, firstName, lastName, firebasetoken;
    FirebaseAuth mAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;

    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_otp_verify);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        context = LocaleHelper.setLocale(OtpVerifyActivity.this);
        resources = context.getResources();

        countdown();
        mAuth = FirebaseAuth.getInstance();
        loadingDialog = new LoadingDialog(this);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d("FIREBASE TOKEN", "ERROR");
                        return;
                    } else {
                        Log.d("FIREBASE TOKEN", "LENGTH" + task.getResult().length() + " : " + task.getResult());
                    }

                    // Get new FCM registration token
                    firebasetoken = task.getResult();
                });

        SharedPreferences sppi = getSharedPreferences("PLAYER_ID", MODE_PRIVATE);
        SharedPreferences.Editor editor = sppi.edit();

        try {
            editor.putString("player_id", firebasetoken);
            editor.apply();
        }
        catch (Exception e) {

        }


        Intent intent = getIntent();
        userName = intent.getStringExtra("USER_NAME");
        firstName = intent.getStringExtra("FIRST_NAME");
        lastName = intent.getStringExtra("LAST_NAME");
        mobileNo = intent.getStringExtra("MOBILE_NO");
        emailId = intent.getStringExtra("EMAIL_ID");
        pass = intent.getStringExtra("PASS");
        cpass = intent.getStringExtra("CPASS");
        promoCode = intent.getStringExtra("PROMO_CODE");
        apiOtp = intent.getStringExtra("API_OTP");
        countryCode = intent.getStringExtra("COUNTRY_CODE");

        otpView = findViewById(R.id.otp_view);
        resend = findViewById(R.id.resend);

        verificationtitle = findViewById(R.id.verificationtitleid);
        otpnote = findViewById(R.id.otpnoteid);
        otpnote2 = findViewById(R.id.otpnote2id);

        otpnote2.setText(resources.getString(R.string.otp_note_2));
        otpnote.setText(resources.getString(R.string.otp_note));
        verificationtitle.setText(resources.getString(R.string.verification));

        resend.setClickable(false);
        resend.setEnabled(false);
        resend.setOnClickListener(v -> {
            sendotp();
        });

        otpView.setShowSoftInputOnFocus(true);
        otpView.setOtpCompletionListener(otp -> {

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(apiOtp, otp);
            signInWithPhoneAuthCredential(credential);
        });
        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                e.printStackTrace();
                Log.d("failed", e.getMessage());
                loadingDialog.dismiss();
                Toast.makeText(OtpVerifyActivity.this, "Something went wrong, Please try again", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                loadingDialog.dismiss();
                countdown();
                apiOtp = s;
                Toast.makeText(getApplicationContext(), resources.getString(R.string.otp_send_successfully), Toast.LENGTH_SHORT).show();
            }

        };
    }

    //show countdown
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

    //resend otp
    public void sendotp() {
        loadingDialog.show();
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(countryCode + mobileNo)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(OtpVerifyActivity.this)                 // Activity (for callback binding)
                        .setCallbacks(mCallback)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        resend.setClickable(false);
        resend.setEnabled(false);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInWithCredential:success");
                        registeruser(promoCode, userName, mobileNo, emailId, pass, cpass, "register");
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w("TAG", "signInWithCredential:failure", task.getException());
                        Toast.makeText(getApplicationContext(), resources.getString(R.string.failed), Toast.LENGTH_SHORT).show();
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(getApplicationContext(), resources.getString(R.string.wrong_otp), Toast.LENGTH_SHORT).show();
                            otpView.setText("");
                            // The verification code entered was invalid
                        }
                    }
                });
    }

    public void registeruser(final String promoCode, final String userNameEt, final String mobile_no, final String emailId, final String password, final String cPassword, final String submit) {

        loadingDialog.show();

        mQueue = Volley.newRequestQueue(getApplicationContext());
        //register api call for new user
        final String URL = resources.getString(R.string.api) + "registrationAcc";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("promo_code", promoCode);
        params.put("first_name", firstName);
        params.put("last_name", lastName);
        params.put("user_name", userNameEt);
        params.put("player_id", firebasetoken);
        params.put("mobile_no", mobile_no);
        params.put("email_id", emailId);
        params.put("password", password);
        params.put("cpassword", cPassword);
        params.put("country_code", countryCode);
        params.put("submit", submit);
        Log.d(URL, new JSONObject(params).toString());

        JsonObjectRequest request_json = new JsonObjectRequest(URL, new JSONObject(params),
                response -> {

                    Log.d("create", response.toString());
                    try {
                        String status = response.getString("status");
                        String message = response.getString("message");
                        final String token = response.getString("api_token");
                        if (TextUtils.equals(status, "true")) {

                            final String member_id = response.getString("member_id");
                            CurrentUser cUser = new CurrentUser(member_id, userNameEt, password, emailId, mobile_no, token, firstName, lastName);
                            final UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());
                            userLocalStore.storeUserData(cUser);
                            loadingDialog.dismiss();
                            Toast.makeText(OtpVerifyActivity.this, resources.getString(R.string.registration_successfully), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), PlayActivity.class));
                            
                        } else {
                            loadingDialog.dismiss();
                            mAuth.signOut();
                            Toast.makeText(OtpVerifyActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            mAuth.signOut();
            VolleyLog.e("Error: ", error.getMessage());
        }) {
            @Override
            protected Map<String, String> getParams() throws
                    AuthFailureError {
                return super.getParams();
            }

            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> headers = new HashMap<>();
                headers.put("x-localization", LocaleHelper.getPersist(context));

                return headers;
            }
        };

        request_json.setShouldCache(false);
        mQueue.add(request_json);
    }
}
