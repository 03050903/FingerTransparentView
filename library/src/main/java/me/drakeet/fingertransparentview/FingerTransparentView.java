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
    private OnZoomTouchListener mZoomTouchListener;

    public FingerTransparentView(Context context) {
        super(context);
    }

    public FingerTransparentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FingerTransparentView);

        try {
            mFingerRadius = a.getDimensionPixelSize(
                    R.styleable.FingerTransparentView_transparent_radius,
                    getResources().getDimensionPixelSize(R.dimen.finger_transparent_radius_default)
            );
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

        mZoomTouchListener = new OnZoomTouchListener() {
            @Override
            public void onZoom(float scale) {
                setScale(scale);
            }
        };

        this.setOnTouchListener(mZoomTouchListener);
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);

        mZoomTouchListener.onTouch(this, event);
        //onTouchEvent(event);

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        int x = (int) event.getX();
        int y = (int) event.getY();

        mShowBelowViewRect.left = (int) (x - mFingerRadius * mScale / 2);
        mShowBelowViewRect.right = (int) (x + mFingerRadius * mScale / 2 + 1) ;//加一防止小数点被砍掉出现漏缝
        mShowBelowViewRect.top = (int) (y - mFingerRadius * mScale);
        mShowBelowViewRect.bottom = y;

        if (action == MotionEvent.ACTION_UP) {
            resetBaseLayer();
        } else if (action != MotionEvent.ACTION_POINTER_DOWN) {
            Canvas canvas = new Canvas();
            canvas.setBitmap(mBaseLayer);
            resetBaseLayer();
            mTouchPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
            canvas.drawBitmap(mFingerLayer, mShowBelowViewRect.left, mShowBelowViewRect.top, mTouchPaint);
            mTouchPaint.setXfermode(null);
            canvas.save();
        }
        invalidate();

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBaseLayer, null, mRect, null);
        canvas.drawBitmap(mFingerLayer, null, mShowBelowViewRect, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        init();
    }

    /**
     * 等比缩放
     * add by Malin
     *
     * @param bitmap:资源图片
     * @param with:手指缩放的半径
     *
     * @return
     */
    private Bitmap scaleBitmap(Bitmap bitmap, int with) {
        return Bitmap.createScaledBitmap(
                bitmap,
                with,
                with * bitmap.getWidth() / bitmap.getHeight(),
                true
        );
    }

    private Bitmap scaleBitmap(Bitmap bitmap, float with) {
        return scaleBitmap(bitmap, (int)(with * mFingerRadius));
    }

    /**
     * 获得两点的距离
     *
     * @param event
     *
     * @return
     */
    private double spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return Math.sqrt(x * x + y * y);
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

}
