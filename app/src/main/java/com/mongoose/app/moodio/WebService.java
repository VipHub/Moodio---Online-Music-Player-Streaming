package com.mongoose.app.moodio;


import com.mongoose.app.moodio.model.PlayList;
import com.mongoose.app.moodio.model.Songs;
import com.mongoose.app.moodio.model.UrlBean;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


/**
 * Created by vips on 7/7/15.
 */
public interface WebService {

    @GET("1.json")
    Call<List<Songs>> getSongs();
    @GET("{lang}")
    Call<List<PlayList>> getPlaylist(
            @Path("lang") String lang);
    @GET("{path}")
    Call<List<Songs>> getPlaylistSongs(
            @Path("path") String path);
    @GET("{path}")
    Call<String> getUrlFromServer(
            @Path("path") String path);
    @GET("{lang}")
    Call<ResponseBody> getLangFromServer(
            @Path("lang") String lang);
    @GET("v3/search?part=snippet&maxResults=10&order=Relevance&type=video&videoCategoryId=10&key=AIzaSyBOOaqXsszkWbAKhy4XlmeXYXc6PKyLiWc")
    Call<ResponseBody> getSongsFromServer(
            @Query("q") String query);
    @GET("v3/search?part=snippet&maxResults=10&order=Relevance&type=video&videoCategoryId=10&key=AIzaSyBOOaqXsszkWbAKhy4XlmeXYXc6PKyLiWc")
    Call<ResponseBody> getSongsFromServerNewPage(
            @Query("q") String q,
            @Query("pageToken") String path );
    /*@GET("v3/playlistItems?part=snippet&chart=&key=AIzaSyBOOaqXsszkWbAKhy4XlmeXYXc6PKyLiWc")
    Call<ResponseBody> getSongsFromDirectPlaylist(
            @Query("playlistId") String id
    );*/
    @GET("playlist?")
    Call<ResponseBody> getSongsFromDirectPlaylist(
            @Query("list") String id
    );
    @GET("results?filters=video")
    Call<ResponseBody> getSongsFromDirectResults(
            @Query("q") String id,
            @Query("page") int no
    );
    @GET("api/generate.php?")
    Call<ResponseBody> getUrlFromYtInMp3(
            @Query("id") String id
    );

    @GET("{path}")
    Call<ResponseBody> callApi(
            @Path("path") String path
    );
    @GET("complete/search?client=youtube&ds=yt")
    Call<ResponseBody> getSuggestions(
      @Query("q") String query
    );
}
