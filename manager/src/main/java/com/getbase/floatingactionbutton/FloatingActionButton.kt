package com.getbase.floatingactionbutton

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint.Style
import android.graphics.Shader
import android.graphics.Shader.TileMode
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.ShapeDrawable.ShaderFactory
import android.graphics.drawable.StateListDrawable
import android.graphics.drawable.shapes.OvalShape
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.annotation.DrawableRes
import android.support.annotation.IntDef
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.TextView

import com.slim.slimfilemanager.R

open class FloatingActionButton : ImageButton {
    internal var mColorNormal: Int = 0
    internal var mColorPressed: Int = 0
    private var mColorDisabled: Int = 0
    private var mTitle: String? = null
    internal var mStrokeVisible: Boolean = false
    @DrawableRes
    private var mIcon: Int = 0
    private var mIconDrawable: Drawable? = null
    private var mSize: Int = 0

    private var mCircleSize: Float = 0.toFloat()
    private var mShadowRadius: Float = 0.toFloat()
    private var mShadowOffset: Float = 0.toFloat()
    private var mDrawableSize: Int = 0

    var size: Int
        @fabSize
        get() = mSize
        set(@fabSize size) {
            if (size != SIZE_MINI && size != SIZE_NORMAL) {
                throw IllegalArgumentException("Use @FAB_SIZE constants only!")
            }

            if (mSize != size) {
                mSize = size
                updateCircleSize()
                updateDrawableSize()
                updateBackground()
            }
        }

    /**
     * @return the current Color for normal state.
     */
    var colorNormal: Int
        get() = mColorNormal
        set(color) {
            if (mColorNormal != color) {
                mColorNormal = color
                updateBackground()
            }
        }

    /**
     * @return the current color for pressed state.
     */
    var colorPressed: Int
        get() = mColorPressed
        set(color) {
            if (mColorPressed != color) {
                mColorPressed = color
                updateBackground()
            }
        }

    /**
     * @return the current color for disabled state.
     */
    private var colorDisabled: Int
        get() = mColorDisabled
        set(color) {
            if (mColorDisabled != color) {
                mColorDisabled = color
                updateBackground()
            }
        }

    var isStrokeVisible: Boolean
        get() = mStrokeVisible
        set(visible) {
            if (mStrokeVisible != visible) {
                mStrokeVisible = visible
                updateBackground()
            }
        }

    internal val labelView: TextView?
        get() = getTag(R.id.fab_label) as TextView?

    var title: String?
        get() = mTitle
        set(title) {
            mTitle = title
            val label = labelView
            if (label != null) {
                label.text = title
            }
        }

    internal open var iconDrawable: Drawable?
        get() = when {
            mIconDrawable != null -> mIconDrawable
            mIcon != 0 -> ContextCompat.getDrawable(context, mIcon)
            else -> ColorDrawable(Color.TRANSPARENT)
        }
        set(iconDrawable) {
            if (mIconDrawable !== iconDrawable) {
                mIcon = 0
                mIconDrawable = iconDrawable
                updateBackground()
            }
        }

    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : super(context,
            attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs,
            defStyle) {
        init(context, attrs)
    }

    open fun init(context: Context, attributeSet: AttributeSet?) {
        val attr =
                context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionButton, 0, 0)
        mColorNormal = attr.getColor(R.styleable.FloatingActionButton_fab_colorNormal,
                getColor(android.R.color.holo_blue_dark))
        mColorPressed = attr.getColor(R.styleable.FloatingActionButton_fab_colorPressed,
                getColor(android.R.color.holo_blue_light))
        mColorDisabled = attr.getColor(R.styleable.FloatingActionButton_fab_colorDisabled,
                getColor(android.R.color.darker_gray))
        mSize = attr.getInt(R.styleable.FloatingActionButton_fab_size, SIZE_NORMAL)
        mIcon = attr.getResourceId(R.styleable.FloatingActionButton_fab_icon, 0)
        mTitle = attr.getString(R.styleable.FloatingActionButton_fab_title)
        mStrokeVisible = attr.getBoolean(R.styleable.FloatingActionButton_fab_stroke_visible, true)
        attr.recycle()

        updateCircleSize()
        mShadowRadius = getDimension(R.dimen.fab_shadow_radius)
        mShadowOffset = getDimension(R.dimen.fab_shadow_offset)
        updateDrawableSize()

        updateBackground()
    }

    private fun updateDrawableSize() {
        mDrawableSize = (mCircleSize + 2 * mShadowRadius).toInt()
    }

    private fun updateCircleSize() {
        mCircleSize = getDimension(
                if (mSize == SIZE_NORMAL) R.dimen.fab_size_normal else R.dimen.fab_size_mini)
    }

    open fun setIcon(@DrawableRes icon: Int) {
        if (mIcon != icon) {
            mIcon = icon
            mIconDrawable = null
            updateBackground()
        }
    }

    fun setColorNormalResId(@ColorRes colorNormal: Int) {
        this.colorNormal = getColor(colorNormal)
    }

    fun setColorPressedResId(@ColorRes colorPressed: Int) {
        this.colorPressed = getColor(colorPressed)
    }

    fun setColorDisabledResId(@ColorRes colorDisabled: Int) {
        this.colorDisabled = getColor(colorDisabled)
    }

    internal fun getColor(@ColorRes id: Int): Int {
        return ContextCompat.getColor(context, id)
    }

    internal fun getDimension(@DimenRes id: Int): Float {
        return resources.getDimension(id)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(mDrawableSize, mDrawableSize)
    }

    internal open fun updateBackground() {
        val strokeWidth = getDimension(R.dimen.fab_stroke_width)
        val halfStrokeWidth = strokeWidth / 2f

        val layerDrawable = LayerDrawable(
                arrayOf(ContextCompat.getDrawable(context,
                        if (mSize == SIZE_NORMAL)
                            R.drawable.fab_bg_normal
                        else
                            R.drawable.fab_bg_mini)!!, createFillDrawable(strokeWidth),
                        createOuterStrokeDrawable(strokeWidth), iconDrawable!!))

        val iconOffset = (mCircleSize - getDimension(R.dimen.fab_icon_size)).toInt() / 2

        val circleInsetHorizontal = mShadowRadius.toInt()
        val circleInsetTop = (mShadowRadius - mShadowOffset).toInt()
        val circleInsetBottom = (mShadowRadius + mShadowOffset).toInt()

        layerDrawable.setLayerInset(1,
                circleInsetHorizontal,
                circleInsetTop,
                circleInsetHorizontal,
                circleInsetBottom)

        layerDrawable.setLayerInset(2,
                (circleInsetHorizontal - halfStrokeWidth).toInt(),
                (circleInsetTop - halfStrokeWidth).toInt(),
                (circleInsetHorizontal - halfStrokeWidth).toInt(),
                (circleInsetBottom - halfStrokeWidth).toInt())

        layerDrawable.setLayerInset(3,
                circleInsetHorizontal + iconOffset,
                circleInsetTop + iconOffset,
                circleInsetHorizontal + iconOffset,
                circleInsetBottom + iconOffset)

        setBackgroundCompat(layerDrawable)
    }

    private fun createFillDrawable(strokeWidth: Float): StateListDrawable {
        val drawable = StateListDrawable()
        drawable.addState(intArrayOf(-android.R.attr.state_enabled),
                createCircleDrawable(mColorDisabled, strokeWidth))
        drawable.addState(intArrayOf(android.R.attr.state_pressed),
                createCircleDrawable(mColorPressed, strokeWidth))
        drawable.addState(intArrayOf(), createCircleDrawable(mColorNormal, strokeWidth))
        return drawable
    }

    private fun createCircleDrawable(color: Int, strokeWidth: Float): Drawable {
        val alpha = Color.alpha(color)
        val opaqueColor = opaque(color)

        val fillDrawable = ShapeDrawable(OvalShape())

        val paint = fillDrawable.paint
        paint.isAntiAlias = true
        paint.color = opaqueColor

        val layers = arrayOf(fillDrawable, createInnerStrokesDrawable(opaqueColor, strokeWidth))

        val drawable = if (alpha == 255 || !mStrokeVisible)
            LayerDrawable(layers)
        else
            TranslucentLayerDrawable(alpha, *layers)

        val halfStrokeWidth = (strokeWidth / 2f).toInt()
        drawable.setLayerInset(1, halfStrokeWidth, halfStrokeWidth, halfStrokeWidth,
                halfStrokeWidth)

        return drawable
    }

    private fun createOuterStrokeDrawable(strokeWidth: Float): Drawable {
        val shapeDrawable = ShapeDrawable(OvalShape())

        val paint = shapeDrawable.paint
        paint.isAntiAlias = true
        paint.strokeWidth = strokeWidth
        paint.style = Style.STROKE
        paint.color = Color.BLACK
        paint.alpha = opacityToAlpha(0.02f)

        return shapeDrawable
    }

    private fun opacityToAlpha(opacity: Float): Int {
        return (255f * opacity).toInt()
    }

    private fun darkenColor(argb: Int): Int {
        return adjustColorBrightness(argb, 0.9f)
    }

    private fun lightenColor(argb: Int): Int {
        return adjustColorBrightness(argb, 1.1f)
    }

    private fun adjustColorBrightness(argb: Int, factor: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(argb, hsv)

        hsv[2] = Math.min(hsv[2] * factor, 1f)

        return Color.HSVToColor(Color.alpha(argb), hsv)
    }

    private fun halfTransparent(argb: Int): Int {
        return Color.argb(
                Color.alpha(argb) / 2,
                Color.red(argb),
                Color.green(argb),
                Color.blue(argb)
                         )
    }

    private fun opaque(argb: Int): Int {
        return Color.rgb(
                Color.red(argb),
                Color.green(argb),
                Color.blue(argb)
                        )
    }

    private fun createInnerStrokesDrawable(color: Int, strokeWidth: Float): Drawable {
        if (!mStrokeVisible) {
            return ColorDrawable(Color.TRANSPARENT)
        }

        val shapeDrawable = ShapeDrawable(OvalShape())

        val bottomStrokeColor = darkenColor(color)
        val bottomStrokeColorHalfTransparent = halfTransparent(bottomStrokeColor)
        val topStrokeColor = lightenColor(color)
        val topStrokeColorHalfTransparent = halfTransparent(topStrokeColor)

        val paint = shapeDrawable.paint
        paint.isAntiAlias = true
        paint.strokeWidth = strokeWidth
        paint.style = Style.STROKE
        shapeDrawable.shaderFactory = object : ShaderFactory() {
            override fun resize(width: Int, height: Int): Shader {
                return LinearGradient((width / 2).toFloat(), 0f, (width / 2).toFloat(),
                        height.toFloat(),
                        intArrayOf(topStrokeColor, topStrokeColorHalfTransparent, color,
                                bottomStrokeColorHalfTransparent, bottomStrokeColor),
                        floatArrayOf(0f, 0.2f, 0.5f, 0.8f, 1f),
                        TileMode.CLAMP
                                     )
            }
        }

        return shapeDrawable
    }

    @SuppressLint("NewApi")
    private fun setBackgroundCompat(drawable: Drawable) {
        background = drawable
    }

    override fun setVisibility(visibility: Int) {
        val label = labelView
        if (label != null) {
            label.visibility = visibility
        }

        super.setVisibility(visibility)
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(SIZE_NORMAL, SIZE_MINI)
    annotation class fabSize

    private class TranslucentLayerDrawable internal constructor(private val mAlpha: Int,
                                                                vararg layers: Drawable) :
            LayerDrawable(layers) {

        override fun draw(canvas: Canvas) {
            val bounds = bounds
            canvas.saveLayerAlpha(bounds.left.toFloat(), bounds.top.toFloat(),
                    bounds.right.toFloat(), bounds.bottom.toFloat(), mAlpha, Canvas.ALL_SAVE_FLAG)
            super.draw(canvas)
            canvas.restore()
        }
    }

    companion object {

        const val SIZE_NORMAL = 0
        const val SIZE_MINI = 1
    }
}
