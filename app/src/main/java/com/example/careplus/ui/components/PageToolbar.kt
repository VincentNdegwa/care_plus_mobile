package com.example.careplus.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.appbar.MaterialToolbar
import com.example.careplus.databinding.LayoutPageToolbarBinding
import androidx.navigation.findNavController
import com.example.careplus.MainActivity
import com.example.careplus.R
import androidx.navigation.NavController
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

class PageToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.toolbarStyle
) : MaterialToolbar(context, attrs, defStyleAttr) {

    private val binding = LayoutPageToolbarBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        setupViews()
    }

    private fun setupViews() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    fun setPageTitle(title: String) {
        binding.toolbarTitle.text = title
    }
} 