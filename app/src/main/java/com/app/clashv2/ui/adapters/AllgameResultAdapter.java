//For show data in completed tab
package com.app.clashv2.ui.adapters;

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

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.clashv2.R;
import com.app.clashv2.models.AllGameResultData;
import com.app.clashv2.ui.activities.SelectedResultActivity;
import com.app.clashv2.utils.LocaleHelper;
import com.squareup.picasso.Picasso;
import java.util.List;
public class AllgameResultAdapter extends RecyclerView.Adapter<AllgameResultAdapter.MyViewHolder> {

    private final Context mContext;
    private final List<AllGameResultData> mData;
    Context context;
    Resources resources;

    public AllgameResultAdapter(Context mContext, List<AllGameResultData> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }


    @NonNull
    @Override
    public AllgameResultAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.allgameresultdata, parent, false);
        return new AllgameResultAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AllgameResultAdapter.MyViewHolder holder, int position) {

        context = LocaleHelper.setLocale(mContext);
        resources = context.getResources();

        SharedPreferences sp = mContext.getSharedPreferences("currencyinfo", Context.MODE_PRIVATE);
        SpannableStringBuilder builder = new SpannableStringBuilder();

        final AllGameResultData data = mData.get(position);
        if (!data.getMatchbanner().isEmpty()) {
            Picasso.get().load(data.getMatchbanner()).placeholder(R.drawable.default_battlemania).fit().into(holder.imageView);
        } else {
            holder.imageView.setVisibility(View.GONE);
        }

        holder.allGametvMatchTitle.setText(data.getMatchname() + " - " + resources.getString(R.string.match) + " #" + data.getMid());
        String newdate = data.getMatchtime().replace(data.getMatchtime().substring(11, 18), "<br><b>" + data.getMatchtime().substring(11, 18));
        holder.allGametvTime.setText(Html.fromHtml(newdate));
        holder.allGametvTime.setClickable(true);
        holder.allGametvTime.setMovementMethod(LinkMovementMethod.getInstance());

        builder.append(Html.fromHtml(resources.getString(R.string.PRIZE_POOL) + "<br>"))
                .append(Html.fromHtml("<b>" + data.getWinprize())).append("(%)");
        holder.allGametvPrizeWin.setText(builder);

        builder = new SpannableStringBuilder();
        builder.append(Html.fromHtml(resources.getString(R.string.PER_KILL) + "<br>"))
                .append(Html.fromHtml("<b>" + data.getPerkill()))
                .append("(%)");
        holder.allGametvPerKill.setText(builder);

        builder = new SpannableStringBuilder();
        builder.append(Html.fromHtml(""))
                .append(" ", new ImageSpan(mContext, R.drawable.resize_coin1618, ImageSpan.ALIGN_BASELINE), 0)
                .append(" ")
                .append(Html.fromHtml("<b>" + data.getEntryfee()));
        holder.allGametvEntryFee.setText(Html.fromHtml("<b>" + data.getEntryfee()));
        holder.allGametvEntryFee.setClickable(true);
        holder.allGametvEntryFee.setMovementMethod(LinkMovementMethod.getInstance());

        holder.allGametvType.setText(Html.fromHtml("<b>" + data.getType() + "</b>"));
        holder.allGametvType.setClickable(true);
        holder.allGametvType.setMovementMethod(LinkMovementMethod.getInstance());

        holder.allGametvVersion.setText(Html.fromHtml("<b>" + data.getVersion() + "</b>"));
        holder.allGametvVersion.setClickable(true);
        holder.allGametvVersion.setMovementMethod(LinkMovementMethod.getInstance());

        holder.allGametvMap.setText(Html.fromHtml("<b>" + data.getMap() + "</b>"));
        holder.allGametvMap.setClickable(true);
        holder.allGametvMap.setMovementMethod(LinkMovementMethod.getInstance());

        holder.allGameResultCardview.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, SelectedResultActivity.class);
            intent.putExtra("M_ID", data.getMid());
            intent.putExtra("BANER", data.getMatchbanner());
            mContext.startActivity(intent);
        });
        holder.watchMatch.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.getMatchurl()));
            mContext.startActivity(intent);
        });
        if (TextUtils.equals(data.getJoinstatus(), "false")) {
            holder.allGameResultStatus.setText(resources.getString(R.string.NOT_JOINED));
        } else {
            holder.allGameResultStatus.setText(resources.getString(R.string.JOINED));
            holder.joinStatusll.setBackgroundColor(mContext.getResources().getColor(R.color.newgreen));
            holder.allGametvEntryFee.setBackgroundColor(mContext.getResources().getColor(R.color.newgreen));
            holder.allGameResultStatus.setBackgroundColor(mContext.getResources().getColor(R.color.newgreen));
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
        ImageView imageView;
        TextView allGametvMatchTitle;
        TextView allGametvTime;
        TextView allGametvPrizeWin;
        TextView allGametvPerKill;
        TextView allGametvEntryFee;
        TextView allGametvType;
        TextView allGametvVersion;
        TextView allGametvMap;
        TextView watchMatch;
        CardView allGameResultCardview;
        TextView allGameResultStatus;
        LinearLayout joinStatusll;

        public MyViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageview);
            allGametvMatchTitle = itemView.findViewById(R.id.allgametvmatchtitle);
            allGametvTime = itemView.findViewById(R.id.allgametvtime);
            allGametvPrizeWin = itemView.findViewById(R.id.allgametvprizewin);
            allGametvPerKill = itemView.findViewById(R.id.allgametvperkill);
            allGametvEntryFee = itemView.findViewById(R.id.allgametventryfee);
            allGametvType = itemView.findViewById(R.id.allgametvtype);
            allGametvVersion = itemView.findViewById(R.id.allgametvversion);
            allGametvMap = itemView.findViewById(R.id.allgametvmap);
            watchMatch = itemView.findViewById(R.id.watchmatch);
            allGameResultCardview = itemView.findViewById(R.id.allgameresultcardview);
            allGameResultStatus = itemView.findViewById(R.id.allgameresultstatus);
            joinStatusll = itemView.findViewById(R.id.joinstatusll);
        }
    }
}
