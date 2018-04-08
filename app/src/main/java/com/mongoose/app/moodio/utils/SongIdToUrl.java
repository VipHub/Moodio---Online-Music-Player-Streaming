package com.mongoose.app.moodio.utils;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;


/**
 * Created by Vips on 4/4/2016.
 */
public class SongIdToUrl extends AsyncTask<String, Void, String> {
    String temp;

   public SongIdToUrl(String passid) {
        this.temp = passid;
    }
    //Function to get url from song Id
        //Thread to handle http connection

    @Override
    protected String doInBackground(String... params) {

        String passurl="";
        try {
            //String url = "";
            URL url = new URL("http://default-environment.in6s46emga.us-west-2.elasticbeanstalk.com/"+temp);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

                passurl = br.readLine();

            //temp = passurl;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //System.out.println(passurl);
        return passurl;
    }
    protected void onPostExecute(String passurl) {
    }
}
