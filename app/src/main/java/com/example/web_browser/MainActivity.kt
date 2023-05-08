package com.example.web_browser

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Browser
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.web_browser.MainActivity.Companion.tabsList
import com.example.web_browser.databinding.ActivityMainBinding
import com.google.android.material.textview.MaterialTextView

class MainActivity : AppCompatActivity() {

    // Declaration of the binding variable as late-initialized
    lateinit var binding: ActivityMainBinding

    // Declaration of the tabsList variable as an object of the ArrayList class
    // Declaration of the isFullscreen variable as Boolean type
    // Declaration of the isDesktopSite variable as Boolean type
    // Declaration of the bookmarkIndex variable as Int type
    // Declaration of the myPager variable as an object of the ViewPager2 class
    // Declaration of the tabsBtn variable as an object of the MaterialTextView class
    companion object {
        var tabsList: ArrayList<Fragment> = ArrayList()
        private var isFullscreen: Boolean = true
        var isDesktopSite: Boolean = false

        //        var bookmarkList: ArrayList<Bookmark> = ArrayList()
        var bookmarkIndex: Int = -1
        lateinit var myPager: ViewPager2
        lateinit var tabsBtn: MaterialTextView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the view from the XML file using the ActivityMainBinding class
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Set the activity layout to the view created by binding
        setContentView(binding.root)
        // Add the first fragment to the tabsList
        tabsList.add(HomeFragment())
        // Set the adapter for ViewPager2 and disable user interaction
        binding.pager.adapter = TabsAdapter(supportFragmentManager, lifecycle)
        binding.pager.isUserInputEnabled = false
    }


    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    @SuppressLint("NotifyDataSetChanged")
    override fun onBackPressed(): Unit {
        // Get the current fragment in the ViewPager
        var fragmet: BrowseFragment? = null
        try {
            fragmet = tabsList[binding.pager.currentItem] as BrowseFragment
        } catch (e: java.lang.Exception) {

        }
        // If the current fragment can go back in its WebView, go back
        when {
            fragmet?.binding?.webView?.canGoBack() == true -> fragmet.binding.webView.goBack()
            // If there are more than one fragment in the ViewPager, remove the current one and
            // go back to the previous one
            binding.pager.currentItem != 0 -> {
                tabsList.removeAt(binding.pager.currentItem)
                binding.pager.adapter?.notifyDataSetChanged()
                binding.pager.currentItem = tabsList.size - 1
            }
            // If there is only one fragment in the ViewPager, call the default implementation
            // of onBackPressed()
            else -> super.onBackPressed()
        }
    }

    // Adapter for the ViewPager that holds the list of tabs (Fragments)
    private inner class TabsAdapter(fa: FragmentManager, lc: Lifecycle) :
        FragmentStateAdapter(fa, lc) {
        override fun getItemCount(): Int = tabsList.size

        override fun createFragment(position: Int): Fragment = tabsList[position]
    }

    // Adds a new tab (Fragment) to the ViewPager
    fun changeTab(query: String, fragment: Fragment) {
        tabsList.add(fragment)
        binding.pager.adapter?.notifyDataSetChanged()
        binding.pager.currentItem = tabsList.size - 1
    }

    // Checks if there is an internet connection available
    fun checkForInternetConnection(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION") return networkInfo.isConnected
        }
    }
}