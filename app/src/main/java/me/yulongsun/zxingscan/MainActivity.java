package me.yulongsun.zxingscan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import me.yulongsun.libzxing.core.ZXingScannerView;


public class MainActivity extends AppCompatActivity implements ZXingScannerView.scanResultListener {
    private static final int CAMERA_REQUEST_CODE = 1;


    private ZXingScannerView     mScannerView;
    private ListView             mLv;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String>    mDatas;


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        mScannerView = (ZXingScannerView) findViewById(R.id.fl_zxing);
        mLv = (ListView) findViewById(R.id.lv);
        mDatas = new ArrayList<>();
        mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mDatas);
        mLv.setAdapter(mAdapter);
        mScannerView.setOnScanResultListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        //申请CAMERA权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    CAMERA_REQUEST_CODE);
        } else {
            mScannerView.startCamera();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }


    /*6.0权限回调*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                mScannerView.startCamera();
            } else {
                // Permission Denied
                Toast.makeText(MainActivity.this, "获取摄像头权限失败", Toast.LENGTH_SHORT).show();
            }
        }

    }
    @Override
    public void handleResult(String thisCode) {
        //add
        if (!mDatas.contains(thisCode)) {
            mDatas.add(0, thisCode);
            mAdapter.notifyDataSetChanged();
        }
    }
}
