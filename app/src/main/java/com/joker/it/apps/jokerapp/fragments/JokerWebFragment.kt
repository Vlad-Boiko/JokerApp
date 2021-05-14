package com.joker.it.apps.jokerapp.fragments

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.joker.it.apps.jokerapp.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class JokerWebFragment : Fragment() {

    private val FILECHOOSER_RESULTCODE = 5

    private var jokerPathCallback: ValueCallback<Array<Uri>>? = null
    private lateinit var webView: WebView
    private lateinit var jokerUrl: String

    private var uri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootInflater = inflater.inflate(R.layout.joker_web_fragment, container, false)

        webView = rootInflater.findViewById(R.id.wv_joker)

        val cookieManager = CookieManager.getInstance()

        CookieManager.setAcceptFileSchemeCookies(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        setUpWebView()

        jokerUrl = Objects.requireNonNull(activity)!!
            .getSharedPreferences("jokerSharedPreferences", Context.MODE_PRIVATE)
            .getString("joker_url", "").toString()

        webView.loadUrl(jokerUrl)

        return rootInflater
    }

    companion object {
        fun newInstance() = JokerWebFragment()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == FILECHOOSER_RESULTCODE) {

            if (jokerPathCallback == null) return

            val result = if (data == null || resultCode != Activity.RESULT_OK) null else data.data

            if (result != null && jokerPathCallback != null) {
                jokerPathCallback!!.onReceiveValue(arrayOf(result))
            }
        }

        jokerPathCallback = null

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setUpWebView() {

        webView.settings.defaultTextEncodingName = "utf-8"
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        webView.settings.displayZoomControls = false
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.builtInZoomControls = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webView.settings.pluginState = WebSettings.PluginState.ON

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

                when {
                    url.contains("facebook.com") -> {
                        Log.i("Social Network", "Facebook")
                    }
                    url.contains("twitter.com") -> {
                        Log.i("Social Network", "Twitter")
                    }
                    url.contains("google.com") -> {
                        Log.i("Social Network", "Google")
                    }
                    url.contains("youtube.com") -> {
                        Log.i("Social Network", "Youtube")
                    }
                    url.contains("flickr.com") -> {
                        Log.i("Social Network", "Flickr")
                    }
                    url.contains("apple.com") -> {
                        Log.i("Social Network", "Apple")
                    }
                    else -> {
                        view.loadUrl(url)
                    }
                }

                Log.i("LOADING_PAGE", url)

                return true
            }
        }

        webView.webChromeClient = object : WebChromeClient() {

            override fun onPermissionRequest(request: PermissionRequest?) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (request != null) {
                        Dexter.withContext(context)
                            .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                            .withListener(object : MultiplePermissionsListener {

                                override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                                    Log.i("REQUEST_TEST", request.resources[0])
                                }

                                override fun onPermissionRationaleShouldBeShown(
                                    list: List<com.karumi.dexter.listener.PermissionRequest>,
                                    permissionToken: PermissionToken
                                ) {
                                }

                            }).check()
                    }
                }
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {

                jokerPathCallback = filePathCallback

                val actionImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                if (actionImageIntent.resolveActivity(Objects.requireNonNull(activity)!!.packageManager) != null) {

                    var jokerFile: File? = null

                    try {
                        jokerFile = createNewImage()
                    } catch (ex: IOException) {
                        Log.e("TAG_CREATE_IMAGE", " error!", ex)
                    }

                    if (jokerFile != null) {

                        val jRoomUri = FileProvider.getUriForFile(
                            Objects.requireNonNull(context)!!,
                            activity!!.application.packageName + ".provider",
                            jokerFile
                        )

                        uri = jRoomUri

                        actionImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, jRoomUri)
                        actionImageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                        val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                        contentSelectionIntent.type = "image/*"

                        val intentArray = arrayOf(contentSelectionIntent)

                        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                        chooserIntent.putExtra(Intent.EXTRA_INTENT, actionImageIntent)
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)

                        startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE)

                        return true
                    }
                }

                return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
            }
        }

        Objects.requireNonNull(activity)!!
            .onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else if (!webView.canGoBack() && webView.url != jokerUrl) {
                        webView.loadUrl(jokerUrl)
                    } else {
                        Objects.requireNonNull(activity)!!.finish()
                    }
                }
            })
    }


    @Throws(IOException::class)
    private fun createNewImage(): File? {

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        val jRoomFile = "JOKER" + timeStamp + "_"

        val jRoomExternal =
            Objects.requireNonNull(activity)!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(jRoomFile, ".jpg", jRoomExternal)
    }
}