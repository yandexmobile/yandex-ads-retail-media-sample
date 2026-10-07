package com.yandex.retailmedia.sample.util

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockWebServer
import org.junit.rules.ExternalResource

class MockBackendRule(private val dispatcher: Dispatcher) : ExternalResource() {
    val server = MockWebServer()

    override fun before() {
        server.dispatcher = dispatcher
        server.start(8089)
        System.setProperty("test.baseUrl", "http://localhost:8089/")
    }

    override fun after() {
        server.shutdown()
    }
}
