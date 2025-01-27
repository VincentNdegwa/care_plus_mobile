package com.example.careplus.ui.view

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.R as MaterialR
import com.example.careplus.R
import android.view.View
import android.view.WindowInsets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FuturisticBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = MaterialR.attr.bottomNavigationStyle
) : BottomNavigationView(context, attrs, defStyleAttr) {

    init {
        // Custom initialization
        itemIconTintList = context.getColorStateList(R.color.bottom_nav_icon_color)
        itemTextColor = context.getColorStateList(R.color.bottom_nav_text_color)
        itemIconSize = resources.getDimensionPixelSize(R.dimen.bottom_nav_icon_size)
        labelVisibilityMode = LABEL_VISIBILITY_LABELED
        setPadding(0, 
            resources.getDimensionPixelSize(R.dimen.bottom_nav_padding_vertical),
            0, 
            resources.getDimensionPixelSize(R.dimen.bottom_nav_padding_vertical)
        )

        // Handle window insets
//        ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
//            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
//            view.updatePadding(bottom = insets.bottom)
//            windowInsets
//        }
    }
} 