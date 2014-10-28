package com.collegecode.mymusic.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.collegecode.mymusic.AlbumActivity;
import com.collegecode.mymusic.Home;
import com.collegecode.mymusic.R;
import com.collegecode.mymusic.adapters.AlbumsGridAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by saurabh on 7/28/14.
 */
public class AlbumsFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums,container, false);
        final GridView gridview = (GridView) view.findViewById(R.id.gridview);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Albums");
        query.orderByAscending("AlbumID");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> lst_albums, ParseException e) {
                ArrayList<ParseObject> albums = new ArrayList<ParseObject>(lst_albums);
                gridview.setAdapter(new AlbumsGridAdapter(getActivity(), albums));
            }
        });

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                AlbumActivity.launch((Home)getActivity(),
                        view.findViewById(R.id.img_album),
                        ((ParseObject)gridview.getItemAtPosition(i)).getNumber("AlbumID"));
            }
        });

        return view;
    }
}
