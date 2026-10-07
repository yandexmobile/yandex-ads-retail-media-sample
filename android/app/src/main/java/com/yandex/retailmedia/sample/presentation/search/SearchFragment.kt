package com.yandex.retailmedia.sample.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.yandex.retailmedia.sample.R
import com.yandex.retailmedia.sample.databinding.ScreenSearchBinding
import com.yandex.retailmedia.sample.presentation.common.GridPaginationScrollListener
import com.yandex.retailmedia.sample.presentation.common.ProductAdapter
import com.yandex.retailmedia.sample.presentation.common.ProductsUiState
import com.yandex.retailmedia.sample.presentation.common.showProductsEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var binding: ScreenSearchBinding? = null
    private val viewModel: SearchViewModel by viewModels()
    private val adapter by lazy {
        ProductAdapter(
            onIncrease = { viewModel.increase(it) },
            onDecrease = { viewModel.decrease(it) },
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ScreenSearchBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = binding ?: return
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.search(binding.searchInput.text.toString())
                hideKeyboard()
                true
            } else {
                false
            }
        }

        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recycler.layoutManager = layoutManager
        binding.recycler.adapter = adapter
        binding.recycler.addOnScrollListener(
            GridPaginationScrollListener(layoutManager, onLoadMore = viewModel::loadMore),
        )
        binding.swipe.setOnRefreshListener {
            if (viewModel.hasQuery) viewModel.refresh() else binding.swipe.isRefreshing = false
        }
        binding.retry.setOnClickListener { viewModel.refresh() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { s -> render(s) }
                }
                launch { viewModel.events.collect { showProductsEvent(binding.root, it) } }
            }
        }

        binding.searchInput.requestFocus()
        binding.searchInput.post { showKeyboard() }
    }

    private fun render(state: ProductsUiState) {
        val binding = binding ?: return
        binding.progress.isVisible = state is ProductsUiState.Loading && !binding.swipe.isRefreshing
        binding.errorView.isVisible = state is ProductsUiState.Error
        binding.emptyView.isVisible = state is ProductsUiState.Idle || state is ProductsUiState.Empty
        binding.emptyText.setText(
            if (state is ProductsUiState.Empty) R.string.state_search_empty else R.string.state_search_start,
        )
        adapter.submitList((state as? ProductsUiState.Content)?.items.orEmpty())
        if (state !is ProductsUiState.Loading) {
            binding.swipe.isRefreshing = false
        }
    }

    private fun showKeyboard() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm?.showSoftInput(binding?.searchInput, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(binding?.searchInput?.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
