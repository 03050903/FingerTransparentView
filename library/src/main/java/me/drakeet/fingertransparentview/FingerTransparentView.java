package me.drakeet.fingertransparentview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by drakeet on 7/29/15.
 */
public class FingerTransparentView extends View {

    private Bitmap mBaseLayer, mFingerLayer;
    private Paint mBasePaint, mTouchPaint;
    private int mBaseColor;
    private int mWidth;
    private int mHeight;
    private Rect mRect, mShowBelowViewRect;

    private int mFingerRadius;
    private float mScale = 1.0f;
    private OnScaleTouchListener mZoomTouchListener;
    private boolean mCanScale = true;
    private Xfermode mXfermode;


    public FingerTransparentView(Context context) {
        super(context);
    }


    public FingerTransparentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FingerTransparentView);

        try {
            mFingerRadius =
                    a.getDimensionPixelSize(R.styleable.FingerTransparentView_transparent_radius,
                            getResources().getDimensionPixelSize(
                                    R.dimen.finger_transparent_radius_default));
        } finally {
            a.recycle();
        }
    }


    private void init() {
        mShowBelowViewRect = new Rect();
        mBaseColor = Color.WHITE; // TODO

        mBasePaint = new Paint();
        mBasePaint.setAntiAlias(true);
        mBasePaint.setStyle(Paint.Style.FILL);
        mBasePaint.setColor(mBaseColor);

        mTouchPaint = new Paint();
        mTouchPaint.setAntiAlias(true);

        initBaseLayer();
        initFingerLayer();

        setWillNotDraw(false);

        mZoomTouchListener = new OnScaleTouchListener() {
            @Override public void onScale(float scale) {
                setScale(scale);
            }
        };
    }


    private void initFingerLayer() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.finger);
        mFingerLayer = scaleBitmap(bitmap, mScale);
    }


    private void initBaseLayer() {
        mBaseLayer = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBaseLayer);

        mRect = new Rect(0, 0, mWidth, mHeight);
        canvas.drawRect(mRect, mBasePaint);
    }


    private void resetBaseLayer() {
        Canvas canvas = new Canvas(mBaseLayer);

        mRect = new Rect(0, 0, mWidth, mHeight);
        canvas.drawRect(mRect, mBasePaint);
    }


    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);

        if (mCanScale) {
            mZoomTouchListener.onTouch(this, event);
        }
        else {
            onTouchEvent(event);
        }

        return true;
    }


    @Override public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        int x = (int) event.getX();
        int y = (int) event.getY();

        mShowBelowViewRect.left = (int) (x - mFingerRadius * mScale / 2);
        mShowBelowViewRect.right = (int) (x + mFingerRadius * mScale / 2 + 1);//加一防止小数点被砍掉出现漏缝
        mShowBelowViewRect.top = (int) (y - mFingerRadius * mScale);
        mShowBelowViewRect.bottom = y;

        switch (action) {
            case MotionEvent.ACTION_UP:
                resetBaseLayer();
                mXfermode = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_DOWN:
                mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT);
                // ↓↓↓
            default:
                Canvas canvas = new Canvas();
                canvas.setBitmap(mBaseLayer);
                resetBaseLayer();
                mTouchPaint.setXfermode(mXfermode);
                canvas.drawBitmap(mFingerLayer, mShowBelowViewRect.left, mShowBelowViewRect.top,
                        mTouchPaint);
                mTouchPaint.setXfermode(null);
                canvas.save();
        }

        invalidate();
        return true;
    }


    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBaseLayer, null, mRect, null);
        canvas.drawBitmap(mFingerLayer, null, mShowBelowViewRect, null);
    }


    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        init();
    }


    /**
     * 等比缩放
     * add by Malin
     */
    private Bitmap scaleBitmap(Bitmap bitmap, int with) {
        return Bitmap.createScaledBitmap(bitmap, with,
                with * bitmap.getWidth() / bitmap.getHeight(), true);
    }


    private Bitmap scaleBitmap(Bitmap bitmap, float with) {
        return scaleBitmap(bitmap, (int) (with * mFingerRadius));
    }


    public int getFingerRadius() {
        return mFingerRadius;
    }


    public void setFingerRadius(int fingerRadius) {
        mFingerRadius = fingerRadius;
    }


    public float getScale() {
        return mScale;
    }


    public void setScale(float scale) {
        mScale = scale;
        initFingerLayer();
        invalidate();
    }


    public boolean getCanScale() {
        return mCanScale;
    }


    public void setCanScale(boolean canScale) {
        mCanScale = canScale;
        if (mCanScale) this.setOnTouchListener(mZoomTouchListener);
    }
}
