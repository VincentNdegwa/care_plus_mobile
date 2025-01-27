package com.example.careplus

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.careplus.databinding.ActivityMainBinding
import com.example.careplus.data.SessionManager
import android.view.Gravity
import androidx.core.view.GravityCompat
import android.graphics.Color
import android.os.Build

class MainActivity : AppCompatActivity() {
    internal lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle system insets
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }

        // Set navigation bar color to transparent
        window.navigationBarColor = Color.TRANSPARENT

        sessionManager = SessionManager(this)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Add destination changed listener to handle both navigation and bottom nav
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment, R.id.forgotPasswordFragment -> {
                    binding.bottomNav.visibility = View.GONE
                }
                R.id.homeFragment -> {
                    binding.bottomNav.visibility = View.VISIBLE
                    binding.bottomNav.selectedItemId = R.id.navigation_home
                }
                R.id.medicationsFragment -> {
                    binding.bottomNav.visibility = View.VISIBLE
                    binding.bottomNav.selectedItemId = R.id.navigation_medications
                }
            }
        }

        // Setup bottom navigation clicks
        binding.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    if (navController.currentDestination?.id != R.id.homeFragment) {
                        navController.navigate(R.id.homeFragment)
                    }
                    true
                }
                R.id.navigation_medications -> {
                    if (navController.currentDestination?.id != R.id.medicationsFragment) {
                        navController.navigate(R.id.medicationsFragment)
                    }
                    true
                }
                R.id.navigation_profile -> {
                    // TODO: Navigate to profile
                    true
                }
                R.id.navigation_more -> {
                    binding.drawerLayout.openDrawer(GravityCompat.END)
                    true
                }
                else -> false
            }
        }

        // Setup Navigation Drawer
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_notifications -> {
                    // Handle notifications
                }
                R.id.menu_reports -> {
                    // Handle reports
                }
                R.id.menu_settings -> {
                    // Handle settings
                }
                R.id.menu_profile -> {
                    // Handle profile
                }
                R.id.menu_medications -> {
                    navController.navigate(R.id.medicationsFragment)
                    binding.drawerLayout.closeDrawer(GravityCompat.END)
                    true
                }
                R.id.menu_logout -> {
                    sessionManager.clearSession()
                    navController.navigate(R.id.loginFragment)
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            true
        }

        // Check if user is logged in and set start destination
        if (sessionManager.isLoggedIn()) {
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
            navGraph.setStartDestination(R.id.homeFragment)
            navController.graph = navGraph
        }
    }
} 