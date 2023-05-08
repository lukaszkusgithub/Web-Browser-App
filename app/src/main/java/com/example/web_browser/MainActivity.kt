package com.example.web_browser

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintJob
import android.print.PrintManager
import android.provider.Browser
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.web_browser.MainActivity.Companion.tabsList
import com.example.web_browser.databinding.ActivityMainBinding
import com.example.web_browser.databinding.MoreToolsBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    // Declaration of the binding variable as late-initialized
    lateinit var binding: ActivityMainBinding

    // This is a private variable printJob declared in the class. It is initially set to null and
    // used to hold a reference to a print job started by the
    // saveWebAsPDF() function. It can be accessed and modified only within the class.
    private var printJob: PrintJob? = null

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

        //This code sets the layoutInDisplayCutoutMode attribute of the current activity's
        // window to LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES if the device is running
        // on Android P or later. This allows the activity to extend to the device's
        // display cutout area, if present, without any visual interruption.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        // Inflate the view from the XML file using the ActivityMainBinding class
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Set the activity layout to the view created by binding
        setContentView(binding.root)
        // Add the first fragment to the tabsList
        tabsList.add(HomeFragment())
        // Set the adapter for ViewPager2 and disable user interaction
        binding.pager.adapter = TabsAdapter(supportFragmentManager, lifecycle)
        binding.pager.isUserInputEnabled = false

        initializeView()

        changeFullscreen(enable = true)
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

    // Initializes the view and sets the onClickListener for the settingButton.
    private fun initializeView() {
        // When clicked, a dialog box will appear with additional tools.
        binding.settingButton.setOnClickListener {
            // Initialize variable for current fragment
            var fragmet: BrowseFragment? = null
            // Try to get current fragment from tabsList
            try {
                fragmet = tabsList[binding.pager.currentItem] as BrowseFragment
            } catch (e: java.lang.Exception) {
                // Do nothing if an exception is thrown
            }
            // Inflate the layout for the more tools dialog
            val view = layoutInflater.inflate(R.layout.more_tools, binding.root, false)
            val dialogBinding = MoreToolsBinding.bind(view)

            // Create and display the more tools dialog
            val dialog = MaterialAlertDialogBuilder(this).setView(view).create()

            dialog.window?.apply {
                attributes.gravity = Gravity.BOTTOM
                attributes.y = 50
            }

            dialog.show()

            // If the variable 'isFullscreen' is true, change the text color
            // of the fullscreenButton to the accent color in dark theme
            if (isFullscreen) {
                dialogBinding.fullscreenButton.apply {
                    setTextColor(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.accent_dark_night
                        )
                    )
                }
            }

            // Set click listeners for the buttons in the more tools dialog
            dialogBinding.backButton.setOnClickListener {
                onBackPressed()
            }

            dialogBinding.forwardButton.setOnClickListener {
                // Check if the current fragment's WebView can go forward
                fragmet?.apply {
                    if (binding.webView.canGoForward()) {
                        binding.webView.goForward()
                    }
                }
            }

            dialogBinding.saveButton.setOnClickListener {
                dialog.dismiss()
                // Check if a fragment is currently active
                if (fragmet != null) {
                    // Save the current fragment's WebView as a PDF
                    saveWebAsPDF(web = fragmet.binding.webView)
                } else {
                    // Display a Snackbar if no website is available to save
                    Snackbar.make(binding.root, "No website to save", 3000)
                }
            }

            // Define a click listener for the fullscreen button in the dialog
            dialogBinding.fullscreenButton.setOnClickListener {
                // Cast the view to a MaterialButton
                it as MaterialButton

                // If the activity is currently in fullscreen mode
                isFullscreen = if (isFullscreen) {
                    // Disable fullscreen mode and update button text color to white
                    changeFullscreen(enable = false)
                    it.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
                    false
                } else {
                    // Enable fullscreen mode and update button text color to accent_dark_night
                    changeFullscreen(enable = true)
                    it.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.accent_dark_night
                        )
                    )
                    true
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        printJob?.let {
            when {
                // Retrieve and display the status of a print job (if it exists)
                it.isCompleted -> Snackbar.make(
                    binding.root,
                    "Successfull -> ${it.info.label}",
                    3000
                ).show()
                it.isFailed -> Snackbar.make(
                    binding.root,
                    "Failed -> ${it.info.label}",
                    3000
                ).show()
            }
        }
    }

    // Print Page as PDF
    private fun saveWebAsPDF(web: WebView) {
        // Get the system print manager
        val pm = getSystemService(Context.PRINT_SERVICE) as PrintManager

        // Define a unique job name based on the current URL and timestamp
        val jobName =
            "${URL(web.url).host}_${
                SimpleDateFormat("HH:mm d_MMM_yy", Locale.ENGLISH).format(
                    Calendar.getInstance().time
                )
            }"

        // Create a print document adapter from the webview
        val printAdapter = web.createPrintDocumentAdapter(jobName)
        // Define the print attributes, including the job name
        val printAttributes = PrintAttributes.Builder()

        // Print the job and store it in a variable for later reference
        printJob = pm.print(jobName, printAdapter, printAttributes.build())
    }

    // Open Fullscreen mode
    private fun changeFullscreen(enable: Boolean) {
        if (enable) {
            // Disable fitting system windows within the decor view to allow for fullscreen mode
            WindowCompat.setDecorFitsSystemWindows(window, false)
            // Use the WindowInsetsControllerCompat to hide system bars and set their behavior
            WindowInsetsControllerCompat(window, binding.root).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Enable fitting system windows within the decor view to exit fullscreen mode
            WindowCompat.setDecorFitsSystemWindows(window, true)
            // Use the WindowInsetsControllerCompat to show system bars
            WindowInsetsControllerCompat(
                window,
                binding.root
            ).show(WindowInsetsCompat.Type.systemBars())
        }
    }
}