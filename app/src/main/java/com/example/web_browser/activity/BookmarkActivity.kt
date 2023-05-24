package com.example.web_browser.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.web_browser.R
import com.example.web_browser.adapter.BookmarkAdapter
import com.example.web_browser.databinding.ActivityBookmarkBinding
import com.example.web_browser.databinding.ActivityMainBinding

class BookmarkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityBookmarkBinding.inflate(layoutInflater)

        setContentView(binding.root)
        // TODO
        binding.recyclerViewAllBookmarks.setItemViewCacheSize(5)
        binding.recyclerViewAllBookmarks.hasFixedSize()
        binding.recyclerViewAllBookmarks.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewAllBookmarks.adapter = BookmarkAdapter(this, isActivity = true)
    }
}