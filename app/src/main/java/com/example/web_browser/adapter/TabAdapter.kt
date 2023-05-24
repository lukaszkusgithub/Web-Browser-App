package com.example.web_browser.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.web_browser.activity.MainActivity
import com.example.web_browser.databinding.TabBinding

// This is a RecyclerView adapter for displaying a list of tabs
// It takes a MyHolder object as its ViewHolder
class TabAdapter(private val context: Context, private val dialog: androidx.appcompat.app.AlertDialog) :
    RecyclerView.Adapter<TabAdapter.MyHolder>() {

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
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.root.setOnClickListener {
            MainActivity.pager.currentItem = position
            dialog.dismiss()
        }
    }

    // This function returns the number of items in the tabs of MainActivity,
    // which is the size of the list.
    override fun getItemCount(): Int {
        return MainActivity.tabsList.size
    }
}