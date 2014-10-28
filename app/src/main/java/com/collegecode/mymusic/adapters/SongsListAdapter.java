package com.collegecode.mymusic.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.collegecode.mymusic.PaymentActivity;
import com.collegecode.mymusic.R;
import com.collegecode.mymusic.objects.RoundedTransformation;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by saurabh on 14-10-10.
 */
public class SongsListAdapter extends ArrayAdapter<ParseObject> {

    public SongsListAdapter(Context context, int resource, ArrayList<ParseObject> objects) {
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
        ImageView img;
        TextView txt_title;
        TextView txt_album;
        Button btn_buy;
    }

    ViewHolder viewHolder;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_song,parent,false);

            viewHolder.img = (ImageView) convertView.findViewById(R.id.img_song_art);
            viewHolder.txt_title = (TextView) convertView.findViewById(R.id.txt_title);
            viewHolder.txt_album = (TextView) convertView.findViewById(R.id.txt_album);
            viewHolder.btn_buy = (Button) convertView.findViewById(R.id.btn_buy);
            convertView.setTag(viewHolder);
        }
        else
            viewHolder = (ViewHolder) convertView.getTag();

        viewHolder.txt_title.setText(getItem(position).getString("Title"));
        viewHolder.txt_album.setText(getItem(position).getString("Album"));

        viewHolder.btn_buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), PaymentActivity.class);
                i.putExtra(PaymentActivity.PAY_URL_KEY, "http://www.ccavenue.com");
                getContext().startActivity(i);
            }
        });

        Picasso.with(getContext())
                .load(getItem(position).getString("CoverArt"))
                .transform(new RoundedTransformation(16))
                .fit()
                .into(viewHolder.img);
        return convertView;
    }
}
