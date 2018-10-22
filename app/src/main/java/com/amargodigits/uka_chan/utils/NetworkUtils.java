package com.amargodigits.uka_chan.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import com.amargodigits.uka_chan.DetailActivity;
import com.amargodigits.uka_chan.R;
import com.amargodigits.uka_chan.SongsProvider;
import com.amargodigits.uka_chan.data.SongContract;
import com.amargodigits.uka_chan.model.Song;
import java.util.ArrayList;
import static com.amargodigits.uka_chan.MainActivity.doGridView;
import static com.amargodigits.uka_chan.MainActivity.mSongList;


final public class NetworkUtils {
    public static final String TAG = "uka_chan_tag";
    /**
     * This method creates AsyncTask to in background load Song list
     */
    public static class LoadSQLiteSongsTask extends AsyncTask<String, Void, ArrayList<Song>> {
        Context mContext;
        public LoadSQLiteSongsTask(Context context) {
            mContext = context;
        }
        /**
         * This method make a Network request in background
         * Load song list
         * @return ArrayList<Recipe>
         */
        @Override
        protected ArrayList<Song> doInBackground(String... params) {
                try {
                    ArrayList<Song> songList = new ArrayList<>();
                    String selection="";
                    if (params.length>0) selection=params[0];
                        Cursor cursor = mContext.getContentResolver().query(
                                SongsProvider.SONGS_URI,
                                null, selection, null, null);
                        int i=0;
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
            if (!DetailActivity.fromWidget)  doGridView(mContext);
        }
    }
}
