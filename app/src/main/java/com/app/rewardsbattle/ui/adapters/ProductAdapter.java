//for show product in list
package com.app.rewardsbattle.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.ProductData;
import com.app.rewardsbattle.ui.activities.SingleProductActivity;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.MyViewHolder> {
    private Context mContext;
    private List<ProductData> mData;

    Context context;
    Resources resources;

    public ProductAdapter(Context mContext, List<ProductData> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    
    @Override
    public ProductAdapter.MyViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.product_layout, parent, false);
        return new ProductAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder( ProductAdapter.MyViewHolder holder, int position) {

        context = LocaleHelper.setLocale(mContext);
        resources = context.getResources();

        ProductData data = mData.get(position);
        if (!data.getpImage().isEmpty()) {
            Picasso.get().load(Uri.parse(data.getpImage())).placeholder(R.drawable.battlemanialogo).fit().into(holder.imageView);
        } else {

            holder.imageView.setImageDrawable(mContext.getDrawable(R.drawable.battlemanialogo));
        }

        holder.name.setText(data.getpName());
        holder.aprice.setText(data.getPaPrice());
        holder.aprice.setPaintFlags(  holder.aprice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.sprice.setText(data.getPsPrice());

        holder.cv.setOnClickListener(view -> {
            Intent intent=new Intent(mContext, SingleProductActivity.class);
            intent.putExtra("pid",data.getpId());
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
        ImageView imageView;
        TextView name;
        TextView aprice;
        TextView sprice;

        public MyViewHolder( View itemView) {
            super(itemView);

            cv= itemView.findViewById(R.id.pcv);
            imageView= itemView.findViewById(R.id.pimage);
            name= itemView.findViewById(R.id.pname);
            aprice= itemView.findViewById(R.id.paprice);
            sprice= itemView.findViewById(R.id.psprice);
        }
    }
}
