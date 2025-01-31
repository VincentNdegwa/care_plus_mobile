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
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.ViewGroup

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set navigation bar color to transparent
        window.navigationBarColor = Color.TRANSPARENT

        sessionManager = SessionManager(this)

        // Get NavController using NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.medicationsFragment,
                R.id.caregiversFragment
            )
        )

        // Setup bottom navigation with custom listener
        binding.bottomNav.setupWithNavController(navController)
        binding.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }
                R.id.medicationsFragment -> {
                    navController.navigate(R.id.medicationsFragment)
                    true
                }
                R.id.caregiversFragment -> {
                    navController.navigate(R.id.caregiversFragment)
                    true
                }
                R.id.navigation_more -> {
                    binding.drawerLayout.openDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }

        // Hide bottom navigation on auth screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment,
                R.id.registerFragment,
                R.id.forgotPasswordFragment -> {
                    binding.bottomNav.visibility = View.GONE
                }
                else -> {
                    binding.bottomNav.visibility = View.VISIBLE
                }
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
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Check if user is logged in and set start destination
        if (sessionManager.isLoggedIn()) {
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
            navGraph.setStartDestination(R.id.homeFragment)
            navController.graph = navGraph
        }

        // Replace the keyboard visibility listener with this simpler version
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val imeVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime())
            val navigationBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            
            // Apply padding for status bar
            binding.root.setPadding(0, statusBars.top, 0, 0)
            
            // Handle bottom navigation visibility
            binding.bottomNav.apply {
                if (imeVisible) {
                    visibility = View.GONE
                } else {
                    visibility = View.VISIBLE
                    setPadding(0, 0, 0, navigationBars.bottom)
                }
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
} 