package com.mongoose.app.moodio.utils;

import android.content.Context;

import com.google.android.gms.ads.AdRequest;
import com.mongoose.app.moodio.R;

/**
 * Created by Vips on 8/24/2017.
 */

public class AdLoaderr {
    public AdRequest adRequest;
    public AdLoaderr(Context context) {
        adRequest = new AdRequest.Builder()
                .addTestDevice(context.getResources().getString(R.string.device_id))
                .build();
    }
}
