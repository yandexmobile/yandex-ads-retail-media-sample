package com.yandex.retailmedia.sample.feed

import com.yandex.retailmedia.sample.catalog.CatalogService

/**
 * Service for building YML (Yandex Market Language) feeds.
 * YML is an XML-based format used to describe product offers and categories for Yandex Market.
 * Official documentation: https://yandex.ru/support/marketplace/assortment/fields/yml.html
 */
class FeedService(private val catalog: CatalogService) {

    fun buildYml(imageBaseUrl: String): String = buildString {
        append("""<?xml version="1.0" encoding="UTF-8"?>""").append('\n')
        append("""<yml_catalog date="2026-01-01T00:00">""").append('\n')
        append("  <shop>\n")
        append("    <name>RetailMedia Sample Store</name>\n")
        append("    <currencies>\n      <currency id=\"RUB\" rate=\"1\"/>\n    </currencies>\n")
        append("    <categories>\n")
        for (category in catalog.categories()) {
            append("      <category id=\"${category.id}\">${escape(category.name)}</category>\n")
        }
        append("    </categories>\n")
        append("    <offers>\n")
        for (product in catalog.allProducts()) {
            append("      <offer id=\"${product.id}\" available=\"${product.available}\">\n")
            append("        <url>${escape(product.url)}</url>\n")
            append("        <price>${product.price}</price>\n")
            product.oldPrice?.let { append("        <oldprice>$it</oldprice>\n") }
            append("        <currencyId>${product.currencyId}</currencyId>\n")
            append("        <categoryId>${product.categoryId}</categoryId>\n")
            append("        <picture>${escape(imageBaseUrl + product.picture)}</picture>\n")
            append("        <name>${escape(product.name)}</name>\n")
            product.vendor?.let { append("        <vendor>${escape(it)}</vendor>\n") }
            append("      </offer>\n")
        }
        append("    </offers>\n")
        append("  </shop>\n")
        append("</yml_catalog>\n")
    }

    private fun escape(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
