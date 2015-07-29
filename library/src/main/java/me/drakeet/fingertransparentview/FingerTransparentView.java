package me.drakeet.fingertransparentview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
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

    private static final int PNG_RADIUS = 164;
    private Bitmap mBaseLayer, mFingerLayer;
    private Paint mBasePaint, mTouchPaint;
    private int mBaseColor;
    private int mWidth;
    private int mHeight;
    private Rect mRect, mTouchRect;
    private int mFingerRadius;

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
        mTouchRect = new Rect();
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
    }

    private void initFingerLayer() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.finger);
        mFingerLayer = scaleBitmap(bitmap, mFingerRadius * 1.0f / PNG_RADIUS);
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

    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);

        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        mTouchRect.left = x - mFingerRadius / 2;
        mTouchRect.right = x + mFingerRadius / 2;
        mTouchRect.top = y - mFingerRadius;
        mTouchRect.bottom = y;

        if (action == MotionEvent.ACTION_UP) {
            resetBaseLayer();
        } else {
            Canvas canvas = new Canvas();
            canvas.setBitmap(mBaseLayer);
            resetBaseLayer();
            mTouchPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
            canvas.drawBitmap(mFingerLayer, mTouchRect.left, mTouchRect.top, mTouchPaint);
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
        canvas.drawBitmap(mFingerLayer, null, mTouchRect, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        init();
    }

    private Bitmap scaleBitmap(Bitmap bitmap, float s) {
        Matrix matrix = new Matrix();
        matrix.postScale(s, s);
        return Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                true
        );
    }
}
