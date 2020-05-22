package com.sjl.equalizerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义EQ均衡器，支持手机和TV端
 *
 * @author Kelly
 * @version 1.0.0
 * @filename EqualizerView.java
 * @time 2020/5/12 10:35
 * @copyright(C) 2020 song
 */
public class EqualizerView extends View {
    private static final String TAG = "EqualizerView";
    private int mWidth, mHeight;
    private int mMinHeight = 600;
    private float circleTextSize;
    private int circleTextColor;
    /**
     * 圆圈半径大小
     */
    private float circleRadius;
    /**
     * x轴文本数字
     */
    private float xTextSize;
    private int xSelectColor, xUnSelectColor;


    /**
     * 左右边距
     */
    private int marginLR;

    /**
     * dB宽度
     */
    private int mDbSize;


    /**
     * dB X轴步长
     */
    private int xAxialStep;

    /**
     * 画笔
     */
    private Paint mPaint;

    /**
     * X轴值,dB
     */
    private int[] yAxialVal = new int[]{5, 0, -5, -10};
    /**
     * Y轴值，Hz
     */
    private int[] xAxialVal = new int[]{100, 500, 1500, 5000, 10000};
    /**
     * 当前选中的Hz Bar，-1表示未选中
     */
    private int currentSelectBarIndex = -1;
    /**
     * dB条数量
     */
    private int maxDbBarNum = 0;

    private Map<Integer, Integer> dBAndHzMap = new HashMap<>();

    private float startY;  //在屏幕上滑动调节dB时，开始的Y轴值
    private float touchRange;  //屏幕的高，因为涉及到横竖屏切换，到时候会取小的值
    /**
     * 当前点击选中的Hz对应的dB值
     */
    private int currentDb = 0;

    public EqualizerView(Context context) {
        this(context, null);
    }

    public EqualizerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public EqualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EqualizerView);

        circleTextSize = a.getDimension(R.styleable.EqualizerView_evCircleTextSize, dip2px(getContext(), 8));
        circleTextColor = a.getColor(R.styleable.EqualizerView_evCircleTextColor, Color.WHITE);
        circleRadius = a.getDimension(R.styleable.EqualizerView_svCircleRadius, 20);
        xTextSize = a.getDimension(R.styleable.EqualizerView_evXTextSize, dip2px(getContext(), 8));
        xSelectColor = a.getColor(R.styleable.EqualizerView_evXSelectColor, Color.parseColor("#81AA81"));
        xUnSelectColor = a.getColor(R.styleable.EqualizerView_evXUnSelectColor, Color.parseColor("#636363"));
        marginLR = a.getInt(R.styleable.EqualizerView_evLRMargin, 50);

        a.recycle();
        mPaint = new Paint();
        //设置画笔的颜色
        mPaint.setColor(Color.WHITE);
        //设置抗锯齿
        mPaint.setAntiAlias(true);
        initXY(xAxialVal, yAxialVal, new int[]{0, 0, 0, 0, 0});
        //下面使回调onkeydown事件
        setFocusableInTouchMode(true); //确保能接收到触屏事件
        setFocusable(true); //确保我们的View能获得输入焦点
    }

    private void initXY(int[] xAxialVal, int[] yAxialVal, int[] defaultYVal) {
        if (xAxialVal.length != defaultYVal.length) {
            throw new IllegalArgumentException("Y坐标值个数不匹配X坐标个数");
        }
        int y = yAxialVal[0] - yAxialVal[yAxialVal.length - 1];
        maxDbBarNum = y + 1;
        for (int i = 0; i < xAxialVal.length; i++) {
            dBAndHzMap.put(i, calculateDbBum(defaultYVal[i]) + 1);
        }
    }

    /**
     * 计算db数量块
     *
     * @param dbValue
     * @return
     */
    private int calculateDbBum(int dbValue) {
        int up = yAxialVal[0];
        int down = yAxialVal[yAxialVal.length - 1];
        int val;
        if (dbValue >= down && dbValue <= up) {
            val = dbValue - down;
        } else {
            throw new IllegalArgumentException("dB 值越界：" + dbValue);
        }
        return val;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width;
        int height;
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = widthSize * 1 / 2;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = heightSize * 1 / 2;
        }
        if (height < mMinHeight) {//适配大屏问题
            height = mMinHeight;
        }
        setMeasuredDimension(width, height);
    }

    //计算高度宽度
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = getWidth();
        mHeight = getHeight();
        int size = xAxialVal.length + 1;
        mDbSize = mWidth / (size * 3);//dB块宽度
        xAxialStep = mWidth / size;
        Log.i(TAG, "mWidth:" + mWidth + ",mHeight:" + mHeight + ",xAxialStep:" + xAxialStep + ",mSize:" + mDbSize);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(xTextSize);
        mPaint.setStyle(Paint.Style.FILL);
        float dB = mPaint.measureText("dB");

        //绘制dB单位
        canvas.drawText("dB", marginLR + circleRadius - dB / 2, marginLR, mPaint);


        int yStep = 150;
        int xStart = 20 + marginLR;
        int yTop = 20 + marginLR;
        //圆形距离顶部dB的开始距离
        int yStart = (int) (circleRadius + yTop);
        int startY = yStart, lastY = 0;

        //绘制Y轴坐标
        for (int i = 0; i < yAxialVal.length; i++) {
            mPaint.setStyle(Paint.Style.STROKE);
            //绘制圆弧
            canvas.drawCircle(xStart, yStart, circleRadius, mPaint);//40直径
            lastY = yStart;
            //绘制圆圈数字
            float v = mPaint.measureText(String.valueOf(yAxialVal[i]));
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(circleTextColor);
            mPaint.setTextSize(circleTextSize);
            //绘制圆圈数字
            canvas.drawText(String.valueOf(yAxialVal[i]), marginLR + circleRadius - v / 2, getBaseLineY((int) (2 * circleRadius)) - circleRadius + yStart, mPaint);
            //绘制直线
            canvas.drawLine(marginLR + 2 * circleRadius, yStart, getWidth() - marginLR, yStart, mPaint);
            yStart += yStep;

        }


        int xAxialStart = (int) (marginLR + 80 + 2 * circleRadius);
        //dB块总高度
        int dBTotalHeight = lastY - startY;
        /**
         * 单个dB块高度
         */
        int dBHeight = dBTotalHeight / (maxDbBarNum * 2 - 2);

        //绘制X轴坐标
        for (int i = 0; i < xAxialVal.length; i++) {
            int dbVal;
            if (i == currentSelectBarIndex) {//选中绿色
                dbVal = dBAndHzMap.get(currentSelectBarIndex);
                mPaint.setColor(xSelectColor);
            } else {//未选中的
                dbVal = dBAndHzMap.get(i);
                mPaint.setColor(xUnSelectColor);
            }

            //柱状dB绘制
            float v1 = xAxialStart;
            for (int j = 0; j < dbVal; j++) {
                //int left, int top, int right, int bottom
                int top = lastY + dBHeight / 2 - dBHeight - 2 * j * dBHeight;
                int bottom = lastY + dBHeight / 2 - 2 * j * dBHeight;
                canvas.drawRect(new Rect((int) (v1), top, (int) (mDbSize + v1), bottom), mPaint);
            }

            float v = mPaint.measureText(String.valueOf(xAxialVal[i]));
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setTextSize(xTextSize);

            //绘制底部文本
            int offsetX;
            if (v / 2 > mDbSize / 2) {//使得对准dB块中间
                offsetX = (int) -Math.abs(v / 2 - mDbSize / 2);
            } else {
                offsetX = (int) Math.abs(v / 2 - mDbSize / 2);
            }

            canvas.drawText(String.valueOf(xAxialVal[i]), v1 + offsetX, lastY + 2 * circleRadius, mPaint);
            xAxialStart += xAxialStep;

        }
        //绘制Hz单位
        mPaint.setColor(Color.WHITE);
        float v = mPaint.measureText("Hz");
        int lastX = xAxialStart - xAxialStep + mDbSize;//最左边dB的右侧坐标
        int hzOffset;
        if (getWidth() - marginLR - lastX - 50 > v) {
            hzOffset = getWidth() - marginLR - 50;
        } else {
            hzOffset = getWidth() - marginLR;
        }
        canvas.drawText("Hz", hzOffset, lastY + 2 * circleRadius, mPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        int x = (int) ev.getX();
        int y = (int) ev.getY();
        int left = (int) (marginLR + 80 + 2 * circleRadius);
        int top = 0;
        int length = xAxialVal.length;
        int right = left + mDbSize;
        int bottom = mHeight;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = ev.getY();
                touchRange = Math.min(mWidth, mHeight);
                for (int i = 0; i < length; i++) {
                    Rect rect = new Rect(left, top, right, bottom);
                    left += xAxialStep;
                    right = left + mDbSize;
                    //精确点击位置
                    if (rect.contains(x, y)) {
                        currentSelectBarIndex = i;
                        currentDb = dBAndHzMap.get(currentSelectBarIndex);  //获取滑动开始时的音量
                        invalidate();
                        break;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                updateDb(ev);
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return true;
    }

    private void updateDb(MotionEvent ev) {
        float endY = ev.getY();     //滑动的距离
        float distance = startY - endY;  //相对滑动的距离
        float changeDb = (distance / touchRange) * maxDbBarNum;  //改变的dB
        int dB = (int) Math.min(Math.max(currentDb + changeDb, 0), maxDbBarNum);  //改变后的dB
        Log.i(TAG, "onTouchEvent, changeDb:" + changeDb + ",dB:" + dB);
        if (changeDb != 0 && dB > 0) {
            dBAndHzMap.put(currentSelectBarIndex, dB);
            invalidate();
        }
    }

    /**
     * 获取基线y轴坐标
     *
     * @param circleR
     * @return
     */
    public int getBaseLineY(int circleR) {
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        float textTop = fontMetrics.top;
        float textBottom = fontMetrics.bottom;
        float contentBottom = circleR / 2;
        int baseLineY = (int) (contentBottom - textTop / 2 - textBottom / 2);
        return baseLineY;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 向做移动音量条
     */
    public void moveLeft() {
        if (currentSelectBarIndex == 0) {
            return;
        }
        currentSelectBarIndex--;
        invalidate();
    }

    /**
     * 向右移动音量条
     */
    public void moveRight() {
        if (currentSelectBarIndex == xAxialVal.length - 1) {
            return;
        }
        currentSelectBarIndex++;
        invalidate();
    }

    /**
     * 增加音量
     */
    public void moveUp() {
        if (currentSelectBarIndex == -1) {
            return;
        }
        int defaultYVal = dBAndHzMap.get(currentSelectBarIndex);
        if (defaultYVal == maxDbBarNum) {
            return;
        }
        defaultYVal++;
        dBAndHzMap.put(currentSelectBarIndex, defaultYVal);
        invalidate();
    }

    /**
     * 降低音量
     */
    public void moveDown() {
        if (currentSelectBarIndex == -1) {
            return;
        }
        int defaultYVal = dBAndHzMap.get(currentSelectBarIndex);
        if (defaultYVal == 1) {//保留最后一隔音量
            return;
        }
        defaultYVal--;
        dBAndHzMap.put(currentSelectBarIndex, defaultYVal);
        Log.i(TAG, "触发了moveDown");
        invalidate();
    }


    /**
     * 设置默认选中的柱状条
     *
     * @param currentSelectBarIndex
     */
    public void setCurrentSelectBarIndex(int currentSelectBarIndex) {
        this.currentSelectBarIndex = currentSelectBarIndex;
        invalidate();
    }

    /**
     * 整体修改
     *
     * @param dbValue 指定值，不能超出y轴范围,可以为正负
     */
    public void setDbVal(int dbValue) {
        int val = calculateDbBum(dbValue);
        for (int i = 0; i < xAxialVal.length; i++) {
            dBAndHzMap.put(i, val + 1);
        }
        invalidate();
    }


    /**
     * 重置
     */
    public void reset() {
        setDbVal(0);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP://向上
                Log.e(TAG, "－－－－－向上－－－－－");
                moveUp();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN://向下
                Log.e(TAG, "－－－－－向下－－－－－");
                moveDown();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT://向左
                Log.e(TAG, "－－－－－向左－－－－－");
                moveLeft();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT://向右
                Log.e(TAG, "－－－－－向右－－－－－");
                moveRight();
                break;
            case KeyEvent.KEYCODE_ENTER://确定
                Log.e("EqualizerView", "－－－－－确定－－－－－");
                break;
            case KeyEvent.KEYCODE_BACK://返回
                Log.e(TAG, "－－－－－返回－－－－－");
                break;
            case KeyEvent.KEYCODE_HOME://房子
                Log.e(TAG, "－－－－－房子－－－－－");
                break;
            case KeyEvent.KEYCODE_MENU://菜单
                Log.e(TAG, "－－－－－菜单－－－－－");
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 设置数据
     *
     * @param xVal        X坐标
     * @param yVal        y坐标
     * @param defaultYVal y坐标默认值
     */
    public void setXYData(int[] xVal, int[] yVal, int[] defaultYVal) {
        this.xAxialVal = xVal;
        this.yAxialVal = yVal;
        int size = xVal.length + 1;
        mDbSize = mWidth / (size * 3);//dB块宽度
        xAxialStep = mWidth / size;
        initXY(xVal, yVal, defaultYVal);
        invalidate();
    }


    /**
     * 设置Y坐标值数据
     *
     * @param defaultYVal y坐标默认值
     */
    public void setYVal(int[] defaultYVal) {
        initXY(this.xAxialVal, this.yAxialVal, defaultYVal);
        invalidate();
    }

    /**
     * 获取d和Hz映射值
     *
     * @return
     */
    public Map<Integer, Integer> getDbAndHzMap() {
        int down = yAxialVal[yAxialVal.length - 1];
        Map<Integer, Integer> temp = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : dBAndHzMap.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            int realVal = value + down - 1;
            temp.put(key, realVal);
        }
        return temp;
    }


}