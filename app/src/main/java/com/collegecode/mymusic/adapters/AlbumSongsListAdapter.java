package com.collegecode.mymusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.collegecode.mymusic.R;
import com.parse.ParseObject;

import java.util.ArrayList;

/**
 * Created by saurabh on 14-10-24.
 */
public class AlbumSongsListAdapter extends ArrayAdapter<ParseObject> {

    public AlbumSongsListAdapter(Context context, int resource, ArrayList<ParseObject> objects) {
        super(context, resource, objects);
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public ParseObject getItem(int position) {
        return super.getItem(position);
    }

    private static class ViewHolder{
        TextView txt_title;
        TextView txt_album;
    }

    ViewHolder viewHolder;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_album_song,parent,false);
            viewHolder.txt_title = (TextView) convertView.findViewById(R.id.txt_title);
            viewHolder.txt_album = (TextView) convertView.findViewById(R.id.txt_album);
            convertView.setTag(viewHolder);
        }
        else
            viewHolder = (ViewHolder) convertView.getTag();

        viewHolder.txt_title.setText(getItem(position).getString("Title"));
        viewHolder.txt_album.setText(getItem(position).getString("Artist"));
        return convertView;
    }
}
