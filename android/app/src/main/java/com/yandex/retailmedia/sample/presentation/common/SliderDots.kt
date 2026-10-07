package com.yandex.retailmedia.sample.presentation.common

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.yandex.retailmedia.sample.R

class SliderDots(
    private val dots: LinearLayout,
    private val slider: RecyclerView,
) {

    private var active = RecyclerView.NO_POSITION

    init {
        slider.viewTreeObserver.addOnPreDrawListener {
            highlight()
            true
        }
    }

    fun setCount(count: Int) {
        dots.isVisible = count > 1
        active = RecyclerView.NO_POSITION
        if (count <= 1) {
            dots.removeAllViews()
            return
        }
        while (dots.childCount > count) dots.removeViewAt(dots.childCount - 1)
        while (dots.childCount < count) dots.addView(inflateDot())
    }

    private fun inflateDot(): View =
        LayoutInflater.from(dots.context).inflate(R.layout.item_slider_dot, dots, false)

    private fun highlight() {
        val current = slider.currentSlidePosition()
        if (current == active || current !in 0 until dots.childCount) return
        active = current
        dots.children.forEachIndexed { index, dot ->
            val color = if (index == current) R.color.slider_dot_active else R.color.slider_dot
            dot.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(dots.context, color))
        }
    }
}
