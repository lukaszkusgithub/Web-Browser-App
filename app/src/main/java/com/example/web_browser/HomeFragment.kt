package com.example.web_browser

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import com.example.web_browser.MainActivity.Companion.tabsList
import com.example.web_browser.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.bind(view)
        return view
    }

    override fun onResume() {
        super.onResume()

        val mainActivityRef = requireActivity() as MainActivity

        binding.searchViewHome.setOnQueryTextListener(object: android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(mainActivityRef.checkForInternetConnection(requireContext()))
                    mainActivityRef.changeTab(query!!, BrowseFragment(query))
                else
                    Snackbar.make(binding.root, "Internet Not Connected", 3000).show()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false

        })
    }



}