package com.example.careplus.ui.health_providers

import android.app.Dialog
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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.careplus.data.SessionManager
import com.example.careplus.data.repository.CaregiverRepository
import com.example.careplus.utils.SnackbarUtils
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import com.example.careplus.MainActivity
import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.example.careplus.utils.BottomSheetUtils

interface CaregiverActionListener {
    fun onCaregiverRemoved(roleId: Int)
    fun onDoctorRemoved(roleId: Int)
}

class CaregiverBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentCaregiverBottomSheetBinding? = null
    private val binding get() = _binding!!
    private var actionListener: CaregiverActionListener? = null
    private var myHealthProvider:Boolean = false

    private lateinit var caregiverData: CaregiverData
    private lateinit var repository: CaregiverRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            caregiverData = it.getParcelable(ARG_CAREGIVER)!!
        }
        repository = CaregiverRepository()
        sessionManager = SessionManager(requireContext())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        actionListener = parentFragment as? CaregiverActionListener
        Log.d("BottomSheet", "onAttach: actionListener is ${if (actionListener == null) "null" else "not null"}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaregiverBottomSheetBinding.inflate(inflater, container, false)
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
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.caregiver_actions_menu, popup.menu)

        // Show/hide menu items based on role
        val menu = popup.menu
        when (caregiverData.role.lowercase()) {
            "doctor" -> {
                menu.findItem(R.id.action_set_as_caregiver)?.isVisible = false
                menu.findItem(R.id.action_remove_as_caregiver)?.isVisible = false
            }
            "caregiver" -> {
                menu.findItem(R.id.action_set_as_doctor)?.isVisible = false
                menu.findItem(R.id.action_remove_as_doctor)?.isVisible = false
            }
        }
        menu.findItem(R.id.action_set_as_caregiver).isVisible = !myHealthProvider
        menu.findItem(R.id.action_set_as_doctor).isVisible = !myHealthProvider

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_set_as_doctor -> {
                    handleSetDoctor()
                    true
                }
                R.id.action_remove_as_doctor -> {
                    handleRemoveDoctor()
                    true
                }
                R.id.action_set_as_caregiver -> {
                    handleSetCaregiver()
                    true
                }
                R.id.action_remove_as_caregiver -> {
                    handleRemoveCaregiver()
                    true
                }
//                R.id.action_call -> {
//                    // Handle call action
//                    Toast.makeText(context, "Call action clicked", Toast.LENGTH_SHORT).show()
//                    true
//                }
//                R.id.action_send_report -> {
//                    // Handle send report action
//                    Toast.makeText(context, "Send report clicked", Toast.LENGTH_SHORT).show()
//                    true
//                }
                else -> false
            }
        }
        popup.show()
    }

    private fun handleSetDoctor() {
        lifecycleScope.launch {
            try {
                val patientId = sessionManager.getUser()?.patient?.id
                if (patientId != null) {
                    val result = repository.setDoctor(caregiverData.user_role.id, patientId)
                    result.onSuccess { response ->
                        if (response.error) {
                            showSnackbar(response.message)
                        } else {
                            showSnackbar("Successfully set as doctor", false)
                        }
                    }.onFailure { exception ->
                        showSnackbar(exception.message ?: "Failed to set doctor")
                    }
                }
            } catch (e: Exception) {
                showSnackbar(e.message ?: "An error occurred")
            }
            dismiss()
        }
    }

    private fun handleRemoveDoctor() {
        lifecycleScope.launch {
            try {
                val patientId = sessionManager.getUser()?.patient?.id
                if (patientId != null) {
                    val result = repository.removeDoctor(caregiverData.user_role.id, patientId)
                    result.onSuccess { response ->
                        if (response.error) {
                            showSnackbar(response.message)
                        } else {
                            Log.d("BottomSheet", "Calling onDoctorRemoved with roleId: ${caregiverData.user_role.id}")
                            actionListener?.onDoctorRemoved(caregiverData.user_role.id)
                            showSnackbar("Successfully removed doctor", false)
                        }
                    }.onFailure { exception ->
                        showSnackbar(exception.message ?: "Failed to remove doctor")
                    }
                }
            } catch (e: Exception) {
                showSnackbar(e.message ?: "An error occurred")
            }
            dismiss()
        }
    }

    private fun handleSetCaregiver() {
        lifecycleScope.launch {
            try {
                val patientId = sessionManager.getUser()?.patient?.id
                if (patientId != null) {
                    val result = repository.setCaregiver(caregiverData.user_role.id, patientId, "mother")
                    result.onSuccess { response ->
                        if (response.error) {
                            showSnackbar(response.message)
                        } else {
                            showSnackbar("Successfully set as caregiver", false)
                        }
                    }.onFailure { exception ->
                        showSnackbar(exception.message ?: "Failed to set caregiver")
                    }
                }
            } catch (e: Exception) {
                showSnackbar(e.message ?: "An error occurred")
            }
            dismiss()

        }
    }

    private fun handleRemoveCaregiver() {
        lifecycleScope.launch {
            try {
                val patientId = sessionManager.getUser()?.patient?.id
                if (patientId != null) {
                    val result = repository.removeCaregiver(caregiverData.user_role.id, patientId)
                    result.onSuccess { response ->
                        if (response.error) {
                            showSnackbar(response.message)
                        } else {
                            actionListener?.onCaregiverRemoved(caregiverData.user_role.id)
                            showSnackbar("Successfully removed caregiver", false)
                        }
                    }.onFailure { exception ->
                        showSnackbar(exception.message ?: "Failed to remove caregiver")
                    }
                }
            } catch (e: Exception) {
                showSnackbar(e.message ?: "An error occurred")
            }
            dismiss()
        }
    }

    private fun showSnackbar(message: String, isError: Boolean = true) {
        val activity = requireActivity() as MainActivity
        val bottomNav = activity.findViewById<View>(R.id.bottomNav)
        val rootView = activity.findViewById<View>(android.R.id.content)

        val snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(
                requireContext(),
                if (isError) R.color.error else R.color.success
            ))
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.surface_light))
            .setAnchorView(bottomNav)

        snackbar.show()
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

    override fun onDetach() {
        super.onDetach()
        actionListener = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        BottomSheetUtils.setupBottomSheetDialog(this, dialog)
        return dialog
    }

    companion object {
        private const val ARG_CAREGIVER = "arg_caregiver"

        fun newInstance(caregiver: CaregiverData, myHealthProvider:Boolean = false): CaregiverBottomSheetFragment {
            val fragment = CaregiverBottomSheetFragment()
            fragment.myHealthProvider = myHealthProvider
            val args = Bundle().apply {
                putParcelable(ARG_CAREGIVER, caregiver)
            }
            fragment.arguments = args
            return fragment
        }
    }
} 