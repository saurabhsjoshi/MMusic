package com.collegecode.mymusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.collegecode.mymusic.R;
import com.collegecode.mymusic.objects.Album;
import com.collegecode.mymusic.objects.RoundedTransformation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by saurabh on 7/28/14.
 */
public class AlbumsGridAdapter extends BaseAdapter {
    private ArrayList<Album> albums;
    private Context context;

    public AlbumsGridAdapter(Context context, ArrayList<Album> albums){
        this.albums = albums;
        this.context = context;
    }

    @Override
    public int getCount() {
        return albums.size();
    }

    @Override
    public Object getItem(int i) {
        return albums.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        if(view == null){
            view = layoutInflater.inflate(R.layout.item_album,viewGroup,false);
        }

        ImageView img = (ImageView) view.findViewById(R.id.img_album);
        TextView txt_view = (TextView) view.findViewById(R.id.lbl_album);

        txt_view.setText(albums.get(i).title);
        Picasso.with(context)
                .load(albums.get(i).album_art)
                .transform(new RoundedTransformation(16,0))
                .fit()
                .into(img);

        return view;
    }
}
