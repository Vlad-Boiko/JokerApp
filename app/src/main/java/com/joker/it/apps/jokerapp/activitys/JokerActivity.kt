package com.joker.it.apps.jokerapp.activitys

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.joker.it.apps.jokerapp.R
import com.joker.it.apps.jokerapp.fragments.JokerWebFragment
import org.apache.commons.validator.routines.UrlValidator
import java.util.*

class JokerActivity : FragmentActivity() {

    private var justStarted = true

    private lateinit var iconStart: ImageView
    private lateinit var startView: LinearLayout


    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var jokerSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.joker_activity)

        startView = findViewById(R.id.start_view)
        iconStart = findViewById(R.id.icon_start)

        configFirebase()

        if (savedInstanceState == null) {

            if (justStarted) {
                val iconStartAlphaAnimation: Animation =
                    AnimationUtils.loadAnimation(this, R.anim.start_scale)

                iconStart.startAnimation(iconStartAlphaAnimation)

                val handler = Handler(Looper.getMainLooper())
                val timer = Timer(false)
                val timerTask: TimerTask = object : TimerTask() {
                    override fun run() {
                        handler.post {
                            startView.visibility = View.GONE

                            supportFragmentManager.beginTransaction()
                                .replace(R.id.main_frame, JokerWebFragment.newInstance())
                                .commitNow()
                        }
                    }
                }

                timer.schedule(timerTask, 2000)
                justStarted = false

            } else {
                startView.visibility = View.GONE
            }
        }
    }

    private fun configFirebase() {

        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }

        remoteConfig.setDefaultsAsync(R.xml.joker_url)
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate()
            .addOnSuccessListener {

                val value = remoteConfig.getString("joker_url")
                val validator = UrlValidator(arrayOf("http", "https"))

                val jokerUrl = if (validator.isValid(value)) value
                else {
                    try {
                        String(Base64.decode(value, Base64.DEFAULT))
                    } catch (e: IllegalArgumentException) {
                        Log.e("VALUE_ERROR", "exc")
                    }
                }

                jokerSharedPreferences = Objects.requireNonNull(this)!!
                    .getSharedPreferences("jokerSharedPreferences", MODE_PRIVATE)

                val editor: SharedPreferences.Editor = jokerSharedPreferences.edit()
                editor.putString("joker_url", jokerUrl.toString())
                editor.apply()
            }
    }
}