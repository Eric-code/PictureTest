package com.example.hebo.picturetest.recyclerView;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hebo.picturetest.BackGroundActivity;
import com.example.hebo.picturetest.ForeGroundActivity;
import com.example.hebo.picturetest.MainActivity;
import com.example.hebo.picturetest.R;

import java.util.List;

/**
 * Created by 何波 on 2017/7/6.
 */
public class PicAdapter extends RecyclerView.Adapter<PicAdapter.ViewHolder>{
    private List<Pic> mPicList;
    private Handler handler=new Handler();
    private static final int BACK_PIC_CLICK=0x456;
    public static boolean BackOrFore=true;//判断此时RecyclerView中显示的是背景还是前景图片，默认为背景图片

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
        /*holder.picView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Pic pic = mPicList.get(position);
                Toast.makeText(v.getContext(), "you clicked view " , Toast.LENGTH_SHORT).show();
            }
        });*/
        holder.picImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = holder.getAdapterPosition();
                Pic pic = mPicList.get(position);
                Toast.makeText(v.getContext(), "you clicked image ", Toast.LENGTH_SHORT).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message message=new Message();
                        message.what=BACK_PIC_CLICK;
                        message.arg1=position;
                        if (BackOrFore){
                            handler= BackGroundActivity.handler;
                        }else {
                            handler= ForeGroundActivity.forehandler;
                        }
                        handler.sendMessage(message);
                    }
                }).start();

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
