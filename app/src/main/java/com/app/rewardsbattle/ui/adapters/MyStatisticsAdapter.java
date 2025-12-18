//For show data in my statics
package com.app.rewardsbattle.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.MyStatisticsData;
import com.app.rewardsbattle.utils.LocaleHelper;

import java.util.List;

public class MyStatisticsAdapter extends RecyclerView.Adapter<MyStatisticsAdapter.MyViewHolder> {

    private Context mContext;
    private List<MyStatisticsData> mData;

    Context context;
    Resources resources;

    public MyStatisticsAdapter(Context mContext, List<MyStatisticsData> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    
    @Override
    public MyStatisticsAdapter.MyViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.mystatisticsdata, parent, false);
        return new MyStatisticsAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder( MyStatisticsAdapter.MyViewHolder holder, int position) {

        context = LocaleHelper.setLocale(mContext);
        resources = context.getResources();

        int count = mData.size() - position;
        holder.noMyStatistics.setText(String.valueOf(count));
        MyStatisticsData data = mData.get(position);
        holder.titleMyStatistics.setText(data.getMatchname() + " - "+resources.getString(R.string.match)+" #" + data.getMid());
        holder.timeMystatistics.setText(data.getMatchtime());
        holder.paid.setText(data.getPaid());
        holder.won.setText(data.getWon());
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
        TextView noMyStatistics;
        TextView titleMyStatistics;
        TextView timeMystatistics;
        TextView paid;
        TextView won;

        public MyViewHolder( View itemView) {
            super(itemView);
            noMyStatistics = itemView.findViewById(R.id.no_mystatistics);
            titleMyStatistics = itemView.findViewById(R.id.title_mystatistics);
            timeMystatistics = itemView.findViewById(R.id.time_mystatistics);
            paid = itemView.findViewById(R.id.paid);
            won = itemView.findViewById(R.id.won);
        }
    }
}
