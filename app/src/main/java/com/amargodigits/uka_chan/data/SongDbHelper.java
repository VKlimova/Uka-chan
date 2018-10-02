package com.amargodigits.uka_chan.data;
// Working with local SQLite database
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.amargodigits.uka_chan.model.Song;

import static com.amargodigits.uka_chan.MainActivity.songList;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.*;
import static com.amargodigits.uka_chan.data.SongContract.*;


public class SongDbHelper extends SQLiteOpenHelper {
    public static String TAG = "uka_chan_tag";
    // The database name
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
                + COLUMN_TEXT + " TEXT NOT NULL, "
                + COLUMN_LANGUAGE + " TEXT NOT NULL, "
                + COLUMN_UPDATE_TIMESTAMP + " TEXT, "
                + COLUMN_VIEW_TIMESTAMP + " TEXT, "
                + COLUMN_LINK + " TEXT"
                + "); ";
        sqLiteDatabase.execSQL(SQL_CREATE_SONG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    /**
     * Fills in the movieList[] array, and returns the length of the array
     */
    public static void makeSongsArrayFromSQLite(SQLiteDatabase sqLiteDatabase) {
// This projection  specifies which columns from the database
// we will actually use in this query.
        String[] projection = {
                SongEntry.COLUMN_SONG_ID,
                SongEntry.COLUMN_TITLE,
                SongEntry.COLUMN_SINGER,
                SongEntry.COLUMN_IMG,
                SongEntry.COLUMN_TEXT,
                SongEntry.COLUMN_LANGUAGE,
                SongEntry.COLUMN_UPDATE_TIMESTAMP,
                SongEntry.COLUMN_VIEW_TIMESTAMP,
                SongEntry.COLUMN_LINK
        };

        String sortOrder = SongEntry.COLUMN_VIEW_TIMESTAMP + " DESC";
        int i = 0;
        Cursor cursor = sqLiteDatabase.query(SongEntry.TABLE_NAME, projection, null, null, null, null, sortOrder);
            while (cursor.moveToNext()) {
                try {
                    songList.add(new Song(
                                    cursor.getString(cursor.getColumnIndex(SongEntry.COLUMN_SONG_ID)),
                                    cursor.getString(cursor.getColumnIndex(SongEntry.COLUMN_TITLE)),
                                    cursor.getString(cursor.getColumnIndex(SongEntry.COLUMN_SINGER)),
                                    cursor.getString(cursor.getColumnIndex(SongEntry.COLUMN_IMG)),
                                    cursor.getString(cursor.getColumnIndex(SongEntry.COLUMN_TEXT)),
                                    cursor.getString(cursor.getColumnIndex(SongEntry.COLUMN_LANGUAGE)),
                                    cursor.getString(cursor.getColumnIndex(SongEntry.COLUMN_UPDATE_TIMESTAMP)),
                                    cursor.getString(cursor.getColumnIndex(SongEntry.COLUMN_VIEW_TIMESTAMP)),
                               cursor.getString(cursor.getColumnIndex(SongEntry.COLUMN_LINK))
                            )
                    );
                } catch (Exception e) {
                    Log.i(TAG, "makeMovieArrayFromSQLite Exception: " + e.toString());
                }
                i++;
            }

    }
}
