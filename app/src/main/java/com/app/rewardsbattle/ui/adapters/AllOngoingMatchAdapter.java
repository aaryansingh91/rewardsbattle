//For show data in ongoing tab
package com.app.rewardsbattle.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.AllOngoingMatchData;
import com.app.rewardsbattle.ui.activities.SelectedTournamentActivity;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AllOngoingMatchAdapter extends RecyclerView.Adapter<AllOngoingMatchAdapter.MyViewHolder> {

    private Context mContext;
    private List<AllOngoingMatchData> mData;

    Context context;
    Resources resources;
    String resultType;


    public AllOngoingMatchAdapter(Context mContext, List<AllOngoingMatchData> mData,String result) {
        this.mContext = mContext;
        this.mData = mData;
        this.resultType = result;
    }


    @Override
    public AllOngoingMatchAdapter.MyViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.allongoingmatchdata, parent, false);
        return new AllOngoingMatchAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder( AllOngoingMatchAdapter.MyViewHolder holder, int position) {

        context = LocaleHelper.setLocale(mContext);
        resources = context.getResources();

        final AllOngoingMatchData data = mData.get(position);
        if (!data.getMatchbanner().matches("")) {
            Picasso.get().load(Uri.parse(data.getMatchbanner())).placeholder(R.drawable.default_battlemania).fit().into(holder.ongoingImageView);
        } else {
            holder.ongoingImageView.setVisibility(View.GONE);
        }
        SharedPreferences sp = mContext.getSharedPreferences("currencyinfo", Context.MODE_PRIVATE);
        SpannableStringBuilder builder = new SpannableStringBuilder();

        holder.ongoingMatchTitle.setText(data.getMatchname() + " - "+resources.getString(R.string.match)+" #" + data.getmId());

        String newdate = data.getMatchtime().replace(data.getMatchtime().substring(11, 18), "<br><b>" + data.getMatchtime().substring(11, 18));
        holder.ongoingTime.setText(Html.fromHtml(newdate));
        holder.ongoingTime.setClickable(true);
        holder.ongoingTime.setMovementMethod(LinkMovementMethod.getInstance());


        if(resultType.equals("1")){
            builder.append(Html.fromHtml(resources.getString(R.string.PRIZE_POOL)+"<br>"))
                    .append("₹")
                    .append(Html.fromHtml("<b>"+data.getWinprize()));

        }else {
            builder.append(Html.fromHtml(resources.getString(R.string.PRIZE_POOL)+"<br>"))
                    .append(Html.fromHtml("<b>"+data.getWinprize()))
                    .append("(%)");
        }
        holder.ongoingPrizeWin.setText(builder);

        builder = new SpannableStringBuilder();
        if(resultType.equals("1")){
            builder.append(Html.fromHtml(resources.getString(R.string.PER_KILL)+"<br>"))
                    .append("₹")
                    .append(Html.fromHtml("<b>"+data.getPerkill()));

        }else {
            builder.append(Html.fromHtml(resources.getString(R.string.PER_KILL)+"<br>"))
                    .append(Html.fromHtml("<b>"+data.getPerkill()))
                    .append("(%)");
        }

        holder.ongoingPerKill.setText(builder);


        builder = new SpannableStringBuilder();
        builder.append(Html.fromHtml(""))
                .append(" ", new ImageSpan(mContext, R.drawable.resize_coin1618,ImageSpan.ALIGN_BASELINE), 0)
                .append(" ")
                .append(Html.fromHtml("<b>"+data.getEntryfee()));
        holder.ongoingEntryFee.setText(Html.fromHtml("<b>"+data.getEntryfee()));
        holder.ongoingEntryFee.setClickable(true);
        holder.ongoingEntryFee.setMovementMethod(LinkMovementMethod.getInstance());


        holder.ongoingType.setText(Html.fromHtml("<b>" + data.getType() + "</b>"));
        holder.ongoingType.setClickable(true);
        holder.ongoingType.setMovementMethod(LinkMovementMethod.getInstance());


        holder.ongoingVersion.setText(Html.fromHtml("<b>" + data.getVersion() + "</b>"));
        holder.ongoingVersion.setClickable(true);
        holder.ongoingVersion.setMovementMethod(LinkMovementMethod.getInstance());

        holder.ongoingMap.setText(Html.fromHtml("<b>" + data.getMap() + "</b>"));
        holder.ongoingMap.setClickable(true);
        holder.ongoingMap.setMovementMethod(LinkMovementMethod.getInstance());

        holder.roomIdPassll.setVisibility(View.GONE);
        if (TextUtils.equals(data.getRoomDescription(), "") || !TextUtils.equals(data.getJoinstatus(), "true")) {
            holder.roomIdPassll.setVisibility(View.GONE);
        } else {
            holder.roomIdPassll.setVisibility(View.VISIBLE);
            holder.ongoingLl.setBackgroundColor(mContext.getResources().getColor(R.color.newgreen));
            holder.ongoingEntryFee.setBackgroundColor(mContext.getResources().getColor(R.color.newgreen));
            holder.ongoingSpactateTv.setBackgroundColor(mContext.getResources().getColor(R.color.newgreen));
        }
        holder.ongoingTournamentCardView.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, SelectedTournamentActivity.class);
            intent.putExtra("FROM", "LIVE");
            intent.putExtra("M_ID", data.getmId());
            intent.putExtra("BANER", data.getMatchbanner());
            mContext.startActivity(intent);
        });

        holder.spectate.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.getMatchurl()));
            mContext.startActivity(intent);
        });
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

        ImageView ongoingImageView;
        TextView ongoingMatchTitle;
        TextView ongoingTime;
        TextView ongoingPrizeWin;
        TextView ongoingPerKill;
        TextView ongoingEntryFee;
        TextView ongoingType;
        TextView ongoingVersion;
        TextView ongoingMap;
        CardView ongoingTournamentCardView;
        CardView spectate;
        LinearLayout ongoingLl;
        TextView ongoingSpactateTv;
        LinearLayout roomIdPassll;

        public MyViewHolder( View itemView) {
            super(itemView);

            ongoingImageView = itemView.findViewById(R.id.ongoingimageview);
            ongoingMatchTitle = itemView.findViewById(R.id.ongoingmatchtitle);
            ongoingTime = itemView.findViewById(R.id.ongoingtime);
            ongoingPrizeWin = itemView.findViewById(R.id.ongoingprizewin);
            ongoingPerKill = itemView.findViewById(R.id.ongoingperkill);
            ongoingEntryFee = itemView.findViewById(R.id.ongoingentryfee);
            ongoingType = itemView.findViewById(R.id.ongoingtype);
            ongoingVersion = itemView.findViewById(R.id.ongoingversion);
            ongoingMap = itemView.findViewById(R.id.ongoingtvmap);
            ongoingTournamentCardView = itemView.findViewById(R.id.ongoingtournamentcardview);
            spectate = itemView.findViewById(R.id.spectate);
            ongoingLl = itemView.findViewById(R.id.ongoingll);
            ongoingSpactateTv = itemView.findViewById(R.id.ongoingspactatetv);
            roomIdPassll = itemView.findViewById(R.id.roomidpassllinlive);
        }
    }
}
