package com.example.careplus.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.example.careplus.databinding.LayoutHeaderBinding
import java.util.Calendar

class HeaderLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        setupViews()
    }

    private fun setupViews() {
        // Setup default greeting
        updateGreeting()
    }

    fun updateGreeting(userName: String? = null) {
        val calendar = Calendar.getInstance()
        val greetingText = when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
        binding.greetingText.text = greetingText
        userName?.let {
            binding.userNameText.text = it
        }
    }

    fun setUserName(name: String) {
        binding.userNameText.text = name
    }
} 