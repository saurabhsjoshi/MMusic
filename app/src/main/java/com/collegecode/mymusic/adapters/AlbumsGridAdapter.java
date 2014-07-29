package com.collegecode.mymusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.collegecode.mymusic.R;
import com.collegecode.mymusic.objects.Album;
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
            /*
            Bitmap src = BitmapFactory.decodeResource(context.getResources(), R.drawable.script);
            src = ThumbnailUtils.extractThumbnail(src, src.getWidth()/2, src.getHeight()/2, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            img.setImageBitmap(src);*/
        }

        ImageView img = (ImageView) view.findViewById(R.id.img_album);
        Picasso.with(context)
                .load("http://poponandon.com/wp-content/uploads/2013/04/onerepublic-native-review-2013.jpg")
                .resize(200,240)
                .into(img);

        return view;
    }
}
