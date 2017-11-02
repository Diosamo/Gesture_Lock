# Gesture_Lock
这是一个手势解锁的库，可以定制显示隐藏宫格点、路径、并且带有小九宫格显示图，和震动。喜欢的朋友star一下！

#### 添加的gradle
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}Copy
Step 2. Add the dependency

	dependencies {
	        compile 'com.github.Diosamo:Gesture_Lock:1.0'
	}

![alt](/IntroduceImage/take1.jpg "效果图1")
![alt](/IntroduceImage/take2.jpg "效果图2")
![alt](/IntroduceImage/take3.jpg "效果图3")

***
# 界面代码：  

### 使用简介：
小伙伴们直接把模版代码和布局copy到你们需要使用的界面在改运行开效果再改就行了

### 3种模式：
        mLockviewExpand.setActionMode(0);//set mode  设置手势密码
        mLockviewExpand.setActionMode(1);//set mode  验证手势密码
        mLockviewExpand.setActionMode(2);//set mode  更换手势密码
 对应修改3中业务流程，需要细改的小伙伴们下载项目自己在ExpandLockView 这个类里面改就行了
 
## 其他可以设置的api和接口回调在下面的代码都有注释：
        
```java  
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
        //初始化控件
        mLockviewExpand = (ExpandLockView) findViewById(R.id.lockviewExpand);
        tvMessage = (TextView) findViewById(R.id.tvMessage);
        lockviewIndicator = (IndicatorLockView) findViewById(R.id.lockviewIndicator);
        mVibrator =(Vibrator)getApplication().getSystemService(Service.VIBRATOR_SERVICE); //震动

//        mLockviewExpand.getPaintL().setStrokeWidth(20); //获取paint 修改连接线段的样式
        mLockviewExpand.setLock_trackColor(0xff04ff9b); //给路径设置不同颜色
        //加载动画资源文件
        mShakeAnimal = AnimationUtils.loadAnimation(this, R.anim.shake);
        
//        mLockviewExpand.setActionMode(0);//set mode  设置手势密码
//        mLockviewExpand.setActionMode(1);//set mode  验证手势密码
        mLockviewExpand.setActionMode(2);//set mode  更换手势密码


        mLockviewExpand.setHiddenTrack(true); //隐藏轨迹和按钮
        mLockviewExpand.setShowError(true); //显示失败视图
//        mLockviewExpand.setLockTime(2);//设置显示的锁住的时间

        //设置各种回调事件
        mLockviewExpand.setOnLockPanelListener(this);
        mLockviewExpand.setOnUpdateIndicatorListener(this);
        mLockviewExpand.setOnUpdateMessageListener(this);
        mLockviewExpand.setOnFinishDrawPasswordListener(this);
    }
    //密码盘被锁住发生的回调
    @Override
    public void onLockPanel() {

    }
    //更新小点显示图
    @Override
    public void onUpdateIndicator() {
        if (mLockviewExpand.getPointTrace().size() > 0) {
            lockviewIndicator.setPath(mLockviewExpand.getPointTrace());
        }
    }
    //返回信息如果是正确的
    private String succeeMsg="再次输入密码,密码已设置,密码正确,密码正确,请输入新密码";
    @Override
    public void onUpdateMessage(String message) {
        if (succeeMsg.contains(message)){
            tvMessage.setTextColor(0xff434242);//设置提示文字颜色
        }else {//Error
            tvMessage.setTextColor(0xffe44d4d);
        tvMessage.startAnimation(mShakeAnimal); //动画效果
        }
        tvMessage.setText(message);
    }

    //vibration 震动对应的接口
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
    public void onSetPassword() {}
    //解开密码锁成功
    @Override
    public void onOpenLock() {}

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

```

### xml布局

``` xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:background="#ffffff"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    >
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:orientation="vertical"
            android:gravity="center_horizontal"
            android:padding="50dp">
            <TextView
                android:textColor="#434242"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textSize="16sp"
                android:text="绘制图案"
                android:paddingBottom="10dp"/>
            <com.fmrtgesturelock.IndicatorLockView
                android:id="@+id/lockviewIndicator"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                app:indicator_selected="@drawable/indicator_selected"
                app:indicator_unselected="@drawable/indicator_unselected"
                android:layout_gravity="center_horizontal"/>

            <TextView
                android:id="@+id/tvMessage"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textSize="14sp"
                android:text=""
                android:paddingTop="20dp"
               />

            <RelativeLayout
                android:id="@+id/rl"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:paddingTop="30dp"
                >

                <com.fmrtgesturelock.ExpandLockView
                    android:id="@+id/lockviewExpand"
                    android:layout_width="280dp"
                    android:layout_height="280dp"
                    app:lock_selected1="@drawable/left"  //设置最左边图片
                    app:lock_selected2="@drawable/center" //设置最中间图片
                    app:lock_selected3="@drawable/right" //设置最右边图片
                    app:lock_trackColor="#04ff9b"        //设置轨迹颜色
                    app:lock_selected_error="@drawable/circle_error"  //设置错误图片
                    app:lock_unselected="@drawable/gusture_icon_default" //设置未选中图片
                    android:layout_gravity="center_horizontal"/>

            </RelativeLayout>
        </LinearLayout>
</RelativeLayout>

```

## 好了喜欢的小伙伴记得给星星哦！


