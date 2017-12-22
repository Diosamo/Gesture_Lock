package com.fmrtgesturelock;

import android.app.Service;
import android.graphics.Color;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
/**
 * Created by Diosamo
 *
 * @version V1.0
 * @Package com.sovnem.lockrelease
 * @Description: 界面布局使用的手势解锁控件的Activity
 * @author: LiHongCheng code72760525@163.com
 * @date 2017/11/1  18:54
 */
public class MainActivity extends AppCompatActivity implements ExpandLockView.OnLockPanelListener, ExpandLockView.OnUpdateIndicatorListener, ExpandLockView.OnUpdateMessageListener, ExpandLockView.OnFinishDrawPasswordListener {

    private ExpandLockView mLockviewExpand;
    private IndicatorLockView lockviewIndicator;
    private TextView tvMessage;
    private Animation mShakeAnimal;
    private Vibrator mVibrator;
    private double activityNum=0; //设置为0时禁止返回

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLockviewExpand = (ExpandLockView) findViewById(R.id.lockviewExpand);
        tvMessage = (TextView) findViewById(R.id.tvMessage);
        lockviewIndicator = (IndicatorLockView) findViewById(R.id.lockviewIndicator);
        mVibrator =(Vibrator)getApplication().getSystemService(Service.VIBRATOR_SERVICE); //震动

//        mLockviewExpand.getPaintL().setStrokeWidth(20); //获取paint 修改连接线段的样式
//        mLockviewExpand.setLock_trackColor(0xffff00ff); //给路径设置不同颜色

        //加载动画资源文件
        mShakeAnimal = AnimationUtils.loadAnimation(this, R.anim.shake);
//        mLockviewExpand.setActionMode(0);//set mode  设置手势密/码
        mLockviewExpand.setActionMode(1);//set mode  验证手势密码
//        mLockviewExpand.setActionMode(2);//set mode  更换手势密码


//        mLockviewExpand.setHiddenTrack(true); //隐藏轨迹和按钮
//        mLockviewExpand.setShowError(true); //显示失败视图





        //设置各种回调事件
        mLockviewExpand.setOnLockPanelListener(this);
        mLockviewExpand.setOnUpdateIndicatorListener(this);
        mLockviewExpand.setOnUpdateMessageListener(this);
        mLockviewExpand.setOnFinishDrawPasswordListener(this);
    }
    //密码盘被锁住
    @Override
    public void onLockPanel() {
        Toast.makeText(this, "onLockPanel", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpdateIndicator() {
        if (mLockviewExpand.getPointTrace().size() > 0) {
            lockviewIndicator.setPath(mLockviewExpand.getPointTrace());
        }
    }
    private String succeeMsg="再次输入密码,密码已设置,密码正确,密码正确,请输入新密码";
    @Override
    public void onUpdateMessage(String message) {
        if (succeeMsg.contains(message)){
            tvMessage.setTextColor(0xff434242);
        }else {//Error
            tvMessage.setTextColor(0xffe44d4d);
        tvMessage.startAnimation(mShakeAnimal); //动画效果
        }
        tvMessage.setText(message);
    }

    //vibration
    @Override
    public void vibration(String time) {
        if ("long".equals(time)){
            mVibrator.vibrate(new long[]{50,200},-1);//长震动
        }else {
            mVibrator.vibrate(new long[]{50,50},-1);//震动
        }
    }

    //设置密码成功
    @Override
    public void onSetPassword() {
        Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();
    }
    //解开密码锁成功
    @Override
    public void onOpenLock() {
        Toast.makeText(this, "解开密码锁成功", Toast.LENGTH_SHORT).show();
    }

    /* 禁止返回按钮的点击 */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && activityNum == 0) {
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

}
