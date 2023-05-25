package com.example.web_browser.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.web_browser.R
import com.example.web_browser.activity.MainActivity
import com.example.web_browser.databinding.TabBinding
import com.google.android.material.snackbar.Snackbar

// This is a RecyclerView adapter for displaying a list of tabs
// It takes a MyHolder object as its ViewHolder
class TabAdapter(
    private val context: Context, private val dialog: androidx.appcompat.app.AlertDialog
) : RecyclerView.Adapter<TabAdapter.MyHolder>() {

    // MyHolder class represents the ViewHolder for the adapter
    // It takes two nullable parameters for different view bindings
    class MyHolder(
        binding: TabBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        // The image view for the tab image
        val cancelButton = binding.cancelButton

        // The text view for the tab name
        val name = binding.tabName

        // The root view for the tab item
        val root = binding.root
    }

    // This function creates and returns a new instance of MyHolder class,
    // which serves as the ViewHolder for the adapter.
    // It takes a ViewGroup parent and an integer viewType as parameters.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        // If yes, inflate the TabBinding layout and return a new instance
        // of MyHolder with the inflated view
        return MyHolder(
            TabBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        // Set the name of the tab at the given position in the tabsList to the name TextView in the ViewHolder
        holder.name.text = MainActivity.tabsList[position].name
        // Set an OnClickListener for the root view of the ViewHolder
        holder.root.setOnClickListener {
            // Set the current item of the pager to the clicked position
            MainActivity.pager.currentItem = position
            // Dismiss the dialog
            dialog.dismiss()
        }
        // Set an OnClickListener for the cancel button in the ViewHolder
        holder.cancelButton.setOnClickListener {
            // Check if there is only one tab left or if the clicked position is the current item in the pager
            if (MainActivity.tabsList.size == 1 || position == MainActivity.pager.currentItem) {
                // Show a Snackbar with an error message indicating that the tab cannot be removed
                Snackbar.make(
                    MainActivity.pager,
                    R.string.error_tab_remove,
                    2000
                ).show()
            } else {
                // Remove the tab at the clicked position from the tabsList
                MainActivity.tabsList.removeAt(position)
                // Notify the adapter that the data set has changed
                notifyDataSetChanged()
                // Notify the pager's adapter that an item has been removed at the clicked position
                MainActivity.pager.adapter?.notifyItemRemoved(position)
            }
        }
    }

    // This function returns the number of items in the tabs of MainActivity,
    // which is the size of the list.
    override fun getItemCount(): Int {
        return MainActivity.tabsList.size
    }
}