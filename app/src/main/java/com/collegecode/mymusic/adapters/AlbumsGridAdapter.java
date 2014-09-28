package com.collegecode.mymusic.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.collegecode.mymusic.R;
import com.collegecode.mymusic.objects.Album;
import com.collegecode.mymusic.objects.RoundedTransformation;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
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

    private static class ViewHolder{
        ImageView img;
        TextView txt_title;
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
        return i;
    }

    ViewHolder viewHolder;

    @Override
    public View getView(int i, View converView, ViewGroup viewGroup) {

        if(converView == null){
            viewHolder = new ViewHolder();

            LayoutInflater layoutInflater = LayoutInflater.from(context);
            converView = layoutInflater.inflate(R.layout.item_album,viewGroup,false);

            viewHolder.img = (ImageView) converView.findViewById(R.id.img_album);
            viewHolder.txt_title = (TextView) converView.findViewById(R.id.lbl_album);

            converView.setTag(viewHolder);
        }

        else
            viewHolder = (ViewHolder) converView.getTag();

        viewHolder.txt_title.setText(albums.get(i).title);

        new onPostLoad(viewHolder).loadImage(albums.get(i).album_art);

        return converView;
    }

    private class onPostLoad{
        private final WeakReference<ViewHolder> viewHolderReference;

        public onPostLoad(ViewHolder viewHolder_weak){
            viewHolderReference = new WeakReference<ViewHolder>(viewHolder_weak);
        }

        public void loadImage(String album_url){
            Picasso.with(context)
                    .load(album_url)
                    .transform(new RoundedTransformation(16))
                    .fit()
                    .into(viewHolderReference.get().img, new Callback() {
                        @Override
                        public void onSuccess() {

                            Bitmap source = ((BitmapDrawable) viewHolderReference.get().img.getDrawable()).getBitmap();
                            Palette.generateAsync(source, 24, new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    try{
                                        viewHolderReference.get().txt_title.setBackgroundColor(palette.getDarkVibrantColor().getRgb());
                                        viewHolderReference.get().txt_title.setTextColor(palette.getLightVibrantColor().getRgb());

                                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                                            viewHolderReference.get().txt_title.setAlpha(0.7f);

                                    }catch (Exception ignore){
                                        viewHolderReference.get().txt_title.setBackgroundColor(Color.parseColor("#80000000"));
                                        viewHolderReference.get().txt_title.setTextColor(Color.WHITE);

                                    }
                                }
                            });
                        }
                        @Override
                        public void onError() {}});
        }

    }
}
