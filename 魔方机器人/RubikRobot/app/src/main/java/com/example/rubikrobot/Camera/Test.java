package com.example.rubikrobot.Camera;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
public class Test {

    public static void main(String[] args) {
        // 列表
        ArrayList array = new ArrayList();
        System.out.println("List: "+array);
       System.loadLibrary("opencv_java4.so");
        Mat image = new Mat();
        // 图像读取
        image = Imgcodecs.imread("C:\\Users\\86157\\Desktop\\1.jpg");
        // 图像行:高度height
        int img_rows = image.rows();
        // 图像列:宽度width
        int img_colums = image.cols();
        // 图像通道:维度dims/channels
        int img_channels = image.channels();
        System.out.println("image mat: " + image+"\n");
        System.out.println("image rows: "+image.rows()+"\n");
        System.out.println("image column: "+image.cols()+"\n");
        System.out.println("image channels: "+image.channels()+"\n");
        System.out.println("image value: "+image.get(0, 0).length+"\n");
        // 图像像素遍历,按通道输出
        for(int i=0;i<img_channels;i++) {
            for(int j=0;j<img_rows;j++){
                for(int k=0; k<img_colums;k++){
                    array.add(image.get(j,k)[i]);
                }
            }
            System.out.println("image value: "+array+"\n");
            // 列表清空
            array.clear();
        }
    }

}
