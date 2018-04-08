package com.mongoose.app.moodio.comp;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.util.Locale;


/**
 * Created by Vips on 4/9/2017.
 */

public class MyTypeface {
    public  Context context;
    public MyTypeface (Context context) {
        this.context = context;
    }

    public Typeface getTypeface() {
        AssetManager am = context.getApplicationContext().getAssets();

        Typeface typeface = Typeface.createFromAsset(am,
                String.format(Locale.US, "fonts/%s", "peace.ttf"));

        return typeface;
    }
}
