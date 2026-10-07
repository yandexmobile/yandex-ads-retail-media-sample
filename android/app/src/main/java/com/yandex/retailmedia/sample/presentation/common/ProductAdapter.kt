@file:OptIn(com.yandex.mobile.ads.retailmedia.RetailMediaApi::class)

package com.yandex.retailmedia.sample.presentation.common

import android.annotation.SuppressLint
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.yandex.mobile.ads.common.AdBindingResult
import com.yandex.mobile.ads.retailmedia.CustomAssetView
import com.yandex.mobile.ads.retailmedia.RetailMediaAdViewBinder
import com.yandex.retailmedia.sample.R
import com.yandex.retailmedia.sample.databinding.ItemAdSliderBinding
import com.yandex.retailmedia.sample.databinding.ItemProductBinding
import com.yandex.retailmedia.sample.domain.model.Product

class ProductAdapter(
    private val onIncrease: (Product) -> Unit,
    private val onDecrease: (Product) -> Unit,
) : ListAdapter<FeedItem, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ProductRow -> VIEW_TYPE_PRODUCT
        is AdSliderRow -> VIEW_TYPE_AD_SLIDER
    }

    fun isFullSpan(position: Int): Boolean =
        position in 0 until itemCount && getItemViewType(position) == VIEW_TYPE_AD_SLIDER

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_AD_SLIDER) {
            AdSliderViewHolder(ItemAdSliderBinding.inflate(inflater, parent, false))
        } else {
            ProductViewHolder(
                ItemProductBinding.inflate(inflater, parent, false),
                container = parent,
                onIncrease = onIncrease,
                onDecrease = onDecrease,
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ProductRow -> (holder as ProductViewHolder).bind(item)
            is AdSliderRow -> (holder as AdSliderViewHolder).bind(item)
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        (holder as? AdSliderViewHolder)?.startAutoScroll()
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        (holder as? AdSliderViewHolder)?.stopAutoScroll()
    }

    private class AdSliderViewHolder(binding: ItemAdSliderBinding) : RecyclerView.ViewHolder(binding.root) {

        private val slider = binding.adSlider
        private val sliderAdapter = AdSliderAdapter()
        private val autoScroller = SliderAutoScroller(slider)
        private val dots = SliderDots(binding.adSliderDots, slider)

        init {
            slider.layoutManager = LinearLayoutManager(slider.context, RecyclerView.HORIZONTAL, false)
                .apply { initialPrefetchItemCount = 1 }
            slider.adapter = sliderAdapter
            PagerSnapHelper().attachToRecyclerView(slider)
        }

        fun bind(row: AdSliderRow) {
            sliderAdapter.submitList(row.ads)
            dots.setCount(row.ads.size)
        }

        fun startAutoScroll() = autoScroller.attach()

        fun stopAutoScroll() = autoScroller.detach()
    }

    private class ProductViewHolder(
        private val binding: ItemProductBinding,
        private val container: ViewGroup,
        private val onIncrease: (Product) -> Unit,
        private val onDecrease: (Product) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: ProductRow) {
            val product = row.product
            binding.name.text = product.name
            binding.price.text = Money.format(product.price, product.currencyId)
            binding.oldPrice.apply {
                val old = product.oldPrice
                if (old != null) {
                    text = Money.format(old, product.currencyId)
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    isVisible = true
                } else {
                    isVisible = false
                }
            }
            binding.image.load(product.picture) {
                placeholder(R.drawable.ic_placeholder_image)
                error(R.drawable.ic_placeholder_image)
            }

            binding.adLabel.isVisible = product.sponsored

            binding.quantity.text = row.quantityInCart.toString()

            binding.addToCart.setOnClickListener { onIncrease(product) }

            // [Step 4] [Step 5]
            // productPromo: the sponsored ad is bound on top of a regular product card, and the
            // "Add to cart" button is passed to the SDK as a custom asset. The SDK reports its clicks
            // to AppMetrica itself; the app only does the store action, inside the callback: before
            // rendering, the SDK calls cleanNativeAdView(), which removes listeners from all passed
            // views, so a separately set listener would be wiped and the button wouldn't add the
            // product to the cart.
            if (row.ad != null) {
                val binder = RetailMediaAdViewBinder.Builder(binding.root)
                    .setTitleView(binding.name)
                    .setPriceView(binding.price)
                    .setCustomAssetViews(
                        listOf(
                            CustomAssetView("cart", binding.addToCart) { onIncrease(product) },
                        ),
                    )
                    .build()
                val result = row.ad.bindRetailMediaAd(container, binder)
                if (result is AdBindingResult.Failure) {
                    Log.e(
                        RETAIL_MEDIA_LOG_TAG,
                        "Sponsored card binding failed: ${result.missingAssetName}",
                        result.exception,
                    )
                }
            }

            // After binding: the SDK makes every custom asset it rendered VISIBLE again, so a button
            // hidden before binding would come back next to the stepper.
            val inCart = row.quantityInCart > 0
            binding.addToCart.isVisible = !inCart
            binding.stepper.isVisible = inCart

            binding.plus.setOnClickListener { onIncrease(product) }
            binding.minus.setOnClickListener { onDecrease(product) }
        }
    }

    companion object {
        private const val VIEW_TYPE_PRODUCT = 0
        private const val VIEW_TYPE_AD_SLIDER = 1

        @SuppressLint("DiffUtilEquals")
        private val DIFF = object : DiffUtil.ItemCallback<FeedItem>() {
            override fun areItemsTheSame(a: FeedItem, b: FeedItem) = when {
                a is ProductRow && b is ProductRow -> a.product.id == b.product.id
                else -> a is AdSliderRow && b is AdSliderRow
            }

            override fun areContentsTheSame(a: FeedItem, b: FeedItem) = when {
                a is ProductRow && b is ProductRow ->
                    a.product == b.product && a.quantityInCart == b.quantityInCart && a.ad === b.ad
                a is AdSliderRow && b is AdSliderRow -> a.ads == b.ads
                else -> false
            }
        }
    }
}
