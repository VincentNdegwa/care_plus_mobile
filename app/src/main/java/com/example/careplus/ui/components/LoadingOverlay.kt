package com.example.careplus.ui.components

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.example.careplus.R
import com.example.careplus.databinding.LayoutLoadingOverlayBinding

class LoadingOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var binding: LayoutLoadingOverlayBinding

    init {
        binding = LayoutLoadingOverlayBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun show(message: String? = null) {
        Log.d("LoadingOverlay", "Showing overlay....")
        message?.let {
            binding.loadingText.text = it
        }
        binding.loadingOverlay.visibility = View.VISIBLE
    }

    fun hide() {
        binding.loadingOverlay.visibility = View.GONE
    }

    fun setLoadingText(message: String) {
        binding.loadingText.text = message
    }
} 