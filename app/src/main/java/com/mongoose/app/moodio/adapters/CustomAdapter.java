package com.mongoose.app.moodio.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mongoose.app.moodio.comp.MyTextView;
import com.mongoose.app.moodio.R;

import java.util.Collections;
import java.util.List;

/**
 * Created by vips on 7/7/15.
 */
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    private Context context;
    private List<String> langList = Collections.emptyList();

    public CustomAdapter(Context context, List<String> langList) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.langList = langList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_row, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String lang = langList.get(position);

        holder.iv_cardImage.setText(lang);
        //Picasso.with(context).load(lang).fit().into(holder.iv_cardImage);
        //holder.tv_title.setText(songs.getTitle());
        //holder.tv_lyrics.setText(songs.getArtist());
    }

    @Override
    public int getItemCount() {
        if(langList != null)
            return langList.size();
        return 0;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private MyTextView iv_cardImage;
        //private TextView tv_title, tv_lyrics;

        public MyViewHolder(View itemView) {
            super(itemView);
            iv_cardImage = (MyTextView) itemView.findViewById(R.id.iv_cardImage);
            //tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            //tv_lyrics = (TextView) itemView.findViewById(R.id.tv_lyrics);
        }
    }
}
