package com.yandex.retailmedia.sample.retailmedia

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

/**
 * `coerceInputValues`, because RM says "nothing here" with an explicit `null` — for `errors`, `items`
 * and the slot's `result` — where our model declares an empty list.
 */
private val rmJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
}

// [Step 2.1]
/**
 * The s2s request: `POST https://yandex.ru/retail/{page-id}` with the ad unit's `imp-id` and the
 * client's `bidder-token` in the body. The response carries `ready_response` for the SDK and
 * `creatives[]` with `offer_id`.
 */
class RealRetailMediaClient(
    private val config: RetailMediaConfig,
    private val http: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) { json(rmJson) }
        install(HttpTimeout) { requestTimeoutMillis = REQUEST_TIMEOUT_MS }
    },
) : RetailMediaClient {

    /**
     * The body is read as text rather than via content negotiation: RM doesn't always label what it
     * returns — ads come as `200` with JSON and **no** `Content-Type`, and "nothing to show" comes as
     * `204` with an empty body. Negotiating by content type would drop live ads in the first case and
     * fail in the second.
     */
    override suspend fun requestAds(context: AdRequestContext): RmAds? {
        val url = "${config.baseUrl}/${context.pageId}"
        logger.debug(
            "RM → POST {} imp-id={} retail-feed-id={} text={} category-id={} " +
                "ad-session-id={} ad-session-hit-number={} bidder-token={}",
            url,
            context.impId,
            config.retailFeedId,
            context.text,
            context.categoryId,
            context.adSessionId,
            context.adSessionHitNumber,
            maskBidderToken(context.bidderToken),
        )

        val response = http.post(url) {
            header("X-Y-Platform", "mobile")
            header("x-forwarded-proto", "https")
            contentType(ContentType.Application.Json)
            setBody(
                RmS2sRequest(
                    impId = context.impId,
                    text = context.text,
                    bidderToken = context.bidderToken,
                    categoryId = context.categoryId,
                    adSessionId = context.adSessionId,
                    adSessionHitNumber = context.adSessionHitNumber,
                    retailFeedId = config.retailFeedId,
                ),
            )
        }
        val body = response.bodyAsText()
        logger.debug(
            "RM ← {} page={} content-type={} body={}",
            response.status,
            context.pageId,
            response.headers[HttpHeaders.ContentType] ?: "none",
            body.take(MAX_LOGGED_BODY_LENGTH).ifBlank { "<empty>" },
        )

        if (!response.status.isSuccess()) {
            logger.warn(
                "RetailMedia refused the request for page {} with {}: {}",
                context.pageId,
                response.status,
                body.take(MAX_LOGGED_ERROR_LENGTH),
            )
            return null
        }
        if (body.isBlank()) {
            logger.debug("RM has no ad for page {} ({})", context.pageId, response.status)
            return null
        }
        val parsed = rmJson.decodeFromString<RmS2sResponse>(body)
        if (!parsed.ok) {
            logger.warn(
                "RetailMedia could not serve page {}: {}",
                context.pageId,
                parsed.errors.joinToString().ifBlank { parsed.status },
            )
            return null
        }
        return parsed.toRmAds().also { ads ->
            logger.debug(
                "RM ads for page {}: readyResponse={} creatives={}",
                context.pageId,
                if (ads.readyResponse == null) "absent" else "${ads.readyResponse.length} chars",
                ads.creatives.map { "offer=${it.offerId}@${it.position}" },
            )
        }
    }

    override suspend fun reportOutOfStock(url: String) {
        logger.debug("RM out-of-stock report: {}", url)
        runCatching { http.get(url) }
            .onFailure { logger.debug("RM out-of-stock report failed: {}", it.message) }
    }

    private companion object {
        private const val REQUEST_TIMEOUT_MS = 3_000L
        private const val MAX_LOGGED_ERROR_LENGTH = 200
        private const val MAX_LOGGED_BODY_LENGTH = 2_000
        private val logger = LoggerFactory.getLogger(RealRetailMediaClient::class.java)
    }
}

/**
 * Keeps `bidder-token` out of the logs: it carries the device's advertising identifiers, user agent
 * and IP, which have no place in a log file.
 */
internal fun maskBidderToken(token: String): String =
    if (token.length <= BIDDER_TOKEN_HEAD) "<${token.length} chars>"
    else "${token.take(BIDDER_TOKEN_HEAD)}… <${token.length} chars>"

private const val BIDDER_TOKEN_HEAD = 8
