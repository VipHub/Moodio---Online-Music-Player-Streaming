package com.mongoose.app.moodio;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mongoose.app.moodio.utils.DatabaseHandler;
import com.mongoose.app.moodio.utils.MusicService;

import java.util.List;



public class SettingsActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setToolbar();
        getFragmentManager().beginTransaction()
                .replace(R.id.setting_container,new SettingFragment()).commit();
    }
    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
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
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicService.forgroundStatus = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public static class SettingFragment extends PreferenceFragment {
        Preference preference,preference_rate;
        ListPreference listPreference;
        public DatabaseHandler db;
        private List<String> langList;
        @Override
        public void onCreate(Bundle SavedInstanceState){
            super.onCreate(SavedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            db = new DatabaseHandler(getActivity());

            preference = findPreference("recent");
            preference_rate = findPreference("rate");
            listPreference = (ListPreference) findPreference("language");
            listPreference.setSummary(getLang());
            langList = db.getLangList();
            CharSequence[] entries = langList.toArray(new CharSequence[langList.size()]);
            listPreference.setEntries(entries);
            listPreference.setEntryValues(entries);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    db.deleteRecentTable();
                                    Toast.makeText(getActivity(), "Recent Player Queue is Cleared", Toast.LENGTH_LONG).show();
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                    return false;
                }
            });
            listPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //setListPreferencedata(listPreference);
                    return false;
                }
            });
            listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    saveLang(getActivity(),(String) newValue);
                    preference.setSummary((String) newValue);
                    MainActivity.FirstTime = true;
                    Toast.makeText(getActivity(),"Language Changed to "+newValue,Toast.LENGTH_LONG).show();
                    return false;
                }
            });
            preference_rate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName()); // missing 'http://' will cause crashed
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    //Toast.makeText(getActivity(),"Rate Preference Clicked",Toast.LENGTH_LONG).show();
                    return false;
                }
            });
        }
        public void saveLang(Context context, String text) {
            SharedPreferences settings;
            SharedPreferences.Editor editor;
            settings = context.getSharedPreferences("LANG_PREFS", Context.MODE_PRIVATE); //1
            editor = settings.edit(); //2
            editor.putString("LANG_PREFS_KEY", text); //3
            editor.commit(); //4
        }
        /*private void requestDataFromServer(String query) {

            final Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Conf.SERVER_LANG)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            WebService api = retrofit.create(WebService.class);
            Call<List<String>> call = api.getLangFromServer(query);
            call.enqueue(new Callback<List<String>>() {
                @Override
                public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                    //Try to get response body
                    langList = response.body();
                    CharSequence[] entries = langList.toArray(new CharSequence[langList.size()]);
                    listPreference.setEntries(entries);
                    listPreference.setEntryValues(entries);
                }

                @Override
                public void onFailure(Call<List<String>> call, Throwable t) {

                }
            });
        }*/
        private String getLang() {
            SharedPreferences sharedPref = getActivity().getSharedPreferences("LANG_PREFS",Context.MODE_PRIVATE);
            String lang = sharedPref.getString("LANG_PREFS_KEY","notSet");
            return lang;
        }
        private void setListPreferencedata(ListPreference lp) {
            langList = db.getLangList();
            CharSequence[] entries = (CharSequence[]) langList.toArray();
            lp.setEntries(entries);
            lp.setEntryValues(entries);
        }
    }
}