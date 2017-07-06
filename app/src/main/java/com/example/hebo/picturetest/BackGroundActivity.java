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

import com.example.hebo.picturetest.recyclerView.Pic;
import com.example.hebo.picturetest.recyclerView.PicAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


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
    //private ImageView picture;
    public static Uri imageUri;
    public static String imagePath=null;
    private String[] mStrs = {"aaa", "bbb", "ccc", "airsaid"};
    private Handler handler;
    //private ListView mListView;
    ArrayAdapter<String>adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_ground);
        //picture=(ImageView)findViewById(R.id.picture);
        handler=new Handler();
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
                return false;
            }
            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                /*Object[] obj = searchItem(newText);
                updateLayout(obj);
                return true;*/
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
        initPic();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        StaggeredGridLayoutManager layoutManager = new
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        PicAdapter adapter=new PicAdapter(picList);
        recyclerView.setAdapter(adapter);


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

    private void initPic(){
        for (int i = 0; i < 3; i++) {
            Pic apple=new Pic(R.drawable.apple_pic);
            picList.add(apple);
            Pic laucher=new Pic(R.drawable.ic_launcher);
            picList.add(laucher);
            Pic background=new Pic(R.drawable.background_picture);
            picList.add(background);
            Pic banana=new Pic(R.drawable.banana_pic);
            picList.add(banana);
            Pic pineapple=new Pic(R.drawable.pineapple_pic);
            picList.add(pineapple);
        }
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
