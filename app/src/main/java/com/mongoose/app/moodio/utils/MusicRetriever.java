package com.mongoose.app.moodio.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.mongoose.app.moodio.model.Songs;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vips on 12/09/16.
 */
public class MusicRetriever extends AsyncTask<String, Void, List<Songs>> {

    private Context context;
    private List<Songs> songsList = new ArrayList<>();
    public static String PAGE_TOKEN;

    public MusicRetriever(Context context) {
        this.context = context;
    }

    public List<Songs> doInBackground(String... params) {
        String temp = params[0];
        String strTemp = "",videoid = "",title = "",ch = "";
        Songs songs = new Songs();
        try {
            //URL url = new URL("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=10&order=Relevance&type=video&videoCategoryId=10&q="+temp.replaceAll(" ","+")+"&key=AIzaSyBOOaqXsszkWbAKhy4XlmeXYXc6PKyLiWc");
            BufferedReader br = new BufferedReader(new StringReader(temp));
            while (null != (strTemp = br.readLine())) {
                if (strTemp.contains("videoId")|| strTemp.contains("title")||strTemp.contains("channelTitle") || strTemp.contains("nextPageToken")) {
                    if(strTemp.contains("nextPageToken")) {
                        PAGE_TOKEN = strTemp.replace("nextPageToken", "").replace("\"", "").replace(":", "").replaceAll("\\s", "").replace(",","");
                    } if (strTemp.contains("videoId")) {
                        videoid = strTemp.replace("videoId", "").replace("\"", "").replace(":", "").replaceAll("\\s", "");
                        songs.setPath(videoid);
                        System.out.print(videoid);
                    } if (strTemp.contains("title")) {
                        title = strTemp.replace("title", "").replaceAll("\"", "").replace(":", "").replaceAll(",","")
                                .replaceAll("^\\s+", "").replaceAll("\\\\", "");
                        songs.setTitle(title);
                    } if (strTemp.contains("channelTitle")) {
                        ch = strTemp.replace("channelTitle", "").replace("\"", "").replace(":", "").replace(",", "").replaceAll("^\\s+", "");
                        songs.setArtist(ch);
                        songs.setSourceType(1);
                        songs.setViewType(1);
                        songs.setAlbumArtUri("https://i.ytimg.com/vi/"+videoid+"/");
                        songsList.add(songs);
                        songs = new Songs();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // return songs list array
        return songsList;
    }
}

