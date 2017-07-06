package com.example.hebo.picturetest;


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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ForeGroundActivity extends AppCompatActivity {
    private SearchView mSearchView;
    PopupMenu popupMenu;
    Menu menu;
    private static final String TAG = "ForeGroundActivity";
    public static final int TAKE_PHOTO=1;//照相
    public static final int CHOOSE_PHOTO=2;//图库中选择照片
    public static final int EMPTY_ESTIMATE=3;//图片非空判断，防止重新剪裁时报错
    public static final int CROP_PHOTO_MSG=0x345;
    public static String bmpPath=null;
    private Handler forehandler=new Handler();
    private ImageView picture;
    public static Uri imageUri;
    private Bitmap baseBitmap;
    private Canvas canvas;
    private Paint paint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fore_ground);
        picture=(ImageView)findViewById(R.id.picture1);
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

        // 创建一张空白图片
        baseBitmap = Bitmap.createBitmap(480, 640, Bitmap.Config.ARGB_8888);
        // 创建一张画布
        canvas = new Canvas(baseBitmap);
        // 画布背景为灰色
        canvas.drawColor(Color.rgb(245,245,245));
        // 创建画笔
        paint = new Paint();
        // 画笔颜色为红色
        paint.setColor(Color.BLACK);
        // 宽度5个像素
        paint.setStrokeWidth(5);
        // 先将灰色背景画上
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
                        // 实时更新开始坐标
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        picture.setImageBitmap(baseBitmap);
                        break;
                    }
                return true;
            }
        });


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
                    case R.id.choose_from_album:
                        if (ContextCompat.checkSelfPermission(ForeGroundActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(ForeGroundActivity.this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                        }else {
                            openAlbum();
                        }
                        break;
                    case R.id.takephotos:
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
                    default:
                        break;
                }
                return false;
            }
        });

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
    private void openAlbum(){
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
                    //startPhotoZoom(data.getData());
                }
                break;
            case TAKE_PHOTO://将拍摄的照片显示出来
                if (resultCode==RESULT_OK){
                    startPhotoZoom(imageUri);
                    /*try {
                        Bitmap bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        picture.setImageBitmap(bitmap);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }*/
                }
                break;
            case EMPTY_ESTIMATE://非空判断，防止重新剪裁时报错
                if (data!=null){
                    setPicToView(data);
                }
                break;
            default:
                break;
        }
    }

    //处理从图库中选择的图片
    private void handleImageOnKitkat(Intent data){
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
        cropPic(imagePath);
        //displayImage(imagePath);//根据图片路径显示图片
    }
    //获取图库中图片的路径
    private String getImagePath(Uri uri,String selection){
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

    private void displayImage(String imagePath){
        if (imagePath!=null){
            Bitmap bitmap=BitmapFactory.decodeFile(imagePath);
            picture.setImageBitmap(bitmap);
        }else {
            Toast.makeText(this,"获取图片失败",Toast.LENGTH_SHORT).show();
        }
    }

    //剪裁图片方法实现
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        //下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 0.1);
        intent.putExtra("aspectY", 0.1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        intent.putExtra("scale", false);
        startActivityForResult(intent, 3);
    }

    //保存剪裁之后的图片数据并将图片绘制出来
    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
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

    private void picSave(Bitmap bitmap){
        Log.e(TAG, "保存图片");
        File f = new File(getExternalCacheDir(), "forepicture.bmp");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i(TAG, "已经保存");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Uri uri=Uri.fromFile(f);
        bmpPath=uri.getPath();
    }

    private void cropPic(String imagePath) {
        File file = new File(imagePath);
        Intent intent = new Intent("com.android.camera.action.CROP");
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(this, "com.leon.crop.fileprovider", file);
            intent.setDataAndType(contentUri, "image*//*");
        } else {
        }*/
        intent.setDataAndType(Uri.fromFile(file), "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0.1);//若均为1则无法任意改变矩阵长宽比
        intent.putExtra("aspectY", 0.1);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        intent.putExtra("scale", true);
        startActivityForResult(intent, 3);
    }
}
