package com.amargodigits.uka_chan.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import com.amargodigits.uka_chan.R;
import java.util.ArrayList;
import static com.amargodigits.uka_chan.MainActivity.LOG_TAG;

public class MusicUtils {
    static String[] chords = new String[]{"A", "B", "C", "D", "E", "F", "G", "H"};
    static String[] chordPrefs = new String[]{"sus4", "#m", "m#", "m7", "#7", "bm", "b", "m", "#", "7", "6", ""};

    // this function takes array of chords and returns the bitmap with this chords of desired width
    public static Bitmap chordsBitmap(ArrayList<String> useChords, int width, Context mContext) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inMutable = true;
        Bitmap oneChordBmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.blank_chord_grey, options);
        int tmpWidth = oneChordBmp.getWidth();
        int tmpHeight = oneChordBmp.getHeight();
        int chordNum = useChords.size(); // Number of chords to be displayed
        if (chordNum < 3) chordNum++;
        int chordWidth = width / (chordNum); // Width of one chord image
        float koef = (float) chordWidth / tmpWidth;
        int chordHeight = (int) Math.floor(tmpHeight * koef); //height of one chord image
        Matrix scaleOnechordMatrix = new Matrix();
        scaleOnechordMatrix.setScale(koef, koef);
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
        fPaint.setTextSize(chordWidth / 2);

        for (int j = 0; j < useChords.size(); j++) { // j is a chord number, repeat for each chord
            moveMatrix.setTranslate(j * chordWidth, 0);
            chordCanvas.drawBitmap(mutableOneChordBmp, moveMatrix, null);
            if (useChords.get(j).length() > 3) {
                paint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
            } else {
                paint.setTypeface(Typeface.DEFAULT_BOLD);
            }
            chordCanvas.drawText(useChords.get(j), j * chordWidth + chordWidth / 15, chordWidth / 3, paint);
            String curChord = getChord(useChords.get(j), mContext);
            if (curChord.length() > 0) {  //if chord found in table, draw the chord
                int fretNum;
                for (int i = 0; i <= 3; i++) { // i is a wire number, repeat for 4 wires of ukulele
                    fretNum = 0;
                    try {
                        fretNum = Integer.valueOf(curChord.substring(i, i + 1));
                    } catch (Exception e) {
                        Log.i(LOG_TAG, "MusicUtils chord " + e.toString());
                    }
                    if (fretNum > 0)
                        chordCanvas.drawCircle(j * chordWidth + (fretNum) * chordWidth * 88 / 500 + chordWidth / 50, chordHeight * 65 / 200 + (4 - i) * chordHeight * 107 / 700, chordWidth / 14, fPaint);
                }
            } else {
                Log.i(LOG_TAG, "Chord not found");
                chordCanvas.drawText("?", j * chordWidth + chordWidth * 88 / 500 + chordWidth / 50, chordHeight * 65 / 200 + (3) * chordHeight * 107 / 700, fPaint);
            }
        }
        return mutableAllChordsBmp;
    }

    public static String getChord(String chordName, Context mContext) {
        String chordSchema = "";
        String packageName = mContext.getPackageName();
        // Resource name can't contain "#" but it has important meaning in music
        int resId = mContext.getResources().getIdentifier(chordName.replace("#", "_"), "string", packageName);
        if (resId > 0) chordSchema = mContext.getString(resId);
        return chordSchema;
    }

    // Create a spannable text with highlighted chords
    public static Spannable chordsText(String songText) {
        songText = songText.replace("\t", "    ");
        songText = songText.replace("\n", " \n");
        songText = songText.replace("\r", " \r");
        songText = songText.replace("\0", " \0");
        String newstring = "";

        Spannable text = new SpannableString(songText);
        int curInd, chordLength;
        // Run through chords+chordPrefs strings to eliminate the chords used in the song
        // Not all chords are implemented so far
        for (String chord : chords) {
            for (String chordPref : chordPrefs) {
                chordLength = chord.length() + chordPref.length();
                curInd = songText.indexOf(chord + chordPref + " ");
                while (curInd > -1) {
                    try {
                        text.setSpan(new ClickableSpan() {
                            @Override
                            public void onClick(View widget) {
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
        return text;
    }

    // Return the list of chords used in this song
    public static ArrayList<String> useChords(String songText) {
        ArrayList<String> useChords = new ArrayList<String>(); //Chord used in a song list
        int curInd;
        // Run through chords+chordPrefs strings to eliminate the chords used in the song
        for (String chord : chords) {
            for (String chordPref : chordPrefs) {
                curInd = songText.indexOf(chord + chordPref + " ");
                if (curInd > -1) useChords.add(chord + chordPref);
            }
        }
        return useChords;
    }
}
