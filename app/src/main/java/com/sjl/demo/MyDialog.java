package com.sjl.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.sjl.equalizerview.EqualizerView;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename MyDialog.java
 * @time 2020/5/14 11:01
 * @copyright(C) 2020 song
 */
public class MyDialog extends AlertDialog {
    private EqualizerView equalizerView;
    public MyDialog(Context context) {
        super(context, R.style.CustomDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();

        Configuration mConfiguration = getContext().getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
            lp.gravity = Gravity.CENTER;
            lp.width = ViewUtils.dip2px(getContext(),650);//修复宽度问题
        } else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
            lp.gravity = Gravity.CENTER;
            lp.width = ViewUtils.dip2px(getContext(),300);//修复宽度问题
        }

        window.setAttributes(lp);
        setContentView(R.layout.eq_layout);

    }

}
