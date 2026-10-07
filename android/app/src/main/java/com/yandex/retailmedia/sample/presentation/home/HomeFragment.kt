package com.yandex.retailmedia.sample.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yandex.retailmedia.sample.R
import com.yandex.retailmedia.sample.databinding.ScreenProductsBinding
import com.yandex.retailmedia.sample.presentation.common.ProductAdapter
import com.yandex.retailmedia.sample.presentation.common.observeProducts
import com.yandex.retailmedia.sample.presentation.common.setupProductsGrid
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var binding: ScreenProductsBinding? = null
    private val viewModel: HomeViewModel by viewModels()
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
    ): View = ScreenProductsBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = binding ?: return
        binding.toolbar.title = getString(R.string.app_name)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_search) {
                findNavController().navigate(R.id.searchFragment)
                true
            } else {
                false
            }
        }
        setupProductsGrid(binding, adapter, onLoadMore = viewModel::loadMore, onRefresh = viewModel::refresh)
        observeProducts(binding, adapter, viewModel.state, viewModel.events)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
