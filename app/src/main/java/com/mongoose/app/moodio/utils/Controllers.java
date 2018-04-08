package com.mongoose.app.moodio.utils;

import android.widget.ImageButton;

import com.mongoose.app.moodio.R;

/**
 * Created by vips on 8/6/2015.
 */
public class Controllers {

    public static void setControllers(MusicService musicSrv, ImageButton btn_play, ImageButton btn_play_short, ImageButton btn_shuffle, ImageButton btn_repeat) {
        if (musicSrv.isPlaying()) {
            btn_play.setImageResource(R.drawable.ic_pause_circle_fill_blue_48dp);
            btn_play_short.setImageResource(R.drawable.ic_pause_black_48dp);
        } else {
            btn_play.setImageResource(R.drawable.ic_play_circle_fill_blue_48dp);
            btn_play_short.setImageResource(R.drawable.ic_play_arrow_black_48dp);
        }
        if (musicSrv.isRepeat == 0) {
            btn_repeat.setImageResource(R.drawable.ic_repeat_black_24dp);
        } else if (musicSrv.isRepeat == 1) {
            btn_repeat.setImageResource(R.drawable.ic_repeat_blue_24dp);
        } else {
            btn_repeat.setImageResource(R.drawable.ic_repeat_one_blue_24dp);
        }
        if (musicSrv.isShuffle) {
            btn_shuffle.setImageResource(R.drawable.ic_shuffle_blue_24dp);
        } else {
            btn_shuffle.setImageResource(R.drawable.ic_shuffle_black_24dp);
        }
    }
}
