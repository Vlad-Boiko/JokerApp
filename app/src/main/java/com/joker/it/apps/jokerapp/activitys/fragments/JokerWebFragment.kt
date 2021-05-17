package com.joker.it.apps.jokerapp.activitys.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.joker.it.apps.jokerapp.Constants.RESULT_CODE
import com.joker.it.apps.jokerapp.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.joker_web_fragment.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class JokerWebFragment : Fragment(R.layout.joker_web_fragment) {


    private var jokerPathCallback: ValueCallback<Array<Uri>>? = null
    private lateinit var webView: WebView
    private lateinit var uri: Uri
    private lateinit var jokerUrl: String
    private val actionImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    var jokerFile: File? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = wv_joker

        settingsForWebView()
        addCookies()


        Objects.requireNonNull(activity)!!
            .onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        Objects.requireNonNull(activity)!!.finish()
                    }
                }
            })
    }


    private fun settingsForWebView() {
        webView.also {
            it.settings.defaultTextEncodingName = "utf-8"
            it.settings.javaScriptEnabled = true
            it.settings.domStorageEnabled = true
            it.settings.loadWithOverviewMode = true
            it.settings.mediaPlaybackRequiresUserGesture = false
            it.settings.displayZoomControls = false
            it.settings.useWideViewPort = true
            it.settings.builtInZoomControls = true
            it.webChromeClient = MyWebChromeClient()
            it.webViewClient = MyWebViewClient()
        }

        jokerUrl = Objects.requireNonNull(activity)!!
            .getSharedPreferences("jokerSharedPreferences", Context.MODE_PRIVATE)
            .getString("joker_url", "").toString()

        webView.loadUrl(jokerUrl)
    }

    class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)

            return true
        }
    }

    inner class MyWebChromeClient : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest?) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (request != null) {
                    checkAllPermission()
                }
            }
        }

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {

            jokerPathCallback = filePathCallback


            if (actionImageIntent.resolveActivity(
                    Objects.requireNonNull(activity)
                    !!.packageManager
                ) != null
            ) {


                try {
                    jokerFile =
                        File.createTempFile(
                            "JOKER" + SimpleDateFormat(
                                "yyyyMMdd_HHmmss",
                                Locale.getDefault()
                            ).format(Date()) + "_", ".jpg", Objects.requireNonNull(activity)!!
                                .getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        )
                } catch (ex: IOException) {
                    Log.e("TAG_CREATE_IMAGE", " error!", ex)
                }

                if (jokerFile != null) {

                    val jRoomUri = FileProvider.getUriForFile(
                        Objects.requireNonNull(context)!!,
                        activity!!.application.packageName + ".provider",
                        jokerFile!!
                    )

                    uri = jRoomUri

                    actionWithIntent()

                    return true
                }
            }

            return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
        }

    }

    fun checkAllPermission() {
        Dexter.withContext(context)
            .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            .withListener(object : MultiplePermissionsListener {

                override fun onPermissionsChecked(
                    multiplePermissionsReport
                    : MultiplePermissionsReport
                ) {
                }

                override fun onPermissionRationaleShouldBeShown(
                    list: List<com.karumi.dexter.listener.PermissionRequest>,
                    permissionToken: PermissionToken
                ) {
                }

            }).check()
    }

    fun actionWithIntent() {
        actionImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        actionImageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
        contentSelectionIntent.type = "image/*"

        val intentArray = arrayOf(contentSelectionIntent)

        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
        chooserIntent.putExtra(Intent.EXTRA_INTENT, actionImageIntent)
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)

        startActivityForResult(chooserIntent, RESULT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var result: Any?
        if (requestCode == RESULT_CODE) {
            if (jokerPathCallback == null) return
            if (data == null || resultCode != Activity.RESULT_OK) result = null
            else result = data.data
            if (result != null && jokerPathCallback != null) {
                jokerPathCallback!!.onReceiveValue(arrayOf(result as Uri))
            }
        }

        jokerPathCallback = null

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun addCookies() {
        CookieManager.setAcceptFileSchemeCookies(true)
        CookieManager.getInstance()
            .setAcceptThirdPartyCookies(webView, true)
    }
}
