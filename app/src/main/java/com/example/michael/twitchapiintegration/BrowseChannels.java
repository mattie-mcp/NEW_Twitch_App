package com.example.michael.twitchapiintegration;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.test.espresso.core.deps.guava.collect.Iterables;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.text.method.ScrollingMovementMethod;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;

public class BrowseChannels extends AppCompatActivity {
    String channelId;
    String status;
    String profileImageUrl;
    String bannerImageUrl;
    String followers;
    String views;
    TextView followersView;
    TextView viewerText;
    TextView titleBanner;
    private TextView chat;
    ImageView channelBanner;
    ImageView profilePic;
    private IRCConnection ircConnection;
    private EditText chatMessage;
    private WebView stream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_channels);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        followersView = (TextView) findViewById(R.id.followerText);      //How many followers channel has
        viewerText = (TextView) findViewById(R.id.viewerText);        //How many viewers are watching stream
        channelBanner = (ImageView) findViewById(R.id.channelBanner);   //Channel logo
        chat = (TextView)findViewById(R.id.chatReadout);
        chat.setMovementMethod(new ScrollingMovementMethod());
        channelId = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        chatMessage = (EditText) findViewById(R.id.insertChat);
        stream = (WebView) findViewById(R.id.webStream);

        RetrieveStreams t = new RetrieveStreams();
        chatMessage.setMovementMethod(new ScrollingMovementMethod());
        chat.setMovementMethod(new ScrollingMovementMethod());
        t.execute(channelId);

        try {
            ircConnection = new IRCConnection("irc.chat.twitch.tv",6667,channelId.toLowerCase(),chat);
            ircConnection.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        getSupportActionBar().setTitle(channelId);
        String myStream= "http://player.twitch.tv/?channel=" + channelId;
//        String myStream= "https://www.twitch.tv/riotgamesbrazil";
        stream.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = stream.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        stream.loadUrl(myStream);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
                URL url = new URL("https://api.twitch.tv/kraken/channels/" + channelId); //url for the API
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
                //profileImageUrl = streams.getString("logo");
                bannerImageUrl = streams.getString("profile_banner");
                status = streams.getString("status");
                followers = streams.getString("followers");
                views = streams.getString("views");

                //titleBanner.setText(channelId);
                viewerText.setText(views);
                followersView.setText(followers);

                try{

                    new DownloadImageTask((ImageView) findViewById(R.id.channelBanner))
                            .execute(bannerImageUrl);

//                    new DownloadImageTask((ImageView) findViewById(R.id.profilePic))
//                            .execute(profileImageUrl);
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

    public class RetrieveStreamVideo extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                //this is the section that grabs the JSON array from the link and makes it into a string to use later
                if (channelId.contains(" ")){
                    channelId = channelId.replace(" ", "%20");
                }
                URL url = new URL("http://player.twitch.tv/?channel=" + channelId + "/access_token");//url for the API
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
                profileImageUrl = streams.getString("logo");
                bannerImageUrl = streams.getString("profile_banner");
                status = streams.getString("status");
                followers = streams.getString("followers");
                views = streams.getString("views");

                //titleBanner.setText(channelId);
                viewerText.setText(views);
                followersView.setText(followers);

                try{
                    new DownloadImageTask((ImageView) findViewById(R.id.channelBanner))
                            .execute(bannerImageUrl);

//                    new DownloadImageTask((ImageView) findViewById(R.id.profilePic))
//                            .execute(profileImageUrl);
                }
                catch(Exception ex){

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
                InputStream in = new java.net.URL(urldisplay).openStream();
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

    public void sendChat(View view){
        ircConnection.sendMessage(chatMessage.getText().toString());
        this.chatMessage.setText("");
    }
}
