package com.example.careplus.ui.settings

import android.app.AlertDialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.R
import com.example.careplus.data.model.settings.*
import com.example.careplus.databinding.FragmentSettingsBinding
import com.example.careplus.databinding.BottomSheetSelectionBinding
import com.example.careplus.utils.SnackbarUtils
import com.example.careplus.utils.BottomSheetUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.*
import java.util.TimeZone

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SettingsViewModel
    private lateinit var emergencyContactAdapter: EmergencyContactAdapter
    private val emergencyContacts = mutableListOf<EmergencyContact>()
    private lateinit var currentSettings: Settings

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setPageTitle("Settings")
        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        fetchData()
        observeViewModel()

        // Initialize the RecyclerView and Adapter
        emergencyContactAdapter = EmergencyContactAdapter(emergencyContacts, this)
        binding.emergencyContactsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.emergencyContactsRecyclerView.adapter = emergencyContactAdapter

        binding.addEmergencyContactButton.setOnClickListener {
            showAddEmergencyContactDialog()
        }

        binding.saveSettingsButton.setOnClickListener {
            saveSettings()
        }

        binding.clickViewProfile.setOnClickListener {
            findNavController().navigate(R.id.action_from_settings_to_profile)
        }

        binding.clickChangePassword.setOnClickListener {
            // Navigate to change password screen or show a dialog
            findNavController().navigate(R.id.action_settings_to_changePassword)
        }

        binding.clickLanguage.setOnClickListener {
            showLanguageSelectionBottomSheet()
        }

        binding.clickTimeZone.setOnClickListener {
            showTimezoneSelectionBottomSheet()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.show()
            } else {
                binding.loadingOverlay.hide()
            }
        }

        viewModel.settings.observe(viewLifecycleOwner, Observer { result ->
            result.onSuccess { settingsResponse ->
                currentSettings= settingsResponse
                displaySettings(settingsResponse)
            }.onFailure { error ->
                SnackbarUtils.showSnackbar(binding.root, "Error updating settings: ${error.message}")
            }
        })

        viewModel.updateSetting.observe(viewLifecycleOwner, { result ->
            result.onSuccess { settingsResponse ->
                if (settingsResponse.error) {
                    currentSettings = settingsResponse.data
                    SnackbarUtils.showSnackbar(binding.root, settingsResponse.message)
                } else {
                    SnackbarUtils.showSnackbar(binding.root, settingsResponse.message, false)
                }
            }.onFailure { error ->
                SnackbarUtils.showSnackbar(binding.root, "Error updating settings: ${error.message}")
            }
        })
    }

    private fun fetchData() {
        viewModel.getSettings()
    }

    private fun displaySettings(settings: Settings) {
        // Update UI with settings data
        binding.languageTextView.setText(settings.user_management.language_preferences)
        binding.timezoneTextView.setText(settings.user_management.timezone)

        // Set notification preferences
        binding.emailNotificationSwitch.isChecked = settings.user_management.notification_preferences.email
        binding.smsNotificationSwitch.isChecked = settings.user_management.notification_preferences.sms
        binding.pushNotificationSwitch.isChecked = settings.user_management.notification_preferences.push_notifications

        // Display emergency contacts (if any)
        emergencyContacts.clear() // Clear existing contacts
        emergencyContacts.addAll(settings.emergency_alerts.emergency_contacts)
        emergencyContactAdapter.notifyDataSetChanged()

        binding.emergencyContactsRecyclerView.visibility = if (settings.emergency_alerts.emergency_contacts.isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun showAddEmergencyContactDialog(contact: EmergencyContact? = null, position: Int? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_emergency_contact, null)

        // Set the background color of the dialog to use the theme's window background
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)
        val backgroundColor = typedValue.data
        dialogView.setBackgroundColor(backgroundColor)

        val nameEditText = dialogView.findViewById<EditText>(R.id.editTextName)
        val phoneEditText = dialogView.findViewById<EditText>(R.id.editTextPhone)
        val emailEditText = dialogView.findViewById<EditText>(R.id.editTextEmail)
        val addressEditText = dialogView.findViewById<EditText>(R.id.editTextAddress)

        // If contact is not null, populate the fields for editing
        contact?.let {
            nameEditText.setText(it.name)
            phoneEditText.setText(it.phone)
            emailEditText.setText(it.email)
            addressEditText.setText(it.address)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (contact == null) "Add Emergency Contact" else "Edit Emergency Contact")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val name = nameEditText.text.toString()
                val phone = phoneEditText.text.toString()
                val email = emailEditText.text.toString()
                val address = addressEditText.text.toString()

                if (name.isNotEmpty() && phone.isNotEmpty() && email.isNotEmpty()) {
                    if (contact == null) {
                        val newContact = EmergencyContact(name, email, phone, address)
                        emergencyContacts.add(newContact)
                        emergencyContactAdapter.notifyItemInserted(emergencyContacts.size - 1)
                    } else {
                        val updatedContact = EmergencyContact(name, email, phone, address)
                        emergencyContacts[position!!] = updatedContact
                        emergencyContactAdapter.notifyItemChanged(position!!)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(backgroundColor))
        dialog.show()
    }

    private fun saveSettings() {
        val language = binding.languageTextView.text.toString()
        val timezone = binding.timezoneTextView.text.toString()
        val emailNotifications = binding.emailNotificationSwitch.isChecked
        val smsNotifications = binding.smsNotificationSwitch.isChecked
        val pushNotifications = binding.pushNotificationSwitch.isChecked

        val updatedSettings = Settings(
            user_management = UserManagement(
                notification_preferences = NotificationPreferences(
                    email = emailNotifications,
                    sms = smsNotifications,
                    push_notifications = pushNotifications
                ),
                language_preferences = language,
                timezone = timezone
            ),
            emergency_alerts = EmergencyAlerts(
                emergency_contacts = emergencyContacts,
                alert_preferences = AlertPreferences(
                    sms = smsNotifications,
                    email = emailNotifications
                )
            )
        )

        viewModel.updateSettings(updatedSettings)
    }

    private fun showLanguageSelectionBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
//        val bottomSheetBinding = BottomSheetSelectionBinding.inflate(layoutInflater)
//        dialog.setContentView(bottomSheetBinding.root)
//
//        val languages = listOf("English", "Spanish", "French", "German", "Chinese", "Japanese", "Arabic")
//
//        bottomSheetBinding.titleTextView.text = "Select Language"
//        val adapter = SelectionAdapter(languages) { selectedLanguage ->
//            binding.languageTextView.text = selectedLanguage
//            dialog.dismiss()
//        }
//
//        bottomSheetBinding.selectionRecyclerView.adapter = adapter
//        BottomSheetUtils.setupBottomSheetDialog(this, dialog)
//        dialog.show()
    }

    private fun showTimezoneSelectionBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val bottomSheetBinding = BottomSheetSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(bottomSheetBinding.root)
        
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                val displayMetrics = resources.displayMetrics
                behavior.peekHeight = this.resources.displayMetrics.heightPixels / 2
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                it.setBackgroundResource(R.drawable.bottom_sheet_background)
            }
        }
        
        bottomSheetBinding.titleTextView.text = "Select Timezone"
        
        val timezoneList = mutableListOf<TimezoneResponse>()
        val adapter = SelectionAdapter(mutableListOf()) { selectedTimezone ->
            val selectedItem = timezoneList.find { it.name == selectedTimezone }
            selectedItem?.let {
                binding.timezoneTextView.text = it.name
                dialog.dismiss()
            }
        }
        
        bottomSheetBinding.selectionRecyclerView.adapter = adapter
        
        // Setup search functionality with debounce
        var searchJob: Job? = null
        bottomSheetBinding.searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300)
                    val query = s?.toString() ?: ""
                    if (query.length >= 2) {
                        viewModel.searchTimezones(query)
                    } else {
                        timezoneList.clear()
                        adapter.updateItems(emptyList())
                    }
                }
            }
        })
        
        viewModel.timezones.observe(viewLifecycleOwner) { result ->
            result.onSuccess { timezones ->
                timezoneList.clear()
                timezoneList.addAll(timezones)
                adapter.updateItems(timezones.map { it.name })
            }.onFailure { error ->
                SnackbarUtils.showSnackbar(binding.root, "Error loading timezones: ${error.message}")
            }
        }
        
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        BottomSheetUtils.setupBottomSheetDialog(this, dialog)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}