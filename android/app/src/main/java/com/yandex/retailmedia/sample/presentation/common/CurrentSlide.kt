package com.yandex.retailmedia.sample.presentation.common

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

internal fun RecyclerView.currentSlidePosition(): Int {
    val layoutManager = layoutManager as? LinearLayoutManager ?: return RecyclerView.NO_POSITION
    return layoutManager.findFirstCompletelyVisibleItemPosition()
        .takeIf { it != RecyclerView.NO_POSITION }
        ?: layoutManager.findFirstVisibleItemPosition()
}
