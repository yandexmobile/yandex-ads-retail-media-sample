@file:OptIn(com.yandex.mobile.ads.retailmedia.RetailMediaApi::class)

package com.yandex.retailmedia.sample.presentation.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.yandex.retailmedia.sample.R
import com.yandex.retailmedia.sample.databinding.ScreenCartBinding
import com.yandex.retailmedia.sample.presentation.common.Money
import com.yandex.retailmedia.sample.presentation.common.bindDisplayAd
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CartFragment : Fragment() {

    private var binding: ScreenCartBinding? = null
    private val viewModel: CartViewModel by viewModels()
    private val adapter = CartAdapter(
        onIncrement = { viewModel.increment(it) },
        onDecrement = { viewModel.decrement(it) },
        onRemove = { viewModel.remove(it) },
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ScreenCartBinding.inflate(inflater, container, false).also { binding = it }.root

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
        binding.swipe.setOnRefreshListener { viewModel.refresh() }
        binding.retry.setOnClickListener { viewModel.refresh() }
        binding.checkout.setOnClickListener { viewModel.checkout() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collect(::render) }
                launch { viewModel.events.collect(::showEvent) }
                launch {
                    viewModel.bannerAd.collect { ad ->
                        bindDisplayAd(binding.adBanner, binding.root, ad)
                    }
                }
            }
        }
    }

    private fun render(state: CartUiState) {
        val binding = binding ?: return
        binding.progress.isVisible = state is CartUiState.Loading && !binding.swipe.isRefreshing
        binding.emptyView.isVisible = state is CartUiState.Empty
        binding.errorView.isVisible = state is CartUiState.Error
        binding.footer.isVisible = state is CartUiState.Content

        if (state is CartUiState.Content) {
            adapter.submitList(state.items)
            binding.total.text =
                getString(R.string.cart_total) + ": " + Money.format(state.total, "RUB")
        } else {
            adapter.submitList(emptyList())
        }
        if (state !is CartUiState.Loading) {
            binding.swipe.isRefreshing = false
        }
    }

    private fun showEvent(event: CartEvent) {
        val binding = binding ?: return
        val message = when (event) {
            CartEvent.CheckedOut -> R.string.msg_order_placed
            CartEvent.ActionFailed -> R.string.msg_action_failed
        }
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
