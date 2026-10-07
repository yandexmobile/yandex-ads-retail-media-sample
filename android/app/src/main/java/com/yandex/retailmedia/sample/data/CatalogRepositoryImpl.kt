package com.yandex.retailmedia.sample.data

import com.yandex.retailmedia.sample.data.ads.AdSessionTracker
import com.yandex.retailmedia.sample.data.ads.RetailMediaBidderTokenProvider
import com.yandex.retailmedia.sample.data.api.StoreApi
import com.yandex.retailmedia.sample.data.dto.AD_REQUEST_PLATFORM
import com.yandex.retailmedia.sample.data.dto.CatalogAdRequestDto
import com.yandex.retailmedia.sample.data.dto.SearchAdRequestDto
import com.yandex.retailmedia.sample.data.mapper.toDomain
import com.yandex.retailmedia.sample.domain.model.CatalogPage
import com.yandex.retailmedia.sample.domain.model.Category
import javax.inject.Inject

// [Step 2]
/**
 * productPromo comes with the catalog itself, so the existing catalog requests carry the SDK
 * `bidderToken` and the ad context. The backend returns sponsored products inside the page along with
 * an `AdSlot` for the SDK.
 */
class CatalogRepositoryImpl @Inject constructor(
    private val api: StoreApi,
    private val bidderTokenProvider: RetailMediaBidderTokenProvider,
    private val session: AdSessionTracker,
) : CatalogRepository {

    override suspend fun home(page: Int, pageSize: Int): CatalogPage =
        api.home(
            CatalogAdRequestDto(
                page = page, pageSize = pageSize, platform = AD_REQUEST_PLATFORM,
                bidderToken = bidderTokenProvider.bidderToken(),
                adSessionId = session.sessionId,
                adSessionHitNumber = session.nextHitNumber(),
            ),
        ).toDomain()

    override suspend fun categories(): List<Category> =
        api.categories().map { it.toDomain() }

    override suspend fun productsInCategory(categoryId: String, page: Int, pageSize: Int): CatalogPage =
        api.productsInCategory(
            categoryId,
            CatalogAdRequestDto(
                page = page, pageSize = pageSize, platform = AD_REQUEST_PLATFORM,
                bidderToken = bidderTokenProvider.bidderToken(),
                adSessionId = session.sessionId,
                adSessionHitNumber = session.nextHitNumber(),
            ),
        ).toDomain()

    override suspend fun search(text: String, page: Int, pageSize: Int): CatalogPage =
        api.search(
            SearchAdRequestDto(
                text = text, page = page, pageSize = pageSize, platform = AD_REQUEST_PLATFORM,
                bidderToken = bidderTokenProvider.bidderToken(),
                adSessionId = session.sessionId,
                adSessionHitNumber = session.nextHitNumber(),
            ),
        ).toDomain()
}
