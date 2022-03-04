package net.lucode.hackware.magicindicatordemo.ext.navigator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import net.lucode.hackware.magicindicator.abs.IPagerNavigator
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicatordemo.BuildConfig
import java.util.*

/**
 * desc:   指示器
 *
 * 配合 [net.lucode.hackware.magicindicator.MagicIndicator] 使用
 */
class RoundRectNavigator2(context: Context) : View(context), IPagerNavigator {

    private var mLineWidth: Float = 0f
    private var mLineHeight: Float = 0f
    private var selectedWidth: Float = 0f
    private var mLineSpacing: Float = 0f
    private var lastPositionOffset = 0f
    private var mSelectedColor: Int = 0
    private var mUnSelectedColor: Int = 0
    private var mCurrentIndex: Int = 0
    private val mPaint = Paint(1)
    private val mLinePoints = ArrayList<RectF>()
    private val mLineColors = ArrayList<Int>()
    private val mStartInterpolator = LinearInterpolator()
    private var mIndicatorX: Float = 0.toFloat()
    private var mFollowTouch: Boolean = false



    var maxCount: Int = 0
    var totalCount: Int = 0
    set(value) {
        field = value
        maxCount= Int.MAX_VALUE
    }
    init {
        this.init(context)
    }

    private fun init(context: Context) {
        mLineWidth = UIUtil.dip2px(context, 10.0).toFloat()
        mLineHeight = UIUtil.dip2px(context, 5.0).toFloat()
        selectedWidth = UIUtil.dip2px(context, 20.0).toFloat()
        mLineSpacing = UIUtil.dip2px(context, 5.0).toFloat()
        mSelectedColor = Color.parseColor("#333333")
        mUnSelectedColor = Color.LTGRAY
        totalCount = 3
        mFollowTouch = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        this.setMeasuredDimension(this.measureWidth(widthMeasureSpec), this.measureHeight(heightMeasureSpec))
    }

    private fun measureWidth(widthMeasureSpec: Int): Int {
        val mode = MeasureSpec.getMode(widthMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        var result = 0
        when (mode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> result = ((this.totalCount - 1) * this.mLineWidth + (this.totalCount - 1) * this.mLineSpacing + this.selectedWidth + this.paddingLeft + this.paddingRight).toInt()
            MeasureSpec.EXACTLY -> result = width
        }

        return result
    }

    private fun measureHeight(heightMeasureSpec: Int): Int {
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        var result = 0
        when (mode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> result = (this.mLineHeight + this.paddingTop + this.paddingBottom).toInt()
            MeasureSpec.EXACTLY -> result = height
        }

        return result
    }

    override fun onDraw(canvas: Canvas) {
        this.mPaint.style = Paint.Style.FILL_AND_STROKE
        this.drawLines(canvas)
    }

    private fun drawLines(canvas: Canvas) {
        this.mPaint.color = this.mUnSelectedColor
        prepareCirclePoints()
        if (this.mLinePoints.size > 0) {
            this.mLinePoints.forEachIndexed { index, rectF ->
                this.mPaint.color = this.mLineColors[index]
                canvas.drawRoundRect(rectF, mLineWidth / 2, mLineWidth / 2, mPaint)
            }
        }
    }

    private fun prepareCirclePoints() {
        this.mLinePoints.clear()
        this.mLineColors.clear()
        for (i in 0 until totalCount) {
            val recto = RectF()
            var left: Float
            var right: Float

            mCurrentIndex %= maxCount
            when {
                mCurrentIndex == totalCount - 1 && lastPositionOffset > 0 -> {
                    when (i) {
                        0 -> {
                            left = i * (mLineWidth + mLineSpacing)
                            right = left + mLineWidth + (selectedWidth - mLineWidth) * lastPositionOffset
                        }
                        mCurrentIndex -> {
                            left = i * (mLineWidth + mLineSpacing) + (selectedWidth - mLineWidth) * lastPositionOffset
                            right = left + mLineWidth + (selectedWidth - mLineWidth) * (1 - lastPositionOffset)
                        }
                        else -> {
                            left = i * (mLineWidth + mLineSpacing) + (selectedWidth - mLineWidth) * lastPositionOffset
                            right = left + mLineWidth
                        }
                    }
                    /////
                    val ratio = (right - left - mLineWidth) / (selectedWidth - mLineWidth)
                    val color=getCurrentColor(ratio,mUnSelectedColor,mSelectedColor)
                    this.mLineColors.add(color)

                }

                i < mCurrentIndex -> {
                    left = i * (mLineWidth + mLineSpacing)
                    right = left + mLineWidth
                    /////
                    this.mLineColors.add(mUnSelectedColor)
                }
                i == mCurrentIndex -> {
                    left = i * (mLineWidth + mLineSpacing)
                    right = left + mLineWidth + (selectedWidth - mLineWidth) * (1 - lastPositionOffset)

                    /////
                    val ratio = (right - left - mLineWidth) / (selectedWidth - mLineWidth)
                    val color=getCurrentColor(ratio,mUnSelectedColor,mSelectedColor)
                    this.mLineColors.add(color)
                }

                i == mCurrentIndex + 1 -> {
                    left = (i - 1) * (mLineSpacing + mLineWidth) + mLineWidth + (selectedWidth - mLineWidth) * (1 - lastPositionOffset) + mLineSpacing
                    right = i * (mLineSpacing + mLineWidth) + selectedWidth

                    /////
                    val ratio = (right - left - mLineWidth) / (selectedWidth - mLineWidth)
                    val color=getCurrentColor(ratio,mUnSelectedColor,mSelectedColor)
                    this.mLineColors.add(color)
                }
                else -> {
                    left = (i - 1) * (mLineWidth + mLineSpacing) + (selectedWidth + mLineSpacing)
                    right = (i - 1) * (mLineWidth + mLineSpacing) + (selectedWidth + mLineSpacing) + mLineWidth

                    /////
                    this.mLineColors.add(mUnSelectedColor)
                }
            }

            val top = 0f
            val bottom = mLineHeight

            recto.left = left
            recto.top = top
            recto.right = right
            recto.bottom = bottom
            this.mLinePoints.add(recto)
        }

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        this.mCurrentIndex = position % totalCount
        Log.e("onPageScrolled","onPageScrolled===$mCurrentIndex")
        lastPositionOffset = this.mStartInterpolator.getInterpolation(positionOffset)
        this.invalidate()
    }


    override fun onPageSelected(position: Int) {
        if (!this.mFollowTouch) {
            this.mIndicatorX = this.mLinePoints[this.mCurrentIndex].left
            this.mCurrentIndex = position % totalCount
            this.invalidate()
        }

    }

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onAttachToMagicIndicator() {}

    override fun notifyDataSetChanged() {
        this.invalidate()
    }

    override fun onDetachFromMagicIndicator() {}

    fun setLineWidth(lineWidth: Float) {
        mLineWidth = lineWidth
    }

    fun setLineHeight(lineHeight: Float) {
        mLineHeight = lineHeight
    }

    fun setSelectedColor(selectedColor: Int) {
        mSelectedColor = selectedColor
    }

    fun setUnSelectedColor(unSelectedColor: Int) {
        mUnSelectedColor = unSelectedColor
    }

    fun setLineSpacing(lineSpacing: Float) {
        mLineSpacing = lineSpacing
    }


    /**
     * 根据fraction值来计算当前的颜色。 fraction值范围  0f-1f
     */
    private fun getCurrentColor(fraction: Float, startColor: Int, endColor: Int): Int {
        val redCurrent: Int
        val blueCurrent: Int
        val greenCurrent: Int
        val alphaCurrent: Int
        val redStart = Color.red(startColor)
        val blueStart = Color.blue(startColor)
        val greenStart = Color.green(startColor)
        val alphaStart = Color.alpha(startColor)
        val redEnd = Color.red(endColor)
        val blueEnd = Color.blue(endColor)
        val greenEnd = Color.green(endColor)
        val alphaEnd = Color.alpha(endColor)
        val redDifference = redEnd - redStart
        val blueDifference = blueEnd - blueStart
        val greenDifference = greenEnd - greenStart
        val alphaDifference = alphaEnd - alphaStart
        redCurrent = (redStart + fraction * redDifference).toInt()
        blueCurrent = (blueStart + fraction * blueDifference).toInt()
        greenCurrent = (greenStart + fraction * greenDifference).toInt()
        alphaCurrent = (alphaStart + fraction * alphaDifference).toInt()
        return Color.argb(alphaCurrent, redCurrent, greenCurrent, blueCurrent)
    }
}