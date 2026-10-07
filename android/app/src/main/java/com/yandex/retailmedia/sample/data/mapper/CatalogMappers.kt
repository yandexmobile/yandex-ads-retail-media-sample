package com.yandex.retailmedia.sample.data.mapper

import com.yandex.retailmedia.sample.data.dto.AdSlotDto
import com.yandex.retailmedia.sample.data.dto.CatalogResponseDto
import com.yandex.retailmedia.sample.data.dto.CategoryDto
import com.yandex.retailmedia.sample.data.dto.PageDto
import com.yandex.retailmedia.sample.data.dto.ProductDto
import com.yandex.retailmedia.sample.domain.model.AdSlot
import com.yandex.retailmedia.sample.domain.model.CatalogPage
import com.yandex.retailmedia.sample.domain.model.Category
import com.yandex.retailmedia.sample.domain.model.Page
import com.yandex.retailmedia.sample.domain.model.Product

fun CategoryDto.toDomain(): Category = Category(id = id, name = name)

fun ProductDto.toDomain(): Product = Product(
    id = id,
    name = name,
    price = price,
    oldPrice = oldPrice,
    currencyId = currencyId,
    picture = picture,
    categoryId = categoryId,
    url = url,
    available = available,
    vendor = vendor,
    sponsored = sponsored,
)

fun PageDto<ProductDto>.toProductPage(): Page<Product> = Page(
    items = items.map { it.toDomain() },
    page = page,
    pageSize = pageSize,
    hasMore = hasMore,
)

fun AdSlotDto.toDomain(): AdSlot = AdSlot(adUnitId = adUnitId, readyResponse = readyResponse)

fun CatalogResponseDto.toDomain(): CatalogPage = CatalogPage(
    products = products.toProductPage(),
    adSlot = adSlot?.toDomain(),
)
