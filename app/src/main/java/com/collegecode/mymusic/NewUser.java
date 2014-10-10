package com.collegecode.mymusic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;

/**
 * Created by saurabh on 14-10-04.
 */
public class NewUser extends Activity {
    private Context context;

    private interface OnComplete{
        public void onLoadComplete(Exception e);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newuser);
        this.context = this;
        (findViewById(R.id.btn_signin)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View t = view;
                view.setEnabled(false);
                ((Button) view).setText("Loading...");

                loadDatabase("Albums", new OnComplete() {
                    @Override
                    public void onLoadComplete(Exception e) {
                        if(e != null){
                            Toast.makeText(context, "Could not contact server! Please try again.", Toast.LENGTH_LONG).show();
                            t.setEnabled(true);
                            ((Button) t).setText("Let's Begin!");
                        }
                        else{
                            loadDatabase("Music", new OnComplete() {
                                @Override
                                public void onLoadComplete(Exception e) {
                                    if(e != null){
                                        Toast.makeText(context, "Could not contact server! Please try again.", Toast.LENGTH_LONG).show();
                                        t.setEnabled(true);
                                        ((Button) t).setText("Let's Begin!");
                                    }
                                    else{
                                        PreferenceManager.getDefaultSharedPreferences(context)
                                                .edit()
                                                .putBoolean("newUser" , false)
                                                .commit();
                                        startActivity(new Intent(context,Home.class));
                                        overridePendingTransition(R.anim.enter, R.anim.exit);
                                    }

                                }
                            });
                        }

                    }
                });

            }
        });
    }

    private void loadDatabase(final String table, final OnComplete onComplete){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(table);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> parseObjects, ParseException e) {
                if(e!=null){
                    onComplete.onLoadComplete(e);
                }
                else{
                    ParseObject.unpinAllInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e != null)
                                onComplete.onLoadComplete(e);
                            else{
                                ParseObject.pinAllInBackground(table, parseObjects, new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        onComplete.onLoadComplete(e);
                                    }
                                });
                            }

                        }
                    });
                }

            }
        });
    }
}
