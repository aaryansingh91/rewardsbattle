//For show checkbox checked or unchecked in select position page
package com.app.clashv2.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.clashv2.R;
import com.app.clashv2.models.JoinSingleMatchData;
import com.app.clashv2.utils.LocaleHelper;

import java.util.ArrayList;
import java.util.List;

public class SelectMatchPositionAdapter extends RecyclerView.Adapter<SelectMatchPositionAdapter.MyViewHolder> {

    final public List<String> checkBoxList = new ArrayList<String>();
    private Context mContext;
    private List<JoinSingleMatchData> mData;

    Context context;
    Resources resources;
    int total;
    String type;

    public SelectMatchPositionAdapter(Context mContext, List<JoinSingleMatchData> mData, String type, int size) {
        this.mContext = mContext;
        this.mData = mData;
        this.type = type;
        this.total = size;
    }


    @Override
    public SelectMatchPositionAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.selectmatchposition_data, parent, false);
        return new SelectMatchPositionAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SelectMatchPositionAdapter.MyViewHolder holder, int position) {

        context = LocaleHelper.setLocale(mContext);
        resources = context.getResources();

        final JoinSingleMatchData data = mData.get(position);

        if (!data.getUsername().trim().matches("") && !data.getPubgid().trim().matches("")) {
            holder.checkBox.setChecked(true);
            holder.checkBox.setEnabled(false);
            holder.checkTv.setEnabled(true);
        }

        if ((position + 1) > total) {
            holder.checkBox.setVisibility(View.GONE);
            holder.checkTv.setVisibility(View.GONE);
        }

        holder.checkTv.setText(data.getPosition());
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    checkBoxList.add("{'team': '" + data.getTeam() + "','position':'" + data.getPosition() + "'}");
                } else {
                    checkBoxList.remove("{'team': '" + data.getTeam() + "','position':'" + data.getPosition() + "'}");
                }
            }
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

        CheckBox checkBox;
        TextView checkTv;

        public MyViewHolder(View itemView) {
            super(itemView);

            checkBox = itemView.findViewById(R.id.checkbox);
            checkTv = itemView.findViewById(R.id.checktv);

        }
    }
}
