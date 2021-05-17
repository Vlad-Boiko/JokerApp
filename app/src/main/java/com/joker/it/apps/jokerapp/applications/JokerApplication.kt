package com.joker.it.apps.jokerapp.applications

import android.app.Application
import com.joker.it.apps.jokerapp.Constants.JOKER_ONESIGNAL_ID
import com.joker.it.apps.jokerapp.Constants.JOKER_YANDEX_ID
import com.onesignal.OneSignal
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig


class JokerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        OneSignal.initWithContext(applicationContext)
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.setAppId(JOKER_ONESIGNAL_ID)

        YandexMetrica.activate(
            applicationContext,
            YandexMetricaConfig.newConfigBuilder(JOKER_YANDEX_ID).build()
        )
        YandexMetrica.enableActivityAutoTracking(this)
    }
}
