//For register new user
package com.app.rewardsbattle.ui.activities;

import static com.android.volley.Request.Method.GET;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CreateNewAccount extends AppCompatActivity {
    EditText userNameEt, emailEt, mobileEt, passwordEt, promoCodeEt, confirmPasswordEt, firstNameEt, lastNameEt;
    TextView registertitle, signIn;
    Button registerNewAccount;
    RequestQueue mQueue;
    LoadingDialog loadingDialog;
    String firstName = "";
    String lastName = "";
    String userName = "";
    String email = "";
    String mobileNumber = "";
    String password = "";
    String confirmPasssword = "";
    String promoCode = "";
    FirebaseAuth mAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    TextView countrycodespinner;
    String countryCode = "";
    List<String> spinnerArray;
    List<String> spinnerArrayCountryCode;
    Context context;
    Resources resources;
    String fireabasetoken = "";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_create_new_account);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d("FIREBASE TOKEN ERROR", "ERROR");
                        return;
                    } else {
                        Log.d("FIREBASE TOKEN", "LENGTH" + task.getResult().length() + " : " + task.getResult());
                    }

                    // Get new FCM registration token
                    fireabasetoken = task.getResult();
                });

        context = LocaleHelper.setLocale(CreateNewAccount.this);
        resources = context.getResources();

        mAuth = FirebaseAuth.getInstance();
        loadingDialog = new LoadingDialog(this);


        firstNameEt = findViewById(R.id.register_firstname);
        lastNameEt = findViewById(R.id.register_lastname);
        userNameEt = findViewById(R.id.register_username);
        emailEt = findViewById(R.id.register_email);
        mobileEt = findViewById(R.id.register_mobilenumber);
        passwordEt = findViewById(R.id.register_password);
        promoCodeEt = findViewById(R.id.register_promocode);
        confirmPasswordEt = findViewById(R.id.register_confirmpassword);
        registerNewAccount = findViewById(R.id.registernewaccount);


        registertitle = findViewById(R.id.registertitleid);

        countrycodespinner = findViewById(R.id.countrycodespinnerregister);

        countrycodespinner.setHint(resources.getString(R.string.code));

        registerNewAccount.setText(resources.getString(R.string.SIGNUP));

        spinnerArray = new ArrayList<>();
        spinnerArrayCountryCode = new ArrayList<>();

        countrycodespinner.setOnClickListener(v -> {

            final Dialog builder = new Dialog(CreateNewAccount.this);
            builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            builder.setCancelable(false);
            WindowManager.LayoutParams wlmp = builder.getWindow().getAttributes();
            builder.getWindow().setAttributes(wlmp);
            builder.setContentView(R.layout.spinner_layout);

            ImageView cancel = builder.findViewById(R.id.spinnercancel);
            LinearLayout ll = builder.findViewById(R.id.spinneritemll);

            for (int i = 0; i < spinnerArray.size(); i++) {
                LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.spinner_item_layout, null);
                view.setLayoutParams(lparams);

                TextView tv = view.findViewById(R.id.tv);
                tv.setText(spinnerArrayCountryCode.get(i) + " (" + spinnerArray.get(i) + ")");
                final int finalI = i;
                tv.setOnClickListener(v12 -> {
                    countrycodespinner.setText(spinnerArrayCountryCode.get(finalI));
                    //countryidString=spinnerArrayid.get(finalI);
                    countryCode = spinnerArrayCountryCode.get(finalI);
                    builder.dismiss();
                });
                ll.addView(view);

            }
            cancel.setOnClickListener(v1 -> builder.dismiss());

            builder.create();
            builder.show();
        });

        viewallcountry();

        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();

        userNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(final CharSequence charSequence, int i, int i1, int i2) {
                charSequence.length();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        promoCodeEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.length() > 0) {

                } else {
                    registerNewAccount.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        signIn = findViewById(R.id.signin);
        signIn.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), MainActivity.class)));

        registerNewAccount.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                firstName = firstNameEt.getText().toString().trim();
                lastName = lastNameEt.getText().toString().trim();
                userName = userNameEt.getText().toString().trim();
                email = emailEt.getText().toString().trim();
                mobileNumber = mobileEt.getText().toString().trim();
                password = passwordEt.getText().toString().trim();
                confirmPasssword = passwordEt.getText().toString().trim();
                promoCode = promoCodeEt.getText().toString().trim();

                if (TextUtils.isEmpty(firstName)) {
                    firstNameEt.setError("First name required...");
                    return;
                }

                if (TextUtils.isEmpty(lastName)) {
                    lastNameEt.setError("Last name required...");
                    return;
                }

                if (TextUtils.isEmpty(userName)) {
                    userNameEt.setError(resources.getString(R.string.username_required___));
                    return;
                }
                if (userName.contains(" ")) {
                    userNameEt.setError(resources.getString(R.string.no_space_allowed));
                    return;
                }
                if (TextUtils.equals(countryCode.trim(), "")) {
                    Toast.makeText(getApplicationContext(), resources.getString(R.string.please_select_country_code), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(mobileNumber)) {
                    mobileEt.setError(resources.getString(R.string.mobile_number_required___));
                    return;
                }
                if (mobileNumber.length() < 7 || mobileNumber.length() > 15) {
                    mobileEt.setError(resources.getString(R.string.wrong_mobile_number___));
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    emailEt.setError(resources.getString(R.string.email_required___));
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEt.setError(resources.getString(R.string.wrong_email_address___));
                    return;
                }


                if (TextUtils.isEmpty(password)) {
                    passwordEt.setError(resources.getString(R.string.password_required___));
                    return;
                }
                if (TextUtils.isEmpty(confirmPasssword)) {
                    confirmPasswordEt.setError(resources.getString(R.string.retype_your_password___));
                    return;
                }

                if (!TextUtils.equals(password, confirmPasssword)) {
                    confirmPasswordEt.setError(resources.getString(R.string.password_not_matched___));
                    return;
                }

                if (password.length() < 6) {
                    passwordEt.setError("Password must be at least 6 characters");
                    passwordEt.requestFocus();
                    return;
                }

                SharedPreferences sp = getSharedPreferences("SMINFO", MODE_PRIVATE);
                if (TextUtils.equals(sp.getString("otp", "no"), "no")) {

                    registeruser(firstName, lastName, promoCode, userName, mobileNumber, email, password, confirmPasssword, "register");

                } else {
                    //callback method for phone number verification
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
                            Toast.makeText(CreateNewAccount.this, "Something went wrong, Please try again", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            super.onCodeSent(s, forceResendingToken);

                            Toast.makeText(getApplicationContext(), resources.getString(R.string.otp_send_successfully), Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                            Intent intent = new Intent(getApplicationContext(), OtpVerifyActivity.class);
                            intent.putExtra("FIRST_NAME", firstName);
                            intent.putExtra("LAST_NAME", lastName);
                            intent.putExtra("USER_NAME", userName);
                            intent.putExtra("MOBILE_NO", mobileNumber);
                            intent.putExtra("EMAIL_ID", email);
                            intent.putExtra("PASS", password);
                            intent.putExtra("CPASS", confirmPasssword);
                            intent.putExtra("PROMO_CODE", promoCode);
                            intent.putExtra("API_OTP", s);
                            intent.putExtra("COUNTRY_CODE", countryCode);
                            startActivity(intent);
                        }
                    };
                    sendotp(promoCode, userName, mobileNumber, email, password, confirmPasssword, "register");
                }

            }
        });
    }

    public void sendotp(final String promoCode, final String userNameEt, final String mobile_no, final String emailId, final String password, final String cpassword, final String submit) {

        loadingDialog.show();

        //checkMember api call for new user
        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();

        final String URL = resources.getString(R.string.api) + "checkMember";
        HashMap<String, String> params = new HashMap<>();
        params.put("promo_code", promoCode);
        params.put("user_name", userNameEt);
        params.put("mobile_no", mobile_no);
        params.put("country_code", countryCode);
        params.put("email_id", emailId);
        params.put("password", password);
        params.put("cpassword", cpassword);
        params.put("submit", submit);

        Log.d(URL, new JSONObject(params).toString());

        JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, URL, new JSONObject(params),
                response -> {


                    Log.d("send otp", response.toString());
                    try {
                        String status = response.getString("status");

                        if (TextUtils.equals(status, "true")) {

                            Toast.makeText(getApplicationContext(), resources.getString(R.string.otp_send_successfully), Toast.LENGTH_SHORT).show();

                            PhoneAuthOptions options =
                                    PhoneAuthOptions.newBuilder(mAuth)
                                            .setPhoneNumber(countryCode + mobile_no)       // Phone number to verify
                                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                            .setActivity(CreateNewAccount.this)                 // Activity (for callback binding)
                                            .setCallbacks(mCallback)          // OnVerificationStateChangedCallbacks
                                            .build();
                            PhoneAuthProvider.verifyPhoneNumber(options);
                        } else {
                            loadingDialog.dismiss();
                            Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            VolleyLog.e("Error: ", error.getMessage());
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                String errorString = new String(response.data);
                Log.d("erorostring ", errorString);
            }
        }) {
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

        request_json.setShouldCache(false);
        mQueue.add(request_json);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInWithCredential:success");
                        registeruser(firstName, lastName, promoCode, userName, mobileNumber, email, password, confirmPasssword, "register");

                        // ...
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w("TAG", "signInWithCredential:failure", task.getException());
                        Toast.makeText(getApplicationContext(), resources.getString(R.string.failed), Toast.LENGTH_SHORT).show();
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                        }
                    }
                });

    }

//
//    public void registeruser(String firstName, String lastName, final String promoCode, final String userNameEt, final String mobile_no, final String emailId, final String password, final String cPassword, final String submit) {
//
//        loadingDialog.show();
//
//        //register api call for new user
//        final String URL = resources.getString(R.string.api) + "registrationAcc";
//        HashMap<String, String> params = new HashMap<>();
//        params.put("promo_code", promoCode);
//        params.put("first_name", firstName);
//        params.put("last_name", lastName);
//        params.put("user_name", userNameEt);
//        params.put("mobile_no", mobile_no);
//        params.put("email_id", emailId);
//        params.put("player_id", fireabasetoken);
//        params.put("password", password);
//        params.put("cpassword", cPassword);
//        params.put("country_code", countryCode);
//        params.put("submit", submit);
//        Log.d(URL, new JSONObject(params).toString());
//
//        JsonObjectRequest request_json = new JsonObjectRequest(URL, new JSONObject(params),
//                response -> {
//
//
//                    Log.d("create", response.toString());
//                    try {
//                        String status = response.getString("status");
//                        String message = response.getString("message");
//
//                        if (TextUtils.equals(status, "true")) {
//                            final String token = response.getString("api_token");
//                            final String member_id = response.getString("member_id");
//
//                            CurrentUser cUser = new CurrentUser(member_id, userNameEt, password, emailId, mobile_no, token, firstName, lastName);
//                            final UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());
//                            userLocalStore.storeUserData(cUser);
//                            loadingDialog.dismiss();
//                            Toast.makeText(CreateNewAccount.this, resources.getString(R.string.registration_successfully), Toast.LENGTH_SHORT).show();
//                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
//
//                        } else {
//                            mAuth.signOut();
//                            loadingDialog.dismiss();
//                            Toast.makeText(CreateNewAccount.this, message, Toast.LENGTH_SHORT).show();
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }, error -> {
//            mAuth.signOut();
//            VolleyLog.e("Error: ", error.getMessage());
//        }) {
//
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                return super.getParams();
//            }
//
//            @Override
//            public Map<String, String> getHeaders() {
//
//                Map<String, String> headers = new HashMap<>();
//
//                headers.put("x-localization", LocaleHelper.getPersist(context));
//
//                return headers;
//            }
//        };
//
//        request_json.setShouldCache(false);
//        mQueue.add(request_json);
//    }


//    public void registeruser(String firstName, String lastName, final String promoCode, final String userNameEt, final String mobile_no, final String emailId, final String password, final String cPassword, final String submit) {
//
//        loadingDialog.show();
//
//        //register api call for new user
//        final String URL = resources.getString(R.string.api) + "registrationAcc";
//        HashMap<String, String> params = new HashMap<>();
//        params.put("promo_code", promoCode);
//        params.put("first_name", firstName);
//        params.put("last_name", lastName);
//        params.put("user_name", userNameEt);
//        params.put("mobile_no", mobile_no);
//        params.put("email_id", emailId);
//        params.put("player_id", fireabasetoken);
//        params.put("password", password);
//        params.put("cpassword", cPassword);
//        params.put("country_code", countryCode);
//        params.put("submit", submit);
//        Log.d(URL, new JSONObject(params).toString());
//
//        JsonObjectRequest request_json = new JsonObjectRequest(URL, new JSONObject(params),
//                response -> {
//
//
//                    Log.d("create", response.toString());
//                    try {
//                        String status = response.getString("status");
//                        String message = response.getString("message");
//
//                        if (TextUtils.equals(status, "true")) {
//                            final String token = response.getString("api_token");
//                            final String member_id = response.getString("member_id");
//
//                            CurrentUser cUser = new CurrentUser(member_id, userNameEt, password, emailId, mobile_no, token, firstName, lastName);
//                            final UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());
//                            userLocalStore.storeUserData(cUser);
//                            loadingDialog.dismiss();
//                            Toast.makeText(CreateNewAccount.this, resources.getString(R.string.registration_successfully), Toast.LENGTH_SHORT).show();
//                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
//
//                        } else {
//                            mAuth.signOut();
//                            loadingDialog.dismiss();
//                            Toast.makeText(CreateNewAccount.this, message, Toast.LENGTH_SHORT).show();
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }, error -> {
//            mAuth.signOut();
//            VolleyLog.e("Error: ", error.getMessage());
//        }) {
//
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                return super.getParams();
//            }
//
//            @Override
//            public Map<String, String> getHeaders() {
//
//                Map<String, String> headers = new HashMap<>();
//
//                headers.put("x-localization", LocaleHelper.getPersist(context));
//
//                return headers;
//            }
//        };
//
//        request_json.setShouldCache(false);
//        mQueue.add(request_json);
//    }

    public void registeruser(String firstName, String lastName, final String promoCode, final String userNameEt,
                             final String mobile_no, final String emailId, final String password,
                             final String cPassword, final String submit) {

        // Show loading dialog
        loadingDialog.show();

        // API URL
        final String URL = resources.getString(R.string.api) + "registrationAcc";

        // Parameters to send
        HashMap<String, String> params = new HashMap<>();
        params.put("promo_code", promoCode);
        params.put("first_name", firstName);
        params.put("last_name", lastName);
        params.put("user_name", userNameEt);
        params.put("mobile_no", mobile_no);
        params.put("email_id", emailId);
        params.put("player_id", fireabasetoken); // Ensure fireabasetoken has a valid value
        params.put("password", password);
        params.put("cpassword", cPassword);
        params.put("country_code", countryCode); // Ensure countryCode has a valid value
        params.put("submit", submit);

        try {
            // Log request data for debugging
            Log.d("Request URL", URL);
            Log.d("Request Params", new JSONObject(params).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // JSON Object Request
        JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, URL, new JSONObject(params),
                response -> {
                    try {
                        Log.d("API Response", response.toString());

                        // Parse the response
                        String status = response.getString("status");
                        String message = response.getString("message");

                        if (TextUtils.equals(status, "true")) {
                            // Successful registration
                            String token = response.getString("api_token");
                            String member_id = response.getString("member_id");

                            // Save user data locally
                            CurrentUser cUser = new CurrentUser(member_id, userNameEt, password, emailId, mobile_no, token, firstName, lastName);
                            UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());
                            userLocalStore.storeUserData(cUser);

                            loadingDialog.dismiss();
                            Toast.makeText(CreateNewAccount.this, resources.getString(R.string.registration_successfully), Toast.LENGTH_SHORT).show();

                            // Navigate to HomeActivity
                            startActivity(new Intent(getApplicationContext(), PlayActivity.class));
                        } else {
                            // Registration failed
                            mAuth.signOut();
                            loadingDialog.dismiss();
                            Toast.makeText(CreateNewAccount.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        // JSON parsing error
                        e.printStackTrace();
                        Toast.makeText(CreateNewAccount.this, "Error parsing server response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle Volley errors
                    loadingDialog.dismiss();
                    mAuth.signOut();

                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String errorMessage = new String(error.networkResponse.data);
                        Log.e("API Error", errorMessage);
                        Toast.makeText(CreateNewAccount.this, "Server Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("API Error", "Unknown server error", error);
                        Toast.makeText(CreateNewAccount.this, "Unknown error occurred.", Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() {
                // Set headers for the API request
                Map<String, String> headers = new HashMap<>();
                headers.put("x-localization", LocaleHelper.getPersist(context));
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Disable caching
        request_json.setShouldCache(false);

        // Add request to queue
        mQueue.add(request_json);
    }

    public void viewallcountry() {

        /*all_country api call start*/
        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();
        String url = resources.getString(R.string.api) + "all_country";
        final UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());

        final JsonObjectRequest request = new JsonObjectRequest(GET, url, null,
                response -> {
                    loadingDialog.dismiss();
                    try {
                        JSONArray arr = response.getJSONArray("all_country");

                        if (!TextUtils.equals(response.getString("all_country"), "[]")) {
                            //noUpcoming.setVisibility(View.GONE);
                        } else {
                            //noUpcoming.setVisibility(View.VISIBLE);
                        }
                        JSON_PARSE_DATA_AFTER_WEBCALL(arr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    loadingDialog.dismiss();
                }, error -> Log.e("**VolleyError", "error" + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return super.getParams();
            }

            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> headers = new HashMap<>();
                CurrentUser user = userLocalStore.getLoggedInUser();
                String credentials = user.getUsername() + ":" + user.getPassword();
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                headers.put("x-localization", LocaleHelper.getPersist(context));
                return headers;
            }
        };
        request.setShouldCache(false);
        mQueue.add(request);
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array) {

        for (int i = 0; i < array.length(); i++) {
            JSONObject json;
            try {
                json = array.getJSONObject(i);

                spinnerArray.add(json.getString("country_name"));
                spinnerArrayCountryCode.add(json.getString("p_code"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
