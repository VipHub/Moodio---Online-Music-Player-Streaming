package com.mongoose.app.moodio;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mongoose.app.moodio.fragments.MainSearchFragment;

import java.security.PrivateKey;

public class SearchActivityTest extends AppCompatActivity {
    private FragmentManager fm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_test);
        fm = getSupportFragmentManager();
        Fragment frag = new MainSearchFragment();
        fm.beginTransaction().add(R.id.container, frag).commit();
    }
}
