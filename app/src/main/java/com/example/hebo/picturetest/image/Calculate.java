package com.example.hebo.picturetest.image;

import android.graphics.Bitmap;

/**
 * Created by 何波 on 2017/7/10.
 */
public class Calculate {

    //根据原图的长宽来判断显示的情况是宽度高度都不适配直接显示在中间、适配宽度还是适配高度
    public static int ShowMode(Bitmap bitmap,double viewwidth,double viewheight){
        int bmpWidth=bitmap.getWidth();
        int bmpHeight=bitmap.getHeight();
        if ((bmpWidth<=viewwidth)&&(bmpHeight<=viewheight)){
            //模式1，都不适配
            return 1;
        }else if(((double)bmpWidth/(double)bmpHeight)>=(viewwidth/viewheight)){
            //模式2，适配宽度
            return 2;
        }else{
            //模式3，适配高度
            return 3;
        }
    }

    //根据显示模式计算相对起始横坐标
    public static int RelativeStartX(Bitmap bitmap,int mode,int startX,double viewwidth,double viewheight){
        double bmpWidth=bitmap.getWidth();
        double bmpHeight=bitmap.getHeight();
        double blankleft;
        int relativesX=0;
        switch (mode){
            case 1://宽度高度都不适配
                blankleft=(viewwidth-bmpWidth)/2;
                relativesX=(int)(startX-blankleft);
                break;
            case 2://适配宽度
                relativesX=(int)(startX*bmpWidth/viewwidth);
                break;
            case 3://适配高度
                blankleft=(viewwidth-bmpWidth*viewheight/bmpHeight)/2;
                relativesX=(int)((startX-blankleft)*bmpHeight/viewheight);
                break;
        }
        return relativesX;
    }

    //根据显示模式计算相对起始纵坐标
    public static int RelativeStartY(Bitmap bitmap,int mode,int startY,double viewwidth,double viewheight){
        double bmpWidth=bitmap.getWidth();
        double bmpHeight=bitmap.getHeight();
        double blanktop;
        int relativesY=0;
        switch (mode){
            case 1://宽度高度都不适配
                blanktop=(viewheight-bmpHeight)/2;
                relativesY=(int)(startY-blanktop);
                break;
            case 2://适配宽度
                blanktop=(viewheight-bmpHeight*viewwidth/bmpWidth)/2;
                relativesY=(int)((startY-blanktop)*bmpWidth/viewwidth);
                break;
            case 3://适配高度
                relativesY=(int)(startY*bmpHeight/viewheight);
                break;
        }
        return relativesY;
    }

    //根据显示模式计算相对宽度
    public static int RelativeWidth(Bitmap bitmap,int mode,int startX,int endX,double viewwidth,double viewheight){
        double bmpWidth=bitmap.getWidth();
        double bmpHeight=bitmap.getHeight();
        double width=endX-startX;
        int relativeWidth=0;
        switch (mode){
            case 1://宽度高度都不适配
                relativeWidth=(int) width;
                break;
            case 2://适配宽度
                relativeWidth=(int)(width*bmpWidth/viewwidth);
                break;
            case 3://适配高度
                relativeWidth=(int)(width*bmpHeight/viewheight);
        }
        return relativeWidth;
    }

    //根据显示模式计算相对宽度
    public static int RelativeHeight(Bitmap bitmap,int mode,int startY,int endY,double viewwidth,double viewheight){
        double bmpWidth=bitmap.getWidth();
        double bmpHeight=bitmap.getHeight();
        double height=endY-startY;
        int relativeHeight=0;
        switch (mode){
            case 1://宽度高度都不适配
                relativeHeight=(int) height;
                break;
            case 2://适配宽度
                relativeHeight=(int)(height*bmpWidth/viewwidth);
                break;
            case 3://适配高度
                relativeHeight=(int)(height*bmpHeight/viewheight);
        }
        return relativeHeight;
    }

}
