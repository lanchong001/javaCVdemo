package com.lbx;


import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_videoio;
import org.bytedeco.javacv.*;

import javax.swing.*;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;

/*
基于javaCV实现收流器功能和录制功能
收流器实现，录制流媒体服务器的rtsp/rtmp视频文件(基于javaCV-FFMPEG)
 */
public class PopStream3 {

    /**
     * 按帧录制视频
     *
     * @param inputFile-该地址可以是网络直播/录播地址，也可以是远程/本地文件路径
     * @param outputFile                              -该地址只能是文件地址，如果使用该方法推送流媒体服务器会报错，原因是没有设置编码格式
     * @throws FrameGrabber.Exception
     * @throws FrameRecorder.Exception
     * @throws org.bytedeco.javacv.FrameRecorder.Exception
     */
    public static void frameRecord(String inputFile, String outputFile, int audioChannel) throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
        //该变量建议设置为全局控制变量，用于控制录制结束
        boolean isStart = true;

        // 获取视频源
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
        // 开始取视频源
        recordByFrame(grabber, outputFile, isStart);
    }

    private static void recordByFrame(FFmpegFrameGrabber grabber, String outputFile, Boolean status)
            throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {

        CanvasFrame canvas = new CanvasFrame("摄像头");//新建一个窗口
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas.setAlwaysOnTop(true);



        grabber.start();

        //转换器
        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();

        //抓取一帧视频并将其转换为图像，这个图像可以用来加水印或者人脸识别
        Frame frame1 = grabber.grab();
        opencv_core.IplImage iplImage = converter.convert(frame1);
        int width = iplImage.width();
        int height = iplImage.height();


        // 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）
        FFmpegFrameRecorder recorder = FFmpegFrameRecorder.createDefault(outputFile, width, height);

        recorder.setVideoOption("tune", "zerolatency");
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setOption("rtsp_transport","tcp");

        //设置视频编码  H264
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        //设置视频格式
        recorder.setFormat("flv");
        //设置视频帧率(FPS，赫兹)
        recorder.setFrameRate(25);


        try {//建议在线程中使用该方法
            recorder.start();
            Frame frame = null;
            int i=0;
            while (status && (frame = grabber.grabFrame()) != null) {

                recorder.record(frame);

                if (!canvas.isDisplayable()) {   //窗口是否关闭
                    grabber.stop();//停止抓取
                }

                //获取摄像头图像并放到窗口上显示， 这里的Frame frame=grabber.grab(); frame是一帧视频图像
                canvas.showImage(grabber.grab());

                i++;
                Thread.sleep(50);//50毫秒刷新一次图像
            }
            recorder.stop();
            grabber.stop();
        } finally {
            if (grabber != null) {
                grabber.stop();
            }
        }
    }


    public static void main(String[] args)
            throws Exception {

        String inputFile = "rtsp://admin:lbx@rd123456789@10.8.0.195:554/id=0";
//        // Decodes-encodes
//        String outputFile = "recorde.mp4";
//        frameRecord(inputFile, outputFile,1);

        //String inputFile = "E:\\Software\\4.h264";
        // Decodes-encodes
        String outputFile = "rtmp://192.168.1.134:1935/live/pushStream8";
        frameRecord(inputFile, outputFile, 1);
    }
}
