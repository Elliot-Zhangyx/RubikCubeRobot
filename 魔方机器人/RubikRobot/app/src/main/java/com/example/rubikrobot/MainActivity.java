/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.rubikrobot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;

import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.rubikrobot.Bluetooth.BluetoothChatService;
import com.example.rubikrobot.Bluetooth.DeviceListActivity;
import com.example.rubikrobot.Camera.ColorRecognition;
import com.example.rubikrobot.Solution.RubikRobot;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.Camera2Renderer;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.hardware.camera2.CameraDevice;

/**
 * This is the main Activity that displays the current chat session.
 */
@SuppressLint("NewApi") public class MainActivity extends Activity {
    // Debugging
    private static final String TAG = "MainActivity";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    //与屏幕和画笔有关的参数
    private int screenWidth;//屏幕宽度
    private int screenHeight;//屏幕高度
    private int locate_x;
    private int locate_y;
    private int lineWidth_x = 70;//中心十字x轴的长度
    private int lineWidth_y = 58;//中心十字x轴的长度
    WindowManager wm;

    private SurfaceView cameraSurface;
    private SurfaceView locateSurface;
    private Button link_bluetooth;
    // Layout Views
    private TextView mTitle;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    // Receive blubooth data
    String readMessage;
    byte[] readchars = new byte[10];

    // Take Photos
    private Camera camera;
    private Camera.Parameters parameters = null;
    Bundle bundle = null;

    private static int photo_order = 0;


    //ColorRecognition
   // ColorRecognition colorRecognition = new ColorRecognition();
    ColorRecognition colorRecognition = new ColorRecognition(MainActivity.this);
    //RubikSolution

    private double time1;
    private double time2;

    RubikRobot rubikrobot = new RubikRobot();
    SurfaceHolder holder;

    //测试相机拍照的时间,测试得到的拍照时间小的在100ms，大的在1500ms左右，建议2000ms
    private int time10;//开始时间
    private int time11;//结束时间

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // if (D) Log.e(TAG, "+++ ON CREATE +++");
        // Set up the window layout
      //  checkPermission();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_NO_TITLE, R.layout.custom_title);
       // System.out.println("filepath"+ getDiskCacheDir(MainActivity.this));
        // Set up the custom title
        initData();
            // Get local Bluetooth adapter
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            // If the adapter is null, then Bluetooth is not supported
            if (mBluetoothAdapter == null) {
                Toast.makeText(this, "蓝牙不可用", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            link_bluetooth = findViewById(R.id.link_bluetooth);
            link_bluetooth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopupMenu(view);
                }
            });
    }
    public void initData(){
        mTitle = findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle =findViewById(R.id.title_right_text);
        // checkPermission();
        //   wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        //  wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        //  screenWidth = wm.getDefaultDisplay().getWidth();
        //  screenHeight = wm.getDefaultDisplay().getHeight();
        // surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraSurface = findViewById(R.id.surfaceView1);
        // cameraSurface.getHolder().setFixedSize(1280, 720);
        cameraSurface.getHolder().setFixedSize(1024, 768);
        cameraSurface.getHolder().setKeepScreenOn(true);
        cameraSurface.getHolder().setFormat(PixelFormat.TRANSPARENT);
        cameraSurface.getHolder().addCallback(new SurfaceCallback());
        cameraSurface.setSecure(true);

        locateSurface = findViewById(R.id.surfaceView2);
        // locateSurface.getHolder().setFixedSize(1024, 768);
        locateSurface.getHolder().addCallback(new SurfaceCallback());
        locateSurface.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        locateSurface.setZOrderMediaOverlay(true);

        // 获取屏幕分辨率尺寸
        wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics display = getResources().getDisplayMetrics();
        screenWidth = display.widthPixels;
        screenHeight = display.heightPixels;
        System.out.println("宽" + screenWidth + "高" + screenHeight);
        //Toast.makeText(MainActivity.this,String.valueOf(screenWidth)+" "+String.valueOf(screenHeight),Toast.LENGTH_LONG).show();
        //计算预览界面和硬件平台的标定位置中心，这里屏幕中心
        locate_x = screenWidth / 2;
        locate_y = screenHeight / 2;
    }
    private void showPopupMenu(View view) {
        // View当前PopupMenu显示的相对View的位置
        PopupMenu popupMenu = new PopupMenu(this, view);
        // menu布局
        popupMenu.getMenuInflater().inflate(R.menu.option_menu, popupMenu.getMenu());
        // menu的item点击事件
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.scan:
                        // Launch the DeviceListActivity to see devices and do scan
                        Intent serverIntent = new Intent();
                        serverIntent.setClass(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                        return true;
                    case R.id.discoverable:
                        // Ensure this device is discoverable by others
                        ensureDiscoverable();
                        return true;
                }
                return false;
            }
        });
        // PopupMenu关闭事件
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                //Toast.makeText(getApplicationContext(), "关闭PopupMenu", Toast.LENGTH_SHORT).show();
            }
        });
        popupMenu.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_13, this, mLoaderCallback);
        }
        else{
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if (D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if (D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        if (D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessages(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            System.out.println(message);
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);

        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
            new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                    // If the action is a key-up event on the return key, send the message
                    if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                        String message = view.getText().toString();
                        //sendMessage(message);
                    }
                    if (D) Log.i(TAG, "END onEditorAction");
                    return true;
                }
            };
    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            mTitle.setText(R.string.title_connected_to);
                            mTitle.append(mConnectedDeviceName);
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            mTitle.setText(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);

                    break;
                case MESSAGE_READ: {
                    time1 = System.nanoTime();
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    int recog_time;
                    long t;
                    String color_result;
                    readMessage = new String(readBuf, 0, msg.arg1);
                    readchars = readMessage.getBytes();
                    if (readchars[0] == '1' || readchars[0] == '2' || readchars[0] == '3' || readchars[0] == '4' || readchars[0] == '5' || readchars[0] == '6') {
                        //camera.takePicture(null, null, new MyPictureCallback());
                        camera.autoFocus(new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean b, Camera camera) {
                                camera.takePicture(null, null, new MyPictureCallback());
                            }
                        });

                        //sendMessages(readMessage);

                    } else if (readchars[0] == '7') {
                        colorRecognition.Get_RubikTotalColor(0, 0);
                        color_result = rubikrobot.Test_Solution(colorRecognition.color_mask);
                        recog_time = 1;//颜色识别的次数
                        t = System.nanoTime();
                        System.out.println("这是"+color_result);
                        System.out.println("这是"+colorRecognition.color_mask);
                        if(color_result==null||color_result.equals("")){
                            color_result="success";
                            
                        }
                        while ((color_result.charAt(0) == 'E') && ((System.nanoTime() - t) < 5000 * 1.0e6))//5S
                        {
                            int x_bias = ((int) (Math.random() * 70)) - 35;//-30到30的随机数
                            int y_bias = ((int) (Math.random() * 70)) - 35;
                            colorRecognition.Get_ColorMask(x_bias, y_bias);
                        //  ColorRecognition.Analy_Error1();
                            color_result = rubikrobot.Test_Solution(colorRecognition.color_mask);
                            recog_time++;
                        }
                        if (color_result.charAt(0) != 'E') {
                            String send_string=rubikrobot.Get_Solution(colorRecognition.color_mask);
                            sendMessages(send_string);
                            //Toast.makeText(MainActivity.this ,"颜色识别结果："+colorRecognition.look_color()+"\r\n"+"颜色识别次数："+String.valueOf(recog_time)+"\r\n"+"解算公式："+send_string,Toast.LENGTH_LONG).show();
                            Toast.makeText(MainActivity.this ,"识别成功！\r\n"+"颜色识别次数："+String.valueOf(recog_time)+"\r\n"+"解算公式："+send_string+"\r\n",Toast.LENGTH_LONG).show();

                        } else {
                            //Toast.makeText(MainActivity.this ,"\r\n"+result+"\r\n"+ColorRecognition.rubiktotalcolor,Toast.LENGTH_LONG).show();
                            Toast.makeText(MainActivity.this, "经过" + String.valueOf(recog_time) + "次颜色识别，用时5S!!!!!\r\n" + colorRecognition.look_color(), Toast.LENGTH_LONG).show();
                        }

                    }
                    //Toast.makeText(MainActivity.this, "蓝牙接收到字符串" + readMessage, Toast.LENGTH_SHORT).show();
                    //sendMessages(search.solution(colorRecognition.Get_RubikTotalColor(), 21, 100, 0, 0));
                }
                break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "连接到 "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    System.out.println("ahifuasgiufgdsugfyuauykdgyufasuykftgyuasgfdsayutfyudsaftyusadgfyuadgfuay");
                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan:
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            case R.id.discoverable:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
            /*
            colorRecognition.GetPhoto_fromSD();
            long t1=System.nanoTime();
            String a=colorRecognition.Get_Colorstate(10, 10);
            long t3=(System.nanoTime()-t1)/1000;
            Toast.makeText(MainActivity.this,"\r\n"+String.valueOf(t3)+"uS",Toast.LENGTH_LONG).show();
            */
                return true;
        }
        return false;
    }

    /**
     *
     */

    private final class MyPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                bundle = new Bundle();
                bundle.putByteArray("bytes", data);
                saveToSDCard(data);
                camera.startPreview();
                time11 = (int) (System.nanoTime() / 1.0e6 - time10);
                // Toast.makeText(getApplicationContext(), String.valueOf(time11),Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     *
     */
    public void saveToSDCard(byte[] data) {
        /*
        Date date = new Date();

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String filename = format.format(date) + ".jpg";
        File fileFolder = new File(Environment.getExternalStorageDirectory()
                + "/finger/");
        if (!fileFolder.exists()) {
            fileFolder.mkdir();
        }
         File jpgFile = new File(fileFolder, filename);*/
        Change_Readchars();
        File jpgFile = new File(getExternalCacheDir(), String.valueOf(photo_order++) + ".jpg");
        //File jpgFile = new File("/storage/emulated/0/DCIM/Camera/ ", String.valueOf(photo_order++) + ".jpg");

        // compress - 压缩的意思

        try {
            FileOutputStream saveImgOut = new FileOutputStream(jpgFile);
            procRGB2HSV(data).compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut);
            saveImgOut.flush();
            saveImgOut.close();
        }catch (Exception ignored
        ){
        }
        //存储完成后需要清除相关的进程

        Log.d("Save Bitmap", "The picture is save to your phone!");


    }
    public Integer brightnessException ( byte[] data) {
        Mat srcImage = new Mat();
        Bitmap srcBitmap =  BitmapFactory.decodeByteArray(data,0,data.length);
        Utils.bitmapToMat(srcBitmap, srcImage);//convert original bitmap to Mat, R G B.
        Mat dstImage = new Mat();
        // 将RGB图转为灰度图
        Imgproc.cvtColor(srcImage,dstImage, Imgproc.COLOR_BGR2GRAY);
        float a=0;
        int Hist[] = new int[256];
        for(int i=0;i<256;i++) {
            Hist[i] = 0;
        }
        for(int i=0;i<dstImage.rows();i++)
        {
            for(int j=0;j<dstImage.cols();j++)
            {
                //在计算过程中，考虑128为亮度均值点
                a+=(float)(dstImage.get(i,j)[0]-128);
                int x=(int)dstImage.get(i,j)[0];
                Hist[x]++;
            }
        }
        float da =  a/(float)(dstImage.rows()*dstImage.cols());
        System.out.println(da);
        float D =Math.abs(da);
        float Ma=0;
        for(int i=0;i<256;i++)
        {
            Ma+=Math.abs(i-128-da)*Hist[i];
        }
        Ma/=(float)((dstImage.rows()*dstImage.cols()));
        float M=Math.abs(Ma);
        float K=D/M;
        float cast = K;
        System.out.printf("亮度指数： %f\n",cast);
        if(cast>=0.9) {
            System.out.printf("亮度：" + da);
            if(da > 0) {
                System.out.printf("过亮\n");
                return 2;
            } else {
                System.out.printf("过暗\n");
                return 1;
            }
        } else {
            System.out.printf("亮度：正常\n");
            return 0;
        }
    }
    /**
     * 用于整体偏暗图像的增强,变亮
     *
     *
     * @return
     */
    public  Bitmap laplaceEnhance(byte[] data) {
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        Bitmap srcBitmap = BitmapFactory.decodeByteArray(data,0,data.length);
        Bitmap grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Utils.bitmapToMat(srcBitmap, rgbMat);//convert original bitmap to Mat, R G B.
        Mat srcClone = rgbMat.clone();
        float[] kernel = {0, 0, 0, -1, 5f, -1, 0, 0, 0};
        Mat kernelMat = new Mat(3, 3, CvType.CV_32FC1);
        kernelMat.put(0, 0, kernel);
        Imgproc.filter2D(srcClone, srcClone, CvType.CV_8UC3, kernelMat);
        Utils.matToBitmap(srcClone, grayBitmap);
        return grayBitmap;
    }
    public Bitmap procRGB2HSV(byte[] data)  {
        Mat rgbMat = new Mat();
        Mat hsvMat = new Mat();
        //Bitmap srcBitmap =  BitmapFactory.decodeFile(getDiskCacheDir() +"/"+ String.valueOf(i) + ".jpg");
        Bitmap srcBitmap =  BitmapFactory.decodeByteArray(data,0,data.length);
        Bitmap hsvBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Utils.bitmapToMat(srcBitmap, rgbMat);//convert original bitmap to Mat, R G B.
        Imgproc.cvtColor(rgbMat, hsvMat, Imgproc.COLOR_RGB2HSV);//rgbMat to gray grayMat
        List<Mat> mv = new ArrayList<Mat>();// 分离出来的彩色通道数据
        Core.split(hsvMat, mv);// 分离色彩通道
        //mv.forEach(System.out::println);
        Mat h = mv.get(0);
        Mat s = mv.get(1);
        Mat v = mv.get(2);
        System.out.println(mv.get(0));

        Imgproc.equalizeHist(v,v);
        Mat dest =new Mat();
        Core.merge(mv, dest);// 合并split()方法分离出来的彩色通道数据
        Mat total = new Mat();
        //  Imgcodecs.imwrite("d:/" + System.currentTimeMillis() + ".jpg", dest);// 把合并通道后数据 输出到文件中
        Imgproc.cvtColor(dest, total, Imgproc.COLOR_HSV2RGB);
        //Imgproc.equalizeHist(rgbMat,grayMat);
        Utils.matToBitmap(total, hsvBitmap); //convert mat to bitmap
        return hsvBitmap;
    }
    public Bitmap autoHistEqualize(byte[] data) {
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        Bitmap srcBitmap =  BitmapFactory.decodeByteArray(data,0,data.length);
        Bitmap grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Utils.bitmapToMat(srcBitmap, rgbMat);//convert original bitmap to Mat, R G B.
        Mat dst = rgbMat.clone();
        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_BGR2YCrCb);
        List<Mat> list1 = new ArrayList<>();
        Core.split(dst, list1);
        CLAHE clahe = Imgproc.createCLAHE();
        clahe.setClipLimit(4);
        clahe.apply(list1.get(0), list1.get(0));
        Core.normalize(list1.get(0),list1.get(0),0,255,Core.NORM_MINMAX);
        Core.merge(list1, dst);
        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_YCrCb2BGR);
        Utils.matToBitmap(dst, grayBitmap); //convert mat to bitmap
        return grayBitmap;
    }

    public String getDiskCacheDir() {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = getExternalCacheDir().getPath();
        } else {
            cachePath = getCacheDir().getPath();
        }
        return cachePath;
    }
    private int getSurfaceId(SurfaceHolder holder) {
        if (holder.equals(cameraSurface.getHolder())) {
            return 1;
        } else if (holder.equals(locateSurface.getHolder())) {
            return 2;
        } else {
            return -1;
        }
    }

    private final class SurfaceCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                    camera = Camera.open();
                    camera.setPreviewDisplay(holder);
                    // camera.setDisplayOrientation(getPreviewDegree(MainActivity.this));
                    camera.startPreview();
                //tryDrawing(holder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            int id = getSurfaceId(holder);
           //camera.setDisplayOrientation(90);
            camera.setDisplayOrientation(getPreviewDegree(MainActivity.this));
            Surface surface = holder.getSurface();

            switch (id) {
                case 1: {
                    parameters = camera.getParameters();
                    parameters.setPictureFormat(PixelFormat.JPEG);

                    /*
                    //获取摄像头支持的PreviewSize列表
                    List<Camera.Size> supportedpreviewSizes = parameters.getSupportedPreviewSizes();
                    for (int i = 0; i < supportedpreviewSizes.size(); i++) {
                        Log.e("预览尺寸", " Supported Size. Width: " + supportedpreviewSizes.get(i).width + " height : " + supportedpreviewSizes.get(i).height);
                        Toast.makeText(MainActivity.this, "预览尺寸：\r\n"+"宽度:"+String.valueOf(supportedpreviewSizes.get(i).width)+"\r\n"+"高度："+String.valueOf(supportedpreviewSizes.get(i).height), Toast.LENGTH_LONG).show();
                    }

                    //获取摄像头支持的照片大小
                    List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
                    for (int i = 0; i < supportedPictureSizes.size(); i++) {

                        Log.e("照片尺寸", " Supported Size. Width: " + supportedPictureSizes.get(i).width + " height : " + supportedPictureSizes.get(i).height);
                        Toast.makeText(MainActivity.this, "图片尺寸：\r\n"+"宽度:"+String.valueOf(supportedPictureSizes.get(i).width)+"\r\n"+"高度："+String.valueOf(supportedPictureSizes.get(i).height), Toast.LENGTH_LONG).show();
                    }

                    */

                    // parameters.setPreviewSize(1280,720);
                    //parameters.setPreviewFrameRate(5);
                    parameters.setPictureSize(1024,768);
                    //Toast.makeText(MainActivity.this, "图片width为："+String.valueOf(width)+"图片height为："+String.valueOf(height)+"   ", Toast.LENGTH_LONG).show();
                    parameters.setJpegQuality(100);
                    // parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    camera.setParameters(parameters);
                    camera.startPreview();
                    //camera.cancelAutoFocus();
                    //camera.startPreview();
                    // List <int>supportedPreviewSizes = parameters.getSupportedPreviewSizes();
                }
                case 2: {
                    // 获取屏幕分辨率尺寸
                    //wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
                    screenWidth = wm.getDefaultDisplay().getWidth();
                    screenHeight = wm.getDefaultDisplay().getHeight();
                    System.out.println("高宽"+screenHeight+""+screenWidth);
                    //Toast.makeText(MainActivity.this, String.valueOf(screenWidth) + " " + String.valueOf(screenHeight), Toast.LENGTH_LONG).show();
                    //计算预览界面和硬件平台的标定位置中心，这里屏幕中心
                    locate_x = screenWidth / 2;
                    locate_y = screenHeight / 2;
                    //tryDrawing(holder);

                }
                break;

                default: {

                }
                break;

            }
        }


        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (camera != null) {
                camera.release();
                camera = null;
            }
        }
        private void tryDrawing(SurfaceHolder holder) {
            Log.i(TAG, "Trying to draw...");

            Canvas canvas = holder.lockCanvas();
            if (canvas == null)
            {
                Log.e(TAG, "Cannot draw onto the canvas as it's null");
            }
            else
            {

                Log.i(TAG, "Drawing...");

                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(Color.BLACK);
                paint.setStyle(Paint.Style.FILL);
                //paint.setShadowLayer(50 /2 + 1, 0, 0, Color.RED);
                //canvas.drawCircle(screenWidth/2,screenHeight/2, 50, paint);
                paint.setStrokeWidth(1); //画笔粗细
                canvas.drawLine(locate_x - locate_x, locate_y, locate_x + locate_x, locate_y, paint);
                canvas.drawLine(locate_x, locate_y - locate_y, locate_x, locate_y + locate_y, paint);
                holder.unlockCanvasAndPost(canvas);

            }

        }

    }

    public static int getPreviewDegree(Activity activity) {

        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degree = 0;
        System.out.println(rotation+"这是rotation");
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }

        return degree;
    }


    public void Change_Readchars() {
        switch (readchars[0]) {
            case '1': {
                photo_order = 1;
            }
            break;
            case '2': {
                photo_order = 2;
            }
            break;

            case '3': {
                photo_order = 3;
            }
            break;

            case '4': {
                photo_order = 4;
            }
            break;

            case '5': {
                photo_order = 5;
            }
            break;

            case '6': {
                photo_order = 6;
            }
            break;
            default: {

            }
            break;

        }

    }


    public boolean onTouchEvent(MotionEvent event) {
        int Action = event.getAction();

        //按下屏幕
        if (Action == MotionEvent.ACTION_DOWN)
        {

//            colorRecognition.GetPhoto_fromSD();  //读取SD中的6张图片信息
//            colorRecognition.Get_color_note(0, 0);
            Bitmap r = BitmapFactory.decodeFile(getDiskCacheDir() +"/"+ String.valueOf(1) + ".jpg");
            if(r==null){
                Toast.makeText(MainActivity.this,"请先进行拍照",Toast.LENGTH_SHORT).show();
                return false;
            }
            colorRecognition.Get_RubikTotalColor(0, 0);
            String result = rubikrobot.Test_Solution(colorRecognition.color_mask);

            int recog_time = 1;//颜色识别的次数
            long t = System.nanoTime();
            int x_bias = 0;
            int y_bias = 0;

            while ((result.charAt(0) == 'E') && ((System.nanoTime() - t) < 5000 * 1.0e6))//1S
            {
                x_bias = ((int) (Math.random() * 70)) - 35;//-35到35的随机数
                y_bias = ((int) (Math.random() * 70)) - 35;
                colorRecognition.Get_color_note(x_bias, y_bias);
                result = rubikrobot.Test_Solution(colorRecognition.color_mask);
                recog_time++;
            }

            //colorRecognition.Get_color_note(x_bias, y_bias);

            if (result.charAt(0) != 'E')//没有错误
            {
                //sendMessages(rubikrobot.Get_Solution(color_statement));
                String send_string = rubikrobot.Get_Solution(colorRecognition.color_mask);
                Toast.makeText(MainActivity.this, "颜色识别成功！\r\n" + "识别结果：" + colorRecognition.look_colo() + "\r\n" + "颜色识别次数：" + String.valueOf(recog_time) + "\r\n" + "解算公式：" + send_string + "\r\n" + "\r\n", Toast.LENGTH_LONG).show();
            }

            else
            {
                //Toast.makeText(MainActivity.this ,"\r\n"+result+"\r\n"+ColorRecognition.rubiktotalcolor,Toast.LENGTH_LONG).show();
                Toast.makeText(MainActivity.this, "颜色识别失败！\r\n" + "经过" + String.valueOf(recog_time) + "次颜色识别，用时5S！！\r\n" + colorRecognition.look_colo() + "\r\n", Toast.LENGTH_LONG).show();
            }



              /*  camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean b, Camera camera) {
                        camera.takePicture(null, null, new MyPictureCallback());
                    }
                });
               */

        }
        return true;
    }
    //OpenCV库加载并初始化成功后的回调函数
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            // TODO Auto-generated method stub
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Log.i(TAG, "成功加载");
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.i(TAG, "加载失败");
                    break;
            }
        }
    };

}



