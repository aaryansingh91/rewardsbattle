// For Login
package com.app.clashv2.ui.activities;

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
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
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
import androidx.cardview.widget.CardView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.clashv2.R;
import com.app.clashv2.models.CurrentUser;
import com.app.clashv2.utils.AnalyticsUtil;
import com.app.clashv2.utils.LoadingDialog;
import com.app.clashv2.utils.LocaleHelper;
import com.app.clashv2.utils.UserLocalStore;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mukesh.OtpView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    TextView logintitle, createNewAccount;
    Button signinMain;
    EditText userNameMain, passwordMain;
    TextView resetPassword;
    Boolean doubleBackToExitPressedOnce = false;
    RequestQueue mQueue;
    UserLocalStore userLocalStore;
    LoadingDialog loadingDialog;
    String firstname = "";
    String lastname = "";
    int RC_SIGN_IN = 23;
    CardView googlecv;
    List<String> spinnerArrayCountryCode;
    List<String> spinnerArrayCountryName;
    List<String> spinnerArrayCountryId;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    Dialog builder;
    OtpView otpView;
    TextView mobileview;
    TextView resend;
    String selectedCode = "";
    String selectedMobile = "";
    String selectedMemberid = "";
    String selectedUsername = "";
    String selectedId = "";
    String selectedEmail = "";
    String selectedApitoken = "";
    String selectedCountryId = "";
    String selectedPromo = "";
    String selectedFirstname = "";
    String selectedLastname = "";
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    String fireabasetoken = "";
    Context context;
    Resources resources;
    String playerId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d("FB TOKEN EXCEPTION", "ERROR");
                        return;
                    } else {
                        Log.d("FIREBASE TOKEN", "LENGTH" + task.getResult().length() + " : " + task.getResult());
                    }

                    // Get new FCM registration token
                    fireabasetoken = task.getResult();
                });

        SharedPreferences sppi = getSharedPreferences("PLAYER_ID", MODE_PRIVATE);
        SharedPreferences.Editor editorpi = sppi.edit();
        try {
            editorpi.putString("player_id", fireabasetoken);
            editorpi.apply();
            playerId = fireabasetoken;
            while (TextUtils.equals(fireabasetoken, "null")) {
                playerId = fireabasetoken;
            }

        } catch (Exception ignored) {

        }

        context = LocaleHelper.setLocale(MainActivity.this);
        resources = context.getResources();

        logintitle = findViewById(R.id.logintitleid);

        spinnerArrayCountryCode = new ArrayList<>();
        spinnerArrayCountryName = new ArrayList<>();
        spinnerArrayCountryId = new ArrayList<>();


        builder = new Dialog(MainActivity.this);
        builder.setContentView(R.layout.mobile_otp_dialog);
        mobileview = builder.findViewById(R.id.mobileotptv);
        otpView = builder.findViewById(R.id.otp_view_verifymobile);
        resend = builder.findViewById(R.id.resend_verifymobile);

        viewallcountry();

        SharedPreferences.Editor editor = getSharedPreferences("first_time", MODE_PRIVATE).edit();
        editor.putBoolean("f_t", false);
        editor.apply();

        googlecv = findViewById(R.id.googlecv);

        //check baner ads enable or not
        SharedPreferences sp = getSharedPreferences("SMINFO", MODE_PRIVATE);
        if (TextUtils.equals(sp.getString("google", "no"), "no")) {
            googlecv.setVisibility(View.GONE);
        } else {
            googlecv.setVisibility(View.VISIBLE);
        }
        loadingDialog = new LoadingDialog(this);

        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();
        userLocalStore = new UserLocalStore(this);

        createNewAccount = findViewById(R.id.createnewaccount);
        signinMain = findViewById(R.id.signin_main);
        userNameMain = findViewById(R.id.username_main);
        passwordMain = findViewById(R.id.password_main);
        resetPassword = findViewById(R.id.resetpassword);


        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googlecv.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });


        resetPassword.setOnClickListener(view -> resetPassword());

        createNewAccount.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), CreateNewAccount.class)));

        signinMain.setOnClickListener(view -> {

            String password = passwordMain.getText().toString();
            String username = userNameMain.getText().toString();

            if (TextUtils.isEmpty(userNameMain.getText().toString())) {
                userNameMain.setError(resources.getString(R.string.username_required___));
                return;
            }
            if (TextUtils.isEmpty(password)) {
                passwordMain.setError(resources.getString(R.string.password_required___));
                return;
            }

            loginUser(username, password, "login");
        });

        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                loadingDialog.dismiss();
                updateMobile(selectedMemberid, selectedUsername, selectedId, selectedEmail, selectedApitoken, selectedCode, selectedMobile, selectedCountryId, selectedPromo, selectedFirstname, selectedLastname);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                e.printStackTrace();
                Log.d("failed", e.getMessage());
                loadingDialog.dismiss();
                Toast.makeText(MainActivity.this, "Something went wrong, Please try again", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                loadingDialog.dismiss();
                Log.d("codesent", s);
                Toast.makeText(getApplicationContext(), resources.getString(R.string.otp_send_successfully), Toast.LENGTH_SHORT).show();
                openotpdialog(s);
            }

        };

    }

    public void resetPassword() {

        final Dialog builder = new Dialog(MainActivity.this);
        builder.setContentView(R.layout.resetpassword);
        final EditText fpemail = builder.findViewById(R.id.fpemail);
        final Button fpsendemail = builder.findViewById(R.id.fpsendemail);
        Button fpcancel = builder.findViewById(R.id.fpcancel);

        fpsendemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(fpemail.getText().toString())) {
                    fpemail.setError("Enter Email or Mobile No.");
                } else {
                    fpsendemail.setEnabled(false);
                    loadingDialog.show();
                    builder.dismiss();

                    //call forgot-password api
                    mQueue = Volley.newRequestQueue(getApplicationContext());
                    mQueue.getCache().clear();

                    String url = resources.getString(R.string.api) + "sendOTP";

                    HashMap<String, String> params = new HashMap<>();
                    params.put("email_mobile", fpemail.getText().toString().trim());

                    final JsonObjectRequest request = new JsonObjectRequest(url, new JSONObject(params),
                            response -> {
                                loadingDialog.dismiss();

                                Log.d("fpresponse", response.toString());
                                try {
                                    Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                                    if (TextUtils.equals(response.getString("status"), "true")) {
                                        Intent intent = new Intent(MainActivity.this, FpOtpVerificationActivity.class);
                                        intent.putExtra("EP", fpemail.getText().toString().trim());
                                        intent.putExtra("MID", response.getString("member_id"));
                                        intent.putExtra("OTP", response.getString("otp"));
                                        startActivity(intent);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }, error -> Log.e("**VolleyError", "error" + error.getMessage())) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            return super.getParams();
                        }

                        @Override
                        public Map<String, String> getHeaders() {

                            Map<String, String> headers = new HashMap<>();
                            headers.put("Content-Type", "application/json");
                            headers.put("x-localization", LocaleHelper.getPersist(context));
                            return headers;
                        }
                    };
                    request.setShouldCache(false);
                    request.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, 0));
                    mQueue.add(request);
                }
            }
        });

        fpcancel.setOnClickListener(view -> builder.dismiss());
        builder.show();
    }

    public void loginUser(final String user_name, final String password, final String submit) {

        loadingDialog.show();
        //call login api
        final String URL = resources.getString(R.string.api) + "login";

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("user_name", user_name);
        params.put("password", password);
        params.put("player_id", fireabasetoken);
        params.put("submit", submit);


        Log.d(URL, new JSONObject(params).toString());
        JsonObjectRequest request_json = new JsonObjectRequest(URL, new JSONObject(params),
                response -> {
                    Log.d("login response", response.toString());

                    try {
                        String status = response.getString("status");
                        String message = response.getString("message");
                        final JSONObject msgobj = response.getJSONObject("message");
                        final String token = msgobj.getString("api_token");
                        if (TextUtils.equals(status, "true")) {


                            JSONObject obj = new JSONObject(message);
                            String member_id = obj.getString("member_id");
                            String email = obj.getString("email_id");
                            String phone = obj.getString("mobile_no");
                            String firstname = obj.getString("first_name");
                            String lastName = obj.getString("last_name");
                            CurrentUser cUser = new CurrentUser(member_id, user_name, password, email, phone, token, firstname, lastName);
                            final UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());
                            userLocalStore.storeUserData(cUser);

                            SharedPreferences sppi = getSharedPreferences("PLAYER_ID", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sppi.edit();
                            try {
                                editor.putString("player_id", fireabasetoken);
                                editor.apply();
                            } catch (Exception ignored) {

                            }
                            loadingDialog.dismiss();

                            Toast.makeText(MainActivity.this, resources.getString(R.string.login_successfully), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), PlayActivity.class));


                        } else {

                            loadingDialog.dismiss();
                        }
                    } catch (JSONException e) {
                        loadingDialog.dismiss();

                        try {
                            String message = response.getString("message");
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }

                        e.printStackTrace();
                    }
                }, error -> VolleyLog.e("Error: ", error.getMessage())) {
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

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAffinity();
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, resources.getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 1500);
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent data) {
        super.onActivityResult(requestCode, responseCode, data);

        loadingDialog.show();
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                loadingDialog.dismiss();
                Log.w("google failed", "Google sign in failed", e);
            }
        } else {
            loadingDialog.dismiss();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("google succesws", "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        assert user != null;
                        Log.d("google name", user.getDisplayName());
                        Log.d("google email", user.getEmail());
                        Log.d("google uuid", user.getUid());
                        String[] name = Objects.requireNonNull(user.getDisplayName()).split("\\s+");

                        Log.d("google firstname", name[0]);
                        //Log.d("google lastname", name[1]);
                        String p_ = "";
                        try {
                            p_ = user.getPhoneNumber();
                            Log.d("google phone number", user.getPhoneNumber());
                        } catch (Exception e) {
                            p_ = "";
                        }
                        if (TextUtils.equals(p_, null)) {
                            p_ = "";
                        }
                        if (name.length == 1) {
                            RegisterGoogle(name[0], "", user.getDisplayName(), user.getEmail(), p_, user.getUid(), "google_login");
                        } else {
                            RegisterGoogle(name[0], name[1], user.getDisplayName(), user.getEmail(), p_, user.getUid(), "google_login");
                        }

                        SharedPreferences sppi = getSharedPreferences("PLAYER_ID", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sppi.edit();
                        try {
                            editor.putString("player_id", fireabasetoken);
                            editor.apply();
                        } catch (Exception ignored) {

                        }

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("google err final", "signInWithCredential:failure", task.getException());

                        SharedPreferences sppi = getSharedPreferences("PLAYER_ID", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sppi.edit();
                        try {
                            editor.putString("player_id", fireabasetoken);
                            editor.apply();
                        } catch (Exception ignored) {

                        }
                    }

                    // ...
                });
    }

    public void RegisterGoogle(final String first_name, final String last_name, final String user_name, final String email, final String phone, final String gid, final String submit) {

        loadingDialog.show();
        //call login api
        final String URL = resources.getString(R.string.api) + "registerGoogle";

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("first_name", first_name);
        params.put("last_name", last_name);
        params.put("user_name", first_name);
        params.put("player_id", fireabasetoken);
        params.put("email_id", email);
        params.put("mobile_no", phone);
        params.put("g_id", gid);
        params.put("submit", submit);


        Log.d(URL, new JSONObject(params).toString());
        JsonObjectRequest request_json = new JsonObjectRequest(URL, new JSONObject(params),
                response -> {

                    Log.d("google response", response.toString());

                    try {
                        String status = response.getString("status");
                        String message = response.getString("message");
                        if (TextUtils.equals(status, "true")) {

                            if (TextUtils.equals(response.getJSONObject("message").getString("mobile_no"), "")) {
                                // member id. username, gid,email,phone,apitoken
                                loadingDialog.dismiss();
                                final String member_id = response.getJSONObject("message").getString("member_id");

                                openMobileDialog(member_id, response.getJSONObject("message").getString("user_name"), gid, response.getJSONObject("message").getString("email_id"), response.getJSONObject("message").getString("api_token"), response.getJSONObject("message").getString("new_user"));
                            } else {
                                String member_id = response.getJSONObject("message").getString("member_id");

                                CurrentUser cUser = new CurrentUser(member_id, response.getJSONObject("message").getString("user_name"), gid, email, response.getJSONObject("message").getString("mobile_no"), response.getJSONObject("message").getString("api_token"), first_name, last_name);
                                final UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());
                                userLocalStore.storeUserData(cUser);

                                loadingDialog.dismiss();
                                Toast.makeText(MainActivity.this, resources.getString(R.string.login_successfully), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), PlayActivity.class));

                            }
                        } else {
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    } catch (JSONException e) {
                        loadingDialog.dismiss();
                        e.printStackTrace();
                    }
                }, error -> {
            VolleyLog.e("Error: ", error.getMessage());

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


    @SuppressLint("SetTextI18n")
    public void openMobileDialog(final String memberid, final String username, final String id, final String email, final String apitoken, String newuser) {

        final Dialog builder = new Dialog(MainActivity.this);
        builder.setContentView(R.layout.enter_mobile_dialog);
        final TextView countrycode = builder.findViewById(R.id.countrycodespinnerem);
        final TextView mobilenumber = builder.findViewById(R.id.em_mobilenumber);
        final TextView promocode = builder.findViewById(R.id.first_promocode);
        TextInputLayout promocodetil = builder.findViewById(R.id.firstpromoTil);
        Button submit = builder.findViewById(R.id.em_submit);

        if (TextUtils.equals(newuser, "Yes")) {
            promocodetil.setVisibility(View.VISIBLE);
        } else {
            promocodetil.setVisibility(View.GONE);
        }

        countrycode.setOnClickListener(v -> {

            final Dialog builder1 = new Dialog(MainActivity.this);
            builder1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            builder1.setCancelable(false);
            WindowManager.LayoutParams wlmp = builder1.getWindow().getAttributes();
            builder1.getWindow().setAttributes(wlmp);
            builder1.setContentView(R.layout.spinner_layout);

            ImageView cancel = builder1.findViewById(R.id.spinnercancel);
            LinearLayout ll = builder1.findViewById(R.id.spinneritemll);

            for (int i = 0; i < spinnerArrayCountryCode.size(); i++) {
                LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                View view = getLayoutInflater().inflate(R.layout.spinner_item_layout, null);
                view.setLayoutParams(lparams);

                TextView tv = view.findViewById(R.id.tv);
                tv.setText(spinnerArrayCountryCode.get(i) + " (" + spinnerArrayCountryName.get(i) + ")");
                final int finalI = i;
                tv.setOnClickListener(v12 -> {
                    final TextView t = (TextView) v12;
                    countrycode.setText(spinnerArrayCountryCode.get(finalI));
                    selectedCode = spinnerArrayCountryCode.get(finalI);
                    selectedCountryId = spinnerArrayCountryId.get(finalI);


                    builder1.dismiss();
                });
                ll.addView(view);

            }
            cancel.setOnClickListener(v1 -> builder1.dismiss());

            builder1.create();
            builder1.show();

        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.equals(selectedCode.trim(), "")) {
                    Toast.makeText(getApplicationContext(), resources.getString(R.string.please_select_country_code), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(mobilenumber.getText().toString().trim())) {
                    mobilenumber.setError(resources.getString(R.string.mobile_number_required___));
                    return;
                }
                if (mobilenumber.getText().toString().trim().length() < 7 || mobilenumber.getText().toString().trim().length() > 15) {
                    mobilenumber.setError(resources.getString(R.string.wrong_mobile_number___));
                    return;
                }


                loadingDialog.show();

                selectedMobile = mobilenumber.getText().toString().trim();
                selectedPromo = promocode.getText().toString().trim();
                selectedMemberid = memberid;
                selectedUsername = username;
                selectedId = id;
                selectedEmail = email;
                selectedApitoken = apitoken;

                SharedPreferences sp = getSharedPreferences("SMINFO", MODE_PRIVATE);
                if (TextUtils.equals(sp.getString("otp", "no"), "no")) {
                    builder.dismiss();
                    updateMobile(memberid, username, id, email, apitoken, selectedCode, mobilenumber.getText().toString().trim(), selectedCountryId, selectedPromo, firstname, lastname);
                } else {
                    mQueue = Volley.newRequestQueue(getApplicationContext());
                    mQueue.getCache().clear();

                    final String URL = resources.getString(R.string.api) + "checkMobileNumber";
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("mobile_no", selectedMobile);
                    params.put("promo_code", selectedPromo);

                    Log.d(URL, new JSONObject(params).toString());

                    JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, URL, new JSONObject(params),
                            response -> {

                                Log.d("send otp", response.toString());
                                try {
                                    String status = response.getString("status");

                                    if (TextUtils.equals(status, "true")) {
                                        builder.dismiss();
                                        PhoneAuthOptions options =
                                                PhoneAuthOptions.newBuilder(mAuth)
                                                        .setPhoneNumber(selectedCode + selectedMobile)       // Phone number to verify
                                                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                                        .setActivity(MainActivity.this)                 // Activity (for callback binding)
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
            }
        });
        builder.create();
        try {
            builder.show();
        } catch (Exception ignored) {

        }
    }

    public void viewallcountry() {

        /*all_country api call start*/
        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();
        String url = resources.getString(R.string.api) + "all_country";
        final UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());

        final JsonObjectRequest request = new JsonObjectRequest(GET, url, null,
                response -> {

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

                headers.put("x-localization", LocaleHelper.getPersist(context));

                return headers;
            }
        };
        request.setShouldCache(false);
        mQueue.add(request);


    }

    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array) {

        for (int i = 0; i < array.length(); i++) {
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);

                spinnerArrayCountryCode.add(json.getString("p_code"));
                spinnerArrayCountryName.add(json.getString("country_name"));
                spinnerArrayCountryId.add(json.getString("country_id"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateMobile(final String memberid, final String username, final String id, final String email, final String apitoken, String code, final String mobile, final String countryId, String promo, String firstname, String lastname) {

        loadingDialog.show();
        //call login api
        final String URL = resources.getString(R.string.api) + "update_mobile_no";


        HashMap<String, String> params = new HashMap<String, String>();
        params.put("member_id", memberid);
        params.put("mobile_no", mobile);
        params.put("country_code", code);
        params.put("country_id", countryId);
        params.put("promo_code", promo);

        Log.d(URL, new JSONObject(params).toString());
        JsonObjectRequest request_json = new JsonObjectRequest(URL, new JSONObject(params),
                response -> {
                    try {
                        String status = response.getString("status");
                        if (TextUtils.equals(status, "true")) {
                            final CurrentUser cUser = new CurrentUser(memberid, username, id, email, mobile, apitoken, firstname, lastname);
                            loadingDialog.dismiss();
                            final UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());
                            userLocalStore.storeUserData(cUser);
                            Toast.makeText(MainActivity.this, resources.getString(R.string.login_successfully), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), PlayActivity.class));
                        } else {
                            String message = response.getString("message");

                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    } catch (JSONException e) {
                        loadingDialog.dismiss();
                        e.printStackTrace();
                    }
                }, error -> {
                    VolleyLog.e("Error: ", error.getMessage());
                    //Log.d("login fb error response", error.getMessage());
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


    @SuppressLint("SetTextI18n")
    public void openotpdialog(final String fotp) {


        ///////////////////////countdown////////////////////////

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

        ////////////////////////countdown///////////////////////
        mobileview.setText(resources.getString(R.string.we_will_sent_you_otp_in_) + selectedCode + selectedMobile);

        resend.setOnClickListener(view -> {
            builder.dismiss();
            loadingDialog.show();
            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(selectedCode + selectedMobile)       // Phone number to verify
                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                            .setActivity(MainActivity.this)                 // Activity (for callback binding)
                            .setCallbacks(mCallback)          // OnVerificationStateChangedCallbacks
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        });

        otpView.setShowSoftInputOnFocus(true);
        otpView.setOtpCompletionListener(otp -> {
            loadingDialog.show();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(fotp, otp);
            signInWithPhoneAuthCredential(credential);
        });
        builder.create();
        builder.show();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        builder.dismiss();
                        Toast.makeText(getApplicationContext(), resources.getString(R.string.mobile_number_successfully_verified), Toast.LENGTH_SHORT).show();
                        updateMobile(selectedMemberid, selectedUsername, selectedId, selectedEmail, selectedApitoken, selectedCode, selectedMobile, selectedCountryId, selectedPromo, selectedFirstname, selectedLastname);
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w("TAG", "signInWithCredential:failure", task.getException());
                        Toast.makeText(getApplicationContext(), resources.getString(R.string.failed), Toast.LENGTH_SHORT).show();
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(getApplicationContext(), resources.getString(R.string.wrong_otp), Toast.LENGTH_SHORT).show();
                            otpView.setText("");
                            loadingDialog.dismiss();
                            // The verification code entered was invalid
                        }
                    }
                });
    }
}
