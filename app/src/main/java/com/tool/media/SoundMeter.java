package com.tool.media;

import android.media.MediaRecorder;
import android.os.Environment;

import java.io.IOException;

/**
 * Created by chenjianjun on 15-1-8.
 * <p/>
 * Beyond their own, let the future
 */
public class SoundMeter {

    static final private double EMA_FILTER = 0.6;

    private MediaRecorder mRecorder = null;
    private double mEMA = 0.0;

    /**
     * 开始录音
     * @param filepath 完整的文件存储路径
     * @return
     */
    public boolean start(String filepath) {

        if (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            return false;
        }

        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(filepath);

            try {
                mRecorder.prepare();
                mRecorder.start();

                mEMA = 0.0;
            } catch (IllegalStateException e) {
                System.out.print(e.getMessage());
                return false;
            } catch (IOException e) {
                System.out.print(e.getMessage());
                return false;
            }

        }

        return true;
    }

    public void stop() {

        try {
            if (mRecorder != null) {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }

//    public void pause() {
//        if (mRecorder != null) {
//            mRecorder.stop();
//        }
//    }
//
//    public void start() {
//        if (mRecorder != null) {
//            mRecorder.start();
//        }
//    }

    // 获取声音振幅
    public double getAmplitude() {
        if (mRecorder != null)
            return (mRecorder.getMaxAmplitude() / 2700.0);
        else
            return 0;

    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }
}
