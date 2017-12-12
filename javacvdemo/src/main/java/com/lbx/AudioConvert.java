package com.lbx;

import java.nio.Buffer;
import java.nio.ShortBuffer;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.FrameRecorder.Exception;

/**
 * 音频参数转换（包含采样率、编码，位数，通道数）
 * 
 * @author eguid
 *
 */
public class AudioConvert {
	/**
	 * 通用音频格式参数转换
	 * 
	 * @param inputFile
	 *            -导入音频文件
	 * @param outputFile
	 *            -导出音频文件
	 * @param audioCodec
	 *            -音频编码
	 * @param sampleRate
	 *            -音频采样率
	 * @param audioBitrate
	 *            -音频比特率
	 */
	public static void convert(String inputFile, String outputFile, int audioCodec, int sampleRate, int audioBitrate,
			int audioChannels) {
		Frame audioSamples = null;
		// 音频录制（输出地址，音频通道）
		FFmpegFrameRecorder recorder = null;
		//抓取器
		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);

		// 开启抓取器
		if (start(grabber)) {
			recorder = new FFmpegFrameRecorder(outputFile, audioChannels);
			recorder.setAudioOption("crf", "0");
			recorder.setAudioCodec(audioCodec);
			recorder.setAudioBitrate(audioBitrate);
			recorder.setAudioChannels(audioChannels);
			recorder.setSampleRate(sampleRate);
			recorder.setAudioQuality(0);
			recorder.setAudioOption("aq", "10");
			// 开启录制器
			if (start(recorder)) {
				try {
					long startTime = 0;
					// 抓取音频
					while ((audioSamples = grabber.grab()) != null) {
						recorder.setTimestamp(grabber.getTimestamp());
						recorder.record(audioSamples);
					}

				} catch (FrameGrabber.Exception e1) {
					System.err.println("抓取失败");
				} catch (Exception e) {
					System.err.println("录制失败");
				}
				stop(grabber);
				stop(recorder);
			}
		}

	}

	public static boolean start(FrameGrabber grabber) {
		try {
			grabber.start();
			return true;
		} catch (FrameGrabber.Exception e2) {
			try {
				System.err.println("首次打开抓取器失败，准备重启抓取器...");
				grabber.restart();
				return true;
			} catch (FrameGrabber.Exception e) {
				try {
					System.err.println("重启抓取器失败，正在关闭抓取器...");
					grabber.stop();
				} catch (FrameGrabber.Exception e1) {
					System.err.println("停止抓取器失败！");
				}
			}

		}
		return false;
	}

	public static boolean start(FrameRecorder recorder) {
		try {
			recorder.start();
			return true;
		} catch (Exception e2) {
			try {
				System.err.println("首次打开录制器失败！准备重启录制器...");
				recorder.stop();
				recorder.start();
				return true;
			} catch (Exception e) {
				try {
					System.err.println("重启录制器失败！正在停止录制器...");
					recorder.stop();
				} catch (Exception e1) {
					System.err.println("关闭录制器失败！");
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
		} catch (FrameGrabber.Exception e) {
			return false;
		} finally {
			try {
				grabber.stop();
			} catch (FrameGrabber.Exception e) {
				System.err.println("关闭抓取器失败");
			}
		}
	}

	public static boolean stop(FrameRecorder recorder) {
		try {
			recorder.stop();
			recorder.release();
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			try {
				recorder.stop();
			} catch (Exception e) {

			}
		}
	}
	/**
	 * 抓取音频数据 ,16bit
	 */
	public static void grabAudio(String inputFile){
		Frame audioSamples = null;
		//抓取器
		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
		if(start(grabber)){
			try {
				while((audioSamples=grabber.grab()) != null)
				{
					Buffer[] buffer=audioSamples.samples;
					System.out.println(buffer.length);
					if(buffer.length==1)
					{
						ShortBuffer buff=(ShortBuffer)buffer[0];
						short[] data=new short[buff.capacity()];
						buff.get(data);
						int index=0;
						while(index<10)
						{
							System.out.println(data[index++]);
						}
					}
				}
			} catch (FrameGrabber.Exception e) {
				System.err.println("抓取失败");
			}
			stop(grabber);
		}
	}
	// 测试
	public static void main(String[] args) {
		//pcm参数转换
//		convert("东部信息.wav", "out.wav", avcodec.AV_CODEC_ID_PCM_S16LE, 8000, 16000,1);
		//pcm转mp3编码示例
//		convert("东部信息.wav", "out1.mp3", avcodec.AV_CODEC_ID_MP3, 8000, 16,1);
		grabAudio("out.wav");
	}
}
