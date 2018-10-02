package com.amargodigits.uka_chan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.amargodigits.uka_chan.data.SongContract;
import com.amargodigits.uka_chan.model.Song;
import com.amargodigits.uka_chan.utils.NetworkUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_IMG;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_LANGUAGE;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_LINK;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_SINGER;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_IMG;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_SONG_ID;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_TEXT;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_TITLE;
import static com.amargodigits.uka_chan.data.SongContract.SongEntry.COLUMN_UPDATE_TIMESTAMP;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

public static String LOG_TAG = "uka_chan_tag";
public FirebaseFirestore db; // = FirebaseFirestore.getInstance();
public String songTag="";
public static RecyclerView mRecyclerView;
public static RecyclerView.LayoutManager mLayoutManager;
public static SongListAdapter mAdapter;
public static ArrayList<Song> mSongList = new ArrayList<>();
TextView helloworld; // = findViewById(R.id.hello_world);;

    public static ArrayList<Song> songList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "Main onCreate 1");
        setContentView(R.layout.activity_main);
        Log.i(LOG_TAG, "Main onCreate 2");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        helloworld = findViewById(R.id.hello_world);
        helloworld.setMovementMethod(new ScrollingMovementMethod());


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        db = FirebaseFirestore.getInstance();

        mRecyclerView = (RecyclerView) findViewById(R.id.songs_rv);

        mLayoutManager = new GridLayoutManager(this, 1);

        try {
            NetworkUtils.LoadSQLiteSongsTask mAsyncTasc = new NetworkUtils.LoadSQLiteSongsTask(getApplicationContext());
            mAsyncTasc.execute();
        } catch (Exception e) {
            Log.i(LOG_TAG, "Loading data exception: " + e.toString());
            throw new RuntimeException(e);
        }
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
        Log.i(LOG_TAG, "mSongList.size =" + mSongList.size());
        Log.i(LOG_TAG, "mAdapter.getItemCount =" + mAdapter.getItemCount());
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Map<String, Object> user = new HashMap<>();
        int id = item.getItemId();
songTag="";
        if (id == R.id.nav_camera) {
            // Create a new song with a name and text
            songTag = "RIEN";
            user.clear();
            user.put("name", "Non, Je ne regrette rien");
            user.put("singer", "Édith Piaf");
            user.put("text", "  G                D \n" +
                    " Non! Rien de rien ...\n" +
                    " D                   G \n" +
                    " Non ! Je ne regrette rien\n" +
                    "       C              C6 \n" +
                    " Ni le bien qu'on m'a fait\n" +
                    "       Am                      D\n" +
                    " Ni le mal tout ça m'est bien égal !\n" +
                    "\n" +
                    " G            D \n" +
                    " Non! Rien de rien ...\n" +
                    " D                   G \n" +
                    " Non ! Je ne regrette rien...\n" +
                    "         C       C6       Am\n" +
                    " C'est payé, balayé, oublié\n" +
                    "       D         G \n" +
                    " Je me fous du passé!\n");
            user.put("lang", "fr");
            user.put("link", "https://www.youtube.com/watch?v=xH0uygXGuWU");

        } else if (id == R.id.nav_gallery) {

            // Create a new song with a name and text
            songTag="MARIACHI";
            user.clear();
            user.put("name", "Canción del Mariachi");
            user.put("singer", "Antonio Banderas");
            user.put("text", "Em\n" +
                    "Soy un hombre muy honrado \n" +
                    "\t\t       B7\n" +
                    "Que me gusta lo mejor\n" +
                    "Em\n" +
                    "las mujeres no me faltan\n" +
                    "                              B7 \n" +
                    " ni el dinero ni el amor\n" +
                    "\n" +
                    "Em\n" +
                    "Cabalgando en mi caballo\n" +
                    "                               B7\n" +
                    "por la sierra yo me voy\n" +
                    "\n" +
                    "Las estrellas y la luna \n" +
                    "\t\t\tEm\n" +
                    "ellas me dicen donde voy\n");
            user.put("lang", "es");
            user.put("link", "https://www.youtube.com/watch?v=KEp5zoPLeWE");
        } else if (id == R.id.nav_slideshow) {
            // Create a new song with a name and text
            songTag="DESPACITO";
            user.clear();
            user.put("name", "Despacito");
            user.put("singer", "Luis Fonsi");
            user.put("text", "Bm                              G\n" +
                    "Si, sabes que ya llevo rato mirándote\n" +
                    "D                            A\n" +
                    "Tengo que bailar contigo hoy (DY)\n" +
                    "Bm                                G\n" +
                    "Vi, que tu mirada ya estaba llamándome\n" +
                    "D                           A\n" +
                    "Muéstrame el camino que yo voy (oh)\n" +
                    "\n" +
                    "Bm                              G\n" +
                    "Tú, tú eres el imán y yo soy el metal\n" +
                    "G                                  D\n" +
                    "Me voy acercando y voy armando el plan\n" +
                    "D                                  A\n" +
                    "Solo con pensarlo se acelera el pulso (oh yeah)\n" +
                    "Bm                                   G\n" +
                    "Ya, ya me está gustando más de lo normal\n" +
                    "G                                D\n" +
                    "Todos mis sentidos van pidiendo más\n" +
                    "D                                 A\n" +
                    "Esto hay que tomarlo sin ningún apuro\n");
            user.put("lang", "es");


        }
        else if (id==R.id.nav_waikiki){

//            songTag="WAIKIKI";
//            user.clear();
//            user.put("name", "On The Beach At Waikiki");
//            user.put("singer", "Henry Kailimai");
//            user.put("img","https://firebasestorage.googleapis.com/v0/b/uka-chan.appspot.com/o/waikiki.png?alt=media&token=fd5a9ca8-483e-4a00-a363-a769b945e92b");
//            user.put("text", "");
//            user.put("lang", "en");

            songTag="IPONEMA";
            user.clear();
            user.put("name", "Girl from Iponema");
            user.put("singer", "Antônio Carlos Jobim");
            user.put("img","https://firebasestorage.googleapis.com/v0/b/uka-chan.appspot.com/o/ipanema.png?alt=media&token=7ad2c882-54d8-4c0f-8553-157b3b09fa96");
            user.put("text", "Fm7                      F6                            \n" +
                    "Tall and tan and young and lovely, \n" +
                    "G7                     G7sus          G7\n" +
                    "the girl from Ipanema goes walking\n" +
                    "Gm7                                    F#7-5                         \n" +
                    "And when she passes each one \n" +
                    "Fm7     F#7-5\n" +
                    "she passes goes ah…\n" +
                    "\n" +
                    "Fm7                             F6                        \n" +
                    "When she walks she’s like a samba \n" +
                    " G7                            G7sus        G7\n" +
                    "that swings so cool and sways so gentle\n" +
                    "Gm7                                      F#7                             \n" +
                    "That when she passes each one\n" +
                    "                               Fm7\n" +
                    " she passes goes ah…\n" +
                    "\n" +
                    "F#m                           B7 B7sus B7 \n" +
                    "Oh, but I watch her so sadly. \n" +
                    "F#m7                       D9 Am7 D9\n" +
                    "How can I tell her I love her\n" +
                    "Gm7                                   Eb9\n" +
                    "Yes I would give my heart gladly\n" +
                    "                Am7                                   D7b9b5\n" +
                    "But each day when she walks to the sea\n" +
                    "        Gm7                                   C7b9b5\n" +
                    "She looks straight ahead not at me\n" +
                    "FM7                     F6                             \n" +
                    "Tall and tan and young and lovely,\n" +
                    "G7                G7sus            G7\n" +
                    "The girl from Ipanema goes walking\n" +
                    "Gm7                                F#7-5 \n" +
                    "And when she passes I smile\n" +
                    "Fm7          F#7-5     Fm7\n" +
                    "but she doesn’t see, she just doesn’t see\n" +
                    "\n" +
                    "F#7-5              Fm7\n" +
                    "No she doesn’t see\n");
            user.put("lang", "en");
        }

//        https://firebasestorage.googleapis.com/v0/b/uka-chan.appspot.com/o/ipanema.png?alt=media&token=7ad2c882-54d8-4c0f-8553-157b3b09fa96

        else if (id == R.id.nav_manage) {
//  SQLite -> TextView
            Log.i(LOG_TAG, "Main R.id.nav_manage  SQLite -> TextView");
            helloworld.setText("Start\n");
            helloworld.append("Song".toString());

            Cursor cursor = this.getContentResolver().query(
                    SongsProvider.SONGS_URI,
                    null, null, null, null);
            Log.i(LOG_TAG, "cursor getCount=" + cursor.getCount());
            int i=0;
            while (cursor.moveToNext()) {
                try {
                    Log.i(LOG_TAG, "Main R.id.nav_manage " + i);
                    helloworld.append(cursor.getString(cursor.getColumnIndex(SongContract.SongEntry.COLUMN_SONG_ID)));
                    helloworld.append("\n\n");
                } catch (Exception e) {
                    Log.i(LOG_TAG, "Main UKA Exception: " + e.toString());
                }
                i++;
            }
            cursor.close();


        } else if (id == R.id.nav_sqlite_get) {
// Firebase -> SQLite
            Log.i(LOG_TAG, "Main R.id.nav_sqlite_get 1");
            db.collection("songs")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Log.i(LOG_TAG, "Main R.id.nav_sqlite_get onComplete ");
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult()) {
//                                    Log.i(LOG_TAG, document.getId() + " => " + document.getData());
//                                    helloworld.append(document.getData().toString());
                                    ContentValues cv = new ContentValues();
                                    cv.put(COLUMN_SONG_ID, document.getId());
                                    cv.put(COLUMN_LANGUAGE, document.getString("lang"));
                                    cv.put(COLUMN_LINK, document.getString("link"));
                                    cv.put(COLUMN_SINGER, document.getString("singer"));
                                    cv.put(COLUMN_IMG, document.getString("img"));
                                    cv.put(COLUMN_TEXT, document.getString("text"));
                                    cv.put(COLUMN_TITLE, document.getString("name"));
//                                    cv.put(COLUMN_UPDATE_TIMESTAMP, document.getString("latestUpdateTimestamp"));
                                    getContentResolver().insert(SongsProvider.ADD_SONG_URI, cv);
                                }
                            } else {
                                Log.i(LOG_TAG, "Error getting documents.", task.getException());
//                                helloworld.setText("Error getting documents.");
                            }
                        }
                    });
            Log.i(LOG_TAG, "Main R.id.nav_sqlite_get 2");

        } else if (id == R.id.nav_sqlite_clear) {

        }
// Add a new document with a generated ID
        if(songTag!="") {
            Log.i(LOG_TAG, "Main songTag!=\"\"");
            user.put("latestUpdateTimestamp", FieldValue.serverTimestamp());
            db.collection("songs").document(songTag)
                    .set(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(LOG_TAG, "DocumentSnapshot added with ID: " + songTag);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(LOG_TAG, "Error adding document", e);
                        }
                    });
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
