package com.skku.studyhelper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import android.util.Log
import kotlin.math.hypot

class PixelatedImageView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val path = Path()
    private var scaledBitmap: Bitmap? = null
    private var shader: BitmapShader? = null

    init {
        if (drawable != null) {
            setupBitmapAndShader()
        }
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        setupBitmapAndShader()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        setupBitmapAndShader()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        setupBitmapAndShader()
    }

    private fun setupBitmapAndShader() {
        try {
            val drawable = drawable ?: return

            if (drawable !is BitmapDrawable) {
                return
            }

            val bitmap = drawable.bitmap ?: return

            scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
            shader = BitmapShader(scaledBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            paint.shader = shader
            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val drawable = drawable ?: return

        if (shader == null) {
            setupBitmapAndShader()
        }

        if (scaledBitmap == null) {
            return
        }

        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvasOutput = Canvas(outputBitmap)

        val pixelSize = 20
        val centerX = width / 2
        val centerY = height / 2
        val radius = centerX.coerceAtMost(centerY).toFloat()

        path.reset()
        for (y in 0 until height step pixelSize) {
            for (x in 0 until width step pixelSize) {
                val dist = hypot((x - centerX).toDouble(), (y - centerY).toDouble()).toFloat()
                if (dist <= radius) {
                    path.addRect(x.toFloat(), y.toFloat(), (x + pixelSize).toFloat(), (y + pixelSize).toFloat(), Path.Direction.CCW)
                }
            }
        }

        canvasOutput.drawPath(path, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvasOutput.drawPath(path, paint)
        paint.xfermode = null

        canvas.drawBitmap(outputBitmap, 0f, 0f, null)
    }
}
