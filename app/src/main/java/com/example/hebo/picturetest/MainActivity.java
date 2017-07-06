package com.example.hebo.picturetest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    private static final String TAG = "MainActivity";
    public static final int TAKE_PHOTO_MSG=0x123;
    public static final int CHOOSE_PHOTO_MSG=0x234;
    public static final int CROP_PHOTO_MSG=0x345;
    private DrawerLayout mdrawerLayout;//滑动菜单
    public ImageView back_picture,fore_picture;
    int fore_picture_item_num=0;//前景图片选项的数目
    int fore_picture_num=0;//前景图片的数目
    public Uri imageUri;
    public URL imageURL;
    public String imagePath=null;
    public static Handler revHandler;
    Canvas canvas;
    int lastX, lastY;
    boolean itemEable=false;
    final ImageView[] imageViews = new ImageView[10];
    NavigationView navigationView;

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

        navigationView=(NavigationView)findViewById(R.id.nav_view);//获取滑动菜单实例
        navigationView.setItemIconTintList(null);//设置每个图标为原来的颜色
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                //在这里处理item的点击事件
                //item.setCheckable(true);//设置选项可选
                Intent fore_intent=new Intent(MainActivity.this,ForeGroundActivity.class);
                switch (item.getItemId()){
                    case R.id.backgroundpicture:
                        Log.e(TAG, "显示背景图片+"+item.getItemId());
                        Intent back_intent=new Intent(MainActivity.this,BackGroundActivity.class);
                        startActivity(back_intent);
                        item.setCheckable(false);//设置选项不可选
                        item.setChecked(true);
                        break;
                    case R.id.foregroundpicture:
                    case R.id.foregroundpicture+1:
                    case R.id.foregroundpicture+2:
                    case R.id.foregroundpicture+3:
                    case R.id.foregroundpicture+4:
                    case R.id.foregroundpicture+5:
                    case R.id.foregroundpicture+6:
                    case R.id.foregroundpicture+7:
                    case R.id.foregroundpicture+8:
                    case R.id.foregroundpicture+9:
                    case R.id.foregroundpicture+10:
                        Log.e(TAG, "显示前景图片+"+item.getItemId());
                        startActivity(fore_intent);
                        item.setCheckable(false);//设置选项不可选
                        item.setChecked(true);
                        //item.setEnabled(false);
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
                    fore_picture_item_num--;
                   // Toast.makeText(MainActivity.this,"删除前景+"+fore_picture_num,Toast.LENGTH_SHORT).show();
                }
            }
        });

        revHandler=new Handler(){
            public void handleMessage(Message msg){
                switch (msg.what){
                    case TAKE_PHOTO_MSG://接收到背景界面拍摄得到的照片
                        imagePath=BackGroundActivity.imagePath;
                        //Bitmap bitmap=BitmapFactory.decodeFile(imagePath);
                        Bitmap bitmap=(Bitmap)msg.obj;
                        back_picture.setWillNotDraw(false);
                        back_picture.setImageBitmap(bitmap);
                        Log.e(TAG,"消息收到+"+imageUri);
                        /*try {
                            //imageUri=BackGroundActivity.imageUri;
                            //Bitmap bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }*/
                        break;
                    case CHOOSE_PHOTO_MSG://接收到背景界面从相册得到的图片
                        imagePath=BackGroundActivity.imagePath;
                        Bitmap bitmap1=BitmapFactory.decodeFile(imagePath);
                        back_picture.setImageBitmap(bitmap1);
                        back_picture.setWillNotDraw(false);
                        Log.e(TAG,"消息收到+"+imageUri);
                        break;
                    case CROP_PHOTO_MSG://接收到前景界面裁剪之后的图片
                        imagePath=ForeGroundActivity.bmpPath;
                        Bitmap bitmap2=BitmapFactory.decodeFile(imagePath);
                        //fore_picture.setImageBitmap(bitmap2);
                        ImageView mImageView = new ImageView(MainActivity.this);
                        imageViews[fore_picture_num]=mImageView;
                        //设置图片长度高度
                        /*RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);*/
                        mImageView.setLayoutParams(new DrawerLayout.LayoutParams(DrawerLayout.LayoutParams.WRAP_CONTENT, DrawerLayout.LayoutParams.WRAP_CONTENT));
                        mImageView.setImageBitmap(bitmap2);
                        mImageView.setWillNotDraw(false);
                        mImageView.setOnTouchListener(MainActivity.this);
                        if (fore_picture_num<10){
                            fore_picture_num++;
                            r.addView(mImageView);
                        }
                        Log.e(TAG,"消息收到+");
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
                back_picture.setWillNotDraw(true);
                //清除imageView中的图片
                if (fore_picture_num>0){
                    for (int i=0;i<fore_picture_num;i++){
                        imageViews[i].setWillNotDraw(true);
                    }
                }
                //删除前景图片的菜单选项
                if (fore_picture_item_num>0){
                    for (int i=fore_picture_item_num;i>0;i--){
                        navigationView.getMenu().removeItem(R.id.foregroundpicture+fore_picture_item_num);
                        fore_picture_item_num--;
                    }
                }
                Toast.makeText(this,"delete",Toast.LENGTH_SHORT).show();
                break;
            case R.id.save:
                Toast.makeText(this,"settings",Toast.LENGTH_SHORT).show();
                break;
            default:
        }
        return true;
    }

    //前景图片手指移动指令
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) event.getRawX() - lastX;
                int dy = (int) event.getRawY() - lastY;
                int left = v.getLeft() + dx;
                int top = v.getTop() + dy;
                int right = v.getRight() + dx;
                int bottom = v.getBottom() + dy;
                Log.i("life", " left = " + left + "  v.getLeft=" + v.getLeft()
                        + " ; event.getRawX = " + event.getRawX() + " ; lastX = "
                        + lastX + " dx = " + dx);
                v.layout(left, top, right, bottom);
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

}
