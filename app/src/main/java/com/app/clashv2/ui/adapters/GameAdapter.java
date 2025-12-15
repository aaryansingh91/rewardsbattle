package com.app.clashv2.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.clashv2.R;
import com.app.clashv2.models.GameData;
import com.app.clashv2.ui.activities.LudoActivity;
import com.app.clashv2.ui.activities.SelectedGameActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.MyViewHolder> {

    private final Context mContext;
    private final List<GameData> mData;

    public GameAdapter(Context mContext, List<GameData> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.newallgamedata, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        final GameData data = mData.get(position);

        Picasso.get().load(Uri.parse(data.getGameImage())).placeholder(R.drawable.logo_space).into(holder.iv);

        if (data.getComing_soon().equals("1")) {
            holder.opacity.setVisibility(View.VISIBLE);
            holder.comingSoon.setVisibility(View.VISIBLE);
            holder.iv.setVisibility(View.GONE);
            holder.comingSoon.setImageDrawable(mContext.getDrawable(R.drawable.comingsoon));
        }

        holder.gameName.setText(data.getGameName());

        // ✅ Random number between 1–99
        int randomValue = (int) (Math.random() * 99 + 1);
        holder.randomNumber.setText(String.valueOf(randomValue));

        holder.cv.setOnClickListener(v -> {
            if (data.getComing_soon().equals("0")) {
                if (TextUtils.equals(data.getGameType(), "1")) {
                    Intent intent = new Intent(mContext, LudoActivity.class);
                    SharedPreferences sp = mContext.getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("gametitle", data.getGameName());
                    editor.putString("gameid", data.getGameId());
                    editor.putString("packege", data.getPackageName());
                    editor.putString("SHAREPAGE", "FIRST");
                    editor.apply();
                    pagenumber("1");
                    mContext.startActivity(intent);
                } else if (TextUtils.equals(data.getGameType(), "0")) {
                    Intent intent = new Intent(mContext, SelectedGameActivity.class);
                    SharedPreferences sp = mContext.getSharedPreferences("gameinfo", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("gametitle", data.getGameName());
                    editor.putString("gameid", data.getGameId());
                    editor.apply();
                    pagenumber("0");
                    mContext.startActivity(intent);
                }
            }
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
        CardView cv;
        LinearLayout opacity;
        TextView gameName, randomNumber;

        public MyViewHolder(View itemView) {
            super(itemView);

            iv = itemView.findViewById(R.id.gameivnew);
            cv = itemView.findViewById(R.id.gamecardviewnew);
            comingSoon = itemView.findViewById(R.id.gameivnew1);
            opacity = itemView.findViewById(R.id.opacity);
            gameName = itemView.findViewById(R.id.gameName);

            // ✅ new bindings
            randomNumber = itemView.findViewById(R.id.randomNumber);
//            greenDot = itemView.findViewById(R.id.greenDot);
        }
    }
}
