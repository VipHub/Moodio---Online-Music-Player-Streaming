package com.mongoose.app.moodio.fragments;

/**
 * Created by Vips on 7/30/2017.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mongoose.app.moodio.R;
import com.mongoose.app.moodio.SearchActivity;
import com.mongoose.app.moodio.listener.DrawerToggleListener;
import com.mongoose.app.moodio.listener.SongClickListener;
import com.mongoose.app.moodio.model.Songs;
import com.mongoose.app.moodio.utils.DatabaseHandler;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainSearchFragment extends Fragment {

    private List<Songs> songsList = new ArrayList<>();
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DrawerToggleListener drawerToggleListener;
    private SongClickListener songClickListener;
    private DatabaseHandler db;

    public MainSearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_main_search, container, false);
        setHasOptionsMenu(true);
        //setToolbar(layout);
        setViewPager(layout);
        db = new DatabaseHandler(getActivity());
        return layout;
    }

    /*private void setToolbar(View layout) {
        toolbar = (Toolbar) layout.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            //((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.title_activity_songs));
            ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_action_icon);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        drawerToggleListener.setNavigationView(toolbar);
    }*/

    private void setViewPager(View layout) {
        viewPager = (ViewPager) layout.findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyFragmentPagerAdapter(getChildFragmentManager(), viewPager));
        tabLayout = (TabLayout) layout.findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_search) {
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            intent.putExtra("CONTEXT", "LibraryFragment");
            startActivityForResult(intent,1);
        }
        return true;
    }


    public class MyFragmentPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {

        private String tabTitles[] = new String[]{"Songs", "Playlist"};
        private ViewPager mViewPager;

        public MyFragmentPagerAdapter(FragmentManager fm, ViewPager pager) {
            super(fm);
            this.mViewPager = pager;
            mViewPager.addOnPageChangeListener(this);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment frag = null;
            switch (position) {
                case 0:
                    frag = new SearchSongsFragment();
                    break;
                case 1:
                    frag = new SearchPlaylistFragment();
                    break;
            }
            return frag;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            FragmentRefreshListener frag = (FragmentRefreshListener) this.instantiateItem(mViewPager, position);
            frag.refreshFragment();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    public interface FragmentRefreshListener {
        void refreshFragment();
    }
}
