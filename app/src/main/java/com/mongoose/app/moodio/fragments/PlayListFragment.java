package com.mongoose.app.moodio.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.mongoose.app.moodio.listener.ClickListener;
import com.mongoose.app.moodio.MainActivity;
import com.mongoose.app.moodio.PlayListActivity;
import com.mongoose.app.moodio.R;
import com.mongoose.app.moodio.listener.RecyclerViewTouchListener;
import com.mongoose.app.moodio.SearchActivity;
import com.mongoose.app.moodio.SongsDetailActivity;
import com.mongoose.app.moodio.WebService;
import com.mongoose.app.moodio.adapters.PlayListAdapter;
import com.mongoose.app.moodio.model.PlayList;
import com.mongoose.app.moodio.utils.AdManager;
import com.mongoose.app.moodio.utils.Conf;
import com.mongoose.app.moodio.utils.DatabaseHandler;
import com.mongoose.app.moodio.utils.NetworkHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlayListFragment extends Fragment implements PlayListAdapter.TouchListener, SwipeRefreshLayout.OnRefreshListener, LibraryFragment.FragmentRefreshListener {

    private RecyclerView recyclerView;
    //private SwipeRefreshLayout swipeRefreshLayout;
    private NetworkHandler networkHandler;
    private ProgressBar progressBar;
    private List<PlayList> playLists;
    PlayListAdapter adapter;
    private String LANG;
    public static boolean updateStatus = true;
    public Call<ResponseBody> call;
    //private AdManager adManager;
    private DatabaseHandler db;
    private ImageView img_empty;

    public PlayListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_songs, container, false);
        setHasOptionsMenu(true);
        networkHandler = new NetworkHandler(getActivity());
        db = new DatabaseHandler(getActivity());
        //adManager = new AdManager(networkHandler);
        LANG = getLang();
        //swipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout);
        //swipeRefreshLayout.setColorSchemeColors(R.color.color_primary);
        //swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView = (RecyclerView) layout.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2 , GridLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        progressBar = (ProgressBar) layout.findViewById(R.id.progressBar);
        img_empty = (ImageView) layout.findViewById(R.id.img_empty);
        img_empty.setVisibility(View.GONE);

        return layout;
    }

    private String getLang() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("LANG_PREFS", Context.MODE_PRIVATE);
        String lang = sharedPref.getString("LANG_PREFS_KEY","notSet");
        return lang;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
            recyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(getActivity(), recyclerView, new ClickListener() {
                @Override
                public void onClick(View view, int position) {
                    //FragmentManager fm = getActivity().getSupportFragmentManager();
                    //Fragment frag = new MySongsFragment();
                    //fm.beginTransaction().addToBackStack(null).replace(R.id.activity_container, frag).commit();
                    PlayList playList = playLists.get(position);
                    if(playList.getViewType() == 1 ) {
                        Intent intent = new Intent(getActivity(), PlayListActivity.class);
                        intent.putExtra("PLAYLIST_TYPE","fix");
                        intent.putExtra("PLAYLIST_ID", playList.getId());
                        intent.putExtra("PLAYLIST_TITLE", playList.getTitle());
                        intent.putExtra("PLAYLIST_DESC", playList.getDescription());
                        intent.putExtra("PLAYLIST_SOURCE", playList.getSource());
                        startActivity(intent);
                    }
                }

                @Override
                public void onLongClick(View view, int position) {

                }
            }));
            if(MainActivity.FirstTime) {
                if (networkHandler.isOnline()) {
                    requestDataFromServer(LANG);
                    MainActivity.FirstTime = false;
                }
            } else {
                playLists = db.getTempPlayList();
                if(playLists == null) {
                    if (networkHandler.isOnline()) {
                        requestDataFromServer(LANG);
                        MainActivity.FirstTime = false;
                    }
                } else {
                    if (updateStatus)
                        updateViews();
                }
            }
        }

    /*private void requestDataFromServer(String lang) {
            //if (!swipeRefreshLayout.isRefreshing())
                progressBar.setVisibility(View.VISIBLE);

            final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Conf.SERVER_ADD)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

            WebService api = retrofit.create(WebService.class);

            call = api.getPlaylist(lang+".json");


            call.enqueue(new Callback<List<PlayList>>() {
                         @Override
                         public void onResponse(Call<List<PlayList>> call, Response<List<PlayList>> response) {
                             //if (swipeRefreshLayout.isRefreshing())
                                 //swipeRefreshLayout.setRefreshing(false);
                             progressBar.setVisibility(View.GONE);
                             playLists = response.body();
                             db.ClearTempPlayList();
                             new Runnable(){
                                 @Override
                                 public void run() {
                                     Log.d("DB","play addded");
                                     if(playLists != null)
                                         db.addTempPlayList(playLists);
                                 }
                             }.run();
                             if(updateStatus)
                                 updateViews();
                         }

                         @Override
                         public void onFailure(Call<List<PlayList>> call, Throwable t) {
                             //if (swipeRefreshLayout.isRefreshing())
                                // swipeRefreshLayout.setRefreshing(false);
                             progressBar.setVisibility(View.GONE);
                             Snackbar.make(recyclerView, "No Connection.."+t.toString(), Snackbar.LENGTH_LONG)
                                     .setAction("Try Again", new View.OnClickListener() {
                                         @Override
                                         public void onClick(View view) {
                                             requestDataFromServer(LANG);
                                         }
                                     }).show();
                         }
                     });
    }*/

    public void requestDataFromServer(String lang) {  // return songs from playlist
        //if (!swipeRefreshLayout.isRefreshing())
        progressBar.setVisibility(View.VISIBLE);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .build();
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Conf.API_SERVER)
                .client(okHttpClient)
                .build();
        final WebService api = retrofit.create(WebService.class);

        Call<ResponseBody> call = api.callApi(lang+".html");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
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
                playLists = gson.fromJson(jsoncode, new TypeToken<List<PlayList>>(){}.getType());
                //sb.toString();
                progressBar.setVisibility(View.GONE);

                db.ClearTempPlayList();
                new Runnable(){
                    @Override
                    public void run() {
                        Log.d("DB","play addded");
                        if(playLists != null)
                            db.addTempPlayList(playLists);
                    }
                }.run();
                if(updateStatus)
                    updateViews();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //if (swipeRefreshLayout.isRefreshing())
                //swipeRefreshLayout.setRefreshing(false);
                img_empty.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Snackbar.make(recyclerView, "No Connection..", Snackbar.LENGTH_LONG)
                        .setAction("Try Again", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                requestDataFromServer(LANG);
                            }
                        }).show();
            }
        });
    }

    private void addSongsToDb() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                //db.addSongs(songsList);
            }
        });
        thread.start();
    }

    private void updateViews() {
        //playLists = adManager.addPlaylistAd(playLists);

        if(playLists == null) {
            img_empty.setVisibility(View.VISIBLE);
            Snackbar.make(recyclerView, "No Connection..", Snackbar.LENGTH_LONG)
                    .setAction("Try Again", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestDataFromServer(LANG);
                        }
                    }).show();
        }
        adapter = new PlayListAdapter(getActivity(), R.layout.playlist_row, playLists);
        adapter.setTouchListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void showSongsDetail(int position) {
        //Songs songs = songsList.get(position);
        Intent intent = new Intent(getActivity(), SongsDetailActivity.class);
        //intent.putExtra("TITLE", songs.getTitle());
        //intent.putExtra("ARTIST", songs.getArtist());
        //intent.putExtra("LYRICS", songs.getLyrics());

        startActivity(intent);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onRefresh() {
        //requestDataFromServer(LANG);
    }
    @Override
    public void onResume() {
        //requestDataFromServer(LANG);
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra("CONTEXT", "SongsFragment");
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void refreshFragment() {
        //updateViews();
    }

    @Override
    public void itemTouched(View v, final int position) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        popup.getMenuInflater().inflate(R.menu.playlist_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.action_open:
                        PlayList playList = playLists.get(position);
                        Intent intent = new Intent(getActivity(), PlayListActivity.class);
                        intent.putExtra("PLAYLIST_TYPE","fix");
                        intent.putExtra("PLAYLIST_ID", playList.getId());
                        intent.putExtra("PLAYLIST_TITLE", playList.getTitle());
                        intent.putExtra("PLAYLIST_DESC", playList.getDescription());
                        intent.putExtra("PLAYLIST_SOURCE", playList.getSource());
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
        popup.show();
    }
}
