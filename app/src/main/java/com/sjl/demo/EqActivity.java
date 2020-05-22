package com.sjl.demo;


import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.sjl.equalizerview.EqualizerView;

import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename EqActivity.java
 * @time 2020/5/12 10:26
 * @copyright(C) 2020 song
 */
public class EqActivity extends AppCompatActivity {
    private static final String TAG = "EqActivity";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eq_activity);
        EqualizerView equalizerView1 = (EqualizerView) findViewById(R.id.equalizerView1);
        equalizerView1.setDbVal(0);
        EqualizerView equalizerView2 = (EqualizerView) findViewById(R.id.equalizerView2);
        /**
         * X轴值,dB
         */
        int[] yVal = new int[]{10, 5, 0, -5, -10};
        /**
         * Y轴值，Hz
         */
        int[] xVal = new int[]{100, 500, 1500, 5000, 10000};
        int[] yDefaultVal = new int[]{-10, 0, -8, 1, 5};
        equalizerView2.setXYData(xVal, yVal,yDefaultVal);
//        equalizerView2.reset();
        Map<Integer, Integer> dbAndHzMap1 = equalizerView1.getDbAndHzMap();
        Log.i(TAG,dbAndHzMap1.toString());
        System.out.println(dbAndHzMap1);
        Map<Integer, Integer> dbAndHzMap2 = equalizerView2.getDbAndHzMap();
        Log.i(TAG,dbAndHzMap2.toString());
    }


    public void openEq(View view) {
        MyDialog myDialog = new MyDialog(this);
        myDialog.show();
    }
}
