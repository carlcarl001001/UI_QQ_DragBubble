package com.example.carl.ui_qq_dragbubble;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.AnimationDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 *  监听当前View的触摸事件
 * */
public class BubbleMessageTouchListener implements View.OnTouchListener, MessageBubbleView.MessageBubbleListener {
    private View mStaticView;
    private WindowManager mWindowManager;
    private MessageBubbleView mMessageBubbleView;
    private WindowManager.LayoutParams mParams;
    private Context mContent;
    private FrameLayout mBombFrame;
    private ImageView mBombImage;

    public BubbleMessageTouchListener(View view, Context context){
        mStaticView = view;
        mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        mMessageBubbleView = new MessageBubbleView(context);
        mMessageBubbleView.setMessageBubbleListener(this);
        mParams = new WindowManager.LayoutParams();
        mParams.format = PixelFormat.TRANSLUCENT;//去掉试试
        mContent=context;

        mBombFrame = new FrameLayout(mContent);
        mBombImage = new ImageView(mContent);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mBombImage.setLayoutParams(params);
        mBombFrame.addView(mBombImage);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mStaticView.setVisibility(View.INVISIBLE);
                mWindowManager.addView(mMessageBubbleView,mParams);
                //初始化 贝塞尔曲线位置 getRaw相对屏幕位置
                int[] location = new int[2];
                mStaticView.getLocationOnScreen(location);
                mMessageBubbleView.initPoint(location[0]+mStaticView.getWidth()/2,
                        location[1]+mStaticView.getHeight()/2-BubbleUtils.getStatusBarHeight(mContent));
                //给消息拖拽设置一个bitmap
                mMessageBubbleView.setDragBitmap(getBitMapByView(mStaticView));
                break;
            case MotionEvent.ACTION_MOVE:
                mMessageBubbleView.updateDragPoint(event.getRawX(),event.getRawY()-BubbleUtils.getStatusBarHeight(mContent));
                break;
            case MotionEvent.ACTION_UP:
                mMessageBubbleView.handleActionUp();
                break;

        }
        return true;
    }

    private Bitmap getBitMapByView(View view) {
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    @Override
    public void restore() {
        //把移动的view移除
        mWindowManager.removeView(mMessageBubbleView);
        //把原来的View显示
        mStaticView.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismiss(PointF position) {
        //执行爆炸动画（帧动画）
        //移除控件的bitmap
        mWindowManager.removeView(mMessageBubbleView);
        //要在mWindowManager中添加一个爆炸动画
        mWindowManager.addView(mBombFrame,mParams);
        mBombImage.setBackgroundResource(R.drawable.anim_bubble_pop);

        AnimationDrawable drawable = (AnimationDrawable) mBombImage.getBackground();
        mBombImage.setX(position.x-drawable.getIntrinsicWidth()/2);
        mBombImage.setY(position.y-drawable.getIntrinsicHeight()/2);
        drawable.start();
        //执行完后要移除
        mBombImage.postDelayed(new Runnable() {
            @Override
            public void run() {
                mWindowManager.removeView(mBombFrame);
                //通知一下外面该消失了
            }
        },getAnimationDrawableTime(drawable));



    }

    private long getAnimationDrawableTime(AnimationDrawable drawable) {
        int numberOfFrames = drawable.getNumberOfFrames();
        long time =0;
        for (int i=0;i<numberOfFrames;i++){
            time += drawable.getDuration(i);
        }
        return time;
    }
}
