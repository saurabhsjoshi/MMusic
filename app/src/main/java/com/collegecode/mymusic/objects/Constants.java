package com.collegecode.mymusic.objects;

/**
 * Created by saurabh on 14-10-13.
 */
public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = "com.collegecode.mmusic.action.main";
        public static String PREV_ACTION = "com.collegecode.mmusic.action.prev";
        public static String PLAY_ACTION = "com.collegecode.mmusic.action.play";
        public static String NEXT_ACTION = "com.collegecode.mmusic.action.next";
        public static String STARTFOREGROUND_ACTION = "com.collegecode.mmusic.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.collegecode.mmusic.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
