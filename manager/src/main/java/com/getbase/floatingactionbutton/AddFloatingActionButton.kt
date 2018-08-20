package com.getbase.floatingactionbutton

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.Shape
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.util.AttributeSet

import com.slim.slimfilemanager.R

open class AddFloatingActionButton : FloatingActionButton {
    internal var mPlusColor: Int = 0

    /**
     * @return the current Color of plus icon.
     */
    private var plusColor: Int
        get() = mPlusColor
        set(color) {
            if (mPlusColor != color) {
                mPlusColor = color
                updateBackground()
            }
        }

    override var iconDrawable: Drawable?
        get() {
            val iconSize = getDimension(R.dimen.fab_icon_size)
            val iconHalfSize = iconSize / 2f

            val plusSize = getDimension(R.dimen.fab_plus_icon_size)
            val plusHalfStroke = getDimension(R.dimen.fab_plus_icon_stroke) / 2f
            val plusOffset = (iconSize - plusSize) / 2f

            val shape = object : Shape() {
                override fun draw(canvas: Canvas, paint: Paint) {
                    canvas.drawRect(plusOffset, iconHalfSize - plusHalfStroke,
                            iconSize - plusOffset, iconHalfSize + plusHalfStroke, paint)
                    canvas.drawRect(iconHalfSize - plusHalfStroke, plusOffset,
                            iconHalfSize + plusHalfStroke, iconSize - plusOffset, paint)
                }
            }

            val drawable = ShapeDrawable(shape)

            val paint = drawable.paint
            paint.color = mPlusColor
            paint.style = Style.FILL
            paint.isAntiAlias = true

            return drawable
        }
        set(value) {
            super.iconDrawable = value
        }

    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : super(context,
            attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs,
            defStyle)

    override fun init(context: Context, attributeSet: AttributeSet?) {
        val attr =
                context.obtainStyledAttributes(attributeSet, R.styleable.AddFloatingActionButton, 0,
                        0)
        plusColor = attr.getColor(R.styleable.AddFloatingActionButton_fab_plusIconColor,
                getColor(android.R.color.white))
        attr.recycle()

        super.init(context, attributeSet)
    }

    @Suppress("unused")
    fun setPlusColorResId(@ColorRes plusColor: Int) {
        this.plusColor = getColor(plusColor)
    }

    override fun setIcon(@DrawableRes icon: Int) {
        throw UnsupportedOperationException(
                "Use FloatingActionButton if you want to use custom icon")
    }
}
