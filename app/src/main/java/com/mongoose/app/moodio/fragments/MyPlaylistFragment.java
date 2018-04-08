package com.mongoose.app.moodio.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.mongoose.app.moodio.listener.ClickListener;
import com.mongoose.app.moodio.listener.DrawerToggleListener;
import com.mongoose.app.moodio.PlayListActivity;
import com.mongoose.app.moodio.R;
import com.mongoose.app.moodio.listener.RecyclerViewTouchListener;
import com.mongoose.app.moodio.SearchActivity;
import com.mongoose.app.moodio.listener.SongClickListener;
import com.mongoose.app.moodio.adapters.PlayListAdapter;
import com.mongoose.app.moodio.adapters.SongsAdapter;
import com.mongoose.app.moodio.model.PlayList;
import com.mongoose.app.moodio.model.Songs;
import com.mongoose.app.moodio.utils.AdManager;
import com.mongoose.app.moodio.utils.DatabaseHandler;
import com.mongoose.app.moodio.utils.NetworkHandler;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyPlaylistFragment extends Fragment implements PlayListAdapter.TouchListener,SongsAdapter.TouchListener {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private DrawerToggleListener drawerToggleListener;
    private CoordinatorLayout coordinatorLayout;
    private SongClickListener songClickListener;
    private List<PlayList> playLists;
    private DatabaseHandler db;
    private NetworkHandler nh;
    //private AdManager adManager;
    private ImageView img_empty;

    public MyPlaylistFragment() {
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
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2 , GridLayoutManager.VERTICAL, false));
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
                PlayList playList = playLists.get(position);
                Log.d("id",playList.getId());
                if(playList.getViewType() == 1){
                    Intent intent = new Intent(getActivity(), PlayListActivity.class);
                    intent.putExtra("PLAYLIST_TYPE", "user");
                    intent.putExtra("PLAYLIST_ID", playList.getId());
                    intent.putExtra("PLAYLIST_TITLE", playList.getTitle());
                    intent.putExtra("PLAYLIST_DESC", playList.getDescription());
                    startActivity(intent);
                }
                List<Songs> abc = db.getPlayListSongs(Integer.parseInt(playList.getId()));
                for(int i=0;i<abc.size();i++)
                    Log.d("Songs", i+"."+abc.get(i).getTitle());
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        updateViews();

    }


    private void updateViews() {

        playLists = db.getPlayList();
        /*Collections.sort(songsList, new Comparator<Songs>(){
            public int compare(Songs s1, Songs s2) {
                return s1.getTitle().compareToIgnoreCase(s2.getTitle());
            }
        });

        if(nh.isOnline()) {
            if(songsList.size() > 2) {
                Songs ad = new Songs();
                ad.setViewType(2);
                songsList.add(2, ad);
            }
        } */
        //playLists = adManager.addPlaylistAd(playLists);
        if(playLists.size() == 0){
            Snackbar.make(coordinatorLayout, "NO Playlists", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Search to Add Playlist", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), SearchActivity.class);
                            intent.putExtra("CONTEXT", "MySongFragment");
                            startActivityForResult(intent,1);
                        }
                    }).show();
            img_empty.setVisibility(View.VISIBLE);
        }
        PlayListAdapter adapter = new PlayListAdapter(getActivity(), R.layout.playlist_row, playLists);
        adapter.setTouchListener(this);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            drawerToggleListener = (DrawerToggleListener) activity;
            songClickListener = (SongClickListener) activity;
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
        List<Songs> songsList = new ArrayList<>();
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
        PopupMenu popup = new PopupMenu(getActivity(), v);
        popup.getMenuInflater().inflate(R.menu.my_playlist_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.action_delete_playlist:
                        //File file = new File(songsList.get(position).getPath());
                        //file.delete();
                        db.removePlayList(Integer.parseInt(playLists.get(position).getId()));
                        Toast.makeText(getActivity(),"Playlist Succesfully Deleted",Toast.LENGTH_LONG).show();
                        updateViews();
                        break;
					/*case R.id.action_generate_json:
						Gson gson = new GsonBuilder().setPrettyPrinting().create();
						try {
								// TODO add your handling code here:
                                PlayList playList = playLists.get(position);
        						File file = new File(Environment.getExternalStoragePublicDirectory("")+"/TestProject/"+playList.getTitle()+".json");
        						// creates the file
        						file.createNewFile();
        
        						// creates a FileWriter Object
        						FileWriter writer = new FileWriter(file);
        						// Writes the content to the file
        						writer.write(gson.toJson(db.getPlayListSongs(Integer.parseInt(playList.getId()))));
                                Toast.makeText(getActivity(),"File Created",Toast.LENGTH_LONG).show();
        						writer.flush();
        						writer.close();
    						} catch (IOException ex) {
     							Log.d("io",ex.toString());
    						}
						break;*/
                }
                return true;
            }
        });
        popup.show();
    }
}
