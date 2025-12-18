//For withdraw money from app wallet
package com.app.rewardsbattle.ui.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.CompoundButtonCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.utils.AnalyticsUtil;
import com.app.rewardsbattle.utils.CustomTypefaceSpan;
import com.app.rewardsbattle.utils.LoadingDialog;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.app.rewardsbattle.utils.UserLocalStore;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WithdrawMoneyActivity extends AppCompatActivity {

    TextView withdrawmoneytitle;
    TextView withdrawtotitle;
    TextView onlywithdarwwinmoneytitle;
    ImageView back;
    String amount;
    int amountInt = 0;
    RequestQueue mQueue;
    LoadingDialog loadingDialog;
    TextView withdrawTitle;
    RadioGroup withdrawOption;
    String withdrawMethod = "";
    RadioButton rdbtn;
    JSONArray array;
    EditText paytm_number;
    EditText withdraw_amount;
    String selectedField = "";
    TextInputLayout til;
    TextView withdrawNote;
    String point = "";
    int pointInt = 0;
    String selectedCurrencySymbol = "";
    String minWithdraw = "";

    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_withdraw_money);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        context = LocaleHelper.setLocale(WithdrawMoneyActivity.this);
        resources = context.getResources();

        back = findViewById(R.id.backfromwithdrawmoney);
        back.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), MyWalletActivity.class);
            intent.putExtra("N", "2");
            startActivity(intent);
        });


        withdrawmoneytitle = findViewById(R.id.withdrawmoneytitleid);
        withdrawtotitle = findViewById(R.id.withdrawtotitleid);
        onlywithdarwwinmoneytitle = findViewById(R.id.onlywithdarwwinmoneytitleid);

        withdrawmoneytitle.setText(resources.getString(R.string.withdraw_money));
        withdrawtotitle.setText(resources.getString(R.string.withdraw_to_));
        onlywithdarwwinmoneytitle.setText(resources.getString(R.string.you_can_only_withdraw_win_money));
        til = findViewById(R.id.numbertil);

        loadingDialog = new LoadingDialog(WithdrawMoneyActivity.this);
        loadingDialog.show();
        paytm_number = findViewById(R.id.paytm_number);
        withdraw_amount = findViewById(R.id.withdraw_amount);
        final Button withdraw_btn = findViewById(R.id.withdraw_btn);
        withdrawTitle = findViewById(R.id.withdrawtitle);
        withdrawOption = findViewById(R.id.withdraw_option);
        withdrawNote = findViewById(R.id.withdraw_note);

        mQueue = Volley.newRequestQueue(WithdrawMoneyActivity.this);
        mQueue.getCache().clear();

        final UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());
        String url = resources.getString(R.string.api) + "withdraw_method";

        final JsonObjectRequest request = new JsonObjectRequest(url, null,
                response -> {
                    Log.d("withdraw", response.toString());
                    loadingDialog.dismiss();
                    try {
                        JSONArray arr = response.getJSONArray("withdraw_method");
                        minWithdraw = response.getString("min_withdrawal");

                        array = arr;
                        if (!TextUtils.equals(response.getString("withdraw_method"), "[]")) {
                            JSON_PARSE_DATA_AFTER_WEBCALL(arr);
                        } else {
                            Toast.makeText(WithdrawMoneyActivity.this, resources.getString(R.string.withdraw_method_not_available), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MyWalletActivity.class));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("**VolleyError", "error" + error.getMessage())) {
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


        withdrawOption.setOnCheckedChangeListener((group, checkedId) -> {

            rdbtn = findViewById(checkedId);
            try {
                withdrawMethod = rdbtn.getText().toString();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject json = null;
                    try {
                        json = array.getJSONObject(i);

                        if (TextUtils.equals(json.getString("withdraw_method"), withdrawMethod)) {
                            paytm_number.setText("");
                            selectedCurrencySymbol = json.getString("currency_symbol");
                            point = json.getString("withdraw_method_currency_point");
                            pointInt = Integer.parseInt(point);
                            if (withdraw_amount.getText().toString().trim().length() > 0) {

                                withdrawNote.setText(TextUtils.concat(resources.getString(R.string.you_will_get_), " ", selectedCurrencySymbol, String.format("%.2f", Double.parseDouble(withdraw_amount.getText().toString().trim()) / (double) pointInt)));
                                if (TextUtils.equals(selectedCurrencySymbol, "₹")) {
                                    Typeface font = Typeface.DEFAULT;
                                    SpannableStringBuilder SS = new SpannableStringBuilder(resources.getString(R.string.Rs));
                                    SS.setSpan(new CustomTypefaceSpan("", font), 0, 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                                    withdrawNote.setText(TextUtils.concat(Html.fromHtml(getString(R.string.you_will_get_)), " ", SS, Html.fromHtml(String.format("%.2f", Double.parseDouble(withdraw_amount.getText().toString().trim()) / (double) pointInt))));
                                    withdrawNote.setClickable(true);
                                    withdrawNote.setMovementMethod(LinkMovementMethod.getInstance());
                                }

                            } else {
                                withdrawNote.setText("");
                            }


                            selectedField = json.getString("withdraw_method_field");

                            if (TextUtils.equals(json.getString("withdraw_method_field"), "mobile no")) {
                                til.setHint(resources.getString(R.string.mobile_number));
                                paytm_number.setInputType(InputType.TYPE_CLASS_PHONE);
                            } else if (TextUtils.equals(json.getString("withdraw_method_field"), "email")) {
                                til.setHint(resources.getString(R.string.email));
                                paytm_number.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                            } else if (TextUtils.equals(json.getString("withdraw_method_field"), "UPI ID")) {
                                til.setHint(resources.getString(R.string.upi_id));
                                paytm_number.setInputType(InputType.TYPE_CLASS_TEXT);
                            } else if (TextUtils.equals(json.getString("withdraw_method_field"), "Wallet Address")) {
                                til.setHint(resources.getString(R.string.withdraw_wallet_address));
                                paytm_number.setInputType(InputType.TYPE_CLASS_TEXT);
                            }

                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            withdrawTitle.setText(resources.getString(R.string.withdraw_to_) + " " + withdrawMethod);
        });

        withdraw_btn.setEnabled(false);
        withdraw_btn.setBackgroundColor(getResources().getColor(R.color.newdisablegreen));
        paytm_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


                if (TextUtils.equals(selectedField, "mobile no")) {
                    if (charSequence.length() < 7 || charSequence.length() > 15) {
                        withdraw_btn.setEnabled(false);
                        withdraw_btn.setBackgroundColor(getResources().getColor(R.color.newdisablegreen));
                    } else {
                        if (!TextUtils.isEmpty(withdraw_amount.getText().toString())) {
                            withdraw_btn.setEnabled(true);
                            withdraw_btn.setBackgroundColor(getResources().getColor(R.color.orange));
                        }
                    }
                } else {
                    withdraw_btn.setEnabled(true);
                    withdraw_btn.setBackgroundColor(getResources().getColor(R.color.orange));
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        withdraw_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    if (!TextUtils.isEmpty(paytm_number.getText().toString()))
                        withdraw_btn.setEnabled(true);
                    withdraw_btn.setBackgroundColor(getResources().getColor(R.color.orange));

                    withdrawNote.setText(TextUtils.concat(resources.getString(R.string.you_will_get_), " ", selectedCurrencySymbol, String.format("%.2f", Double.parseDouble(String.valueOf(charSequence)) / (double) pointInt)));
                    if (TextUtils.equals(selectedCurrencySymbol, "₹")) {
                        Typeface font = Typeface.DEFAULT;
                        SpannableStringBuilder SS = new SpannableStringBuilder(resources.getString(R.string.Rs));
                        SS.setSpan(new CustomTypefaceSpan("", font), 0, 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        withdrawNote.setText(TextUtils.concat(Html.fromHtml(getString(R.string.you_will_get_)), " ", SS, Html.fromHtml(String.format("%.2f", Double.parseDouble(String.valueOf(charSequence)) / (double) pointInt))));
                        withdrawNote.setClickable(true);
                        withdrawNote.setMovementMethod(LinkMovementMethod.getInstance());
                    }

                } else {
                    withdrawNote.setText("");
                    withdraw_btn.setEnabled(false);
                    withdraw_btn.setBackgroundColor(getResources().getColor(R.color.newdisablegreen));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        withdraw_btn.setOnClickListener(view -> {
            String paytmnumber = paytm_number.getText().toString().trim();

            amount = withdraw_amount.getText().toString().trim();
            try {
                amountInt = Integer.parseInt(amount);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            if (TextUtils.equals(selectedField, "mobile no")) {
                if (TextUtils.equals(paytmnumber, "")) {
                    Toast.makeText(getApplicationContext(), resources.getString(R.string.please_enter_mobile_number), Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (TextUtils.equals(selectedField, "email")) {
                if (TextUtils.equals(paytmnumber, "")) {
                    Toast.makeText(getApplicationContext(), resources.getString(R.string.please_enter_email), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(paytmnumber).matches()) {
                    Toast.makeText(getApplicationContext(), resources.getString(R.string.wrong_email_address___), Toast.LENGTH_SHORT).show();
                    return;
                }

            } else if (TextUtils.equals(selectedField, "Wallet Address")) {
                if (TextUtils.equals(paytmnumber, "")) {
                    Toast.makeText(getApplicationContext(), resources.getString(R.string.please_enter_wallet_address), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!TextUtils.equals(String.valueOf(paytmnumber.length()), "34")) {
                    Toast.makeText(getApplicationContext(), resources.getString(R.string.please_enter_valid_wallet_address), Toast.LENGTH_SHORT).show();
                    return;
                }

            } else if (TextUtils.equals(selectedField, "UPI ID")) {
                if (TextUtils.equals(paytmnumber, "")) {
                    Toast.makeText(getApplicationContext(), resources.getString(R.string.please_enter_upi_id), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            int minWithdrawInt = Integer.parseInt(minWithdraw);
            if (amountInt < minWithdrawInt) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(WithdrawMoneyActivity.this);
                builder.setTitle(getString(R.string.error));
                SpannableStringBuilder builderp = new SpannableStringBuilder();
                builderp.append(Html.fromHtml(getString(R.string.enter_minmum) + " "))
                        .append(" ", new ImageSpan(getApplicationContext(), R.drawable.resize_coin, ImageSpan.ALIGN_BASELINE), 0)
                        .append(" ")
                        .append(Html.fromHtml(minWithdraw) + ".");
                builder.setMessage(builderp);
                builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {

                });
                builder.show();
                builder.create();
                return;
            }

            /*withdraw api call start*/
            loadingDialog.show();
            mQueue = Volley.newRequestQueue(WithdrawMoneyActivity.this);
            mQueue.getCache().clear();

            final UserLocalStore userLocalStore1 = new UserLocalStore(getApplicationContext());
            final CurrentUser user = userLocalStore1.getLoggedInUser();
            String url1 = resources.getString(R.string.api) + "withdraw";

            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("submit", "withdraw");
            hashMap.put("amount", withdraw_amount.getText().toString().trim());
            hashMap.put("pyatmnumber", paytm_number.getText().toString().trim());
            hashMap.put("withdraw_method", withdrawMethod);
            hashMap.put("member_id", user.getMemberid());

            final JsonObjectRequest request1 = new JsonObjectRequest(url1, new JSONObject(hashMap),
                    response -> {

                        Log.d("withdr", response.toString());
                        loadingDialog.dismiss();
                        try {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(WithdrawMoneyActivity.this);
                            if (TextUtils.equals(response.getString("status"), "true")) {
                                builder.setTitle(resources.getString(R.string.success));
                            } else {
                                builder.setTitle(resources.getString(R.string.error));
                            }
                            builder.setMessage(response.getString("message"));
                            builder.setPositiveButton(resources.getString(R.string.ok), (dialogInterface, i) -> {
                            });
                            builder.show();
                            builder.create();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, error -> Log.e("**VolleyError", "error" + error.getMessage())) {
                @Override
                public Map<String, String> getHeaders() {

                    Map<String, String> headers = new HashMap<>();
                    CurrentUser user = userLocalStore1.getLoggedInUser();
                    String token = "Bearer " + user.getToken();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", token);
                    headers.put("x-localization", LocaleHelper.getPersist(context));

                    return headers;
                }
            };
            request1.setShouldCache(false);
            mQueue.add(request1);
            /*withdraw api call end*/

            paytm_number.setText("");
            withdraw_amount.setText("");
            withdraw_btn.setEnabled(false);
        });
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array) {

        withdrawOption.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);
                rdbtn = new RadioButton(this);
                rdbtn.setId(i);
                rdbtn.setText(json.getString("withdraw_method"));

                rdbtn.setTextColor(ContextCompat.getColor(this, R.color.white));
                int color = ContextCompat.getColor(this, R.color.orange); // Replace with your color
                ColorStateList colorStateList = ColorStateList.valueOf(color);
                CompoundButtonCompat.setButtonTintList(rdbtn, colorStateList);

                if (i == 0) {
                    selectedCurrencySymbol = json.getString("currency_symbol");
                    point = json.getString("withdraw_method_currency_point");
                    pointInt = Integer.parseInt(point);
                    rdbtn.setChecked(true);
                    withdrawMethod = rdbtn.getText().toString();
                    withdrawTitle.setText(resources.getString(R.string.withdraw_to_) + " " + withdrawMethod);
                    selectedField = json.getString("withdraw_method_field");
                    if (TextUtils.equals(json.getString("withdraw_method_field"), "mobile no")) {
                        til.setHint(resources.getString(R.string.mobile_number));
                        paytm_number.setInputType(InputType.TYPE_CLASS_PHONE);
                    } else if (TextUtils.equals(json.getString("withdraw_method_field"), "email")) {
                        til.setHint(resources.getString(R.string.email));
                        paytm_number.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    } else if (TextUtils.equals(json.getString("withdraw_method_field"), "UPI ID")) {
                        til.setHint(resources.getString(R.string.upi_id));
                        paytm_number.setInputType(InputType.TYPE_CLASS_TEXT);
                    } else if (TextUtils.equals(json.getString("withdraw_method_field"), "Wallet Address")) {
                        til.setHint(resources.getString(R.string.withdraw_wallet_address));
                        paytm_number.setInputType(InputType.TYPE_CLASS_TEXT);
                    }
                }
                withdrawOption.addView(rdbtn);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
