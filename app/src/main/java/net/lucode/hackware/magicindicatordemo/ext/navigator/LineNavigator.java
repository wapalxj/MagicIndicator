package net.lucode.hackware.magicindicatordemo.ext.navigator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import net.lucode.hackware.magicindicator.abs.IPagerNavigator;
import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * desc:   直线指示器 ，
 * <p>
 * 配合 {@link net.lucode.hackware.magicindicator.MagicIndicator} 使用
 */
public class LineNavigator extends View implements IPagerNavigator {

    private int mLineWidth;
    private int mLineHeight;
    private int mSelectedColor;
    private int mUnSelectedColor;
    private int mLineSpacing;
    private int mCurrentIndex;
    private int mTotalCount;
    private Paint mPaint = new Paint(1);
    private List<RectF> mLinePoints = new ArrayList<>();
    private Interpolator mStartInterpolator = new LinearInterpolator();
    private float mIndicatorX;
    private boolean mFollowTouch;


    public LineNavigator(Context context) {
        super(context);
        this.init(context);
    }


    private void init(Context context) {
        mLineWidth = UIUtil.dip2px(context, 15);
        mLineHeight = UIUtil.dip2px(context, 1);
        mLineSpacing = UIUtil.dip2px(context, 10);
        mSelectedColor = Color.RED;
        mUnSelectedColor = Color.BLACK;
        mTotalCount = 3;
        mFollowTouch = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.setMeasuredDimension(this.measureWidth(widthMeasureSpec), this.measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int widthMeasureSpec) {
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int result = 0;
        switch (mode) {
            case -2147483648:
            case 0:
                result = this.mTotalCount * this.mLineWidth + (this.mTotalCount - 1) * this.mLineSpacing + this.getPaddingLeft() + this.getPaddingRight();
                break;
            case 1073741824:
                result = width;
        }

        return result;
    }

    private int measureHeight(int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int result = 0;
        switch (mode) {
            case -2147483648:
            case 0:
                result = this.mLineHeight + this.getPaddingTop() + this.getPaddingBottom();
                break;
            case 1073741824:
                result = height;
        }

        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        this.mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.drawLines(canvas);
        this.drawIndicator(canvas);
    }

    private void drawLines(Canvas canvas) {
        int i = 0;
        this.mPaint.setColor(this.mUnSelectedColor);
        for (int j = this.mLinePoints.size(); i < j; ++i) {
            RectF rectF = this.mLinePoints.get(i);
            canvas.drawRect(rectF, mPaint);
        }

    }

    private void drawIndicator(Canvas canvas) {
        this.mPaint.setColor(mSelectedColor);
        if (this.mLinePoints.size() > 0) {
            canvas.drawRect(mIndicatorX, 0, mIndicatorX + mLineWidth, mLineHeight, mPaint);
        }

    }

    private void prepareCirclePoints() {
        this.mLinePoints.clear();
        if (this.mTotalCount > 0) {
            int startX = this.getPaddingLeft();
            for (int i = 0; i < this.mTotalCount; ++i) {
                RectF rectF = new RectF(startX, 0, startX + mLineWidth, mLineHeight);
                this.mLinePoints.add(rectF);
                startX += (mLineSpacing + mLineWidth);
            }

            this.mIndicatorX = ((RectF) this.mLinePoints.get(this.mCurrentIndex)).left;
        }

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (this.mFollowTouch) {
            if (this.mLinePoints.isEmpty()) {
                return;
            }

            int currentPosition = Math.min(this.mLinePoints.size() - 1, position);
            int nextPosition = Math.min(this.mLinePoints.size() - 1, position + 1);
            RectF current = (RectF) this.mLinePoints.get(currentPosition);
            RectF next = (RectF) this.mLinePoints.get(nextPosition);
            this.mIndicatorX = current.left + (next.left - current.left) * this.mStartInterpolator.getInterpolation(positionOffset);
            this.invalidate();
        }

    }

    @Override
    public void onPageSelected(int position) {
        this.mCurrentIndex = position;
        if (!this.mFollowTouch) {
            this.mIndicatorX = ((RectF) this.mLinePoints.get(this.mCurrentIndex)).left;
            this.invalidate();
        }

    }
    @Override
    public void onPageScrollStateChanged(int state) {
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.prepareCirclePoints();
    }
    @Override
    public void onAttachToMagicIndicator() {
    }
    @Override
    public void notifyDataSetChanged() {
        this.prepareCirclePoints();
        this.invalidate();
    }
    @Override
    public void onDetachFromMagicIndicator() {
    }

    public void setLineWidth(int lineWidth) {
        mLineWidth = lineWidth;
    }

    public void setLineHeight(int lineHeight) {
        mLineHeight = lineHeight;
    }

    public void setSelectedColor(int selectedColor) {
        mSelectedColor = selectedColor;
    }

    public void setUnSelectedColor(int unSelectedColor) {
        mUnSelectedColor = unSelectedColor;
    }

    public void setLineSpacing(int lineSpacing) {
        mLineSpacing = lineSpacing;
    }

    public int getTotalCount() {
        return mTotalCount;
    }

    public void setTotalCount(int totalCount) {
        mTotalCount = totalCount;
    }
}