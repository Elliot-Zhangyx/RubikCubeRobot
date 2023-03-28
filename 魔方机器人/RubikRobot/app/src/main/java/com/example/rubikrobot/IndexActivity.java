package com.example.rubikrobot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class IndexActivity extends Activity {
    private Button button;
    private Button alert_root;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_index);
        final RippleBackground rippleBackground=findViewById(R.id.content);
        final Handler handler=new Handler(Looper.myLooper());
        //foundDevice=(ImageView)findViewById(R.id.foundDevice);
        button=findViewById(R.id.centerImage);
        alert_root=findViewById(R.id.alert_root);
        rippleBackground.startRippleAnimation();
        Intent intent=new Intent();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });
        String path=getDiskCacheDir(this);
        alert_root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(IndexActivity.this,path,Toast.LENGTH_SHORT).show();
                //Toast.makeText(MainActivity.this,"拒绝通过",Toast.LENGTH_SHORT).show();
            }
        });

    }
    public static String getDiskCacheDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }
    //检测用户是否同意权限
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
        }
        else {
            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                //同意申请的权限
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"同意通过",Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(this,MainActivity.class);
                    startActivity(intent);
                    //拒绝申请的权限
                }else{
                    Toast.makeText(this,"拒绝通过",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }

    }
}