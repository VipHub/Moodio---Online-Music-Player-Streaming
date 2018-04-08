package com.mongoose.app.moodio.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.mongoose.app.moodio.listener.ClickListener;
import com.mongoose.app.moodio.listener.DrawerToggleListener;
import com.mongoose.app.moodio.MainActivity;
import com.mongoose.app.moodio.R;
import com.mongoose.app.moodio.listener.RecyclerViewTouchListener;
import com.mongoose.app.moodio.SearchActivity;
import com.mongoose.app.moodio.listener.SongClickListener;
import com.mongoose.app.moodio.adapters.SongsAdapter;
import com.mongoose.app.moodio.model.Songs;
import com.mongoose.app.moodio.utils.AdManager;
import com.mongoose.app.moodio.utils.DatabaseHandler;
import com.mongoose.app.moodio.utils.LocalSongsManager;
import com.mongoose.app.moodio.utils.NetworkHandler;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
/**
 * A simple {@link Fragment} subclass.
 */
public class MySongsFragment extends Fragment implements SongsAdapter.TouchListener {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DrawerToggleListener drawerToggleListener;
    private CoordinatorLayout coordinatorLayout;
    private List<Songs> songsList;
    private SongClickListener songClickListener;
    private DatabaseHandler db;
    private NetworkHandler nh;
    //private AdManager adManager;
    private SongsAdapter adapter;
    private ImageView img_empty;

    public MySongsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_my_songs, container, false);
        setHasOptionsMenu(true);
        setToolbar(layout);
        //networkHandler = new NetworkHandler(getActivity());
        //db = new DatabaseHandler(getActivity());

        //swipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout);
        //swipeRefreshLayout.setColorSchemeColors(R.color.color_primary);
        //swipeRefreshLayout.setOnRefreshListener(this);
        coordinatorLayout = (CoordinatorLayout) layout.findViewById(R.id.rootLayout);
        recyclerView = (RecyclerView) layout.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        img_empty = (ImageView) layout.findViewById(R.id.img_empty);
        img_empty.setVisibility(View.GONE);
        nh = new NetworkHandler(getActivity());
        db = new DatabaseHandler(getActivity());
        //adManager = new AdManager(nh);
        //progressBar = (ProgressBar) layout.findViewById(R.id.progressBar);


        return layout;
    }

    private void setToolbar(View layout) {
        toolbar = (Toolbar) layout.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_action_icon);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        drawerToggleListener.setNavigationView(toolbar);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(getActivity(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                songClickListener.onSongClick(position, songsList);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        new Runnable() {
            @Override
            public void run() {
                if(MainActivity.LocalLoad) {
                    MainActivity.LocalLoad = false;
                    if (new LocalSongsManager(getActivity()).getPlayList())
                        updateViews();
                } else
                    updateViews();
            }
        }.run();
    }


    private void updateViews() {
        songsList = db.getDownSongs();
        Collections.sort(songsList, new Comparator<Songs>(){
            public int compare(Songs s1, Songs s2) {
                return s1.getTitle().compareToIgnoreCase(s2.getTitle());
            }
        });

        //songsList = adManager.addAd(songsList);

        if(songsList.size() == 0){
            img_empty.setVisibility(View.VISIBLE);
            Snackbar.make(coordinatorLayout, "NO Songs", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Download", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), SearchActivity.class);
                            intent.putExtra("CONTEXT", "MySongFragment");
                            startActivityForResult(intent,1);
                        }
                    }).show();
        }
        adapter = new SongsAdapter(getActivity(), songsList,true);
        adapter.setTouchListener(this);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            songClickListener = (SongClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SongClickListener");
        }
        try {
            drawerToggleListener = (DrawerToggleListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DrawerToggleListener");
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra("CONTEXT", "MySongFragment");
                startActivityForResult(intent,1);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == 1) {
                songsList = db.getRecentSongs();
                songClickListener.onSongClick(songsList.size()-1, songsList);
                Toast.makeText(getActivity(),"Song Added to Queue",Toast.LENGTH_LONG)
                        .show();
            } else if(resultCode == 2) {
                String title = data.getStringExtra("title");
                songsList = db.getRecentSongs();
                for (int i = 0; i < songsList.size(); i++) {
                    if(title.equalsIgnoreCase(songsList.get(i).getTitle())) {
                        songClickListener.onSongClick(i,songsList);
                        break;
                    }
                }
            }
        }
    }
    @Override
    public void itemTouched(View v, final int position) {
        final PopupMenu popup = new PopupMenu(getActivity(), v);
        popup.getMenuInflater().inflate(R.menu.delete_song, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.action_delete_song:
                        File file = new File(songsList.get(position).getPath());
                        file.delete();
                        Toast.makeText(getActivity(),"Song Succesfully Deleted",Toast.LENGTH_LONG).show();
                        songsList.remove(position);
                        adapter.setList(songsList);
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

}
