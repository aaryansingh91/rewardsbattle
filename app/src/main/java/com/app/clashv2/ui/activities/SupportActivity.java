package com.app.clashv2.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.clashv2.R;
import com.app.clashv2.utils.LoadingDialog;
import com.app.clashv2.utils.LocaleHelper;

public class SupportActivity extends AppCompatActivity {

    Context context;
    Resources resources;
    WebView webView;
    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        context = LocaleHelper.setLocale(SupportActivity.this);
        resources = context.getResources();

        loadingDialog = new LoadingDialog(SupportActivity.this);

        loadingDialog.show();

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String email = intent.getStringExtra("email");

        if (name == null || name.isEmpty() || email == null || email.isEmpty()) {
            Toast.makeText(this, "Name or email is missing. Cannot start chat.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        webView = findViewById(R.id.webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Dismiss the dialog when the page is fully loaded
                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient());

        webView.loadUrl("https://tawk.to/chat/676986b8af5bfec1dbe0b8ee/1ifq3cgu9");
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