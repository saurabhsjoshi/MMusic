package com.collegecode.mymusic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.collegecode.mymusic.adapters.AlbumSongsListAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by saurabh on 14-10-17.
 */
public class AlbumActivity extends ActionBarActivity {
    private Context context;
    public PlayBackService playBackService;
    private Intent playIntent;

    private ListView listView;
    private ImageView album_art;
    private TextView txt_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        context = this;

        txt_title = (TextView) findViewById(R.id.txt_title);
        listView = (ListView)findViewById(R.id.list);
        album_art = (ImageView) findViewById(R.id.img_albumArt);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Music");
        query.whereEqualTo("AlbumID", getIntent().getExtras().getInt("AlbumID"));
        query.orderByAscending("Title");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> lst_songs, ParseException e) {
                AlbumSongsListAdapter adapter = new AlbumSongsListAdapter(context,0,new ArrayList<ParseObject>(lst_songs));
                listView.setAdapter(adapter);
            }
        });

        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Albums");
        query1.whereEqualTo("AlbumID", getIntent().getExtras().getInt("AlbumID"));
        query1.fromLocalDatastore();
        query1.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> lst_songs, ParseException e) {
                if(e == null)
                {
                    if(lst_songs.size() !=0){
                        Picasso.with(context)
                                .load(lst_songs.get(0).getString("AlbumArt"))
                                .fit()
                                .into(album_art);
                        txt_title.setText(lst_songs.get(0).getString("Title"));
                    }

                }
                else
                    e.printStackTrace();
            }
        });
    }
}
