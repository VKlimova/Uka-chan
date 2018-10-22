package com.amargodigits.uka_chan.data;
// Working with local SQLite database
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.*;


public class SongDbHelper extends SQLiteOpenHelper {
    public static String TAG = "uka_chan_tag";
    private static final String DATABASE_NAME = "songs.db";
    // If you change the database schema, you must increment the database version
    private static final int DATABASE_VERSION = 1;

    public SongDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_SONG_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_SONG_ID + " TEXT NOT NULL, "
                + COLUMN_TITLE + " TEXT NOT NULL, "
                + COLUMN_SINGER + " TEXT, "
                + COLUMN_IMG + " TEXT, "
                + COLUMN_TEXT + " TEXT, "
                + COLUMN_LANGUAGE + " TEXT, "
                + COLUMN_UPDATE_TIMESTAMP + " TEXT, "
                + COLUMN_VIEW_TIMESTAMP + " TEXT, "
                + COLUMN_LINK + " TEXT, "
                + COLUMN_LIKE + " TEXT "
                + "); ";
        sqLiteDatabase.execSQL(SQL_CREATE_SONG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
