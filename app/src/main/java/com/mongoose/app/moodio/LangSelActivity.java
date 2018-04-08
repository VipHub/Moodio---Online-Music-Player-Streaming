package com.mongoose.app.moodio;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.mongoose.app.moodio.adapters.CustomAdapter;
import com.mongoose.app.moodio.listener.ClickListener;
import com.mongoose.app.moodio.listener.RecyclerViewTouchListener;
import com.mongoose.app.moodio.utils.Conf;
import com.mongoose.app.moodio.utils.DatabaseHandler;
import com.mongoose.app.moodio.utils.NetworkHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LangSelActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "LANG_PREFS";
    public static final String PREFS_KEY = "LANG_PREFS_KEY";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Context context;
    private List<String> langList = Collections.EMPTY_LIST;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 971;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    private CoordinatorLayout rootLayout;
    private View view;
    private NetworkHandler nh;
    private DatabaseHandler db;
    boolean status = false,per1 = false,per2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Create an Intent that will start the Menu-Activity. */
        context = this;
        //updateViews();
        nh = new NetworkHandler(this);
        db = new DatabaseHandler(this);
        getPermission();
        next();
    }

    private void getPermission() {

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_PHONE_STATE)) {

                getPermission2();
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
    public void getPermission2() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    System.out.print("GRanted");
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    getPermission2();

                } else {

                    System.out.print("Not GTamted");
                    System.exit(0);
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    System.out.print("GRanted");
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    System.out.print("Not GTamted");
                    System.exit(0);
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void next(){
        if(nh.isOnline()) {
            if (getLang().equals("notSet")) {
                setUi();
                requestDataFromServer("language.html");
            } else {
                Intent mainIntent = new Intent(LangSelActivity.this, MainActivity.class);
                mainIntent.putExtra("STATUS", "ONLINE");
                startActivity(mainIntent);
                finish();
            }
        }
        else {
            showNetworkOfflinePopup();
        }
    }
    private void showNetworkOfflinePopup() {

        Button btn_set, btn_off,  btn_retry, btn_exit;
        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.popup_network);
        d.setTitle("No Connection..!");
        d.setCancelable(false);
        btn_set = (Button) d.findViewById(R.id.btn_set);
        //btn_off = (Button) d.findViewById(R.id.btn_off);
        btn_retry = (Button) d.findViewById(R.id.btn_retry);
        btn_exit = (Button) d.findViewById(R.id.btn_exit);

        btn_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
                startActivity(intent);
            }
        });
        /*btn_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(LangSelActivity.this, MainActivity.class);
                mainIntent.putExtra("STATUS","OFFLINE");
                startActivity(mainIntent);
                finish();
            }
        });*/
        btn_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next();
            }
        });
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });

        d.show();
    }


    public void saveLang(Context context, String text) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE); //1
        editor = settings.edit(); //2
        editor.putString(PREFS_KEY, text); //3
        editor.commit(); //4
    }
    private String getLang() {
        SharedPreferences sharedPref = this.getSharedPreferences("LANG_PREFS",Context.MODE_PRIVATE);
        String lang = sharedPref.getString("LANG_PREFS_KEY","notSet");
        return lang;
    }
    private void updateViews() {
        //adapter = new SearchAdapter(this, songsList);
        //recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);
        CustomAdapter adapter = new CustomAdapter(this, langList);
        recyclerView.setAdapter(adapter);
        //progressBar.setVisibility(View.GONE);
    }
    private void requestDataFromServer(String query) {
            progressBar.setVisibility(View.VISIBLE);

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Conf.API_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WebService api = retrofit.create(WebService.class);
        Call<ResponseBody> call = api.getLangFromServer(query);
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
                langList = gson.fromJson(jsoncode, new TypeToken<List<String>>(){}.getType());;
                view.setVisibility(View.VISIBLE);
                db.addLang(langList);
                updateViews();
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Snackbar.make(recyclerView, "" + t, Snackbar.LENGTH_LONG)
                        .setAction("Try Again", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                requestDataFromServer("language.html");
                            }
                        })
                        .show();
            }
        });
    }
    private void setUi() {
        setContentView(R.layout.activity_langsel);
        rootLayout = (CoordinatorLayout) findViewById(R.id.rootLayout);
        view = findViewById(R.id.lang_info);
        view.setVisibility(View.GONE);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setVisibility(View.INVISIBLE);
        recyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(this, recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                progressBar.setVisibility(View.VISIBLE);
                System.out.println(langList.get(position));
                saveLang(context,langList.get(position));
                Intent mainIntent = new Intent(LangSelActivity.this, MainActivity.class);
                mainIntent.putExtra("STATUS","ONLINE");
                startActivity(mainIntent);
                finish();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }
}
