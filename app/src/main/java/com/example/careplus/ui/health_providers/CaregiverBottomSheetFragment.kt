package com.example.careplus.ui.health_providers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayCaregiverDetails()
    }

    private fun displayCaregiverDetails() {
        binding.caregiverName.text = caregiverData.name
        binding.caregiverEmail.text = caregiverData.email
        binding.caregiverRole.text = caregiverData.role
        // Load avatar if available
        // Glide.with(this).load(caregiverData.profile.avatar).into(binding.caregiverAvatar)
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