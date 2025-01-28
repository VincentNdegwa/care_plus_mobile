package com.example.careplus.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
        
        setupRecyclerView()
        setupObservers()
        setupCalendar()
        
        // Only fetch profile if we don't have stored user data
        if (sessionManager.getUser() == null) {
            viewModel.fetchProfile()
        }
        viewModel.fetchMedicationSchedules()
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // No need to set bottom nav selection
    }

    private fun setupRecyclerView() {
        scheduleAdapter = MedicationScheduleAdapter()
        binding.medicationsList.apply {
            adapter = scheduleAdapter
            layoutManager = LinearLayoutManager(context)
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 