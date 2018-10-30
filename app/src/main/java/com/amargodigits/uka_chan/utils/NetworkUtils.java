package com.amargodigits.uka_chan.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.amargodigits.uka_chan.DetailActivity;
import com.amargodigits.uka_chan.MainActivity;
import com.amargodigits.uka_chan.R;
import com.amargodigits.uka_chan.SongsProvider;
import com.amargodigits.uka_chan.data.SongContract;
import com.amargodigits.uka_chan.model.Song;
//import com.google.firebase.firestore.DocumentSnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;

import static android.content.Context.MODE_PRIVATE;
import static com.amargodigits.uka_chan.DetailActivity.PREFS_NAME;
import static com.amargodigits.uka_chan.MainActivity.doGridView;
import static com.amargodigits.uka_chan.MainActivity.mContext;
import static com.amargodigits.uka_chan.MainActivity.mSongList;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_IMG;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_LANGUAGE;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_LINK;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_SINGER;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_SONG_ID;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_TEXT;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_TITLE;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_UPDATE_TIMESTAMP;


final public class NetworkUtils {
    public static final String TAG = "uka_chan_tag";

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    /**
     * This method creates AsyncTask to in background load Song list from SQLite to songList
     */
    public static class LoadSQLiteSongsTask extends AsyncTask<String, Void, ArrayList<Song>> {
        Context mContext;

        public LoadSQLiteSongsTask(Context context) {
            mContext = context;
        }

        /**
         * This method make a Network request in background
         * to load song list from sqlite to array
         *
         * @return ArrayList<Song>
         */
        @Override
        protected ArrayList<Song> doInBackground(String... params) {
            try {
                ArrayList<Song> songList = new ArrayList<>();
                String selection = "";
                if (params.length > 0) selection = params[0];
                Cursor cursor = mContext.getContentResolver().query(
                        SongsProvider.SONGS_URI,
                        null, selection, null, null);
                int i = 0;
                while (cursor.moveToNext()) {
                    try {
                        songList.add(new Song(
                                cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_SONG_ID)),
                                cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_TITLE)),
                                cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_SINGER)),
                                cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_IMG)),
                                cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_TEXT)),
                                cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_LANGUAGE)),
                                cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_UPDATE_TIMESTAMP)),
                                cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_VIEW_TIMESTAMP)),
                                cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_LINK)),
                                cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_LIKE))
                        ));
                    } catch (Exception e) {
                        Log.e(TAG, "Exception", e);
                    }
                    i++;
                }
                cursor.close();
                return songList;
            } catch (Exception e) {
                Log.e(TAG, R.string.error_message + "", e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Song> result) {
            super.onPostExecute(result);
            mSongList = result;
            if (!DetailActivity.fromWidget) doGridView(mContext);
        }
    }

    /**
     * This method creates AsyncTask to in background load Song list from Json to SQLite
     */
    public static class LoadJsonSongsTask extends AsyncTask<String, Void, ArrayList<Song>> {
        Context mContext;

        public LoadJsonSongsTask(Context context) {
            mContext = context;
        }

        /**
         * This method make a Network request in background
         * to load song list
         *
         * @return ArrayList<Song>
         */
        @Override
        protected ArrayList<Song> doInBackground(String... params) {

            if (isOnline(mContext)) {
                try {
                    URL scheduleRequestUrl =
                            new URL("https://firebasestorage.googleapis.com/v0/b/uka-chan.appspot.com/o/JSON_example.json?alt=media&token=f6a5eb80-87ec-453b-b138-621baf81b4ce");
                    ArrayList<Song> songsArr;
                    String songResponse = NetworkUtils.getResponseFromHttpUrl(scheduleRequestUrl);
                    songsArr = JsonUtils.getSongListFromJson(songResponse);
                    Log.i(TAG, "LoadJsonSongsTask doInBackground songsArr.size="+songsArr.size());
                    return songsArr;
                } catch (Exception e) {
                    Log.e(TAG, R.string.error_message + e.toString());
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(mContext, "no data", Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Song> result) {
            super.onPostExecute(result);
            ArrayList<Song> nSongList;

            nSongList = result;

            if (nSongList.size() > 0) {

                SharedPreferences prefs;
                prefs = mContext.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                String todayStr = formatter.format(new Date());
                editor.putString("LAST_UPDATE", todayStr);
                editor.apply();

                int updCount = 0, insCount = 0, oldCount=0;
                for (Song song : nSongList) {
                    ContentValues cv = new ContentValues();
                    cv.put(COLUMN_SONG_ID, song.getSongId());
                    cv.put(COLUMN_LANGUAGE, song.getLanguage());
                    cv.put(COLUMN_LINK, song.getLink());
                    cv.put(COLUMN_SINGER, song.getSinger());
                    cv.put(COLUMN_IMG, song.getImg());
                    cv.put(COLUMN_TEXT, song.getText());
                    cv.put(COLUMN_TITLE, song.getTitle());
                    String strDate = song.getUpdate_timestamp();
                    cv.put(COLUMN_UPDATE_TIMESTAMP, song.getUpdate_timestamp());
                    String res = mContext.getContentResolver().insert(SongsProvider.ADD_SONG_URI, cv).toString();
                    if (res.contains("upd_song")) ++updCount;
                    if (res.contains("add_song")) ++insCount;
                    if (res.contains("old_song")) ++oldCount;
                }
                NetworkUtils.LoadSQLiteSongsTask mAsyncTasc = new NetworkUtils.LoadSQLiteSongsTask(mContext);
                mAsyncTasc.execute();
                Toast.makeText(mContext, "Added " + insCount + " new songs\n" +
                        "Updated " + updCount + " songs\n" +
                        "Ignored " + oldCount + " songs", Toast.LENGTH_LONG).show();
            } else {
                Log.i(TAG, "No songs");
            }
//            doGridView(mContext);
        }


        /**
         * Checks if the device has network connection
         *
         * @param tContext - context variable
         * @return true if the device is connected to network, otherwise returns false
         */
        public static boolean isOnline(Context tContext) {
            ConnectivityManager cm =
                    (ConnectivityManager) tContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm != null ? cm.getActiveNetworkInfo() : null;
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }
    }

}
