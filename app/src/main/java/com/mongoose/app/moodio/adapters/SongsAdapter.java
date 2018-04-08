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

import com.google.android.gms.ads.AdLoader;
import com.mongoose.app.moodio.MainActivity;
import com.mongoose.app.moodio.comp.MyTextView;
import com.mongoose.app.moodio.model.Songs;
import com.mongoose.app.moodio.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.mongoose.app.moodio.utils.AdLoaderr;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

/**
 * Created by vips on 7/25/2015.
 */
public class SongsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater inflater;
    private Context context;
    private List<Songs> songsList = Collections.emptyList();
    private TouchListener touchListener;
    private int Addid = 2;
    private boolean CacheImage;

    public SongsAdapter(Context context, List<Songs> songList,boolean cacheImage) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.songsList = songList;
        this.CacheImage = cacheImage;
    }
    public void setList(List<Songs> list) {
        this.songsList = list;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;

        switch(viewType){
            case 1:{
                View view = inflater.inflate(R.layout.songs_row, parent, false);
                holder = new MyViewHolder(view);
                break;
            }
            case 2:{
                View view = inflater.inflate(R.layout.ad_row, parent, false);
                holder = new ViewHolderAdMob(view);
                break;
            }
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holderp, int position) {
        Songs songs = songsList.get(position);
        switch(holderp.getItemViewType()){
            case 1:{
                MyViewHolder holder = (MyViewHolder) holderp;
                if(songs.getSourceType()==2)
                    holder.btn_down.setVisibility(View.GONE);
                holder.tv_title.setText(songs.getTitle());
                holder.tv_artist.setText(songs.getArtist());
                if (songs.getAlbumArtUri() != null) {

                        String buildAlbumArtUri = songs.getAlbumArtUri() + "default.jpg";
                        if(CacheImage) {
                            Picasso.with(context)
                                    .load(buildAlbumArtUri)
                                    .fit().into(holder.iv_cardImage);
                        } else {
                            Picasso.with(context)
                                    .load(buildAlbumArtUri)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                    .networkPolicy(NetworkPolicy.NO_CACHE)
                                    .fit().into(holder.iv_cardImage);
                        }
                } else {
                    if(songs.getSourceType()==2){
                        /*MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(songs.getLyrics());
                        byte[] artBytes =  mmr.getEmbeddedPicture();
                        if(artBytes!=null)
                        {
                            //     InputStream is = new ByteArrayInputStream(mmr.getEmbeddedPicture());
                            Bitmap bm = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
                            holder.iv_cardImage.setImageBitmap(bm);
                        }*/
                        holder.iv_cardImage.setImageResource(R.drawable.music_bg);
                    } else
                        holder.iv_cardImage.setImageResource(R.drawable.music_bg);
                }

                break;
            }
            case 2:{
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        if(songsList!=null) {
            return songsList.size();
        }
        return 0;
    }

    public void setTouchListener(TouchListener touchListener){
        this.touchListener = touchListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener{

        private ImageView iv_cardImage;
        private ImageButton btn_overflow,btn_down;
        private MyTextView tv_title, tv_artist;

        public MyViewHolder(View itemView) {
            super(itemView);
            iv_cardImage = (ImageView) itemView.findViewById(R.id.iv_cardImage);
            tv_title = (MyTextView) itemView.findViewById(R.id.tv_title);
            tv_artist = (MyTextView) itemView.findViewById(R.id.tv_artist);
            btn_overflow = (ImageButton) itemView.findViewById(R.id.btn_close);
            btn_overflow.setOnTouchListener(this);
            btn_down = (ImageButton) itemView.findViewById(R.id.btn_play);
            btn_down.setOnTouchListener(this);
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
        //public AdView mAdView;
        //String adUnit[] = { "",context.getString(R.string.native_ad_unit_id_1),context.getString(R.string.native_ad_unit_id_2)};
        String adUnit[] = { "","ca-app-pub-7649988513709896/2971693962","ca-app-pub-7649988513709896/4829872360"};
        private NativeExpressAdView mAdView;
        public ViewHolderAdMob(View view) {
            super(view);
            //mAdView = (AdView) view.findViewById(R.id.adView);
            //RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.ad_parent);
            ViewGroup insertPoint = (ViewGroup) view.findViewById(R.id.ad_parent);
            int n = Resources.getSystem().getDisplayMetrics().widthPixels;
            n = (int)convertpxtodp(n,context);
            n = n - 20;
            Log.d("songADsize", String.valueOf(n));
            mAdView = new NativeExpressAdView(context);

            Addid = (Addid % 2 == 0) ? 1 : 2;
            Log.d("songAddid  :",""+Addid);

            mAdView.setAdUnitId(adUnit[Addid]);
            mAdView.setAdSize(new AdSize(n,80));
            insertPoint.addView(mAdView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            //RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mAdView.getLayoutParams();
            //lp.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
            //mAdView.setLayoutParams(lp);
            /*AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(context.getResources().getString(R.string.device_id))
                    .build();*/
            mAdView.loadAd(MainActivity.adLoaderr.adRequest);
        }
    }
    public static float convertpxtodp(int px,Context con) {
        DisplayMetrics displayMetrics = con.getResources().getDisplayMetrics();
        float dp = px /((float)displayMetrics.densityDpi/DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }
    @Override
    public int getItemViewType(int position) {
        return songsList.get(position).getViewType();
    }
    public interface TouchListener{
        void itemTouched(View v, int position);
    }
}
