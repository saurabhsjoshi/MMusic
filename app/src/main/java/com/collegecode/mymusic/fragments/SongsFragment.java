package com.collegecode.mymusic.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.collegecode.mymusic.BaseActivity;
import com.collegecode.mymusic.Home;
import com.collegecode.mymusic.PlayBackService;
import com.collegecode.mymusic.R;
import com.collegecode.mymusic.adapters.SongsListAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by saurabh on 14-09-27.
 */
public class SongsFragment extends Fragment {

    private PlayBackService playBackService;
    private ArrayList<ParseObject> songs;
    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songs,container, false);
        listView = (ListView) view.findViewById(R.id.list);
        updateUI();
        return view;
    }

    public void updateUI(){
        playBackService = BaseActivity.getPlayBackService();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Music");
        query.orderByAscending("Title");
        query.fromLocalDatastore();

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> lst_songs, ParseException e) {
                songs = new ArrayList<ParseObject>(lst_songs);
                listView.setAdapter(new SongsListAdapter(getActivity(),0, songs));
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try{
                    if(playBackService == null)
                        playBackService = ((Home) getActivity()).playBackService;
                    playBackService.setList(songs,i);
                    playBackService.resetPlayer();
                    playBackService.playSong();
                    ((Home)getActivity()).updateSmallPlayer();
                }catch (Exception e){e.printStackTrace();}
            }
        });
    }
}
