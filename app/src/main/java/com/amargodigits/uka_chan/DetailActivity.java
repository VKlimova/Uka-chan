package com.amargodigits.uka_chan;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/*
Detail Activity shows the song text and details
 */

public class DetailActivity extends AppCompatActivity {

    public static TextView textTV, titleTV, singerTV, linkTV;
    public Context mContext;
    Toolbar mToolbar;
    public static String LOG_TAG = "uka_chan_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mContext = getApplicationContext();
        Log.i(LOG_TAG, "Detail onCreate");
        textTV = (TextView) findViewById(R.id.songtext_view);
        titleTV = (TextView) findViewById(R.id.songtitle_view);
        singerTV = (TextView) findViewById(R.id.singer_view);
        linkTV = (TextView) findViewById(R.id.link_view);

        Intent intent = getIntent();
        String songId = intent.getStringExtra("songId");
        String songTitle = intent.getStringExtra("songTitle");
        String songText = intent.getStringExtra("songText");
        String songSinger = intent.getStringExtra("songSinger");
        String songImg = intent.getStringExtra("songImg");
        final String songLink = intent.getStringExtra("songLink");

        titleTV.setText(songTitle);

        if (songSinger == null) {
            singerTV.setVisibility(View.GONE);
        } else {
            singerTV.setText(songSinger);
        }

        if (songLink == null) {
            linkTV.setVisibility(View.GONE);
        } else {
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
                        //todo
                        Log.i(LOG_TAG, "Exception opening " + Uri.parse("songLink").toString() + " : " + e.toString());
                    }
                }
            });
        }


        songText = songText.replace("\t", "    ");
        songText = songText.replace("\n", " \n");
        songText = songText.replace("\r", " \r");
        songText = songText.replace("\0", " \0");
        String newstring = "";

        String[] chords = new String[]{"A", "B", "C", "D", "E", "F", "G", "H"};
        String[] chordPrefs = new String[]{"#m", "m#", "b", "m", "#", "7", "6", "b", ""};
        ArrayList<String> useChords = new ArrayList<String>(); //Chord used in a song list
        Spannable text = new SpannableString(songText);
        int curInd, chordLength;
        for (String chord : chords) {
            for (String chordPref : chordPrefs) {
                chordLength = chord.length() + chordPref.length();
                curInd = songText.indexOf(chord + chordPref + " ");
                if (curInd > -1) useChords.add(chord + chordPref);
                while (curInd > -1) {
                    try {
//                    Log.i(TAG, "found " + chord+chordPref + " at " + curInd);
                        text.setSpan(new ClickableSpan() {
                            @Override
                            public void onClick(View widget) {
                                // TODO Open link
//                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(revUrl));
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            try {
//                                mContext.startActivity(intent);
//                            } catch (Exception e) {
//                                Log.i(TAG, "Exception opening " + Uri.parse("http://ya.ru").toString() + " : " + e.toString());
//                            }
                            }

                            @Override
                            public void updateDrawState(TextPaint textPaint) {
                                textPaint.setColor(textPaint.linkColor);
                                textPaint.setUnderlineText(false);    // this remove the underline
                            }
                        }, curInd, curInd + chordLength, 0);

                        newstring = songText.substring(0, curInd);
                        newstring = newstring.concat(chord + chordPref + ".");
                        newstring = newstring + (songText.substring(curInd + chordLength + 1, songText.length()));
                        curInd = newstring.indexOf(chord + chordPref + " ", curInd + chordLength);
                        songText = newstring;
                    } catch (Exception e) {
                        Log.i(LOG_TAG, "Exception " + e.toString());
                    }
                }
            }
        }

        textTV.setText(text);
// IMG
        ImageView img = (ImageView) findViewById(R.id.img);
        if (songImg == null) {
            img.setVisibility(View.GONE);
        } else {
            Picasso.with(this).load(songImg)
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.ic_cloud_off_black_24dp)
                    .into(img);
        }





        Log.i(LOG_TAG, "-----Starting with images-----");
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        Log.i(LOG_TAG, "width=" + width);
        ImageView chordsIV = (ImageView) findViewById(R.id.chordsIV); // Image view to hold chords image
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inMutable = true;
        Bitmap oneChordBmp = BitmapFactory.decodeResource(getResources(), R.drawable.blank_chord, options);
        int tmpWidth = oneChordBmp.getWidth();
        int tmpHeight = oneChordBmp.getHeight();
        int chordNum = useChords.size(); // Number of chords to be displayed
        if (chordNum<3) chordNum++;
        int chordWidth = width / (chordNum); // Width of one chord image
        float koef = (float) chordWidth / tmpWidth;
        Log.i(LOG_TAG, "koef=" + String.valueOf(koef));
        int chordHeight = (int) Math.floor(tmpHeight * koef); //height of one chord image

        Log.i(LOG_TAG, "chordWidth=" + chordWidth + " chordHeight=" + chordHeight);

        Matrix scaleOnechordMatrix = new Matrix();
        scaleOnechordMatrix.setScale(koef, koef);
        try {
            Bitmap mutableOneChordBmp = Bitmap.createBitmap(oneChordBmp, 0, 0, tmpWidth, tmpHeight, scaleOnechordMatrix, true);
            Bitmap mutableAllChordsBmp = Bitmap.createBitmap(chordWidth * chordNum, chordHeight, Bitmap.Config.ARGB_8888);
            Canvas chordCanvas = new Canvas(mutableAllChordsBmp);
            Matrix moveMatrix = new Matrix();
            Paint paint = new Paint();
            paint.setColor(Color.DKGRAY);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextSize(chordWidth / 3);
            Paint fPaint = new Paint();
            fPaint.setColor(Color.RED);
            fPaint.setTypeface(Typeface.DEFAULT_BOLD);
            fPaint.setTextSize(chordWidth/2);
            for (int j = 0; j < useChords.size(); j++) { // j is a chord number
                moveMatrix.setTranslate(j * chordWidth, 0);
                chordCanvas.drawBitmap(mutableOneChordBmp, moveMatrix, null);
                chordCanvas.drawText(useChords.get(j).toString(), j * chordWidth + (int) chordWidth / 15, chordWidth / 3, paint);
                String curChord = getChord(useChords.get(j));
                if (curChord.length()>0) {  //if chord found in table, draw the chord
                    Log.i(LOG_TAG, useChords.get(j) + " " + curChord);
                    int fretNum;
                    for (int i = 0; i <= 3; i++) { // i is a wire number
                        fretNum = Integer.valueOf(curChord.substring(i,i+1));
                        Log.i(LOG_TAG, fretNum + " ");
                        if (fretNum > 0)
                            chordCanvas.drawCircle(j * chordWidth + (fretNum) * chordWidth * 88 / 500 + chordWidth / 50, chordHeight * 65 / 200 + (4-i) * chordHeight * 107 / 700, chordWidth / 14, fPaint);
                    }
                }
                else {
                    Log.i(LOG_TAG, "Chord not found");
                    chordCanvas.drawText("?",j * chordWidth + (1) * chordWidth * 88 / 500 + chordWidth / 50, chordHeight * 65 / 200 + (3) * chordHeight * 107 / 700, fPaint);
                }
            }
            chordsIV.setImageBitmap(mutableAllChordsBmp);
        } catch (Exception e) {
            Log.i(LOG_TAG, "Exception " + e.toString());
        }
    }

    public  String getChord(String chordName) {

String chordSchema="";
        String packageName = getPackageName();
        // Resource name can't contain "#" but it has important meaning in music
        int resId = getResources().getIdentifier(chordName.replace("#","_"), "string", packageName);
        Log.i(LOG_TAG, "getChord " + chordName + " resId=" + resId);
        if (resId>0) chordSchema=getString(resId);
        return chordSchema;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
