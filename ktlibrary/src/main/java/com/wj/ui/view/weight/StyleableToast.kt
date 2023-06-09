package com.wj.ui.view.weight

import android.content.Context
import android.view.View
import android.graphics.Color
import android.widget.LinearLayout
import android.annotation.SuppressLint
import android.content.res.TypedArray
import android.widget.TextView
import android.widget.Toast
import android.view.Gravity
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.core.text.TextUtilsCompat
import java.util.Locale
import androidx.core.view.ViewCompat
import com.wj.ktutils.R

@SuppressLint("ViewConstructor")
class StyleableToast : LinearLayout {
    private var cornerRadius = 0
    private var backgroundColor1 = 0
    private var strokeColor = 0
    private var strokeWidth = 0
    private var iconStart = 0
    private var iconEnd = 0
    private var textColor = 0
    private var font = 0
    private var length: Int
    private var style = 0
    private var textSize = 0f
    private var isTextSizeFromStyleXml = false
    private var solidBackground = false
    private var textBold = false
    private var text: String?
    private var typedArray: TypedArray? = null
    private var textView: TextView? = null
    private var gravity1 = 0
    private var toast: Toast? = null
    private var rootLayout: LinearLayout? = null
    private var viewId = -1

    private constructor(context: Context, text: String, length: Int, @StyleRes style: Int) : super(
        context) {
        this.text = text
        this.length = length
        this.style = style
    }

    private constructor(builder: Builder) : super(builder.context) {
        backgroundColor1 = builder.backgroundColor
        cornerRadius = builder.cornerRadius
        iconEnd = builder.iconEnd
        iconStart = builder.iconStart
        strokeColor = builder.strokeColor
        strokeWidth = builder.strokeWidth
        solidBackground = builder.solidBackground
        textColor = builder.textColor
        textSize = builder.textSize
        textBold = builder.textBold
        font = builder.font
        text = builder.text
        gravity1 = builder.gravity
        length = builder.length
        viewId = builder.viewId
    }

    private fun initStyleableToast() {
        val v: View
        v =
            if (viewId == -1) inflate(context, R.layout.styleable_layout, null) else inflate(
                context,
                viewId,
                null)
        rootLayout = v.rootView as LinearLayout
        textView = v.findViewById(R.id.textview)
        if (style > 0) {
            typedArray = context.obtainStyledAttributes(style, R.styleable.StyleableToast)
        }
        makeShape()
        makeTextView()
        makeIcon()

        // Very important to recycle AFTER the make() methods!
        if (typedArray != null) {
            typedArray!!.recycle()
        }
    }

    fun show() {
        initStyleableToast()
        toast = Toast(context)
        toast!!.setGravity(gravity1, 0, if (gravity1 == Gravity.CENTER) 0 else toast!!.yOffset)
        toast!!.duration =
            if (length == Toast.LENGTH_LONG) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        toast!!.view = rootLayout
        toast!!.show()
    }

    fun cancel() {
        if (toast != null) {
            toast!!.cancel()
        }
    }

    private fun makeShape() {
        loadShapeAttributes()
        val gradientDrawable = rootLayout!!.background.mutate() as GradientDrawable
        gradientDrawable.alpha = resources.getInteger(R.integer.defaultBackgroundAlpha)
        if (strokeWidth > 0) {
            gradientDrawable.setStroke(strokeWidth, strokeColor)
        }
        if (cornerRadius > -1) {
            gradientDrawable.cornerRadius = cornerRadius.toFloat()
        }
        if (backgroundColor1 != 0) {
            gradientDrawable.setColor(backgroundColor1)
        }
        if (solidBackground) {
            gradientDrawable.alpha = resources.getInteger(R.integer.fullBackgroundAlpha)
        }
        rootLayout!!.setBackgroundDrawable(gradientDrawable)
    }

    private fun makeTextView() {
        loadTextViewAttributes()
        textView!!.text = text
        if (textColor != 0) {
            textView!!.setTextColor(textColor)
        }
        if (textSize > 0) {
            textView!!.setTextSize(if (isTextSizeFromStyleXml) 0 else TypedValue.COMPLEX_UNIT_SP,
                textSize)
        }
        if (font > 0) {
            textView!!.setTypeface(ResourcesCompat.getFont(context, font),
                if (textBold) Typeface.BOLD else Typeface.NORMAL)
        }
        if (textBold && font == 0) {
            textView!!.setTypeface(textView!!.typeface, Typeface.BOLD)
        }
    }

    private fun makeIcon() {
        loadIconAttributes()
        val paddingVertical = resources.getDimension(R.dimen.toast_vertical_padding).toInt()
        val paddingHorizontal1 =
            resources.getDimension(R.dimen.toast_horizontal_padding_icon_side).toInt()
        val paddingNoIcon =
            resources.getDimension(R.dimen.toast_horizontal_padding_empty_side).toInt()
        val iconSize = resources.getDimension(R.dimen.icon_size).toInt()
        if (iconStart != 0) {
            val drawable = ContextCompat.getDrawable(context, iconStart)
            if (drawable != null) {
                drawable.setBounds(0, 0, iconSize, iconSize)
                TextViewCompat.setCompoundDrawablesRelative(textView!!, drawable, null, null, null)
                if (isRTL) {
                    rootLayout!!.setPadding(paddingNoIcon,
                        paddingVertical,
                        paddingHorizontal1,
                        paddingVertical)
                } else {
                    rootLayout!!.setPadding(paddingHorizontal1,
                        paddingVertical,
                        paddingNoIcon,
                        paddingVertical)
                }
            }
        }
        if (iconEnd != 0) {
            val drawable = ContextCompat.getDrawable(context, iconEnd)
            if (drawable != null) {
                drawable.setBounds(0, 0, iconSize, iconSize)
                TextViewCompat.setCompoundDrawablesRelative(textView!!, null, null, drawable, null)
                if (isRTL) {
                    rootLayout!!.setPadding(paddingHorizontal1,
                        paddingVertical,
                        paddingNoIcon,
                        paddingVertical)
                } else {
                    rootLayout!!.setPadding(paddingNoIcon,
                        paddingVertical,
                        paddingHorizontal1,
                        paddingVertical)
                }
            }
        }
        if (iconStart != 0 && iconEnd != 0) {
            val drawableLeft = ContextCompat.getDrawable(context, iconStart)
            val drawableRight = ContextCompat.getDrawable(context, iconEnd)
            if (drawableLeft != null && drawableRight != null) {
                drawableLeft.setBounds(0, 0, iconSize, iconSize)
                drawableRight.setBounds(0, 0, iconSize, iconSize)
                textView!!.setCompoundDrawables(drawableLeft, null, drawableRight, null)
                rootLayout!!.setPadding(paddingHorizontal1,
                    paddingVertical,
                    paddingHorizontal1,
                    paddingVertical)
            }
        }
    }

    /**
     * loads style attributes from styles.xml if a style resource is used.
     */
    private fun loadShapeAttributes() {
        if (style == 0) {
            return
        }
        val defaultBackgroundColor =
            ContextCompat.getColor(context, R.color.default_background_color)
        val defaultCornerRadius = resources.getDimension(R.dimen.default_corner_radius).toInt()
        solidBackground = typedArray!!.getBoolean(R.styleable.StyleableToast_solidBackground, false)
        backgroundColor1 = typedArray!!.getColor(R.styleable.StyleableToast_colorBackground,
            defaultBackgroundColor)
        cornerRadius = typedArray!!.getDimension(R.styleable.StyleableToast_radius,
            defaultCornerRadius.toFloat()).toInt()
        length = typedArray!!.getInt(R.styleable.StyleableToast_length, 0)
        gravity1 = typedArray!!.getInt(R.styleable.StyleableToast_gravity, Gravity.BOTTOM)
        if (gravity1 == 1) {
            gravity1 = Gravity.CENTER
        } else if (gravity1 == 2) {
            gravity1 = Gravity.TOP
        }
        if (typedArray!!.hasValue(R.styleable.StyleableToast_strokeColor) && typedArray!!.hasValue(R.styleable.StyleableToast_strokeWidth)) {
            strokeWidth =
                typedArray!!.getDimension(R.styleable.StyleableToast_strokeWidth, 0f).toInt()
            strokeColor =
                typedArray!!.getColor(R.styleable.StyleableToast_strokeColor, Color.TRANSPARENT)
        }
    }

    private fun loadTextViewAttributes() {
        if (style == 0) {
            return
        }
        textColor =
            typedArray!!.getColor(R.styleable.StyleableToast_textColor, textView!!.currentTextColor)
        textBold = typedArray!!.getBoolean(R.styleable.StyleableToast_textBold, false)
        textSize = typedArray!!.getDimension(R.styleable.StyleableToast_textSize, 0f)
        font = typedArray!!.getResourceId(R.styleable.StyleableToast_font, 0)
        isTextSizeFromStyleXml = textSize > 0
    }

    private fun loadIconAttributes() {
        if (style == 0) {
            return
        }
        iconStart = typedArray!!.getResourceId(R.styleable.StyleableToast_iconStart, 0)
        iconEnd = typedArray!!.getResourceId(R.styleable.StyleableToast_iconEnd, 0)
    }

    class Builder(val context: Context) {
        var cornerRadius = -1
        var backgroundColor = 0
        var strokeColor = 0
        var strokeWidth = 0
        var iconStart = 0
        var iconEnd = 0
        var textColor = 0
        var font = 0
        var length = Toast.LENGTH_LONG
        var textSize = 0f
        var solidBackground = false
        var textBold = false
        var text: String? = null
        var gravity = Gravity.BOTTOM
        private var toast: StyleableToast? = null
        private var showTime = 0L
        var viewId = -1
        fun text(text: String?): Builder {
            this.text = text
            return this
        }

        fun textColor(@ColorInt textColor: Int): Builder {
            this.textColor = textColor
            return this
        }

        fun textBold(): Builder {
            textBold = true
            return this
        }

        fun textSize(textSize: Float): Builder {
            this.textSize = textSize
            return this
        }

        /**
         * @param font A font resource id like R.font.somefont as introduced with the new font api in Android 8
         */
        fun font(@FontRes font: Int): Builder {
            this.font = font
            return this
        }

        fun backgroundColor(@ColorInt backgroundColor: Int): Builder {
            this.backgroundColor = backgroundColor
            return this
        }

        /**
         * This call will make the StyleableToast's background completely solid without any opacity.
         */
        fun solidBackground(): Builder {
            solidBackground = true
            return this
        }

        fun stroke(strokeWidth: Int, @ColorInt strokeColor: Int): Builder {
            this.strokeWidth = toDp(context, strokeWidth)
            this.strokeColor = strokeColor
            return this
        }

        fun setView(viewId: Int): Builder {
            this.viewId = viewId
            return this
        }

        /**
         * @param cornerRadius Sets the corner radius of the StyleableToast's shape.
         */
        fun cornerRadius(cornerRadius: Int): Builder {
            this.cornerRadius = toDp(context, cornerRadius)
            return this
        }

        fun iconStart(@DrawableRes iconStart: Int): Builder {
            this.iconStart = iconStart
            return this
        }

        fun iconEnd(@DrawableRes iconEnd: Int): Builder {
            this.iconEnd = iconEnd
            return this
        }

        /**
         * Sets where the StyleableToast will appear on the screen
         */
        fun gravity(gravity: Int): Builder {
            this.gravity = gravity
            return this
        }

        /**
         * @param length [Toast.LENGTH_SHORT] or [Toast.LENGTH_LONG]
         */
        fun length(length: Int): Builder {
            this.length = length
            return this
        }

        fun getToast(): StyleableToast {
            if (toast != null && System.currentTimeMillis() - showTime < 3 * 1000) {
                toast!!.textView!!.text = text
                return toast!!
            }
            toast = StyleableToast(this)
            return toast!!
        }

        fun show() {
            getToast().show()
            showTime = System.currentTimeMillis()
        }

        fun build(): StyleableToast {
            toast = StyleableToast(this)
            return toast!!
        }
    }

    companion object {
        fun makeText(
            context: Context,
            text: String,
            length: Int,
            @StyleRes style: Int
        ): StyleableToast {
            return StyleableToast(context, text, length, style)
        }

        fun makeText(context: Context, text: String, @StyleRes style: Int): StyleableToast {
            return StyleableToast(context, text, Toast.LENGTH_SHORT, style)
        }

        private fun toDp(context: Context, value: Int): Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                value.toFloat(),
                context.resources.displayMetrics).toInt()
        }

        private val isRTL: Boolean
            private get() = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL
    }
}