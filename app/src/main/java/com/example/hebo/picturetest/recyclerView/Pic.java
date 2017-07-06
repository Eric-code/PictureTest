package com.example.hebo.picturetest.recyclerView;

import android.graphics.Bitmap;

/**
 * Created by 何波 on 2017/7/6.
 */
public class Pic {
    private Bitmap BmpId;

    public Pic(Bitmap BmpId){
        this.BmpId=BmpId;
    }

    public Bitmap getBmpId() {
        return BmpId;
    }
}
