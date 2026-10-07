@file:OptIn(com.yandex.mobile.ads.retailmedia.RetailMediaApi::class)

package com.yandex.retailmedia.sample.presentation.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.yandex.retailmedia.sample.R
import com.yandex.retailmedia.sample.databinding.ScreenCategoriesBinding
import com.yandex.retailmedia.sample.domain.model.Category
import com.yandex.retailmedia.sample.presentation.common.bindDisplayAd
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoriesFragment : Fragment() {

    private var binding: ScreenCategoriesBinding? = null
    private val viewModel: CategoriesViewModel by viewModels()
    private val adapter = CategoryAdapter(onClick = ::openCategory)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ScreenCategoriesBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = binding ?: return
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_search) {
                findNavController().navigate(R.id.searchFragment)
                true
            } else {
                false
            }
        }
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter
        binding.swipe.setOnRefreshListener { viewModel.load() }
        binding.retry.setOnClickListener { viewModel.load() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collect(::render) }
                launch {
                    viewModel.bannerAd.collect { ad ->
                        bindDisplayAd(binding.adBanner, binding.root, ad)
                    }
                }
            }
        }
    }

    private fun render(state: CategoriesUiState) {
        val binding = binding ?: return
        binding.progress.isVisible = state is CategoriesUiState.Loading && !binding.swipe.isRefreshing
        binding.emptyView.isVisible = state is CategoriesUiState.Empty
        binding.errorView.isVisible = state is CategoriesUiState.Error
        adapter.submitList((state as? CategoriesUiState.Content)?.items.orEmpty())
        if (state !is CategoriesUiState.Loading) {
            binding.swipe.isRefreshing = false
        }
    }

    private fun openCategory(category: Category) {
        findNavController().navigate(
            R.id.productListFragment,
            bundleOf("categoryId" to category.id, "categoryName" to category.name),
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
