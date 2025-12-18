//For update
package com.app.rewardsbattle.ui.activities;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getExternalStoragePublicDirectory;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.BuildConfig;
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.utils.AnalyticsUtil;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AppUpdateActivity extends AppCompatActivity {

    TextView appupdatetitle;
    TextView appname;
    CardView cardUpdate;
    String downloadUrl = "";
    String latestVersionName = null;
    RequestQueue vQueue;
    LinearLayout llDownload;
    TextView progressDownload;
    ProgressBar progressBar;
    SharedPreferences sp;
    Boolean downloaded = false;
    TextView updateBtn;
    TextView updateInfo;

    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_app_update);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(this);

        context = LocaleHelper.setLocale(AppUpdateActivity.this);
        resources = context.getResources();

        llDownload = findViewById(R.id.lldownload);
        progressDownload = findViewById(R.id.progressdownload);
        progressBar = findViewById(R.id.progressbar);
        updateBtn = findViewById(R.id.updatebtn);
        appname = findViewById(R.id.appnameid);
        appupdatetitle = findViewById(R.id.appupdatetitleid);
        updateInfo = findViewById(R.id.updateinfo);
        cardUpdate = findViewById(R.id.cardupdate);

        appupdatetitle.setText(resources.getString(R.string.app_update));
        appname.setText(resources.getString(R.string.app_name));
        updateBtn.setText(resources.getString(R.string.update_now));

        //get download url,version and update description for update app
        vQueue = Volley.newRequestQueue(getApplicationContext());

        String versionUrl = resources.getString(R.string.api) + "version/android";

        JsonObjectRequest versionRequest = new JsonObjectRequest(Request.Method.GET, versionUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    latestVersionName = response.getString("version");
                    downloadUrl = response.getString("url");
                    updateInfo.setText(Html.fromHtml(response.getString("description")));
                    updateInfo.setClickable(true);
                    updateInfo.setMovementMethod(LinkMovementMethod.getInstance());
                } catch (
                        JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.e("error", error.toString())) {
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
        vQueue.add(versionRequest);

        sp = getSharedPreferences("downloadinfo", Context.MODE_PRIVATE);
        downloaded = Boolean.valueOf(sp.getString("downloaded", "false"));

        if (downloaded) {
            sp = getSharedPreferences("downloadinfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("downloaded", "false");
            editor.apply();
            installapp(resources.getString(R.string.app_name) + "-" + latestVersionName + ".apk");
        }

        cardUpdate.setOnClickListener(v -> {

            if (!downloaded) {
                // check storage permission for download new app version

               /* if (Build.VERSION_CODES.R >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    } else {
                        //storage permission not grant request for it
                        requestStoragePermission();
                    }
                } else {
                    updateBtn.setText(resources.getString(R.string.downloading___));
                    cardUpdate.setEnabled(false);
                    update(downloadUrl);
                }*/

                updateBtn.setText(resources.getString(R.string.downloading___));
                cardUpdate.setEnabled(false);
                update(downloadUrl);
            }
        });
    }

    private void update(String downloadUrl) {

        final String fileName = resources.getString(R.string.app_name) + "-" + latestVersionName + ".apk";
        Log.d("download url", downloadUrl);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setDescription(resources.getString(R.string.updating___));
        request.setTitle(fileName);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, fileName);

        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);

        new Thread(new Runnable() {

            @SuppressLint("Range")
            @Override
            public void run() {

                boolean downloading = true;
                while (downloading) {
                    DownloadManager.Query q = new DownloadManager.Query();
                    Cursor cursor = manager.query(q);
                    cursor.moveToFirst();
                    @SuppressLint("Range") final int bytes_downloaded = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    @SuppressLint("Range") final int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                    }
                    runOnUiThread(() -> {
                        Log.d((int) (((bytes_downloaded * 100L) / bytes_total)) + "/100", "OK");
                        llDownload.setVisibility(View.VISIBLE);
                        progressBar.setMax(100);
                        progressBar.setProgress((int) (((bytes_downloaded * 100L) / bytes_total)));
                        progressDownload.setText((int) (((bytes_downloaded * 100L) / bytes_total)) + "/100");
                    });
                    cursor.close();
                }

            }
        }).start();

        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {

                downloaded = true;
                sp = getSharedPreferences("downloadinfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("downloaded", "true");
                editor.apply();

                updateBtn.setText(resources.getString(R.string.download_completed));
                cardUpdate.setEnabled(false);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    //check install unknown app permission
                    if (getPackageManager().canRequestPackageInstalls()) {
                        installapp(fileName);
                    } else {
                        Intent intentper = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intentper, 23);
                    }
                } else {
                    installapp(fileName);
                }
                unregisterReceiver(this);

            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), RECEIVER_EXPORTED);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateBtn.setText(resources.getString(R.string.downloading___));
                cardUpdate.setEnabled(false);
                update(downloadUrl);
            } else {
                Toast.makeText(this, resources.getString(R.string.storage_permission_denied), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 23) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, resources.getString(R.string.permission_granted), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, resources.getString(R.string.install_permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void installapp(String fileName) {

        //after download install app

        if (TextUtils.equals(latestVersionName, "")) {


            vQueue = Volley.newRequestQueue(getApplicationContext());

            String vurl = resources.getString(R.string.api) + "version/android";

            JsonObjectRequest vrequest = new JsonObjectRequest(Request.Method.GET, vurl, null, response -> {
                try {
                    latestVersionName = response.getString("version");
                    downloadUrl = response.getString("url");
                    updateInfo.setText(Html.fromHtml(response.getString("description")));
                    updateInfo.setClickable(true);
                    updateInfo.setMovementMethod(LinkMovementMethod.getInstance());

                    String newfileName = resources.getString(R.string.app_name) + "-" + latestVersionName + ".apk";

                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        String PATH = getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS) + "/" + newfileName;
                        Log.d("Path", PATH);
                        File file = new File(PATH);
                        if (file.exists()) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uriFromFile(getApplicationContext(), new File(PATH)), "application/vnd.android.package-archive");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            try {
                                getApplicationContext().startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                                Log.e("TAG", "Error in opening the file!");
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), resources.getString(R.string.app_not_found_in_download), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Intent install = new Intent(Intent.ACTION_VIEW);
                        install.setDataAndType(Uri.fromFile(new File(getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS) + "/" + newfileName)), "application/vnd.android.package-archive");
                        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(install);
                        finish();
                    }

                } catch (
                        JSONException e) {
                }
            }, error -> Log.e("error", error.toString())) {
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
            vQueue.add(vrequest);


        } else {
            String PATH = getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS) + "/" + fileName;
            Log.d("Path", PATH);
            File file = new File(PATH);
            if (file.exists()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uriFromFile(getApplicationContext(), new File(PATH)), "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    getApplicationContext().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Log.e("TAG", "Error in opening the file!");
                }
            } else {
                Toast.makeText(getApplicationContext(), resources.getString(R.string.app_not_found_in_download), Toast.LENGTH_LONG).show();
            }
        }

    }

    Uri uriFromFile(Context context, File file) {
        return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //after install unknown app permission app restart and install new version after download
        if (downloaded) {
            sp = getSharedPreferences("downloadinfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("downloaded", "false");
            editor.apply();
            updateBtn.setText(resources.getString(R.string.download_completed));
            cardUpdate.setEnabled(false);
            installapp(resources.getString(R.string.app_name) + "-" + latestVersionName + ".apk");
        }
    }
}
