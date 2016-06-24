###对于barcodescan的改进
https://github.com/dm77/barcodescanner

###ZXing版本
   compile 'com.google.zxing:core:3.2.1'
   
###问题：
扫描之后到返回之前出现卡顿现象

###问题解决：
1.主进程扫描获取图片
2.子线程解析图像，将结果返回给主线程
扫描解析是时间控制在100ms以内

###使用：
1.布局设置
```xml
    <me.yulongsun.libzxing.core.ZXingScannerView
        android:id="@+id/fl_zxing"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```

2.设置监听
```java
    mScannerView = (ZXingScannerView) findViewById(R.id.fl_zxing);
    mScannerView.setOnScanResultListener(this);
```

3.
```java

    @Override
    public void handleResult(String result) {
           //todo
            
    }
```