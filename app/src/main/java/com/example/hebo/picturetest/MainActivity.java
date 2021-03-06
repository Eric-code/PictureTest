package com.example.hebo.picturetest;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.hebo.picturetest.ForeAcivitity.Fore0Activity;
import com.example.hebo.picturetest.ForeAcivitity.Fore1Activity;
import com.example.hebo.picturetest.ForeAcivitity.Fore2Activity;
import com.example.hebo.picturetest.ForeAcivitity.Fore3Activity;
import com.example.hebo.picturetest.ForeAcivitity.Fore4Activity;
import com.example.hebo.picturetest.JSON.HttpUtil;
import com.example.hebo.picturetest.JSON.PhotoCrop;
import com.example.hebo.picturetest.image.Calculate;
import com.example.hebo.picturetest.image.ImageUtil;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    private static final String TAG = "MainActivity";
    public static final int TAKE_PHOTO_MSG=0x123;
    public static final int CHOOSE_PHOTO_MSG=0x234;
    public static final int CROP_PHOTO_MSG=0x345;
    public static final int BACK_PIC_CLICK=0x567;
    public static boolean forePhotoFrom=true;//默认前景裁剪图片来自本地，为false表示来自网络的图片
    private DrawerLayout mdrawerLayout;//滑动菜单
    public ImageView back_picture,fore_picture;
    int fore_picture_item_num=0;//前景图片选项的数目
    int fore_picture_num=0;//前景图片的数目
    private int[] imageViewLeft={0,0,0,0,0,0,0,0,0,0};
    private int[] imageViewRight={100,100,100,100,100,100,100,100,100,100};
    private int[] imageViewTop={0,0,0,0,0,0,0,0,0,0};
    private int[] imageViewBottom={100,100,100,100,100,100,100,100,100,100};
    private String[] imageDatas={"0","1","2","3","4","5","6","7","8","9"};
    private String[] imageDatasId={"0","1","2","3","4","5","6","7","8","9"};
    private String[] imageDatasX={"0","1","2","3","4","5","6","7","8","9"};
    private String[] imageDatasY={"0","1","2","3","4","5","6","7","8","9"};
    private String[] imageDatasWidth={"0","1","2","3","4","5","6","7","8","9"};
    private String[] imageDatasHeight={"0","1","2","3","4","5","6","7","8","9"};
    public Uri imageUri;
    public URL imageURL;
    String base64BackString="123456";
    public String imagePath=null;
    public static Handler revHandler;
    int lastX, lastY;
    boolean itemEable=false;
    final ImageView[] imageViews = new ImageView[10];
    NavigationView navigationView;
    ProgressDialog progressDialog;
    public Bitmap backBitmap;
    public int showMode=1;
    public double viewWidth=720;
    public double viewHeight=1100;
    double nLenStart = 0;//双指之间几何距离
    float preLen=0;
    float nowLen=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ViewGroup r = (ViewGroup)findViewById (R.id.viewGroup);

        back_picture=(ImageView)findViewById(R.id.back_picture);
        back_picture.setDrawingCacheEnabled(true);//启动缓存
        fore_picture=(ImageView)findViewById(R.id.fore_picture);
        fore_picture.setDrawingCacheEnabled(true);
        fore_picture.setOnTouchListener(this);

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);//添加toolBar
        mdrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBar actionBar=getSupportActionBar();//设置toolbar上的导航按钮
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        progressDialog=new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("任务正在执行中");
        progressDialog.setMessage("任务正在执行中，请等待……");
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(false);//不显示进度条


        navigationView=(NavigationView)findViewById(R.id.nav_view);//获取滑动菜单实例
        navigationView.setItemIconTintList(null);//设置每个图标为原来的颜色
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                //在这里处理item的点击事件
                //item.setCheckable(true);//设置选项可选
                switch (item.getItemId()){
                    case R.id.backgroundpicture:
                        if (base64BackString=="123456"){
                            new  AlertDialog.Builder(MainActivity.this).setTitle("警告").setMessage("请先选择背景图片").setPositiveButton("确定",null).show();
                        }else {
                            Log.e(TAG, "显示背景图片+" + item.getItemId());
                            Intent back_intent = new Intent(MainActivity.this, BackGroundActivity.class);
                            startActivity(back_intent);
                            item.setCheckable(false);//设置选项不可选
                            item.setChecked(true);
                        }
                        break;
                    case R.id.foregroundpicture+1:
                        if (base64BackString=="123456"){
                            new  AlertDialog.Builder(MainActivity.this).setTitle("警告").setMessage("请先选择背景图片").setPositiveButton("确定",null).show();
                        }else {
                            Log.e(TAG, "前景界面1");
                            Intent fore_intent1 = new Intent(MainActivity.this, Fore0Activity.class);
                            startActivity(fore_intent1);
                            item.setCheckable(false);//设置选项不可选
                            item.setChecked(true);
                        }
                        break;
                    case R.id.foregroundpicture+2:
                        if (base64BackString=="123456"){
                            new  AlertDialog.Builder(MainActivity.this).setTitle("警告").setMessage("请先选择背景图片").setPositiveButton("确定",null).show();
                        }else {
                            Log.e(TAG, "前景界面2");
                            Intent fore_intent2 = new Intent(MainActivity.this, Fore1Activity.class);
                            startActivity(fore_intent2);
                            item.setCheckable(false);//设置选项不可选
                            item.setChecked(true);
                        }
                        break;
                    case R.id.foregroundpicture+3:
                        if (base64BackString=="123456"){
                        new  AlertDialog.Builder(MainActivity.this).setTitle("警告").setMessage("请先选择背景图片").setPositiveButton("确定",null).show();
                    }else {
                            Log.e(TAG, "前景界面3");
                            Intent fore_intent3 = new Intent(MainActivity.this, Fore2Activity.class);
                            startActivity(fore_intent3);
                            item.setCheckable(false);//设置选项不可选
                            item.setChecked(true);
                        }
                        break;
                    case R.id.foregroundpicture+4:
                        if (base64BackString=="123456"){
                        new  AlertDialog.Builder(MainActivity.this).setTitle("警告").setMessage("请先选择背景图片").setPositiveButton("确定",null).show();
                    }else {
                        Log.e(TAG, "前景界面4");
                        Intent fore_intent4 = new Intent(MainActivity.this, Fore3Activity.class);
                        startActivity(fore_intent4);
                        item.setCheckable(false);//设置选项不可选
                        item.setChecked(true);
                    }
                        break;
                    case R.id.foregroundpicture+5:
                        if (base64BackString=="123456"){
                            new  AlertDialog.Builder(MainActivity.this).setTitle("警告").setMessage("请先选择背景图片").setPositiveButton("确定",null).show();
                        }else{
                        Log.e(TAG,"前景界面5");
                        Intent fore_intent5=new Intent(MainActivity.this,Fore4Activity.class);
                        startActivity(fore_intent5);
                        item.setCheckable(false);//设置选项不可选
                        item.setChecked(true);
                        }
                        break;
                    case R.id.foregroundpicture:
                    case R.id.foregroundpicture+6:
                    case R.id.foregroundpicture+7:
                    case R.id.foregroundpicture+8:
                    case R.id.foregroundpicture+9:
                    case R.id.foregroundpicture+10:
                        if (base64BackString=="123456"){
                            new  AlertDialog.Builder(MainActivity.this).setTitle("警告").setMessage("请先选择背景图片").setPositiveButton("确定",null).show();
                        }else{
                            Intent fore_intent=new Intent(MainActivity.this,ForeGroundActivity.class);
                            startActivity(fore_intent);
                            item.setCheckable(false);//设置选项不可选
                            item.setChecked(true);
                            //item.setEnabled(false);
                        }
                        break;
                    default:
                }
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);//点击菜单之后滑动菜单收回
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        Button add_btn=(Button)findViewById(R.id.add_item);
        Button minus_btn=(Button)findViewById(R.id.minus_item);
        Button quit_btn=(Button)findViewById(R.id.quit);
        add_btn.setOnClickListener(new View.OnClickListener() {//添加前景图片菜单项
            @Override
            public void onClick(View v) {
                if (fore_picture_item_num<10){
                    fore_picture_item_num++;
                    navigationView.getMenu().add(R.id.g1,R.id.foregroundpicture+fore_picture_item_num,1,"前景图片"+fore_picture_item_num).setIcon(R.drawable.foreground_picture);
                    //Toast.makeText(MainActivity.this,"添加前景+"+fore_picture_num,Toast.LENGTH_SHORT).show();
                }
            }
        });
        minus_btn.setOnClickListener(new View.OnClickListener() {//删除前景图片菜单项
            @Override
            public void onClick(View v) {
                if (fore_picture_item_num>0){
                    navigationView.getMenu().removeItem(R.id.foregroundpicture+fore_picture_item_num);
                    switch (fore_picture_num){
                        case 1:
                            ForeGroundActivity.instance.finish();
                            break;
                        case 2:
                            Fore0Activity.instance.finish();
                            break;
                        case 3:
                            Fore1Activity.instance.finish();
                            break;
                        case 4:
                            Fore2Activity.instance.finish();
                            break;
                        case 5:
                            Fore3Activity.instance.finish();
                            break;
                        case 6:
                            Fore4Activity.instance.finish();
                            break;
                        default:
                            break;
                    }
                    if (imageViews[fore_picture_item_num]!=null){
                        fore_picture_num--;
                        imageViews[fore_picture_item_num].setWillNotDraw(true);
                    }
                    fore_picture_item_num--;
                   // Toast.makeText(MainActivity.this,"删除前景+"+fore_picture_num,Toast.LENGTH_SHORT).show();
                }
            }
        });
        quit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < PublicWay.activityList.size(); i++) {
                    if (null != PublicWay.activityList.get(i)) {
                        // 关闭存放在activityList集合里面的所有activity
                        PublicWay.activityList.get(i).finish();
                    }
                }
                finish();
                System.exit(0);
            }
        });

        revHandler=new Handler(){
            public void handleMessage(Message msg){
                switch (msg.what){
                    case TAKE_PHOTO_MSG://接收到背景界面拍摄得到的照片
                        imagePath=BackGroundActivity.imagePath;
                        backBitmap=BitmapFactory.decodeFile(imagePath);
                        base64BackString=ImageUtil.bitmapToString(imagePath);//获取base64算法压缩之后的背景图字符串
                        back_picture.setWillNotDraw(false);
                        back_picture.setImageBitmap(backBitmap);
                        showMode=4;
                        Log.e(TAG,"消息收到+"+imageUri);
                        break;
                    case CHOOSE_PHOTO_MSG://接收到背景界面从相册得到的图片
                        imagePath=BackGroundActivity.imagePath;
                        backBitmap=BitmapFactory.decodeFile(imagePath);
                        base64BackString=ImageUtil.bitmapToString(imagePath);//获取base64算法压缩之后的背景图字符串
                        back_picture.setWillNotDraw(false);
                        back_picture.setImageBitmap(backBitmap);
                        showMode=Calculate.ShowMode(backBitmap,viewWidth,viewHeight);
                        Log.e(TAG,"消息收到+"+imageUri);
                        break;
                    case BACK_PIC_CLICK://获取从背景界面搜索得到的图
                        imagePath=BackGroundActivity.bmpPath;
                        backBitmap=BitmapFactory.decodeFile(imagePath);
                        base64BackString=ImageUtil.bitmapToString(imagePath);//获取base64算法压缩之后的背景图字符串
                        back_picture.setWillNotDraw(false);
                        back_picture.setImageBitmap(backBitmap);
                        showMode=Calculate.ShowMode(backBitmap,viewWidth,viewHeight);
                        Log.e(TAG,"大图绘制成功");
                        break;
                    case CROP_PHOTO_MSG://接收到前景界面裁剪之后的图片
                        String str=null;
                        if (forePhotoFrom){
                            str=ForeGroundActivity.imageId;//记录图片id,只选取其中的数字标识符
                            imagePath=ForeGroundActivity.bmpPath;
                            Log.e(TAG,"来自前景界面"+imagePath);
                        }else {
                            str=ForeCropActivity.imageId;
                            imagePath=ForeCropActivity.bmpPath;
                            Log.e(TAG,"来自前景剪裁界面"+imagePath);
                        }
                        Bitmap bitmap2 =BitmapFactory.decodeFile(imagePath) ;
                        ImageView mImageView = new ImageView(MainActivity.this);
                        //设置图片长度高度
                        mImageView.setLayoutParams(new DrawerLayout.LayoutParams(DrawerLayout.LayoutParams.WRAP_CONTENT, DrawerLayout.LayoutParams.WRAP_CONTENT));
                        mImageView.setImageBitmap(bitmap2);
                        imageViews[fore_picture_num]=mImageView;
                        mImageView.setWillNotDraw(false);
                        mImageView.setId(fore_picture_num);
                        mImageView.setOnTouchListener(MainActivity.this);
                        str=str.trim();
                        String str2="";
                        if(str != null && !"".equals(str)) {
                            for (int j = 0; j < str.length(); j++) {
                                if (str.charAt(j) >= 48 && str.charAt(j) <= 57) {
                                    str2 += str.charAt(j);
                                }
                            }
                        }
                        imageDatasId[fore_picture_num]=str2;
                        if (fore_picture_num<10) {
                            r.addView(mImageView);
                            for (int i = 0; i < fore_picture_num ; i++) {
                                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                layoutParams.setMargins(imageViewLeft[i], imageViewTop[i], 720-imageViewRight[i], 960-imageViewBottom[i]);
                                imageViews[i].setLayoutParams(layoutParams);
                                Log.e(TAG, "图片" + i + "已经重新放置"+" sX="+imageViewLeft[i]+" sY="+imageViewTop[i]+" eX="+imageViewRight[i]+" eY="+imageViewBottom[i]);
                            }
                            fore_picture_num++;
                        }

                        Log.e(TAG,"消息收");
                        break;
                    default:
                        break;
                }
            }
        };
    }

    //toolbar菜单命令
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar,menu);
        getMenuInflater().inflate(R.menu.nav_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home://打开滑动菜单
                mdrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.delete:
                //弹出一个对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("确认删除吗？");
                builder.setTitle("提示");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    for (int i = 0; i < PublicWay.activityList.size(); i++) {
                        if (null != PublicWay.activityList.get(i)) {
                            // 关闭存放在activityList集合里面的所有activity
                            PublicWay.activityList.get(i).finish();
                        }
                    }
                    back_picture.setWillNotDraw(true);
                    base64BackString="123456";
                    //清除imageView中的图片
                    if (fore_picture_num>0){
                        for (int i=0;i<fore_picture_num;i++){
                            imageViews[i].setWillNotDraw(true);
                            imageViews[i]=null;
                        }
                    }
                    fore_picture_num=0;
                    //删除前景图片的菜单选项
                    if (fore_picture_item_num>0){
                        for (int i=fore_picture_item_num;i>0;i--){
                            navigationView.getMenu().removeItem(R.id.foregroundpicture+fore_picture_item_num);
                            fore_picture_item_num--;
                        }
                    }
                    }
                 });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    }
                });
                builder.create().show();
                break;
            case R.id.mix:
                Boolean mixable=true;
                for (int i=0;i<fore_picture_num;i++){
                    int x=Integer.parseInt(imageDatasX[i]);
                    int y=Integer.parseInt(imageDatasY[i]);
                    int width=Integer.parseInt(imageDatasWidth[i]);
                    int height=Integer.parseInt(imageDatasHeight[i]);
                    if ((x<0)||(y<0)||((x+width)>backBitmap.getWidth())||(y+height)>backBitmap.getHeight()){
                        new  AlertDialog.Builder(MainActivity.this).setTitle("错误！").setMessage("前景图位置超出指定范围!").setPositiveButton("确定",null).show();
                        mixable=false;
                    }
                }
                if (mixable){
                    progressDialog.show();
                    getToString(fore_picture_num);
                    String imageData=getToLastString(imageDatas,fore_picture_num);
                    Log.e(TAG, "融合:"+imageData);
                    if (base64BackString=="123456"){//背景图片为空
                        new  AlertDialog.Builder(MainActivity.this).setTitle("错误！").setMessage("背景图片不能为空!").setPositiveButton("确定",null).show();
                    }else {
                        RequestBody requestBody=new FormBody.Builder()
                                .add("value",base64BackString)
                                .add("imageData",imageData)//提交的请求
                                .build();
                        HttpUtil.sendOkHttpRequest("http://10.108.125.20:8900/flaskr2/mixAndroid",requestBody,new Callback(){
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
                    Log.e(TAG, "融合背景图片:"+base64BackString);
                }
                break;
            default:
                break;
        }
        return true;
    }

    //前景图片手指移动指令
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int nCnt = event.getPointerCount();
        int n = event.getAction();
        int xlen,ylen;
        int viewLeft=v.getLeft();
        int viewRight=v.getRight();
        int viewTop=v.getTop();
        int viewBottom=v.getBottom();
        if (nCnt==2){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    for(int i=0; i< nCnt; i++)
                    {
                        float x = event.getX(i);
                        float y = event.getY(i);
                        Point pt = new Point((int)x, (int)y);
                    }
                    xlen = Math.abs((int)event.getX(0) - (int)event.getX(1));
                    ylen = Math.abs((int)event.getY(0) - (int)event.getY(1));
                    preLen=(float) Math.sqrt(xlen*xlen +  ylen * ylen);
                    break;
                case MotionEvent.ACTION_MOVE:
                    for(int i=0; i< nCnt; i++)
                    {
                        float x = event.getX(i);
                        float y = event.getY(i);
                        Point pt = new Point((int)x, (int)y);
                    }
                    xlen = Math.abs((int)event.getX(0) - (int)event.getX(1));
                    ylen = Math.abs((int)event.getY(0) - (int)event.getY(1));
                    nowLen=(float) Math.sqrt(xlen*xlen +  ylen * ylen);
                    if (nowLen>preLen){
                        v.layout(viewLeft-3,viewTop-3,viewRight+3,viewBottom+3);
                    }else if(nowLen<preLen){
                        v.layout(viewLeft+3,viewTop+3,viewRight-3,viewBottom-3);
                    }
                    preLen=(float) Math.sqrt(xlen*xlen +  ylen * ylen);
                    break;
                case MotionEvent.ACTION_UP:
                    imageViewLeft[v.getId()] = v.getLeft();
                    imageViewRight[v.getId()] = v.getRight();
                    imageViewTop[v.getId()] = v.getTop();
                    imageViewBottom[v.getId()] = v.getBottom();
                    imageDatasX[v.getId()] = String.valueOf(Calculate.RelativeStartX(backBitmap, showMode, v.getLeft(), viewWidth, viewHeight));
                    imageDatasY[v.getId()] = String.valueOf(Calculate.RelativeStartY(backBitmap, showMode, v.getTop(), viewWidth, viewHeight));
                    imageDatasWidth[v.getId()] = String.valueOf(Calculate.RelativeWidth(backBitmap, showMode, v.getLeft(), v.getRight(), viewWidth, viewHeight));
                    imageDatasHeight[v.getId()] = String.valueOf(Calculate.RelativeHeight(backBitmap, showMode, v.getTop(), v.getBottom(), viewWidth, viewHeight));
                    break;
            }
        }
        if ((nCnt==1)){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) event.getRawX() - lastX;
                    int dy = (int) event.getRawY() - lastY;
                    if ((dx>40)||(dx<-40))
                        dx=0;
                    if ((dy>40)||(dy<-40))
                        dy=0;
                    int left = v.getLeft() + dx;
                    int top = v.getTop() + dy;
                    int right = v.getRight() + dx;
                    int bottom = v.getBottom() + dy;
                    Log.e(TAG,"dx:"+dx+" dy:"+dy);
                    Log.i(TAG, " left = " + left + "  v.getTon=" + top + " ; event.getBottom = " + bottom + " ; right = " + right + " dx = " + dx);
                    Log.i(TAG, "ID:" + v.getId());
                    imageViewLeft[v.getId()] = left;
                    imageViewRight[v.getId()] = right;
                    imageViewTop[v.getId()] = top;
                    imageViewBottom[v.getId()] = bottom;
                    v.layout(left, top, right, bottom);
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    imageViewLeft[v.getId()] = v.getLeft();
                    imageViewRight[v.getId()] = v.getRight();
                    imageViewTop[v.getId()] = v.getTop();
                    imageViewBottom[v.getId()] = v.getBottom();
                    imageDatasX[v.getId()] = String.valueOf(Calculate.RelativeStartX(backBitmap, showMode, imageViewLeft[v.getId()], viewWidth, viewHeight));
                    imageDatasY[v.getId()] = String.valueOf(Calculate.RelativeStartY(backBitmap, showMode, imageViewTop[v.getId()], viewWidth, viewHeight));
                    imageDatasWidth[v.getId()] = String.valueOf(Calculate.RelativeWidth(backBitmap, showMode, imageViewLeft[v.getId()], imageViewRight[v.getId()], viewWidth, viewHeight));
                    imageDatasHeight[v.getId()] = String.valueOf(Calculate.RelativeHeight(backBitmap, showMode, imageViewTop[v.getId()], imageViewBottom[v.getId()], viewWidth, viewHeight));
                    break;
            }
        }
        return true;
    }

    public void parseJSONWithGSONCrop(String jsonData){
        Gson gson=new Gson();
        try {
            PhotoCrop photoCrop=gson.fromJson(jsonData,PhotoCrop.class);
            String resultString="http://10.108.125.20:8900/flaskr2/"+photoCrop.getResult();
            /*imageUri= Uri.parse(resultString);
            imageURL=new URL(resultString);
            bmpPath=imageUri.getPath();
            cropBitmap=HttpUtil.returnBitMap(resultString);*/
            Log.e(TAG,"融合图:"+resultString);
            progressDialog.dismiss();
            Intent lastintent=new Intent(MainActivity.this,LastActivity.class);
            lastintent.putExtra("result",resultString);
            startActivity(lastintent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //保存View为图片的方法
    public static void saveBitmap(View v, String name) {
        String fileName = name + ".png";
        Bitmap bm = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        v.draw(canvas);
        File f = new File(Environment.getExternalStorageDirectory(), name + ".png");
        //File f = new File("/sdcard/DCIM/",fileName);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i(TAG, "已经保存");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //获取iamgeData内部子数组
    public void getToString(int pictureNum){
        if (pictureNum<10){
            for (int i=0;i<pictureNum;i++){
                imageDatas[i]="{'id':"+imageDatasId[i]+","
                        + "'x1':"+imageDatasX[i]+","
                        + "'y':"+imageDatasY[i]+","
                        + "'width':"+imageDatasWidth[i]+","
                        + "'height':"+imageDatasHeight[i]+","
                        + "'rotate':0}";
            }
        }
    }
    public String getToLastString(String[] arrayData,int pictureNum) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (int i = 0 ; i < pictureNum; i++) {
            stringBuilder.append(arrayData[i]);
            if (i < pictureNum - 1) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

}
