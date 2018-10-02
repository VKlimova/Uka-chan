package com.amargodigits.uka_chan;

// Working with content provider of the app

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.amargodigits.uka_chan.data.SongContract;
import com.amargodigits.uka_chan.data.SongDbHelper;

public class SongsProvider extends ContentProvider {

    // // Uri
    // authority
    static final String AUTHORITY = "com.amargodigits.uka_chan";
    static final String SONGS_PATH = "songs";
    static final String ADD_PATH = "add_song";
    //    static final String ADDUPDATE_PATH = "addupdate_song";
    static final String DELETE_PATH = "delete_song";
    // Content Uri's
    public static final Uri SONGS_URI = Uri.parse("content://" + AUTHORITY + "/" + SONGS_PATH);
    public static final Uri ADD_SONG_URI = Uri.parse("content://" + AUTHORITY + "/" + ADD_PATH);
    //    public static final Uri ADDUPDATE_SONG_URI = Uri.parse("content://" + AUTHORITY + "/" + ADDUPDATE_PATH);
    public static final Uri DELETE_SONG_URI = Uri.parse("content://" + AUTHORITY + "/" + DELETE_PATH);

    // Strings
    static final String SONGS_CONTENT_TYPE = ".dir/vnd." + AUTHORITY + "." + SONGS_PATH;
    // one string
    static final String SONGS_CONTENT_ITEM_TYPE = ".item/vnd." + AUTHORITY + "." + SONGS_PATH;

    //// UriMatcher
    //  Uri to query list of songs
    static final int URI_SONGS = 1;
    // Uri to query one song by ID
    static final int URI_SONG_ID = 2;
    // Uri to add song to DB if not exist or to update if exists
//    static final int URI_SONG_ADDUPDATE = 3;
    // Uri to add song to DB
    static final int URI_SONG_ADD = 4;
    // Uri to remove song from DB
    static final int URI_SONG_DELETE = 5;
    public String LOG_TAG = "uka_chan_tag";

    // Create UriMatcher
    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, SONGS_PATH, URI_SONGS);
        uriMatcher.addURI(AUTHORITY, SONGS_PATH + "/#", URI_SONG_ID);
//        uriMatcher.addURI(AUTHORITY, ADD_PATH, URI_SONG_ADDUPDATE); // to add or update song to local database
        uriMatcher.addURI(AUTHORITY, ADD_PATH, URI_SONG_ADD); // to add song to local database
        uriMatcher.addURI(AUTHORITY, DELETE_PATH + "/#", URI_SONG_DELETE); // to delete song from database
    }

    SongDbHelper dbHelper;
    SQLiteDatabase mDb;

    @Override
    public boolean onCreate() {
        Log.i(LOG_TAG, "Provider onCreate");
        dbHelper = new SongDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        String id;
        // check Uri
        switch (uriMatcher.match(uri)) {
            case URI_SONGS: //  Uri for list
                // sorting by the name if not specified
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = SongContract.SongEntry.COLUMN_VIEW_TIMESTAMP + " DESC";
                }
                break;
            case URI_SONG_ID: // Uri with ID
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = SongContract.SongEntry.COLUMN_SONG_ID + " = " + id;
                } else {
                    selection = selection + " AND " + SongContract.SongEntry.COLUMN_SONG_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        mDb = dbHelper.getWritableDatabase();
        Cursor cursor = mDb.query(SongContract.SongEntry.TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), SONGS_URI);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        Log.i(LOG_TAG, "Provider getType" + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_SONGS:
                return SONGS_CONTENT_TYPE;
            case URI_SONG_ID:
                return SONGS_CONTENT_ITEM_TYPE;
        }
        return null;
    }

//    //  insert the record to DB
//    @Nullable
//    @Override
//    public Uri addupdate(@NonNull Uri uri, @Nullable ContentValues contentValues) {
//        if (uriMatcher.match(uri) != URI_SONG_ADD)
//            throw new IllegalArgumentException("Wrong URI: " + uri);
//        mDb = dbHelper.getWritableDatabase();
//        long rowID = mDb.insert(SongContract.SongEntry.TABLE_NAME, null, contentValues);
//        Uri resultUri = ContentUris.withAppendedId(ADD_SONG_URI, rowID);
//        getContext().getContentResolver().notifyChange(resultUri, null);
//        return resultUri;
//    }

    //  insert the record to DB, or update it if exist
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        if (uriMatcher.match(uri) != URI_SONG_ADD)
            throw new IllegalArgumentException("Wrong URI: " + uri);
        mDb = dbHelper.getWritableDatabase();
        // Checking if song already exists in SQLite

        String song_id = contentValues.getAsString(SongContract.SongEntry.COLUMN_SONG_ID);
        Log.i(LOG_TAG, "SongsProvider insert-update " + song_id);
        String selection = SongContract.SongEntry.COLUMN_SONG_ID + " = '" + song_id +"'";
        Log.i(LOG_TAG, "SongsProvider insert-update " + song_id + "  selection= " + selection);
        Cursor cursor=null;
        try {
             cursor = mDb.query(SongContract.SongEntry.TABLE_NAME, null, selection,
                    null, null, null, null);
        } catch (Exception e) {
            Log.i(LOG_TAG, "Exception " +e.toString());
        }
        Uri resultUri = null;
        long rowID = 0;
        if ((cursor != null) && (cursor.getCount() > 0)) {
            Log.i(LOG_TAG, " Updating ");
            rowID = mDb.update(SongContract.SongEntry.TABLE_NAME, contentValues, selection, null);
            resultUri = ContentUris.withAppendedId(ADD_SONG_URI, rowID);
        } else {
            Log.i(LOG_TAG, " Inserting " );
            rowID = mDb.insert(SongContract.SongEntry.TABLE_NAME, null, contentValues);
            resultUri = ContentUris.withAppendedId(ADD_SONG_URI, rowID);
        }
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }


    //  delete the record in DB
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (uriMatcher.match(uri) != URI_SONG_DELETE)
            throw new IllegalArgumentException("Wrong URI: " + uri);
        mDb = dbHelper.getWritableDatabase();
        String id = uri.getLastPathSegment();
        selection = SongContract.SongEntry.COLUMN_SONG_ID + "=?";
        selectionArgs = new String[]{String.valueOf(id)};
        int cnt = mDb.delete(SongContract.SongEntry.TABLE_NAME, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    // No need for update functionality
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
