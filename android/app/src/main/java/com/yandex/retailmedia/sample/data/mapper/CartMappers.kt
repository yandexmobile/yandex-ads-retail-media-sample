package com.yandex.retailmedia.sample.data.mapper

import com.yandex.retailmedia.sample.data.dto.CartDto
import com.yandex.retailmedia.sample.data.dto.CartItemDto
import com.yandex.retailmedia.sample.data.dto.CheckoutResultDto
import com.yandex.retailmedia.sample.domain.model.Cart
import com.yandex.retailmedia.sample.domain.model.CartItem
import com.yandex.retailmedia.sample.domain.model.CheckoutResult

fun CartItemDto.toDomain(): CartItem = CartItem(
    id = id,
    name = name,
    price = price,
    picture = picture,
    quantity = quantity,
)

fun CartDto.toDomain(): Cart = Cart(items = items.map { it.toDomain() })

fun CheckoutResultDto.toDomain(): CheckoutResult = CheckoutResult(ok = ok, message = message)
