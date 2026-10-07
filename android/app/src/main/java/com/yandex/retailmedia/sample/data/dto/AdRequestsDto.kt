package com.yandex.retailmedia.sample.data.dto

import kotlinx.serialization.Serializable

/**
 * The partner interface creates a separate page and ad unit for each platform's app, so every ad
 * request says which app it comes from: the backend picks the ad unit by `platform` and rejects a
 * request without it.
 */
const val AD_REQUEST_PLATFORM = "android"

@Serializable
data class CatalogAdRequestDto(
    val page: Int,
    val pageSize: Int,
    val platform: String,
    val bidderToken: String? = null,
    val adSessionId: String? = null,
    val adSessionHitNumber: Int? = null,
)

@Serializable
data class SearchAdRequestDto(
    val text: String,
    val page: Int,
    val pageSize: Int,
    val platform: String,
    val bidderToken: String? = null,
    val adSessionId: String? = null,
    val adSessionHitNumber: Int? = null,
)

@Serializable
data class AdsRequestDto(
    val screen: String,
    val platform: String,
    val bidderToken: String? = null,
    val adSessionId: String? = null,
    val adSessionHitNumber: Int? = null,
)
