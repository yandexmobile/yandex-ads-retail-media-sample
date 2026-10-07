package com.yandex.retailmedia.sample.data.api

import kotlinx.serialization.json.Json

val storeJson: Json = Json {
    ignoreUnknownKeys = true
}
