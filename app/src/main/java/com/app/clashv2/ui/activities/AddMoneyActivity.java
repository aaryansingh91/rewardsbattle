package com.app.clashv2.ui.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cashfree.pg.api.CFPaymentGatewayService;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.CompoundButtonCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cashfree.pg.core.api.CFSession;
import com.cashfree.pg.core.api.callback.CFCheckoutResponseCallback;
import com.cashfree.pg.core.api.exception.CFException;
import com.cashfree.pg.core.api.utils.CFErrorResponse;
import com.cashfree.pg.ui.api.CFDropCheckoutPayment;
import com.cashfree.pg.ui.api.CFPaymentComponent;
import com.app.clashv2.R;
import com.app.clashv2.models.CurrentUser;
import com.app.clashv2.utils.AnalyticsUtil;
import com.app.clashv2.utils.CustomTypefaceSpan;
import com.app.clashv2.utils.HashGenerationUtils;
import com.app.clashv2.utils.LoadingDialog;
import com.app.clashv2.utils.LocaleHelper;
import com.app.clashv2.utils.UserLocalStore;
import com.gocashfree.cashfreesdk.CFPaymentService;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.instamojo.android.Instamojo;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.payu.base.models.ErrorResponse;
import com.payu.base.models.PayUPaymentParams;
import com.payu.checkoutpro.PayUCheckoutPro;
import com.payu.checkoutpro.utils.PayUCheckoutProConstants;
import com.payu.ui.model.listeners.PayUCheckoutProListener;
import com.payu.ui.model.listeners.PayUHashGenerationListener;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import com.upitranzact.sdk.UpiTranzactSDK;
import com.upitranzact.sdk.network.PaymentCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.model.Charge;

public class AddMoneyActivity extends AppCompatActivity implements Instamojo.InstamojoPaymentCallback, PaymentResultWithDataListener, CFCheckoutResponseCallback {

    private static final int TEZ_REQUEST_CODE = 123;
    private static final String GOOGLE_TEZ_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user";
    String amount;
    int amountInt = 0;
    float amountFloat = 0;
    int minAmountInt = 0;
    ImageView back;
    RequestQueue mQueue, rQueue, pQueue, psQueue;
    JsonObjectRequest request;
    JsonObjectRequest paypal_response_request;
    JsonObjectRequest pay_stack_response_request;
    JsonObjectRequest instamojo_response_request;
    JsonObjectRequest cash_free_response_request;
    JsonObjectRequest cashfree_response_request;
    JsonObjectRequest paytm_response_request;
    JsonObjectRequest google_pay_response_request;
    JsonObjectRequest razorpay_response_request;
    JsonObjectRequest payu_response_request;
    UserLocalStore userLocalStore;
    String custId = "";
    String finalTotalAmount = "";
    LoadingDialog loadingDialog;
    int PAYPAL_REQUEST_CODE = 1234;
    PayPalConfiguration config;
    String payment = "";
    String minAmount = "";
    String modeStatus = "";
    String paymentDescription = "";
    String secretKey = "";
    co.paystack.android.model.Card card;
    LinearLayout paystackll;
    TextView addMoneyTitle;
    EditText cardNumber;
    EditText CVV;
    TextView expMonth;
    TextView expYear;
    TextView paystackTestNote;
    Checkout checkout;
    String receipt = "";
    String depositId = "";
    String cfAppid = "";
    String upi_id = "";
    RadioGroup addMoneyOption;
    RadioButton rdbtn;
    JSONArray array;
    TextView addNote;
    String point = "";
    long pointInt = 0;
    String selectedCurrency = "";
    String selectedCurrencySymbol = "";

    Context context;
    Resources resources;

    CurrentUser user;
    String puMId = "";
    String puMKey = "";
    String puSalt = "";
    String oId = "";

    final int UPI_PAYMENT = 0;

    String spMId = "";
    String spMKey = "";
    String spEUrl = "";
    private RequestQueue requestQueue;
    private JsonObjectRequest upiTranzactResponseRequest;
    String public_key = "";
    String secret_key = "";
    String upitz_mid = "";

    @Override
    protected void onDestroy() {
        //only for paypal
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject json;
                try {
                    json = array.getJSONObject(i);
                    if (TextUtils.equals(json.getString("payment_name"), "PayPal")) {
                        stopService(new Intent(AddMoneyActivity.this, PayPalService.class));
                    }
                } catch (JSONException ignored) {

                }
            }
        } catch (Exception ignored) {

        }
        super.onDestroy();
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_add_money);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        context = LocaleHelper.setLocale(AddMoneyActivity.this);
        resources = context.getResources();

        loadingDialog = new LoadingDialog(AddMoneyActivity.this);
        loadingDialog.show();

        addMoneyOption = findViewById(R.id.addmoney_option);
        addNote = findViewById(R.id.add_note);
        addMoneyTitle = findViewById(R.id.addmoneytitleid);
        paystackll = findViewById(R.id.paystackll);
        cardNumber = findViewById(R.id.add_amount_cardnumber);
        CVV = findViewById(R.id.add_amount_cvv);
        expMonth = findViewById(R.id.add_amount_expmonth);
        expYear = findViewById(R.id.add_amount_expyear);
        paystackTestNote = findViewById(R.id.paystacktestnote);

        // payment start
        // get payment gateway info in payment api
        pQueue = Volley.newRequestQueue(getApplicationContext());
        pQueue.getCache().clear();

        String url = resources.getString(R.string.api) + "payment";
        request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {

                    Log.d("payme", response.toString());
                    loadingDialog.dismiss();
                    try {
                        minAmount = response.getString("min_addmoney");
                        JSONArray arr = response.getJSONArray("payment");
                        if (!TextUtils.equals(response.getString("payment"), "[]")) {
                            array = arr;
                            JSON_PARSE_DATA_AFTER_WEBCALL(arr);
                        } else {
                            Toast.makeText(AddMoneyActivity.this, getString(R.string.payment_method_not_available), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MyWalletActivity.class));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            Log.e("**VolleyError", "error" + error.getMessage());

            if (error instanceof TimeoutError) {

                request.setShouldCache(false);
                mQueue.add(request);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws
                    AuthFailureError {
                return super.getParams();
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                userLocalStore = new UserLocalStore(getApplicationContext());
                CurrentUser user = userLocalStore.getLoggedInUser();
                String token = "Bearer " + user.getToken();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                headers.put("x-localization", LocaleHelper.getPersist(context));
                return headers;
            }
        };
        request.setShouldCache(false);
        pQueue.add(request);
        //payment end

        back = findViewById(R.id.backfromaddmoney);
        back.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), MyWalletActivity.class);
            startActivity(intent);
        });

        addMoneyTitle.setText(resources.getString(R.string.add_money));

        userLocalStore = new UserLocalStore(getApplicationContext());
        user = userLocalStore.getLoggedInUser();
        custId = user.getMemberid();

        userLocalStore = new UserLocalStore(getApplicationContext());
        final CurrentUser user = userLocalStore.getLoggedInUser();
        custId = user.getMemberid();

        final EditText addamountEdit = findViewById(R.id.add_amount_edit);
        final Button addamounBtn = findViewById(R.id.add_amount_btn);

        addamounBtn.setEnabled(false);
        addamounBtn.setBackgroundColor(getResources().getColor(R.color.newdisablegreen));
        addamountEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.length() > 0) {
                    addamounBtn.setEnabled(true);
                    addamounBtn.setText(resources.getString(R.string.ADD_MONEY));
                    addamounBtn.setBackgroundColor(getResources().getColor(R.color.orange));
                    if (TextUtils.equals(payment, "PayStack")) {
                        addNote.setText(resources.getString(R.string.you_will_pay_) + " " + selectedCurrencySymbol + Integer.parseInt(String.valueOf(charSequence)) / pointInt);
                        if (TextUtils.equals(selectedCurrencySymbol, "₹")) {
                            Typeface font = Typeface.DEFAULT;
                            SpannableStringBuilder SS = new SpannableStringBuilder(resources.getString(R.string.Rs));
                            SS.setSpan(new CustomTypefaceSpan("", font), 0, 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                            addNote.setText(TextUtils.concat(resources.getString(R.string.you_will_pay_), " ", SS, String.valueOf(Integer.parseInt(String.valueOf(charSequence)) / pointInt)));
                        }
                    } else {
                        addNote.setText(resources.getString(R.string.you_will_pay_) + " " + selectedCurrencySymbol + String.format("%.2f", Double.parseDouble(String.valueOf(charSequence)) / (double) pointInt));
                        if (TextUtils.equals(selectedCurrencySymbol, "₹")) {
                            Typeface font = Typeface.DEFAULT;
                            SpannableStringBuilder SS = new SpannableStringBuilder(resources.getString(R.string.Rs));
                            SS.setSpan(new CustomTypefaceSpan("", font), 0, 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                            addNote.setText(TextUtils.concat(resources.getString(R.string.you_will_pay_), " ", SS, String.format("%.2f", Double.parseDouble(String.valueOf(charSequence)) / (double) pointInt)));

                        }
                    }

                } else {
                    addNote.setText("");
                    addamounBtn.setEnabled(false);
                    addamounBtn.setBackgroundColor(getResources().getColor(R.color.newdisablegreen));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        addMoneyOption.setOnCheckedChangeListener((group, checkedId) -> {

            try {
                rdbtn = findViewById(checkedId);

                for (int i = 0; i < array.length(); i++) {
                    JSONObject json;
                    try {
                        json = array.getJSONObject(i);
                        if (TextUtils.equals(json.getString("payment_name"), rdbtn.getText().toString())) {
                            payment = rdbtn.getText().toString();
                            modeStatus = json.getString("payment_status");

                            selectedCurrency = json.getString("currency_code");
                            selectedCurrencySymbol = json.getString("currency_symbol");
                            point = json.getString("currency_point");
                            pointInt = Integer.parseInt(point);

                            if (TextUtils.equals(payment, "PayStack")) {
                                paystackll.setVisibility(View.VISIBLE);
                                if (TextUtils.equals(json.getString("payment_status"), "Test")) {
                                    paystackTestNote.setVisibility(View.VISIBLE);
                                    paystackTestNote.setText(resources.getString(R.string.paystack_note));
                                } else {
                                    paystackTestNote.setVisibility(View.GONE);
                                }
                            } else {
                                paystackll.setVisibility(View.GONE);
                                paystackTestNote.setVisibility(View.GONE);
                            }

                            if (addamountEdit.getText().toString().trim().length() > 0) {

                                if (TextUtils.equals(payment, "PayStack")) {
                                    addNote.setText(resources.getString(R.string.you_will_pay_) + " " + selectedCurrencySymbol + Integer.parseInt(addamountEdit.getText().toString().trim()) / pointInt);
                                    if (TextUtils.equals(selectedCurrencySymbol, "₹")) {
                                        Typeface font = Typeface.DEFAULT;
                                        SpannableStringBuilder SS = new SpannableStringBuilder(resources.getString(R.string.Rs));
                                        SS.setSpan(new CustomTypefaceSpan("", font), 0, 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                                        addNote.setText(TextUtils.concat(resources.getString(R.string.you_will_pay_), " ", SS, String.valueOf(Integer.parseInt(addamountEdit.getText().toString().trim()) / pointInt)));
                                    }
                                } else {
                                    addNote.setText(resources.getString(R.string.you_will_pay_) + " " + selectedCurrencySymbol + String.format("%.2f", Double.parseDouble(addamountEdit.getText().toString().trim()) / (double) pointInt));
                                    if (TextUtils.equals(selectedCurrencySymbol, "₹")) {
                                        Typeface font = Typeface.DEFAULT;
                                        SpannableStringBuilder SS = new SpannableStringBuilder(resources.getString(R.string.Rs));
                                        SS.setSpan(new CustomTypefaceSpan("", font), 0, 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                                        addNote.setText(TextUtils.concat(resources.getString(R.string.you_will_pay_), " ", SS, String.format("%.2f", Double.parseDouble(addamountEdit.getText().toString().trim()) / (double) pointInt)));
                                    }
                                }

                            } else {
                                addNote.setText("");

                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        });

        RelativeLayout rs10 = findViewById(R.id.rs10);
        rs10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMoney("10");
            }
        });

        RelativeLayout rs20 = findViewById(R.id.rs20);
        rs20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMoney("20");
            }
        });

        RelativeLayout rs50 = findViewById(R.id.rs50);
        rs50.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMoney("50");
            }
        });

        RelativeLayout rs100 = findViewById(R.id.rs100);
        rs100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMoney("100");
            }
        });

        RelativeLayout rs200 = findViewById(R.id.rs200);
        rs200.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMoney("200");
            }
        });

        RelativeLayout rs500 = findViewById(R.id.rs500);
        rs500.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMoney("500");
            }
        });

        RelativeLayout rs1000 = findViewById(R.id.rs1000);
        rs1000.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMoney("1000");
            }
        });

        addamounBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amount = addamountEdit.getText().toString().trim();
                addMoney(amount);
            }
        });

    }

    public void addMoney(String amount) {
        try {
            amountInt = Integer.parseInt(amount);
            minAmountInt = Integer.parseInt(minAmount);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (amountInt < minAmountInt) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(AddMoneyActivity.this);
            builder.setTitle(getString(R.string.error));
            builder.setMessage(getString(R.string.enter_minmum) + minAmount);
            builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {

            });
            builder.show();
            builder.create();
        } else {
            //amountFloat = Float.parseFloat(String.valueOf(Math.round(Double.parseDouble(String.valueOf(amountInt)) / (double) pointInt)));
            amountFloat = Float.parseFloat(String.format("%.2f", Double.parseDouble(String.valueOf(amountInt)) / (double) pointInt));

            if (TextUtils.equals(payment, "PayU")) {
                // if payment gateway select paypal

                if (TextUtils.equals(user.getPhone().trim(), "")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.please_update_profile_with_mobile_number), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MyProfileActivity.class));
                    return;
                }

                if (TextUtils.equals(user.getEmail().trim(), "")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.please_update_profile_with_email), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MyProfileActivity.class));
                    return;
                }
                PayUAddMoney();
            } else if (TextUtils.equals(payment, "PayPal")) {
                // if payment gateway select paypal
                PaypalPayment(String.valueOf(amountFloat));
            } else if (TextUtils.equals(payment, "PayTm")) {
                // if payment gateway select paytm
                Toast.makeText(getApplicationContext(), "Paytm SDK not found.", Toast.LENGTH_SHORT).show();
                //add_money for paytm end
            } else if (TextUtils.equals(payment, "Offline")) {

                loadingDialog.show();
                //add_money for paytm start
                mQueue = Volley.newRequestQueue(getApplicationContext());
                mQueue.getCache().clear();

                String url = resources.getString(R.string.api) + "add_money";
                HashMap<String, String> params = new HashMap<>();
                params.put("payment_name", payment);
                params.put("CUST_ID", custId);
                params.put("TXN_AMOUNT", amount);

                request = new JsonObjectRequest(url, new JSONObject(params),
                        response -> {

                            Log.d("response", response.toString());
                            loadingDialog.dismiss();
                            try {
                                if (TextUtils.equals(response.getString("status"), "false")) {
                                    Toast.makeText(AddMoneyActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                                } else {
                                    Intent intent = new Intent(getApplicationContext(), OfflinePaymentActivity.class);
                                    intent.putExtra("paymentdesc", paymentDescription);
                                    startActivity(intent);

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }, error -> {
                    Log.e("**VolleyError", "error" + error.getMessage());

                    if (error instanceof TimeoutError) {

                        request.setShouldCache(false);
                        mQueue.add(request);
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws
                            AuthFailureError {
                        return super.getParams();
                    }

                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        CurrentUser user = userLocalStore.getLoggedInUser();
                        String token = "Bearer " + user.getToken();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", token);
                        headers.put("x-localization", LocaleHelper.getPersist(context));
                        return headers;
                    }
                };
                request.setShouldCache(false);
                mQueue.add(request);

            } else if (TextUtils.equals(payment, "PayStack")) {

                if (TextUtils.equals(user.getEmail().trim(), "")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.please_update_profile_with_email), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MyProfileActivity.class));
                    return;
                }
                if (TextUtils.equals(cardNumber.getText().toString().trim(), "")) {
                    cardNumber.setError(getString(R.string.please_enter_card_number));
                    return;
                }
                if (TextUtils.equals(CVV.getText().toString().trim(), "")) {
                    CVV.setError(getString(R.string.please_enter_cvv));
                    return;
                }
                if (TextUtils.equals(expMonth.getText().toString().trim(), "")) {
                    expMonth.setError(getString(R.string.please_enter_expiry_month));
                    return;
                }
                if (TextUtils.equals(expYear.getText().toString().trim(), "")) {
                    expYear.setError(getString(R.string.please_enter_expiry_year));
                    return;
                }

                String cardnumber = cardNumber.getText().toString().trim();
                int expirymonth = Integer.parseInt(expMonth.getText().toString().trim()); //any month in the future
                int expiryyear = Integer.parseInt(expYear.getText().toString().trim()); // any year in the future. '2018' would work also!
                String cvv = CVV.getText().toString().trim();  // cvv of the test card

                card = new co.paystack.android.model.Card(cardnumber, expirymonth, expiryyear, cvv);

                if (card.isValid()) {
                    //Toast.makeText(AddMoneyActivity.this, "charged", Toast.LENGTH_SHORT).show();
                    // charge card
                } else {
                    //Toast.makeText(AddMoneyActivity.this, "not charged", Toast.LENGTH_SHORT).show();
                    //do something
                }

                Charge charge = new Charge();
                charge.setAmount((int) amountFloat);
                CurrentUser user = userLocalStore.getLoggedInUser();
                charge.setEmail(user.getEmail());
                charge.setCard(card); //sets the card to charge
                performCharge(charge);

            } else if (TextUtils.equals(payment, "Instamojo")) {
                loadingDialog.show();
                //add_money for instamojo start
                mQueue = Volley.newRequestQueue(getApplicationContext());
                mQueue.getCache().clear();

                String url = resources.getString(R.string.api) + "add_money";
                HashMap<String, String> params = new HashMap<>();
                params.put("payment_name", payment);
                params.put("CUST_ID", custId);
                params.put("TXN_AMOUNT", amount);

                Log.d(url, new JSONObject(params).toString());

                request = new JsonObjectRequest(url, new JSONObject(params),
                        response -> {
                            Log.d("addmoney", response.toString());
                            loadingDialog.dismiss();
                            try {
                                Log.d(response.getString("order_id"), response.toString());
                                if (TextUtils.equals(response.getString("status"), "false")) {
                                    Toast.makeText(AddMoneyActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                                } else {
                                    depositId = response.getString("deposit_id");
                                    initiateInstamojoSDKPayment(response.getString("order_id"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }, error -> {
                    Log.e("**VolleyError", "error" + error.getMessage());

                    if (error instanceof TimeoutError) {

                        request.setShouldCache(false);
                        mQueue.add(request);
                    }

                }) {
                    @Override
                    protected Map<String, String> getParams() throws
                            AuthFailureError {
                        return super.getParams();
                    }

                    @Override
                    public Map<String, String> getHeaders() {

                        Map<String, String> headers = new HashMap<>();
                        CurrentUser user = userLocalStore.getLoggedInUser();
                        String token = "Bearer " + user.getToken();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", token);
                        headers.put("x-localization", LocaleHelper.getPersist(context));
                        return headers;
                    }
                };
                request.setShouldCache(false);
                mQueue.add(request);
                //add_money for instamojo end

            } else if (TextUtils.equals(payment, "Razorpay")) {

                loadingDialog.show();
                //add_money for razorpay start
                mQueue = Volley.newRequestQueue(getApplicationContext());
                mQueue.getCache().clear();

                String url = resources.getString(R.string.api) + "add_money";
                HashMap<String, String> params = new HashMap<>();
                params.put("CUST_ID", custId);
                params.put("payment_name", payment);
                final int newamount = 100 * amountInt;
                params.put("TXN_AMOUNT", String.valueOf(newamount));

                Log.d(url, new JSONObject(params).toString());

                request = new JsonObjectRequest(url, new JSONObject(params),
                        response -> {

                            Log.d("addmoney", response.toString());
                            loadingDialog.dismiss();

                            try {
                                if (TextUtils.equals(response.getString("status"), "false")) {
                                    Toast.makeText(AddMoneyActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                                } else {
                                    receipt = response.getString("receipt");
                                    startRazorpayPayment(response.getString("key_id"), response.getString("order_id"), response.getString("currency"), String.valueOf(newamount));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }, error -> {
                    Log.e("**VolleyError", "error" + error.getMessage());

                    if (error instanceof TimeoutError) {

                        request.setShouldCache(false);
                        mQueue.add(request);
                    }

                }) {
                    @Override
                    protected Map<String, String> getParams() throws
                            AuthFailureError {
                        return super.getParams();
                    }


                    @Override
                    public Map<String, String> getHeaders() {

                        Map<String, String> headers = new HashMap<>();
                        CurrentUser user = userLocalStore.getLoggedInUser();
                        String token = "Bearer " + user.getToken();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", token);
                        headers.put("x-localization", LocaleHelper.getPersist(context));
                        return headers;
                    }
                };


                request.setShouldCache(false);
                mQueue.add(request);
                //add_money for razorpay end

            } else if (TextUtils.equals(payment, "Cashfree")) {

                if (TextUtils.equals(user.getEmail().trim(), "") && TextUtils.equals(user.getPhone().trim(), "")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.please_update_profile_with_email_and_mobile_number), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MyProfileActivity.class));
                    return;
                }

                if (TextUtils.equals(user.getEmail().trim(), "")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.please_update_profile_with_email), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MyProfileActivity.class));
                    return;
                }

                if (TextUtils.equals(user.getPhone().trim(), "")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.please_update_profile_with_mobile_number), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MyProfileActivity.class));
                    return;
                }

                loadingDialog.show();
                //add_money for cashfree start
                mQueue = Volley.newRequestQueue(getApplicationContext());
                mQueue.getCache().clear();

                String url = resources.getString(R.string.api) + "add_money";
                HashMap<String, String> params = new HashMap<>();
                params.put("payment_name", payment);
                params.put("CUST_ID", custId);
                params.put("TXN_AMOUNT", amount);

                Log.d(url, new JSONObject(params).toString());

                request = new JsonObjectRequest(url, new JSONObject(params),
                        response -> {

                            loadingDialog.dismiss();
                            Log.d("cashfree add money ", response.toString());
                            try {
                                if (TextUtils.equals(response.getString("status"), "false")) {
                                    Toast.makeText(AddMoneyActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                                } else {
                                    JSONObject obj = new JSONObject(response.getString("message"));
                                    HashMap<String, String> params1 = new HashMap<>();
                                    params1.put("appId", cfAppid);
                                    params1.put("orderId", obj.getString("order_id"));
                                    params1.put("orderAmount", String.valueOf(amountFloat));
                                    params1.put("customerPhone", user.getPhone());
                                    params1.put("customerEmail", user.getEmail());
                                            /*CFPaymentService cfPaymentService = CFPaymentService.getCFPaymentServiceInstance();
                                            cfPaymentService.setOrientation(0);*/
                                    Log.d("CashFree", String.valueOf(params1));
                                    if (TextUtils.equals(modeStatus, "Test")) {
//                                                cfPaymentService.doPayment(AddMoneyActivity.this, params1, obj.getString("cftoken"), "TEST");
                                        try {
                                            CFSession cfSession = new CFSession.CFSessionBuilder()
                                                    .setEnvironment(CFSession.Environment.SANDBOX)
                                                    .setPaymentSessionID(obj.getString("session_id"))
                                                    .setOrderId(obj.getString("order_id"))
                                                    .build();
                                            CFPaymentComponent cfPaymentComponent = new CFPaymentComponent.CFPaymentComponentBuilder()
                                                    .add(CFPaymentComponent.CFPaymentModes.CARD)
                                                    .add(CFPaymentComponent.CFPaymentModes.UPI)
                                                    .add(CFPaymentComponent.CFPaymentModes.WALLET)
                                                    .add(CFPaymentComponent.CFPaymentModes.NB)
                                                    .add(CFPaymentComponent.CFPaymentModes.PAYPAL)
                                                    .build();
                                            CFDropCheckoutPayment cfDropCheckoutPayment = new CFDropCheckoutPayment.CFDropCheckoutPaymentBuilder()
                                                    .setSession(cfSession)
                                                    .setCFUIPaymentModes(cfPaymentComponent)
                                                    .build();
                                            CFPaymentGatewayService gatewayService = CFPaymentGatewayService.getInstance();
                                            gatewayService.setCheckoutCallback(AddMoneyActivity.this);
                                            gatewayService.doPayment(AddMoneyActivity.this, cfDropCheckoutPayment);
                                        } catch (CFException exception) {
                                            exception.printStackTrace();
                                        }
                                    } else {
                                        /*cfPaymentService.doPayment(AddMoneyActivity.this, params1, obj.getString("cftoken"), "PROD");*/
                                        try {
                                            CFSession cfSession = new CFSession.CFSessionBuilder()
                                                    .setEnvironment(CFSession.Environment.PRODUCTION)
                                                    .setPaymentSessionID(obj.getString("session_id"))
                                                    .setOrderId(obj.getString("order_id"))
                                                    .build();
                                            CFPaymentComponent cfPaymentComponent = new CFPaymentComponent.CFPaymentComponentBuilder()
                                                    .add(CFPaymentComponent.CFPaymentModes.CARD)
                                                    .add(CFPaymentComponent.CFPaymentModes.UPI)
                                                    .add(CFPaymentComponent.CFPaymentModes.WALLET)
                                                    .add(CFPaymentComponent.CFPaymentModes.NB)
                                                    .add(CFPaymentComponent.CFPaymentModes.PAYPAL)
                                                    .build();
                                            CFDropCheckoutPayment cfDropCheckoutPayment = new CFDropCheckoutPayment.CFDropCheckoutPaymentBuilder()
                                                    .setSession(cfSession)
                                                    .setCFUIPaymentModes(cfPaymentComponent)
                                                    .build();
                                            CFPaymentGatewayService gatewayService = CFPaymentGatewayService.getInstance();
                                            gatewayService.setCheckoutCallback(AddMoneyActivity.this);
                                            gatewayService.doPayment(AddMoneyActivity.this, cfDropCheckoutPayment);
                                        } catch (CFException exception) {
                                            exception.printStackTrace();
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }, error -> {
                    Log.e("**VolleyError", "error" + error.getMessage());

                    if (error instanceof TimeoutError) {

                        request.setShouldCache(false);
                        mQueue.add(request);

                    }

                }) {
                    @Override
                    protected Map<String, String> getParams() throws
                            AuthFailureError {
                        return super.getParams();
                    }

                    @Override
                    public Map<String, String> getHeaders() {

                        Map<String, String> headers = new HashMap<>();
                        CurrentUser user = userLocalStore.getLoggedInUser();
                        String token = "Bearer " + user.getToken();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", token);
                        headers.put("x-localization", LocaleHelper.getPersist(context));
                        return headers;
                    }
                };
                request.setShouldCache(false);
                mQueue.add(request);
                //add_money for cashfree end

            }
            //OLD TRON USDT DESIGN ACTIVITY
                    /*else if (TextUtils.equals(payment, "Tron")) {

                        //add_money for cashfree start
                        mQueue = Volley.newRequestQueue(getApplicationContext());
                        mQueue.getCache().clear();

                        String url = resources.getString(R.string.api) + "add_money";
                        HashMap<String, String> params = new HashMap<String, String>();
                        params.put("payment_name", payment);
                        params.put("CUST_ID", custId);
                        params.put("TXN_AMOUNT", amount);

                        loadingDialog.dismiss();

                        Log.d(url, new JSONObject(params).toString());

                        request = new JsonObjectRequest(url, new JSONObject(params),
                                response -> {

                                    Log.d("tron add money ", response.toString());
                                    loadingDialog.dismiss();

                                    try {


                                        String gf = response.getString("status");

                                        if (TextUtils.equals(response.getString("status"), "false")) {

                                            Toast.makeText(AddMoneyActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();

                                        } else {

                                            String walletAddress = response.getString("wallet_address");

                                            Intent intent = new Intent(getApplicationContext(), TronActivity.class);
                                            intent.putExtra("address", walletAddress);
                                            intent.putExtra("sta", gf);
                                            intent.putExtra("selected", selectedCurrencySymbol);
                                            intent.putExtra("TAMOUNT", String.valueOf((int) amountFloat));
                                            intent.putExtra("TAMOUNT", finalTamount);
                                            startActivity(intent);
                                        }
                                        // receipt = response.getString("receipt");
                                        // startRazorpayPayment(response.getString("key_id"), response.getString("order_id"), response.getString("currency"),

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }, error -> {
                            Log.e("**VolleyError", "error" + error.getMessage());

                            if (error instanceof TimeoutError) {

                                request.setShouldCache(false);
                                mQueue.add(request);
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                return super.getParams();
                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {

                                Map<String, String> headers = new HashMap<>();
                                CurrentUser user = userLocalStore.getLoggedInUser();
                                String token = "Bearer " + user.getToken();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", token);
                                headers.put("x-localization", LocaleHelper.getPersist(context));
                                return headers;
                            }
                        };
                        request.setShouldCache(false);
                        mQueue.add(request);

                    }*/
            else if (TextUtils.equals(payment, "Tron")) {

                mQueue = Volley.newRequestQueue(getApplicationContext());
                mQueue.getCache().clear();

                String url = resources.getString(R.string.api) + "add_money";
                HashMap<String, String> params = new HashMap<>();
                params.put("payment_name", payment);
                params.put("CUST_ID", custId);
                params.put("TXN_AMOUNT", amount);

                loadingDialog.dismiss();

                Log.d(url, new JSONObject(params).toString());

                request = new JsonObjectRequest(url, new JSONObject(params),
                        response -> {

                            Log.d("bep20 add money ", response.toString());
                            loadingDialog.dismiss();

                            try {


                                String gf = response.getString("status");

                                if (TextUtils.equals(response.getString("status"), "false")) {

                                    Toast.makeText(AddMoneyActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();

                                } else {

                                    String walletAddress = response.getString("wallet_address");
                                    Intent intent = new Intent(getApplicationContext(), bep20Activity.class);
                                    intent.putExtra("address", walletAddress);
                                    intent.putExtra("sta", gf);
                                    intent.putExtra("selected", selectedCurrencySymbol);
                                    intent.putExtra("bep20amount", amount);
                                    startActivity(intent);
                                }
                                // receipt = response.getString("receipt");
                                // startRazorpayPayment(response.getString("key_id"), response.getString("order_id"), response.getString("currency"),

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }, error -> {
                    Log.e("**VolleyError", "error" + error.getMessage());

                    if (error instanceof TimeoutError) {

                        request.setShouldCache(false);
                        mQueue.add(request);
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws
                            AuthFailureError {
                        return super.getParams();
                    }

                    @Override
                    public Map<String, String> getHeaders() {

                        Map<String, String> headers = new HashMap<>();
                        CurrentUser user = userLocalStore.getLoggedInUser();
                        String token = "Bearer " + user.getToken();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", token);
                        headers.put("x-localization", LocaleHelper.getPersist(context));
                        return headers;
                    }
                };
                request.setShouldCache(false);
                mQueue.add(request);

            } else if (TextUtils.equals(payment, "UPI")) {
                String[] packageNames = {
                        "net.one97.paytm",
                        "com.google.android.apps.nbu.paisa.user",
                        "com.phonepe.app",
                        "in.org.npci.upiapp"
                };

                boolean isAnyPackageInstalled = false;

                for (String packageName : packageNames) {
                    if (isPackageInstalled(getApplicationContext(), packageName)) {
                        isAnyPackageInstalled = true;
                        break;
                    }
                }

                if (isAnyPackageInstalled) {

                } else {
                    Toast.makeText(AddMoneyActivity.this, "No UPI app found, please install one to continue", Toast.LENGTH_SHORT).show();
                    return;
                }

                payUsingUpi(String.valueOf(amountFloat));

            } else if (TextUtils.equals(payment, "Google Pay")) {

                loadingDialog.show();
                //add_money for google pay start
                mQueue = Volley.newRequestQueue(getApplicationContext());
                mQueue.getCache().clear();

                String url = resources.getString(R.string.api) + "add_money";
                HashMap<String, String> params = new HashMap<>();
                params.put("payment_name", payment);
                params.put("CUST_ID", custId);
                params.put("TXN_AMOUNT", amount);

                Log.d(url, new JSONObject(params).toString());

                request = new JsonObjectRequest(url, new JSONObject(params),
                        response -> {
                            Log.d("addmoney", response.toString());
                            loadingDialog.dismiss();
                            try {

                                if (TextUtils.equals(response.getString("status"), "false")) {
                                    Toast.makeText(AddMoneyActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                                } else {
                                    String gtid = response.getString("transection_no");
                                    String goid = response.getString("order_id");
                                    Uri uri =
                                            new Uri.Builder()
                                                    .scheme("upi")
                                                    .authority("pay")
                                                    .appendQueryParameter("pa", upi_id)
                                                    .appendQueryParameter("pn", resources.getString(R.string.app_name))
                                                    .appendQueryParameter("tn", "Transaction for " + gtid)
                                                    .appendQueryParameter("am", String.valueOf(amountFloat))
                                                    .appendQueryParameter("cu", "INR")
                                                    .build();
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(uri);
                                    intent.setPackage(GOOGLE_TEZ_PACKAGE_NAME);
                                    try {
                                        SharedPreferences sharedPreferences = getSharedPreferences("gpay", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("amount", String.valueOf(amountFloat));
                                        editor.putString("tid", gtid);
                                        editor.putString("oid", goid);
                                        editor.apply();

                                        startActivityForResult(intent, TEZ_REQUEST_CODE);
                                    } catch (Exception e) {

                                        AlertDialog.Builder builder1 = new AlertDialog.Builder(AddMoneyActivity.this);
                                        builder1.setMessage(resources.getString(R.string.google_pay_not_installed));
                                        builder1.setTitle(resources.getString(R.string.error));
                                        builder1.setCancelable(false);
                                        builder1.setPositiveButton(
                                                resources.getString(R.string.ok),
                                                (dialog, id) -> {

                                                }).create().show();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }, error -> {
                    Log.e("**VolleyError", "error" + error.getMessage());

                    if (error instanceof TimeoutError) {

                        request.setShouldCache(false);
                        mQueue.add(request);
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws
                            AuthFailureError {
                        return super.getParams();
                    }

                    @Override
                    public Map<String, String> getHeaders() {

                        Map<String, String> headers = new HashMap<>();
                        CurrentUser user = userLocalStore.getLoggedInUser();
                        String token = "Bearer " + user.getToken();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", token);
                        headers.put("x-localization", LocaleHelper.getPersist(context));
                        return headers;
                    }
                };
                request.setShouldCache(false);
                mQueue.add(request);

                //add_money for google pay end
            } else if (TextUtils.equals(payment, "UPITranzact")) {

                loadingDialog.show();
                //add_money for paytm start
                mQueue = Volley.newRequestQueue(getApplicationContext());
                mQueue.getCache().clear();

                String url = resources.getString(R.string.api) + "add_money";
                HashMap<String, String> params = new HashMap<>();
                params.put("payment_name", "UPITranzact");
                params.put("CUST_ID", custId);
                params.put("TXN_AMOUNT", amount);

                request = new JsonObjectRequest(url, new JSONObject(params),
                        response -> {

                            Log.d("response", response.toString());
                            loadingDialog.dismiss();
                            try {
                                if (TextUtils.equals(response.getString("status"), "false")) {
                                    Toast.makeText(AddMoneyActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                                } else {
                                    String gtid = response.getString("transection_no");
                                    String goid = response.getString("order_id");

                                    DecimalFormat decimalFormat = new DecimalFormat("#");
                                    String formattedAmtValue = decimalFormat.format(amountFloat);

                                    createOrder(context, goid, gtid, formattedAmtValue);


                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }, error -> {
                    Log.e("**VolleyError", "error" + error.getMessage());

                    if (error instanceof TimeoutError) {

                        request.setShouldCache(false);
                        mQueue.add(request);
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws
                            AuthFailureError {
                        return super.getParams();
                    }

                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        CurrentUser user = userLocalStore.getLoggedInUser();
                        String token = "Bearer " + user.getToken();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", token);
                        headers.put("x-localization", LocaleHelper.getPersist(context));
                        return headers;
                    }
                };
                request.setShouldCache(false);
                mQueue.add(request);

            } else if (TextUtils.equals(payment, "UPITranzact Business")) {

                loadingDialog.show();
                //add_money for paytm start
                mQueue = Volley.newRequestQueue(getApplicationContext());
                mQueue.getCache().clear();

                String url = resources.getString(R.string.api) + "add_money";
                HashMap<String, String> params = new HashMap<>();
                params.put("payment_name", "UPITranzact Business");
                params.put("CUST_ID", custId);
                params.put("TXN_AMOUNT", amount);

                request = new JsonObjectRequest(url, new JSONObject(params),
                        response -> {

                            Log.d("response", response.toString());
                            loadingDialog.dismiss();
                            try {
                                if (TextUtils.equals(response.getString("status"), "false")) {
                                    Toast.makeText(AddMoneyActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                                } else {
                                    String gtid = response.getString("transection_no");
                                    String goid = response.getString("order_id");

                                    DecimalFormat decimalFormat = new DecimalFormat("#");
                                    String formattedAmtValue = decimalFormat.format(amountFloat);

                                    createOrderRequest(context, goid, gtid, formattedAmtValue);


                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }, error -> {
                    Log.e("**VolleyError", "error" + error.getMessage());

                    if (error instanceof TimeoutError) {

                        request.setShouldCache(false);
                        mQueue.add(request);
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws
                            AuthFailureError {
                        return super.getParams();
                    }

                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        CurrentUser user = userLocalStore.getLoggedInUser();
                        String token = "Bearer " + user.getToken();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", token);
                        headers.put("x-localization", LocaleHelper.getPersist(context));
                        return headers;
                    }
                };
                request.setShouldCache(false);
                mQueue.add(request);

            }
        }


    }

    public static boolean isPackageInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    void payUsingUpi(String amount) {
        String transactionRef = UUID.randomUUID().toString();

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", "q813839460@ybl")
                .appendQueryParameter("pn", resources.getString(R.string.app_name))
                .appendQueryParameter("mc", "")
                //.appendQueryParameter("tid", "02125412")
                .appendQueryParameter("tr", "")
//                .appendQueryParameter("tn", "Transaction for " + transactionRef)
                .appendQueryParameter("am", "1"/*amount*/)
                .appendQueryParameter("cu", "INR")
                //.appendQueryParameter("refUrl", "blueapp")
                .build();


        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");

        if (null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT);
        } else {
            Toast.makeText(AddMoneyActivity.this, "No UPI app found, please install one to continue", Toast.LENGTH_SHORT).show();
        }

    }

    private void PaypalPayment(String amount) {
        try {
            PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf(amount)), selectedCurrency, resources.getString(R.string.pay_for_) + resources.getString(R.string.app_name) + " " + resources.getString(R.string._wallet), PayPalPayment.PAYMENT_INTENT_SALE);
            Intent intent = new Intent(AddMoneyActivity.this, PaymentActivity.class);
            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
            intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
            startActivityForResult(intent, PAYPAL_REQUEST_CODE);
        } catch (Resources.NotFoundException e) {
            Toast.makeText(AddMoneyActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }
    }

    public void performCharge(Charge charge) {
        //create a Charge object
        loadingDialog.show();

        PaystackSdk.chargeCard(AddMoneyActivity.this, charge, new Paystack.TransactionCallback() {
            @Override
            public void onSuccess(final Transaction transaction) {
                Log.d("transaction", transaction.toString());
                psQueue = Volley.newRequestQueue(getApplicationContext());
                psQueue.getCache().clear();
                String url = resources.getString(R.string.api) + "paystack_response";
                HashMap<String, String> params = new HashMap<>();
                params.put("reference", transaction.getReference());
                params.put("amount", amount);

                Log.d("paystack resposne data", new JSONObject(params).toString());

                pay_stack_response_request = new JsonObjectRequest(url, new JSONObject(params),
                        response -> {

                            loadingDialog.dismiss();
                            try {
                                if (TextUtils.equals(response.getString("status"), "true")) {
                                    Intent intent = new Intent(getApplicationContext(), TransactionSuccessActivity.class);
                                    intent.putExtra("selected", selectedCurrencySymbol);
                                    intent.putExtra("TID", transaction.getReference());
                                    intent.putExtra("TAMOUNT", String.valueOf((int) amountFloat));
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(getApplicationContext(), TransactionFailActivity.class);
                                    intent.putExtra("selected", selectedCurrencySymbol);
                                    intent.putExtra("TID", transaction.getReference());
                                    intent.putExtra("TAMOUNT", String.valueOf((int) amountFloat));
                                    startActivity(intent);

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }, error -> {
                    Log.e("**VolleyError", "error" + error.getMessage());

                    if (error instanceof TimeoutError) {

                        pay_stack_response_request.setShouldCache(false);
                        mQueue.add(pay_stack_response_request);
                    }

                }) {
                    @Override
                    protected Map<String, String> getParams() throws
                            AuthFailureError {
                        return super.getParams();
                    }

                    @Override
                    public Map<String, String> getHeaders() {

                        Map<String, String> headers = new HashMap<>();
                        CurrentUser user = userLocalStore.getLoggedInUser();
                        String token = "Bearer " + user.getToken();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", token);
                        headers.put("x-localization", LocaleHelper.getPersist(context));
                        return headers;
                    }
                };
                pay_stack_response_request.setShouldCache(false);
                psQueue.add(pay_stack_response_request);

                // This is called only after transaction is deemed successful.
                // Retrieve the transaction, and send its reference to your server
                // for verification.
            }

            @Override
            public void beforeValidate(Transaction transaction) {
                Log.d("beforevalidate", transaction.getReference());
                // This is called only before requesting OTP.
                // Save reference so you may send to server. If
                // error occurs with OTP, you should still verify on server.
            }

            @Override
            public void onError(Throwable error, Transaction transaction) {
                loadingDialog.dismiss();
                Intent intent = new Intent(getApplicationContext(), TransactionFailActivity.class);
                intent.putExtra("TID", transaction.getReference());
                intent.putExtra("TAMOUNT", String.valueOf(amountFloat));
                startActivity(intent);
                //handle error here
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("paypal", "in activity result");

        if (requestCode == PAYPAL_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                assert data != null;
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if ((confirmation != null)) {
                    JSONObject paymentdetails = confirmation.toJSONObject();
                    try {
                        final JSONObject responsedetails = (JSONObject) paymentdetails.get("response");

                        //  call paypal_response  api after paypal gateway response
                        loadingDialog.show();
                        mQueue = Volley.newRequestQueue(getApplicationContext());
                        mQueue.getCache().clear();

                        String url = resources.getString(R.string.api) + "paypal_response";

                        HashMap<String, String> params = new HashMap<>();
                        params.put("member_id", custId);
                        params.put("id", responsedetails.getString("id"));
                        params.put("amount", amount);
                        params.put("state", responsedetails.getString("state"));
                        paypal_response_request = new JsonObjectRequest(url, new JSONObject(params),
                                response -> {

                                    loadingDialog.dismiss();
                                    try {
                                        if (TextUtils.equals(response.getString("status"), "true")) {
                                            Intent intent = new Intent(getApplicationContext(), TransactionSuccessActivity.class);
                                            intent.putExtra("selected", selectedCurrencySymbol);
                                            intent.putExtra("TID", responsedetails.getString("id"));
                                            intent.putExtra("TAMOUNT", String.valueOf(amountFloat));
                                            startActivity(intent);
                                        } else {
                                            Intent intent = new Intent(getApplicationContext(), TransactionFailActivity.class);
                                            intent.putExtra("selected", selectedCurrencySymbol);
                                            intent.putExtra("TID", responsedetails.getString("id"));
                                            intent.putExtra("TAMOUNT", String.valueOf(amountFloat));
                                            startActivity(intent);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }, error -> {
                            Log.e("**VolleyError", "error" + error.getMessage());

                            if (error instanceof TimeoutError) {

                                paypal_response_request.setShouldCache(false);
                                mQueue.add(paypal_response_request);
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws
                                    AuthFailureError {
                                return super.getParams();
                            }

                            @Override
                            public Map<String, String> getHeaders() {

                                Map<String, String> headers = new HashMap<>();
                                CurrentUser user = userLocalStore.getLoggedInUser();
                                String token = "Bearer " + user.getToken();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", token);
                                headers.put("x-localization", LocaleHelper.getPersist(context));
                                return headers;
                            }
                        };
                        paypal_response_request.setShouldCache(false);
                        mQueue.add(payu_response_request);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), resources.getString(R.string.cancel), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            Toast.makeText(getApplicationContext(), resources.getString(R.string.invalid), Toast.LENGTH_SHORT).show();
        } else if (requestCode == CFPaymentService.REQ_CODE) {
            loadingDialog.show();
            if (data != null) {
                Bundle bundle = data.getExtras();
                final JSONObject json = new JSONObject();
                if (bundle != null) {
                    for (String key : bundle.keySet()) {
                        if (bundle.getString(key) != null) {
                            Log.d("TAG", key + " : " + bundle.getString(key));
                            try {
                                json.put(key, JSONObject.wrap(bundle.get(key)));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    mQueue = Volley.newRequestQueue(getApplicationContext());
                    mQueue.getCache().clear();

                    String url = resources.getString(R.string.api) + "cashfree_response";

                    cash_free_response_request = new JsonObjectRequest(url, json,
                            response -> {

                                Log.d("cashfree_response", response.toString());

                                loadingDialog.dismiss();
                                try {
                                    if (TextUtils.equals(response.getString("status"), "true")) {
                                        Intent intent = new Intent(getApplicationContext(), TransactionSuccessActivity.class);
                                        intent.putExtra("selected", selectedCurrencySymbol);
                                        intent.putExtra("TID", json.getString("referenceId"));
                                        intent.putExtra("TAMOUNT", String.valueOf(amountFloat));
                                        startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(getApplicationContext(), TransactionFailActivity.class);
                                        intent.putExtra("selected", selectedCurrencySymbol);
                                        try {
                                            intent.putExtra("TID", json.getString("referenceId"));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        intent.putExtra("TAMOUNT", String.valueOf(amountFloat));
                                        startActivity(intent);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }, error -> {
                        Log.e("**VolleyError", "error" + error.getMessage());

                        if (error instanceof TimeoutError) {

                            cash_free_response_request.setShouldCache(false);
                            mQueue.add(cash_free_response_request);
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws
                                AuthFailureError {
                            return super.getParams();
                        }

                        @Override
                        public Map<String, String> getHeaders() {

                            Map<String, String> headers = new HashMap<>();
                            CurrentUser user = userLocalStore.getLoggedInUser();
                            String token = "Bearer " + user.getToken();
                            headers.put("Content-Type", "application/json");
                            headers.put("Authorization", token);
                            headers.put("x-localization", LocaleHelper.getPersist(context));
                            return headers;
                        }
                    };
                    cash_free_response_request.setShouldCache(false);
                    mQueue.add(cash_free_response_request);
                }
            }
        } else if (requestCode == TEZ_REQUEST_CODE) {
            // Process based on the data in response.
            if (resultCode == RESULT_OK) {
                Log.d("google pay result ok", data.toString() + "------" + data.getStringExtra("Status") + " " + data.getStringExtra("response"));
                Toast.makeText(getApplicationContext(), data.getStringExtra("Status"), Toast.LENGTH_SHORT).show();
                SharedPreferences sharedPreferences = getSharedPreferences("gpay", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (TextUtils.equals(data.getStringExtra("Status"), "SUCCESS")) {
                    googlePayResponse("true", sharedPreferences.getString("amount", "0"), sharedPreferences.getString("tid", ""), sharedPreferences.getString("oid", ""));
                } else {
                    googlePayResponse("false", sharedPreferences.getString("amount", "0"), sharedPreferences.getString("tid", ""), sharedPreferences.getString("oid", ""));
                }

                editor.clear();
            } else {

                try {
                    Toast.makeText(getApplicationContext(), data.getStringExtra("Status"), Toast.LENGTH_SHORT).show();
                } catch (Exception ignored) {

                }
            }
        } else if (requestCode == UPI_PAYMENT) {
            if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                if (data != null) {
                    String trxt = data.getStringExtra("response");
                    Log.e("UPI", "onActivityResult: " + trxt);
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add(trxt);
                    upiPaymentDataOperation(dataList);
                } else {
                    Log.e("UPI", "onActivityResult: " + "Return data is null");
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Transaction cancelled.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Transaction Failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isConnectionAvailable(AddMoneyActivity.this)) {
            String str = data.get(0);
            Log.e("UPIPAY", "upiPaymentDataOperation: " + str);
            String paymentCancel = "";
            if (str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String[] response = str.split("&");
            for (String s : response) {
                String[] equalStr = s.split("=");
                if (equalStr.length >= 2) {
                    if (equalStr[0].equalsIgnoreCase("Status")) {
                        status = equalStr[1].toLowerCase();
                    } else if (equalStr[0].equalsIgnoreCase("ApprovalRefNo") || equalStr[0].equalsIgnoreCase("txnRef")) {
                        approvalRefNo = equalStr[1];
                    }
                } else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }
            if (status.equals("success")) {
                //Code to handle successful transaction here.
                Log.e("UPI", "payment successfull: " + approvalRefNo);
                String targetKey = "txnId=";
                int startIndex = str.indexOf(targetKey) + targetKey.length();
                int endIndex = str.indexOf('&', startIndex);
                String extractedValue = str.substring(startIndex, endIndex);
                upiPayResponse(extractedValue);
            } else if ("Payment cancelled by user.".equals(paymentCancel)) {
                //Toast.makeText(AddMoneyActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "Cancelled by user: " + approvalRefNo);
                Toast.makeText(getApplicationContext(), "Transaction cancelled.", Toast.LENGTH_SHORT).show();
            } else {
                //  Toast.makeText(AddMoneyActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "failed payment: " + approvalRefNo);
                Toast.makeText(getApplicationContext(), "Transaction Failed.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("UPI", "Internet issue: ");
            Toast.makeText(AddMoneyActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }
    }

    public void upiPayResponse(String tid) {
        Intent intent = new Intent(getApplicationContext(), TransactionSuccessActivity.class);
        intent.putExtra("selected", selectedCurrencySymbol);
        intent.putExtra("TID", tid);
        intent.putExtra("TAMOUNT", String.valueOf(amountFloat));
        startActivity(intent);
    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable();
        }
        return false;
    }

    private void initiateInstamojoSDKPayment(String orderID) {
        Instamojo.getInstance().initiatePayment(this, orderID, AddMoneyActivity.this);
    }

    @Override
    public void onInstamojoPaymentComplete(String orderID, String transactionID, String paymentID, String paymentStatus) {
        //instamojo
        Log.d("Instamojo success", "Payment complete. Order ID: " + orderID + ", Transaction ID: " + transactionID
                + ", Payment ID:" + paymentID + ", Status: " + paymentStatus);
        InstamojoResponse(paymentStatus, paymentID);
    }

    @Override
    public void onPaymentCancelled() {
        //instamojo
        Log.d("Instamojo cancel", "Payment cancelled");
        Toast.makeText(getApplicationContext(), resources.getString(R.string.payment_cancelled), Toast.LENGTH_SHORT).show();
        InstamojoResponse("cancel", "");
    }

    @Override
    public void onInitiatePaymentFailure(String errorMessage) {
        //instamojo
        Log.d("Instamojo fail", "Initiate payment failed " + errorMessage);
        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    public void InstamojoResponse(String status, final String payid) {
        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();

        loadingDialog.show();
        String url = resources.getString(R.string.api) + "instamojo_response";

        HashMap<String, String> params = new HashMap<>();
        params.put("status", status);
        params.put("amount", String.valueOf(amount));
        params.put("member_id", custId);
        params.put("order_id", depositId);
        params.put("payment_id", payid);

        Log.d(url, new JSONObject(params).toString());
        instamojo_response_request = new JsonObjectRequest(url, new JSONObject(params),
                response -> {
                    loadingDialog.dismiss();
                    try {
                        if (TextUtils.equals(response.getString("status"), "true")) {
                            Intent intent = new Intent(getApplicationContext(), TransactionSuccessActivity.class);
                            intent.putExtra("selected", selectedCurrencySymbol);
                            intent.putExtra("TID", payid);
                            intent.putExtra("TAMOUNT", String.valueOf(amountFloat));
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(getApplicationContext(), TransactionFailActivity.class);
                            intent.putExtra("selected", selectedCurrencySymbol);
                            intent.putExtra("TID", payid);
                            intent.putExtra("TAMOUNT", String.valueOf(amountFloat));
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            Log.e("**VolleyError", "error" + error.getMessage());

            if (error instanceof TimeoutError) {

                instamojo_response_request.setShouldCache(false);
                mQueue.add(instamojo_response_request);
            }

        }) {
            @Override
            protected Map<String, String> getParams() throws
                    AuthFailureError {
                return super.getParams();
            }

            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> headers = new HashMap<>();
                CurrentUser user = userLocalStore.getLoggedInUser();
                String credentials = user.getUsername() + ":" + user.getPassword();
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                String token = "Bearer " + user.getToken();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                headers.put("x-localization", LocaleHelper.getPersist(context));
                return headers;
            }
        };
        instamojo_response_request.setShouldCache(false);
        mQueue.add(instamojo_response_request);
    }

    public void startRazorpayPayment(String key_id, String order_id, String currency, String amount) {
        checkout.setKeyID(key_id);
        checkout.setImage(R.drawable.battlemanialogo);
        final Activity activity = this;

        try {
            JSONObject options = new JSONObject();
            options.put("name", resources.getString(R.string.app_name));
            //options.put("description", "Reference No. #123456");
            //options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            options.put("order_id", order_id);
            options.put("currency", currency);
            options.put("amount", amount);

            checkout.open(activity, options);
        } catch (Exception e) {
            Log.e("Razorpay", "Error in starting Razorpay Checkout", e);
        }
    }

    @Override
    public void onPaymentSuccess(String s, PaymentData paymentData) {
        //Log.d("final razor pay"+s,paymentData.toString());
        //razorpay
        //Log.d(s,paymentData.getOrderId()+"------"+paymentData.getData()+"------"+paymentData.getPaymentId()+"------"+paymentData.getExternalWallet()+"------"+paymentData.getSignature());
        Checkout.clearUserData(getApplicationContext());
        RazorpayResponse("true", paymentData.getOrderId(), paymentData.getPaymentId(), paymentData.getSignature());

    }

    @Override
    public void onPaymentError(int i, String s, PaymentData paymentData) {
        //razorpay

        try {
            Checkout.clearUserData(getApplicationContext());
            Log.d(String.valueOf(i), s + "------" + paymentData.getData() + "------" + paymentData.getPaymentId() + "------" + paymentData.getSignature());
            if (TextUtils.equals(paymentData.getPaymentId(), null) && TextUtils.equals(paymentData.getPaymentId(), null)) {
                startActivity(new Intent(getApplicationContext(), TransactionFailActivity.class));
                loadingDialog.dismiss();
            } else {
                RazorpayResponse("false", paymentData.getOrderId(), paymentData.getPaymentId(), paymentData.getSignature());
            }
        } catch (Exception e) {
            //Toast.makeText(getApplicationContext(),"Toast error",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), TransactionFailActivity.class));
            loadingDialog.dismiss();
        }
    }

    public void RazorpayResponse(String status, String orderid, final String payid, String signature) {
        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();

        loadingDialog.show();
        String url = resources.getString(R.string.api) + "razorpay_response";

        HashMap<String, String> params = new HashMap<>();
        params.put("status", status);
        params.put("amount", amount);
        params.put("member_id", custId);
        params.put("razorpay_order_id", orderid);
        params.put("razorpay_payment_id", payid);
        params.put("razorpay_signature", signature);
        params.put("receipt", receipt);

        Log.d(url, new JSONObject(params).toString());
        razorpay_response_request = new JsonObjectRequest(url, new JSONObject(params),
                response -> {

                    loadingDialog.dismiss();
                    try {
                        if (TextUtils.equals(response.getString("status"), "true")) {
                            Intent intent = new Intent(getApplicationContext(), TransactionSuccessActivity.class);
                            intent.putExtra("selected", selectedCurrencySymbol);
                            intent.putExtra("TID", payid);
                            intent.putExtra("TAMOUNT", String.valueOf(amountFloat));
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(getApplicationContext(), TransactionFailActivity.class);
                            intent.putExtra("selected", selectedCurrencySymbol);
                            intent.putExtra("TID", payid);
                            intent.putExtra("TAMOUNT", String.valueOf(amountFloat));
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            Log.e("**VolleyError", "error" + error.getMessage());

            if (error instanceof TimeoutError) {

                razorpay_response_request.setShouldCache(false);
                mQueue.add(razorpay_response_request);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws
                    AuthFailureError {
                return super.getParams();
            }

            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> headers = new HashMap<>();
                CurrentUser user = userLocalStore.getLoggedInUser();
                String credentials = user.getUsername() + ":" + user.getPassword();
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                String token = "Bearer " + user.getToken();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                headers.put("x-localization", LocaleHelper.getPersist(context));
                return headers;
            }
        };
        razorpay_response_request.setShouldCache(false);
        mQueue.add(razorpay_response_request);
    }

    public void googlePayResponse(String status, String amount, final String tid, String oid) {

        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();

        loadingDialog.show();
        String url = resources.getString(R.string.api) + "googlepay_response";

        HashMap<String, String> params = new HashMap<>();
        params.put("status", status);
        params.put("amount", amount);
        params.put("member_id", custId);
        params.put("transaction_id", tid);
        params.put("order_id", oid);

        Log.d(url, new JSONObject(params).toString());
        google_pay_response_request = new JsonObjectRequest(url, new JSONObject(params),
                response -> {

                    Log.d("googlepay resp", response.toString());
                    loadingDialog.dismiss();
                    try {
                        if (TextUtils.equals(response.getString("status"), "true")) {
                            Intent intent = new Intent(getApplicationContext(), TransactionSuccessActivity.class);
                            intent.putExtra("selected", selectedCurrencySymbol);
                            intent.putExtra("TID", tid);
                            intent.putExtra("TAMOUNT", String.valueOf(amountFloat));
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(getApplicationContext(), TransactionFailActivity.class);
                            intent.putExtra("TID", tid);
                            intent.putExtra("selected", selectedCurrencySymbol);
                            intent.putExtra("TAMOUNT", String.valueOf(amountFloat));
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {

            NetworkResponse response = error.networkResponse;
            String errorString = new String(response.data);

            Log.e("**VolleyError", "error" + errorString);

            if (error instanceof TimeoutError) {

                google_pay_response_request.setShouldCache(false);
                mQueue.add(google_pay_response_request);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws
                    AuthFailureError {
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
        google_pay_response_request.setShouldCache(false);
        mQueue.add(google_pay_response_request);
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array) {

        addMoneyOption.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);
                rdbtn = new RadioButton(this);
                rdbtn.setId(i);
                rdbtn.setText(json.getString("payment_name"));

                rdbtn.setTextColor(ContextCompat.getColor(this, R.color.white));
                int color = ContextCompat.getColor(this, R.color.orange); // Replace with your color
                ColorStateList colorStateList = ColorStateList.valueOf(color);
                CompoundButtonCompat.setButtonTintList(rdbtn, colorStateList);

                if (i == 0) {
                    rdbtn.setChecked(true);

                    modeStatus = json.getString("payment_status");
                    payment = json.getString("payment_name");
                    selectedCurrency = json.getString("currency_code");
                    selectedCurrencySymbol = json.getString("currency_symbol");
                    point = json.getString("currency_point");
                    pointInt = Integer.parseInt(point);

                    if (TextUtils.equals(rdbtn.getText().toString(), "PayStack")) {
                        paystackll.setVisibility(View.VISIBLE);
                        if (TextUtils.equals(json.getString("payment_status"), "Test")) {
                            paystackTestNote.setVisibility(View.VISIBLE);
                        } else {
                            paystackTestNote.setVisibility(View.GONE);
                        }
                    } else {
                        paystackll.setVisibility(View.GONE);
                        paystackTestNote.setVisibility(View.GONE);
                    }
                }

                if (TextUtils.equals(json.getString("payment_name"), "PayPal")) {
                    if (TextUtils.equals(json.getString("payment_status"), "Sandbox")) {
                        Log.d("paypall", "sandbox : " + json.getString("client_id"));
                        config = new PayPalConfiguration().environment(PayPalConfiguration.ENVIRONMENT_SANDBOX).clientId(json.getString("client_id"));
                        Intent intent = new Intent(AddMoneyActivity.this, PayPalService.class);
                        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                        startService(intent);
                    } else {
                        // Log.d("paypall", "production");
                        config = new PayPalConfiguration().environment(PayPalConfiguration.ENVIRONMENT_PRODUCTION).clientId(json.getString("client_id"));
                        Intent intent = new Intent(AddMoneyActivity.this, PayPalService.class);
                        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                        startService(intent);
                    }
                } else if (TextUtils.equals(json.getString("payment_name"), "PayStack")) {
                    if (TextUtils.equals(json.getString("payment_status"), "Test")) {
                        // paystacktestnote.setVisibility(View.VISIBLE);
                    }

                    PaystackSdk.initialize(getApplicationContext());
                    PaystackSdk.setPublicKey(json.getString("public_key"));
                    secretKey = json.getString("secret_key");

                } else if (TextUtils.equals(json.getString("payment_name"), "Instamojo")) {
                    if (TextUtils.equals(json.getString("payment_status"), "Test")) {
                        Instamojo.getInstance().initialize(AddMoneyActivity.this, Instamojo.Environment.TEST);
                    } else {
                        Instamojo.getInstance().initialize(AddMoneyActivity.this, Instamojo.Environment.PRODUCTION);
                    }
                } else if (TextUtils.equals(json.getString("payment_name"), "Razorpay")) {

                    checkout = new Checkout();
                    Checkout.preload(getApplicationContext());

                } else if (TextUtils.equals(json.getString("payment_name"), "Cashfree")) {
                    cfAppid = json.getString("app_id");
                } else if (TextUtils.equals(json.getString("payment_name"), "Google Pay")) {
                    upi_id = json.getString("upi_id");
                } else if (TextUtils.equals(json.getString("payment_name"), "Offline")) {
                    paymentDescription = json.getString("payment_description");

                } else if (TextUtils.equals(json.getString("payment_name"), "PayU")) {
                    puMId = json.getString("mid");
                    puMKey = json.getString("mkey");
                    puSalt = json.getString("salt");

                } else if (TextUtils.equals(json.getString("payment_name"), "Tron")) {


                } else if (TextUtils.equals(json.getString("payment_name"), "UPITranzact")) {
                    spMId = json.getString("mid");
                    spMKey = json.getString("mkey");
                    spEUrl = json.getString("e_url");
                } else if (TextUtils.equals(json.getString("payment_name"), "UPITranzact Business")) {
                    upitz_mid = json.getString("mid");
                    public_key = json.getString("pkey");
                    secret_key = json.getString("mkey");
                }
                addMoneyOption.addView(rdbtn);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static String stripZeros(String number) {
        return new BigDecimal(number).stripTrailingZeros().toPlainString();
    }

    public void createOrder(Context context, String orderId, String tId, String amt) {
        loadingDialog.show();
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = spEUrl;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loadingDialog.dismiss();
                        try {
                            JSONObject jsonResponse = new JSONObject(response);

                            // Check the status
                            boolean status = jsonResponse.optBoolean("status", false);
                            String message = jsonResponse.optString("message", "Order creation failed");

                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                            if (status) {
                                JSONObject result = jsonResponse.optJSONObject("result");
                                if (result != null) {
                                    String orderId = result.optString("orderId");
                                    String paymentUrl = result.optString("payment_url");

                                    System.out.println("Order ID: " + orderId);
                                    System.out.println("Payment URL: " + paymentUrl);

                                    // Assuming you are in another activity
                                    Intent intent = new Intent(AddMoneyActivity.this, WebActivity.class);
                                    intent.putExtra("P_URL", paymentUrl);
                                    intent.putExtra("amount", amt);
                                    intent.putExtra("tid", tId);
                                    intent.putExtra("oid", orderId);
                                    startActivity(intent);
                                    finish();

                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Response parsing error", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Print the error
                        loadingDialog.dismiss();
                        error.printStackTrace();
                        Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("public_key", spMId);
                params.put("secret_key", spMKey);
                params.put("customer_mobile", user.getPhone());
                params.put("amount", amt);
                params.put("order_id", orderId);
                params.put("redirect_url", "https://upitranzact.com");
                params.put("note", "Add Money");
                return params;
            }
        };

        queue.add(stringRequest);
    }

    public void createOrderRequest(Context context, String orderId, String tId, String amt) {
        UpiTranzactSDK sdk = new UpiTranzactSDK(AddMoneyActivity.this, public_key, secret_key, upitz_mid);
        sdk.startPayment(amt,
                orderId, "https://google.com", user.getFirstName(), user.getEmail(), user.getPhone(), new PaymentCallback() {
                    @Override
                    public void onPaymentSuccess(String order_id, String message) {
                        UPITranzactBusinessResponse("true", amount, tId, orderId);
                    }

                    @Override
                    public void onPaymentFailed(String order_id, String message) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        UPITranzactBusinessResponse("false", amount, tId, orderId);

                    }
                });
    }

    public void UPITranzactBusinessResponse(String status, String amount, final String transactionId, String orderId) {

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.getCache().clear();

        String url = resources.getString(R.string.api) + "upitranzact_response";

        HashMap<String, String> params = new HashMap<>();
        params.put("status", status);
        params.put("amount", amount);
        params.put("member_id", user.getMemberid());
        params.put("transaction_id", transactionId);
        params.put("order_id", orderId);

        Log.d("GooglePayResponseURL", new JSONObject(params).toString());
        upiTranzactResponseRequest = new JsonObjectRequest(url, new JSONObject(params), response -> {

            Log.d("GooglePayResponse", response.toString());
//            loadingDialog.dismiss();
            try {
                if (TextUtils.equals(response.getString("status"), "true")) {
                    Intent intent = new Intent(getApplicationContext(), TansactionSuccessActivity.class);
                    intent.putExtra("selected", "₹");
                    intent.putExtra("TID", transactionId);
                    intent.putExtra("TAMOUNT", amount);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplicationContext(), TransactionFailActivity.class);
                    intent.putExtra("TID", transactionId);
                    intent.putExtra("selected", "₹");
                    intent.putExtra("TAMOUNT", amount);
                    startActivity(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {

            NetworkResponse response = error.networkResponse;
            String errorString = new String(response.data);

            Log.e("VolleyError", "error: " + errorString);

            if (error instanceof TimeoutError) {
                upiTranzactResponseRequest.setShouldCache(false);
                requestQueue.add(upiTranzactResponseRequest);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                CurrentUser user = userLocalStore.getLoggedInUser();

                String credentials = user.getUsername() + ":" + user.getPassword();
                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                headers.put("x-localization", LocaleHelper.getPersist(context));
                return headers;
            }
        };
        upiTranzactResponseRequest.setShouldCache(false);
        requestQueue.add(upiTranzactResponseRequest);
    }

    void PayUAddMoney() {

        userLocalStore = new UserLocalStore(getApplicationContext());

        loadingDialog.show();
        //add_money for google pay start
        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();

        String url = getResources().getString(R.string.api) + "add_money";
        HashMap<String, String> params = new HashMap<>();

        params.put("payment_name", payment);
        params.put("CUST_ID", custId);
        params.put("TXN_AMOUNT", amount);
        params.put("firstName", user.getUsername());
        params.put("Email", user.getEmail());
        params.put("phone", user.getPhone());

        Log.d(url, new JSONObject(params).toString());

        request = new JsonObjectRequest(url, new JSONObject(params),
                response -> {
                    Log.d("payu addmoney", response.toString());
                    loadingDialog.dismiss();
                    try {

                        if (TextUtils.equals(response.getString("status"), "false")) {
                            Toast.makeText(AddMoneyActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        } else {

                            oId = response.getString("order_id");
                            String tid = response.getString("transaction_id");
                            PayU(tid);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            Log.e("**VolleyError", "error" + error.getMessage());

            if (error instanceof TimeoutError) {

                request.setShouldCache(false);
                mQueue.add(request);

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws
                    AuthFailureError {
                return super.getParams();
            }

            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> headers = new HashMap<>();
                String token = "Bearer " + user.getToken();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                return headers;
            }
        };
        request.setShouldCache(false);
        mQueue.add(request);

    }

    void PayU(String tid) {

        boolean isProduction = true;

        PayUPaymentParams.Builder builder = new PayUPaymentParams.Builder();
        builder.setAmount(String.valueOf(amountFloat))
                .setIsProduction(isProduction)
                .setProductInfo(getResources().getString(R.string.app_name) + " Wallet Balance")
                .setKey(puMKey)
                .setPhone(user.getPhone())
                .setTransactionId(tid)
                .setFirstName(user.getUsername())
                .setEmail(user.getEmail())
                .setSurl(getResources().getString(R.string.api) + "payu_succ_fail")
                .setFurl(getResources().getString(R.string.api) + "payu_succ_fail");

        PayUPaymentParams payUPaymentParams = builder.build();


        //declare paymentParam object
        try {
            PayUCheckoutPro.open(
                    AddMoneyActivity.this,
                    payUPaymentParams,
                    new PayUCheckoutProListener() {

                        @Override
                        public void onPaymentSuccess(Object response) {
                            Log.d("PAYMENT", response.toString());
                            HashMap<String, Object> result = (HashMap<String, Object>) response;
                            String payuResponse = (String) result.get(PayUCheckoutProConstants.CP_PAYU_RESPONSE);
                            String mode = "";
                            try {
                                JSONObject parentObject = new JSONObject(payuResponse);
                                JSONObject userDetails = parentObject.getJSONObject("result");
                                String transaction_id = "";
                                mode = userDetails.getString("mode");
                                if (mode.contains("UPI")) {
                                    transaction_id = userDetails.getString("mihpayid");
                                    if (TextUtils.equals(userDetails.getString("status"), "success")) {
                                        PayUResponse("true", transaction_id, tid);
                                    } else {
                                        PayUResponse("false", transaction_id, tid);
                                    }
                                } else {
                                    transaction_id = parentObject.getString("id");
                                    if (TextUtils.equals(parentObject.getString("status"), "success")) {
                                        PayUResponse("true", transaction_id, tid);
                                    } else {
                                        PayUResponse("false", transaction_id, tid);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                try {
                                    String transaction_id = "";
                                    JSONObject parentObject = new JSONObject(payuResponse);
                                    transaction_id = parentObject.getString("id");
                                    if (TextUtils.equals(parentObject.getString("status"), "success")) {
                                        PayUResponse("true", transaction_id, tid);
                                    } else {
                                        PayUResponse("false", transaction_id, tid);
                                    }
                                } catch (JSONException ex) {
                                    ex.printStackTrace();
                                }

                            }
                        }

                        @Override
                        public void onPaymentFailure(Object response) {
                            //Cast response object to HashMap
                            Log.d("PAYMENT", "FAILED" + response.toString());
                            HashMap<String, Object> result = (HashMap<String, Object>) response;
                            String payuResponse = (String) result.get(PayUCheckoutProConstants.CP_PAYU_RESPONSE);
                            String merchantResponse = (String) result.get(PayUCheckoutProConstants.CP_MERCHANT_RESPONSE);
                            String responcefail = "";
                            String transaction_id = "";
                            try {
                                assert payuResponse != null;
                                JSONObject obj = new JSONObject(payuResponse);
                                PayUResponse("false", transaction_id, tid);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onPaymentCancel(boolean isTxnInitiated) {
                            Log.d("PAYMENT", "CANCEL" + isTxnInitiated);
                            String transaction_id = "";
                            PayUResponse("false", transaction_id, tid);
                        }

                        @Override
                        public void onError(ErrorResponse errorResponse) {
                            String errorMessage = errorResponse.getErrorMessage();
                            Toast.makeText(AddMoneyActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void setWebViewProperties(WebView webView, Object o) {
                            //For setting webview properties, if any. Check Customized Integration section for more details on this
                        }

                        @Override
                        public void generateHash(HashMap<String, String> valueMap, PayUHashGenerationListener hashGenerationListener) {
                            String hashName = valueMap.get(PayUCheckoutProConstants.CP_HASH_NAME);
                            String hashData = valueMap.get(PayUCheckoutProConstants.CP_HASH_STRING);
                            if (!TextUtils.isEmpty(hashName) && !TextUtils.isEmpty(hashData)) {
                                //Do not generate hash from local, it needs to be calculated from server side only. Here, hashString contains hash created from your server side.
                                //String hash = hashString;
                                String hash = null;
                                assert hashName != null;
                                if (hashName.equals(PayUCheckoutProConstants.CP_LOOKUP_API_HASH)) {
                                    hash = HashGenerationUtils.INSTANCE.generateHashFromSDK(hashData, puSalt, "e425e539233044146a2d185a346978794afd7c66");
                                    Log.d("------------", "merchant");
                                } else {
                                    Log.d("-----not-------", "merchant");
                                    hash = HashGenerationUtils.INSTANCE.generateHashFromSDK(hashData, puSalt, null);
                                }

                                HashMap<String, String> dataMap = new HashMap<>();
                                dataMap.put(hashName, hash);
                                hashGenerationListener.onHashGenerated(dataMap);
                            }
                        }
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void PayUResponse(String status, String transactionId, String tid) {

        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();

        loadingDialog.show();
        String url = getResources().getString(R.string.api) + "payu_response";

        HashMap<String, String> params = new HashMap<>();
        params.put("status", status);
        params.put("amount", amount);
        params.put("member_id", custId);
        params.put("transaction_id", transactionId);
        params.put("order_id", oId);
        params.put("payment_name", payment);
        params.put("custom_transaction_id", tid);

        Log.d(url, new JSONObject(params).toString());
        payu_response_request = new JsonObjectRequest(url, new JSONObject(params),
                response -> {

                    Log.d("payu resp", response.toString());
                    loadingDialog.dismiss();
                    try {
                        if (TextUtils.equals(response.getString("status"), "true")) {
                            Intent intent = new Intent(getApplicationContext(), TransactionSuccessActivity.class);
                            intent.putExtra("selected", selectedCurrencySymbol);
                            intent.putExtra("TID", transactionId);
                            intent.putExtra("TAMOUNT", String.valueOf(amountFloat));
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(getApplicationContext(), TransactionFailActivity.class);
                            intent.putExtra("TID", transactionId);
                            intent.putExtra("selected", selectedCurrencySymbol);
                            intent.putExtra("TAMOUNT", String.valueOf(amountFloat));
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {

            NetworkResponse response = error.networkResponse;
            String errorString = new String(response.data);

            Log.e("**VolleyError", "error" + errorString);

            if (error instanceof TimeoutError) {

                payu_response_request.setShouldCache(false);
                mQueue.add(payu_response_request);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws
                    AuthFailureError {
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
                return headers;
            }
        };
        payu_response_request.setShouldCache(false);
        mQueue.add(payu_response_request);
    }

    @Override
    public void onPaymentVerify(String orderID) {
        Log.d("OK CASHFREE TEST", orderID);
        loadingDialog.show();
        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();

        String url = resources.getString(R.string.api) + "cashfree_response";

        HashMap<String, String> params = new HashMap<>();
        params.put("response", orderID);

        cashfree_response_request = new JsonObjectRequest(url, new JSONObject(params),
                response -> {
                    Log.d("cashfree_response", response.toString());
                    loadingDialog.dismiss();
                    try {
                        if (TextUtils.equals(response.getString("status"), "true")) {
                            Intent intent = new Intent(getApplicationContext(), TransactionSuccessActivity.class);
                            intent.putExtra("selected", selectedCurrencySymbol);
                            intent.putExtra("TID", orderID);
                            intent.putExtra("TAMOUNT", String.valueOf(amountFloat));
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(getApplicationContext(), TransactionFailActivity.class);
                            intent.putExtra("selected", selectedCurrencySymbol);
                            try {
                                intent.putExtra("TID", ""/*json.getString("referenceId")*/);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            intent.putExtra("TAMOUNT", String.valueOf(amountFloat));
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            Log.e("**VolleyError", "error" + error.getMessage());

            if (error instanceof TimeoutError) {

                cashfree_response_request.setShouldCache(false);
                mQueue.add(cashfree_response_request);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws
                    AuthFailureError {
                return super.getParams();
            }

            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> headers = new HashMap<>();
                CurrentUser user = userLocalStore.getLoggedInUser();
                String token = "Bearer " + user.getToken();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                headers.put("x-localization", LocaleHelper.getPersist(context));
                return headers;
            }
        };
        cashfree_response_request.setShouldCache(false);
        mQueue.add(cashfree_response_request);
    }

    @Override
    public void onPaymentFailure(CFErrorResponse cfErrorResponse, String s) {
        Log.d("OK CASHFREE FAIL", cfErrorResponse.getMessage());
        Log.d("OK CASHFREE FAIL", s);
        loadingDialog.dismiss();
        Toast.makeText(getApplicationContext(), cfErrorResponse.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
