package com.mongoose.app.moodio.listener;

import com.mongoose.app.moodio.model.Songs;

import java.util.List;

/**
 * Created by vips on 7/22/2015.
 */
public interface SongClickListener {
    //void onSongsPrepared(List<Songs> songsList);
    void onSongClick(int position, List<Songs> songsList);
}
