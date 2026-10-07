package com.yandex.retailmedia.sample.presentation.common

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GridPaginationScrollListener(
    private val layoutManager: GridLayoutManager,
    private val threshold: Int = 4,
    private val onLoadMore: () -> Unit,
) : RecyclerView.OnScrollListener() {

    private var lastTriggerCount = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy <= 0) return
        val totalItemCount = layoutManager.itemCount
        if (totalItemCount < lastTriggerCount) lastTriggerCount = 0

        val visibleItemCount = layoutManager.childCount
        val firstVisible = layoutManager.findFirstVisibleItemPosition()
        val nearEnd = firstVisible + visibleItemCount + threshold >= totalItemCount
        if (nearEnd && totalItemCount > lastTriggerCount) {
            lastTriggerCount = totalItemCount
            onLoadMore()
        }
    }
}
