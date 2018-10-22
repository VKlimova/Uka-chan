package com.amargodigits.uka_chan;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.amargodigits.uka_chan.model.Song;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.amargodigits.uka_chan.MainActivity.LOG_TAG;
import static com.amargodigits.uka_chan.MainActivity.mAdapter;
import static com.amargodigits.uka_chan.MainActivity.mSongList;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_LINK;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_SINGER;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_SONG_ID;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_TEXT;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_TITLE;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_UPDATE_TIMESTAMP;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_VIEW_TIMESTAMP;

/*
Edit Activity is used to modify the song text and details
 */

public class EditActivity extends AppCompatActivity {

    public static EditText tagTV, textTV, titleTV, singerTV, linkTV;
    String songImg, songLike, newSinger, songLink, songTitle, songText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tagTV = (TextInputEditText) findViewById(R.id.tagEdit);
        textTV = (TextInputEditText) findViewById(R.id.textEdit);
        titleTV = (TextInputEditText) findViewById(R.id.titleEdit);
        singerTV = (TextInputEditText) findViewById(R.id.singerEdit);
        linkTV = (TextInputEditText) findViewById(R.id.linkEdit);
        Intent intent = getIntent();
        String songId = intent.getStringExtra("songId");
        int songNum = getSongPos(songId);

        if (songNum != -1) {
            Song song = mSongList.get(songNum);
            songTitle = song.getTitle();
            songText = song.getText();
            newSinger = song.getSinger();
            songImg = song.getImg();
            songLink = song.getLink();
            songLike = song.getLiked();
        }
        tagTV.setText(songId);
        titleTV.setText(songTitle);
        singerTV.setText(newSinger);
        linkTV.setText(songLink);
        textTV.setText(songText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            String tagStr = "";
            if (tagTV.getText() != null) tagStr = tagTV.getText().toString();

            if (tagStr.length() == 0) {
                tagStr = titleTV.getText().toString().replace("\\s", "").toUpperCase();
                while (getSongPos(tagStr) > 0) tagStr = tagStr + "0";
            }

            newSinger = singerTV.getText().toString();
            //update local database
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_SONG_ID, tagStr);
            cv.put(COLUMN_TITLE, titleTV.getText().toString());
            cv.put(COLUMN_SINGER, newSinger);
            cv.put(COLUMN_LINK, linkTV.getText().toString());
            cv.put(COLUMN_TEXT, textTV.getText().toString());
            Date today = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
            cv.put(COLUMN_UPDATE_TIMESTAMP, formatter.format(today));
            cv.put(COLUMN_VIEW_TIMESTAMP, formatter.format(today));
            getContentResolver().insert(SongsProvider.ADD_SONG_URI, cv);

            // update Adapter for RecyclerView in Main Activity
            Song changedSong = new Song(
                    tagStr,
                    titleTV.getText().toString(),
                    newSinger,
                    songImg,
                    textTV.getText().toString(),
                    "",
                    formatter.format(today),
                    formatter.format(today),
                    linkTV.getText().toString(), songLike);
            int songPos = getSongPos(tagStr);
            if (songPos > -1) mSongList.set(songPos, changedSong);
            else mSongList.add(changedSong);
            Log.i(LOG_TAG, "Edit changedSong.getSinger(" + songPos + ")='" + changedSong.getSinger() + "'");
            mAdapter.notifyDataSetChanged();
            // update Firebase
            Map<String, Object> SongMap = new HashMap<>();
            SongMap.put("name", titleTV.getText().toString());
            SongMap.put("singer", newSinger);
            SongMap.put("text", textTV.getText().toString());
            SongMap.put("link", linkTV.getText().toString());
            SongMap.put("img", songImg);
            SongMap.put("latestUpdateTimestamp", FieldValue.serverTimestamp());

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("songs").document(tagStr)
                    .set(SongMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(LOG_TAG, "Edit Activity: To Firebase added with title= " + titleTV.getText().toString());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(LOG_TAG, "Edit Activity: Error adding document", e);
                        }
                    });
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public int getSongPos(String songTag) {
        for (int i = 0; i < mSongList.size(); ++i) {
            if (mSongList.get(i).getSongId().equals(songTag)) return i;
        }
        return -1;
    }
}
