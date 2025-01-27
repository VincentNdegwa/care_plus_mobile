package com.example.careplus.utils

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.careplus.R
import com.google.android.material.snackbar.Snackbar

object SnackbarUtils {
    fun showSnackbar(
        view: View,
        message: String,
        isError: Boolean = true,
        duration: Int = Snackbar.LENGTH_LONG
    ) {
        val snackbar = Snackbar.make(view, message, duration)
        
        // Set background and text colors
        snackbar.setBackgroundTint(
            ContextCompat.getColor(
                view.context,
                if (isError) R.color.error else R.color.success
            )
        )
        snackbar.setTextColor(
            ContextCompat.getColor(
                view.context,
                R.color.surface_light
            )
        )

        // Try to find the bottom navigation view
        try {
            val activity = view.context
            val bottomNav = (view.rootView as? ViewGroup)?.findViewById<View>(R.id.bottomNav)
            if (bottomNav != null) {
                snackbar.setAnchorView(bottomNav)
            } else {
                // If bottom nav not found, add margin to bottom
                val snackbarView = snackbar.view
                val params = snackbarView.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(
                    params.leftMargin,
                    params.topMargin,
                    params.rightMargin,
                    params.bottomMargin + 56.dpToPx(view.context) // Standard bottom nav height
                )
                snackbarView.layoutParams = params
            }
        } catch (e: Exception) {
            // If anything goes wrong with anchor view, just show the snackbar normally
        }
        
        snackbar.show()
    }

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
} 