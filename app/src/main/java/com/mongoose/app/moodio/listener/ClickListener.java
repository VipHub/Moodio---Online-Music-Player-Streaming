package com.mongoose.app.moodio.listener;

import android.view.View;

/**
 * Created by vips on 7/19/15.
 */
public interface ClickListener {
    void onClick(View view, int position);

    void onLongClick(View view, int position);
}
