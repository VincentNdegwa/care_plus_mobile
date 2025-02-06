package com.example.careplus.utils

import android.view.View
import android.view.ViewGroup
import android.view.Gravity
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.example.careplus.R
import com.google.android.material.snackbar.Snackbar

object SnackbarUtils {
    fun showTopSnackbar(
        view: View,
        message: String,
        isError: Boolean = false,
        duration: Int = Snackbar.LENGTH_LONG
    ) {
        val snackbar = Snackbar.make(view, message, duration)
        
        // Get the Snackbar's layout params
        val params = snackbar.view.layoutParams as FrameLayout.LayoutParams
        
        // Set the layout params to show at top
        params.gravity = Gravity.TOP
        params.topMargin = 64 // Add some margin from top
        snackbar.view.layoutParams = params
        
        snackbar.setTextColor(ContextCompat.getColor(view.context, R.color.white))

        // Set background color based on error state
        if (isError) {
            snackbar.setBackgroundTint(view.context.getColor(R.color.error))
        } else {
            snackbar.setBackgroundTint(view.context.getColor(R.color.success))
        }

        snackbar.show()
    }

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