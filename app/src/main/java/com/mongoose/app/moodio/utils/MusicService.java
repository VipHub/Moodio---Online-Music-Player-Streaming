package com.mongoose.app.moodio.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.mongoose.app.moodio.WebService;
import com.mongoose.app.moodio.model.Songs;
import com.mongoose.app.moodio.model.UrlBean;
import com.mongoose.app.moodio.MainActivity;
import com.mongoose.app.moodio.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



/**
 * Created by vips on 7/19/15.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnBufferingUpdateListener {

    public static MediaPlayer player;
    private List<Songs> songsList;
    private int songIndex;
    public static Boolean isShuffle = false;
    public static int isRepeat = 0;
    public static Boolean isMusicSet = false;
    private Boolean isPausedOnCall = false;
    //private boolean PROGRESSBARSTATUS = false;
    private String title, artist;
    private int totalDuration = 0;
    private int buffer;

    private MusicServiceListener musicServiceListener;
    private final IBinder musicBind = new MusicBinder();
    private static NotificationManager mNotificationManager;
    private static NotificationCompat.Builder mBuilder;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    public static boolean forgroundStatus;
    private boolean STATUS;
    private Context context;

    InterstitialAd mInterstitialAd;
    public static ServiceCallbacks serviceCallbacks;
    public Retrofit retrofit;
    public WebService api;
    public Call<ResponseBody> call;
    public Boolean enforcedStop = false;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7649988513709896/9122111565");
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                if (serviceCallbacks != null) {
                    if (!STATUS)
                        serviceCallbacks.backPressed();
                }
                requestNewInterstitial();
            }
        });
        requestNewInterstitial();                   //  Calling Ad

        initMusicPlayer();
    }

    private void initMusicPlayer() {                    // Initialize media player Object
        //set listeners
        player = new MediaPlayer();
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        //player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnBufferingUpdateListener(this);
    }

    public void setMusicServiceListener(MusicServiceListener musicServiceListener) {
        this.musicServiceListener = musicServiceListener;
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }

    public void setList(List<Songs> songsList) {
        this.songsList = songsList;
    }
    public List<Songs> getSongsList() {
        return songsList;
    }
    public int currentPlayingSongIndex() {
        return songIndex;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (player != null) {
                            if (isPausedOnCall) {
                                player.start();
                                isPausedOnCall = false;
                            }
                        }
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        if (player != null && player.isPlaying()) {
                            player.pause();
                            isPausedOnCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (player != null && player.isPlaying()) {
                            player.pause();
                            isPausedOnCall = true;
                        }
                        break;

                }
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);      //Listen the call state
        return START_STICKY;
    }

    private void requestNewInterstitial() {                             // New Interstitial ad
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getResources().getString(R.string.device_id))
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    public void playSong(int songIndex) {
        serviceCallbacks.showProgressBar();
        this.songIndex = songIndex;
        Songs songs = songsList.get(songIndex);
        musicServiceListener.onPlayMusic(songs);
        title = songs.getTitle();
        artist = songs.getArtist();
        //totalDuration = songs.getDuration();
        setNotifications(1);
        isMusicSet = true;
        player.reset();
        String url = null;
        switch (songs.getSourceType()) {
            case 1:
                requestUrlFromServer(songs.getPath());
                break;
            case 2:
                //player.setDataSource(songs.getPath());
                requestUrlFromServer(songs.getPath());
                //player.prepareAsync();
                break;
            default:
                playNext();
                break;
        }
    }

    public void pauseSong() {
        if (player.isPlaying()) {
            player.pause();
            setNotifications(2);
        }
    }

    public void resumeSong() {
        if (!player.isPlaying()) {
            player.start();
            setNotifications(3);
        }
    }

    public void playNext() {
        if (isShuffle) {
            int newSong = songIndex;
            while (newSong == songIndex) {
                newSong = new Random().nextInt(songsList.size());
            }
            songIndex = newSong;
        } else if (isRepeat == 0) {
            songIndex++;
            if (songIndex >= songsList.size()) {
                musicServiceListener.onStopMusic();
                isMusicSet = false;
                player.stop();
                return;
            }
        } else if (isRepeat == 1) {
            songIndex++;
            if (songIndex >= songsList.size()) songIndex = 0;
        }
        playSong(songIndex);
    }

    public void playPrev() {
        songIndex--;
        if (songIndex < 0) songIndex = songsList.size() - 1;
        playSong(songIndex);
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }


    public Songs getSongDetails() {
        return songsList.get(songIndex);
    }

    public int getDuration() {
        if (player != null)
            if (player.isPlaying()) {
                totalDuration = player.getDuration();
                return totalDuration;
            }
        return totalDuration;
    }

    public void seekTo(int time) {
        player.seekTo(time);
    }

    public int getCurrentPosition() {
        if (player != null)
            return player.getCurrentPosition();
        return 0;
    }

    //public void setProgeressBarStatus(boolean status) {
    //  this.PROGRESSBARSTATUS = status;
    //}
    //public boolean getProgressBarStatus() {
    //  return PROGRESSBARSTATUS;
    //}
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        //MainActivity.musicSrv.setNotifications(2);
        STATUS = forgroundStatus;
        if (enforcedStop) {
            enforcedStop = false;
            return;
        } else if (player.getCurrentPosition() > 0) {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer player, int i, int i1) {

        if (i == -38) {
            player.stop();
            player.start();
            enforcedStop = true;
        } else {
            Log.d("MUSIC PLAYER", "Playback Error");
            //Toast.makeText(this, "Playback Error", Toast.LENGTH_LONG).show();
            player.reset();
            playSong(songIndex);
            //serviceCallbacks.hideProgressBar();
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        serviceCallbacks.hideProgressBar();
        mediaPlayer.start();
        musicServiceListener.onPlayMusic(songsList.get(songIndex));
        setNotifications(1);
        serviceCallbacks.notificationStatus(false);
        if (mediaPlayer.isPlaying())
            totalDuration = mediaPlayer.getDuration();
    }

    private void setNotifications(int pause) {

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_row);
        remoteViews.setImageViewResource(R.id.iv_cardImage, R.mipmap.ic_launcher);
        if(pause == 1) {
            remoteViews.setTextViewText(R.id.tv_title, title);
            remoteViews.setTextViewText(R.id.tv_artist, artist);
            //remoteViews.setViewVisibility(R.id.btn_close, View.INVISIBLE);
            remoteViews.setImageViewResource(R.id.btn_close, R.drawable.ic_skip_next_blue_36dp);
        } if(pause == 2) {
            remoteViews.setTextViewText(R.id.tv_title, title);
            remoteViews.setTextViewText(R.id.tv_artist, artist);
            remoteViews.setImageViewResource(R.id.btn_play, R.drawable.ic_play_circle_fill_blue_48dp);
            //remoteViews.setViewVisibility(R.id.btn_close, View.VISIBLE);
            remoteViews.setImageViewResource(R.id.btn_close, android.R.drawable.ic_menu_close_clear_cancel);
        } else {
            remoteViews.setTextViewText(R.id.tv_title, title);
            remoteViews.setTextViewText(R.id.tv_artist, artist);
            remoteViews.setImageViewResource(R.id.btn_play, R.drawable.ic_pause_circle_fill_blue_48dp);
            //remoteViews.setViewVisibility(R.id.btn_close, View.INVISIBLE);
            remoteViews.setImageViewResource(R.id.btn_close, R.drawable.ic_skip_next_blue_36dp);
        }


        mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        //.setContentTitle(title)
                        //.setContentText(artist)
                        .setOngoing(true)
                        .setContent(remoteViews);


        Intent nIntent = new Intent(this, MainActivity.class);
        nIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, nIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent = new Intent("com.asterisk.testproject.ACTION_PLAY");
        PendingIntent pendingplayIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent closeIntent = new Intent("com.asterisk.testproject.ACTION_CLOSE");
        PendingIntent pendingcloseIntent = PendingIntent.getBroadcast(this, 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.btn_play, pendingplayIntent);
        remoteViews.setOnClickPendingIntent(R.id.btn_close, pendingcloseIntent);

        mBuilder.setContentIntent(contentIntent);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mNotificationManager != null)
            mNotificationManager.cancelAll();
        stopForeground(true);
        player.release();
        player = null;
        isMusicSet = false;
    }

    //binder
    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public interface MusicServiceListener {
        void onPlayMusic(Songs songs);

        void onStopMusic();
    }

    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        buffer = percent;
    }

    public int getBuffer() {
        return buffer;
    }
    public Boolean SKIP = false;

    /*
    private void requestUrlFromServer(final String path) {// Generating Url from Server
        if(call!=null)
            call.cancel();
        retrofit = new Retrofit.Builder()
                .baseUrl(Conf.SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(WebService.class);

        call = api.getUrlFromServer(path);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    player.setDataSource(context,Uri.parse(response.body()+""));
                } catch (IOException e) {
                    e.printStackTrace();
                     SKIP = true;
                }
                if(SKIP) {
                    SKIP = false;
                    serviceCallbacks.hideProgressBar();
                    Log.d("Data","Invalid Data Source");
                } else {
                    player.prepareAsync();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }
        });

    } */

    private void requestUrlFromServer(final String path) {// Generating Url from Server
        if(call!=null)
            call.cancel();
        retrofit = new Retrofit.Builder()
                .baseUrl(Conf.YTINMP3)
                //.addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(WebService.class);

        call = api.getUrlFromYtInMp3(path);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                     String data = response.body().string();
                    //System.out.println("haha"+data);

                    if (data != null) {
                        Document document = Jsoup.parse(data);


                        Elements elements = document.getElementsByTag("a");
                        //elements.stream().forEach((e) -> {
                        //  System.out.println(e.attr("onclick"));
                        //});
                        Element ele = elements.get(0);
                        String str = ele.attr("onclick");
                        str = str.substring(13, str.length()-2);
                        System.out.println("hiiiii"+str);



                        Uri uri = Uri.parse(str);



                        player.setDataSource(context, uri);

                        uri = null;
                        call = null;
                    }
                    else
                        requestUrlFromServer(path);
                } catch (IOException e) {
                    e.printStackTrace();
                    SKIP = true;
                }
                if(SKIP) {
                    SKIP = false;
                    serviceCallbacks.hideProgressBar();
                    Log.d("Data","Invalid Data Source");
                } else {
                    player.prepareAsync();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });

    }

    public interface ServiceCallbacks {
        void backPressed();

        void showProgressBar();

        void hideProgressBar();
        void notificationStatus(boolean status);
    }

    public static class AudioPlayerBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(action.equalsIgnoreCase("com.asterisk.testproject.ACTION_PLAY")){
                if(player.isPlaying()){
                    player.pause();
                    MainActivity.musicSrv.setNotifications(2);
                    serviceCallbacks.notificationStatus(true);
                }
                else {
                    player.start();
                    MainActivity.musicSrv.setNotifications(3);
                    serviceCallbacks.notificationStatus(false);
                }
            } if(action.equalsIgnoreCase("com.asterisk.testproject.ACTION_CLOSE")) {
                if(player.isPlaying()) {
                    MainActivity.musicSrv.playNext();
                } else {
                    mNotificationManager.cancel(1);
                    MainActivity.exitapp();
                }
            }
        }
    }
}
