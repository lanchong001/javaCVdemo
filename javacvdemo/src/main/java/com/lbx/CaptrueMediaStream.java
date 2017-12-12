package com.lbx;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameGrabber;

/**
 * 收流器（支持实时流和本地摄像机）
 * @author eguid
 *
 */
public class CaptrueMediaStream {
	/**
	 * 获取屏幕尺寸
	 * @return
	 */
	public Rectangle getScreenSize(){
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();//本地环境
		Rectangle screenSize=ge.getMaximumWindowBounds();//获取当前屏幕最大窗口边界
		return screenSize;
	}
	public CanvasFrame getVideoFrame(String title,int width,int height){
		Rectangle screenSize=getScreenSize();
		CanvasFrame frame = new CanvasFrame(title);// javacv提供的图像展现窗口
		frame.setBounds((int) (screenSize.getWidth() - width) / 2, (int) (screenSize.getHeight() - height) / 2, width,
				height);// 窗口居中
		frame.setCanvasSize(width, height);// 设置CanvasFrame窗口大小
		return frame;
	}
	
	 public static boolean start(FrameGrabber grabber) {  
	        try {  
	            grabber.start();  
	            return true;  
	        } catch (Exception e2) {
	        	System.err.println("第一次打开失败，重新开始");
	            try {
	                grabber.restart(); 
	                
	                return true;  
	            } catch (Exception e) {
	                try {  
	                    System.err.println("重启抓取器失败，正在关闭抓取器...");  
	                    grabber.stop();  
	                } catch (Exception e1) {
	                    System.err.println("停止抓取器失败！");  
	                }  
	            }
	        }  
	        return false;  
	    }  

	    public static boolean stop(FrameGrabber grabber) {  
	        try {  
	            grabber.flush();  
	            grabber.stop();  
	            return true;  
	        } catch (Exception e) {
	            return false;  
	        } finally {  
	            try {  
	                grabber.stop();  
	            } catch (Exception e) {
	                System.err.println("关闭抓取器失败");  
	            }  
	        }  
	    }  
	    /**
	     * 流媒体收流器
	     * @param filename -媒体源（文件、rtsp、rtmp、hls等等）
	     */
	    public void captrue(String filename){
			int width = 800;
			int height = 600;
			FFmpegFrameGrabber grabber=new FFmpegFrameGrabber(filename);
			if(start(grabber)){
				CanvasFrame frame=getVideoFrame("收流",width,height);
				while (frame.isShowing()) {
					Frame img = null;
					try {
						img = grabber.grabImage();
						if(img!=null){
							frame.showImage(img);
							if(img.keyFrame){
								System.err.println("关键帧："+img.image);
							}
						}else{
							System.err.println("没有帧");
						}
					} catch (Exception e) {
						System.err.println("丢帧");
					}
				}
				frame.dispose();
				stop(grabber);
			}
		}
	    /**
	     * 摄像机视频采集
	     * @param camera
	     */
	    public void captrue(int camera){
	    	int width = 800;
			int height = 600;
			OpenCVFrameGrabber grabber=new OpenCVFrameGrabber(camera);
			if(start(grabber)){
				CanvasFrame frame=getVideoFrame("收流",width,height);
				while (frame.isShowing()) {
					Frame img = null;
					try {
						img = grabber.grab();
						if(img!=null){
							frame.showImage(img);
							if(img.keyFrame){
								System.err.println("关键帧："+img.image);
							}
						}else{
							System.err.println("没有帧");
						}
					} catch (Exception e) {
						System.err.println("丢帧");
					}
				}
				frame.dispose();
				stop(grabber);
			}
	    }
	public static void main(String[]args){
		//rtmp://live.hkstv.hk.lxdns.com/live/hks
		//new CaptrueMediaStream ().captrue("rtmp://10.13.200.206:1935/live/911000002");
		new CaptrueMediaStream ().captrue(0);
	}
}
