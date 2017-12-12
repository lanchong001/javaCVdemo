package com.lbx;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.*;

import javax.swing.*;

/*
采用按帧录制/推流，通过关闭播放窗口停止视频录制/推流
推流器实现，推本地摄像头视频到流媒体服务器以及摄像头录制视频功能实现(基于javaCV-FFMPEG、javaCV-openCV)
 */
public class PushStream2 {

    /**
     * 按帧录制本机摄像头视频（边预览边录制，停止预览即停止录制）
     *
     * @author eguid
     * @param outputFile -录制的文件路径，也可以是rtsp或者rtmp等流媒体服务器发布地址
     * @param frameRate - 视频帧率
     * @throws Exception
     * @throws InterruptedException
     * @throws org.bytedeco.javacv.FrameRecorder.Exception
     */
    public static void RecordCamera(String outputFile,double frameRate) throws FrameGrabber.Exception, FrameRecorder.Exception, InterruptedException {
        Loader.load(opencv_objdetect.class);

        //本机摄像头默认为0，这里使用javacv的抓取器
        FrameGrabber frameGrabber = FrameGrabber.createDefault(0);
        frameGrabber.start();

        //转换器
        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        //抓取一帧视频并将其转换为图像，这个图像可以用来加水印或者人脸识别
        opencv_core.IplImage iplImage = converter.convert(frameGrabber.grab());
        int width = iplImage.width();
        int height = iplImage.height();

        //视频记录器
        FrameRecorder frameRecorder = FrameRecorder.createDefault(outputFile,width,height);
        //设置视频编码  H264
        frameRecorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        //设置视频格式
        frameRecorder.setFormat("flv");
        //设置视频帧率(FPS，赫兹)
        frameRecorder.setFrameRate(frameRate);
        frameRecorder.start();

        long startTime = 0;
        long videoTS = 0;
        //创建画布窗口
        CanvasFrame canvasFrame = new CanvasFrame("camera",CanvasFrame.getDefaultGamma() / frameGrabber.getGamma());
        canvasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvasFrame.setAlwaysOnTop(true);
        Frame rotatedFrame = converter.convert(iplImage);

        while (canvasFrame.isVisible() && (iplImage = converter.convert(frameGrabber.grab())) != null) {
            rotatedFrame = converter.convert(iplImage);
            canvasFrame.showImage(rotatedFrame);
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }
            videoTS = 1000 * (System.currentTimeMillis() - startTime);
            frameRecorder.setTimestamp(videoTS);
            frameRecorder.record(rotatedFrame);
            Thread.sleep(40);
        }
        canvasFrame.dispose();
        frameRecorder.stop();
        frameRecorder.release();
        frameGrabber.stop();
    }

    public static void main(String[] args) throws Exception, InterruptedException, org.bytedeco.javacv.FrameRecorder.Exception {

        //RecordCamera("output.mp4",25);

        //将本地视频推送到流媒体服务器，进行直播输出
        RecordCamera("rtmp://192.168.1.192:1935/live/record1",25);
    }
}
