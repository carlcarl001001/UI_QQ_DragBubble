package com.example.carl.ui_qq_dragbubble;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.Nullable;

public class MessageBubbleView extends View {
    //两个圆的圆心
    private PointF mFixationPoint,mDragPoint;
    private int mDragRadius=10;
    private Paint mPaint;
    private int mFixationRadiusMax = 10;
    private int mFixationRadiusMin = 3;
    private int mFixationRadius;
    private Bitmap mDragBitmap;
    public MessageBubbleView(Context context) {
        this(context,null);
    }

    public MessageBubbleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MessageBubbleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDragRadius = dip2px(mDragRadius);
        mFixationRadiusMax = dip2px(mFixationRadiusMax);
        mFixationRadiusMin = dip2px(mFixationRadiusMin);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }



    private int dip2px(int dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dip,getResources().getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //画两个圆
        if (mDragPoint==null||mFixationPoint==null){
            return;
        }
        //拖拽圆
        canvas.drawCircle(mDragPoint.x,mDragPoint.y,mDragRadius,mPaint);
        //画固定圆 有一个初始位置 半径随距离的增大而减小
        //计算两个点的距离
        double distance = getDistance(mDragPoint,mFixationPoint);
        mFixationRadius = (int)(mFixationRadiusMax - distance/14);
        if (mFixationRadius>mFixationRadiusMin) {
            canvas.drawCircle(mFixationPoint.x, mFixationPoint.y, mFixationRadius, mPaint);
        }
        if (getBezeierPath()!=null) {
            canvas.drawPath(getBezeierPath(), mPaint);
        }
        //画图片
        if (this.mDragBitmap!=null){
            canvas.drawBitmap(mDragBitmap,mDragPoint.x-mDragBitmap.getWidth()/2,mDragPoint.y-mDragBitmap.getHeight()/2,null);
        }


    }
/**
 * 获取两个圆之间的距离
 * */
    private double getDistance(PointF point1, PointF point2) {
        return Math.sqrt(Math.pow((point1.x-point2.x),2)+Math.pow((point1.y-point2.y),2));
    }

    public Path getBezeierPath(){
        double distance = getDistance(mDragPoint,mFixationPoint);
        mFixationRadius = (int)(mFixationRadiusMax - distance/14);
        if (mFixationRadius<mFixationRadiusMin) {
            return null;
        }
        Path bezeierPath = new Path();
        float dy = (mDragPoint.y-mFixationPoint.y);
        float dx = (mDragPoint.x-mFixationPoint.x);
        float tanA = dy/dx;
        double arcTanA = Math.atan(tanA);

        //P0
        float p0x = (float)(mFixationPoint.x+mFixationRadius*Math.sin(arcTanA));
        float p0y = (float)(mFixationPoint.y-mFixationRadius*Math.cos(arcTanA));
        //P1
        float p1x = (float)(mDragPoint.x+mDragRadius*Math.sin(arcTanA));
        float p1y = (float)(mDragPoint.y-mDragRadius*Math.cos(arcTanA));
        //P2
        float p2x = (float)(mDragPoint.x-mDragRadius*Math.sin(arcTanA));
        float p2y = (float)(mDragPoint.y+mDragRadius*Math.cos(arcTanA));
        //P3
        float p3x = (float)(mFixationPoint.x-mFixationRadius*Math.sin(arcTanA));
        float p3y = (float)(mFixationPoint.y+mFixationRadius*Math.cos(arcTanA));

        bezeierPath.moveTo(p0x,p0y);
        PointF controlPoint = getControlPoint();
        //画第一条曲线 第一给参数是控制点，第二给参数是结束点
        bezeierPath.quadTo(controlPoint.x,controlPoint.y,p1x,p1y);
        //画第二条曲线
        bezeierPath.lineTo(p2x,p2y);
        bezeierPath.quadTo(controlPoint.x,controlPoint.y,p3x,p3y);
        bezeierPath.close();


        return bezeierPath;
    }

    private PointF getControlPoint() {
        return new PointF((mDragPoint.x+mFixationPoint.x)/2,
                (mDragPoint.y+mFixationPoint.y)/2);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()){
//            case MotionEvent.ACTION_DOWN:
//                //手指按下要去指定当前的位置
//                float downX=event.getX();
//                float downY=event.getY();
//                initPoint(downX,downY);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                float moveX=event.getX();
//                float moveY=event.getY();
//                updateDragPoint(moveX,moveY);
//                break;
//            case MotionEvent.ACTION_UP:
//                break;
//        }
//        invalidate();//看源码 性能优化
//        return true;
//    }

    public void updateDragPoint(float moveX, float moveY) {
        mDragPoint.x=moveX;
        mDragPoint.y=moveY;
        invalidate();
    }

    public void initPoint(float downX,float downY){
        mFixationPoint = new PointF(downX,downY);
        mDragPoint = new PointF(downX,downY);
    }
    /*
    * 绑定可以拖拽的控件
    * */
    public static void attach(View view,BubbleDisappearListener disappearListener) {
        view.setOnTouchListener(new BubbleMessageTouchListener(view,view.getContext()));
    }

    public void setDragBitmap(Bitmap dragBitmap) {
        this.mDragBitmap = dragBitmap;
    }
/**
 * 处理手指松开
 * */
    public void handleActionUp() {
        if(mFixationRadius>mFixationRadiusMin){
            //回弹
            ValueAnimator animator = ObjectAnimator.ofFloat(1);
            animator.setDuration(250);
            final PointF start = new PointF(mDragPoint.x,mDragPoint.y);
            final PointF end = new PointF(mFixationPoint.x,mFixationPoint.y);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float percent = (float)animation.getAnimatedValue();
                    PointF pointF = BubbleUtils.getPointByPercent(start,end,percent);
                    updateDragPoint(pointF.x,pointF.y);
                }
            });
            //设置一个插值器 在结束时回弹
            animator.setInterpolator(new OvershootInterpolator(5f));
            animator.start();
            //还要通知TouchListener移除当前View 然后显示静态的View
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mListener!=null){
                        mListener.restore();
                    }
                }
            });

        }else {
            //爆炸
            if (mListener!=null){
                mListener.dismiss(mDragPoint);
            }
        }


    }

    public interface BubbleDisappearListener{
        void dismiss(View view);
    }

    private MessageBubbleListener mListener;

    public void setMessageBubbleListener( MessageBubbleListener listener){
        this.mListener = listener;
    }

    public interface MessageBubbleListener{
        //归位
        public void restore();
        //爆炸
        public void dismiss(PointF position);
    }


}

