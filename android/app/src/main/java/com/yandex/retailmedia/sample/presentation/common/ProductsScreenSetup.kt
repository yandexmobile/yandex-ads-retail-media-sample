package com.yandex.retailmedia.sample.presentation.common

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.yandex.retailmedia.sample.R
import com.yandex.retailmedia.sample.databinding.ScreenProductsBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private const val GRID_COLUMNS = 2

fun setupProductsGrid(
    binding: ScreenProductsBinding,
    adapter: ProductAdapter,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
) {
    val layoutManager = GridLayoutManager(binding.root.context, GRID_COLUMNS)
    layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int =
            if (adapter.isFullSpan(position)) GRID_COLUMNS else 1
    }.apply {
        isSpanIndexCacheEnabled = true
        isSpanGroupIndexCacheEnabled = true
    }
    binding.recycler.layoutManager = layoutManager
    binding.recycler.adapter = adapter
    binding.recycler.addOnScrollListener(
        GridPaginationScrollListener(layoutManager, onLoadMore = onLoadMore),
    )
    binding.swipe.setOnRefreshListener { onRefresh() }
    binding.retry.setOnClickListener { onRefresh() }
}

fun Fragment.observeProducts(
    binding: ScreenProductsBinding,
    adapter: ProductAdapter,
    state: Flow<ProductsUiState>,
    events: Flow<ProductsEvent>,
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch { state.collect { s -> renderProductsScreen(binding, s, adapter) } }
            launch { events.collect { showProductsEvent(binding.root, it) } }
        }
    }
}

fun showProductsEvent(view: View, event: ProductsEvent) {
    val message = when (event) {
        ProductsEvent.CartActionFailed -> R.string.msg_action_failed
    }
    Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
}
