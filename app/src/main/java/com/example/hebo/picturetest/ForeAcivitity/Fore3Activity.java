package com.example.hebo.picturetest.ForeAcivitity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.hebo.picturetest.ForeCropActivity;
import com.example.hebo.picturetest.ForeGroundActivity;
import com.example.hebo.picturetest.JSON.HttpUtil;
import com.example.hebo.picturetest.MainActivity;
import com.example.hebo.picturetest.PhotoCropView;
import com.example.hebo.picturetest.PublicWay;
import com.example.hebo.picturetest.R;
import com.example.hebo.picturetest.image.Calculate;
import com.example.hebo.picturetest.image.ImageUtil;
import com.example.hebo.picturetest.recyclerView.PicAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Fore3Activity extends ForeGroundActivity {
    public static Fore3Activity instance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fore_ground);
        instance = this;//用来在主界面中关闭本活动
        PublicWay.activityList.add(this); // 把这个界面添加到activityList集合里面

        //配置进度等待框
        progressDialog=new ProgressDialog(Fore3Activity.this);
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



        mSearchView = (SearchView) findViewById(R.id.searchView1);//搜索框下部检索提示信息显示
        mSearchView.onActionViewExpanded();// 写上此句后searchView初始是可以点击输入的状态
        // 设置搜索文本监听
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                //progressDialog.show();
                PicAdapter.BackOrFore=false;
                //发送网络请求
                RequestBody requestBody=new FormBody.Builder()
                        .add("queryexpression",query)//提交的请求
                        .build();
                HttpUtil.sendOkHttpRequest("http://10.108.125.20:8900/flaskr2/resAndroid",requestBody,new Callback(){
                    //得到服务器返回的具体内容
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData=response.body().string();
                        parseJSONWithGSONPhoto(responseData);
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
                    //mListView.setFilterText(newText);
                    //Toast.makeText(BackGroundActivity.this,"搜索成功",Toast.LENGTH_SHORT).show();
                }else{
                    //mListView.clearTextFilter();
                    //Toast.makeText(BackGroundActivity.this,"搜索内容为空",Toast.LENGTH_SHORT).show();
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
                        progressDialog.show();
                        mode=Calculate.ShowMode(baseBitmap,viewWidth,viewHeight);
                        relativeX= Calculate.RelativeStartX(baseBitmap,mode,sX,viewWidth,viewHeight);
                        relativeY= Calculate.RelativeStartY(baseBitmap,mode,sY,viewWidth,viewHeight);
                        relativeWidth=Calculate.RelativeWidth(baseBitmap,mode,sX,eX,viewWidth,viewHeight);
                        relativeHeight=Calculate.RelativeHeight(baseBitmap,mode,sY,eY,viewWidth,viewHeight);
                    }
                    else{
                        progressDialog.show();
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
                    PicAdapter.BackOrFore=false;
                    if (!canvasEmpty){
                        progressDialog.show();
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
                        Toast.makeText(Fore3Activity.this, "手绘图片为空", Toast.LENGTH_SHORT).show();
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
                        progressDialog.dismiss();
                        Log.e(TAG,"图形绘制");
                        break;
                    case RECYCLER_CLICK:
                        final int clickNum=msg.arg1;//点击的缩略图序列号
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (SearchOrDraw){
                                    actualURL=result1[clickNum];
                                }else {
                                    actualURL="http://10.108.125.20:8900/flaskr2/static/Imgs/"+result[clickNum];
                                }
                                Bitmap bitmap3= HttpUtil.returnBitMap(actualURL);
                                Log.e(TAG,"position:"+clickNum);
                                Log.e(TAG,"result:"+result[clickNum]);
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
                                /*Message message=new Message();
                                message.what=CROP_SHOW;
                                forehandler=ForeCropActivity.foreCropHandler;
                                forehandler.sendMessage(message);*/
                                Intent intent=new Intent(Fore3Activity.this,ForeCropActivity.class);
                                intent.putExtra("bmpPath",bmpPath);
                                startActivity(intent);
                                //finish();
                            }
                        }).start();
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
                        if (ContextCompat.checkSelfPermission(Fore3Activity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(Fore3Activity.this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
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
                            imageUri= FileProvider.getUriForFile(Fore3Activity.this,"com.example.hebo.picturetest.fileprovider",outputImage);
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
}
