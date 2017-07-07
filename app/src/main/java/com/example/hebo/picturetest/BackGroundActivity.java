package com.example.hebo.picturetest;

import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.hebo.picturetest.JSON.HttpUtil;
import com.example.hebo.picturetest.JSON.Photo;
import com.example.hebo.picturetest.recyclerView.Pic;
import com.example.hebo.picturetest.recyclerView.PicAdapter;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;


public class BackGroundActivity extends AppCompatActivity{
    private List<Pic> picList=new ArrayList<>();

    private SearchView mSearchView;
    PopupMenu popupMenu;
    Menu menu;
    private static final String TAG = "BackGroundActivity";
    public static final int TAKE_PHOTO=1;//照相
    public static final int CHOOSE_PHOTO=2;//图库中选择照片
    public static final int TAKE_PHOTO_MSG=0x123;
    public static final int CHOOSE_PHOTO_MSG=0x234;
    public static final int BACK_PIC_CLICK=0x456;
    public static final int UPDATE_BMP=0x12;
    public static String bmpPath=null;
    //private ImageView picture;
    public static Uri imageUri;
    public static String imagePath=null;
    public static URL imageURL=null;
    private String[] mStrs = {"aaa", "bbb", "ccc", "airsaid"};
    public static String[] result;
    private String[] result1;
    private Bitmap[] showBmp;
    private URL[] originPhoto,smallPhoto;
    public static Handler handler;
    //private ListView mListView;
    ArrayAdapter<String>adapter;
    private boolean picListEmpty=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_ground);
        //picture=(ImageView)findViewById(R.id.picture);

        Toolbar toolbar=(Toolbar)findViewById(R.id.back_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //搜索框下部检索提示信息显示
        //mListView=(ListView)findViewById(R.id.listView);
        adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mStrs);
        //mListView.setAdapter(adapter);
        //mListView.setTextFilterEnabled(true);//过滤数据属性
        mSearchView = (SearchView) findViewById(R.id.searchView);
        mSearchView.onActionViewExpanded();// 写上此句后searchView初始是可以点击输入的状态
        mSearchView.setIconifiedByDefault(false);//默认不自动缩小成图标
        mSearchView.setSubmitButtonEnabled(true);//显示搜索按钮
        mSearchView.setQueryHint("搜索图片");
        // 设置搜索文本监听
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                //发送网络请求
                RequestBody requestBody=new FormBody.Builder()
                        .add("queryexpression",query)//提交的请求
                        .build();
                HttpUtil.sendOkHttpRequest("http://10.108.125.20:8900/flaskr2/resAndroid",requestBody,new Callback(){
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
                return false;
            }
            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)){
                    adapter.getFilter().filter(newText.toString());
                    //mListView.setFilterText(newText);
                    //Toast.makeText(BackGroundActivity.this,"搜索成功",Toast.LENGTH_SHORT).show();
                }else{
                    adapter.getFilter().filter("");
                    //mListView.clearTextFilter();
                    //Toast.makeText(BackGroundActivity.this,"搜索内容为空",Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        //瀑布流列表的实现
        handler=new Handler(){
            public void handleMessage(Message msg){
                switch (msg.what){
                    case UPDATE_BMP:
                        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
                        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                        recyclerView.setLayoutManager(layoutManager);
                        PicAdapter adapter=new PicAdapter(picList);
                        recyclerView.setAdapter(adapter);
                        break;
                    case BACK_PIC_CLICK:
                        final int clickNum=msg.arg1;//点击的缩略图序列号
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap bitmap3= HttpUtil.returnBitMap(BackGroundActivity.result[clickNum]);
                                Log.e(TAG,"position:"+clickNum);
                                Log.e(TAG,"result:"+BackGroundActivity.result[clickNum]);
                                //把获得的图片保存到本地并将路径传输到主界面中
                                File f = new File("/sdcard/"+"backpicture.png");
                                if (f.exists()) {
                                    f.delete();
                                }
                                try {
                                    FileOutputStream out = new FileOutputStream(f);
                                    bitmap3.compress(Bitmap.CompressFormat.PNG, 90, out);
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
                                Message message=new Message();
                                message.what=0x567;
                                handler=MainActivity.revHandler;
                                handler.sendMessage(message);
                            }
                        }).start();
                        finish();
                    default:
                        break;
                }
            }
        };

        popupMenu = new PopupMenu(this, findViewById(R.id.popupmenu_btn));
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
                        if (ContextCompat.checkSelfPermission(BackGroundActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(BackGroundActivity.this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
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
                            imageUri= FileProvider.getUriForFile(BackGroundActivity.this,"com.example.hebo.picturetest.fileprovider",outputImage);
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

    //处理网络数据
    private void parseJSONWithGSON(String jsonData){
        Gson gson=new Gson();
        try {
            Photo photo=gson.fromJson(jsonData,Photo.class);
            Log.e(TAG,"原图:");
            Log.e(TAG,"缩略图:");
            result=photo.getResult();
            result1=photo.getResult1();
            imageURL=new URL(result1[0]);
            imagePath=imageURL.getPath();
            Log.e(TAG,0+"result[j]:"+result1[33]);
            Log.e(TAG,1+"result[j]:"+result1[1]);
            Log.e(TAG,2+"result[j]:"+result1[2]);
            upDatePic();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void upDatePic(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=0;i<result1.length;i++){
                    //Bitmap bitmap=BitmapFactory.decodeResource(BackGroundActivity.this.getResources(), R.drawable.apple);
                    Bitmap bitmap=HttpUtil.returnBitMap(result1[i]);
                    Pic pic=new Pic(bitmap);
                    if (!picListEmpty){//判断之前照片列表中是否有之前搜索产生的图片
                        picList.remove(i);
                    }
                    picList.add(i,pic);
                }
                picListEmpty=false;
                Message message=new Message();
                message.what=UPDATE_BMP;
                handler.sendMessage(message);
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
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Message message=new Message();
                            message.what=CHOOSE_PHOTO_MSG;
                            handler=MainActivity.revHandler;
                            handler.sendMessage(message);
                        }
                    }).start();
                    finish();//结束本活动，就直接显示主界面
                }
                break;
            case TAKE_PHOTO://将拍摄的照片显示出来
                if (resultCode==RESULT_OK){
                    imagePath=imageUri.getPath();//将图片信息的uri转换成路径
                    //不知道下面这个try...catch...起到了什么作用，但是一删掉主界面就无法显示拍摄的照片
                    try {
                        Bitmap bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        //picture.setImageBitmap(bitmap);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Message message=new Message();
                            message.what=TAKE_PHOTO_MSG;
                            handler=MainActivity.revHandler;
                            handler.sendMessage(message);
                        }
                    }).start();
                    Log.e(TAG,"已经传送+"+imageUri);
                    finish();//结束本活动，就直接显示主界面
                }
                break;
            default:
                break;
        }
    }

    private void handleImageOnKitkat(Intent data){
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
        //displayImage(imagePath);//根据图片路径显示图片
    }

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
            //picture.setImageBitmap(bitmap);
        }else {
            Toast.makeText(this,"获取图片失败",Toast.LENGTH_SHORT).show();
        }
    }
}
