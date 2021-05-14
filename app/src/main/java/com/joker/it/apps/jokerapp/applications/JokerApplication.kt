package com.joker.it.apps.jokerapp.applications

import android.app.Application
import com.onesignal.OneSignal
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig


class JokerApplication : Application() {

    private val JOKER_ONESIGNAL_ID = "ca4b5004-a6f0-462b-a60d-815ee870eddd"
    private val JOKER_YANDEX_ID = "857d7c7b-9633-4b8c-8ae0-73936e96406e"

    override fun onCreate() {
        super.onCreate()

        OneSignal.initWithContext(applicationContext)
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.setAppId(JOKER_ONESIGNAL_ID)

        val jokerYanConf = YandexMetricaConfig.newConfigBuilder(JOKER_YANDEX_ID).build()
        YandexMetrica.activate(applicationContext, jokerYanConf)
        YandexMetrica.enableActivityAutoTracking(this)
    }
}