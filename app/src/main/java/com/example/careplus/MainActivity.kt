package com.example.careplus

import android.Manifest
import android.content.pm.PackageManager
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
import android.content.Intent
import android.util.Log
import com.example.careplus.services.PusherService

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private var currentNavController: NavController? = null
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
        currentNavController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.medicationsFragment,
                R.id.caregiversFragment
            )
        )

        // Setup bottom navigation with custom listener
        binding.bottomNav.setupWithNavController(currentNavController!!)
        binding.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeFragment -> {
                    currentNavController!!.navigate(R.id.homeFragment)
                    true
                }
                R.id.medicationsFragment -> {
                    currentNavController!!.navigate(R.id.medicationsFragment)
                    true
                }
                R.id.caregiversFragment -> {
                    currentNavController!!.navigate(R.id.caregiversFragment)
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
        currentNavController!!.addOnDestinationChangedListener { _, destination, _ ->
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
                    currentNavController!!.navigate(R.id.medicationsFragment)
                    binding.drawerLayout.closeDrawer(GravityCompat.END)
                    true
                }
                R.id.menu_logout -> {
                    sessionManager.clearSession()
                    currentNavController!!.navigate(R.id.loginFragment)
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Check if user is logged in and set start destination
        if (sessionManager.isLoggedIn()) {
            val navGraph = currentNavController!!.navInflater.inflate(R.navigation.nav_graph)
            navGraph.setStartDestination(R.id.homeFragment)
            currentNavController!!.graph = navGraph

            // Start foreground service when activity is created
            sessionManager.getUser()?.patient?.id?.toString()?.let { patientId ->
                startPusherForegroundService(patientId)
            }
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

        requestNotificationPermission()

        handleNotificationIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.action == "MEDICATION_NOTIFICATION") {
            Log.d("MainActivity", "Handling notification intent")
            intent.getStringExtra("notification_data")?.let { notificationData ->
                Log.d("MainActivity", "Notification data: $notificationData")
                
                // Ensure NavController is initialized
                val navHostFragment = supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val navController = navHostFragment.navController

                // Create bundle with notification data
                val bundle = Bundle().apply {
                    putString("notification_data", notificationData)
                }

                // Navigate to home fragment with data
                navController.navigate(R.id.homeFragment, bundle)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController!!.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun startPusherForegroundService(patientId: String) {
        try {
            Intent(this, PusherService::class.java).also { intent ->
                intent.putExtra(PusherService.EXTRA_PATIENT_ID, patientId)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting Pusher service", e)
        }
    }

    override fun onResume() {
        super.onResume()
        // Restart service if needed when activity comes to foreground
        if (sessionManager.isLoggedIn()) {
            sessionManager.getUser()?.patient?.id?.toString()?.let { patientId ->
                startPusherForegroundService(patientId)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
} 