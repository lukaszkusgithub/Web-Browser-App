package com.example.web_browser.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.web_browser.R
import com.example.web_browser.databinding.ActivityBookmarkBinding
import com.example.web_browser.databinding.ActivityMainBinding

class BookmarkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityBookmarkBinding.inflate(layoutInflater)

        setContentView(binding.root)

    }
}