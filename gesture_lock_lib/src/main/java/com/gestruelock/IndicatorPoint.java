package com.gestruelock;

import android.graphics.Rect;

/**
 * Created by Diosamo
 * @version V1.0
 * @Package com.sovnem.lockrelease
 * @Description: 展现图点对象
 * @author: LiHongCheng code72760525@163.com
 * @date 2017/11/1  18:34
 */
public class IndicatorPoint {
    int index;
    float diameter;
    float centerX;
    float centerY;
    int width;
    float side;
    float gab;
    float radius;

    int status;

    Rect rectF;

    public IndicatorPoint(int index, int width, int paddingLeft, int paddingTop, int paddingRight, int paddingDown){
        init(index, width, paddingLeft, paddingTop, paddingRight, paddingDown);
    }

    private void init(int index,int width,int paddingLeft,int paddingTop,int paddingRight,int paddingDown){
        this.index = index;
        this.width = width;
        int w = width - paddingLeft - paddingRight;
        int h = width - paddingTop - paddingDown;
        side = Math.min(w,h);

        diameter = 3*side/11; //直径
        gab = side/11; //圆圈的间隔是直径的1/3

        centerX = (index%3)*(diameter +gab)+ diameter /2 + paddingLeft;
        centerY = (index/3)*(diameter +gab)+ diameter /2 + paddingTop;

        radius = diameter/2-1.5f;

        rectF = new Rect((int)(centerX-radius),(int)(centerY-radius),(int)(centerX+radius),(int)(centerY+radius));

        status = 0;
    }

    public Rect getRectF() {
        return rectF;
    }

    public void setRect(Rect rectF) {
        this.rectF = rectF;
    }

    public float getCenterY() {
        return centerY;
    }

    public void setCenterY(float centerY) {
        this.centerY = centerY;
    }

    public float getCenterX() {
        return centerX;
    }

    public void setCenterX(float centerX) {
        this.centerX = centerX;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
