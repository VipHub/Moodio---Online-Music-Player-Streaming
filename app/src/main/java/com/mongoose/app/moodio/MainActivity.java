package com.mongoose.app.moodio;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
//import android.widget.TextView;
import android.widget.Toast;

import com.mongoose.app.moodio.adapters.PlayListAdapter;
import com.mongoose.app.moodio.adapters.SongsAdapter;
import com.mongoose.app.moodio.comp.MyTextView;
import com.mongoose.app.moodio.fragments.LibraryFragment;
import com.mongoose.app.moodio.fragments.MyPlaylistFragment;
import com.mongoose.app.moodio.fragments.MySongsFragment;
import com.mongoose.app.moodio.fragments.PlayListFragment;
import com.mongoose.app.moodio.listener.ClickListener;
import com.mongoose.app.moodio.listener.DrawerToggleListener;
import com.mongoose.app.moodio.listener.RecyclerViewTouchListener;
import com.mongoose.app.moodio.listener.SongClickListener;
import com.mongoose.app.moodio.model.PlayList;
import com.mongoose.app.moodio.model.Songs;
import com.mongoose.app.moodio.utils.AdLoaderr;
import com.mongoose.app.moodio.utils.Controllers;
import com.mongoose.app.moodio.utils.DatabaseHandler;
import com.mongoose.app.moodio.utils.DownloadSong;
import com.mongoose.app.moodio.utils.MusicService;
import com.mongoose.app.moodio.utils.NetworkHandler;
import com.mongoose.app.moodio.utils.SongUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        DrawerToggleListener,
        SongClickListener,
        SeekBar.OnSeekBarChangeListener,
        MusicService.MusicServiceListener,
        SlidingUpPanelLayout.PanelSlideListener, MusicService.ServiceCallbacks, PlayListActivity.PlaylistCallbacks {

    public static AdLoaderr adLoaderr;
    private FragmentManager fm;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private SongClickListener songClickListener;
    //service
    public static MusicService musicSrv;
    private Intent serviceIntent;
    //binding
    private boolean musicBound = false;

    private MyTextView tv_title, tv_artist, tv_currentTime, tv_totalDuration;
    private ImageButton btn_play, btn_next, btn_prev, btn_repeat, btn_shuffle, btn_like, btn_play_short, btn_overflow;
    private ImageView playerImage, songImage;
    private SlidingUpPanelLayout slidingLayout;
    private SeekBar songProgressbar;
    private Handler mHandler = new Handler();
    private SongUtils utils;
    private DatabaseHandler db;
    public static ProgressBar Loader;
    public static Dialog d;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AdView adView;
    private NetworkHandler nh;
    private String status;
    public static boolean FirstTime = true;
    public static boolean LocalLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("peace.ttf")
                .setFontAttrId(com.mongoose.app.moodio.R.attr.fontPath)
                .build()
        );

        adLoaderr = new AdLoaderr(MainActivity.this);

        //d = new Dialog(this,android.R.style.Theme_NoTitleBar_Fullscreen);
        //d.setContentView(R.layout.activity_langsel);
        //d.setCancelable(false);
        //d.show();

        setContentView(R.layout.activity_main);
        PlayListActivity.setPlayListCallbacks(MainActivity.this);
        setNavigationView(null);

        songClickListener = this;

        Intent intent = getIntent();
        status = intent.getStringExtra("STATUS");

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-7649988513709896/9122111565");
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        File file = new File(Environment.getExternalStorageDirectory(),"Moodio");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("TravellerLog :: ", "Problem creating folder");
            }
        }

        fm = getSupportFragmentManager();
        Fragment frag;
        if(status.equals("ONLINE")) {
            if (savedInstanceState != null) {
                frag = fm.getFragment(savedInstanceState, "MAINFRAGMENT");
                fm.beginTransaction().replace(R.id.activity_container, frag).addToBackStack("MAINFRAGMENT").commit();
            } else {

                frag = new LibraryFragment();
                fm.beginTransaction().add(R.id.activity_container, frag, "MAINFRAGMENT").addToBackStack("MAINFRAGMENT").commit();
            }
        } else {
            PlayListFragment.updateStatus = false;
            MySongsFragment mysongsFrag = new MySongsFragment();
            fm.beginTransaction().add(R.id.activity_container, mysongsFrag).commit();
            //onNavigationItemSelected((MenuItem) findViewById(R.id.action_downloads));
        }

        utils = new SongUtils();
        db = new DatabaseHandler(this);
        nh = new NetworkHandler(this);

        if(nh.isOnline()) {
            adView = (AdView) findViewById(R.id.player_adView);
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("6DF8DBFB26D805DEB6024C8FB3E7E227").build();
            adView.loadAd(adRequest);
        }

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
        Loader = (ProgressBar) findViewById(R.id.progressBarmain);

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
            public void onClick(View view) {
                final Dialog d = new Dialog(MainActivity.this);
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
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

                recyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(MainActivity.this, recyclerView, new ClickListener() {
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

                SongsAdapter adapter = new SongsAdapter(MainActivity.this,musicSrv.getSongsList(),false);
                adapter.setTouchListener(new SongsAdapter.TouchListener() {
                    @Override
                    public void itemTouched(View v, int position) {

                    }
                });
                recyclerView.setAdapter(adapter);
                d.show();
            }
        });

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }




    private void showDownloadPopup(final Songs songs) {

        final Dialog d = new Dialog(MainActivity.this);
        d.setContentView(R.layout.popup_download);
        d.setTitle(songs.getTitle());
        d.setCancelable(true);

        final Button btn_down = (Button) d.findViewById(R.id.down);
        AdView mAdView = (AdView) d.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("6DF8DBFB26D805DEB6024C8FB3E7E227").build();
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
                if(ds.add(songs,MainActivity.this)){
                    Toast.makeText(MainActivity.this,"Song Added For Downloading",Toast.LENGTH_LONG).show();
                }
                d.dismiss();
            }
        });

        d.show();
    }
    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            musicSrv.setMusicServiceListener(MainActivity.this);
            if (musicSrv.isMusicSet) {
                setSongDetail(musicSrv.getSongDetails());
                slidingLayout.setPanelState(PanelState.COLLAPSED);
                updateProgressBar();
                Controllers.setControllers(musicSrv, btn_play, btn_play_short, btn_shuffle, btn_repeat);
            }
            musicBound = true;
            musicSrv.setCallbacks(MainActivity.this);
            musicSrv.forgroundStatus = true;
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
            serviceIntent = new Intent(this, MusicService.class);
            startService(serviceIntent);
            bindService(serviceIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }
        musicSrv.forgroundStatus = true;
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
    public static void exitapp() {
        System.exit(0);
    }
    private void setSongDetail(Songs songs) {
        tv_title.setText(songs.getTitle());
        tv_artist.setText(songs.getArtist());
        tv_totalDuration.setText("0:00");
        tv_currentTime.setText("0:00");
        if (songs.getAlbumArtUri() != null) {
            Picasso.with(this).load(songs.getAlbumArtUri()+"hqdefault.jpg")
                    .into(playerImage);
            Picasso.with(this).load(songs.getAlbumArtUri()+"default.jpg")
                    .fit().into(songImage);
        } else if(songs.getSourceType()==2) {
                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        try {
                            mmr.setDataSource(songs.getPath());
                        } catch (Exception e) {
                            playerImage.setImageResource(R.drawable.music_bg);
                            songImage.setImageResource(R.drawable.music_bg);
                        }

                        byte[] artBytes =  mmr.getEmbeddedPicture();
                        if(artBytes!=null)
                        {
                            //     InputStream is = new ByteArrayInputStream(mmr.getEmbeddedPicture());
                            Bitmap bm = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
                            playerImage.setImageBitmap(bm);
                            songImage.setImageBitmap(bm);
                        }
            mmr.release();
        } else {
            playerImage.setImageResource(R.drawable.music_bg);
            songImage.setImageResource(R.drawable.music_bg);
        }
    }
    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        songProgressbar.setProgress(0);
        songProgressbar.setMax(100);
        mHandler.postDelayed(mUpdateTimeTask, 200);
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            int totalDuration = musicSrv.getDuration();
            int currentDuration = musicSrv.getCurrentPosition();
            int bufferDuration = musicSrv.getBuffer();

            //if(musicSrv.getProgressBarStatus()) {
              //  Loader.setVisibility(View.INVISIBLE);
                //if (btn_play_short.getVisibility() == View.VISIBLE) {

                //} else {
                 //   if(slidingLayout.getPanelState()==PanelState.COLLAPSED)
                 //       btn_play_short.setVisibility(View.VISIBLE);
                //}
           // }
            //else {
              //  Loader.setVisibility(View.VISIBLE);
                //if (btn_play_short.getVisibility() == View.VISIBLE) {
                 //   btn_play_short.setVisibility(View.INVISIBLE);
                //}
            //}


                tv_currentTime.setText("" + utils.milliSecondsToTimer(currentDuration));
                tv_totalDuration.setText(""+ utils.milliSecondsToTimer(totalDuration));
                // Updating progress bar
                int progress = (int) utils.getProgressPercentage(currentDuration, totalDuration);
                //Log.d("Progress", ""+totalDuration);
                songProgressbar.setProgress(progress);
                songProgressbar.setSecondaryProgress(bufferDuration);
                // Running this thread after 200 milliseconds
                if (musicBound)
                    mHandler.postDelayed(this, 200);
        }};


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings_option:
                Intent intent = new Intent(this, SettingsActivity.class);
                overridePendingTransition(R.anim.fadein,R.anim.fadeout);
                startActivity(intent);
                break;
            case R.id.action_search:

                return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setNavigationView(Toolbar toolbar) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView.setNavigationItemSelectedListener(this);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        //menuItem.setChecked(true);
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.action_library:
                PlayListFragment.updateStatus = true;
                //LibraryFragment libFrag = new LibraryFragment();
                Fragment libFrag = fm.findFragmentByTag("MAINFRAGMENT");
                if(libFrag != null)
                    fm.beginTransaction().replace(R.id.activity_container, libFrag,"MAINFRAGMENT").commit();
                else
                    fm.beginTransaction().replace(R.id.activity_container, new LibraryFragment(),"MAINFRAGMENT").commit();
                break;
            case R.id.action_myplaylist:
                PlayListFragment.updateStatus = false;
                MyPlaylistFragment playlistFrag = new MyPlaylistFragment();
                fm.beginTransaction().replace(R.id.activity_container, playlistFrag).commit();
                break;
            /*case R.id.action_downloads:
                PlayListFragment.updateStatus = false;
                MySongsFragment mysongsFrag = new MySongsFragment();
                fm.beginTransaction().replace(R.id.activity_container, mysongsFrag).commit();
                break; */
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                overridePendingTransition(R.anim.fadein,R.anim.fadeout);
                startActivity(intent);

                break;
            case R.id.action_about:
                //Intent intent = new Intent(this, AboutActivity.class);
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.action_exit:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                onDestroy();
                                System.exit(0);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Are you Want to Exit?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
                break;
        }
        drawerLayout.closeDrawers();
        return true;
    }

    @Override
    protected void onResume() {
        musicSrv.forgroundStatus = true;
        super.onResume();
        serviceIntent = new Intent(this, MusicService.class);
        startService(serviceIntent);
        bindService(serviceIntent, musicConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        musicSrv.forgroundStatus = false;
        super.onStop();
        if (musicBound) {
            unbindService(musicConnection);
            musicBound = false;
        }
    }

    @Override
    public void onDestroy() {
        //unbindService(musicConnection);
        musicSrv.onDestroy();
        stopService(serviceIntent);
        musicBound = false;
        musicSrv.forgroundStatus = false;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (slidingLayout != null &&
                (slidingLayout.getPanelState() == PanelState.EXPANDED || slidingLayout.getPanelState() == PanelState.ANCHORED)) {
            slidingLayout.setPanelState(PanelState.COLLAPSED);
        } else {
            moveTaskToBack(true);
        }
            //getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if(b) { // Event is triggered only if the seekbar position was modified by the user
            int progress = seekBar.getProgress();
            int time = utils.progressToTimer(progress, musicSrv.getDuration());
            musicSrv.seekTo(time);
            seekBar.setProgress(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onPlayMusic(Songs songs) {
        setSongDetail(songs);
    }

    @Override
    public void onStopMusic() {
        mHandler.removeCallbacks(mUpdateTimeTask);
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

    private void showPlayListPopup(final Songs songs) {
        final List<PlayList> playLists = db.getPlayList();
        PlayListAdapter adapter;

        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.popup_content);
        d.setTitle("PlayLists");
        d.setCancelable(true);
        RecyclerView pRecyclerView = (RecyclerView) d.findViewById(R.id.playList_recyclerView);
        pRecyclerView.setHasFixedSize(true);
        pRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pRecyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(this, pRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                PlayList playList = playLists.get(position);
                //db.addPlayListSongs(songs, playList.getId());
                d.dismiss();
            }
            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        Button btn_createPlaylist = (Button) d.findViewById(R.id.btn_createPlaylist);
        adapter = new PlayListAdapter(this, R.layout.search_row, playLists);
        pRecyclerView.setAdapter(adapter);
        btn_createPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreatePlaylistDialog(songs);
                d.dismiss();
            }
        });
        d.show();
    }

    private void showCreatePlaylistDialog(final Songs songs) {
        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.dialog_add_playlist);
        d.setTitle("Create PlayList");
        d.setCancelable(false);
        final EditText et_title = (EditText) d.findViewById(R.id.et_title);
        Button btn_create = (Button) d.findViewById(R.id.btn_create);
        Button btn_cancel = (Button) d.findViewById(R.id.btn_cancel);
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = et_title.getText().toString();
                String description = songs.getAlbumArtUri()+"hqdefault.jpg";
                db.addPlayList(new PlayList(title, description), songs);
                d.dismiss();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                d.dismiss();
            }
        });
        d.show();
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        fm.putFragment(outState, "MAINFRAGMENT", fm.findFragmentById(R.id.activity_container));
    }

    @Override
    public void backPressed() {
        onBackPressed();
    }

    @Override
    public void showProgressBar() {
        Loader.setVisibility(View.VISIBLE);
        btn_play_short.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideProgressBar() {
        Loader.setVisibility(View.INVISIBLE);
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
    public void showmainProgressBar() {
        Loader.setVisibility(View.VISIBLE);
        btn_play_short.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hidemainProgressBar() {
        Loader.setVisibility(View.INVISIBLE);
        if(slidingLayout.getPanelState()==PanelState.COLLAPSED)
            btn_play_short.setVisibility(View.VISIBLE);
    }
}