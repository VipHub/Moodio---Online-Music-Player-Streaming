package com.mongoose.app.moodio;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.mongoose.app.moodio.comp.MyTextView;
import com.mongoose.app.moodio.utils.SongUtils;

import java.io.IOException;


public class SongsDetailActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener
        , SeekBar.OnSeekBarChangeListener {

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private MyTextView tv_title, tv_lyrics, tv_artist;
    private ImageButton btn_play, btn_repeat;
    private SeekBar songProgressbar;
    private MediaPlayer mp;
    private Boolean isRepeat = false;
    private Handler mHandler = new Handler();
    private SongUtils timer;
    private NestedScrollView scrollView;
    String title, artist, lyrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_detail);

        Intent intent = getIntent();
        title = intent.getStringExtra("TITLE");
        artist = intent.getStringExtra("ARTIST");
        lyrics = intent.getStringExtra("LYRICS");

        setCollapsingAppBar();
        mp = new MediaPlayer();
        timer = new SongUtils();

        tv_title = (MyTextView) findViewById(R.id.tv_title);
        tv_artist = (MyTextView) findViewById(R.id.tv_artist);
        tv_lyrics = (MyTextView) findViewById(R.id.tv_lyrics);
        btn_play = (ImageButton) findViewById(R.id.btn_play);
        songProgressbar = (SeekBar) findViewById(R.id.songProgressBar);
        scrollView = (NestedScrollView) findViewById(R.id.scrollView);

        tv_title.setText(title);
        tv_artist.setText(artist);
        tv_lyrics.setText(lyrics);

        mp.setOnCompletionListener(this);
        songProgressbar.setOnSeekBarChangeListener(this);

        setMediaPlayer();

        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mp.isPlaying()) {
                    mp.start();
                    btn_play.setImageResource(R.drawable.ic_pause_circle_fill_black_48dp);
                } else {
                    mp.pause();
                    btn_play.setImageResource(R.drawable.ic_play_circle_fill_black_48dp);
                }
            }
        });

       /* btn_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRepeat) {
                    Toast.makeText(SongsDetailActivity.this, "Repeat is off.", Toast.LENGTH_SHORT).show();
                    isRepeat = false;
                }else {
                    Toast.makeText(SongsDetailActivity.this, "Repeat is on.", Toast.LENGTH_SHORT).show();
                    isRepeat = true;
                }
            }
        });*/

    }

    private void setMediaPlayer() {
        try {
            mp.reset();
            mp.setDataSource(Environment.getExternalStorageDirectory() + "/rolling in the deep.mp4");
            mp.prepare();

            songProgressbar.setProgress(0);
            songProgressbar.setMax(100);

            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }


    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();

            // Displaying Total Duration time
            //songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            //songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int)(timer.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            songProgressbar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    private void setCollapsingAppBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(title);

        ImageView img_backdrop = (ImageView) findViewById(R.id.backdrop);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_songs_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                break;

            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(isRepeat){
            //setMediaPlayer();
            mp.start();
        } else{
            mp.stop();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        int time = timer.progressToTimer(progress, mp.getDuration());
        mp.seekTo(time);
        seekBar.setProgress(progress);
    }
}
