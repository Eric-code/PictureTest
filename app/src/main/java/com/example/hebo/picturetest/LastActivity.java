package com.example.hebo.picturetest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.hebo.picturetest.JSON.HttpUtil;

public class LastActivity extends AppCompatActivity {
    Bitmap bitmap;
    String data;
    Handler handler;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last);
        PublicWay.activityList.add(this); // 把这个界面添加到activityList集合里面

        Toolbar toolbar=(Toolbar)findViewById(R.id.last_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent=getIntent();
        data=intent.getStringExtra("result");

        Log.e("LastAcitivity","收到信息: "+data);

        imageView=(ImageView)findViewById(R.id.finalPicture);

        handler=new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0x789:
                        if (bitmap!=null){
                            imageView.setImageBitmap(bitmap);
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                bitmap= HttpUtil.returnBitMap(data);
                Message message=new Message();
                message.what=0x789;
                handler.sendMessage(message);
            }
        }).start();
    }

    //toolbar菜单命令
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.finaltoolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home://打开滑动菜单
                finish();
                break;
            case R.id.save:
                MainActivity.saveBitmap(imageView,"finalImage");
                Toast.makeText(LastActivity.this,"图片成功保存",Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
