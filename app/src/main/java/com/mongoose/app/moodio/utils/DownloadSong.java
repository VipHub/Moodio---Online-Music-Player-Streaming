package com.mongoose.app.moodio.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.mongoose.app.moodio.WebService;
import com.mongoose.app.moodio.model.Songs;
import com.mongoose.app.moodio.model.UrlBean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Vips on 21-09-2016.
 */

public class DownloadSong {
    private DownloadManager dm;
    private DownloadManager.Request request;
    private Songs songs;
    private FragmentActivity activity;

    public DownloadSong() {

    }

    public boolean add(Songs songs, FragmentActivity activity) {
        dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        request = null;
        this.songs = songs;
        this.activity = activity;
        //requestUrlFromServer(songs.getPath());

        return true;
    }

  /*  private void requestUrlFromServer(final String path) {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Conf.YTINMP3)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WebService api = retrofit.create(WebService.class);
        Call<UrlBean> call = api.getUrlFromYtInMp3("https://www.youtube.com/watch?v="+path);
        call.enqueue(new Callback<UrlBean>() {
            @Override
            public void onResponse(Call<UrlBean> call, Response<UrlBean> response) {
                request = new DownloadManager.Request(
                        Uri.parse(response.body().getLink()));

                request.setDestinationInExternalPublicDir("/Moodio", songs.getTitle() + ".mp3"); //
                dm.enqueue(request);
            }
            @Override
            public void onFailure(Call<UrlBean> call, Throwable t) {
                Toast.makeText(activity, "Failed to fetch Download URL", Toast.LENGTH_LONG).show();
                //requestUrlFromServer(path);
            }
        });
    }*/
}