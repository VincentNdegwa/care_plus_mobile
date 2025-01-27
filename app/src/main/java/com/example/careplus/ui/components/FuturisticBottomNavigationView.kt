package com.example.careplus.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import com.example.careplus.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class FuturisticBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BottomNavigationView(context, attrs, defStyleAttr) {

    init {
        background = context.getDrawable(R.drawable.bottom_nav_background)
        elevation = 24f
        
        setOnItemSelectedListener { item ->
            animateItem(item)
            true
        }
    }

    private fun animateItem(item: MenuItem) {
        val view = findViewById<View>(item.itemId)
        val bounceAnimation = AnimationUtils.loadAnimation(context, R.anim.bounce_anim)
        view?.startAnimation(bounceAnimation)
    }
} 