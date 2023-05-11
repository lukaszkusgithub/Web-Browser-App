package com.example.web_browser.fragment

import com.example.web_browser.`interface`.OnDayNightStateChanged
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.web_browser.activity.MainActivity
import com.example.web_browser.R
import com.example.web_browser.databinding.FragmentBrowseBinding
import java.io.ByteArrayOutputStream


class BrowseFragment(private var query: String) : Fragment(), OnDayNightStateChanged {
    // Declare a late-initialized binding variable of type FragmentBrowseBinding
    lateinit var binding: FragmentBrowseBinding
    var web_favicon: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_browse, container, false)
        // Bind the view to the binding variable
        binding = FragmentBrowseBinding.bind(view)
        // Enable the ability to go back in the webview's history
        binding.webView.canGoBack()

        // Register the webview for a context menu (i.e. long-press menu)
        registerForContextMenu(binding.webView)

        // Load the URL into the webview based on the value of the query variable
        binding.webView.apply {
            when {
                // If the query is a valid URL, load the URL
                URLUtil.isValidUrl(query) -> loadUrl(query)
                // If the query contains ".com" (ignoring case), assume it is a URL and load it
                query.contains(".com", ignoreCase = true) -> loadUrl(query)
                // Otherwise, search Google for the query
                else -> loadUrl("https://www.google.com/search?q=$query")
            }
        }
        // Return the view
        return view
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onResume() {
        super.onResume()

        // Get a reference to the MainActivity
        val mainActivityRef = requireActivity() as MainActivity

        // Set up the WebView with appropriate settings and clients
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            webViewClient = object : WebViewClient() {
                override fun onLoadResource(view: WebView?, url: String?) {
                    super.onLoadResource(view, url)
                    if (MainActivity.isDesktopSite) {
                        evaluateJavascript(
                            "document.querySelector('meta[name=\"viewport\"]').setAttribute('content'," +
                                    " 'width=1024px, initial-scale=' + (document.documentElement.clientWidth / 1024));",
                            null
                        )
                    }
                }

                // Override the doUpdateVisitedHistory method to update the text in bottomSearchBar
                override fun doUpdateVisitedHistory(
                    view: WebView?,
                    url: String?,
                    isReload: Boolean
                ) {
                    super.doUpdateVisitedHistory(view, url, isReload)
                    mainActivityRef.binding.bottomSearchBar.text = SpannableStringBuilder(url)

                }

                // Show progress bar when page starts loading
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    // Set progress bar to 0 and make it visible
                    mainActivityRef.binding.progressBar.progress = 0
                    mainActivityRef.binding.progressBar.visibility = View.VISIBLE
                    // Check if the URL contains "you" (ignoring case) and transition to
                    // the end of the layout if true
                    if (url!!.contains("you", false)) {
                        mainActivityRef.binding.root.transitionToEnd()
                    }
                }

                // Hide progress bar when page finished loading
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    mainActivityRef.binding.progressBar.visibility = View.GONE
                    binding.webView.zoomOut()
                }

            }

            // Enable Video play Full screen
            webChromeClient = object : WebChromeClient() {
                // Hide the WebView and show the custom view
                // Add the custom view to the layout
                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    super.onShowCustomView(view, callback)
                    binding.webView.visibility = View.GONE
                    binding.customView.visibility = View.VISIBLE
                    binding.customView.addView(view)

                    // Transitions the root view of the MainActivity to its end position.
                    mainActivityRef.binding.root.transitionToEnd()
                }

                // Show the WebView and hide the custom view
                override fun onHideCustomView() {
                    super.onHideCustomView()
                    binding.webView.visibility = View.VISIBLE
                    binding.customView.visibility = View.GONE
                }

                // Update the progress bar in the MainActivity
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    mainActivityRef.binding.progressBar.progress = newProgress
                }

                // Show web icon in bottom search bar
                override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                    super.onReceivedIcon(view, icon)
                    try {
                        // Set the web icon in the bottom search bar
                        mainActivityRef.binding.webIcon.setImageBitmap(icon)
                        web_favicon = icon
                        // Check if the page is bookmarked and update the bookmark icon
                        MainActivity.bookmarkIndex = mainActivityRef.isBookmarked(view?.url!!)
                        if (MainActivity.bookmarkIndex != -1) {
                            val array = ByteArrayOutputStream()
                            icon!!.compress(Bitmap.CompressFormat.PNG, 100, array)
                            // Update the image of the bookmark with the new icon
                            MainActivity.bookmarkList[MainActivity.bookmarkIndex].image =
                                array.toByteArray()
                        }
                    } catch (e: Exception) {
                        // Handle exceptions that might occur while updating the icon
                    }
                }
            }

            // Load the appropriate URL based on the value of the query variable
            when {
                URLUtil.isValidUrl(query) -> loadUrl(query)
                query.contains(".com", ignoreCase = true) -> {
                    if (!query.startsWith("http://") && !query.startsWith("https://"))
                        query = "http://" + query;
                    loadUrl(query)
                }
                else -> loadUrl("https://www.google.com/search?q=$query")
            }

            // Set an OnTouchListener on the WebView element
            binding.webView.setOnTouchListener { _, motionEvent ->
                // Pass the touch event to the parent view using onTouchEvent()
                mainActivityRef.binding.root.onTouchEvent(motionEvent)
                // Indicate that the touch event has been handled and no further
                // processing is required
                return@setOnTouchListener false
            }

        }
    }

    override fun onPause() {
        super.onPause()
        // Save all Bookmarks
        (requireActivity() as MainActivity).saveAllBookmarks()

        binding.webView.apply {
            // clear search matches
            clearMatches()
            // clear web history
            clearHistory()
            // clear form data
            clearFormData()
            // clear SSL preferences
            clearSslPreferences()
            // clear cache (including disk cache)
            clearCache(true)
            // remove all cookies
            CookieManager.getInstance().removeAllCookies(null)
            // delete all web storage
            WebStorage.getInstance().deleteAllData()
        }
    }

    // NIGHT & LIGHT mode when changing UI config
    override fun onDayNightApplied() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}