package com.amargodigits.uka_chan;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.ScrollView;
import android.widget.TextView;
import com.amargodigits.uka_chan.model.Song;
import com.amargodigits.uka_chan.utils.MusicUtils;
import com.amargodigits.uka_chan.utils.UkaWidgetProvider;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import static com.amargodigits.uka_chan.MainActivity.LOG_TAG;
import static com.amargodigits.uka_chan.MainActivity.mAdapter;
import static com.amargodigits.uka_chan.MainActivity.mSongList;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_LIKE;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_LINK;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_SINGER;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_SONG_ID;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_TEXT;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_TITLE;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_VIEW_TIMESTAMP;


/*
Detail Activity shows the song text and details
 */

public class DetailActivity extends AppCompatActivity {
    Drawable starDrawable; // like-unlike song
    Drawable gridDrawable; // show-hide chords
    public static final String PREFS_NAME = "UkaPrefs";
    public static TextView textTV, titleTV, singerTV, linkTV;
    public static ImageView imgIV;
    public static Context mContext;
    public static ScrollView mScrollView;
    static ImageView chordsIV;
    static String songId, songTitle, songText, songSinger, songLink, songImg, songTextBasic, songLike, songUpdate, songView;
    public static boolean fromWidget = false;
    ArrayList<String> useChords;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mScrollView = findViewById(R.id.detail_scroll);
        mContext = getApplicationContext();
        textTV = findViewById(R.id.songtext_view);
        titleTV = findViewById(R.id.songtitle_view);
        singerTV = findViewById(R.id.singer_view);
        linkTV = findViewById(R.id.link_view);
        imgIV = findViewById(R.id.img);
        chordsIV = findViewById(R.id.chordsIV); // Image view to hold chords image
        Intent intent = getIntent();
        songId = intent.getStringExtra("songId");
        detailRedraw(songId);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        songId = intent.getStringExtra("songId");
        try {
            detailRedraw(songId);
        } catch (Exception e) {
            Log.e(LOG_TAG, "DetailActivity onCreate ", e);
        }
    }

    public void detailRedraw(String songID) {
        int songNum = getSongPos(songId);
        Song song = mSongList.get(songNum);
        try {
            songTitle = song.getTitle();
            songText = song.getText();
            songSinger = song.getSinger();
            songImg = song.getImg();
            songLink = song.getLink();
            songLike = song.getLiked();
            songUpdate = song.getUpdate_timestamp();
            songView = song.getView_timestamp();
            songTextBasic = songText;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Detail Activity ", e);
        }
        titleTV.setText(songTitle);
        if ((songSinger == null) || (songSinger.length() < 1)) {
            singerTV.setVisibility(View.GONE);
            singerTV.setText("");
        } else {
            singerTV.setVisibility(View.VISIBLE);
            singerTV.setText(songSinger);
        }
        if ((songLink == null) || (songLink.length() < 1)) {
            linkTV.setVisibility(View.GONE);
            linkTV.setText("");
        } else {
            linkTV.setVisibility(View.VISIBLE);
            linkTV.setText(songLink);
            linkTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Open link in new window
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(songLink));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        mContext.startActivity(intent);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Exception opening " + Uri.parse("songLink").toString(), e);
                    }
                }
            });
        }

       useChords=MusicUtils.useChords(songText);

        textTV.setText(MusicUtils.chordsText(songText));

        try {
            if ((songImg == null) || (songImg.length() < 1)) {
                imgIV.setVisibility(View.GONE);
            } else {
                imgIV.setVisibility(View.VISIBLE);
                Picasso.with(mContext).load(songImg)
                        .placeholder(R.drawable.progress_animation)
                        .error(R.drawable.ic_cloud_off_black_24dp)
                        .into(imgIV);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "DetailActivity " , e);
        }



        if (useChords.size() == 0 || useChords == null) {
            chordsIV.setVisibility(View.GONE);
        } else {
            chordsIV.setVisibility(View.VISIBLE);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            // Show the chords
            chordsIV.setImageBitmap(MusicUtils.chordsBitmap(useChords, width, mContext));
        }

        //update local database view_timestamp
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_SONG_ID, songId);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        cv.put(COLUMN_VIEW_TIMESTAMP, formatter.format(new Date()));
        getContentResolver().insert(SongsProvider.ADD_SONG_URI, cv);

        // update view_timestamp in Adapter for RecyclerView in Main Activity
        Song changedSong = new Song(
                songId,
                titleTV.getText().toString(),
                singerTV.getText().toString(),
                songImg,
                textTV.getText().toString(),
                "",
                songUpdate,
                formatter.format(new Date()),
                linkTV.getText().toString(), songLike);
        int songPos = getSongPos(songId);
        if (songPos > -1) mSongList.set(songPos, changedSong); // else mSongList.add(changedSong);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        starDrawable = menu.findItem(R.id.action_like).getIcon();
        if (songLike != null) setLiked(songLike);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Boolean showChords = prefs.getBoolean("showChords", true);
        gridDrawable = menu.findItem(R.id.action_grid).getIcon();
        if (showChords) {
            chordsIV.setVisibility(View.VISIBLE);
            menu.findItem(R.id.action_grid).setTitle(R.string.chordoff);
            gridShow("TRUE");
        } else {
            chordsIV.setVisibility(View.GONE);
            menu.findItem(R.id.action_grid).setTitle(R.string.chordon);
            gridShow("FALSE");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }

        if (id == R.id.action_like) {
            if (songLike == null) songLike = "TRUE";
            else {
                if (songLike.equals("TRUE")) songLike = "FALSE";
                else songLike = "TRUE";
            }
            setLiked(songLike);
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_SONG_ID, songId);
            cv.put(COLUMN_TITLE, titleTV.getText().toString());
            cv.put(COLUMN_SINGER, singerTV.getText().toString());
            cv.put(COLUMN_LINK, linkTV.getText().toString());
            cv.put(COLUMN_TEXT, textTV.getText().toString());
            cv.put(COLUMN_LIKE, songLike);
            getContentResolver().insert(SongsProvider.ADD_SONG_URI, cv);
            Song changedSong = new Song(
                    songId, songTitle,
                    songSinger,
                    songImg,
                    songTextBasic,
                    "",
                    songUpdate,
                    songView,
                    songLink, songLike);
            int songPos = getSongPos(songId);
            if (!fromWidget) {
                mSongList.set(songPos, changedSong);
                mAdapter.notifyDataSetChanged();
            }
        }

        if (id == R.id.action_grid) {
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            if (item.getTitle() == getString(R.string.chordoff)) {
                chordsIV.setVisibility(View.GONE);
                editor.putBoolean("showChords", false);
                editor.apply();
                item.setTitle(R.string.chordon);
                gridShow("FALSE");
            } else {
                editor.putBoolean("showChords", true);
                editor.apply();
                chordsIV.setVisibility(View.VISIBLE);
                item.setTitle(R.string.chordoff);
                gridShow("TRUE");
            }
        }

        if (id==R.id.action_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, songTitle + "\n" + songTextBasic);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Share '" + songTitle + "'"));
        }

        if (id == R.id.action_edit) {
            Intent intent = new Intent(mContext, EditActivity.class);
            intent.putExtra("songId", songId);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                mContext.startActivity(intent);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Edit song  exception: ", e);
                throw new RuntimeException(e);
            }
        }

        if (id == R.id.action_widget) {
            Context context = mContext;
            SharedPreferences.Editor spEditor = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            spEditor.putString("songId", songId);
            spEditor.putString("songName", songTitle);
            spEditor.putString("songSinger", songSinger);
            spEditor.putString("songImg", songImg);
            spEditor.putString("songText", songTextBasic);
            spEditor.putString("songLink", songLink);
            spEditor.putString("songLike", songLike);
            spEditor.putString("songUpdate", songUpdate);
            spEditor.putString("songView", songView);
            spEditor.apply();
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.uka_widget_provider);
            ComponentName thisWidget = new ComponentName(context, UkaWidgetProvider.class);
            remoteViews.setTextViewText(R.id.appwidget_title, songTitle);
            remoteViews.setTextViewText(R.id.appwidget_text, songTextBasic);
            remoteViews.setImageViewBitmap(R.id.chords_image, MusicUtils.chordsBitmap(useChords, 1200, mContext));
            appWidgetManager.updateAppWidget(thisWidget, remoteViews);
        }
        return super.onOptionsItemSelected(item);
    }

    public static int getSongPos(String songTag) {
        if (!fromWidget) {
            for (int i = 0; i < mSongList.size(); ++i) {
                if (mSongList.get(i).getSongId().equals(songTag)) return i;
            }
        }
        return -1;
    }

    private void setLiked(String ifliked) {
        try {
            if (starDrawable != null) {
                starDrawable.mutate();
                if (ifliked.equals("TRUE")) {
                    //like song
                    starDrawable.setColorFilter(Color.rgb(255, 153, 51), PorterDuff.Mode.SRC_ATOP);

                    // Log to Firebase Analytics
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, songId);
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, songTitle);
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "like");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_TO_WISHLIST, bundle);

                } else {
                    //unlike song
                    starDrawable.setColorFilter(Color.parseColor("#C2C2C2"), PorterDuff.Mode.SRC_ATOP);
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Set star color Exception: ", e);
        }
    }

    private void gridShow(String ifshow) {
        try {
            if (gridDrawable != null) {
                gridDrawable.mutate();
                if (ifshow.equals("TRUE")) {
                    gridDrawable.setColorFilter(Color.rgb(255, 153, 51), PorterDuff.Mode.SRC_ATOP);
                } else {
                    gridDrawable.setColorFilter(Color.parseColor("#9E9E9E"), PorterDuff.Mode.SRC_ATOP);
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Set gridDrawable color Exception ", e );
        }
    }

    @Override
    public void onBackPressed() {
        try {
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction("Detail_Back");
            startActivity(intent);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Detail onBackPressed" , e);
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        detailRedraw(songId);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
