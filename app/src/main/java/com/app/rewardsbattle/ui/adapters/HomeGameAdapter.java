package com.app.rewardsbattle.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.HomeGameData;
import com.app.rewardsbattle.ui.activities.SubGameActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class HomeGameAdapter extends RecyclerView.Adapter<HomeGameAdapter.MyViewHolder> {

    private final Context mContext;
    private final List<HomeGameData> mData;

    public HomeGameAdapter(Context mContext, List<HomeGameData> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public HomeGameAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.newhomegamedata, parent, false);
        return new HomeGameAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(HomeGameAdapter.MyViewHolder holder, int position) {

        final HomeGameData data = mData.get(position);

        SharedPreferences sptab = mContext.getSharedPreferences("tabitem", Context.MODE_PRIVATE);
        String tab = sptab.getString("TAB", "0");

//        Picasso.get().load(Uri.parse(data.getGameImage())).placeholder(R.drawable.loading).fit().into(holder.iv);

        Picasso.get()
                .load(Uri.parse(data.getGameImage()))
                .placeholder(R.drawable.loading)
                .centerCrop()
                .fit()
                .into(holder.iv);

        holder.cv.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, SubGameActivity.class);
            SharedPreferences sp = mContext.getSharedPreferences("homegameinfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("gametitle", data.getGameName());
            editor.putString("gameid", data.getGameId());
            editor.apply();
            pagenumber("0");
            mContext.startActivity(intent);

        });

    }

    void pagenumber(String page) {
        SharedPreferences sp = mContext.getSharedPreferences("tabitem", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor1111 = sp.edit();
        editor1111.putString("TAB", page);
        editor1111.apply();
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

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView iv, comingSoon;
        LinearLayout cv;
        LinearLayout opacity;

        public MyViewHolder(View itemView) {
            super(itemView);

            iv = itemView.findViewById(R.id.gameivnew);
            cv = itemView.findViewById(R.id.gamecardviewnew);
            comingSoon = itemView.findViewById(R.id.gameivnew1);
            opacity = itemView.findViewById(R.id.opacity);
        }
    }
}
