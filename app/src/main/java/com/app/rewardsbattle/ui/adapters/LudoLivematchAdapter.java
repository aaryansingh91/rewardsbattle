package com.app.rewardsbattle.ui.adapters;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.models.LudoLivematchData;
import com.app.rewardsbattle.utils.LoadingDialog;
import com.app.rewardsbattle.utils.UserLocalStore;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LudoLivematchAdapter extends RecyclerView.Adapter<LudoLivematchAdapter.MyViewHolder> {

    private Context mContext;
    private List<LudoLivematchData> mData;
    Activity activity;

    CurrentUser user;
    UserLocalStore userLocalStore;
    LoadingDialog loadingDialog;

    public LudoLivematchAdapter(Context mContext, List<LudoLivematchData> mData, Activity mActivity) {
        this.mContext = mContext;
        this.mData = mData;
        this.activity = mActivity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(mContext).inflate(R.layout.live_challenges_data, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        userLocalStore = new UserLocalStore(mContext);
        user = userLocalStore.getLoggedInUser();

        loadingDialog = new LoadingDialog(mContext);

        final LudoLivematchData data = mData.get(position);

        holder.autoTv.setText(data.getAutoId());

        holder.coinTv.setText(data.getCoin() + " Coins");

        holder.creatorNameTv.setText(data.getFirstName() + " " + data.getLastName());


        if (TextUtils.equals(data.getProfileImage(), "null") || TextUtils.equals(data.getProfileImage(), "")) {

        } else {
            Picasso.get().load(data.getProfileImage()).placeholder(R.drawable.battlemanialogo).fit().into(holder.creatorIv);
        }

        if (TextUtils.equals(data.getAcceptedProfileImage(), "null") || TextUtils.equals(data.getAcceptedProfileImage(), "")) {

        } else {
            Picasso.get().load(data.getAcceptedProfileImage()).placeholder(R.drawable.battlemanialogo).fit().into(holder.acceptedIv);
        }

        holder.winningTv.setText("Winning " + data.getWinningPrice() + " Coins");

        if (TextUtils.equals(data.getAcceptStatus(), "0")) {
            //can join

            holder.acceptedNameTv.setText("Waiting");

            holder.acceptTv.setOnClickListener(v -> openWarningDialog(data.getLudoChallengeId(), data.getCoin(), holder.acceptTv, data.getWithPassword(), data.getChallengePassword(), data.getWithPassword()));

        } else {
            //can not join
            holder.acceptedNameTv.setText(data.getAcceptedMemberName());
            holder.acceptTv.setText("Accepted");
            holder.acceptTv.setEnabled(false);
        }

        holder.lockvisible.setVisibility(TextUtils.equals(data.getWithPassword(), "1") ? View.VISIBLE : View.GONE);

    }


    @Override
    public int getItemCount() {
        return mData.size();
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView autoTv;
        TextView coinTv;
        TextView creatorNameTv;
        TextView acceptedNameTv;
        TextView winningTv;
        TextView acceptTv;
        ImageView creatorIv;
        ImageView acceptedIv;
        ImageView lockvisible;


        public MyViewHolder(View itemView) {
            super(itemView);

            autoTv = itemView.findViewById(R.id.autocodetv);
            coinTv = itemView.findViewById(R.id.cointv);
            creatorNameTv = itemView.findViewById(R.id.creatorname_tv);
            acceptedNameTv = itemView.findViewById(R.id.acceptedname_tv);
            winningTv = itemView.findViewById(R.id.winingcointv);
            acceptTv = itemView.findViewById(R.id.accepttv);//btn
            creatorIv = itemView.findViewById(R.id.creater_iv);
            acceptedIv = itemView.findViewById(R.id.acceptediv);
            lockvisible = itemView.findViewById(R.id.lockvisible);
        }
    }

    public void openWarningDialog(String challengeId, String coin, TextView accept, String passstatus, String challengepassword, String withpass) {

        Dialog builder = new Dialog(mContext);
        builder.setContentView(R.layout.accepted_warning_dialog);

        TextView tv = builder.findViewById(R.id.coin_warning);
        CardView yes = builder.findViewById(R.id.yes_accept_warning);
        CardView no = builder.findViewById(R.id.no_accept_warning);

        tv.setText(coin + " coins will be deducted from your wallet");

        yes.setOnClickListener(v -> {
            builder.dismiss();
            openUserDetailDialog(challengeId, coin, accept, passstatus, challengepassword, withpass);
        });

        no.setOnClickListener(v -> builder.dismiss());


        builder.create();
        builder.show();


    }

    public void openUserDetailDialog(String challengeId, String coin, TextView accep, String passstatus, String challengepassword, String withpass) {

        Dialog builder = new Dialog(mContext);
        builder.setContentView(R.layout.add_player_details_data);

        TextView title = builder.findViewById(R.id.main_title);
        EditText et = builder.findViewById(R.id.newplayername);
        TextInputLayout password_textinputlayoutforaddinfoTextInputLayout = builder.findViewById(R.id.password_textinputlayoutforaddinfo);
        EditText accept_password = builder.findViewById(R.id.accept_password);
        if (TextUtils.equals(passstatus, "0")) {
            password_textinputlayoutforaddinfoTextInputLayout.setVisibility(View.GONE);
        } else {
            password_textinputlayoutforaddinfoTextInputLayout.setVisibility(View.VISIBLE);
        }

        SharedPreferences sp = mContext.getSharedPreferences("LUDO", Context.MODE_PRIVATE);
        SharedPreferences sp1 = mContext.getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
        String gamename = sp1.getString("gametitle", "");

        et.setText(sp.getString("ludoname", ""));
        TextInputLayout textInputLayout = builder.findViewById(R.id.textinputlayoutforaddinfo);

        Button cancel = builder.findViewById(R.id.newcancel);
        Button join = builder.findViewById(R.id.newok);

        title.setText(withpass.contains("0") ? "Join Challenge" : "Private Contest");
        textInputLayout.setHint(gamename + " username");

        join.setOnClickListener(v -> {

            if (TextUtils.equals(et.getText().toString().trim(), "")) {
                Toast.makeText(mContext, "Please enter " + gamename + " username", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.equals(passstatus, "1")) {
                if (challengepassword == null || challengepassword.isEmpty()) {

                } else {
                    if (TextUtils.equals(accept_password.getText().toString().trim(), "") && challengepassword.length() < 6) {
                        Toast.makeText(mContext, "Please enter password", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!(TextUtils.equals(accept_password.getText().toString().trim(), challengepassword))) {
                        Toast.makeText(mContext, "Please enter valid password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }


            builder.dismiss();
            joinChallenge(challengeId, coin, et.getText().toString().trim(), accep, passstatus, accept_password.getText().toString().trim());
        });

        cancel.setOnClickListener(v -> builder.dismiss());


        builder.create();
        builder.show();


    }

    public void joinChallenge(String challengeId, String coin, String ludoname, TextView accept, String status, String challengepassword) {

        loadingDialog.show();

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("ludo_challenge_id", challengeId);
        params.put("accepted_member_id", user.getMemberid());
        params.put("ludo_king_username", ludoname);
        params.put("coin", coin);
        params.put("submit", "acceptChallenge");
        params.put("challenge_password", challengepassword);
        RequestQueue mQueue = Volley.newRequestQueue(mContext);
        mQueue.getCache().clear();

        String url = mContext.getResources().getString(R.string.api) + "accept_challenge";

        final JsonObjectRequest mrequest = new JsonObjectRequest(url, new JSONObject(params),
                response -> {
                    loadingDialog.dismiss();

                    Log.d("accept challenge", response.toString());
                    loadingDialog.dismiss();

                    try {
                        String status1 = response.getString("status");

                        if (TextUtils.equals(status1, "true")) {
                            accept.setText("Accepted");
                            accept.setEnabled(false);
                            openSuccessChallengeDialog();
                        }

                        Toast.makeText(mContext, response.getString("message"), Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        loadingDialog.dismiss();
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
                CurrentUser user = userLocalStore.getLoggedInUser();
                String token = "Bearer " + user.getToken();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                return headers;
            }
        };

        mrequest.setShouldCache(false);
        mQueue.add(mrequest);

    }

    public void openSuccessChallengeDialog() {

        SharedPreferences sp = mContext.getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
        String gamename = sp.getString("gametitle", "");
        // String packege = sp.getString("packege", "");
        Dialog builder = new Dialog(mContext);
        builder.setContentView(R.layout.challenge_accepte_success_dialog);


        CardView ok = builder.findViewById(R.id.ok_casd);
        TextView match_joind_sucessfully_meassge = builder.findViewById(R.id.match_joind_sucessfully_meassge_id);
        match_joind_sucessfully_meassge.setText("1. Go to my contest section and wait for room code\n2. When you get the room code play on " + gamename + " app.\n3. After winning take screen shot of the winning page .");

        ok.setOnClickListener(v -> {
            builder.dismiss();
            refresh();
        });

        builder.create();
        builder.show();
    }

    public void refresh() {

        activity.finish();
        activity.overridePendingTransition(0, 0);
        Intent intent = activity.getIntent();
        intent.putExtra("N", "1");
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

}
