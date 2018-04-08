package com.mongoose.app.moodio.fragments;


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.mongoose.app.moodio.listener.ClickListener;
import com.mongoose.app.moodio.R;
import com.mongoose.app.moodio.listener.RecyclerViewTouchListener;
import com.mongoose.app.moodio.listener.SongClickListener;
import com.mongoose.app.moodio.adapters.PlayListAdapter;
import com.mongoose.app.moodio.adapters.SongsAdapter;
import com.mongoose.app.moodio.comp.MyTextView;
import com.mongoose.app.moodio.model.Songs;
import com.mongoose.app.moodio.utils.AdManager;
import com.mongoose.app.moodio.utils.MusicRetriever;
import com.mongoose.app.moodio.utils.NetworkHandler;
import com.mongoose.app.moodio.model.PlayList;
import com.mongoose.app.moodio.utils.DatabaseHandler;
import com.mongoose.app.moodio.utils.DownloadSong;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicFragment extends Fragment implements SongsAdapter.TouchListener, LibraryFragment.FragmentRefreshListener {

    private List<Songs> songsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SongClickListener songClickListener;
    private DatabaseHandler db;
    private MusicRetriever musicRetriever;
    private SongsAdapter adapter;
    private NetworkHandler nh;
    //private AdManager adManager;
    private ImageView img_empty;

    public MusicFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_music, container, false);
        db = new DatabaseHandler(getActivity());
        recyclerView = (RecyclerView) layout.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        img_empty = (ImageView) layout.findViewById(R.id.img_empty);
        img_empty.setVisibility(View.GONE);
        nh = new NetworkHandler(getActivity());
        //adManager = new AdManager(nh);
        return layout;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //musicRetriever = new MusicRetriever(getActivity());
        //songsList = musicRetriever.prepare();
        recyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(getActivity(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.d("com.vips", "" + position);
                if(songsList.get(position).getViewType()==1) {
                    songClickListener.onSongClick(position, songsList);
                }
            }
            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        updateView();
    }
    public void updateView() {
        songsList = db.getRecentSongs();
        if(songsList.size() == 0)
            img_empty.setVisibility(View.VISIBLE);
        else
            img_empty.setVisibility(View.GONE);
        //songsList = adManager.addAd(songsList);
        adapter = new SongsAdapter(getActivity(), songsList,true);
        adapter.setTouchListener(this);
        recyclerView.setAdapter(adapter);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            songClickListener = (SongClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SongClickListener");
        }
    }

    @Override
    public void itemTouched(View v, final int position) {
        switch (v.getId()) {
            case R.id.btn_close:
                PopupMenu popup = new PopupMenu(getActivity(), v);
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    int id = menuItem.getItemId();
                    switch (id) {
                        case R.id.action_download:
                            showDownloadPopup(position);
                            break;
                        case R.id.action_add_song:
                            showPlayListPopup(songsList.get(position));
                            break;
                        case R.id.action_remove_song:
                            db.removeRecentSong(songsList.get(position).getPath());
                            Toast.makeText(getActivity(), "Song Removed from Queue", Toast.LENGTH_LONG).show();
                            songsList.remove(position);
                            adapter.setList(songsList);
                            break;
                    }
                    return true;
                    }
                });
                popup.show();
                break;
            case R.id.btn_play:
                showDownloadPopup(position);
                break;
        }
    }

    private void showPlayListPopup(final Songs songs) {
        final List<PlayList> playLists = db.getPlayList();
        PlayListAdapter adapter;

        final Dialog d = new Dialog(getActivity());
        d.setContentView(R.layout.popup_content);
        d.setTitle("PlayLists");
        d.setCancelable(true);
        RecyclerView pRecyclerView = (RecyclerView) d.findViewById(R.id.playList_recyclerView);
        pRecyclerView.setHasFixedSize(true);
        pRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        pRecyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(getActivity(), pRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                PlayList playList = playLists.get(position);
                db.addPlayListSongs(songs, Integer.parseInt(playList.getId()));
                d.dismiss();
                Toast.makeText(getActivity(),"Song Added to Playlist",Toast.LENGTH_LONG)
                        .show();
            }
            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        Button btn_createPlaylist = (Button) d.findViewById(R.id.btn_createPlaylist);
        adapter = new PlayListAdapter(getActivity(), R.layout.search_row, playLists);
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
    private void showDownloadPopup(final int position) {

        final Dialog d = new Dialog(getActivity());
        d.setContentView(R.layout.popup_download);
        d.setTitle(songsList.get(position).getTitle());
        d.setCancelable(true);

        final Button btn_down = (Button) d.findViewById(R.id.down);
        MyTextView tv_tit = (MyTextView) d.findViewById(R.id.tv_title);
        tv_tit.setText(songsList.get(position).getTitle());
        AdView mAdView = (AdView) d.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(getActivity().getResources().getString(R.string.device_id)).build();
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
                if(ds.add(songsList.get(position),getActivity())){
                    Toast.makeText(getActivity(),"Song Added For Downloading",Toast.LENGTH_LONG).show();
                }
                d.dismiss();
            }
        });

        d.show();
    }

    private void showCreatePlaylistDialog(final Songs songs) {
        final Dialog d = new Dialog(getActivity());
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
                Toast.makeText(getActivity(),"Playlist Created",Toast.LENGTH_LONG)
                        .show();
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

    @Override
    public void refreshFragment() {

        //updateView();
    }
    @Override
    public void onResume() {
        updateView();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


}
