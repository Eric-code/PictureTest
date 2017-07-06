package com.example.hebo.picturetest.recyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hebo.picturetest.R;

import java.util.List;

/**
 * Created by 何波 on 2017/7/6.
 */
public class PicAdapter extends RecyclerView.Adapter<PicAdapter.ViewHolder>{
    private List<Pic> mPicList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View picView;
        ImageView picImage;

        public ViewHolder(View view) {
            super(view);
            picView = view;
            picImage = (ImageView) view.findViewById(R.id.pic_image);
        }
    }

    public PicAdapter(List<Pic> picList) {
        mPicList = picList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picture_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.picView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Pic pic = mPicList.get(position);
                Toast.makeText(v.getContext(), "you clicked view " , Toast.LENGTH_SHORT).show();
            }
        });
        holder.picImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Pic pic = mPicList.get(position);
                Toast.makeText(v.getContext(), "you clicked image ", Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Pic pic = mPicList.get(position);
        holder.picImage.setImageBitmap(pic.getBmpId());
    }

    @Override
    public int getItemCount() {
        return mPicList.size();
    }



}
