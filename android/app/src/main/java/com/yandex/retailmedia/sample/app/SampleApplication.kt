package com.yandex.retailmedia.sample.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig

@HiltAndroidApp
class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppMetrica.activate(
            this,
            AppMetricaConfig.newConfigBuilder("00000000-0000-0000-0000-000000000000").build(),
        )
    }
}
