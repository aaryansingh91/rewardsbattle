//for show data in ongoing fragment
package com.app.rewardsbattle.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rewardsbattle.R;
import com.app.rewardsbattle.models.CurrentUser;
import com.app.rewardsbattle.models.LotteryData;
import com.app.rewardsbattle.ui.activities.SelectedLotteryActivity;
import com.app.rewardsbattle.utils.LoadingDialog;
import com.app.rewardsbattle.utils.LocaleHelper;
import com.app.rewardsbattle.utils.UserLocalStore;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LotteryAdapter extends RecyclerView.Adapter<LotteryAdapter.MyViewHolder> {

    UserLocalStore userLocalStore;
    LoadingDialog loadingDialog;
    RequestQueue mQueue;
    private final Context mContext;
    private final List<LotteryData> mData;

    Context context;
    Resources resources;

    public LotteryAdapter(Context mContext, List<LotteryData> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }
    
    @NonNull
    @Override
    public LotteryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.lottery_layout, parent, false);
        return new LotteryAdapter.MyViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final LotteryAdapter.MyViewHolder holder, final int position) {

        context = LocaleHelper.setLocale(mContext);
        resources = context.getResources();

        userLocalStore=new UserLocalStore(mContext);
        final CurrentUser user=userLocalStore.getLoggedInUser();
        loadingDialog=new LoadingDialog(mContext);

        SpannableStringBuilder builder = new SpannableStringBuilder();

        final LotteryData data = mData.get(position);

        if(!TextUtils.equals(data.getlImage(),"")){
            Picasso.get().load(Uri.parse(data.getlImage())).placeholder(R.drawable.lucky_draw).fit().into(holder.limageview);
        }

        /*builder.append(Html.fromHtml(""))
                .append(" ", new ImageSpan(mContext, R.drawable.resize_coin,ImageSpan.ALIGN_BASELINE), 0)
                .append("  ")
                .append(Html.fromHtml(""+data.getlPrize()));*/
        holder.winprize.setText(data.getlPrize());

        holder.title.setText(data.getlTitla()+" - "+resources.getString(R.string.lottery)+" #"+data.getlId());
        holder.time.setText(data.getlTime());
        holder.remainingtotal.setText(data.getlTotalJoined()+"/"+data.getlSize());
        holder.progressBar.setMax(Integer.parseInt(data.getlSize()));
        holder.progressBar.setProgress(Integer.parseInt(data.getlTotalJoined()));

       /* builder = new SpannableStringBuilder();
        builder.append(Html.fromHtml(""))
                .append(" ", new ImageSpan(mContext, R.drawable.resize_coin,ImageSpan.ALIGN_BASELINE), 0)
                .append(" ")
                .append(Html.fromHtml(""+data.getLfee()));*/
        holder.entryfee.setText(data.getLfee());

        if(Integer.parseInt(data.getlTotalJoined())>=Integer.parseInt(data.getlSize())){
            holder.joinstatus.setText(resources.getString(R.string.full));
            holder.joincardview.setCardBackgroundColor(mContext.getResources().getColor(R.color.newdisablegreen));
            holder.joincardview.setEnabled(false);
        }
        if(TextUtils.equals(data.getLjoinStatus(),"true")){
            holder.joinstatus.setText(resources.getString(R.string.registered));
            holder.joincardview.setCardBackgroundColor(mContext.getResources().getColor(R.color.newdisablegreen));
        }

        holder.joincardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (data.getLjoinStatus().matches("true")) {
                    Toast.makeText(mContext, "You are already registered.", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadingDialog.show();
                mQueue = Volley.newRequestQueue(mContext);

                String url = resources.getString(R.string.api) + "lottery_join";

                HashMap<String, String> params = new HashMap<String, String>();
                params.put("submit", "joinnow");
                params.put("lottery_id", data.getlId());
                params.put("member_id", user.getMemberid());

                final JsonObjectRequest request = new JsonObjectRequest(url, new JSONObject(params),
                        response -> {
                            loadingDialog.dismiss();

                            try {
                                Toast.makeText(mContext, response.getString("message"), Toast.LENGTH_SHORT).show();
                                if(TextUtils.equals(response.getString("status"),"true")){
                                    holder.joinstatus.setText(resources.getString(R.string.registered));
                                    holder.joincardview.setCardBackgroundColor(mContext.getResources().getColor(R.color.newdisablegreen));
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }, error -> Log.e("**VolleyError", "error" + error.getMessage())) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        return super.getParams();
                    }
                    @Override
                    public Map<String, String> getHeaders() {

                        Map<String, String> headers = new HashMap<>();
                        CurrentUser user = userLocalStore.getLoggedInUser();

                        String credentials = user.getUsername() + ":" + user.getPassword();
                        String auth = "Basic "
                                + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                        String token="Bearer "+user.getToken();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", token);
                        headers.put("x-localization", LocaleHelper.getPersist(context));
                        return headers;
                    }
                };
                mQueue.add(request);
            }
        });

        holder.loterycardview.setOnClickListener(view -> {
            Intent intent=new Intent(mContext, SelectedLotteryActivity.class);
            intent.putExtra("FROM","ONGOING");
            intent.putExtra("LID",data.getlId());
            intent.putExtra("TITLE",data.getlTitla());
            intent.putExtra("BANER",data.getlImage());
            intent.putExtra("TIME",data.getlTime());
            intent.putExtra("ENTRYFEE",data.getLfee());
            intent.putExtra("PRIZE",data.getlPrize());
            intent.putExtra("WONBY",data.getlWonBy());
            intent.putExtra("ABOUT",data.getlRule());
            intent.putExtra("JOINMEMBER",data.getlJoinedMember());
            intent.putExtra("STATUS",holder.joinstatus.getText().toString().trim());
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

        CardView loterycardview;
        ImageView limageview;
        TextView winprize;
        TextView title;
        TextView time;
        TextView remainingtotal;
        ProgressBar progressBar;
        TextView entryfee;
        TextView joinstatus;
        CardView joincardview;

        public MyViewHolder( View itemView) {
            super(itemView);

            loterycardview= itemView.findViewById(R.id.lotterycardview);
            limageview= itemView.findViewById(R.id.limageview);
            winprize = itemView.findViewById(R.id.lwinprize);
            title = itemView.findViewById(R.id.ltitle);
            time = itemView.findViewById(R.id.ltime);
            remainingtotal = itemView.findViewById(R.id.lremainingtotal);
            progressBar= itemView.findViewById(R.id.lprogressbar);
            entryfee= itemView.findViewById(R.id.ltventryfee);
            joinstatus= itemView.findViewById(R.id.ljoin);
            joincardview= itemView.findViewById(R.id.ljoincardview);
        }
    }
}
