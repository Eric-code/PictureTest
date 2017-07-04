package com.example.hebo.picturetest;

import android.content.Intent;
import android.net.Uri;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ChooseAreaFragment";
    private DrawerLayout mdrawerLayout;//滑动菜单
    public ImageView back_picture;
    int fore_picture_num=0;
    public Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        back_picture=(ImageView)findViewById(R.id.back_picture);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);//添加toolBar
        mdrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBar actionBar=getSupportActionBar();//设置toolbar上的导航按钮
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        final NavigationView navigationView=(NavigationView)findViewById(R.id.nav_view);//获取滑动菜单实例
        navigationView.setItemIconTintList(null);//设置每个图标为原来的颜色
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                //在这里处理item的点击事件
                item.setCheckable(true);//设置选项可选
                switch (item.getItemId()){
                    case R.id.backgroundpicture:
                        Log.e(TAG, "显示背景图片+"+item.getItemId());
                        Intent back_intent=new Intent(MainActivity.this,BackGroundActivity.class);
                        startActivity(back_intent);
                        item.setCheckable(false);//设置选项不可选
                        item.setChecked(false);
                        break;
                    case R.id.foregroundpicture:
                        Log.e(TAG, "显示前景图片+"+item.getItemId());
                        Intent fore_intent=new Intent(MainActivity.this,ForeGroundActivity.class);
                        startActivity(fore_intent);
                        item.setCheckable(false);//设置选项不可选
                        item.setChecked(false);
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
                if (fore_picture_num<10){
                    fore_picture_num++;
                    navigationView.getMenu().add(R.id.g1,R.id.foregroundpicture+fore_picture_num,1,"前景图片"+fore_picture_num).setIcon(R.drawable.foreground_picture);
                    //Toast.makeText(MainActivity.this,"添加前景+"+fore_picture_num,Toast.LENGTH_SHORT).show();
                }
            }
        });
        minus_btn.setOnClickListener(new View.OnClickListener() {//删除前景图片菜单项
            @Override
            public void onClick(View v) {
                if (fore_picture_num>0){
                    navigationView.getMenu().removeItem(R.id.foregroundpicture+fore_picture_num);
                    fore_picture_num--;
                   // Toast.makeText(MainActivity.this,"删除前景+"+fore_picture_num,Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                Toast.makeText(this,"delete",Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings:
                Toast.makeText(this,"settings",Toast.LENGTH_SHORT).show();
                break;
            default:
        }
        return true;
    }

}
