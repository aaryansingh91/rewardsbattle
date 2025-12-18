package com.app.rewardsbattle.ui.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.models.LudoLivematchData;
import com.app.rewardsbattle.utils.LoadingDialog;
import com.app.rewardsbattle.utils.UserLocalStore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class LudoResultAdapter extends RecyclerView.Adapter<LudoResultAdapter.MyViewHolder> {

    private Context mContext;
    private List<LudoLivematchData> mData;

    CurrentUser user;
    UserLocalStore userLocalStore;
    LoadingDialog loadingDialog;

    public LudoResultAdapter(Context mContext, List<LudoLivematchData> mData) {
        this.mContext = mContext;
        this.mData = mData;
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

        holder.lockvisible.setVisibility(TextUtils.equals(data.getWithPassword(), "1") ? View.VISIBLE : View.GONE);


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
            holder.acceptedNameTv.setText("");
        } else {
            holder.acceptedNameTv.setText(data.getAcceptedMemberName());
        }

        if (TextUtils.equals(data.getChallengeStatus(), "1")) {//active

            if (TextUtils.equals(data.getRoomCode(), "")) {

                holder.acceptTv.setText("Cancel");
            } else {
                holder.acceptTv.setVisibility(View.GONE);
            }


        } else if (TextUtils.equals(data.getChallengeStatus(), "2")) {//canceled
            holder.acceptTv.setText("Canceled");

        } else if (TextUtils.equals(data.getChallengeStatus(), "3")) {//completed

            holder.acceptTv.setText("Completed");

        } else if (TextUtils.equals(data.getChallengeStatus(), "4")) {//pending

            holder.acceptTv.setText("Wait for result");

        }

        holder.acceptTv.setEnabled(false);

        if (!TextUtils.equals(data.getWinnerId(), "0")) {
            if (TextUtils.equals(data.getWinnerId(), data.getMemberId())) {
                holder.winnerCreator.setVisibility(View.VISIBLE);
            } else {
                holder.winnerAccept.setVisibility(View.VISIBLE);
            }
        }


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
        TextView winnerCreator;
        TextView winnerAccept;
        CardView ludolivematchcardview;
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
            winnerCreator = itemView.findViewById(R.id.winner_creator);
            winnerAccept = itemView.findViewById(R.id.winner_accept);
            ludolivematchcardview = itemView.findViewById(R.id.ludolivematchcardview1);
            lockvisible = itemView.findViewById(R.id.lockvisible);
        }
    }
}

