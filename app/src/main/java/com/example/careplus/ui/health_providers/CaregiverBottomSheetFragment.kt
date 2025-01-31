package com.example.careplus.ui.health_providers

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.careplus.R
import com.example.careplus.data.model.CaregiverData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.careplus.databinding.FragmentCaregiverBottomSheetBinding

class CaregiverBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentCaregiverBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var caregiverData: CaregiverData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            caregiverData = it.getParcelable(ARG_CAREGIVER)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaregiverBottomSheetBinding.inflate(inflater, container, false)
        
        // Set the background of the bottom sheet to transparent
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayCaregiverDetails()
        binding.menuButton.setOnClickListener {
            showMenu(it)
        }
    }

    private fun showMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)

        // Inflate the menu layout
        popupMenu.menuInflater.inflate(R.menu.caregiver_actions_menu, popupMenu.menu)

        // Clear existing menu items
        popupMenu.menu.clear()

        // Add menu items conditionally based on the caregiver's role
        if (caregiverData.role == "Doctor") {
            popupMenu.menu.add(0, R.id.action_set_as_doctor, 0, "Set as My Doctor")
        } else if (caregiverData.role == "Caregiver") {
            popupMenu.menu.add(0, R.id.action_set_as_caregiver, 0, "Set as Caregiver")
        }

        // Always add these options
        popupMenu.menu.add(0, R.id.action_call, 0, "Call")
        popupMenu.menu.add(0, R.id.action_send_report, 0, "Send Health Report")

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_call -> {
                    Toast.makeText(requireContext(), "Call clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_send_report -> {
                    Toast.makeText(requireContext(), "Send Health Report clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_set_as_doctor -> {
                    Toast.makeText(requireContext(), "Set as My Doctor clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_set_as_caregiver -> {
                    Toast.makeText(requireContext(), "Set as Caregiver clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun displayCaregiverDetails() {
        binding.caregiverName.text = caregiverData.name
        binding.caregiverEmail.text = caregiverData.email
        binding.caregiverRole.text = caregiverData.role

        binding.caregiverAddress.text = caregiverData.profile.address ?: "---"
        binding.caregiverPhone.text = caregiverData.profile.phone_number ?: "---"
        binding.caregiverSpecialization.text = caregiverData.user_role.specialization ?: "---"

        // Load agency and clinic details
        binding.agencyName.text = caregiverData.user_role.agency_name ?: "---"
        binding.clinicName.text = caregiverData.user_role.clinic_name ?: "---"

        // Load profile image (if available)
        if (!caregiverData.profile.avatar.isNullOrEmpty()) {
            Glide.with(binding.root.context)
                .load(caregiverData.profile.avatar)
                .placeholder(R.drawable.caregiver)
                .error(R.drawable.caregiver)
                .into(binding.profileImage)
        } else {
            binding.profileImage.setImageResource(R.drawable.caregiver)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CAREGIVER = "arg_caregiver"

        fun newInstance(caregiver: CaregiverData): CaregiverBottomSheetFragment {
            val fragment = CaregiverBottomSheetFragment()
            val args = Bundle().apply {
                putParcelable(ARG_CAREGIVER, caregiver)
            }
            fragment.arguments = args
            return fragment
        }
    }

} 