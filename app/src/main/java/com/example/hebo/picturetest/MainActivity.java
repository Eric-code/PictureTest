package com.example.hebo.picturetest;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
import com.example.hebo.picturetest.JSON.HttpUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    public static final int BACK_PIC_CLICK=0x567;
    private DrawerLayout mdrawerLayout;//滑动菜单
    public ImageView back_picture,fore_picture;
    int fore_picture_item_num=0;//前景图片选项的数目
    int fore_picture_num=0;//前景图片的数目
    private int[] imageViewLeft={0,0,0,0,0,0,0,0,0,0};
    private int[] imageViewRight={100,100,100,100,100,100,100,100,100,100};
    private int[] imageViewTop={0,0,0,0,0,0,0,0,0,0};
    private int[] imageViewBottom={100,100,100,100,100,100,100,100,100,100};
    public Uri imageUri;
    public URL imageURL;
    public String imagePath=null;
    public static Handler revHandler;
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
                switch (item.getItemId()){
                    case R.id.backgroundpicture:
                        Log.e(TAG, "显示背景图片+"+item.getItemId());
                        Intent back_intent=new Intent(MainActivity.this,BackGroundActivity.class);
                        startActivity(back_intent);
                        item.setCheckable(false);//设置选项不可选
                        item.setChecked(true);
                        break;
                    case R.id.foregroundpicture+1:
                        Log.e(TAG,"前景界面1");
                        Intent fore_intent1=new Intent(MainActivity.this,Fore0Activity.class);
                        startActivity(fore_intent1);
                        item.setCheckable(false);//设置选项不可选
                        item.setChecked(true);
                        break;
                    case R.id.foregroundpicture:
                    case R.id.foregroundpicture+2:
                    case R.id.foregroundpicture+3:
                    case R.id.foregroundpicture+4:
                    case R.id.foregroundpicture+5:
                    case R.id.foregroundpicture+6:
                    case R.id.foregroundpicture+7:
                    case R.id.foregroundpicture+8:
                    case R.id.foregroundpicture+9:
                    case R.id.foregroundpicture+10:
                        Intent fore_intent=new Intent(MainActivity.this,ForeGroundActivity.class);
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
                    if (imageViews[fore_picture_item_num]!=null){
                        imageViews[fore_picture_item_num].setWillNotDraw(true);
                    }
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
                        Bitmap bitmap=BitmapFactory.decodeFile(imagePath);
                        //Bitmap bitmap=(Bitmap)msg.obj;
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
                        back_picture.setWillNotDraw(false);
                        back_picture.setImageBitmap(bitmap1);
                        Log.e(TAG,"消息收到+"+imageUri);
                        break;
                    case CROP_PHOTO_MSG://接收到前景界面裁剪之后的图片
                        imagePath=ForeGroundActivity.bmpPath;
                        Bitmap bitmap2 =BitmapFactory.decodeFile(imagePath) ;
                        ImageView mImageView = new ImageView(MainActivity.this);
                        imageViews[fore_picture_num]=mImageView;
                        //设置图片长度高度
                        mImageView.setLayoutParams(new DrawerLayout.LayoutParams(DrawerLayout.LayoutParams.WRAP_CONTENT, DrawerLayout.LayoutParams.WRAP_CONTENT));
                        mImageView.setImageBitmap(bitmap2);
                        mImageView.setWillNotDraw(false);
                        mImageView.setId(fore_picture_num);
                        mImageView.setOnTouchListener(MainActivity.this);
                        if (fore_picture_num<10) {
                            fore_picture_num++;
                            r.addView(mImageView);
                            for (int i = 0; i < fore_picture_num - 1; i++) {
                                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                layoutParams.setMargins(imageViewLeft[i], imageViewTop[i], imageViewRight[i], imageViewBottom[i]);
                                imageViews[i].setLayoutParams(layoutParams);
                                Log.e(TAG, "图片" + i + "已经重新放置");
                            }
                        }
                       /* imagePath=ForeGroundActivity.bmpPath;
                        Bitmap bitmap2=BitmapFactory.decodeFile(imagePath);*/
                        //fore_picture.setImageBitmap(bitmap2);

                        Log.e(TAG,"消息收");
                        break;
                    case BACK_PIC_CLICK:
                        imagePath=BackGroundActivity.bmpPath;
                        Bitmap bitmap3=BitmapFactory.decodeFile(imagePath);
                        back_picture.setWillNotDraw(false);
                        back_picture.setImageBitmap(bitmap3);
                        Log.e(TAG,"大图绘制成功");
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
                //saveBitmap(back_picture,"mixpic");


                Toast.makeText(this,"融合图片:"+Environment.getExternalStorageDirectory(),Toast.LENGTH_SHORT).show();
                Log.e(TAG, "融合图片:"+Environment.getExternalStorageDirectory());
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
                Log.i(TAG, " left = " + left + "  v.getLeft=" + v.getLeft() + " ; event.getRawX = " + event.getRawX() + " ; lastX = "
                        + lastX + " dx = " + dx);
                Log.e(TAG,"ID:"+v.getId());
                imageViewLeft[v.getId()]=left;
                imageViewRight[v.getId()]=right;
                imageViewTop[v.getId()]=top;
                imageViewBottom[v.getId()]=bottom;
                v.layout(left, top, right, bottom);
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
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

}
