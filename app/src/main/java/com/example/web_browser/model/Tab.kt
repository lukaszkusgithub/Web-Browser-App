package com.example.web_browser.model

import androidx.fragment.app.Fragment
import com.example.web_browser.fragment.BrowseFragment

data class Tab(var name: String, val fragment: Fragment)
