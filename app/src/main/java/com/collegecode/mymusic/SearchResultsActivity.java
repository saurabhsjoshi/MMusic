package com.collegecode.mymusic;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.collegecode.mymusic.adapters.SongsListAdapter;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;

/**
 * Created by saurabh on 14-11-02.
 */
public class SearchResultsActivity extends BaseActivity {
    private Context context;
    private boolean isRunning = false;
    private SearchAsync searchAsync;

    ListView listView;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_searchresults;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listView = (ListView) findViewById(R.id.list);
        context = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setFocusable(true);
        searchView.setIconified(false);
        searchView.requestFocusFromTouch();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(isRunning)
                    searchAsync.cancel(true);

                searchAsync = new SearchAsync();
                searchAsync.execute(s);
                isRunning = true;
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if(isRunning)
                    searchAsync.cancel(true);
                finish();
                return true;
            }
        });
        return true;
    }

    @Override
    protected void onPause() {
        if(isRunning)
            searchAsync.cancel(true);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(isRunning)
            searchAsync.cancel(true);
        super.onDestroy();
    }

    private class SearchAsync extends AsyncTask<String, Void, Void>{
        private boolean isCancelled = false;
        private ArrayList<ParseObject> songs = null;

        @Override
        protected Void doInBackground(String... strings) {
            try{
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Music");
                query.whereMatches("Title", "^.*("
                        + Character.toUpperCase(strings[0].charAt(0))
                        + "|" + Character.toLowerCase(Character.toUpperCase(strings[0].charAt(0)))
                        + ")" + strings[0].substring(1)
                        + ".*$");
                query.fromLocalDatastore();
                songs = new ArrayList<ParseObject>(query.find());
            }catch (Exception ignore){}

            if(isCancelled())
                isCancelled = true;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            final PlayBackService playBackService = getPlayBackService();
            super.onPostExecute(aVoid);
            if(!isCancelled && songs != null){
                listView.setAdapter(new SongsListAdapter(context,0, songs));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        try{
                            ParseObject temp = songs.get(i);
                            songs = new ArrayList<ParseObject>();
                            songs.add(temp);
                            playBackService.setList(songs,0);
                            playBackService.resetPlayer();
                            playBackService.playSong();
                            finish();
                            isRunning = false;
                        }catch (Exception e){e.printStackTrace();}
                    }
                });
            }
        }
    }
}