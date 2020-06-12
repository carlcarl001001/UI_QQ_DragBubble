package com.example.carl.ui_qq_dragbubble;

import android.content.Context;
import android.graphics.PointF;
import android.util.TypedValue;

public class BubbleUtils {
    public static BubbleUtils getInstance(){
        return new BubbleUtils();
    }
    public static int getStatusBarHeight(Context context){
        int resourceId = context.getResources().getIdentifier("status_bar_height","dimen","android");
        if (resourceId>0){
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return dip2px(25,context);
    }

    private static int dip2px(int dip, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dip,context.getResources().getDisplayMetrics());
    }
    /**
     * 通过百分比，获得对应点的位置
     * **/
    public static PointF getPointByPercent(PointF p1, PointF p2, float percent) {
        return new PointF(evaluateValue(percent,p1.x,p2.x),evaluateValue(percent,p1.y,p2.y));
    }

    private static float evaluateValue(float percent, Number start, Number end) {
        return start.floatValue()+(end.floatValue()-start.floatValue())*percent;
    }
}
