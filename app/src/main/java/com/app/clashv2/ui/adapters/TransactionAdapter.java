//For showing list of transaction
package com.app.clashv2.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.clashv2.R;
import com.app.clashv2.models.TransactionDetails;
import com.app.clashv2.utils.LocaleHelper;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.MyViewHolder> {
    private final Context mContext;
    private final List<TransactionDetails> mData;

    Context context;
    Resources resources;

    public TransactionAdapter(Context mContext, List<TransactionDetails> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.transaction_data, parent, false);
        return new MyViewHolder(v);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        context = LocaleHelper.setLocale(mContext);
        resources = context.getResources();

        TransactionDetails transactionDetails = mData.get(position);
        SharedPreferences sp = mContext.getSharedPreferences("currencyinfo", Context.MODE_PRIVATE);

        if (transactionDetails.getNoteid().matches("1") || transactionDetails.getNoteid().matches("2") || transactionDetails.getNoteid().matches("8") || transactionDetails.getNoteid().matches("10") || transactionDetails.getNoteid().matches("12") || transactionDetails.getNoteid().matches("14") || transactionDetails.getNoteid().matches("15") || transactionDetails.getNoteid().matches("18")) {
            holder.crOrDb.setText(resources.getString(R.string.debit));
            holder.crOrDb.setTextColor(mContext.getResources().getColor(R.color.newred));
            holder.status.setVisibility(View.GONE);

            holder.statuschange.setText("-");
            holder.amount.setText(Html.fromHtml("" + (isDouble(transactionDetails.getWithdraw()) ? String.format("%.2f", Double.parseDouble(transactionDetails.getWithdraw())) : transactionDetails.getWithdraw())));

            holder.statuschange.setTextColor(mContext.getResources().getColor(R.color.newred));
            holder.amount.setTextColor(mContext.getResources().getColor(R.color.newred));
        } else if (transactionDetails.getNoteid().matches("0") || transactionDetails.getNoteid().matches("3") || transactionDetails.getNoteid().matches("4") || transactionDetails.getNoteid().matches("5") || transactionDetails.getNoteid().matches("6") || transactionDetails.getNoteid().matches("7") || transactionDetails.getNoteid().matches("11") || transactionDetails.getNoteid().matches("13") || transactionDetails.getNoteid().matches("16") || transactionDetails.getNoteid().matches("17")) {
            holder.crOrDb.setText(resources.getString(R.string.credit));
            holder.crOrDb.setTextColor(mContext.getResources().getColor(R.color.orange));
            holder.status.setVisibility(View.GONE);

            holder.statuschange.setText("+");
            holder.amount.setText(Html.fromHtml("" + (isDouble(transactionDetails.getDeposit()) ? String.format("%.2f", Double.parseDouble(transactionDetails.getDeposit())) : transactionDetails.getDeposit())));

            holder.statuschange.setTextColor(mContext.getResources().getColor(R.color.orange));
            holder.amount.setTextColor(mContext.getResources().getColor(R.color.orange));
        } else if (transactionDetails.getNoteid().matches("9")) {
            holder.status.setVisibility(View.VISIBLE);
            holder.crOrDb.setText(resources.getString(R.string.debit));
            holder.statuschange.setText("-");
            holder.status.setText(resources.getString(R.string.pending));
            holder.amount.setText(Html.fromHtml("" + (isDouble(transactionDetails.getWithdraw()) ? String.format("%.2f", Double.parseDouble(transactionDetails.getWithdraw())) : transactionDetails.getWithdraw())));
        } else if (transactionDetails.getNoteid().matches("19") || transactionDetails.getNoteid().matches("20")) {
            holder.status.setVisibility(View.VISIBLE);
            holder.crOrDb.setText(resources.getString(R.string.credit));
            holder.status.setText(transactionDetails.getNoteid().matches("20") ? resources.getString(R.string.failed) : resources.getString(R.string.pending));
            holder.statuschange.setText("+");
            holder.amount.setText(Html.fromHtml("" + (isDouble(transactionDetails.getDeposit()) ? String.format("%.2f", Double.parseDouble(transactionDetails.getDeposit())) : transactionDetails.getDeposit())));
        } else if (transactionDetails.getNoteid().matches("21")) {
            holder.status.setVisibility(View.GONE);
            holder.crOrDb.setText(resources.getString(R.string.debit));
            holder.statuschange.setText("-");
            holder.amount.setText(Html.fromHtml("" + (isDouble(transactionDetails.getWithdraw()) ? String.format("%.2f", Double.parseDouble(transactionDetails.getWithdraw())) : transactionDetails.getWithdraw())));
        }

        if (transactionDetails.getMatchid().matches("0")) {
            holder.detail.setText(transactionDetails.getNote() + " - #" + transactionDetails.getTransactionid());
        } else {
            holder.detail.setText(transactionDetails.getNote() + " - #" + transactionDetails.getMatchid());
        }

        if (transactionDetails.getWinmoney().startsWith("0")) {
            if (isDouble(transactionDetails.getJoinmoney())) {
                holder.available.setText(String.valueOf(Double.parseDouble(transactionDetails.getJoinmoney())));
            } else {
                holder.available.setText(String.valueOf(Integer.parseInt(transactionDetails.getJoinmoney())));
            }
        } else if (transactionDetails.getJoinmoney().startsWith("-")) {
            if (isDouble(transactionDetails.getWinmoney())) {
                holder.available.setText(String.valueOf(Double.parseDouble(transactionDetails.getWinmoney())));
            } else {
                holder.available.setText(String.valueOf(Integer.parseInt(transactionDetails.getWinmoney())));
            }
        } else {
            holder.available.setText(Html.fromHtml("" + (isDouble(String.valueOf(Double.parseDouble(transactionDetails.getWinmoney()) + Double.parseDouble(transactionDetails.getJoinmoney()))) ? String.format("%.2f", Double.parseDouble(String.valueOf(Double.parseDouble(transactionDetails.getWinmoney()) + Double.parseDouble(transactionDetails.getJoinmoney())))) : String.valueOf(Double.parseDouble(transactionDetails.getWinmoney()) + Double.parseDouble(transactionDetails.getJoinmoney())))));
        }

        if (transactionDetails.getNoteid().matches("19") || transactionDetails.getNoteid().matches("20")) {
            holder.mainbalanace.setVisibility(View.GONE);
        }
        holder.time.setText(transactionDetails.getDate());
    }

    boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
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

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView crOrDb;
        TextView detail;
        TextView time;
        TextView status;
        TextView amount;
        TextView available;
        TextView mobile;
        TextView statuschange;
        LinearLayout mainbalanace;

        public MyViewHolder(View itemView) {
            super(itemView);

            crOrDb = itemView.findViewById(R.id.crordb_t);
            detail = itemView.findViewById(R.id.detail_t);
            time = itemView.findViewById(R.id.time_t);
            status = itemView.findViewById(R.id.status_t);
            amount = itemView.findViewById(R.id.amount_t);
            available = itemView.findViewById(R.id.available_t);
            mobile = itemView.findViewById(R.id.mobile_t);
            statuschange = itemView.findViewById(R.id.statuschange);
            mainbalanace = itemView.findViewById(R.id.mainbalanace);
        }
    }
}
