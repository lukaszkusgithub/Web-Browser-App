package com.example.web_browser.fragment

import com.example.web_browser.`interface`.OnDayNightStateChanged
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.view.*
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import com.example.web_browser.activity.MainActivity
import com.example.web_browser.R
import com.example.web_browser.activity.changeTab
import com.example.web_browser.databinding.FragmentBrowseBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import android.util.Base64
import android.util.Log
import java.util.*


@Suppress("DEPRECATION")
class BrowseFragment(private var query: String) : Fragment(), OnDayNightStateChanged {
    // Declare a late-initialized binding variable of type FragmentBrowseBinding
    lateinit var binding: FragmentBrowseBinding
    var web_favicon: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
            // Load the appropriate URL based on the value of the query variable
            when {
                URLUtil.isValidUrl(query) -> loadUrl(query)
                query.contains(".com", ignoreCase = true) -> {
                    if (!query.startsWith("http://") && !query.startsWith("https://")) query =
                        "http://" + query;
                    loadUrl(query)
                }
                else -> loadUrl("https://www.google.com/search?q=$query")
            }
        }

        // Return the view
        return view
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onResume() {
        super.onResume()
        // Set tab name to ulr
        MainActivity.tabsList[MainActivity.pager.currentItem].name = binding.webView.url.toString()
        MainActivity.tabsButton.text = MainActivity.tabsList.size.toString()

        // Enable to download files
        binding.webView.setDownloadListener { query, _, _, _, _ ->
            startActivity(
                Intent(Intent.ACTION_VIEW).setData(
                    Uri.parse(query)
                )
            )
        }

        // Get a reference to the MainActivity
        val mainActivityRef = requireActivity() as MainActivity

        // Set the visibility of the refresh button to visible
        mainActivityRef.binding.refreshButton.visibility = View.VISIBLE
        // Set an OnClickListener for the refresh button
        mainActivityRef.binding.refreshButton.setOnClickListener {
            // Reload the WebView when the refresh button is clicked
            binding.webView.reload()
        }

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
                            "document.querySelector('meta[name=\"viewport\"]').setAttribute('content'," + " 'width=1024px, initial-scale=' + (document.documentElement.clientWidth / 1024));",
                            null
                        )
                    }
                }

                // Override the doUpdateVisitedHistory method to update the text in bottomSearchBar
                override fun doUpdateVisitedHistory(
                    view: WebView?, url: String?, isReload: Boolean
                ) {
                    super.doUpdateVisitedHistory(view, url, isReload)
                    mainActivityRef.binding.bottomSearchBar.text = SpannableStringBuilder(url)
                    MainActivity.tabsList[MainActivity.pager.currentItem].name = url.toString()
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
//        // Save all Tabs
//        (requireActivity() as MainActivity).saveAllTabs()

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

    // Context menu
    override fun onCreateContextMenu(
        menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        // Get the hit test result from the WebView
        val result = binding.webView.hitTestResult
        // Handle the context menu options based on the hit test result type
        when (result.type) {
            // When the hit test result is an image
            WebView.HitTestResult.IMAGE_TYPE -> {
                // Add context menu options for displaying, saving, sharing, and closing the image
                menu.add(R.string.show_image_long_press)
                menu.add(R.string.save_image_long_press)
                menu.add(R.string.share_long_press)
                menu.add(R.string.close_long_press)
            }
            // When the hit test result is a link (anchor)
            WebView.HitTestResult.SRC_ANCHOR_TYPE, WebView.HitTestResult.ANCHOR_TYPE -> {
                // Add context menu options for opening the link in a new tab, opening it in the background,
                // sharing the link, and closing the context menu
                menu.add(R.string.new_tab_long_press)
                menu.add(R.string.in_background_long_press)
                menu.add(R.string.share_long_press)
                menu.add(R.string.close_long_press)
            }
            // When the hit test result is an editable text or unknown type
            WebView.HitTestResult.EDIT_TEXT_TYPE, WebView.HitTestResult.UNKNOWN_TYPE -> {}
            // For any other hit test result type
            else -> {
                // Add context menu options for opening the link in a new tab, opening it in the background,
                // sharing the link, and closing the context menu
                menu.add(R.string.new_tab_long_press)
                menu.add(R.string.in_background_long_press)
                menu.add(R.string.share_long_press)
                menu.add(R.string.close_long_press)
            }
        }
    }

    // Context menu select items actions
    override fun onContextItemSelected(item: MenuItem): Boolean {

        // Create a message handler
        val message = Handler().obtainMessage()
        // Request the focused node href from the WebView
        binding.webView.requestFocusNodeHref(message)
        // Retrieve the URL and image URL from the message data
        val url = message.data.getString("url")
        val imgUrl = message.data.getString("src")

        // Handle the selected item based on its title
        when (item.title) {
            // When the item is "New Tab"
            "${getResources().getString(R.string.new_tab_long_press)}" -> {
                // Change the tab with the provided URL and create a new BrowseFragment
                changeTab(url.toString(), BrowseFragment(url.toString()))
            }
            // When the item is "In Background"
            "${getResources().getString(R.string.in_background_long_press)}" -> {
                // Change the tab with the provided URL, create a new BrowseFragment, and set isBackground to true
                changeTab(url.toString(), BrowseFragment(url.toString()), isBackground = true)
            }
            // When the item is "Show Image"
            "${getResources().getString(R.string.show_image_long_press)}" -> {
                if (imgUrl != null) {
                    if (imgUrl.contains("base64")) {
                        // If the image URL is in base64 format, decode it and display the image in an AlertDialog
                        val pureBytes = imgUrl.substring(imgUrl.indexOf(",") + 1)
                        val decodedBytes = Base64.decode(pureBytes, Base64.DEFAULT)
                        val finalImg =
                            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                        val imgView = ShapeableImageView(requireContext())
                        imgView.setImageBitmap(finalImg)

                        val imgDialog =
                            MaterialAlertDialogBuilder(requireContext()).setView(imgView).create()
                        imgDialog.show()

                        imgView.layoutParams.width =
                            Resources.getSystem().displayMetrics.widthPixels
                        imgView.layoutParams.height =
                            (Resources.getSystem().displayMetrics.heightPixels * .75).toInt()
                        imgView.requestLayout()

                        imgDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    }
                    // If the image URL is not in base64 format, open it in a new tab by changing the current tab
                    else changeTab(imgUrl, BrowseFragment(imgUrl))
                }
            }
            // When the item is "Save Image"
            "${getResources().getString(R.string.save_image_long_press)}" -> {
                if (imgUrl != null) {
                    if (imgUrl.contains("base64")) {
                        // If the image URL is in base64 format, decode it and save the image to the device's MediaStore
                        val pureBytes = imgUrl.substring(imgUrl.indexOf(",") + 1)
                        val decodedBytes = Base64.decode(pureBytes, Base64.DEFAULT)
                        val finalImg =
                            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                        MediaStore.Images.Media.insertImage(
                            requireActivity().contentResolver, finalImg, "Image", null
                        )
                        Snackbar.make(binding.root, R.string.save_image_alert_long_press, 2000)
                            .show()
                    }
                    // If the image URL is not in base64 format, open it in the default image viewer
                    else startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(imgUrl)))
                }
            }
            // When the item is "Share"
            "${getResources().getString(R.string.share_long_press)}" -> {
                val tempUrl = url ?: imgUrl
                if (tempUrl != null) {
                    if (tempUrl.contains("base64")) {
                        // If the URL is in base64 format, decode it and share the image
                        val pureBytes = tempUrl.substring(tempUrl.indexOf(",") + 1)
                        val decodedBytes = Base64.decode(pureBytes, Base64.DEFAULT)
                        val finalImg =
                            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                        val path = MediaStore.Images.Media.insertImage(
                            requireActivity().contentResolver, finalImg, "Image", null
                        )

                        ShareCompat.IntentBuilder(requireContext())
                            .setChooserTitle(R.string.share_alert_long_press).setType("image/*")
                            .setStream(Uri.parse(path)).startChooser()
                    } else {
                        // If the URL is not in base64 format, share it as plain text
                        ShareCompat.IntentBuilder(requireContext())
                            .setChooserTitle(R.string.share_alert_long_press).setType("text/plain")
                            .setText(tempUrl).startChooser()
                    }
                } else Snackbar.make(binding.root, R.string.link_error_long_press.toString(), 2000)
                    .show()
            }
            // When the item is "Close"
            "${getResources().getString(R.string.close_long_press)}" -> {}
        }
        // Call the superclass method to handle the selected item
        return super.onContextItemSelected(item)
    }
}