package me.yulongsun.libzxing.core;

import android.graphics.Rect;

public interface IViewFinder {

    void setupViewFinder();

    Rect getFramingRect();

    int getWidth();

    int getHeight();
}
