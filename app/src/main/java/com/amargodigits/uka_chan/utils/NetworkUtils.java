package com.amargodigits.uka_chan.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.amargodigits.uka_chan.R;
import com.amargodigits.uka_chan.SongsProvider;
import com.amargodigits.uka_chan.data.SongContract;
import com.amargodigits.uka_chan.model.Song;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import static com.amargodigits.uka_chan.MainActivity.doGridView;
import static com.amargodigits.uka_chan.MainActivity.mSongList;


final public class NetworkUtils {
    public static String TAG = "uka_chan_tag";
    /**
     * This method creates AsyncTask to make a Network request in background
     * To load recipies list
     */
    public static class LoadSQLiteSongsTask extends AsyncTask<Integer, Void, ArrayList<Song>> {
        Context mContext;
        public LoadSQLiteSongsTask(Context context) {
            mContext = context;
        }
        /**
         * This method make a Network request in background
         * Load recipes list
         * @return ArrayList<Recipe>
         */
        @Override
        protected ArrayList<Song> doInBackground(Integer... params) {
                try {
                    ArrayList<Song> songList = new ArrayList<>();
                        /* Get the JSON object representing the recipe */
                        Cursor cursor = mContext.getContentResolver().query(
                                SongsProvider.SONGS_URI,
                                null, null, null, null);
                        Log.i(TAG, "cursor getCount=" + cursor.getCount());
                        int i=0;
                        while (cursor.moveToNext()) {
                            try {
//                                Log.i(TAG, "NetworkUtils LoadSQLiteSongsTask " + i);

                                songList.add(new Song(
                                        cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_SONG_ID)),
                                        cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_TITLE)),
                                        cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_SINGER)),
                                        cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_IMG)),
                                        cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_TEXT)),
                                        cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_LANGUAGE)),
                                        cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_UPDATE_TIMESTAMP)),
                                        cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_VIEW_TIMESTAMP)),
                                        cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_LINK))
                                ));
                            } catch (Exception e) {
                                Log.i(TAG, "NetworkUtils UKA Exception: " + e.toString());
                            }
                            i++;
                        }
                        cursor.close();
                    return songList;
                } catch (Exception e) {
                    Log.i(TAG, R.string.error_message + e.toString());
                    e.printStackTrace();
                }

            return null;
        }
        @Override
        protected void onPostExecute(ArrayList<Song> result) {
            super.onPostExecute(result);
            mSongList = result;
            doGridView(mContext);
        }
    }

    /**
     * Checks if the device has network connection
     * @param tContext - context variable
     * @return true if the device is connected to network, otherwise returns false
     */
//    public static boolean isOnline(Context tContext) {
//        ConnectivityManager cm =
//                (ConnectivityManager) tContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo netInfo = cm != null ? cm.getActiveNetworkInfo() : null;
//        return netInfo != null && netInfo.isConnectedOrConnecting();
//    }
}
