package com.mongoose.app.moodio.utils;

import com.mongoose.app.moodio.model.Songs;
import com.mongoose.app.moodio.model.PlayList;

import java.util.List;

/**
 * Created by Vips on 11-02-2017.
 */

public class AdManager {

    private NetworkHandler nh;

    public AdManager(NetworkHandler nh) {
        this.nh = nh;
    }

    public List<Songs> addAd(List<Songs> list) { // Add ads to Songs list after every  7 inerval
        int position = 2;
        if(nh.isOnline()) {
            if(list != null)
                while (list.size() > position) {
                    Songs ad = new Songs();
                    ad.setViewType(2);
                    list.add(position, ad);
                    position = position + 7;
                }
        }
        return list;
    }

    public List<PlayList> addPlaylistAd(List<PlayList> list) { // Add ads to Play list after every  4 inerval
        int position = 4;
        if(nh.isOnline()) {
            if(list != null)
                while (list.size() > position) {
                    PlayList ad = new PlayList();
                    ad.setViewType(2);
                    list.add(position, ad);
                    position = position + 5;
                }
        }
        return list;
    }
}
