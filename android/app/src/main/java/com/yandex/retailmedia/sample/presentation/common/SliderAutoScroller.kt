package com.yandex.retailmedia.sample.presentation.common

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView

class SliderAutoScroller(
    private val slider: RecyclerView,
    private val intervalMs: Long = DEFAULT_INTERVAL_MS,
) : DefaultLifecycleObserver {

    private val handler = Handler(Looper.getMainLooper())
    private val advance = Runnable { scrollToNext() }

    private var lifecycle: Lifecycle? = null
    private var onScreen = false
    private var started = false

    private val dragWatcher = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            when (newState) {
                RecyclerView.SCROLL_STATE_DRAGGING -> handler.removeCallbacks(advance)
                RecyclerView.SCROLL_STATE_IDLE -> schedule()
                else -> Unit
            }
        }
    }

    fun attach() {
        if (onScreen) return
        onScreen = true
        slider.addOnScrollListener(dragWatcher)
        val owner = slider.findViewTreeLifecycleOwner()
        lifecycle = owner?.lifecycle
        started = owner == null
        lifecycle?.addObserver(this)
        schedule()
    }

    fun detach() {
        onScreen = false
        slider.removeOnScrollListener(dragWatcher)
        lifecycle?.removeObserver(this)
        lifecycle = null
        handler.removeCallbacks(advance)
    }

    override fun onStart(owner: LifecycleOwner) {
        started = true
        schedule()
    }

    override fun onStop(owner: LifecycleOwner) {
        started = false
        handler.removeCallbacks(advance)
    }

    private fun schedule() {
        handler.removeCallbacks(advance)
        if (onScreen && started) {
            handler.postDelayed(advance, intervalMs)
        }
    }

    private fun scrollToNext() {
        val count = slider.adapter?.itemCount ?: return
        if (count <= 1) return
        val current = slider.currentSlidePosition()
        if (current != RecyclerView.NO_POSITION) {
            val next = (current + 1) % count
            if (next == 0) slider.scrollToPosition(0) else slider.smoothScrollToPosition(next)
        }
        schedule()
    }

    private companion object {
        private const val DEFAULT_INTERVAL_MS = 3_000L
    }
}
