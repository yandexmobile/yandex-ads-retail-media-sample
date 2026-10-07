package com.yandex.retailmedia.sample.data.api

import com.yandex.retailmedia.sample.data.dto.AddToCartRequestDto
import com.yandex.retailmedia.sample.data.dto.AdSlotDto
import com.yandex.retailmedia.sample.data.dto.AdsRequestDto
import com.yandex.retailmedia.sample.data.dto.CartDto
import com.yandex.retailmedia.sample.data.dto.CatalogAdRequestDto
import com.yandex.retailmedia.sample.data.dto.CatalogResponseDto
import com.yandex.retailmedia.sample.data.dto.CategoryDto
import com.yandex.retailmedia.sample.data.dto.CheckoutResultDto
import com.yandex.retailmedia.sample.data.dto.SearchAdRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface StoreApi {

    @POST("home")
    suspend fun home(@Body body: CatalogAdRequestDto): CatalogResponseDto

    @GET("categories")
    suspend fun categories(): List<CategoryDto>

    @POST("categories/{id}/products")
    suspend fun productsInCategory(
        @Path("id") id: String,
        @Body body: CatalogAdRequestDto,
    ): CatalogResponseDto

    @POST("search")
    suspend fun search(@Body body: SearchAdRequestDto): CatalogResponseDto

    @POST("ads")
    suspend fun ads(@Body body: AdsRequestDto): Response<AdSlotDto>

    @GET("cart")
    suspend fun cart(): CartDto

    @POST("cart/items")
    suspend fun addToCart(@Body body: AddToCartRequestDto): CartDto

    @DELETE("cart/items/{id}")
    suspend fun removeFromCart(@Path("id") id: String): CartDto

    @POST("cart/checkout")
    suspend fun checkout(): CheckoutResultDto
}
