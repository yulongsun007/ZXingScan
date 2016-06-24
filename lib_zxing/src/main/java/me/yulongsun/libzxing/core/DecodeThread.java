package me.yulongsun.libzxing.core;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;

import java.util.EnumMap;
import java.util.Map;


/**
 * Created by yulongsun on 16/3/31.
 * 解析图片线程
 */
public class DecodeThread extends Thread {

    private static final String            TAG                = DecodeThread.class.getSimpleName();
    private static       MultiFormatReader mMultiFormatReader = null;

    public DecodeThread() {
        Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, ZXingScannerView.ALL_FORMATS);
        mMultiFormatReader = new MultiFormatReader();
        mMultiFormatReader.setHints(hints);
    }

    public static Handler sHandler;

    @Override
    public void run() {
        super.run();
        Looper.prepare();
        sHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                BinaryBitmap bitmap = (BinaryBitmap) msg.obj;
                try {
                    Result rawResult = mMultiFormatReader.decodeWithState(bitmap);
                    Log.e(TAG, rawResult.getText());
                    Message message = Message.obtain();
                    message.obj = rawResult.getText();
                    ZXingScannerView.mMainThread.sendMessage(message);

                } catch (NotFoundException e) {
                    e.printStackTrace();
                }

            }
        };
        Looper.loop();

    }
}
