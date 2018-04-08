package com.mongoose.app.moodio.fragments;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongoose.app.moodio.R;
import com.mongoose.app.moodio.WebService;
import com.mongoose.app.moodio.adapters.PlayListAdapter;
import com.mongoose.app.moodio.adapters.SongsAdapter;
import com.mongoose.app.moodio.comp.MyTextView;
import com.mongoose.app.moodio.listener.ClickListener;
import com.mongoose.app.moodio.listener.RecyclerViewTouchListener;
import com.mongoose.app.moodio.listener.SongClickListener;
import com.mongoose.app.moodio.model.PlayList;
import com.mongoose.app.moodio.model.Songs;
import com.mongoose.app.moodio.utils.AdManager;
import com.mongoose.app.moodio.utils.Conf;
import com.mongoose.app.moodio.utils.DatabaseHandler;
import com.mongoose.app.moodio.utils.DownloadSong;
import com.mongoose.app.moodio.utils.MusicRetriever;
import com.mongoose.app.moodio.utils.NetworkHandler;
import com.mongoose.app.moodio.utils.Parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TrendingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TrendingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrendingFragment extends Fragment implements LibraryFragment.FragmentRefreshListener,SongsAdapter.TouchListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private List<Songs> songsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SongClickListener songClickListener;
    private DatabaseHandler db;
    private MusicRetriever musicRetriever;
    private SongsAdapter adapter;
    private NetworkHandler nh;
    //private AdManager adManager;
    private ImageView img_empty;
    private ProgressBar progressBar;
    public Retrofit retrofit;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public TrendingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TrendingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TrendingFragment newInstance(String param1, String param2) {
        TrendingFragment fragment = new TrendingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_trending, container, false);

        View layout = inflater.inflate(R.layout.fragment_trending, container, false);
        db = new DatabaseHandler(getActivity());
        recyclerView = (RecyclerView) layout.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        progressBar = (ProgressBar) layout.findViewById(R.id.progressBar);

        img_empty = (ImageView) layout.findViewById(R.id.img_empty);
        //img_empty.setVisibility(View.GONE);

        nh = new NetworkHandler(getActivity());
        //adManager = new AdManager(nh);

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
        //updateView();
        getSongsFromPlaylist("PLFgquLnL59akdKNX_4XuE2_kqT7iQp-Ds");

        return layout;
    }
    public void updateView() {
        //songsList = getSongsFromPlaylist("");
        if(songsList.size() == 0)
            img_empty.setVisibility(View.VISIBLE);
        else
            img_empty.setVisibility(View.GONE);
        //songsList = adManager.addAd(songsList);
        adapter = new SongsAdapter(getActivity(), songsList,true);
        adapter.setTouchListener(this);
        recyclerView.setAdapter(adapter);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            songClickListener = (SongClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement SongClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void refreshFragment() {

    }

    @Override
    public void itemTouched(View v, final int position) {
        switch (v.getId()) {
            case R.id.btn_close:
                PopupMenu popup = new PopupMenu(getActivity(), v);
                popup.getMenuInflater().inflate(R.menu.popup_add_song, popup.getMenu());
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /*private void requestDataFromServer(String id) {
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
                songsList = adManager.addAd(songsList);
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
    } */
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
                    updateView();
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
                                //getSongsFromPlaylist(playListId);
                            }
                        }).show();
            }
        });
    }
}
