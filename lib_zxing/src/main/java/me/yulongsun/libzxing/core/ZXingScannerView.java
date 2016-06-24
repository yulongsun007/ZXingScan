package me.yulongsun.libzxing.core;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import me.yulongsun.libzxing.utils.DisplayUtils;


public class ZXingScannerView extends BarcodeScannerView {
    private static final String TAG = "ZXingScannerView";
    private DecodeThread mDecodeThread;


    private MultiFormatReader mMultiFormatReader;
    public static final List<BarcodeFormat> ALL_FORMATS = new ArrayList<BarcodeFormat>();
    private List<BarcodeFormat> mFormats;

    static {
        ALL_FORMATS.add(BarcodeFormat.UPC_A);
        ALL_FORMATS.add(BarcodeFormat.UPC_E);
        ALL_FORMATS.add(BarcodeFormat.EAN_13);
        ALL_FORMATS.add(BarcodeFormat.EAN_8);
        ALL_FORMATS.add(BarcodeFormat.RSS_14);
        ALL_FORMATS.add(BarcodeFormat.CODE_39);
        ALL_FORMATS.add(BarcodeFormat.CODE_93);
        ALL_FORMATS.add(BarcodeFormat.CODE_128);
        ALL_FORMATS.add(BarcodeFormat.ITF);
        ALL_FORMATS.add(BarcodeFormat.CODABAR);
        ALL_FORMATS.add(BarcodeFormat.QR_CODE);
        ALL_FORMATS.add(BarcodeFormat.DATA_MATRIX);
        ALL_FORMATS.add(BarcodeFormat.PDF_417);
    }

    public ZXingScannerView(Context context) {
        super(context);
        initMultiFormatReader();
    }

    public ZXingScannerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initMultiFormatReader();
    }

    public void setFormats(List<BarcodeFormat> formats) {
        mFormats = formats;
        initMultiFormatReader();
    }


    public Collection<BarcodeFormat> getFormats() {
        if (mFormats == null) {
            return ALL_FORMATS;
        }
        return mFormats;
    }

    private void initMultiFormatReader() {
        Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, getFormats());
        mMultiFormatReader = new MultiFormatReader();
        mMultiFormatReader.setHints(hints);

        mDecodeThread = new DecodeThread();
        mDecodeThread.start();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        try {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size       size       = parameters.getPreviewSize();
            int               width      = size.width;
            int               height     = size.height;

            if (DisplayUtils.getScreenOrientation(getContext()) == Configuration.ORIENTATION_PORTRAIT) {
                byte[] rotatedData = new byte[data.length];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++)
                        rotatedData[x * height + height - y - 1] = data[x + y * width];
                }
                int tmp = width;
                width = height;
                height = tmp;
                data = rotatedData;
            }

            PlanarYUVLuminanceSource source = buildLuminanceSource(data, width, height);

            if (source != null) {
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                //主线程向子线程发消息
                Message obtain = Message.obtain();
                obtain.obj = bitmap;
                DecodeThread.sHandler.sendMessage(obtain);
                mMultiFormatReader.reset();
            }

            camera.setOneShotPreviewCallback(this);
        } catch (RuntimeException e) {
            Log.e(TAG, e.toString(), e);
        }
    }


    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = getFramingRectInPreview(width, height);
        if (rect == null) {
            return null;
        }
        // Go ahead and assume it's YUV rather than die.
        PlanarYUVLuminanceSource source = null;

        try {
            source = new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                    rect.width(), rect.height(), false);
        } catch (Exception e) {
        }

        return source;
    }


    //子线程向住线程发送通知
    public static Handler mMainThread = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String resultStr = String.valueOf(msg.obj);
            mScanResultListener.handleResult(resultStr);
        }
    };


    public static scanResultListener mScanResultListener;

    public interface scanResultListener {
        void handleResult(String resultStr);
    }

    public void setOnScanResultListener(scanResultListener listener) {
        mScanResultListener = listener;
    }
}
