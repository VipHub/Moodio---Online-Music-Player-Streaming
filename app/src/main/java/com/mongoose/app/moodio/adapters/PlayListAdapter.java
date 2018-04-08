package com.mongoose.app.moodio.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.mongoose.app.moodio.MainActivity;
import com.mongoose.app.moodio.comp.MyTextView;
import com.mongoose.app.moodio.R;
import com.mongoose.app.moodio.model.PlayList;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

/**
 * Created by vips on 7/27/2015.
 */
public class PlayListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<PlayList> playLists = Collections.emptyList();
    private LayoutInflater inflater;
    private int layout_id;
    private TouchListener touchListener;
    private int Addid = 2;

    public PlayListAdapter(Context context, int layout_id, List<PlayList> playLists){
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.playLists = playLists;
        this.layout_id = layout_id;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //View view = inflater.inflate(layout_id, parent, false);
        //MyViewHolder holder = new MyViewHolder(view);
        //return holder;
        RecyclerView.ViewHolder holder = null;
        switch(viewType){
            case 1:{
                View view = inflater.inflate(layout_id, parent, false);
                holder = new MyViewHolder(view);
                break;
            }
            case 2:{
                View view = inflater.inflate(R.layout.ad_playlist_row, parent, false);
                holder = new ViewHolderAdMob(view);
                break;
            }
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holderp, int position) {
        PlayList playList = playLists.get(position);
        switch (holderp.getItemViewType()) {
            case 1: {
                MyViewHolder holder = (MyViewHolder) holderp;
                holder.tv_title.setText(playList.getTitle());
                //System.out.println(playList.getDescription());
                if (!playList.getDescription().isEmpty())
                    Picasso.with(context).load(playList.getDescription()).fit().into(holder.iv_cardImage);
                break;
            }
            case 2: {
                break;
            }
        }
    }

    public void setTouchListener(TouchListener touchListener){
        this.touchListener = touchListener;
    }

    @Override
    public int getItemCount() {
        if(playLists!=null)
            return playLists.size();
        return 0;
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener{

        private ImageView iv_cardImage;
        private MyTextView tv_title;
        private ImageButton btn_overflow;

        public MyViewHolder(View itemView) {
            super(itemView);
            iv_cardImage = (ImageView) itemView.findViewById(R.id.iv_cardImage);
            tv_title = (MyTextView) itemView.findViewById(R.id.tv_title);
            btn_overflow = (ImageButton) itemView.findViewById(R.id.btn_close);
            btn_overflow.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            switch(motionEvent.getAction()){
                case MotionEvent.ACTION_UP:
                    //Toast.makeText(context, "from adapter", Toast.LENGTH_LONG).show();
                    touchListener.itemTouched(view, getAdapterPosition());
                    return true;
            }
            return false;
        }
    }
   private class ViewHolderAdMob extends RecyclerView.ViewHolder {
        //private NativeExpressAdView mAdView;
       private AdView mAdView;
        public ViewHolderAdMob(View view) {
            super(view);
            final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            //String adUnit[] = { "",context.getString(R.string.banner_ad_unit_id),context.getString(R.string.banner_ad_unit_id_2)};
            String adUnit[] = { "","ca-app-pub-7649988513709896/6076939964","ca-app-pub-7649988513709896/4690271561"};

            ViewGroup insertPoint = (ViewGroup) view.findViewById(R.id.ad_playlist_container);
            int n = Resources.getSystem().getDisplayMetrics().widthPixels;
            n = (int)convertpxtodp(n,context);
            n = n - 25;
            n = n/2;
            Log.d("playADsize", String.valueOf(n));
            //mAdView = new NativeExpressAdView(context);
            mAdView = new AdView(context);
            Addid = (Addid % 2 == 0) ? 1 : 2;
            Log.d("playAddid  :",""+Addid);

            mAdView.setAdUnitId(adUnit[Addid]);
            mAdView.setAdSize(new AdSize(n,150));
            insertPoint.addView(mAdView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            //RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mAdView.getLayoutParams();
            //lp.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
            //mAdView.setLayoutParams(lp);
            /*AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(context.getResources().getString(R.string.device_id))
                    .build();*/
            mAdView.loadAd(MainActivity.adLoaderr.adRequest);
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }
    public static float convertpxtodp(int px,Context con) { // Convert view pixel to dp value
        DisplayMetrics displayMetrics = con.getResources().getDisplayMetrics();
        float dp = px /((float)displayMetrics.densityDpi/DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public interface TouchListener{
        void itemTouched(View v, int position);
    }
    @Override
    public int getItemViewType(int position) {
        return playLists.get(position).getViewType();
    }
}
