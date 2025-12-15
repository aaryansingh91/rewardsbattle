package com.app.clashv2.ui.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.clashv2.R;
import com.app.clashv2.models.CurrentUser;
import com.app.clashv2.ui.activities.AddMoneyActivity;
import com.app.clashv2.ui.activities.CustomerSupportActivity;
import com.app.clashv2.ui.activities.MeActivity;
import com.app.clashv2.ui.activities.MyWalletActivity;
import com.app.clashv2.ui.activities.TransactionActivity;
import com.app.clashv2.ui.activities.WithdrawMoneyActivity;
import com.app.clashv2.utils.AnalyticsUtil;
import com.app.clashv2.utils.LoadingDialog;
import com.app.clashv2.utils.LocaleHelper;
import com.app.clashv2.utils.UserLocalStore;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;
import me.toptas.fancyshowcase.listener.DismissListener;

public class WalletFragment extends Fragment {
    TextView balance;
    TextView topBal;
    TextView winMoneyTv;
    TextView joinMoneyTv;
    RequestQueue dQueue;
    UserLocalStore userLocalStore;
    LoadingDialog loadingDialog;
    String winMoney = "";
    String joinMoney = "";
    TextView username;
    LinearLayout addBtn;
    LinearLayout withdrawBtn;
    LinearLayout transaction;
    TextView earnings;
    TextView payouts;
    String from = "";

    Context context;
    Resources resources;
    ImageView visit_web, visit_support;
    CircleImageView profile;
    LinearLayout visit_wallet;

    public WalletFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_wallet, container, false);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(requireActivity());
        AnalyticsUtil.initialize(firebaseAnalytics);
        AnalyticsUtil.recordScreenView(getActivity());

        context = LocaleHelper.setLocale(requireActivity());
        resources = context.getResources();

        SharedPreferences sp = context.getSharedPreferences("SMINFO", MODE_PRIVATE);
        if (TextUtils.equals(sp.getString("baner", "no"), "yes")) {

            AdView mAdView = root.findViewById(R.id.adView);
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

        loadingDialog = new LoadingDialog(requireActivity());
        loadingDialog.show();


        balance = root.findViewById(R.id.balanceinwallet);
        topBal = root.findViewById(R.id.balance);
        winMoneyTv = root.findViewById(R.id.winmoneyinwallet);
        joinMoneyTv = root.findViewById(R.id.joinmoneyinwallet);
        earnings = root.findViewById(R.id.earnings);
        payouts = root.findViewById(R.id.payouts);
        username = root.findViewById(R.id.username);

        visit_web = root.findViewById(R.id.visit_web);
        visit_support = root.findViewById(R.id.visit_support);
        profile = root.findViewById(R.id.profile_image);
        visit_wallet = root.findViewById(R.id.visit_wallet);

        Intent intent = requireActivity().getIntent();
        from = intent.getStringExtra("FROM");

        addBtn = root.findViewById(R.id.addbtn);
        withdrawBtn = root.findViewById(R.id.withdrawbtn);
        transaction = root.findViewById(R.id.transaction);
        addBtn.setOnClickListener(v -> {
            Intent intent1 = new Intent(requireContext(), AddMoneyActivity.class);
            startActivity(intent1);
        });
        withdrawBtn.setOnClickListener(v -> {
            Intent intent12 = new Intent(requireContext(), WithdrawMoneyActivity.class);
            startActivity(intent12);
        });
        transaction.setOnClickListener(v -> {
            Intent intent12 = new Intent(requireContext(), TransactionActivity.class);
            startActivity(intent12);
        });

        visit_web.setOnClickListener(view -> {
            String url = resources.getString(R.string.url);
            Intent intentweb = new Intent(Intent.ACTION_VIEW);
            intentweb.setData(Uri.parse(url));

            try {
                startActivity(intentweb);
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(getActivity(), "No browser app found to open the link", Toast.LENGTH_SHORT).show();
            }
        });

        visit_support.setOnClickListener(view -> {
            Intent intentsupport = new Intent(getActivity(), CustomerSupportActivity.class);
            startActivity(intentsupport);
        });

        profile.setOnClickListener(view -> {
            Intent intentprofile = new Intent(getActivity(), MeActivity.class);
            startActivity(intentprofile);
        });

        visit_wallet.setOnClickListener(v -> {
            Intent intentwallet = new Intent(getContext(), MyWalletActivity.class);
            intentwallet.putExtra("FROM", "PLAY");
            startActivity(intentwallet);
        });

        userLocalStore = new UserLocalStore(requireContext());

        //dashboard api call
        dashBoard();

        new FancyShowCaseQueue()
                .add(addView(addBtn, resources.getString(R.string.show_case_4), "4"))
                .add(addView(withdrawBtn, resources.getString(R.string.show_case_5), "5"))
                .show();

        return root;
    }

    public void dashBoard() {

        dQueue = Volley.newRequestQueue(requireContext());
        dQueue.getCache().clear();

        userLocalStore = new UserLocalStore(requireContext());
        final CurrentUser user = userLocalStore.getLoggedInUser();

        String durl = resources.getString(R.string.api) + "dashboard/" + user.getMemberid();

        @SuppressLint("DefaultLocale") final JsonObjectRequest drequest = new JsonObjectRequest(durl, null,
                response -> {
                    loadingDialog.dismiss();
                    try {
                        JSONObject memobj = new JSONObject(response.getString("member"));
                        Log.d("MEMOBJ", String.valueOf(memobj));
                        winMoney = memobj.getString("wallet_balance");
                        joinMoney = memobj.getString("join_money");
                        if (TextUtils.equals(winMoney, "null")) {
                            winMoney = "0";
                        }
                        if (TextUtils.equals(joinMoney, "null")) {
                            joinMoney = "0";
                        }


                        String win = "";
                        if (isDouble(winMoney)) {
                            win = String.format("%.2f", Double.parseDouble(winMoney));
                        } else {
                            win = String.valueOf(Integer.parseInt(winMoney));
                        }


                        String joinm = "";
                        if (isDouble(joinMoney)) {
                            joinm = String.format("%.2f", Double.parseDouble(joinMoney));
                        } else {
                            joinm = String.valueOf(Integer.parseInt(joinMoney));
                        }

                        String totalMoney = "";
                        if (win.startsWith("0")) {
                            if (isDouble(joinm)) {
                                totalMoney = String.valueOf(Double.parseDouble(joinm));
                            } else {
                                totalMoney = String.valueOf(Integer.parseInt(joinm));
                            }
                        } else if (joinm.startsWith("-")) {
                            if (isDouble(win)) {
                                totalMoney = String.valueOf(Double.parseDouble(win));
                            } else {
                                totalMoney = String.valueOf(Integer.parseInt(win));
                            }
                        } else {
                            if (isDouble(win) && isDouble(joinm)) {
                                totalMoney = String.valueOf(Double.parseDouble(win) + Double.parseDouble(joinm));
                            } else {
                                totalMoney = String.valueOf(Integer.parseInt(win) + Integer.parseInt(joinm));
                            }
                        }
                        if (isDouble(totalMoney)) {
                            totalMoney = String.format("%.2f", Double.parseDouble(totalMoney));
                        }

                        username.setText(memobj.getString("user_name"));

                        balance.setText(totalMoney);
                        topBal.setText(totalMoney);

                        SpannableStringBuilder builder = new SpannableStringBuilder();
                        builder.append(Html.fromHtml(win));
                        winMoneyTv.setText(builder);

                        builder = new SpannableStringBuilder();
                        builder.append(Html.fromHtml(joinm));
                        joinMoneyTv.setText(builder);

                        JSONObject totwinobj = new JSONObject(response.getString("tot_win"));
                        if (TextUtils.equals(totwinobj.getString("total_win"), "null")) {
                            earnings.setText("0");
                        } else {
                            if (isDouble(totwinobj.getString("total_win"))) {
                                earnings.setText(String.format("%.2f", Double.parseDouble(totwinobj.getString("total_win"))));
                            } else {
                                earnings.setText(totwinobj.getString("total_win"));
                            }
                        }

                        JSONObject totwithobj = new JSONObject(response.getString("tot_withdraw"));

                        if (TextUtils.equals(totwithobj.getString("tot_withdraw"), "null")) {
                            payouts.setText("0");
                        } else {
                            if (isDouble(totwithobj.getString("tot_withdraw"))) {
                                payouts.setText(String.format("%.2f", Double.parseDouble(totwithobj.getString("tot_withdraw"))));
                            } else {
                                payouts.setText(totwithobj.getString("tot_withdraw"));
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("**VolleyError", "error" + error.getMessage())) {
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
        drequest.setShouldCache(false);
        dQueue.add(drequest);
    }

    boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public FancyShowCaseView addView(View view, String title, String n) {
        FancyShowCaseView view1 = new FancyShowCaseView.Builder(requireActivity())
                .focusOn(view)
                .title(title)
                //.titleGravity(Gravity.BOTTOM)
                .titleSize(20, 1)
                .focusShape(FocusShape.CIRCLE)
                .enableAutoTextPosition()
                .roundRectRadius(10)
                .focusBorderSize(1)
                .showOnce(n)
                .focusBorderColor(getResources().getColor(R.color.newblue))
                .dismissListener(new DismissListener() {
                    @Override
                    public void onDismiss(String s) {

                    }

                    @Override
                    public void onSkipped(String s) {

                    }
                })
                .build();

        return view1;
    }
}