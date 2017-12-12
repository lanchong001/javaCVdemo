package com.lbx;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacv.*;

import javax.swing.*;

/**
 * 转流器实现（也可作为本地收流器、推流器，新增添加图片及文字水印，视频图像帧保存），实现rtsp/rtmp/本地文件转发到rtmp流媒体服务器(基于javaCV-FFMPEG)
 * 实现按帧取流和转发服务
 */
public class ConverterStream4 {

    /**
     * 转流器
     * @param inputFile
     * @param outputFile
     * @throws Exception
     * @throws org.bytedeco.javacv.FrameRecorder.Exception
     * @throws InterruptedException
     */
    public static void recordPush(String inputFile, String outputFile, int v_rs) throws FrameGrabber.Exception, FrameRecorder.Exception, InterruptedException {
        Loader.load(opencv_objdetect.class);
        long startTime = 0;
        FrameGrabber grabber = FFmpegFrameGrabber.createDefault(inputFile);

        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            try {
                grabber.restart();
            } catch (FrameGrabber.Exception e1) {
                throw e;
            }
        }


        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        Frame grabframe = grabber.grab();
        opencv_core.IplImage grabbedImage = null;
        if (grabframe != null) {
            System.out.println("取到第一帧");
            grabbedImage = converter.convert(grabframe);
        } else {
            System.out.println("没有取到第一帧");
        }

        //如果想要保存图片,可以使用 opencv_imgcodecs.cvSaveImage("hello.jpg", grabbedImage);来保存图片
        FrameRecorder recorder;
        try {
            recorder = FrameRecorder.createDefault(outputFile, 1280, 720);
        } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
            throw e;
        }
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264
        recorder.setFormat("flv");
        recorder.setFrameRate(v_rs);
        recorder.setGopSize(v_rs);
        System.out.println("准备开始推流...");
        try {
            recorder.start();
        } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
            try {
                System.out.println("录制器启动失败，正在重新启动...");
                if (recorder != null) {
                    System.out.println("尝试关闭录制器");
                    recorder.stop();
                    System.out.println("尝试重新开启录制器");
                    recorder.start();
                }

            } catch (org.bytedeco.javacv.FrameRecorder.Exception e1) {
                throw e;
            }
        }

        System.out.println("开始推流");
        CanvasFrame frame = new CanvasFrame("camera", CanvasFrame.getDefaultGamma() / grabber.getGamma());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        while (frame.isVisible() && (grabframe = grabber.grab()) != null) {
            System.out.println("推流...");
            frame.showImage(grabframe);
            grabbedImage = converter.convert(grabframe);
            Frame rotatedFrame = converter.convert(grabbedImage);

            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }
            recorder.setTimestamp(1000 * (System.currentTimeMillis() - startTime));//时间戳
            if (rotatedFrame != null) {
                recorder.record(rotatedFrame);
            }

            Thread.sleep(40);
        }
        frame.dispose();
        recorder.stop();
        recorder.release();
        grabber.stop();
        System.exit(2);
    }


    public static void main(String[] args)
            throws FrameRecorder.Exception, FrameGrabber.Exception, InterruptedException {

        String inputFile = "E:\\1.h264";

        String outputFile = "rtmp://192.168.1.192:1935/live/record2";

        recordPush(inputFile, outputFile, 25);
    }

}
