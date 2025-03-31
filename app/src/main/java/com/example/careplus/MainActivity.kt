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
import androidx.navigation.findNavController
import com.example.careplus.utils.FCMManager
import com.example.careplus.ui.notification.NotificationViewModel
import androidx.activity.viewModels
import androidx.navigation.NavDestination
import androidx.work.WorkManager
import com.example.careplus.services.AlarmService
import com.google.android.material.imageview.ShapeableImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.careplus.ui.profile.ProfileViewModel
import com.google.android.material.chip.Chip
import com.example.careplus.ui.report.ReportFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val notificationViewModel: NotificationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set navigation bar color to transparent
        window.navigationBarColor = Color.TRANSPARENT

        sessionManager = SessionManager(this)

        // Initialize NavController
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
                R.id.changePasswordFragment,
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
//                R.id.menu_notifications -> {
//                    // Handle notifications
//                }
                R.id.menu_reports -> {
                    // Navigate to ReportFragment
                    findNavController(R.id.nav_host_fragment).navigate(R.id.reportFragment)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.menu_settings -> {
                    // Navigate to SettingsFragment
                    findNavController(R.id.nav_host_fragment).navigate(R.id.settingsFragment)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.menu_profile -> {
                    navController.navigate(R.id.profileFragment)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.menu_medications -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.medicationsFragment)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.menu_side_effects -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.sideEffectsFragment)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.menu_diagnoses -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.diagnosesFragment)
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.menu_caregivers -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.caregiversFragment)
                    binding.drawerLayout.closeDrawers()
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

        if (sessionManager.isLoggedIn()) {
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
            navGraph.setStartDestination(R.id.homeFragment)
            navController.graph = navGraph

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
        subscribeNotifications()
        setupNavigationDrawer()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.action == "MEDICATION_NOTIFICATION") {
            intent.getStringExtra("notification_data")?.let { notificationData ->
                // Wait for navigation to be ready
                navController.addOnDestinationChangedListener(object : NavController.OnDestinationChangedListener {
                    override fun onDestinationChanged(
                        controller: NavController,
                        destination: NavDestination,
                        arguments: Bundle?
                    ) {
                        // Remove listener to avoid multiple calls
                        navController.removeOnDestinationChangedListener(this)
                        
                        // Navigate to reminder fragment
                        val bundle = Bundle().apply {
                            putString("notification_data", notificationData)
                        }
                        navController.navigate(R.id.medicationReminderFragment, bundle)
                    }
                })
            }
            if (intent.getBooleanExtra("stop_alarm", false)) {
                WorkManager.getInstance(applicationContext)
                    .cancelAllWorkByTag("alarm_work")
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
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun testFCM() {
        FCMManager.getCurrentToken { token ->
            Log.d(TAG, "FCM Token for testing: $token")
        }
    }

    private fun registerFCMToken() {
        FCMManager.getCurrentToken { token ->
            token?.let {
                notificationViewModel.registerToken(it)
            }
        }
    }
    private  fun subscribeNotifications(){
        FCMManager.subscribeToTopic("new_diagnosis_notification")
    }

    override fun onResume() {
        super.onResume()
        if (intent?.action == "MEDICATION_NOTIFICATION") {
            // Stop the alarm service when the app is opened from notification
            stopService(Intent(this, AlarmService::class.java))
        }
    }

    private fun setupNavigationDrawer() {
        val navigationView = binding.navigationView
        val headerView = navigationView.getHeaderView(0)
        
        val avatarImage = headerView.findViewById<ShapeableImageView>(R.id.avatarImage)
        val nameText = headerView.findViewById<TextView>(R.id.nameText)
        val emailText = headerView.findViewById<TextView>(R.id.emailText)
        val roleChip = headerView.findViewById<Chip>(R.id.roleChip)

        sessionManager.getUser()?.let { user ->
            nameText.text = user.name
            emailText.text = user.email
            roleChip.text = user.role?.capitalize()

            val imageUrl =  ProfileViewModel(this.application).getDisplayImageUrl(user.avatar)
            imageUrl.let { avatarUrl->
                Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .circleCrop()
                    .into(avatarImage)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
} 