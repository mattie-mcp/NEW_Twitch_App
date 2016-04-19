package com.example.michael.twitchapiintegration;

import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.*;
import android.widget.TextView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.*;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText sBox;
    private Button sButton;
    private ArrayList<String> channels;
    private ArrayList<String> previews;
    private ArrayList<String> gameTitles;
    private TextView output;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter  mAdapter;
    private RecyclerView.LayoutManager  mLayoutManager;
    private FloatingActionButton tinyButton;
    private View currentLayout;
    private LinearLayout cardLayout;
    public final static String EXTRA_MESSAGE = "com.example.michael.twitchapiintegration.MESSAGE";
    private ListView mDrawerList;
    private ArrayAdapter<String> arrayAdapter;
    private Toolbar toolbar;
    private DrawerLayout navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //instantiate all of the screen content
        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        sButton = (Button) findViewById(R.id.button);
        sBox = (EditText) findViewById(R.id.editText);
        tinyButton = (FloatingActionButton) findViewById(R.id.fab);
        mDrawerList = (ListView)findViewById(R.id.left_drawer);
        tinyButton.setVisibility(View.INVISIBLE);
        channels = new ArrayList<>();
        previews = new ArrayList<>();
        gameTitles = new ArrayList<>();
        currentLayout = findViewById(R.id.main);
        //Recycler View stuff
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SimpleAdapter(channels,previews, gameTitles);
        mRecyclerView.setAdapter(mAdapter);

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        //handles when sButton is clicked
        sButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                channels.clear();
                previews.clear();
                gameTitles.clear();
                String w = sBox.getText().toString();
                StreamInformation t = new StreamInformation();
                t.execute(w);
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickMenu(position);
            }
        });
    }

    private void clickMenu(int position){
        if (position == 0){
            Intent intent = new Intent(this, HomeScreen.class);
            navigation.closeDrawer(Gravity.LEFT);
            startActivity(intent);
        }
    }

    private void addDrawerItems() {
        String[] osArray = { "Home", "Search Steams"};
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(arrayAdapter);
    }

    //this handles everything that has to do with the API
    public class StreamInformation extends AsyncTask<String, String, String> {
        String input = sBox.getText().toString(); //grabs the input for the search
        HttpURLConnection urlConnection;

        @Override
        protected String doInBackground(String... args) {

            StringBuilder result = new StringBuilder();
            try {
            //this is the section that grabs the JSON array from the link and makes it into a string to use later
                if (input.contains(" ")){
                    input = input.replace(" ", "%20");
                }
                URL url = new URL("https://api.twitch.tv/kraken/search/streams?limit=100&q=" + input); //url for the API
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
                streams = json.getJSONArray("streams"); //grabs the "streams" array from the main JSON object
                for(int i=0; i < streams.length(); i++) { //itterates through JSON objects in streams
                    String name = streams.getJSONObject(i).getJSONObject("channel").getString("display_name");//grabs the displayname of the streamers
                    String game = streams.getJSONObject(i).getJSONObject("channel").getString("game");
                    previews.add(streams.getJSONObject(i).getJSONObject("preview").getString("large"));
                    channels.add(name);
                    gameTitles.add(game);
                }
                mAdapter.notifyDataSetChanged();
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            } // catch (JSONException e)
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void selectStream(View v){
        Intent intent = new Intent(this, BrowseChannels.class);
        TextView editText = (TextView) findViewById(R.id.title);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}
