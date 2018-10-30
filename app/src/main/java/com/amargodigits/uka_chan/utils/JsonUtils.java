package com.amargodigits.uka_chan.utils;

import com.amargodigits.uka_chan.model.Song;
//import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * JSONUtils to work with JSON data
 */

class JsonUtils {
    /** takes as the input raw Json string, fills in the songList[] array
     * @param rawJsonStr - raw string with JSON data
     */
    public static ArrayList<Song> getSongListFromJson(String rawJsonStr)
            throws JSONException {
        JSONObject rawJson = new JSONObject(rawJsonStr);
        JSONArray songJsonArr = rawJson.getJSONArray("songs");
        ArrayList<Song> tSongList= new ArrayList<>();

        for (int i = 0; (i < songJsonArr.length()); i++) {
            /* Get the JSON object representing the song */
            JSONObject songObj = songJsonArr.getJSONObject(i);
            String songTimestamp=songObj.getString("timestamp");
            tSongList.add(new Song(
                    songObj.optString("id"),
                    songObj.optString("title"),
                    songObj.optString("singer"),
                    songObj.optString("img"),
                    songObj.optString("text"),
                    songObj.optString("language"),
                    songTimestamp,
                    "2018.01.01 00:00:00",
                    songObj.optString("link"),
                    "false"
                    )
            );

        }
        return tSongList;
    }


}
