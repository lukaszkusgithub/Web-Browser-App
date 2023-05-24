package com.example.web_browser.adapter

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.example.web_browser.R
import com.example.web_browser.activity.MainActivity
import com.example.web_browser.activity.changeTab
import com.example.web_browser.activity.checkForInternetConnection
import com.example.web_browser.databinding.BookmarkViewBinding
import com.example.web_browser.databinding.LongBookmarkViewBinding
import com.example.web_browser.fragment.BrowseFragment
import com.google.android.material.snackbar.Snackbar

// This is a RecyclerView adapter for displaying a list of bookmarks
// It takes a MyHolder object as its ViewHolder
class BookmarkAdapter(private val context: Context, private val isActivity: Boolean = false) :
    RecyclerView.Adapter<BookmarkAdapter.MyHolder>() {
    // An array of colors used to set the background of the bookmark
    // image view in case the bookmark image is not available
    private val colors = context.resources.getIntArray(R.array.ColorArray)

    // MyHolder class represents the ViewHolder for the adapter
    // It takes two nullable parameters for different view bindings
    class MyHolder(
        binding: BookmarkViewBinding? = null,
        bindingLong: LongBookmarkViewBinding? = null
    ) : RecyclerView.ViewHolder((binding?.root ?: bindingLong?.root)!!) {
        // The image view for the bookmark image
        val image = (binding?.bookmarkIcon ?: bindingLong?.bookmarkIcon)!!
        // The text view for the bookmark name
        val name = (binding?.bookmarkName ?: bindingLong?.bookmarkName)!!
        // The root view for the bookmark item
        val root = (binding?.root ?: bindingLong?.root)!!
    }

    // This function creates and returns a new instance of MyHolder class,
    // which serves as the ViewHolder for the adapter.
    // It takes a ViewGroup parent and an integer viewType as parameters.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        // Check if the adapter is used in an Activity
        if (isActivity) {
            // If yes, inflate the LongBookmarkViewBinding layout and return a new instance
            // of MyHolder with the inflated view
            return MyHolder(
                bindingLong = LongBookmarkViewBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
            )
        }
        // If no, inflate the BookmarkViewBinding layout and return a
        // new instance of MyHolder with the inflated view
        return MyHolder(
            binding = BookmarkViewBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    // This function binds the data at the given position to the corresponding views
    // in the MyHolder object
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        try {
            // Try to decode the image from the bookmarkList at the given position and
            // set it as background of the holder image view
            val icon = BitmapFactory.decodeByteArray(
                MainActivity.bookmarkList[position].image, 0,
                MainActivity.bookmarkList[position].image!!.size
            )
            holder.image.background = icon.toDrawable(context.resources)
        } catch (e: Exception) {
            // If an exception occurs while decoding the image, set a random
            // background color and set the first letter of the bookmark name
            // as text of the holder image view
            holder.image.setBackgroundColor(colors[(colors.indices).random()])
            holder.image.text = MainActivity.bookmarkList[position].name[0].toString()
        }
        // Set the bookmark name as text of the holder name view
        holder.name.text = MainActivity.bookmarkList[position].name

        // Set a click listener on the holder root view to open the corresponding bookmark in
        // a new tab or show a snackbar if internet connection is not available
        holder.root.setOnClickListener {
            when {
                // Check if internet connection is available
                checkForInternetConnection(context) -> {
                    // If internet connection is available, open the corresponding
                    // bookmark in a new tab and close the current activity if it is an activity
                    changeTab(
                        MainActivity.bookmarkList[position].name,
                        BrowseFragment(query = MainActivity.bookmarkList[position].url)
                    )
                    if (isActivity) (context as Activity).finish()
                }
                // If internet connection is not available, show a
                // snackbar with an appropriate message
                else -> Snackbar.make(holder.root, R.string.internet_error, 3000)
                    .show()
            }
        }
    }

    // This function returns the number of items in the bookmarkList of MainActivity,
    // which is the size of the list.
    override fun getItemCount(): Int {
        return MainActivity.bookmarkList.size
    }
}