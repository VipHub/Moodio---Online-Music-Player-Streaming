package com.mongoose.app.moodio.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mongoose.app.moodio.R;
import com.mongoose.app.moodio.listener.SongClickListener;

/**
 * Created by Vips on 7/30/2017.
 */

public class SearchSongsFragment extends Fragment implements MainSearchFragment.FragmentRefreshListener{
    public SearchSongsFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_music, container, false);
        return layout;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //musicRetriever = new MusicRetriever(getActivity());
        //songsList = musicRetriever.prepare();
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void refreshFragment() {

        //updateView();
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
