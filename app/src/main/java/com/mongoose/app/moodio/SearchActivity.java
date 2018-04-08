package com.mongoose.app.moodio;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mongoose.app.moodio.adapters.PlayListAdapter;
import com.mongoose.app.moodio.adapters.SongsAdapter;
import com.mongoose.app.moodio.comp.MyTextView;
import com.mongoose.app.moodio.listener.ClickListener;
import com.mongoose.app.moodio.listener.RecyclerViewTouchListener;
import com.mongoose.app.moodio.model.PlayList;
import com.mongoose.app.moodio.model.Songs;
import com.mongoose.app.moodio.utils.AdManager;
import com.mongoose.app.moodio.utils.Conf;
import com.mongoose.app.moodio.utils.DatabaseHandler;
import com.mongoose.app.moodio.utils.DownloadSong;
import com.mongoose.app.moodio.utils.MusicRetriever;
import com.mongoose.app.moodio.utils.NetworkHandler;
import com.mongoose.app.moodio.utils.Parser;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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


public class SearchActivity extends AppCompatActivity implements SongsAdapter.TouchListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener ,SwipyRefreshLayout.OnRefreshListener {

    private Toolbar toolbar;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private DatabaseHandler db;
    private List<Songs> songsList = null;
    private String context;
    private Context con;
    private ProgressBar progressBar;
    private SwipyRefreshLayout swipeRefreshLayout;
    private MyTextView textView;
    private ImageView search_icon;
    private Call<ResponseBody> call;
    private SongsAdapter adapter;
    private LinearLayoutManager mLinearLayoutManager;
    private boolean mLoading = false;
    public String gbl_query;
    //private AdManager adManager;
    private int gbl_page = 1;
    private boolean loadmore = true;

    private String[] SUGGESTIONS = new String[10];

    private SimpleCursorAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setToolbar();
        db = new DatabaseHandler(this);
        Intent intent = getIntent();
        context = intent.getStringExtra("CONTEXT");
        Log.d("com.asterisk", "" + context);
        con = this;

        final String[] from = new String[] {"cityName"};
        final int[] to = new int[] {android.R.id.text1};
        mAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                null,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        //adManager = new AdManager(new NetworkHandler(this));
        search_icon = (ImageView) findViewById(R.id.search_imageView);
        textView = (MyTextView) findViewById(R.id.error_textView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        swipeRefreshLayout = (SwipyRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(R.color.color_primary);
        swipeRefreshLayout.setOnRefreshListener(this);
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.onActionViewExpanded();
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

        searchView.setSuggestionsAdapter(mAdapter);
        searchView.setIconifiedByDefault(false);

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                searchView.setQuery(SUGGESTIONS[position],true);
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                searchView.setQuery(SUGGESTIONS[position],true);
                return false;
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(this, recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.d(songsList.get(position).getTitle(), "" + position);

                if(db.addRecentSongs(songsList.get(position))) {
                    setResult(1);
                    finish();
                }
                else {
                    Intent intent1 = new Intent();
                    intent1.putExtra("title",songsList.get(position).getTitle());
                    setResult(2,intent1);
                    finish();
                    Toast.makeText(SearchActivity.this, "Song Already in Queue", Toast.LENGTH_LONG).show();
            }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if(songsList != null)
                        if(mLinearLayoutManager.findLastVisibleItemPosition() == songsList.size()-1)
                            if(loadmore) {
                                Toast.makeText(SearchActivity.this, "Swipe Up to Load More", Toast.LENGTH_LONG).show();
                                loadmore = false;
                            }
                }
            });
        }
    }

    private void populateAdapter(String query) {

        final MatrixCursor c = new MatrixCursor(new String[]{ BaseColumns._ID, "cityName" });
        for (int i=0; i<SUGGESTIONS.length; i++) {
            if (SUGGESTIONS[i].toLowerCase().startsWith(query.toLowerCase()))
                c.addRow(new Object[] {i, SUGGESTIONS[i]});
        }
        mAdapter.changeCursor(c);
    }
    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /*private void requestDataFromServer(String query, final Boolean newPage) {
        if (!swipeRefreshLayout.isRefreshing())
            progressBar.setVisibility(View.VISIBLE);

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Conf.API_ADD)
                .build();
        WebService api = retrofit.create(WebService.class);
        if(newPage){
             call = api.getSongsFromServerNewPage(query.replaceAll(" ", "+"),MusicRetriever.PAGE_TOKEN);
        } else {
             call = api.getSongsFromServer(query.replaceAll(" ", "+"));
        }
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //Try to get response body
                //BufferedReader reader = null;
                //StringBuilder sb = new StringBuilder();
                //try {
                    //reader = new BufferedReader(new InputStreamReader(
                           // new ByteArrayInputStream(response.body().string().getBytes())));
               // } catch (IOException e) {
                //    e.printStackTrace();
               // }
                //String line;
                  //  try {
                     //   while ((line = reader.readLine()) != null) {
                       //     sb.append("\n");
                        //    sb.append(line);
                       // }
                    //} catch (IOException e) {
                      //  e.printStackTrace();
                    //}

                String resultp = null;
                try {
                    resultp = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //sb.toString();
                    try {
                        if(!newPage) {
                            songsList = new MusicRetriever(con).execute(resultp).get();
                            songsList = adManager.addAd(songsList);
                            updateViews();
                        } else {
                            songsList.addAll(new MusicRetriever(con).execute(resultp).get());
                            dataChanged();
                            mLoading = false;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (swipeRefreshLayout.isRefreshing())
                     swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                Snackbar.make(recyclerView, "No Connection..", Snackbar.LENGTH_LONG)
                        .setAction("Try Again", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //requestDataFromServer();
                            }
                        }).show();
            }
        });

        //method is already commented
        api.getSongsFromServer(query.replaceAll(" ","+") ,new Callback<Response>() {
            @Override
            public void success(Response result, Response response) {

            }
            @Override
            public void failure(RetrofitError error) {
                if (swipeRefreshLayout.isRefreshing())
                    swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                Snackbar.make(recyclerView, "" + error, Snackbar.LENGTH_LONG)
                        .setAction("Try Again", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //requestDataFromServer(query);
                            }
                        })
                        .show();
            }
        });
    } */

    public void getSuggestions(final String query) {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Conf.SUG_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WebService api = retrofit.create(WebService.class);
        if(query.isEmpty())
            return;
        Call<ResponseBody> call = api.getSuggestions(query.replaceAll(" ","+"));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String temp;
                try {
                    //Document d = Jsoup.parse();
                    temp = response.body().string().replace("window.google.ac.h(","");
                    temp = temp.replace(")","");
                    //temp = response.body().string();

                    //JSONObject ob = new JSONObject(temp);

                    JSONArray arr = new JSONArray(temp);
                    JSONArray j = arr.getJSONArray(1);

                    for(int i=0; i<j.length(); i++){
                        //JSONArray jaa = j.getJSONArray(i).getJSONObject(0);
                        SUGGESTIONS[i] = j.getJSONArray(i).getString(0);
                    }
                    //System.out.println(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                populateAdapter(query);
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Snackbar.make(recyclerView, "" + t, Snackbar.LENGTH_LONG)
                        .setAction("Try Again", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //requestDataFromServer("language.html");
                            }
                        })
                        .show();
            }
        });

    }
    public void requestDataFromServer(String query, final int newPage) {  // return songs from playlist
        //if (!swipeRefreshLayout.isRefreshing())
        progressBar.setVisibility(View.VISIBLE);

        if(newPage == 1)
            loadmore = true;

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .build();
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Conf.NEW_YT_SERVER)
                .client(okHttpClient)
                .build();
        WebService api = retrofit.create(WebService.class);

        Call<ResponseBody> call = api.getSongsFromDirectResults(query.replaceAll(" ","+"),newPage);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String resultp = null;
                try {
                    resultp = response.body().string();      // Raw html data stored
                    //System.out.println(resultp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //sb.toString();
                try {
                    if(newPage == 1) {
                        songsList = new Parser().execute(resultp, "results").get(); // Raw html data send to get results from html page
                        //songsList = adManager.addAd(songsList);
                        updateViews(false);
                    } else {
                        //songsList.addAll(adManager.addAd(new Parser().execute(resultp,"results").get()));
                        updateViews(true);
                    }
                    //songsList.remove(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                //swipeRefreshLayout.setRefreshing(false);
                if(swipeRefreshLayout.isRefreshing())
                    swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //if (swipeRefreshLayout.isRefreshing())
                //swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                Snackbar.make(recyclerView, "No Connection..", Snackbar.LENGTH_LONG)
                        .setAction("Try Again", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //requestDataFromServer();
                            }
                        }).show();
            }
        });
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        //if (context.equalsIgnoreCase("LibraryFragment"))
            //try {
                db.addTerminSearchTable(query);
                gbl_query = query;
                gbl_page = 1;
                requestDataFromServer(query,gbl_page);
                //songsList = new MusicRetriever(this).execute(query).get();
            //} catch (InterruptedException e) {
             //   e.printStackTrace();
            //} catch (ExecutionException e) {
             //   e.printStackTrace();
           // }
        //updateViews();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        int len = newText.length();

        //setSearchSugg(newText);

        getSuggestions(newText);

        if (false) {
                try {
                    songsList = new MusicRetriever(this).execute(newText).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            //updateViews();
        }
        return false;
    }

    private void updateViews(boolean refresh) {
        //adapter = new SearchAdapter(this, songsList);
        //recyclerView.setAdapter(adapter);
        if(songsList.isEmpty()) {
            textView.setText("SORRY No Result\n Try Another Search term..!");
        }else {
            textView.setVisibility(View.GONE);
            search_icon.setVisibility(View.GONE);
        }
        if(refresh) {
            adapter.setList(songsList);
            //progressBar.setVisibility(View.GONE);
        } else {
            adapter = new SongsAdapter(this, songsList,false);
            adapter.setTouchListener(this);
            recyclerView.setAdapter(adapter);
        }
    }
    private void dataChanged() {
        adapter.setList(songsList);
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public void itemTouched(View v, final int position) {
        PopupMenu popup = new PopupMenu(SearchActivity.this, v);
        popup.getMenuInflater().inflate(R.menu.popup_menu_search, popup.getMenu());
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
                }
                return true;
            }
        });
        popup.show();
    }

    private void showPlayListPopup(final Songs songs) {  //dialog to add song in playlist
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
                db.addPlayListSongs(songs, Integer.parseInt(playList.getId()));
                d.dismiss();
                Toast.makeText(SearchActivity.this,"Song Added to Playlist",Toast.LENGTH_LONG)
                        .show();
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

    private void showCreatePlaylistDialog(final Songs songs) { //diaglog to create local playlist
        final Dialog d = new Dialog(this);
        d.setTitle("Create PlayList");
        d.setContentView(R.layout.dialog_add_playlist);
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
                Toast.makeText(SearchActivity.this,"Playlist Created",Toast.LENGTH_LONG)
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


    private void showDownloadPopup(final int position) {  ///genrate download ad diaglog

        final Dialog d = new Dialog(SearchActivity.this);
        d.setContentView(R.layout.popup_download);
        d.setTitle(songsList.get(position).getTitle());
        d.setCancelable(true);

        final Button btn_down = (Button) d.findViewById(R.id.down);
        MyTextView tv_tit = (MyTextView) d.findViewById(R.id.tv_title);
        tv_tit.setText(songsList.get(position).getTitle());
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
                if(ds.add(songsList.get(position),SearchActivity.this)){
                    Toast.makeText(SearchActivity.this,"Song Added For Downloading",Toast.LENGTH_LONG).show();
                }
                d.dismiss();
            }
        });

        d.show();
    }

    @Override
    public void onResume() {
        MainActivity.musicSrv.forgroundStatus = true;
        super.onResume();
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {
        requestDataFromServer(gbl_query,++gbl_page);
    }
}
