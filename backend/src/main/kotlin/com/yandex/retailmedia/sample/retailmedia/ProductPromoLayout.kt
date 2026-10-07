package com.yandex.retailmedia.sample.retailmedia

import com.yandex.retailmedia.sample.model.Product

internal class PromotedProduct(
    val product: Product,
    val position: Int,
)

internal fun atRmPositions(
    base: List<Product>,
    promoted: Map<String, PromotedProduct>,
): List<Product> {
    val items = base.filterNotTo(mutableListOf()) { promoted.containsKey(it.id) }
    for (promotion in promoted.values.sortedBy { it.position }) {
        items.add(promotion.position.coerceIn(0, items.size), promotion.product)
    }
    return items.toList()
}

internal fun organicFirst(
    base: List<Product>,
    promoted: Map<String, PromotedProduct>,
): List<Product> {
    val baseIds = base.mapTo(mutableSetOf()) { it.id }
    val tail = promoted.values
        .filterNot { it.product.id in baseIds }
        .sortedBy { it.position }
        .map { it.product }
    return base.map { promoted[it.id]?.product ?: it } + tail
}
