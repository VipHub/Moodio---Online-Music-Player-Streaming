package com.mongoose.app.moodio.utils;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Environment;

import com.mongoose.app.moodio.model.Songs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * Created by rohan on 7/19/15.
 */
public class LocalSongsManager {
    // SDCard Path
    private final String MEDIA_PATH = Environment.getExternalStorageDirectory()
            .getPath() + "/Moodio";
    private ArrayList<Songs> songsList = new ArrayList<>();
    private DatabaseHandler db;
    // Constructor
    public LocalSongsManager(Context context){
        db = new DatabaseHandler(context);
    }

    /**
     * Function to read all mp3 files from sdcard
     * and store the details in ArrayList
     * */
    public boolean getPlayList(){
                File home = new File(MEDIA_PATH);
                if (home.listFiles(new FileExtensionFilter()).length > 0) {
                    for (File file : home.listFiles(new FileExtensionFilter())) {
                        Songs songs = new Songs();
                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(file.getPath());
                        songs.setTitle(file.getName().substring(0, (file.getName().length() - 4)));
                        songs.setPath(file.getPath());
                        songs.setSourceType(2);
                        songs.setArtist(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM).replace("youtube.com/watch?v=",""));
                        songs.setAlbumArtUri(null);
                        songs.setViewType(1);
                        songsList.add(songs);
            }
        }

        // return songs list array
        db.addDownSongs(songsList);
        //return songsList;
        return true;
    }
    /**
     * Class to filter files which are having .mp3 extension
     * */
    class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".m4a") || name.endsWith(".mp3"));
        }
    }
}