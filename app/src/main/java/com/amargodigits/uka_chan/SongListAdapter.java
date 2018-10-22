package com.amargodigits.uka_chan;

import android.content.Context;
import android.content.Intent;

import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.amargodigits.uka_chan.model.Song;

import java.util.ArrayList;


/**
 * SongListAdapter is responsible for showing 1 item of the list in RecyclerView in MainActivity
 * SongListAdapter is backed by an ArrayList of {@link com.amargodigits.uka_chan.model.Song} objects which populate
 * the RecyclerView in MainActivity
 */

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList mSongList;
    public String TAG = "uka_chan_tag";

    @Override
    public SongListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView v = null;
        try {
            v = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.grid_item_layout, parent, false);
        } catch (Exception e) {
            Log.e(TAG, "Song list adapter onCreateViewHolder exception:", e);
        }
        return new ViewHolder(v);
    }

    public SongListAdapter(Context myContext, ArrayList<Song> myDataset) {
        mSongList = myDataset;
        mContext = myContext;
    }

    @Override
    public void onBindViewHolder(SongListAdapter.ViewHolder holder, int position) {
        final Song item = (Song) mSongList.get(holder.getAdapterPosition());
        String text;
        holder.recTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        if (item.getLiked() != null) {
            if (item.getLiked().equals("TRUE")) {
                holder.recTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_border_orange_24dp, 0, 0, 0);
            }
        }
        text = item.getTitle() + "; " + item.getSinger();
        holder.recTitle.setText(text);
        holder.recTitle.setOnClickListener(new AdapterView.OnClickListener() {
                                               @Override
                                               public void onClick(View view) {

                                                   String songId = item.getSongId();
                                                   Intent intent = new Intent(mContext, DetailActivity.class);
                                                   intent.putExtra("songId", songId);
                                                   intent.setAction("uka.songList");
                                                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                   try {
                                                       mContext.startActivity(intent);
                                                   } catch (Exception e) {
                                                       Log.e(TAG, "Opening song details exception: ", e);
                                                       throw new RuntimeException(e);
                                                   }
                                               }
                                           }
        );
    }

    @Override
    public int getItemCount() {
        return mSongList.size();
    }

    // Provide a reference to the views for each data item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView recTitle;

        ViewHolder(TextView v) {
            super(v);
            recTitle = v;
        }
    }
}