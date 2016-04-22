package com.example.michael.twitchapiintegration;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BrowseTeams extends AppCompatActivity {
    String channelId;
    String bannerImageUrl;
    TextView followersView;
    TextView viewerText;
    ImageView channelBanner;
    private TextView description;
    private TextView dateCreated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_teams);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        channelBanner = (ImageView) findViewById(R.id.channelBanner);   //Channel logo
        channelId = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        description = (TextView) findViewById(R.id.description);
        dateCreated = (TextView) findViewById(R.id.dateCreated);
        RetrieveStreams t = new RetrieveStreams();
        t.execute(channelId);
        getSupportActionBar().setTitle(channelId);
    }

    public class RetrieveStreams extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                //this is the section that grabs the JSON array from the link and makes it into a string to use later
                if (channelId.contains(" ")){
                    channelId = channelId.replace(" ", "%20");
                }
                URL url = new URL("https://api.twitch.tv/kraken/teams/" + channelId); //url for the API
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            }catch( Exception e) {
                e.printStackTrace();
            }
            finally {
//                urlConnection.disconnect();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            //findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            super.onPostExecute(s);
            try {
                JSONObject streams = new JSONObject(s);
                bannerImageUrl = streams.getString("background");
                description.setText(streams.getString("info"));
                dateCreated.setText(streams.getString("created_at"));
                try{

                    new DownloadImageTask((ImageView) findViewById(R.id.channelBanner))
                            .execute(bannerImageUrl);
                }
                catch(Exception ex){
                    String k = ex.toString();
                }


            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            }
            //findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
