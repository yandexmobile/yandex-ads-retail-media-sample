package com.yandex.retailmedia.sample.retailmedia

import io.ktor.server.config.ApplicationConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class RetailMediaConfig(
    val baseUrl: String,
    val retailFeedId: String,
    val placements: Map<PlacementKey, AdPlacement>,
) {
    fun placement(key: PlacementKey): AdPlacement? {
        if (retailFeedId.isBlank()) return null
        return placements[key]?.takeIf { it.enabled }
    }

    companion object {
        fun from(config: ApplicationConfig): RetailMediaConfig {
            fun value(path: String): String =
                config.propertyOrNull("retailmedia.$path")?.getString().orEmpty()
            val placements = PlacementKey.all.associateWith { key ->
                val path = "placements.${key.platform.key}.${key.screen.key}.${key.format.key}"
                AdPlacement(
                    pageId = value("$path.pageId"),
                    impId = value("$path.impId"),
                    adUnitId = value("$path.adUnitId"),
                )
            }
            return RetailMediaConfig(
                baseUrl = value("baseUrl").ifBlank { DEFAULT_BASE_URL },
                retailFeedId = value("retailFeedId"),
                placements = placements,
            )
        }

        private const val DEFAULT_BASE_URL = "https://yandex.ru/retail"
    }
}

@Serializable
enum class Platform(val key: String) {
    @SerialName("android")
    ANDROID("android"),

    @SerialName("ios")
    IOS("ios"),
}

enum class AdFormat(val key: String) {
    PRODUCT_PROMO("productPromo"),
    DISPLAY("display"),
}

@Serializable
enum class Screen(val key: String, val formats: Set<AdFormat>) {
    @SerialName("home")
    HOME("home", setOf(AdFormat.PRODUCT_PROMO, AdFormat.DISPLAY)),

    @SerialName("category")
    CATEGORY("category", setOf(AdFormat.PRODUCT_PROMO)),

    @SerialName("search")
    SEARCH("search", setOf(AdFormat.PRODUCT_PROMO)),

    @SerialName("categories")
    CATEGORIES("categories", setOf(AdFormat.DISPLAY)),

    @SerialName("cart")
    CART("cart", setOf(AdFormat.DISPLAY)),
}

data class PlacementKey(val platform: Platform, val screen: Screen, val format: AdFormat) {

    companion object {
        val all: List<PlacementKey> = Platform.entries.flatMap { platform ->
            Screen.entries.flatMap { screen ->
                screen.formats.map { format ->
                    PlacementKey(
                        platform = platform,
                        screen = screen,
                        format = format,
                    )
                }
            }
        }
    }
}

class AdPlacement(val pageId: String, val impId: String, val adUnitId: String) {
    val enabled: Boolean get() = pageId.isNotBlank() && impId.isNotBlank() && adUnitId.isNotBlank()
}
