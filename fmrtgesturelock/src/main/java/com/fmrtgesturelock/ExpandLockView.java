package com.fmrtgesturelock;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Diosamo
 * @version V1.0
 * @Package com.sovnem.lockrelease
 * @Description: 手势解锁点对象
 * @author: LiHongCheng code72760525@163.com
 * @date 2017/11/1  19:23
 * 这个View的思想主要是:把判断是否选中的任务交给Point完成,并且Point自动更新自己的状态,然后LockView根据Point的状态,绘制相应的图标和手势轨迹.
 * LockView利用 ActionMode参数和 step参数判断当前的模式和应该进行的操作.
 *
 *   ActionMode    step     status
 *   0              1        设置初始密码,第一次输入
 *   0              2        设置初始密码,第二次输入
 *   1              NA       开锁
 *   2              1        重设密码,判断是否允许
 *   2              2        重设密码,第一次输入新密码
 *   2              3        重设密码,第二次输入新密码
 *
 * 使用方法: 只需要调用 setActionMode(ActionMode),然后LockView可自动进行相关处理.
 *
 * 注意事项:
 *  1.LockView被强制成正方形,高度被强制等于宽度,因此android:layout_width有效,而 android:layout_height无效.
 *  2.必须在xml中设置密码锁的图标
 *  3.必须实现接口 OnUpdateMessageListener 和 OnLockPanelListener,否则没有提示语和锁定功能
 *  3.可以在xml中制定提示文案(需补上相关代码).
 *  4.可以增加小指示盘,但需要实现接口OnUpdateIndicatorListener:根据pointTrace更新小指示盘
 *  5.隐藏轨迹功能
 */
public class ExpandLockView extends View {
    Context context;

    Point point0, point1, point2, point3, point4, point5, point6, point7, point8; //九个锁圈
    List<Point> points = new ArrayList<Point>(); //锁圈的集合

    List<Point> pointTrace = new ArrayList<Point>(); //手势轨迹

    Paint paintL; //绘制轨迹
    Drawable lock_unselected, lock_selected1,lock_selected2,lock_selected3, lock_error_selected;

    int currentX, currentY; //当前手指的坐标

    volatile boolean isReseted = false; //LockView的状态是否已经被重置为全部未选中状态

    String password; //手势密码

    //LockView的模式
    int ActionMode; //0,第一次设置密码;1,解锁;2,重设密码
    //当前操作处于第几步
    int step;

    int password_length_restriction; //密码最小长度为4

    int try_time_restriction; //密码最多允许尝试5次

    boolean isHiddenTrack; //是否隐藏轨迹


    int lock_trackColor;  //设置路径颜色
    private boolean isShowError;

    public void setShowError(boolean showError) {
        isShowError = showError;
    }

    public void setLock_trackColor(int lock_trackColor) {
        this.lock_trackColor = lock_trackColor;
        paintL.setColor(lock_trackColor);
    }
    //获取路径的paint
    public Paint getPaintL() {
        return paintL;
    }

    public void setHiddenTrack(boolean hiddenTrack) {
        isHiddenTrack = hiddenTrack;
    }


    public ExpandLockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public ExpandLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    public ExpandLockView(Context context) {
        super(context);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;



        //获取到三个bitmap
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandLockView,0,0);
        try{
            lock_selected1 = typedArray.getDrawable(R.styleable.ExpandLockView_lock_selected1);
            lock_selected2 = typedArray.getDrawable(R.styleable.ExpandLockView_lock_selected2);
            lock_selected3 = typedArray.getDrawable(R.styleable.ExpandLockView_lock_selected3);
            lock_error_selected = typedArray.getDrawable(R.styleable.ExpandLockView_lock_selected_error);
            lock_unselected = typedArray.getDrawable(R.styleable.ExpandLockView_lock_unselected);
            lock_trackColor=typedArray.getInt(R.styleable.ExpandLockView_lock_trackColor,0);
        }finally {
            typedArray.recycle();
        }
        //主要用于绘制轨迹
        paintL = new Paint();


        paintL.setAntiAlias(true);
        paintL.setStrokeWidth(5);
        if (lock_trackColor!=0)
        paintL.setColor(lock_trackColor); //初始化轨迹画笔

        //取手势密码,如果没有设置过密码,将取出空
        password = getPassword();

        //初始化LockView为开锁模式
        ActionMode = 1;
        step = 1;

        password_length_restriction = 4;
        try_time_restriction = 5;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int MeasuredWidth, MeasureHeight;

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

        //把LockView设置成一个正方形.使width = height.
        if (widthSpecMode == MeasureSpec.EXACTLY) {
            MeasuredWidth = widthSpecSize;
            MeasureHeight = MeasuredWidth;
        } else {  //如果没有指定明确的值,就用父View测量的值

            //获取LockView的margin值,后来用不到了.
//            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();

            MeasuredWidth = getMeasuredWidth();
            MeasureHeight = MeasuredWidth;
        }

        //测量好边长后,初始化Point
        initPoints(MeasuredWidth - getPaddingLeft() - getPaddingRight(), getPaddingLeft(), getPaddingRight());
        setMeasuredDimension(MeasuredWidth, MeasureHeight);
    }

    /**
     * 初始化九个Point
     *
     * @param sideLength, 正方形锁盘的边长
     * @param paddingLeft
     * @param paddingTop
     */
    private void initPoints(int sideLength, int paddingLeft, int paddingTop) {
        points.clear();
        point0 = new Point(0, sideLength, paddingLeft, paddingTop);
        point1 = new Point(1, sideLength, paddingLeft, paddingTop);
        point2 = new Point(2, sideLength, paddingLeft, paddingTop);
        point3 = new Point(3, sideLength, paddingLeft, paddingTop);
        point4 = new Point(4, sideLength, paddingLeft, paddingTop);
        point5 = new Point(5, sideLength, paddingLeft, paddingTop);
        point6 = new Point(6, sideLength, paddingLeft, paddingTop);
        point7 = new Point(7, sideLength, paddingLeft, paddingTop);
        point8 = new Point(8, sideLength, paddingLeft, paddingTop);

        points.add(point0);
        points.add(point1);
        points.add(point2);
        points.add(point3);
        points.add(point4);
        points.add(point5);
        points.add(point6);
        points.add(point7);
        points.add(point8);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isHiddenTrack) {
            drawLines(canvas, currentX, currentY);
        }
        drawPoints(canvas);

    }

    //绘制所有的Point
    private void drawPoints(Canvas canvas) {
        for (Point point : points) {
            lock_unselected.setBounds(point.getRect());
            lock_unselected.draw(canvas);
            if (point.getState() == 1) {
                if (!isHiddenTrack){
                    int index = point.getIndex()%3;
                    if (index==0) {
                        drawPointImage(lock_selected1,canvas, point);
                    }else if (index==1){
                        drawPointImage(lock_selected2,canvas, point);
                    }else {
                        drawPointImage(lock_selected3,canvas, point);
                    }
                }
            } else if (point.getState() == -1) {
                //  Error
                if (lock_error_selected!=null && isShowError){
                    lock_error_selected.setBounds(point.getRect());
                    lock_error_selected.draw(canvas);
                }
            } else if (point.getState()==0){

            }
        }
    }
    //画出对应的图片 没有指定默认使用icon图片
    private void drawPointImage(Drawable drawable,Canvas canvas, Point point) {
        if (drawable==null){
            drawable = getResources().getDrawable(R.mipmap.ic_launcher);
        }
        drawable.setBounds(point.getRect());
        drawable.draw(canvas);
    }

    //绘制轨迹
    private void drawLines(Canvas canvas, int currentX, int currentY) {
        if (pointTrace.size() > 0) {
            Point firstPoint = pointTrace.get(0);

            for (Point point : pointTrace) {
                    if (point.getState() ==-1) return;  //如果是错误视图直接什么线都不画
                    canvas.drawLine(firstPoint.getCenterX(), firstPoint.getCenterY(), point.getCenterX(), point.getCenterY(), paintL);
                    firstPoint = point;
            }

            canvas.drawLine(firstPoint.getCenterX(), firstPoint.getCenterY(), currentX, currentY, paintL);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //重绘LockView,更新UI

                if (!isReseted) {
                    resetLockView();
                    isReseted = true;
                }

                currentX = (int) event.getX();
                currentY = (int) event.getY();
                //判断当前的坐标是否击中了某个Point
                for (Point point : points) {
                    if (point.isIntersected(currentX, currentY)) {
                        pointTrace.add(point);
                        onUpdateMessageListener.vibration("short");
                        invalidate();
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                currentX = (int) event.getX();
                currentY = (int) event.getY();
                for (int i = 0; i < 9; i++) {
                    Point point = points.get(i);
                    if (point.isIntersected(currentX, currentY)) {
                        if (pointTrace.size() > 0) { //判断两点之间连成的直线,是否穿过中间的某个点,如果穿过,那么这个点也要被选中
                            Point middlePoint = checkPointInline(pointTrace.get(pointTrace.size() - 1), point);
                            if (middlePoint != null) {
                                middlePoint.setState(1);
                                pointTrace.add(middlePoint);
                                onUpdateMessageListener.vibration("short");
                            }
                        }
                        //把当前Point添加到轨迹中
                        pointTrace.add(point);
                        onUpdateMessageListener.vibration("short");
                        onUpdateMessageListener.vibration("short");
                        break;
                    }
                }

                if (pointTrace.size() > 0) {
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (pointTrace.size() > 0) {
                    Log.d("onDraw","run again");
                    if(null != onUpdateIndicatorListener){
                        onUpdateIndicatorListener.onUpdateIndicator();
                    }
                    if (ActionMode == 0) {//初设密码
                        initPassword();
                    } else if (ActionMode == 1) { //打开密码锁
                        openLockView();
                    } else if (ActionMode == 2) { //重设密码
                        resetPassword();
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    public interface OnFinishDrawPasswordListener{
        public void onSetPassword();
        public void onOpenLock();
    }

    private OnFinishDrawPasswordListener mFinishDrawPasswordListener;

    public void setOnFinishDrawPasswordListener(OnFinishDrawPasswordListener mFinishDrawPasswordListener){
        this.mFinishDrawPasswordListener = mFinishDrawPasswordListener;
    }

    //第一次设置密码
    private void initPassword() {
        String message = "";
        if (step == 1) {
            password = convertToPassword(pointTrace);
            if(password.length() >= password_length_restriction) {
                message = "再次输入密码";
                refreshLockView(100);
                step++;
            }else{
                message = "密码不能小于"+ String.valueOf(password_length_restriction)+"位";
                refreshLockView(100);
                onUpdateMessageListener.vibration("long");
            }
        } else if (step == 2) {
            if (password.equalsIgnoreCase(convertToPassword(pointTrace))) {
                message = "密码已设置";
                refreshLockView(100);
                putPassword(password);
                if(mFinishDrawPasswordListener != null){
                    mFinishDrawPasswordListener.onSetPassword();
                }
                ActionMode++; //切换到打开模式
                step--;
            } else {
                message = "密码不符,重新设置新密码";
                for (Point point : pointTrace) {
                    point.setState(-1);
                }
                paintL.setColor(Color.RED);
                refreshLockView(500);
                onUpdateMessageListener.vibration("long");
                step--;
            }
        }
        if(null != onUpdateMessageListener){
            onUpdateMessageListener.onUpdateMessage(message);
        }
    }

    //解锁
    private void openLockView() {
        String message = "";
        if (password.equalsIgnoreCase(convertToPassword(pointTrace))) {
            message = "密码正确";
            refreshLockView(100);
            //setVisibility(View.GONE);
            if(mFinishDrawPasswordListener != null){
                mFinishDrawPasswordListener.onOpenLock();
            }
            try_time_restriction = 5;
        } else {
            if(try_time_restriction>1) {
                message = "密码错误,还可尝试" + String.valueOf(--try_time_restriction) + "次";
                onUpdateMessageListener.vibration("long");
                for (Point point : pointTrace) {
                    point.setState(-1);
                }
                paintL.setColor(Color.RED);
                refreshLockView(2000);
            }else{
                if(null != onLockPanelListener) {
                    onLockPanelListener.onLockPanel(); //超过尝试次数,仍然没有输对,锁定密码锁
                }
                message = "密码盘已锁定";
                for (Point point : pointTrace) {
                    point.setState(-1);
                }
                paintL.setColor(Color.RED);
                refreshLockView(1000);
                onUpdateMessageListener.vibration("long");
                try_time_restriction = 5;
            }
        }

        if(null != onUpdateMessageListener){
            onUpdateMessageListener.onUpdateMessage(message);
        }
    }

    //重设密码,由解锁和设置密码组成.
    private void resetPassword() {
        String message = "";
        if (step == 1) {
            if (password.equalsIgnoreCase(convertToPassword(pointTrace))) {
                message = "密码正确,请输入新密码";
                refreshLockView(100);
                step++;
                try_time_restriction = 5;
            } else {
                if(pointTrace.size() < 4) {//轨迹小于四,弹出错误提示,但是不计入尝试次数
                    message = "密码错误";
                    for (Point point : pointTrace) {
                        point.setState(-1);
                    }
                    paintL.setColor(Color.RED);
                    refreshLockView(1000);
                    onUpdateMessageListener.vibration("long");
                }else if(try_time_restriction>1) {
                    message = "密码错误,还可尝试" + String.valueOf(--try_time_restriction) + "次";
                    for (Point point : pointTrace) {
                        point.setState(-1);
                    }
                    paintL.setColor(Color.RED);
                    refreshLockView(1000);
                    onUpdateMessageListener.vibration("long");
                }else{
                    if(null != onLockPanelListener) {
                        onLockPanelListener.onLockPanel(); //超过尝试次数,仍然没有输对,锁定密码锁
                    }
                    message = "密码盘已锁定,请1分钟以后再试";
                    onUpdateMessageListener.vibration("long");
                    for (Point point : pointTrace) {
                        point.setState(-1);
                    }
                    paintL.setColor(Color.RED);
                    refreshLockView(1000);
                    try_time_restriction = 5;
                }
            }
        } else if (step == 2) {
            password = convertToPassword(pointTrace);
            if(password.length() >= password_length_restriction) {
                message = "再次输入密码";
                refreshLockView(100);
                step++;
            }else{
                message = "密码不能小于"+ String.valueOf(password_length_restriction)+"位";
                onUpdateMessageListener.vibration("long");
                refreshLockView(100);
            }
        } else if (step == 3) {
            if (password.equalsIgnoreCase(convertToPassword(pointTrace))) {
                message = "密码已设置";
                refreshLockView(100);
                putPassword(password);
//                setVisibility(View.GONE);
                mFinishDrawPasswordListener.onSetPassword();
                ActionMode--; //切换到打开模式
                step = 1;
            } else {
                message = "密码不符,重新设置新密码";
                for (Point point : pointTrace) {
                    point.setState(-1);
                }
                paintL.setColor(Color.RED);
                onUpdateMessageListener.vibration("long");
                refreshLockView(500);
                step--;
            }
        }
        if(null != onUpdateMessageListener){
            onUpdateMessageListener.onUpdateMessage(message);
        }
    }

    //根据当前的状态,重绘LockView,更新UI
    private void refreshLockView(int time) {
        currentX = pointTrace.get(pointTrace.size() - 1).getCenterX();
        currentY = pointTrace.get(pointTrace.size() - 1).getCenterY();
        invalidate();
        isReseted = false;

        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isReseted) {
                    resetLockView();
                    isReseted = true;
                }
            }
        }, time);
    }

    //将轨迹转换成字符串密码
    private String convertToPassword(List<Point> pointList) {
        StringBuilder builder = new StringBuilder();
        for (Point point : pointList) {
            builder.append(point.getIndex());
        }
        Log.d("password", builder.toString());
        return builder.toString();
    }

    //重设所有Point的状态为未选中
    private void resetLockView() {
        pointTrace.clear();
        for (Point point : points) {
            point.reset();
        }
        invalidate();
    }

    //检查两点连成的直线,是否穿过了某个中间的点
    private Point checkPointInline(Point beginPoint, Point endPoint) {
        int sum = beginPoint.getIndex() + endPoint.getIndex();
        //如果和不是偶数,直接返回null
        if (sum % 2 == 0) {
            int index = sum / 2;
            //一条横线
            if ((beginPoint.getCenterY() == endPoint.getCenterY()) && (beginPoint.getCenterY()) == points.get(index).getCenterY()) {
                return points.get(index);
            }
            //一条竖线
            if ((beginPoint.getCenterX() == endPoint.getCenterX()) && (beginPoint.getCenterX()) == points.get(index).getCenterX()) {
                return points.get(index);
            }
            //一条斜线
            if (((endPoint.getCenterX() - points.get(index).getCenterX()) == (points.get(index).getCenterX() - beginPoint.getCenterX())) &&
                    ((endPoint.getCenterY() - points.get(index).getCenterY()) == (points.get(index).getCenterY() - beginPoint.getCenterY()))) {
                return points.get(index);
            }
        }
        return null;
    }

    public void saveActionMode(){
        SharedPreferences sp = context.getSharedPreferences("lock", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("actionmode",getActionMode());
        editor.commit();
    }

    public int retrieveActionMode(){
        SharedPreferences sp = context.getSharedPreferences("lock", MODE_PRIVATE);
        int action_mode = sp.getInt("actionmode",1);
        return action_mode;
    }

    //存储密码到SharedPreferences中,可重写成其他存储方式
    public void putPassword(String pw) {
        Log.d("dadada", "putPassword: " + pw);
        //TODO savepassword
        SPUtils.put(context, "gesturePw", pw);
        //取出存储在SharedPreferences中的密码,可重写成其他存储方式
    }
    public String getPassword() {
        String gesturePw = (String) SPUtils.get(context, "gesturePw", "12369");
        //TODO takepassword
//        SharedPreferencesUtils shUtils = SharedPreferencesUtils.getInstance(context);
//        String pw = shUtils.getShareP_gesturePWD();
        return gesturePw;
    }

    /**
     * 设置LockView的操作模式
     *
     * @param ActionMode 0, 代表LockView处于第一次设置密码模式;
     *                   1, 代表LockView处于开锁模式;
     *                   2, 代表LockView处于重设密码模式.
     *                   <p/>
     *                   ActionMode 与 step 合作,一起决定LockView的当前状态
     */
    public void setActionMode(int ActionMode) {
        this.ActionMode = ActionMode;
        this.step = 1;
    }

    /**
     *
     * @return 返回当前的ActionMode
     */
    public int getActionMode(){
        return this.ActionMode;
    }

    /**
     *
     * @return 返回当前的step
     */
    public int getStep(){
        return step;
    }

    /**
     * 接口,更新提示文案
     */
    public interface OnUpdateMessageListener{
        public abstract void onUpdateMessage(String message);

        public abstract void vibration(String time);
    }

    OnUpdateMessageListener onUpdateMessageListener;

    public void setOnUpdateMessageListener(OnUpdateMessageListener listener){
        this.onUpdateMessageListener = listener;
    }

    /**
     * 接口,当尝试解锁次数超过限定次数后,锁定密码盘,一定时间间隔内不让尝试.
     */
    public interface OnLockPanelListener{
        public abstract  void onLockPanel();
    }

    OnLockPanelListener onLockPanelListener;

    public void setOnLockPanelListener(OnLockPanelListener listener){
        this.onLockPanelListener = listener;
    }

    /**
     * 接口,更新密码锁的小指示盘.如果没有可以不设置.
     */
    public interface OnUpdateIndicatorListener{
        public abstract void onUpdateIndicator();
    }

    OnUpdateIndicatorListener onUpdateIndicatorListener;

    public void setOnUpdateIndicatorListener(OnUpdateIndicatorListener listener){
        this.onUpdateIndicatorListener = listener;
    }

    boolean isPanelLocked = false;

    /**
     * 判断密码盘是否被锁定
     * @return
     */
    public boolean getIsPanelLocked() {
        return isPanelLocked;
    }

    /**
     * 解锁/锁定密码盘
     * @param isPanelLocked
     */
    public void setIsPanelLocked(boolean isPanelLocked) {
        this.isPanelLocked = isPanelLocked;
    }


    /**
     * 获取密码盘被锁定的时长
     * @return
     */
    public long getLockTime(){
        SharedPreferences sp = context.getSharedPreferences("lock", MODE_PRIVATE);
        long locktime = sp.getLong("locktime",-1);
        return locktime;
    }

    /**
     * 设置密码盘被锁定的时间
     * @param time 单位:分钟
     */
    public void setLockTime(int time){
        long currentTime = System.currentTimeMillis();
        long unbanTime = currentTime + time * 60 * 1000;

        SharedPreferences sp = context.getSharedPreferences("lock", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("locktime", unbanTime);
        editor.commit();
    }

    /**
     * 把时间转换形式时分秒
     * @param time
     * @return
     */
    public String formatTime(long time){
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(time);
        String humanTime = dateFormat.format(date);
        return humanTime;
    }

    public List<Point> getPointTrace() {
        return pointTrace;
    }

    public void setPointTrace(List<Point> pointTrace) {
        this.pointTrace = pointTrace;
    }
}
