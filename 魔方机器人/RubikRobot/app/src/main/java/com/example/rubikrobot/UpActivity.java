package com.example.rubikrobot;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UpActivity extends Activity {
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_up);
        button1=findViewById(R.id.button1);
        button2=findViewById(R.id.button2);
        button3=findViewById(R.id.button3);
        button4=findViewById(R.id.button4);
        button5=findViewById(R.id.button5);
        button6=findViewById(R.id.button6);
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),
                R.mipmap.l1, null);
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(),
                R.mipmap.l2, null);
        Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(),
                R.mipmap.l3, null);
        Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(),
                R.mipmap.l4, null);
        Bitmap bitmap5 = BitmapFactory.decodeResource(getResources(),
                R.mipmap.l5, null);
        Bitmap bitmap6 = BitmapFactory.decodeResource(getResources(),
                R.mipmap.l6, null);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dealPhoto(bitmap1,1);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dealPhoto(bitmap2,2);
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dealPhoto(bitmap3,3);
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dealPhoto(bitmap4,4);
            }
        });
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dealPhoto(bitmap5,5);
            }
        });
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dealPhoto(bitmap6,6);
            }
        });

    }
    //处理并保存图像
    private File dealPhoto(Bitmap photo,int i){
        FileOutputStream fileOutputStream = null;
        //图片存放地址
        //File saveDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM); //保存到系统图库中
        File file = new File(getExternalCacheDir(),String.valueOf(i)+".jpg");  //在这个路径下生成这么一个文件生成这么一个文件
        try {
            fileOutputStream = new FileOutputStream(file);  //将本地图片读成流
            photo.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);   //保存图片到本地，100是压缩比率,表示100%压缩

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            if (photo != null && photo.isRecycled()){
                photo.recycle();   //释放内存
            }
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
