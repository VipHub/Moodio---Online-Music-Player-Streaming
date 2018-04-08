package com.mongoose.app.moodio;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.mongoose.app.moodio.adapters.SongsAdapter;
import com.mongoose.app.moodio.comp.MyTextView;
import com.mongoose.app.moodio.listener.ClickListener;
import com.mongoose.app.moodio.listener.RecyclerViewTouchListener;
import com.mongoose.app.moodio.listener.SongClickListener;
import com.mongoose.app.moodio.model.Songs;
import com.mongoose.app.moodio.utils.AdManager;
import com.mongoose.app.moodio.utils.Conf;
import com.mongoose.app.moodio.utils.MusicService;
import com.mongoose.app.moodio.utils.NetworkHandler;
import com.mongoose.app.moodio.utils.Controllers;
import com.mongoose.app.moodio.utils.DatabaseHandler;
import com.mongoose.app.moodio.utils.DownloadSong;
import com.mongoose.app.moodio.utils.Parser;
import com.mongoose.app.moodio.utils.SongUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class PlayListActivity extends AppCompatActivity implements MusicService.MusicServiceListener,
        SlidingUpPanelLayout.PanelSlideListener,
        SeekBar.OnSeekBarChangeListener,
        SongsAdapter.TouchListener,SwipeRefreshLayout.OnRefreshListener, MusicService.ServiceCallbacks,SongClickListener {

    private SongClickListener songClickListener;
    private String playListId;
    private String PLAYLIST_TYPE;
    private String title;
    private String source;
    private String description;
    private DatabaseHandler db;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    //service
    private MusicService musicSrv;
    private Intent serviceIntent;
    //binding
    private boolean musicBound = false;
    private ImageView backdrop;
    private NetworkHandler nh;

    private MyTextView tv_title, tv_artist, tv_currentTime, tv_totalDuration;
    private ImageButton btn_play, btn_next, btn_prev, btn_repeat, btn_shuffle, btn_like, btn_play_short, btn_overflow;
    private ImageView playerImage, songImage;
    private SlidingUpPanelLayout slidingLayout;
    private SeekBar songProgressbar;
    private RelativeLayout player_layout;

    private SongUtils utils;
    private Handler pHandler = new Handler();
    private List<Songs> songsList;
    private SongsAdapter adapter;
    //private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    public  ProgressBar Loader;
    private AdView adView;
    public static PlaylistCallbacks playlistCallbacks;
    private FloatingActionButton btn_fab;
    public Retrofit retrofit;
    //private AdManager adManager;
    private ImageView img_empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);

        img_empty = (ImageView) findViewById(R.id.img_empty);
        img_empty.setVisibility(View.GONE);

        songClickListener = this;
        Intent intent = getIntent();
        PLAYLIST_TYPE = intent.getStringExtra("PLAYLIST_TYPE");
        playListId = intent.getStringExtra("PLAYLIST_ID");
        title = intent.getStringExtra("PLAYLIST_TITLE");
        description = intent.getStringExtra("PLAYLIST_DESC");
        source = intent.getStringExtra("PLAYLIST_SOURCE");
        db = new DatabaseHandler(this);
        utils = new SongUtils();
        setToolbar();
        initViews();
        nh = new NetworkHandler(PlayListActivity.this);
        //adManager = new AdManager(nh);
        backdrop = (ImageView) findViewById(R.id.backdrop);
        if(!description.isEmpty())
            Picasso.with(this).load(description).fit().into(backdrop);

        if(nh.isOnline()) {
            adView = (AdView) findViewById(R.id.player_adView);
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("6DF8DBFB26D805DEB6024C8FB3E7E227").build();
            adView.loadAd(adRequest);
        }

        Loader = (ProgressBar) findViewById(R.id.progressBarmain);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        //swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        //swipeRefreshLayout.setColorSchemeColors(R.color.color_primary);
        //swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if(PLAYLIST_TYPE.equals("fix")) {
            if(source.equals("direct"))
                getSongsFromPlaylist(playListId);
            else
                requestDataFromServer(playListId);
        } else {
            songsList = db.getPlayListSongs(Integer.parseInt(playListId));
            //songsList = adManager.addAd(songsList);
            updateViews();
        }
        recyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(this, recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (musicBound) {
                    if(songsList.get(position).getViewType()==1) {
                        onSongClick(songsList, position);
                    }
                }
            }
            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        btn_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSongClick(songsList,0);
            }
        });
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicBound) {
                    if (musicSrv.isMusicSet) {
                        if (musicSrv.isPlaying()) {
                            musicSrv.pauseSong();
                            btn_play.setImageResource(R.drawable.ic_play_circle_fill_blue_48dp);
                            btn_play_short.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                        } else {
                            musicSrv.resumeSong();
                            btn_play.setImageResource(R.drawable.ic_pause_circle_fill_blue_48dp);
                            btn_play_short.setImageResource(R.drawable.ic_pause_black_48dp);
                        }
                    } else {
                        //List<Songs> songsList = new MusicRetriever(PlayListActivity.this).prepare();
                        onSongClick(songsList, 0);
                    }
                }
            }
        });

        btn_play_short.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicBound) {
                    if (musicSrv.isMusicSet) {
                        if (musicSrv.isPlaying()) {
                            musicSrv.pauseSong();
                            btn_play_short.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                            btn_play.setImageResource(R.drawable.ic_play_circle_fill_blue_48dp);
                        } else {
                            musicSrv.resumeSong();
                            btn_play_short.setImageResource(R.drawable.ic_pause_black_48dp);
                            btn_play.setImageResource(R.drawable.ic_pause_circle_fill_blue_48dp);
                        }
                    }
                }
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicBound) {
                    if (musicSrv.isMusicSet) {
                        musicSrv.playNext();
                    }
                }
            }
        });

        btn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicBound) {
                    if (musicSrv.isMusicSet) {
                        musicSrv.playPrev();
                    }
                }
            }
        });

        btn_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicBound) {
                    int status = musicSrv.isRepeat;
                    switch (status) {
                        case 0:
                            musicSrv.isRepeat = 1;
                            btn_repeat.setImageResource(R.drawable.ic_repeat_blue_24dp);
                            break;
                        case 1:
                            musicSrv.isRepeat = 2;
                            btn_repeat.setImageResource(R.drawable.ic_repeat_one_blue_24dp);
                            break;
                        case 2:
                            musicSrv.isRepeat = 0;
                            btn_repeat.setImageResource(R.drawable.ic_repeat_black_24dp);
                            break;
                    }
                }
            }
        });

        btn_shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicBound) {
                    if (musicSrv.isShuffle) {
                        musicSrv.isShuffle = false;
                        btn_shuffle.setImageResource(R.drawable.ic_shuffle_black_24dp);
                    } else {
                        musicSrv.isShuffle = true;
                        btn_shuffle.setImageResource(R.drawable.ic_shuffle_blue_24dp);
                    }
                }
            }
        });
        btn_overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog d = new Dialog(PlayListActivity.this);
                d.setContentView(R.layout.dialog_playlist);
                d.setTitle("Playlist");
                d.setCancelable(true);

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                Window window = d.getWindow();
                lp.copyFrom(window.getAttributes()); //This makes the dialog take up the full width
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(lp);

                final FloatingActionButton fab = (FloatingActionButton) d.findViewById(R.id.floatingActionButton);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        d.dismiss();
                    }
                });

                final RecyclerView recyclerView = (RecyclerView) d.findViewById(R.id.recyclerview);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(PlayListActivity.this));

                recyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(PlayListActivity.this, recyclerView, new ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        Log.d("com.vips", "" + position);
                        if(musicSrv.getSongsList().get(position).getViewType()==1) {
                            songClickListener.onSongClick(position, musicSrv.getSongsList());
                            d.dismiss();                        }
                    }
                    @Override
                    public void onLongClick(View view, int position) {

                    }
                }));
                recyclerView.smoothScrollToPosition(musicSrv.currentPlayingSongIndex());

                SongsAdapter adapter = new SongsAdapter(PlayListActivity.this,musicSrv.getSongsList(),false);
                adapter.setTouchListener(new SongsAdapter.TouchListener() {
                    @Override
                    public void itemTouched(View v, int position) {

                    }
                });
                recyclerView.setAdapter(adapter);
                d.show();
            }
        });
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            musicSrv.setMusicServiceListener(PlayListActivity.this);
            if (musicSrv.isMusicSet) {
                setSongDetail(musicSrv.getSongDetails());
                updateProgressBar();
                Controllers.setControllers(musicSrv, btn_play, btn_play_short, btn_shuffle, btn_repeat);
            }else{
                player_layout.setVisibility(View.GONE);
            }
            musicBound = true;
            musicSrv.setCallbacks(PlayListActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        // Start and Bind service to activity

        if (serviceIntent == null) {
            Log.d("legend.ace18", "ololo");
            serviceIntent = new Intent(this, MusicService.class);
            startService(serviceIntent);
            bindService(serviceIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }
        musicSrv.forgroundStatus = true;
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        collapsingToolbarLayout.setTitle(title);
    }

    private void initViews() {
        player_layout = (RelativeLayout) findViewById(R.id.player);
        tv_title = (MyTextView) findViewById(R.id.tv_title);
        tv_artist = (MyTextView) findViewById(R.id.tv_artist);
        tv_currentTime = (MyTextView) findViewById(R.id.tv_currentTime);
        tv_totalDuration = (MyTextView) findViewById(R.id.tv_totalDuration);
        btn_play = (ImageButton) findViewById(R.id.btn_play);
        btn_next = (ImageButton) findViewById(R.id.btn_next);
        btn_prev = (ImageButton) findViewById(R.id.btn_prev);
        btn_repeat = (ImageButton) findViewById(R.id.btn_repeat);
        btn_shuffle = (ImageButton) findViewById(R.id.btn_shuffle);
        btn_play_short = (ImageButton) findViewById(R.id.btn_play_short);
        btn_overflow = (ImageButton) findViewById(R.id.btn_close);
        playerImage = (ImageView) findViewById(R.id.iv_playerImage);
        songImage = (ImageView) findViewById(R.id.iv_cardImage);
        slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingLayout.setPanelSlideListener(this);
        songProgressbar = (SeekBar) findViewById(R.id.songProgressBar);
        songProgressbar.setOnSeekBarChangeListener(this);
        btn_fab = (FloatingActionButton) findViewById(R.id.btn_fab);
    }

    private void onSongClick(List<Songs> songsList, int position) {
        setSongDetail(songsList.get(position));
        musicSrv.setList(songsList);
        musicSrv.playSong(position);
        btn_play.setImageResource(R.drawable.ic_pause_circle_fill_blue_48dp);
        btn_play_short.setImageResource(R.drawable.ic_pause_black_48dp);
        slidingLayout.setPanelState(PanelState.COLLAPSED);
        updateProgressBar();
    }


    private void setSongDetail(Songs songs) {
        tv_title.setText(songs.getTitle());
        tv_artist.setText(songs.getArtist());
        String totalDuration = utils.milliSecondsToTimer(songs.getDuration());
        tv_totalDuration.setText(totalDuration);
        tv_currentTime.setText("0:00");
        if (songs.getAlbumArtUri() != null) {
            Picasso.with(this).load(songs.getAlbumArtUri()+"hqdefault.jpg").into(playerImage);
            Picasso.with(this).load(songs.getAlbumArtUri()+"default.jpg").fit().into(songImage);
        }
    }

    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        songProgressbar.setProgress(0);
        songProgressbar.setMax(100);
        pHandler.postDelayed(pUpdateTimeTask, 200);
    }

    /**
     * Background Runnable thread
     */
    private Runnable pUpdateTimeTask = new Runnable() {
        public void run() {
            int totalDuration = musicSrv.getDuration();
            int currentDuration = musicSrv.getCurrentPosition();
            int bufferDuration = musicSrv.getBuffer();

            //if(musicSrv.getProgressBarStatus()) {
              //  Loader.setVisibility(View.INVISIBLE);
                //if (btn_play_short.getVisibility() == View.VISIBLE) {

///                } else {
    //                if(slidingLayout.getPanelState()==PanelState.COLLAPSED)
      //                  btn_play_short.setVisibility(View.VISIBLE);
        //        }
          //  }
            //else {
              //  Loader.setVisibility(View.VISIBLE);
                //if (btn_play_short.getVisibility() == View.VISIBLE) {
                 //   btn_play_short.setVisibility(View.INVISIBLE);
             //   }
            //}
            tv_currentTime.setText("" + utils.milliSecondsToTimer(currentDuration));
            tv_totalDuration.setText(""+ utils.milliSecondsToTimer(totalDuration));
            // Updating progress bar
            int progress = (int) utils.getProgressPercentage(currentDuration, totalDuration);
            //Log.d("Progress", ""+progress);
            songProgressbar.setProgress(progress);
            songProgressbar.setSecondaryProgress(bufferDuration);
            // Running this thread after 100 milliseconds
            if (musicBound)
                pHandler.postDelayed(this, 200);
        }};

    @Override
    protected void onStop() {
        super.onStop();
        if (musicBound) {
            unbindService(musicConnection);
            musicBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_play_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear_recents) {
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPlayMusic(Songs songs) {
        setSongDetail(songs);
    }

    @Override
    public void onStopMusic() {
        pHandler.removeCallbacks(pUpdateTimeTask);
    }

    @Override
    public void onPanelSlide(View view, float v) {

    }

    @Override
    public void onPanelCollapsed(View view) {
        if(Loader.getVisibility()==View.VISIBLE) {
            //btn_play_short.setVisibility(View.INVISIBLE);
        } else {
            btn_play_short.setVisibility(View.VISIBLE);
        }
        btn_overflow.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPanelExpanded(View view) {
        btn_play_short.setVisibility(View.INVISIBLE);
        btn_overflow.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPanelAnchored(View view) {

    }

    @Override
    public void onPanelHidden(View view) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        int time = utils.progressToTimer(progress, musicSrv.getDuration());
        musicSrv.seekTo(time);
        seekBar.setProgress(progress);
    }

    private void setControllers() {
        if (musicSrv.isPlaying()) {
            btn_play.setImageResource(R.drawable.ic_pause_circle_fill_blue_48dp);
            btn_play_short.setImageResource(R.drawable.ic_pause_black_48dp);
        } else {
            btn_play.setImageResource(R.drawable.ic_play_circle_fill_blue_48dp);
            btn_play_short.setImageResource(R.drawable.ic_play_arrow_black_48dp);
        }
        if (musicSrv.isRepeat == 0) {
            btn_repeat.setImageResource(R.drawable.ic_repeat_black_24dp);
        } else if (musicSrv.isRepeat == 1) {
            btn_repeat.setImageResource(R.drawable.ic_repeat_blue_24dp);
        } else {
            btn_repeat.setImageResource(R.drawable.ic_repeat_one_blue_24dp);
        }
        if (musicSrv.isShuffle) {
            btn_shuffle.setImageResource(R.drawable.ic_shuffle_blue_24dp);
        } else {
            btn_shuffle.setImageResource(R.drawable.ic_shuffle_black_24dp);
        }
    }

    @Override
    public void itemTouched(View v, final int position) {
        final Songs songs = songsList.get(position);
        PopupMenu popup = new PopupMenu(this, v);
        if(PLAYLIST_TYPE.equals("fix")) {
            popup.getMenuInflater().inflate(R.menu.playlist_songs_popup_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.action_play:
                        onSongClick(songsList, position);
                        break;
                    //case R.id.action_download:
                      //  showDownloadPopup(position);
                        //break;
                }
                return true;
            }
        });
        } else {
            popup.getMenuInflater().inflate(R.menu.local_playlist_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    int id = menuItem.getItemId();
                    switch (id) {
                        case R.id.action_download:
                            showDownloadPopup(position);
                            break;
                        case R.id.action_remove_song:
                            db.removePlayListSong(Integer.valueOf(playListId),songsList.get(position).getPath());
                            Toast.makeText(PlayListActivity.this,"Song Removed Succesfully",Toast.LENGTH_LONG).show();
                            break;
                    }
                    return true;
                }
            });
        }
        popup.show();
    }

    private void showDownloadPopup(final int position) {

        final Dialog d = new Dialog(PlayListActivity.this);
        d.setContentView(R.layout.popup_download);
        d.setTitle(songsList.get(position).getTitle());
        d.setCancelable(true);

        final Button btn_down = (Button) d.findViewById(R.id.down);
        MyTextView tv_tit = (MyTextView) d.findViewById(R.id.tv_title);
        tv_tit.setText(songsList.get(position).getTitle());
        AdView mAdView = (AdView) d.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(getResources().getString(R.string.device_id)).build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                btn_down.setVisibility(View.VISIBLE);
                super.onAdLoaded();
            }
        });

        btn_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadSong ds = new DownloadSong();
                if(ds.add(songsList.get(position),PlayListActivity.this)){
                    Toast.makeText(PlayListActivity.this,"Song Added For Downloading",Toast.LENGTH_LONG).show();
                }
                d.dismiss();
            }
        });

        d.show();
    }

    public static void setPlayListCallbacks(PlaylistCallbacks callbacks) {
        playlistCallbacks = callbacks;
    }

    @Override
    public void onRefresh() {
        ///requestDataFromServer(playListId);
    }
    private void updateViews() {
        if(songsList == null)
            img_empty.setVisibility(View.VISIBLE);
        adapter = new SongsAdapter(this, songsList,true);
        adapter.setTouchListener(this);
        recyclerView.setAdapter(adapter);
        //if(songsList != null)
            //btn_fab.setVisibility(View.VISIBLE);
    }
    private void requestDataFromServer(String id) {
        //if (!swipeRefreshLayout.isRefreshing())
            progressBar.setVisibility(View.VISIBLE);

        retrofit = new Retrofit.Builder()
                .baseUrl(Conf.API_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WebService api = retrofit.create(WebService.class);

        Call<ResponseBody> call = api.callApi(id+".html");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //if (swipeRefreshLayout.isRefreshing())
                    //swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                String resultp = null;
                try {
                    resultp = response.body().string();      // Raw html data stored
                    System.out.println(resultp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Document document = Jsoup.parse(resultp);
                Elements apicode = document.getElementsByTag("data");
                String jsoncode = apicode.toString().replace("<data>", "").replace("</data>", "");
                Gson gson = new Gson();
                songsList = gson.fromJson(jsoncode, new TypeToken<List<Songs>>(){}.getType());;
                //songsList = adManager.addAd(songsList);
                updateViews();
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //if (swipeRefreshLayout.isRefreshing())
                    //swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                img_empty.setVisibility(View.VISIBLE);
                Snackbar.make(recyclerView, "No Connection..", Snackbar.LENGTH_LONG)
                        .setAction("Try Again", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                requestDataFromServer(playListId);
                            }
                        }).show();
            }
        });
    }
    public void getSongsFromPlaylist(String id) {  // return songs from playlist
        //if (!swipeRefreshLayout.isRefreshing())
            progressBar.setVisibility(View.VISIBLE);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .build();
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Conf.NEW_YT_SERVER)
                .client(okHttpClient)
                .build();
        WebService api = retrofit.create(WebService.class);

        Call<ResponseBody> call = api.getSongsFromDirectPlaylist(id);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String resultp = null;
                try {
                    resultp = response.body().string();
                    //System.out.println(resultp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //sb.toString();
                try {
                        songsList = new Parser().execute(resultp,"playlist").get();
                        //songsList.remove(0);
                        //songsList = adManager.addAd(songsList);
                        updateViews();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                //swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //if (swipeRefreshLayout.isRefreshing())
                    //swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                img_empty.setVisibility(View.VISIBLE);
                Snackbar.make(recyclerView, "No Connection..", Snackbar.LENGTH_LONG)
                        .setAction("Try Again", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                getSongsFromPlaylist(playListId);
                            }
                        }).show();
            }
        });
    }

    @Override
    public void backPressed() {
        onBackPressed();
    }

    @Override
    public void showProgressBar() {
        Loader.setVisibility(View.VISIBLE);
        playlistCallbacks.showmainProgressBar();
        btn_play_short.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideProgressBar() {
        Loader.setVisibility(View.INVISIBLE);
        playlistCallbacks.hidemainProgressBar();
        if(slidingLayout.getPanelState()==PanelState.COLLAPSED)
            btn_play_short.setVisibility(View.VISIBLE);
    }
    @Override
    public void notificationStatus(boolean status) {
        if (status) {
            btn_play.setImageResource(R.drawable.ic_play_circle_fill_blue_48dp);
            btn_play_short.setImageResource(R.drawable.ic_play_arrow_black_48dp);
        } else {
            btn_play.setImageResource(R.drawable.ic_pause_circle_fill_blue_48dp);
            btn_play_short.setImageResource(R.drawable.ic_pause_black_48dp);
        }
    }

    @Override
    public void onSongClick(int position, List<Songs> songsList) {
        setSongDetail(songsList.get(position));
        musicSrv.setList(songsList);
        musicSrv.playSong(position);
        btn_play.setImageResource(R.drawable.ic_pause_circle_fill_blue_48dp);
        btn_play_short.setImageResource(R.drawable.ic_pause_black_48dp);
        slidingLayout.setPanelState(PanelState.COLLAPSED);
        updateProgressBar();
        btn_play_short.setImageResource(R.drawable.ic_pause_black_48dp);
    }

    public interface PlaylistCallbacks {
        void showmainProgressBar();

        void hidemainProgressBar();

    }
}
