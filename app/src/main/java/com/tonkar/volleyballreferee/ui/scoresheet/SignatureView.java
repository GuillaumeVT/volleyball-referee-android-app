package com.tonkar.volleyballreferee.ui.scoresheet;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.*;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.tonkar.volleyballreferee.R;

public class SignatureView extends View {

    private final Paint mPaint;
    private final Path  mPath;
    private       float mCurX;
    private       float mCurY;
    private       float mStartX;
    private       float mStartY;

    public SignatureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPath = new Path();
        mCurX = 0f;
        mCurY = 0f;
        mStartX = 0f;
        mStartY = 0f;

        mPaint.setColor(ContextCompat.getColor(context, R.color.colorOnScoreSheetBackground));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(5f);
        mPaint.setAntiAlias(true);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath, mPaint);
    }

    private void actionDown(float x, float y) {
        mPath.moveTo(x, y);
        mCurX = x;
        mCurY = y;
    }

    private void actionMove(float x, float y) {
        mPath.quadTo(mCurX, mCurY, (x + mCurX) / 2f, (y + mCurY) / 2f);
        mCurX = x;
        mCurY = y;
    }

    private void actionUp() {
        mPath.lineTo(mCurX, mCurY);

        // draw a dot on click
        if (mStartX == mCurX && mStartY == mCurY) {
            mPath.lineTo(mCurX, mCurY + 2);
            mPath.lineTo(mCurX + 1, mCurY + 2);
            mPath.lineTo(mCurX + 1, mCurY);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                mStartX = event.getX();
                mStartY = event.getY();
                actionDown(event.getX(), event.getY());
            }
            case MotionEvent.ACTION_MOVE -> actionMove(event.getX(), event.getY());
            case MotionEvent.ACTION_UP -> actionUp();
        }

        invalidate();

        return true;
    }

    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }
}
