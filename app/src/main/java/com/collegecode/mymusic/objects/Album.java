package com.collegecode.mymusic.objects;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by saurabh on 7/28/14.
 */
public class Album {
    public String title;
    public String album_art;
    public String artist;
    public String songsJSON;


    public ArrayList<Song> getSongs(){
        ArrayList<Song> songs = new ArrayList<Song>();
        try
        {
            JSONArray jsonArray = new JSONArray(songsJSON);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return songs;
    }
}
