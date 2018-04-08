package com.mongoose.app.moodio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.mongoose.app.moodio.comp.MyTypeface;
import com.mongoose.app.moodio.utils.MusicService;
import com.mongoose.app.moodio.utils.WebViewActivity;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private Button btn_website,btn_contact,btn_tc,btn_pp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setToolbar();

        MyTypeface mft = new MyTypeface(this);

        btn_website = (Button)findViewById(R.id.btn_website);
        btn_contact = (Button)findViewById(R.id.btn_contact);
        btn_tc = (Button)findViewById(R.id.btn_tc);
        btn_pp = (Button)findViewById(R.id.btn_pp);

        btn_website.setOnClickListener(this);
        btn_contact.setOnClickListener(this);
        btn_tc.setOnClickListener(this);
        btn_pp.setOnClickListener(this);

        /*btn_website.setTypeface(mft.getTypeface());
        btn_contact.setTypeface(mft.getTypeface());
        btn_tc.setTypeface(mft.getTypeface());
        btn_pp.setTypeface(mft.getTypeface());*/
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
    public void onClick(View v) {
        int id = v.getId();
        System.out.print(id);
        switch (id) {
            case R.id.btn_website:
                //Uri uri = Uri.parse(""); // missing 'http://' will cause crashed
                //Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                //startActivity(intent);
                break;
            case R.id.btn_contact:
                Intent intentcu = new Intent(this, WebViewActivity.class);
                intentcu.putExtra("TITLE","Contact Us");
                intentcu.putExtra("FILENAME","cu.html");
                overridePendingTransition(R.anim.fadein,R.anim.fadeout);
                startActivity(intentcu);
                break;
            case R.id.btn_tc:
                Intent intenttc = new Intent(this, WebViewActivity.class);
                intenttc.putExtra("TITLE","Terms & Conditions");
                intenttc.putExtra("FILENAME","terms.html");
                overridePendingTransition(R.anim.fadein,R.anim.fadeout);
                startActivity(intenttc);
                break;
            case R.id.btn_pp:
                Intent intentpp = new Intent(this, WebViewActivity.class);
                intentpp.putExtra("TITLE","Privacy Policy");
                intentpp.putExtra("FILENAME","pp.html");
                overridePendingTransition(R.anim.fadein,R.anim.fadeout);
                startActivity(intentpp);
                break;

        }
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
}
