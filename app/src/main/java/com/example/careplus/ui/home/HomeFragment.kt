package com.example.careplus.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.careplus.R
import com.example.careplus.data.SessionManager
import com.example.careplus.databinding.FragmentHomeBinding
import com.example.careplus.databinding.ItemCalendarDateBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import com.example.careplus.utils.SnackbarUtils
import com.example.careplus.MainActivity
import com.example.careplus.data.api.ApiClient
import com.example.careplus.data.model.DashboardResponse
import com.example.careplus.data.model.HealthVital
import com.example.careplus.data.model.MedicationDetails
import com.example.careplus.data.model.PatientStats
import retrofit2.HttpException
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private lateinit var scheduleAdapter: MedicationScheduleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        
        // Display stored user name immediately
        sessionManager.getUser()?.let { user ->
            binding.headerLayout.setUserName(user.name)
        }
        binding.medicationLoadingProgressBar.visibility = View.VISIBLE


        setupRecyclerView()
        setupObservers()
        setupCalendar()
        
        // Only fetch profile if we don't have stored user data
        if (sessionManager.getUser() == null) {
            viewModel.fetchProfile()
        }
        viewModel.fetchMedicationSchedules()
        viewModel.fetchStats()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // No need to set bottom nav selection
    }

    private fun setupRecyclerView() {
        scheduleAdapter = MedicationScheduleAdapter { medicationId:Int ->
            fetchMedicationDetails(medicationId)
        }
        binding.medicationsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
        }
        binding.medicationLoadingProgressBar.visibility = View.GONE
    }

    private fun setupObservers() {
        viewModel.profile.observe(viewLifecycleOwner) { result ->
            result.onSuccess { profile ->
                binding.headerLayout.setUserName(profile.name)
            }.onFailure { exception ->
                SnackbarUtils.showSnackbar(
                    binding.root,
                    exception.message ?: "Failed to load profile"
                )
            }
        }

        viewModel.schedules.observe(viewLifecycleOwner) { result ->
            result.onSuccess { schedules ->
                scheduleAdapter.submitList(schedules)
            }.onFailure { exception ->
                SnackbarUtils.showSnackbar(
                    binding.root,
                    exception.message ?: "Failed to load schedules"
                )
            }
        }

        viewModel.stats.observe(viewLifecycleOwner) { result ->
            result.onSuccess { dashboardResponse ->
                updateDashboardUI(dashboardResponse.patient_stats, dashboardResponse.health_vitals)
            }.onFailure { exception ->
                // Handle the error (e.g., show a Snackbar)
                SnackbarUtils.showSnackbar(binding.root, exception.message ?: "Error fetching stats", true)
            }
        }
    }

    private fun setupCalendar() {
        val today = LocalDate.now()
        
        // Set calendar title with current date
        val formatter = DateTimeFormatter.ofPattern("d'th' MMMM")
        binding.calendarTitle.text = "Today, ${today.format(formatter)}"

        val currentDayOfWeek = today.dayOfWeek.value
        val monday = today.minusDays((currentDayOfWeek - 1).toLong())

        binding.calendarContainer.removeAllViews()

        for (i in 0..6) {
            val date = monday.plusDays(i.toLong())
            val isToday = date == today
            
            val dateView = ItemCalendarDateBinding.inflate(
                layoutInflater,
                binding.calendarContainer,
                false
            ).root as MaterialCardView

            // Set date number and day name
            dateView.findViewById<TextView>(R.id.dateNumber).apply {
                text = date.dayOfMonth.toString()
                setTextColor(if (isToday) 
                    ContextCompat.getColor(requireContext(), R.color.surface_light)
                else 
                    ContextCompat.getColor(requireContext(), 
                        if (isNightMode()) R.color.text_primary_dark else R.color.text_primary_light
                    )
                )
            }

            dateView.findViewById<TextView>(R.id.dayName).apply {
                text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                setTextColor(if (isToday) 
                    ContextCompat.getColor(requireContext(), R.color.surface_light)
                else 
                    ContextCompat.getColor(requireContext(), 
                        if (isNightMode()) R.color.text_secondary_dark else R.color.text_secondary_light
                    )
                )
            }

            // Set background color based on theme and selection
            if (isToday) {
                dateView.setCardBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.primary)
                )
            } else {
                dateView.setCardBackgroundColor(
                    ContextCompat.getColor(requireContext(), 
                        if (isNightMode()) R.color.surface_dark else R.color.surface_light
                    )
                )
            }

            // Add to container
            binding.calendarContainer.addView(dateView)
        }
    }

    private fun isNightMode(): Boolean {
        return resources.configuration?.uiMode?.and(android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    @SuppressLint("SetTextI18n")
    private fun updateDashboardUI(patientStats: PatientStats, healthVitals: List<HealthVital>) {
        binding.medicationCount.text = patientStats.medication.current.toString()
        binding.caregiversCount.text = patientStats.caregiver.current.toString()
        binding.sideEffectsCount.text = patientStats.side_effect.current.toString()
        binding.diagnosesCount.text = patientStats.diagnosis.current.toString()

        // Update medication change text and color
        binding.medicationChangeText.text = "${patientStats.medication.change} ${patientStats.medication.label}"
        setChangeTextColor(binding.medicationChangeText, patientStats.medication.change)

        // Update caregiver change text (no color change)
        binding.caregiverChangeText.text = "${patientStats.caregiver.change} ${patientStats.caregiver.label}"

        // Update side effect change text and color
        binding.sideEffectChangeText.text = "${patientStats.side_effect.change} ${patientStats.side_effect.label}"
        setChangeTextColor(binding.sideEffectChangeText, patientStats.side_effect.change)

        // Update diagnoses change text and color
        binding.diagnosesChangeText.text = "${patientStats.diagnosis.change} ${patientStats.diagnosis.label}"
        setChangeTextColor(binding.diagnosesChangeText, patientStats.diagnosis.change)

        for (vital in healthVitals) {
            when (vital.name) {
                "Blood Pressure" -> binding.bloodPressureStat.text = "${vital.value} ${vital.unit}"
                "Heart Rate" -> binding.heartRateStat.text = "${vital.value} ${vital.unit}"
                "Glucose" -> binding.glucoseStat.text = "${vital.value} ${vital.unit}"
                "Cholesterol" -> binding.cholesterolStat.text = "${vital.value} ${vital.unit}"
            }
        }
    }

    // Helper function to set text color based on change value
    private fun setChangeTextColor(textView: TextView, change: String) {
        val changeValue = change.replace("%", "").toIntOrNull() ?: 0
        textView.setTextColor(
            when {
                changeValue < 0 -> ContextCompat.getColor(requireContext(), R.color.success) // Red for negative
                changeValue > 0 -> ContextCompat.getColor(requireContext(), R.color.error) // Green for positive
                else -> ContextCompat.getColor(requireContext(), R.color.success)
            }
        )
    }

    // Function to fetch medication details and navigate
    private fun fetchMedicationDetails(medicationId: Int) {
        // Use the viewModel to launch a coroutine
        viewModel.viewModelScope.launch {
            try {
                // Call the repository method to fetch medication details
                val result = viewModel.repository.getMedicationById(medicationId)
                // Navigate to MedicationDetailFragment with the medication details
                val action = HomeFragmentDirections.actionHomeFragmentToMedicationDetailFragment(result)
                findNavController().navigate(action)
            } catch (e: Exception) {
                SnackbarUtils.showSnackbar(binding.root, e.message ?: "Error fetching medication details", true)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 