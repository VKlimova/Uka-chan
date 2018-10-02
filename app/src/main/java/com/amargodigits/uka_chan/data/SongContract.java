package com.amargodigits.uka_chan.data;

// Working with local SQLite database

import android.provider.BaseColumns;

public  class SongContract {
    public static final class SongEntry implements BaseColumns {
        public static final String TABLE_NAME="songs";
        public static final String COLUMN_SONG_ID = "songId";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SINGER = "singer";
        public static final String COLUMN_IMG = "img";
        public static final String COLUMN_TEXT = "text";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_UPDATE_TIMESTAMP = "update_timestamp";
        public static final String COLUMN_VIEW_TIMESTAMP =  "view_timestamp";
        public static final String COLUMN_LINK = "link";
    }
}
