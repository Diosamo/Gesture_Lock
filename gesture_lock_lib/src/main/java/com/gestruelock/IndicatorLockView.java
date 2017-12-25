package com.gestruelock;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.gesturelock.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Diosamo
 * @version V1.0
 * @Package com.sovnem.lockrelease
 * @Description: 展现九宫格手势选中点的View
 * @author: LiHongCheng code72760525@163.com
 * @date 2017/11/1  18:48
 */
public class IndicatorLockView extends View {
    List<IndicatorPoint> points;
    Drawable pointSelected;
    Drawable pointUnselected;
    Context context;

    public IndicatorLockView(Context context) {
        super(context);
    }

    public IndicatorLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context,attrs);
    }

    public IndicatorLockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs){
        points = new ArrayList<IndicatorPoint>();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IndicatorLockView,0,0);
        try {
            pointSelected = typedArray.getDrawable(R.styleable.IndicatorLockView_indicator_selected);
            pointUnselected = typedArray.getDrawable(R.styleable.IndicatorLockView_indicator_unselected);
        }finally {
            typedArray.recycle();
        }
        this.context = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width,height;

        if(widthMode == MeasureSpec.EXACTLY){
            width = height = MeasureSpec.getSize(widthMeasureSpec);
        }else{
            width = height = dp2px(context,50);
        }

        //这里有个坑,在初始化的时候,measure函数会被多次调用
        initPoints(width);

        setMeasuredDimension(width,height);

    }

    private void initPoints(int width){
        //坑,当隐藏或者显示大密码锁盘时,父控件会重新测量布局,所以会再次调用子view的onMeasure()方法
        if(points.isEmpty()) { //保证只有初始化控件的时候才初始化list,其他情况不再重新初始化
            for (int i = 0; i < 9; i++) {
                points.add(new IndicatorPoint(i, width, getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom()));
            }
        }
        Log.d("onDraw","initPoints");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for(IndicatorPoint point:points){
            if(0 == point.getStatus()){
                pointUnselected.setBounds(point.getRectF());
                pointUnselected.draw(canvas);
                Log.d("onDraw","indicator unselected "+ String.valueOf(point.getIndex()));
            }else{
                pointSelected.setBounds(point.getRectF());
                pointSelected.draw(canvas);
                Log.d("onDraw","indicator selected "+ String.valueOf(point.getIndex()));
            }
        }
        Log.d("onDraw","indicator的数量 "+ String.valueOf(points.size()));
    }

    public void setPath(List<Point> pointTrace){
        for(IndicatorPoint point:points){
            point.setStatus(0);
        }
        for(Point point:pointTrace){
            points.get(point.getIndex()).setStatus(1);
        }
        Log.d("onDraw","pointTrace size "+ String.valueOf(pointTrace.size()));
        Log.d("onDraw","run setPath");
        for(IndicatorPoint point:points){
            Log.d("onDraw","indicator status "+ String.valueOf(point.getStatus()));
        }
        invalidate();
    }

    private int dp2px(Context context, int length){
        if (context == null) {
            return length;
        }
        return (int) (length * context.getResources().getDisplayMetrics().density + 0.5f);
    }

}
