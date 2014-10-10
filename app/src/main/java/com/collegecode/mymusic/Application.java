package com.collegecode.mymusic;

import com.parse.Parse;

/**
 * Created by saurabh on 14-09-28.
 */

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "0zU51Sxf2upo8a7poNrEaxM9iQCf35TQRCxiogkV", "n3jWn8kxYO8PiIoKtjFHHxjvbHyvLOuuB9Yvxa21");

    }
}
