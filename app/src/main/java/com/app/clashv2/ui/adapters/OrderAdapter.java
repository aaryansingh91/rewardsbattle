//for show order in list
package com.app.clashv2.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.clashv2.R;
import com.app.clashv2.models.OrderData;
import com.app.clashv2.ui.activities.SingleOrderActivity;
import com.app.clashv2.utils.LocaleHelper;
import com.squareup.picasso.Picasso;

import java.util.List;

public  class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.MyViewHolder> {
    private Context mContext;
    private List<OrderData> mData;

    Context context;
    Resources resources;

    public OrderAdapter(Context mContext, List<OrderData> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    
    @Override
    public OrderAdapter.MyViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.order_layout, parent, false);
        return new OrderAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder( OrderAdapter.MyViewHolder holder, int position) {

        context = LocaleHelper.setLocale(mContext);
        resources = context.getResources();

        OrderData data = mData.get(position);
        if (!data.getpImage().isEmpty()) {
            Picasso.get().load(Uri.parse(data.getpImage())).placeholder(R.drawable.battlemanialogo).fit().into(holder.iv);
        } else {
            holder.iv.setImageDrawable(mContext.getDrawable(R.drawable.battlemanialogo));
        }

        holder.ordername.setText(data.getOrderNo());
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(Html.fromHtml(resources.getString(R.string.price__)+" "))
                .append(" ", new ImageSpan(mContext, R.drawable.resize_coin,ImageSpan.ALIGN_BASELINE), 0)
                .append(" ")
                .append(Html.fromHtml(data.getpPrice()));
        holder.price.setText(builder);

        holder.status.setText(resources.getString(R.string.status__)+" "+data.getOrderStatus().substring(0, 1).toUpperCase() + data.getOrderStatus().substring(1).toLowerCase());

        holder.date.setText((resources.getString(R.string.date__)+" "+data.getCreatedDate()));

        holder.cv.setOnClickListener(view -> {

            Intent intent=new Intent(mContext, SingleOrderActivity.class);
            intent.putExtra("ordername",data.getOrderNo());
            intent.putExtra("pname",data.getpName());
            intent.putExtra("price",data.getpPrice());
            intent.putExtra("image",data.getpImage());
            intent.putExtra("uname",data.getUserName());
            intent.putExtra("add",data.getAddress());
            intent.putExtra("status",data.getOrderStatus().substring(0, 1).toUpperCase() + data.getOrderStatus().substring(1).toLowerCase());
            intent.putExtra("tracklink",data.getCourierLink());
            intent.putExtra("additional",data.getAdditional());
            intent.putExtra("date",data.getCreatedDate());
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

        CardView cv;
        ImageView iv;
        TextView ordername;
        TextView price;
        TextView status;
        TextView date;

        public MyViewHolder( View itemView) {
            super(itemView);

            cv= itemView.findViewById(R.id.ordercv);
            iv= itemView.findViewById(R.id.pimage);
            ordername= itemView.findViewById(R.id.ordername);
            price= itemView.findViewById(R.id.price);
            status= itemView.findViewById(R.id.status);
            date= itemView.findViewById(R.id.date);

        }
    }

}
