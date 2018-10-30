package com.amargodigits.uka_chan;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;

import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.amargodigits.uka_chan.model.Song;
import com.amargodigits.uka_chan.utils.NetworkUtils;
//
// Firestore is forbidden for Capstone project :-(
//
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.QuerySnapshot;
//
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.reward.RewardedVideoAd;

import static com.amargodigits.uka_chan.DetailActivity.PREFS_NAME;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_TITLE;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RewardedVideoAdListener {
    public static final String LOG_TAG = "uka_chan_tag";
    //    public FirebaseFirestore db;
    public static RecyclerView mRecyclerView;
    public static GridLayoutManager mLayoutManager;
    public static SongListAdapter mAdapter;
    public static ArrayList<Song> mSongList = new ArrayList<>();
    public static int positionIndex = 0;
    private RewardedVideoAd mRewardedVideoAd;
    SearchView searchView;
    SharedPreferences prefs;
    public static Context mContext;
    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Main onCreate setContentView(R.layout.activity_main); Exception:", e);
        }
        prefs = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        mContext = getApplicationContext();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
//        db = FirebaseFirestore.getInstance();
        mRecyclerView = findViewById(R.id.songs_rv);
        mLayoutManager = new GridLayoutManager(this, 1);
        handleIntent(getIntent());
        try {
            NetworkUtils.LoadSQLiteSongsTask mAsyncTasc = new NetworkUtils.LoadSQLiteSongsTask(getApplicationContext());
            mAsyncTasc.execute();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Loading data exception: ", e);
            throw new RuntimeException(e);
        }

        Date lastUpdDate;
        String lastUpdStr = prefs.getString("LAST_UPDATE", "");
        ParsePosition pos = new ParsePosition(0);
        if (lastUpdStr.length() < 1) {
            // if the app is run for a first time, consider it to have the oldest lastUpdateDate
            lastUpdDate = formatter.parse("2018.01.01 00:00:00", pos);
        } else {
            lastUpdDate = formatter.parse(lastUpdStr, pos);
        }
        // if the lastUpdStr is 2 days from now, check data in Firebase or Json
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_YEAR, -2);
        Date twoDaysBefore = calendar.getTime();
        if (lastUpdDate.before(twoDaysBefore)) {
//            firebase2Sqlite();
            NetworkUtils.LoadJsonSongsTask jAsyncTasc = new NetworkUtils.LoadJsonSongsTask(getApplicationContext());
            jAsyncTasc.execute();
        }
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();

    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build());
    }

    public static void doGridView(Context tContext) {
        try {
            mAdapter = new SongListAdapter(tContext, mSongList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(tContext, 1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager.scrollToPositionWithOffset(positionIndex, 0);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                NetworkUtils.LoadSQLiteSongsTask mAsyncTasc = new NetworkUtils.LoadSQLiteSongsTask(getApplicationContext());
                mAsyncTasc.execute("");
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.search) {
            onSearchRequested();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        int id = item.getItemId();
        if (id == R.id.nav_adMob) {
            if (mRewardedVideoAd.isLoaded()) {
                mRewardedVideoAd.show();
            }
        }
        if (id == R.id.nav_title) {
            Collections.sort(mSongList, new Comparator<Song>() {
                public int compare(Song o1, Song o2) {
                    return o1.getTitle().compareTo(o2.getTitle());
                }
            });
            mAdapter.notifyDataSetChanged();
        }

        if (id == R.id.nav_artist) {
            Collections.sort(mSongList, new Comparator<Song>() {
                public int compare(Song o1, Song o2) {
                    return o1.getSinger().compareTo(o2.getSinger());
                }
            });
            mAdapter.notifyDataSetChanged();
        }

        if (id == R.id.nav_like) {
            Collections.sort(mSongList, new Comparator<Song>() {
                public int compare(Song song1, Song song2) {
                    int res;
                    if (song1.getLiked() == null) res = -1;
                    else if (song2.getLiked() == null) res = 1;
                    else res = song1.getLiked().compareTo(song2.getLiked());
                    res = -res;
                    return res;
                }
            });
            mAdapter.notifyDataSetChanged();
        }
        if (id == R.id.nav_add) {
            Intent intent = new Intent(this, EditActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);

        }

        if (id == R.id.nav_date) {
            Collections.sort(mSongList, new Comparator<Song>() {
                public int compare(Song song1, Song song2) {
                    int res;
                    if (song1.getView_timestamp() == null) res = -1;
                    else if (song2.getView_timestamp() == null) res = 1;
                    else res = song1.getView_timestamp().compareTo(song2.getView_timestamp());
                    res = -res;
                    return res;
                }
            });
            mAdapter.notifyDataSetChanged();
        }

        if (id == R.id.nav_sqlite_get) {
//            firebase2Sqlite();
            NetworkUtils.LoadJsonSongsTask jAsyncTasc = new NetworkUtils.LoadJsonSongsTask(getApplicationContext());
            jAsyncTasc.execute();
        }

        if (id == R.id.nav_help) {
            ImageView drawerImg = findViewById(R.id.drawerImg);
            drawerImg.setImageResource(R.drawable.fox_uka_fest);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setIcon(R.drawable.fox_uka);
            builder.setTitle(R.string.nav_help);
            builder.setMessage(R.string.help_txt);
            builder.setInverseBackgroundForced(true);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String query = "";
        String selection = "";

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            selection = COLUMN_TITLE + " LIKE " + "'%" + query + "%' ";
            try {
                NetworkUtils.LoadSQLiteSongsTask mAsyncTasc = new NetworkUtils.LoadSQLiteSongsTask(getApplicationContext());
                mAsyncTasc.execute(selection);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Loading data exception: ", e);
                throw new RuntimeException(e);
            }
            searchView.setQuery(query, false);
            searchView.clearFocus();
            mAdapter.notifyDataSetChanged();
        }

        if (intent.getAction().equalsIgnoreCase("uka.widget")) {
            if (prefs.getString("songId", "").length() > 0) {
                Intent intent1 = new Intent(this, DetailActivity.class);
                intent1.setAction("Widget2Main2Detail");
                intent1.putExtra("songId", prefs.getString("songId", ""));
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    this.startActivity(intent1);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Opening song details exception: ", e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // Firestore is forbidden for Capstone Project :-(

    void firebase2Sqlite() {
//    Read the last Update date from Shared Preferences and select from Firebase
//    only records with lastUpdate in Firebase greater
//    then in local base. Then update the date in Shared Preferences to "Today".
//    Also, SQLite doesn't support Date type, so keeping the date in string.
//        Date lastUpdDate;
//        String lastUpdStr = prefs.getString("LAST_UPDATE", "");
//        ParsePosition pos = new ParsePosition(0);
//        if ((lastUpdStr == null) || (lastUpdStr.length() < 1)) {
//            // if the app is run for a first time, consider it to have the oldest lastUpdateDate
//            lastUpdDate = formatter.parse("2018.01.01 00:00:00", pos);
//        } else {
//            lastUpdDate = formatter.parse(lastUpdStr, pos);
//        }
//        db.collection("songs")
//                .whereGreaterThan("latestUpdateTimestamp", lastUpdDate)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        SharedPreferences.Editor editor = prefs.edit();
//                        String todayStr = formatter.format(new Date());
//                        editor.putString("LAST_UPDATE", todayStr);
//                        editor.apply();
//                        if (task.isSuccessful()) {
//                            int updCount = 0, insCount = 0, oldCount=0;
//                            for (DocumentSnapshot document : task.getResult()) {
//                                ContentValues cv = new ContentValues();
//                                cv.put(COLUMN_SONG_ID, document.getId());
//                                cv.put(COLUMN_LANGUAGE, document.getString("lang"));
//                                cv.put(COLUMN_LINK, document.getString("link"));
//                                cv.put(COLUMN_SINGER, document.getString("singer"));
//                                cv.put(COLUMN_IMG, document.getString("img"));
//                                cv.put(COLUMN_TEXT, document.getString("text"));
//                                cv.put(COLUMN_TITLE, document.getString("name"));
//                                String strDate = formatter.format(document.getDate("latestUpdateTimestamp"));
//                                cv.put(COLUMN_UPDATE_TIMESTAMP, strDate);
//                                String result = getContentResolver().insert(SongsProvider.ADD_SONG_URI, cv).toString();
//                                if (result.contains("upd_song")) ++updCount;
//                                if (result.contains("add_song")) ++insCount;
//                                if (result.contains("old_song")) ++oldCount;
//                            }
//                            NetworkUtils.LoadSQLiteSongsTask mAsyncTasc = new NetworkUtils.LoadSQLiteSongsTask(getApplicationContext());
//                            mAsyncTasc.execute();
//                            Toast.makeText(MainActivity.this,
//                                    "Added " + insCount + " new songs, " +
//                                    "Updated " + updCount + " songs, Ignored " + oldCount + " songs", Toast.LENGTH_LONG).show();
//                        } else {
//                            Log.e(LOG_TAG, "Error getting documents.", task.getException());
//                        }
//                    }
//                });

    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        // Load the next rewarded video ad.
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        ImageView drawerImg = findViewById(R.id.drawerImg);
        drawerImg.setImageResource(R.drawable.fox_uka_fest);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setIcon(R.drawable.fox_uka_fest);
        builder.setTitle(R.string.thanks);
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

        ImageView image = new ImageView(this);
        image.setImageResource(R.drawable.fox_uka_fest);

        AlertDialog.Builder alertBuilder =
                new AlertDialog.Builder(this).
                        setMessage(R.string.thanks).
                        setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).
                        setView(image);
        alertBuilder.create().show();
        TextView drawerTV = findViewById(R.id.drawer_tv);
        drawerTV.setText(R.string.nav_header_fest);
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
    }

    @Override
    public void onRewardedVideoCompleted() {
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        positionIndex = mLayoutManager.findFirstVisibleItemPosition();
        outState.putInt("GRID_SCROLL_POSITION", positionIndex);
    }

    //    Then restore the position in the onRestoreInstanceState method.
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        positionIndex = savedInstanceState.getInt("GRID_SCROLL_POSITION");
        mLayoutManager.scrollToPositionWithOffset(positionIndex, 0);
    }
}