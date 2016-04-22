package com.example.michael.twitchapiintegration;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.*;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.*;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class HomeScreen extends AppCompatActivity {

    private ListView mDrawerList;
    private DrawerLayout navigation;
    private ArrayAdapter<String> arrayAdapter;
    private TextView topGames_control;
    public final static int EXTRA_MESSAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerList = (ListView)findViewById(R.id.left_drawer);
        toolbar.setTitle("Twitch API Integration");

        navigation = (DrawerLayout) findViewById(R.id.navigation_drawer);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,  navigation, toolbar,
                R.string.drawer_open, R.string.drawer_close
        );
        navigation.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();
        addDrawerItems();
        ImageView image = (ImageView) findViewById(R.id.homeIcon);
        image.setImageResource(R.drawable.twitch_home);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickMenu(position);
            }
        });
        topGames_control = (TextView) findViewById(R.id.top_games);
        TopGames t = new TopGames();
        t.execute();
        //topGames.setText();
    }

    private void clickMenu(int position){
         if (position == 1){
             navigation.closeDrawer(Gravity.LEFT);
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("EXTRA_MESSAGE", (position - 1));
            startActivity(intent);
        }
        else if (position == 2){
             navigation.closeDrawer(Gravity.LEFT);
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("EXTRA_MESSAGE", (position - 1));
            startActivity(intent);
        }
    }

    private void addDrawerItems() {
        String[] osArray = { "Home", "Search Streams", "Search Teams"};
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(arrayAdapter);
    }

    public class TopGames extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;

        @Override
        protected String doInBackground(String... args) {

            StringBuilder result = new StringBuilder();
            try {
                //this is the section that grabs the JSON array from the link and makes it into a string to use later

                URL url = new URL("https://api.twitch.tv/kraken/games/top"); //url for the API
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
                urlConnection.disconnect();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String s) {

            //findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            super.onPostExecute(s);
            try {
                JSONObject json = new JSONObject(s);
                JSONArray streams= new JSONArray();
                streams = json.getJSONArray("top"); //grabs the "streams" array from the main JSON object
                String setControlText = "";
                for(int i=0; i < streams.length(); i++) { //itterates through JSON objects in streams
                    String name = streams.getJSONObject(i).getJSONObject("game").getString("name");//grabs the displayname of the streamers
                    //currentGames.add(name);
                    setControlText += Integer.toString(i+1) + ". " + name + "\n";

                }
                topGames_control.setText(setControlText);
                //findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            } // catch (JSONException e)
        }
    }

}
