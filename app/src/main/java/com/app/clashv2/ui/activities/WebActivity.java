package com.app.clashv2.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.clashv2.R;
import com.app.clashv2.models.CurrentUser;
import com.app.clashv2.utils.LocaleHelper;
import com.app.clashv2.utils.UserLocalStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class WebActivity extends AppCompatActivity {
    private Context context;
    private Resources resources;
    private String paymentUrl, amount, transactionId, orderId;
    private boolean isTransactionHandled = false;
    private UserLocalStore userLocalStore;
    private CurrentUser user;
    private String customerId = "";
    private RequestQueue requestQueue;
    //    private LoadingDialog loadingDialog;
    private JsonObjectRequest googlePayResponseRequest;
    private ImageView backFromWebView;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        context = LocaleHelper.setLocale(WebActivity.this);
        resources = context.getResources();
//        loadingDialog = new LoadingDialog(WebActivity.this);

        Intent intent = getIntent();
        paymentUrl = intent.getStringExtra("P_URL");
        amount = intent.getStringExtra("amount");
        transactionId = intent.getStringExtra("tid");
        orderId = intent.getStringExtra("oid");

        userLocalStore = new UserLocalStore(getApplicationContext());
        user = userLocalStore.getLoggedInUser();
        customerId = user.getMemberid();

        backFromWebView = findViewById(R.id.backfromwebview);
        webView = findViewById(R.id.webview);

        webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webView.getSettings();
        webView.getSettings().setJavaScriptEnabled(true);
        String userAgent = "Mozilla/5.0 (Linux; Android 11; Pixel 4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Mobile Safari/537.36";
        webView.getSettings().setUserAgentString(userAgent);
        webView.getSettings().setDomStorageEnabled(true);

        webView.setWebChromeClient(new WebChromeClient());

        // Add a JavaScript interface to handle redirection
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        if (paymentUrl != null && !paymentUrl.isEmpty()) {
            webView.loadUrl(paymentUrl);
        } else {
            webView.loadUrl("about:blank");
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && (url.startsWith("upi://") || url.startsWith("intent://"))) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        startActivity(intent);
                        return true;  // Indicate that the host application handles the URL
                    } catch (URISyntaxException e) {
                        // Handle exception if URI is invalid
                    }
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        backFromWebView.setOnClickListener(view -> finish());
    }

    public class WebAppInterface {
        private Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void transactionSuccess() {
            if (!isTransactionHandled) {
                isTransactionHandled = true;
                googlePayResponse("true", amount, transactionId, orderId);
            }
        }

        @JavascriptInterface
        public void transactionFailed() {
            if (!isTransactionHandled) {
                isTransactionHandled = true;
                googlePayResponse("false", amount, transactionId, orderId);
            }
        }
    }

    public void googlePayResponse(String status, String amount, final String transactionId, String orderId) {

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.getCache().clear();

//        loadingDialog.show();
        String url = resources.getString(R.string.api) + "googlepay_response";

        HashMap<String, String> params = new HashMap<>();
        params.put("status", status);
        params.put("amount", amount);
        params.put("member_id", customerId);
        params.put("transaction_id", transactionId);
        params.put("order_id", orderId);

        Log.d("GooglePayResponseURL", new JSONObject(params).toString());
        googlePayResponseRequest = new JsonObjectRequest(url, new JSONObject(params), response -> {

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
                googlePayResponseRequest.setShouldCache(false);
                requestQueue.add(googlePayResponseRequest);
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
        googlePayResponseRequest.setShouldCache(false);
        requestQueue.add(googlePayResponseRequest);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}