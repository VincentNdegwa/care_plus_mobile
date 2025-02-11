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
import android.net.Uri
import android.provider.Settings
import android.content.Context
import android.os.PowerManager
import androidx.navigation.findNavController
import com.example.careplus.utils.FCMManager
import com.example.careplus.ui.notification.NotificationViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private var currentNavController: NavController? = null
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var notificationViewModel: NotificationViewModel

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

        notificationViewModel = NotificationViewModel(application)
        
        if (sessionManager.isLoggedIn()) {
            val navGraph = currentNavController!!.navInflater.inflate(R.navigation.nav_graph)
            navGraph.setStartDestination(R.id.homeFragment)
            currentNavController!!.graph = navGraph

            registerFCMToken()
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
//        requestBatteryOptimizationExemption()

        handleNotificationIntent(intent)

        // Test FCM
        testFCM()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.action == "MEDICATION_NOTIFICATION") {
            intent.getStringExtra("notification_data")?.let { notificationData ->
                val navController = findNavController(R.id.nav_host_fragment)
                val bundle = Bundle().apply {
                    putString("notification_data", notificationData)
                }
                navController.navigate(R.id.medicationReminderFragment, bundle)
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

    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController!!.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun testFCM() {
        FCMManager.getCurrentToken { token ->
            Log.d(TAG, "FCM Token for testing: $token")
            // You can copy this token and use it to send test notifications
        }
    }

    private fun registerFCMToken() {
        FCMManager.getCurrentToken { token ->
            token?.let {
                notificationViewModel.registerToken(it)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
} 