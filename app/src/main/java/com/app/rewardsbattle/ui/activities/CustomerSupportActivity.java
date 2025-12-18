//For detail about customer support options
package com.app.rewardsbattle.ui.activities;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.utils.AnalyticsUtil;
import com.app.rewardsbattle.utils.LoadingDialog;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.app.rewardsbattle.utils.UserLocalStore;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CustomerSupportActivity extends AppCompatActivity {

    RequestQueue mQueue;
    LoadingDialog loadingDialog;
    String customerAddress = "";
    String customerPhone = "";
    String customerCode = "";
    String custEmail = "";
    String custStreet = "";
    String custTime = "";
    String custInstaId = "";

    LinearLayout addll;
    LinearLayout phonell;
    LinearLayout emailll;
    LinearLayout install;
    LinearLayout streetll;
    LinearLayout timell;
    Button chat;

    TextView customersuporttitle;
    TextView nocontactsupport;
    LinearLayout contactsupport;

    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_customer_support);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        //check baner ads enable or not
        SharedPreferences sp = getSharedPreferences("SMINFO", MODE_PRIVATE);
        if (TextUtils.equals(sp.getString("baner", "no"), "yes")) {

            AdView mAdView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);

            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    mAdView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                    // Code to be executed when an ad request fails.
                    mAdView.setVisibility(View.GONE);
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }

                @Override
                public void onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                }

                

                @Override
                public void onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                }
            });
        }

        context = LocaleHelper.setLocale(CustomerSupportActivity.this);
        resources = context.getResources();

        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();

        customersuporttitle = findViewById(R.id.customersuporttitleid);
        chat = findViewById(R.id.chat);
        addll = findViewById(R.id.addll);
        phonell = findViewById(R.id.phonell);
        emailll = findViewById(R.id.emailll);
        install = findViewById(R.id.install);
        streetll = findViewById(R.id.streetll);
        timell = findViewById(R.id.timell);

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CustomerSupportActivity.this, SupportActivity.class);
                intent.putExtra("name", "fullName");
                intent.putExtra("email", "fullEmail");
                startActivity(intent);
            }
        });

        ImageView back = findViewById(R.id.backfromcustomersupport);
        final TextView addressView = findViewById(R.id.address);
        final TextView phoneView = findViewById(R.id.phone);
        final TextView emailView = findViewById(R.id.email);
        final TextView streetView = findViewById(R.id.street);
        final TextView timeView = findViewById(R.id.time);
        final TextView instagramView = findViewById(R.id.instagram);
        final ImageView call = findViewById(R.id.call);
        final ImageView sms = findViewById(R.id.sms);
        final ImageView mail = findViewById(R.id.mail);
        final ImageView insta = findViewById(R.id.insta);

        customersuporttitle.setText(resources.getString(R.string.customer_support));

        // customer_support api call
        mQueue = Volley.newRequestQueue(this);
        mQueue.getCache().clear();

        contactsupport = findViewById(R.id.contactsupport);
        nocontactsupport = findViewById(R.id.nocontactsupport);

        String url = resources.getString(R.string.api) + "customer_support";

        @SuppressLint("SetTextI18n") JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {

            loadingDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(response.getString("customer_support"));

                customerAddress = jsonObject.getString("company_address");
                customerPhone = jsonObject.getString("comapny_phone");
                customerCode = jsonObject.getString("comapny_country_code");
                custEmail = jsonObject.getString("company_email");
                custStreet = jsonObject.getString("company_street");
                custTime = jsonObject.getString("company_time");
                custInstaId = jsonObject.getString("insta_link");

                if (TextUtils.equals(customerAddress, "") || TextUtils.equals(customerAddress, "null")) {
                    addll.setVisibility(View.GONE);
                }

                if (TextUtils.equals(customerPhone, "") || TextUtils.equals(customerPhone, "null")) {
                    phonell.setVisibility(View.GONE);
                }

                if (TextUtils.equals(custEmail, "") || TextUtils.equals(custEmail, "null")) {
                    emailll.setVisibility(View.GONE);
                }

                if (TextUtils.equals(custInstaId, "") || TextUtils.equals(custInstaId, "null")) {
                    install.setVisibility(View.GONE);
                }

                if (TextUtils.equals(custStreet, "") || TextUtils.equals(custStreet, "null")) {
                    streetll.setVisibility(View.GONE);
                }

                if (TextUtils.equals(custTime, "") || TextUtils.equals(custTime, "null")) {
                    timell.setVisibility(View.GONE);
                }

                if (addll.getVisibility() == View.GONE && phonell.getVisibility() == View.GONE && emailll.getVisibility() == View.GONE && install.getVisibility() == View.GONE && streetll.getVisibility() == View.GONE && timell.getVisibility() == View.GONE) {
                    contactsupport.setVisibility(View.GONE);
                    nocontactsupport.setVisibility(View.VISIBLE);
                }

                addressView.setText(resources.getString(R.string.address__) + customerAddress);
                phoneView.setText(resources.getString(R.string.phone__) + customerCode + customerPhone);
                emailView.setText(resources.getString(R.string.email__) + custEmail);
                instagramView.setText(resources.getString(R.string.instagram__) + custInstaId);
                streetView.setText(resources.getString(R.string.street__) + custStreet);
                timeView.setText(resources.getString(R.string.time__) + custTime);

                call.setOnClickListener(view -> {
                    Intent call_intent = new Intent(Intent.ACTION_DIAL);
                    call_intent.setData(Uri.parse("tel:" + customerCode + customerPhone));
                    startActivity(call_intent);
                });

                sms.setOnClickListener(view -> openWhatsApp(customerCode + customerPhone));

                mail.setOnClickListener(view -> {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{custEmail});
                    try {
                        startActivity(Intent.createChooser(i, resources.getString(R.string.send_mail___)));
                    }
                    catch (ActivityNotFoundException ex) {
                        Toast.makeText(getApplicationContext(), resources.getString(R.string.there_are_no_email_apps_installed), Toast.LENGTH_SHORT).show();
                    }
                });

                insta.setOnClickListener(v -> {
                    Uri uri = Uri.parse("https://www.instagram.com/" + custInstaId + "/");
                    Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

                    likeIng.setPackage("com.instagram.android");

                    try {
                        startActivity(likeIng);
                    }
                    catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                uri));
                    }
                });
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.e("error", error.toString())) {
            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> headers = new HashMap<>();
                UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());
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

        back.setOnClickListener(view -> onBackPressed());
    }

    private void openWhatsApp(String number) {
        try {
            number = number.replace(" ", "").replace("+", "");

            Intent sendIntent = new Intent("android.intent.action.SEND");
            sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
            sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators(number) + "@s.whatsapp.net");
            //sendIntent.putExtra(Intent.EXTRA_TEXT,"sample text you want to send along with the image");
            startActivity(sendIntent);

        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), resources.getString(R.string.whatsapp_not_found), Toast.LENGTH_SHORT).show();
        }
    }
}
