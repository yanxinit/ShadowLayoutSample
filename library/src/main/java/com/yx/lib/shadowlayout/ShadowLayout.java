package com.yx.lib.shadowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class ShadowLayout extends FrameLayout {

    private static final int DEFAULT_SHADOW_RADIUS = 20;
    private static final int DEFAULT_SHADOW_DISTANCE = 0;
    private static final int DEFAULT_SHADOW_ANGLE = 0;
    private static final int DEFAULT_SHADOW_COLOR = Color.DKGRAY;
    private static final int DEFAULT_SHADOW_PADDING = DEFAULT_SHADOW_RADIUS;

    private static final int MAX_ANGLE = 360;
    private static final int MIN_RADIUS = 1;
    private static final int MIN_ANGLE = 0;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
        {
            setDither(true);
            setFilterBitmap(true);
        }
    };

    private Bitmap mBitmap;
    private Canvas mCanvas = new Canvas();

    private Rect mBounds = new Rect();

    private boolean mIsShadowed;
    private int mShadowColor;
    private float mShadowRadius;
    private float mShadowDistance;
    private float mShadowAngle;
    private int mShadowPadding;

    private float mShadowDx;
    private float mShadowDy;

    public ShadowLayout(final Context context) {
        this(context, null);
    }

    public ShadowLayout(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShadowLayout(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_HARDWARE, mPaint);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShadowLayout);
        mIsShadowed = typedArray.getBoolean(R.styleable.ShadowLayout_sl_shadowed, true);
        mShadowRadius = Math.max(MIN_RADIUS, typedArray.getDimension(
                R.styleable.ShadowLayout_sl_shadow_radius, DEFAULT_SHADOW_RADIUS));
        mShadowDistance = typedArray.getDimension(R.styleable.ShadowLayout_sl_shadow_distance, DEFAULT_SHADOW_DISTANCE);
        mShadowAngle = Math.max(MIN_ANGLE, Math.min(typedArray.getInteger(
                R.styleable.ShadowLayout_sl_shadow_angle, DEFAULT_SHADOW_ANGLE), MAX_ANGLE));
        mShadowAngle = Math.max(MIN_ANGLE, Math.min(typedArray.getInteger(
                R.styleable.ShadowLayout_sl_shadow_angle, DEFAULT_SHADOW_ANGLE), MAX_ANGLE));
        mShadowColor = typedArray.getColor(R.styleable.ShadowLayout_sl_shadow_color, DEFAULT_SHADOW_COLOR);
        mShadowPadding = typedArray.getDimensionPixelSize(R.styleable.ShadowLayout_sl_shadow_padding, DEFAULT_SHADOW_PADDING);
        typedArray.recycle();

        init();
    }

    private void init() {
        mPaint.setColor(mShadowColor);
        mPaint.setMaskFilter(new BlurMaskFilter(mShadowRadius, BlurMaskFilter.Blur.OUTER));
        mShadowDx = (float) ((mShadowDistance) * Math.cos(mShadowAngle / 180.0f * Math.PI));
        mShadowDy = (float) ((mShadowDistance) * Math.sin(mShadowAngle / 180.0f * Math.PI));
        setPadding(mShadowPadding, mShadowPadding, mShadowPadding, mShadowPadding);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mBounds.set(0, 0, MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void dispatchDraw(final Canvas canvas) {
        if (!mIsShadowed) {
            super.dispatchDraw(canvas);
            return;
        }
        mBitmap = Bitmap.createBitmap(mBounds.width(), mBounds.height(), Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap);
        super.dispatchDraw(mCanvas);
        Bitmap extractedAlpha = mBitmap.extractAlpha();
        mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        mCanvas.drawBitmap(extractedAlpha, mShadowDx, mShadowDy, mPaint);
        extractedAlpha.recycle();
        canvas.drawBitmap(mBitmap, 0, 0, null);
        super.dispatchDraw(canvas);
    }

}
