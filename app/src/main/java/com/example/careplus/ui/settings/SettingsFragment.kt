package com.example.careplus.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.R
import com.example.careplus.data.model.settings.EmergencyContact
import com.example.careplus.databinding.FragmentSettingsBinding
import com.example.careplus.data.model.settings.Settings

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SettingsViewModel
    private lateinit var emergencyContactAdapter: EmergencyContactAdapter
    private val emergencyContacts = mutableListOf<EmergencyContact>()

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

        viewModel.getSettings()

        viewModel.settings.observe(viewLifecycleOwner, Observer { result ->
            result.onSuccess { settingsResponse ->
                displaySettings(settingsResponse)
            }.onFailure { error ->
                binding.languageEditText.setText("Error fetching settings: ${error.message}")
            }
        })

        // Initialize the RecyclerView and Adapter
        emergencyContactAdapter = EmergencyContactAdapter(emergencyContacts)
        binding.emergencyContactsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.emergencyContactsRecyclerView.adapter = emergencyContactAdapter

        binding.addEmergencyContactButton.setOnClickListener {
            showAddEmergencyContactDialog()
        }

        binding.saveSettingsButton.setOnClickListener {
            saveSettings()
        }
    }

    private fun displaySettings(settings: Settings) {
        // Update UI with settings data
        binding.languageEditText.setText(settings.user_management.language_preferences)
        binding.timezoneEditText.setText(settings.user_management.timezone)

        // Set notification preferences
        binding.emailNotificationSwitch.isChecked = settings.user_management.notification_preferences.email
        binding.smsNotificationSwitch.isChecked = settings.user_management.notification_preferences.sms
        binding.pushNotificationSwitch.isChecked = settings.user_management.notification_preferences.push_notifications

        // Display emergency contacts (if any)
        emergencyContacts.clear() // Clear existing contacts
        emergencyContacts.addAll(settings.emergency_alerts.emergency_contacts)
        emergencyContactAdapter.notifyDataSetChanged()

        if(settings.emergency_alerts.emergency_contacts.size>0){
            binding.emergencyContactsRecyclerView.visibility = View.VISIBLE
        }else{
            binding.emergencyContactsRecyclerView.visibility = View.GONE
        }
    }

    private fun showAddEmergencyContactDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_emergency_contact, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.editTextName)
        val phoneEditText = dialogView.findViewById<EditText>(R.id.editTextPhone)
        val emailEditText = dialogView.findViewById<EditText>(R.id.editTextEmail)
        val addressEditText = dialogView.findViewById<EditText>(R.id.editTextAddress)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Emergency Contact")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val name = nameEditText.text.toString()
                val phone = phoneEditText.text.toString()
                val email = emailEditText.text.toString()
                val address = addressEditText.text.toString()
                if (name.isNotEmpty() && phone.isNotEmpty() && email.isNotEmpty()) {
                    val newContact = EmergencyContact(name, phone, email, address)
                    emergencyContacts.add(newContact)
                    emergencyContactAdapter.notifyItemInserted(emergencyContacts.size - 1)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveSettings() {
        // Collect data from UI and save settings
        val language = binding.languageEditText.text.toString()
        val timezone = binding.timezoneEditText.text.toString()
        val emailNotifications = binding.emailNotificationSwitch.isChecked
        val smsNotifications = binding.smsNotificationSwitch.isChecked
        val pushNotifications = binding.pushNotificationSwitch.isChecked

        // Here you would typically call a method in your ViewModel to save these settings
        // For example:
        // viewModel.saveSettings(language, timezone, emailNotifications, smsNotifications, pushNotifications)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 