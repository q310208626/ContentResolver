package com.lsj.hdmi.contentreceivertest.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lsj.hdmi.contentreceivertest.R;
import com.lsj.hdmi.contentreceivertest.bean.MediaItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by hdmi on 17-5-2.
 */
public class MediaItemAdapter extends BaseAdapter {
    private List<MediaItem> mediaItemList;
    private Context mContext;
    private LayoutInflater inflater;
    private ViewHolder viewHolder;
    private SimpleDateFormat simpleDateFormat;

    public MediaItemAdapter(Context mContext,List<MediaItem> mediaItemList) {
        this.mediaItemList = mediaItemList;
        this.mContext = mContext;
        inflater=LayoutInflater.from(mContext);
        viewHolder=new ViewHolder();
        simpleDateFormat=new SimpleDateFormat("mm:ss");
    }

    @Override
    public int getCount() {
        return mediaItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return mediaItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MediaItem mediaItem=mediaItemList.get(position);
        convertView=inflater.inflate(R.layout.media_item_list,null);
        viewHolder.musicName= (TextView) convertView.findViewById(R.id.music_name);
        viewHolder.artistName= (TextView) convertView.findViewById(R.id.artist_name);
        viewHolder.duraTime= (TextView) convertView.findViewById(R.id.dura_time);
        Date date=new Date(mediaItem.getDuration());
        String duration=simpleDateFormat.format(date);
        viewHolder.musicName.setText(mediaItem.getMusicName());
        viewHolder.artistName.setText(mediaItem.getArtistName());
//        viewHolder.duraTime.setText(String.valueOf(mediaItem.getDuration()));
        viewHolder.duraTime.setText(duration);
        return convertView;
    }

    private class ViewHolder{
        private TextView musicName;
        private TextView artistName;
        private TextView duraTime;
    }
}
