package com.joker.it.apps.jokerapp.activitys

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.joker.it.apps.jokerapp.Constants.JOKER_URL
import com.joker.it.apps.jokerapp.R
import com.joker.it.apps.jokerapp.ShowingInterface
import com.joker.it.apps.jokerapp.activitys.fragments.JokerWebFragment
import com.joker.it.apps.jokerapp.activitys.fragments.StartScreenFragment
import org.apache.commons.validator.routines.UrlValidator
import java.util.*

class JokerActivity : FragmentActivity(), ShowingInterface {


    private lateinit var firebaseConfig: FirebaseRemoteConfig
    private lateinit var jokerSharedPreferences: SharedPreferences

    lateinit var tmpUrl: String
    private var jokerUrl: Any? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.joker_activity)

        initFirebaseConfig()

        if (savedInstanceState == null) {

            showStartScreen()

        }
    }

    private fun initFirebaseConfig() {


        firebaseConfig = FirebaseRemoteConfig.getInstance()

        firebaseConfig.setConfigSettingsAsync(remoteConfigSettings {
            minimumFetchIntervalInSeconds = 2000
        })

        firebaseConfig.setDefaultsAsync(JOKER_URL)
        firebaseConfig.activate()

        firebaseConfig.fetchAndActivate()
            .addOnCompleteListener {

                tmpUrl = firebaseConfig.getString("joker_url")

                jokerUrl = if (UrlValidator(
                        arrayOf(
                            "http",
                            "https"
                        )
                    ).isValid(tmpUrl)
                ) tmpUrl else {
                    try {
                        String(
                            Base64.decode(
                                tmpUrl,
                                Base64.DEFAULT
                            )
                        )
                    } catch (e: IllegalArgumentException) {
                        Log.e("VALUE_ERROR", e.stackTraceToString())
                    }
                }
                jokerSharedPreferences = Objects.requireNonNull(this)!!
                    .getSharedPreferences("jokerSharedPreferences", MODE_PRIVATE)

                val editor: SharedPreferences.Editor = jokerSharedPreferences.edit()
                editor.putString("joker_url", jokerUrl.toString())
                editor.apply()
            }


    }

    override fun showStartScreen() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, StartScreenFragment())
            .commit()
    }

    override fun showWebView() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, JokerWebFragment())
            .commit()
    }
}