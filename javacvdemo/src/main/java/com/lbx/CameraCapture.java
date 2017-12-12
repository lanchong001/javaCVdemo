package com.lbx;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import static org.bytedeco.javacpp.opencv_core.cvReleaseImage;

public class CameraCapture {
    public static String savedImageFile = "E:\\WorkSpaces\\bigdatajavaCVdemo\\src\\resources\\my.jpg";

    //timer for image capture animation
    static class TimerAction implements ActionListener {
        private Graphics2D g;
        private CanvasFrame canvasFrame;
        private int width, height;

        private int delta = 10;
        private int count = 0;

        private Timer timer;

        public void setTimer(Timer timer) {
            this.timer = timer;
        }

        public TimerAction(CanvasFrame canvasFrame) {
            this.g = (Graphics2D) canvasFrame.getCanvas().getGraphics();
            this.canvasFrame = canvasFrame;
            this.width = canvasFrame.getCanvas().getWidth();
            this.height = canvasFrame.getCanvas().getHeight();
        }

        public void actionPerformed(ActionEvent e) {
            int offset = delta * count;
            if (width - offset >= offset && height - offset >= offset) {
                g.drawRect(offset, offset, width - 2 * offset, height - 2 * offset);
                canvasFrame.repaint();
                count++;
            } else {
                //when animation is done, reset count and stop timer.
                timer.stop();
                count = 0;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        //open camera source
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.start();

        //create a frame for real-time image display
        CanvasFrame canvasFrame = new CanvasFrame("Camera");
        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        opencv_core.IplImage image = converter.convert(grabber.grab());
        int width = image.width();
        int height = image.height();
        canvasFrame.setCanvasSize(width, height);


        //onscreen buffer for image capture
        final BufferedImage bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D bGraphics = bImage.createGraphics();

        //animation timer
        TimerAction timerAction = new TimerAction(canvasFrame);
        final Timer timer = new Timer(10, timerAction);
        timerAction.setTimer(timer);

        //click the frame to capture an image
        canvasFrame.getCanvas().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                timer.start(); //start animation
                try {
                    ImageIO.write(bImage, "jpg", new File(savedImageFile));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        //real-time image display
        while (canvasFrame.isVisible() && (grabber.grab()) != null) {
            if (!timer.isRunning()) { //when animation is on, pause real-time display
                image = converter.convert(grabber.grab());
                canvasFrame.showImage(grabber.grab());
                //draw the onscreen image simutaneously
                bGraphics.drawImage(iplToBufImgData(image), null, 0, 0);
            }
        }

        //release resources
        cvReleaseImage(image);
        grabber.stop();
        canvasFrame.dispose();
    }


    public static BufferedImage iplToBufImgData(opencv_core.IplImage mat) {
        if (mat.height() > 0 && mat.width() > 0) {
            BufferedImage image = new BufferedImage(mat.width(), mat.height(),
                    BufferedImage.TYPE_3BYTE_BGR);
            WritableRaster raster = image.getRaster();
            DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
            byte[] data = dataBuffer.getData();
            BytePointer bytePointer =new BytePointer(data);
            mat.imageData(bytePointer);
            return image;
        }
        return null;
    }
}