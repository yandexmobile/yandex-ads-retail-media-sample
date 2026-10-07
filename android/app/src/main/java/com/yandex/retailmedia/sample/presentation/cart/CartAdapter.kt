package com.yandex.retailmedia.sample.presentation.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.yandex.retailmedia.sample.R
import com.yandex.retailmedia.sample.databinding.ItemCartBinding
import com.yandex.retailmedia.sample.domain.model.CartItem
import com.yandex.retailmedia.sample.presentation.common.Money

private const val CART_CURRENCY = "RUB"

class CartAdapter(
    private val onIncrement: (CartItem) -> Unit,
    private val onDecrement: (CartItem) -> Unit,
    private val onRemove: (CartItem) -> Unit,
) : ListAdapter<CartItem, CartAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onIncrement, onDecrement, onRemove)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class ViewHolder(
        private val binding: ItemCartBinding,
        private val onIncrement: (CartItem) -> Unit,
        private val onDecrement: (CartItem) -> Unit,
        private val onRemove: (CartItem) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.name.text = item.name
            binding.price.text = Money.format(item.price, CART_CURRENCY)
            binding.quantity.text = item.quantity.toString()
            binding.image.load(item.picture) {
                placeholder(R.drawable.ic_placeholder_image)
                error(R.drawable.ic_placeholder_image)
            }
            binding.minus.setOnClickListener { onDecrement(item) }
            binding.plus.setOnClickListener { onIncrement(item) }
            binding.remove.setOnClickListener { onRemove(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CartItem>() {
            override fun areItemsTheSame(a: CartItem, b: CartItem) = a.id == b.id
            override fun areContentsTheSame(a: CartItem, b: CartItem) = a == b
        }
    }
}
