package com.amargodigits.uka_chan.model;

// Song object, used to keep one-song data

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {
    private String songId;
    private String title;
    private String singer;
    private String img;
    private String text;
    private String language;
    private String update_timestamp;
    private String view_timestamp;
    private String link;

    public Song(String songId, String title, String singer, String img, String text, String language, String update_timestamp, String view_timestamp, String link)
     {
        this.songId = songId;
        this.title = title;
        this.singer = singer;
        this.img = img;
        this.text = text;
        this.language = language;
        this.update_timestamp = update_timestamp;
        this.view_timestamp = view_timestamp;
        this.link = link;

    }

    public Song(String songId)
    {
        this.songId = songId;
    }

    private Song(Parcel in)
    {
        String[] data = new String[9];
        in.readStringArray(data);
        this.songId = data[0];
        this.title = data[1];
        this.singer = data[2];
        this.img = data[3];
        this.text = data[4];
        this.language = data[5];
        this.update_timestamp = data[6];
        this.view_timestamp = data[7];
        this.link = data[8];

    }

    public String getSongId() { return this.songId;}
    public String getTitle() { return this.title;}
    public String getSinger() { return this.singer;}
    public String getImg() { return this.img;}
    public String getText() { return this.text; }
    public String getLanguage() {return this.language; }
    public String getUpdate_timestamp() {return this.update_timestamp; }
    public String getView_timestamp() {return this.view_timestamp; }
    public String getLink() { return link; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[] {songId, title, singer, img, text,
                language, update_timestamp, view_timestamp, link});
    }

    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {

        @Override
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}

