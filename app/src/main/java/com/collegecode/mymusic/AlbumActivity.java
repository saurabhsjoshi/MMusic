package com.collegecode.mymusic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.AdapterView;
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
public class AlbumActivity extends BaseActivity{
    private Context context;
    public PlayBackService playBackService;

    private ListView listView;
    private ImageView album_art;
    private TextView txt_title;
    private TextView txt_artist;
    private View fab;

    private ArrayList<ParseObject> song_lst = null;

    public static final String EXTRA_IMAGE = "AlbumActivity:image";
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_album;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        getSupportActionBar().setTitle("Album");
        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_artist = (TextView) findViewById(R.id.txt_artist);
        listView = (ListView)findViewById(R.id.list);
        album_art = (ImageView) findViewById(R.id.img_albumArt);
        fab = findViewById(R.id.btn_playAll);

        playBackService = getPlayBackService();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Music");
        query.whereEqualTo("AlbumID", getIntent().getExtras().getInt("AlbumID"));
        query.orderByAscending("Title");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> lst_songs, ParseException e) {
                AlbumSongsListAdapter adapter = new AlbumSongsListAdapter(context,0,new ArrayList<ParseObject>(lst_songs));
                txt_artist.setText(lst_songs.size() + " songs");
                listView.setAdapter(adapter);
                song_lst = new ArrayList<ParseObject>(lst_songs);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        if(song_lst != null){
                            playBackService.setList(song_lst, i);
                            playBackService.resetPlayer();
                            playBackService.playSong();
                            playBackService.showNotification();
                        }
                    }
                });

                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(song_lst != null){
                            playBackService.setList(song_lst, 0);
                            playBackService.resetPlayer();
                            playBackService.playSong();
                            finish();
                        }
                    }
                });
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
        ViewCompat.setTransitionName(album_art, EXTRA_IMAGE);
    }

    public static void launch(BaseActivity activity, View transitionView, Number albumId) {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity, transitionView, EXTRA_IMAGE);
        Intent intent = new Intent(activity, AlbumActivity.class);
        intent.putExtra("AlbumID",albumId);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }
}
