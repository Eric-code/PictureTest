package com.example.hebo.picturetest;


import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.hebo.picturetest.JSON.HttpUtil;
import com.example.hebo.picturetest.JSON.Photo;
import com.example.hebo.picturetest.JSON.PhotoCrop;
import com.example.hebo.picturetest.JSON.PhotoDraft;
import com.example.hebo.picturetest.image.Calculate;
import com.example.hebo.picturetest.image.ImageUtil;
import com.example.hebo.picturetest.recyclerView.Pic;
import com.example.hebo.picturetest.recyclerView.PicAdapter;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForeGroundActivity extends AppCompatActivity implements PhotoCropView.onLocationListener{
    public List<Pic> picList=new ArrayList<>();
    public SearchView mSearchView;
    public PopupMenu popupMenu;
    public Menu menu;
    public static final String TAG = "ForeGroundActivity";
    public static  String imageId;
    public static final int TAKE_PHOTO=1;//照相
    public static final int CHOOSE_PHOTO=2;//图库中选择照片
    public static final int EMPTY_ESTIMATE=3;//图片非空判断，防止重新剪裁时报错
    public static final int CROP_PHOTO_MSG=0x345;
    public static final int UPDATE_BMP=0x12;
    public static final int CHANGE_UI=0x23;
    public static String bmpPath=null;
    public static String[] result;
    public static String resultString;
    public Handler forehandler=new Handler();
    public ImageView picture;
    public RecyclerView recyclerView;
    public Button popButton;
    public FloatingActionButton okButton,quitButton;
    public static Uri imageUri,imageUri1;
    public static String imagePath,imagePath1;
    public static URL imageURL;
    public Bitmap baseBitmap;
    public Bitmap cropBitmap;
    public Canvas canvas;
    public Paint paint;
    public PhotoCropView mCropView;
    public boolean canvasEmpty=true;
    public boolean picListEmpty=true;
    public boolean floatingbtn=true;//ture表示用来发送截图，flase表示用来发送手绘图
    public int sX,sY,eX,eY,coverWidth,coverHeight;
    public double viewWidth=720;
    public double viewHeight=960;
    public int mode,relativeX,relativeY,relativeWidth,relativeHeight;
    public ProgressDialog progressDialog;
    public int albumorcamera=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fore_ground);

        //配置进度等待框
        progressDialog=new ProgressDialog(ForeGroundActivity.this);
        progressDialog.setTitle("任务正在执行中");
        progressDialog.setMessage("任务正在执行中，请等待……");
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(false);//不显示进度条

        mCropView = (PhotoCropView)findViewById(R.id.crop);
        mCropView.setLocationListener(this);
        mCropView.setVisibility(View.INVISIBLE);
        picture=(ImageView)findViewById(R.id.picture1);

        //picture.setVisibility(View.GONE);

        recyclerView=(RecyclerView)findViewById(R.id.recycler_view1);
        popButton=(Button)findViewById(R.id.popupmenu_btn1);
        okButton=(FloatingActionButton) findViewById(R.id.okButton);
        quitButton=(FloatingActionButton)findViewById(R.id.quitButton);
        Toolbar toolbar=(Toolbar)findViewById(R.id.fore_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }



        mSearchView = (SearchView) findViewById(R.id.searchView1);
        mSearchView.onActionViewExpanded();// 写上此句后searchView初始是可以点击输入的状态
        // 设置搜索文本监听
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)){
                    //mListView.setFilterText(newText);
                    Toast.makeText(ForeGroundActivity.this,"搜索成功",Toast.LENGTH_SHORT).show();
                }else{
                    //mListView.clearTextFilter();
                    Toast.makeText(ForeGroundActivity.this,"搜索内容为空",Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });


        //悬浮按钮ok
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (floatingbtn){//截取图片
                    if (albumorcamera==1){
                        //progressDialog.show();
                        mode=Calculate.ShowMode(baseBitmap,viewWidth,viewHeight);
                        relativeX= Calculate.RelativeStartX(baseBitmap,mode,sX,viewWidth,viewHeight);
                        relativeY= Calculate.RelativeStartY(baseBitmap,mode,sY,viewWidth,viewHeight);
                        relativeWidth=Calculate.RelativeWidth(baseBitmap,mode,sX,eX,viewWidth,viewHeight);
                        relativeHeight=Calculate.RelativeHeight(baseBitmap,mode,sY,eY,viewWidth,viewHeight);
                    }
                    else{
                        relativeX=(int)(sX*780/viewWidth);
                        relativeY=(int)(sY*780/viewWidth);
                        relativeWidth=(int)((eX-sX)*780/viewWidth);
                        relativeHeight=(int)((eY-sY)*780/viewWidth);
                    }
                    String base64Crop=ImageUtil.bitmapToString(bmpPath);
                    Log.e(TAG,"模式:"+mode+" 相对起点X："+relativeX+" 相对起点Y："+relativeY+" 相对宽度："+relativeWidth+" 相对高度："+relativeHeight);
                    Log.e(TAG,base64Crop);

                    /*new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Message message=new Message();
                            message.what=CROP_PHOTO_MSG;
                            forehandler=MainActivity.revHandler;
                            forehandler.sendMessage(message);
                        }
                    }).start();
                    finish();//结束本活动，就直接显示主界面*/
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

                }else {//手绘图片
                    if (!canvasEmpty){
                        picSave(baseBitmap,"forepicture.bmp");
                        String base64Draw=ImageUtil.bitmapToString(bmpPath);
                        Log.e(TAG,"press"+base64Draw);

                        //发送网络请求
                        RequestBody requestBody=new FormBody.Builder()
                                .add("value",base64Draw)//提交的请求
                                .build();
                        HttpUtil.sendOkHttpRequest("http://10.108.125.20:8900/flaskr2/draftAndroid",requestBody,new Callback(){
                            //得到服务器返回的具体内容
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String responseData=response.body().string();
                                parseJSONWithGSON(responseData);
                            }
                            //对异常情况进行处理
                            @Override
                            public void onFailure(Call call, IOException e) {
                                //Toast.makeText(BackGroundActivity.this, "图片加载失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else {
                        Toast.makeText(ForeGroundActivity.this, "手绘图片为空", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        //悬浮按钮，清除画布
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (floatingbtn){//截取图片

                }else{
                    canvas.drawColor(Color.rgb(245,245,245),PorterDuff.Mode.CLEAR);
                    picture.setImageBitmap(baseBitmap);
                    canvasEmpty=true;
                }
            }
        });

        //瀑布流列表的实现
        forehandler=new Handler(){
            public void handleMessage(Message msg){
                switch (msg.what){
                    case UPDATE_BMP:
                        recyclerView.setVisibility(View.VISIBLE);
                        mSearchView.setVisibility(View.VISIBLE);
                        popButton.setVisibility(View.VISIBLE);
                        picture.setVisibility(View.GONE);
                        okButton.setVisibility(View.GONE);
                        quitButton.setVisibility(View.GONE);
                        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                        recyclerView.setLayoutManager(layoutManager);
                        PicAdapter adapter=new PicAdapter(picList);
                        recyclerView.setAdapter(adapter);
                        Log.e(TAG,"图形绘制");
                        break;
                    default:
                        break;
                }
            }
        };

        popupMenu = new PopupMenu(this, findViewById(R.id.popupmenu_btn1));
        menu = popupMenu.getMenu();
        // 通过XML文件添加菜单项
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.popupmenu, menu);
        // 监听事件
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.choose_from_album://从相册中选择照片
                        albumorcamera=1;
                        floatingbtn=true;
                        recyclerView.setVisibility(View.GONE);
                        mSearchView.setVisibility(View.GONE);
                        popButton.setVisibility(View.GONE);
                        picture.setVisibility(View.VISIBLE);
                        if (ContextCompat.checkSelfPermission(ForeGroundActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(ForeGroundActivity.this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                        }else {
                            openAlbum();
                        }
                        break;
                    case R.id.takephotos://拍摄照片
                        albumorcamera=2;
                        floatingbtn=true;
                        recyclerView.setVisibility(View.GONE);
                        mSearchView.setVisibility(View.GONE);
                        popButton.setVisibility(View.GONE);
                        picture.setVisibility(View.VISIBLE);
                        //创建File对象，用于存储拍照后的图片,命名为outputimage.jpg,存放在SD卡应用关联缓存目录下
                        File outputImage = new File(getExternalCacheDir(),"output_image.jpg");
                        try {
                            if (outputImage.exists()){
                                outputImage.delete();
                            }
                            outputImage.createNewFile();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        if (Build.VERSION.SDK_INT>=24){//安卓版本不低于7.0
                            imageUri= FileProvider.getUriForFile(ForeGroundActivity.this,"com.example.hebo.picturetest.fileprovider",outputImage);
                        }else {
                            imageUri=Uri.fromFile(outputImage);
                        }
                        //启动相机程序
                        Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);//指定照片的输出地址
                        startActivityForResult(intent,TAKE_PHOTO);
                        break;
                    case R.id.drawphoto://手绘图形
                        floatingbtn=false;
                        recyclerView.setVisibility(View.GONE);
                        mSearchView.setVisibility(View.GONE);
                        popButton.setVisibility(View.GONE);
                        picture.setVisibility(View.VISIBLE);
                        okButton.setVisibility(View.VISIBLE);
                        quitButton.setVisibility(View.VISIBLE);
                        // 创建一张空白图片
                        baseBitmap = Bitmap.createBitmap(720, 1000, Bitmap.Config.ARGB_8888);
                        // 创建一张画布
                        canvas = new Canvas(baseBitmap);
                        // 画布背景
                        canvas.drawColor(Color.rgb(190,190,190));
                        // 创建画笔
                        paint = new Paint();
                        // 画笔颜色为黑色
                        paint.setColor(Color.BLACK);
                        // 宽度5个像素
                        paint.setStrokeWidth(5);
                        // 先将白色背景画上
                        canvas.drawColor(Color.rgb(190,190,190),PorterDuff.Mode.CLEAR);
                        canvas.drawBitmap(baseBitmap, new Matrix(), paint);
                        picture.setImageBitmap(baseBitmap);
                        picture.setOnTouchListener(new View.OnTouchListener() {
                            int startX;
                            int startY;
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                switch (event.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        // 获取手按下时的坐标
                                        startX = (int) event.getX();
                                        startY = (int) event.getY();
                                        break;
                                    case MotionEvent.ACTION_MOVE:
                                        // 获取手移动后的坐标
                                        int stopX = (int) event.getX();
                                        int stopY = (int) event.getY();
                                        // 在开始和结束坐标间画一条线
                                        canvas.drawLine(startX, startY, stopX, stopY, paint);
                                        canvasEmpty=false;
                                        // 实时更新开始坐标
                                        startX = (int) event.getX();
                                        startY = (int) event.getY();
                                        picture.setImageBitmap(baseBitmap);
                                        break;
                                }
                                return true;
                            }
                        });
                    default:
                        break;
                }
                return false;
            }
        });
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

    //处理网络数据
    public void parseJSONWithGSON(String jsonData){
        Gson gson=new Gson();
        try {
            PhotoDraft photoDraft=gson.fromJson(jsonData,PhotoDraft.class);
            Log.e(TAG,"抽象图:"+photoDraft);
            result=photoDraft.getResult();
            /*imageURL=new URL(result[0]);
            imagePath=imageURL.getPath();*/
            Log.e(TAG,0+"result[j]:"+result[0]);
            Log.e(TAG,1+"result[j]:"+result[1]);
            Log.e(TAG,2+"result[j]:"+result[2]);
            upDatePic();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void parseJSONWithGSONCrop(String jsonData){
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
                    picSave(cropBitmap,"cropbmp.bmp");
                    Message message=new Message();
                    message.what=CROP_PHOTO_MSG;
                    forehandler=MainActivity.revHandler;
                    forehandler.sendMessage(message);
                    progressDialog.dismiss();
                }
            }).start();
            Log.e(TAG,"裁剪图:"+resultString);
        }catch (Exception e){
            e.printStackTrace();
        }

        finish();//结束本活动，就直接显示主界面
    }

    //更新recyclerView中的图片
    public void upDatePic(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=0;i<result.length;i++){
                    //Bitmap bitmap=BitmapFactory.decodeResource(ForeGroundActivity.this.getResources(), R.drawable.apple);
                    String actualURL="http://10.108.125.20:8900/flaskr2/static/Imgs/"+result[i];
                    Bitmap bitmap=HttpUtil.returnBitMap(actualURL);
                    Pic pic=new Pic(bitmap);
                    if (!picListEmpty){
                        picList.remove(i);
                    }
                    picList.add(pic);
                }
                picListEmpty=false;
                Message message=new Message();
                message.what=UPDATE_BMP;
                forehandler.sendMessage(message);
            }
        }).start();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home://返回上一级
                finish();
                break;
            default:
        }
        return true;
    }

    //显示弹出式菜单
    public void popupmenu(View v) {
        popupMenu.show();
    }

    //打开图库
    public void openAlbum(){
        Intent intent=new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else {
                    Toast.makeText(this,"你关闭了本项权限",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        switch (requestCode){
            case CHOOSE_PHOTO://对图库中选择的图片进行处理
                if (resultCode==RESULT_OK){
                    handleImageOnKitkat(data);
                    mCropView.setVisibility(View.VISIBLE);
                    okButton.setVisibility(View.VISIBLE);
                    //quitButton.setVisibility(View.VISIBLE);
                    //startPhotoZoom(data.getData());
                }
                break;
            case TAKE_PHOTO://将拍摄的照片显示出来
                if (resultCode==RESULT_OK){
                     //startPhotoZoom(imageUri);
                    /*try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        picture.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }*/
                    //不知道下面这个try...catch...起到了什么作用，但是一删掉主界面就无法显示拍摄的照片
                    try {
                        Bitmap bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        //picture.setImageBitmap(bitmap);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                    bmpPath=imageUri.getPath();//将图片信息的uri转换成路径
                    baseBitmap=BitmapFactory.decodeFile(bmpPath);
                    picture.setImageBitmap(baseBitmap);
                    mCropView.setVisibility(View.VISIBLE);
                    okButton.setVisibility(View.VISIBLE);
                    //quitButton.setVisibility(View.VISIBLE);
                    //picture.setImageResource(R.drawable.apple);
                }
                break;
            case EMPTY_ESTIMATE://非空判断，防止重新剪裁时报错
                if (data!=null){
                    //setPicToView(data);
                }
                break;
            default:
                break;
        }
    }

    //处理从图库中选择的图片
    public void handleImageOnKitkat(Intent data){
        String imagePath=null;
        Uri uri=data.getData();
        if (DocumentsContract.isDocumentUri(this,uri)){
            //如果是document类型的Uri，则通过document id 处理
            String docId=DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id=docId.split(":")[1];//解析出数字格式的id
                String selection=MediaStore.Images.Media._ID+"="+id;
                imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri= ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath=getImagePath(contentUri,null);
            }
        }else if ("content".equalsIgnoreCase(uri.getScheme())){
            //如果是content类型的Uri，则使用普通方式处理
            imagePath=getImagePath(uri,null);
        }else if ("file".equalsIgnoreCase(uri.getScheme())){
            //如果是file类型的Uri，直接获取图片路径即可
            imagePath=uri.getPath();
        }
        //cropPic(imagePath);
        bmpPath=imagePath;
        displayImage(imagePath);//根据图片路径显示图片
    }
    //获取图库中图片的路径
    public String getImagePath(Uri uri,String selection){
        String path=null;
        //通过Uri和selection来获取真实的图片路径
        Cursor cursor=getContentResolver().query(uri,null,selection,null,null);
        if (cursor!=null){
            if (cursor.moveToFirst()){
                path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    public void displayImage(String imagePath){
        if (imagePath!=null){
            baseBitmap=BitmapFactory.decodeFile(imagePath);
            picture.setImageBitmap(baseBitmap);
        }else {
            Toast.makeText(this,"获取图片失败",Toast.LENGTH_SHORT).show();
        }
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


    /*//剪裁图片方法实现
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image*//*");
        //下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 0);
        intent.putExtra("aspectY", 0);
        // outputX outputY 是裁剪图片宽高
        *//*intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);*//*
        intent.putExtra("return-data", true);
        intent.putExtra("scale", true);
        startActivityForResult(intent, 3);
    }

    //保存剪裁之后的图片数据并将图片绘制出来
    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Log.e(TAG,"保存宽度："+photo.getWidth()+"+保存高度："+photo.getHeight());
            picture.setImageBitmap(photo);
            picSave(photo);
            new Thread(new Runnable() {
                @Override
                public void run() {
                Message message=new Message();
                message.what=CROP_PHOTO_MSG;
                forehandler=MainActivity.revHandler;
                forehandler.sendMessage(message);
                }
            }).start();
            finish();//结束本活动，就直接显示主界面
        }
    }



    private void cropPic(String imagePath) {
        File file = new File(imagePath);
        Intent intent = new Intent("com.android.camera.action.CROP");
        *//*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(this, "com.leon.crop.fileprovider", file);
            intent.setDataAndType(contentUri, "image*//**//*");
        } else {
        }*//*
        intent.setDataAndType(Uri.fromFile(file), "image*//*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0);//若均为1则无法任意改变矩阵长宽比
        intent.putExtra("aspectY", 0);
*//*        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);*//*
        intent.putExtra("return-data", true);
        intent.putExtra("scale", true);
        startActivityForResult(intent, 3);
    }*/
}
