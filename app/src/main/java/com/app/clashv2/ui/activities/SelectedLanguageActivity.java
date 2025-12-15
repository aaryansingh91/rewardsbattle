package com.app.clashv2.ui.activities;

import static com.android.volley.Request.Method.GET;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.clashv2.R;
import com.app.clashv2.utils.AnalyticsUtil;
import com.app.clashv2.utils.LoadingDialog;
import com.app.clashv2.utils.LocaleHelper;
import com.app.clashv2.utils.UserLocalStore;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SelectedLanguageActivity extends AppCompatActivity {


    Button buttoncontinue;
    Context context;
    Resources resources;
    RequestQueue mQueue;
    LoadingDialog loadingDialog;
    RadioGroup langRg;
    JSONArray langArray = new JSONArray();
    String selectedLangKey="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_selected_language);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        context = LocaleHelper.setLocale(SelectedLanguageActivity.this);
        resources = context.getResources();
        loadingDialog=new LoadingDialog(this);
        loadingDialog.show();

        getLanguage();

        buttoncontinue = findViewById(R.id.btncontinue);
        langRg= findViewById(R.id.langll);

        langRg.setOnCheckedChangeListener((group, checkedId) -> {
            try {
                selectedLangKey=langArray.getJSONObject(checkedId).keys().next();
            } catch (JSONException e) {
                e.printStackTrace();
            }


        });

        buttoncontinue.setOnClickListener(view -> {
            LocaleHelper.persist(SelectedLanguageActivity.this, selectedLangKey);
            startActivity(new Intent(getApplicationContext(), IndroductionActivity.class));

        });

    }

    void getLanguage(){

        /*all_country api call start*/
        mQueue = Volley.newRequestQueue(getApplicationContext());
        mQueue.getCache().clear();
        String url = resources.getString(R.string.api) + "all_language";
        final UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());

        final JsonObjectRequest request = new JsonObjectRequest(GET, url, null,
                response -> {

                    try {
                        Log.d("language",String.valueOf(response));

                        JSONObject lang= response.getJSONObject("supported_language");
                        Iterator x = lang.keys();


                        int id=0;
                        while (x.hasNext()){
                            String key = (String) x.next();
                            if(id==0){
                                selectedLangKey=key;
                            }
                            JSONObject obj=new JSONObject();
                            obj.put(key,lang.get(key));
                            langArray.put(obj);

                            RadioButton rdbtn = new RadioButton(SelectedLanguageActivity.this);
                            if(TextUtils.equals(selectedLangKey,key)){
                                rdbtn.setChecked(true);
                            }
                            rdbtn.setId(id);
                            String langText=lang.get(key).toString();
                            langText=langText.substring(0,1).toUpperCase()+langText.substring(1);
                            rdbtn.setText(langText);
                            rdbtn.setBackgroundDrawable(resources.getDrawable(R.drawable.border));
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.setMargins(0,20,0,20);
                            rdbtn.setLayoutParams(params);
                            rdbtn.setPadding(50,50,50,50);
                            langRg.addView(rdbtn);
                            id++;
                        }

                        Log.d("lang",langArray.toString());

                        JSONObject rtl= response.getJSONObject("rtl_supported_language");
                        Iterator y = rtl.keys();
                        JSONArray rtlArray = new JSONArray();

                        while (y.hasNext()){
                            String key = (String) y.next();
                            JSONObject obj=new JSONObject();
                            obj.put(key,rtl.get(key));
                            rtlArray.put(obj);
                        }

                        Log.d("rtl",rtlArray.toString());

                        //JSON_PARSE_DATA_AFTER_WEBCALL(arr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    loadingDialog.dismiss();
                }, error -> Log.e("**VolleyError", "error" + error.getMessage())){

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
}