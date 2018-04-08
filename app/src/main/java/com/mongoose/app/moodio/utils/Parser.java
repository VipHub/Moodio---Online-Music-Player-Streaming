package com.mongoose.app.moodio.utils;

import android.os.AsyncTask;

import com.mongoose.app.moodio.model.Songs;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vips on 01-03-2017.
 */

public class Parser extends AsyncTask<String, Void, List<Songs>> {
    private List<Songs> songsList = new ArrayList<>();
    private Document document;

    public List<Songs> doInBackground(String... params){

        String html = params[0];
        String playOrResult= params[1];

        if(playOrResult.equals("results"))
            songsList = getResults(html);
        else
            songsList = getPlaylist(html);

        return songsList;
    }
    public List<Songs> getPlaylist(String html) {
        Songs song;
        int ii =0;
        String chan[];
        document = Jsoup.parse(html);
        Elements elements = document.getElementsByTag("tr");
        Elements elements1 = document.getElementsByClass("g-hovercard");
        chan = new String[elements1.size()];
        for(Element i : elements1) {
            chan[ii++] = i.ownText();
            //System.out.println(chan);
        }
        ii = 0;
        for(Element i : elements) {
            song = new Songs();
            String id = i.attr("data-video-id");
            String title = i.attr("data-title");
            //System.out.println(id);
            if(!(title.equals("[Deleted video]") | title.equals("[Private video]"))) {
                song.setPath(id);
                song.setTitle(title);
                //song.setArtist(chan[ii++]);
                song.setSourceType(2);
                song.setViewType(1);
                song.setAlbumArtUri("https://i.ytimg.com/vi/" + id + "/");
                songsList.add(song);
            }
        }
        return songsList;
    }
    public List<Songs> getResults(String html) {
        Songs song;
        String chan[];
        int ii = 0;
        document = Jsoup.parse(html);
        Elements abc = document.getElementsByClass("yt-uix-tile-link");
        Elements elements1 = document.getElementsByClass("g-hovercard");
        chan = new String[elements1.size()];
        for(Element i : elements1) {
            chan[ii++] = i.ownText();
            //System.out.println(chan);
        }
        ii = 0;
        for(Element i : abc) {
            song = new Songs();
                String id = i.attr("href").replace("/watch?v=", "");
                String title = i.attr("title");
                song.setPath(id);
                song.setTitle(title);
                //song.setArtist(chan[ii++]);
                song.setSourceType(2);
                song.setViewType(1);
                song.setAlbumArtUri("https://i.ytimg.com/vi/" + id + "/");
                songsList.add(song);
            }
        return songsList;
    }
}
