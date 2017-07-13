package com.example.hebo.picturetest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.hebo.picturetest.JSON.HttpUtil;
import com.example.hebo.picturetest.JSON.PhotoCrop;
import com.example.hebo.picturetest.image.Calculate;
import com.example.hebo.picturetest.image.ImageUtil;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForeCropActivity extends AppCompatActivity implements PhotoCropView.onLocationListener{

    public static final String TAG = "ForeCropActivity";
    public ProgressDialog progressDialog;
    public int sX,sY,eX,eY,coverWidth,coverHeight;
    public double viewWidth=720;
    public double viewHeight=960;
    public int mode,relativeX,relativeY,relativeWidth,relativeHeight;
    public Bitmap baseBitmap;
    public Bitmap cropBitmap;
    public static Handler foreCropHandler;
    public static final int CROP_SHOW=0x789;
    public static String imagePath;
    public static String resultString;
    public static Uri imageUri;
    public static URL imageURL;
    public static  String imageId;
    public static String bmpPath=null;
    ImageView picture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fore_crop);

        //配置进度等待框
        progressDialog = new ProgressDialog(ForeCropActivity.this);
        progressDialog.setTitle("任务正在执行中");
        progressDialog.setMessage("任务正在执行中，请等待……");
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(false);//不显示进度条

        PhotoCropView mCropView = (PhotoCropView) findViewById(R.id.crop1);
        mCropView.setLocationListener(this);
        mCropView.setVisibility(View.INVISIBLE);
        picture = (ImageView) findViewById(R.id.croppicture1);

        FloatingActionButton okButton = (FloatingActionButton) findViewById(R.id.okCropButton);
        FloatingActionButton quitButton = (FloatingActionButton) findViewById(R.id.quitCropButton);
        Toolbar toolbar = (Toolbar) findViewById(R.id.fore_crop_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent=getIntent();
        imagePath=intent.getStringExtra("bmpPath");
        baseBitmap= BitmapFactory.decodeFile(imagePath);
        picture.setImageBitmap(baseBitmap);
        mCropView.setVisibility(View.VISIBLE);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //progressDialog.show();
                mode= Calculate.ShowMode(baseBitmap,viewWidth,viewHeight);
                relativeX= Calculate.RelativeStartX(baseBitmap,mode,sX,viewWidth,viewHeight);
                relativeY= Calculate.RelativeStartY(baseBitmap,mode,sY,viewWidth,viewHeight);
                relativeWidth=Calculate.RelativeWidth(baseBitmap,mode,sX,eX,viewWidth,viewHeight);
                relativeHeight=Calculate.RelativeHeight(baseBitmap,mode,sY,eY,viewWidth,viewHeight);

                String base64Crop= ImageUtil.bitmapToString(imagePath);
                Log.e(TAG,"模式:"+mode+" 相对起点X："+relativeX+" 相对起点Y："+relativeY+" 相对宽度："+relativeWidth+" 相对高度："+relativeHeight);
                Log.e(TAG,base64Crop);

                //发送网络请求
                RequestBody requestBody=new FormBody.Builder()
                        .add("value",base64Crop)//提交的请求
                        .add("x",String.valueOf(relativeX))
                        .add("y",String.valueOf(relativeY))
                        .add("width",String.valueOf(relativeWidth))
                        .add("height",String.valueOf(relativeHeight))
                        .build();
                HttpUtil.sendOkHttpRequest("http://10.108.125.20:8900/flaskr2/cropAndroid",requestBody,new Callback(){
                    //得到服务器返回的具体内容
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData=response.body().string();
                        parseJSONWithGSONCrop(responseData);
                    }
                    //对异常情况进行处理
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //Toast.makeText(BackGroundActivity.this, "图片加载失败", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        foreCropHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CROP_SHOW:
                        imagePath=ForeGroundActivity.bmpPath;
                        baseBitmap= BitmapFactory.decodeFile(imagePath);
                        picture.setImageBitmap(baseBitmap);
                        break;
                }
            }
        };
    }

    //重写位置监听器中的方法
    @Override
    public void locationRect(int startX, int startY, int endX, int endY){
        Log.e(TAG,"[ "+startX+"--"+startY+"--"+endX+"--"+endY+" ]");
        sX=startX;
        sY=startY;
        eX=endX;
        eY=endY;
        coverWidth=endX-startX;
        coverHeight=endY-startY;
    }

    public void parseJSONWithGSONCrop(String jsonData){
        MainActivity.forePhotoFrom=false;//告知主界面图片是网络图片不是本地
        Gson gson=new Gson();
        try {
            PhotoCrop photoCrop=gson.fromJson(jsonData,PhotoCrop.class);
            imageId=photoCrop.getResult();
            resultString="http://10.108.125.20:8900/flaskr2/"+imageId;
            imageUri= Uri.parse(resultString);
            imageURL=new URL(resultString);
            bmpPath=imageUri.getPath();
            cropBitmap=HttpUtil.returnBitMap(resultString);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    picSave(cropBitmap,"cropimage.bmp");
                    //picture.setImageBitmap(cropBitmap);
                    Message message=new Message();
                    message.what=0x345;
                    foreCropHandler=MainActivity.revHandler;
                    foreCropHandler.sendMessage(message);
                    //progressDialog.dismiss();
                }
            }).start();
            Log.e(TAG,"裁剪图:"+resultString);
        }catch (Exception e){
            e.printStackTrace();
        }
        /*Intent intentMain=new Intent(ForeCropActivity.this,MainActivity.class);
        startActivity(intentMain);*/
        finish();//结束本活动，就直接显示主界面
    }

    public void picSave(Bitmap bitmap,String name){
        Log.e(TAG, "保存图片");
        File f = new File(getExternalCacheDir(), name);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.e(TAG, "已经保存");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Uri uri=Uri.fromFile(f);
        bmpPath=uri.getPath();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home://打开滑动菜单
                finish();
                break;
        }
        return true;
    }

}
