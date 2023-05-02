package com.example.web_browser

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.example.web_browser.databinding.FragmentBrowseBinding


class BrowseFragment(private var query: String) : Fragment() {

    private lateinit var binding: FragmentBrowseBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_browse, container, false)
        binding = FragmentBrowseBinding.bind(view)
        registerForContextMenu(binding.webView)


        binding.webView.apply {
            when{
                URLUtil.isValidUrl(query) -> loadUrl(query)
                query.contains(".com", ignoreCase = true) -> loadUrl(query)
                else -> loadUrl("https://www.google.com/search?q=$query")
            }
        }

        return view
    }

    @SuppressLint ("SetJavaScriptEnabled")
    override fun onResume() {
        super.onResume()

        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()

            when{
                URLUtil.isValidUrl(query) -> loadUrl(query)
                query.contains(".com", ignoreCase = true) -> {
                    if(!query.startsWith("http://") && !query.startsWith("https://"))
                        query = "http://" + query;
                    loadUrl(query)
                }
                else -> loadUrl("https://www.google.com/search?q=$query")
            }

        }
    }


}