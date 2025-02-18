package com.example.careplus.ui.profile

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.careplus.R
import com.example.careplus.data.api.FileUploadResponse
import com.example.careplus.databinding.FragmentEditProfileBinding
import com.example.careplus.utils.SnackbarUtils
import com.example.careplus.data.model.profile.ProfileData
import com.example.careplus.data.model.profile.ProfileUpdateRequest
import com.example.careplus.data.model.profile.UserProfile
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.Calendar

class EditProfileFragment : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private val args: EditProfileFragmentArgs by navArgs()

    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedUri ->
            selectedImageUri = selectedUri // Store the URI for later upload
            // Show image preview
            Glide.with(this)
                .load(selectedUri)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .circleCrop()
                .into(binding.profileImage)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupGenderDropdown()
        setupDatePicker()
        setupObservers()
        setupListeners()
        
        populateFields(args.profileData)
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setPageTitle("Edit Profile")
        }
    }

    private fun setupGenderDropdown() {
        val genders = arrayOf("male", "female", "other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genders)
        binding.genderInput.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        binding.dobInput.setOnClickListener {
            showDatePicker()
        }
    }

    private fun setupListeners() {
        binding.changeImageButton.setOnClickListener {
            if (checkPermissions()) {
                pickImage.launch("image/*")
            } else {
                requestPermissions()
            }
        }

        binding.saveButton.setOnClickListener {
            saveProfile()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.show()
            } else {
                binding.loadingOverlay.hide()
            }
            binding.saveButton.isEnabled = !isLoading
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                showSnackbar("Profile updated successfully", isError = false)
                findNavController().popBackStack()
            }.onFailure { exception ->
                showSnackbar(exception.message ?: "Failed to update profile")
            }
        }
    }

    private fun populateFields(profileData: ProfileData) {
        binding.apply {
            genderInput.setText(profileData.profile.gender)
            dobInput.setText(profileData.profile.date_of_birth)
            phoneInput.setText(profileData.profile.phone_number)
            addressInput.setText(profileData.profile.address)

            // Load profile image
            profileData.profile.avatar?.let { avatarUrl ->
                if (!avatarUrl.isNullOrBlank() && avatarUrl != "null") {
                    val imageUrl = viewModel.getDisplayImageUrl(avatarUrl)
                    imageUrl?.let {
                        Glide.with(this@EditProfileFragment)
                            .load(it)
                            .placeholder(R.drawable.default_profile)
                            .error(R.drawable.default_profile)
                            .circleCrop()
                            .into(profileImage)
                    }
                }
            }
        }
    }

    private suspend fun uploadImage(uri: Uri): Result<FileUploadResponse> {
        return try {
            val contentResolver = requireContext().contentResolver
            
            // Get file name from URI
            val fileName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: "image.jpg"
            
            // Create temp file
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(requireContext().cacheDir, fileName)
            file.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
            }

            // Create request body
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData(
                name = "file", // This must match Laravel's $request->file('file')
                filename = fileName,
                body = requestFile
            )

            // Upload and get result
            var uploadResult: Result<FileUploadResponse>? = null
            viewModel.uploadProfileImage(body).collect { result ->
                uploadResult = result
            }
            
            // Clean up temp file
            file.delete()
            
            uploadResult ?: Result.failure(Exception("Failed to upload image"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.loadingOverlay.show("Updating profile...")
                
                // If there's a selected image, upload it first
                val avatarPath = if (selectedImageUri != null) {
                    val uploadResult = uploadImage(selectedImageUri!!)
                    uploadResult.getOrNull()?.data?.file_path
                } else {
                    null
                }

                // Create profile update request with the file path
                val request = ProfileUpdateRequest(
                    gender = binding.genderInput.text?.toString(),
                    date_of_birth = binding.dobInput.text?.toString(),
                    phone_number = binding.phoneInput.text?.toString(),
                    address = binding.addressInput.text?.toString(),
                    avatar = avatarPath
                )
                
                viewModel.updateProfile(request)
                
            } catch (e: Exception) {
                showSnackbar("Failed to update profile: ${e.message}")
            } finally {
                binding.loadingOverlay.hide()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val date = String.format("%d-%02d-%02d", year, month + 1, day)
                binding.dobInput.setText(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showSnackbar(message: String, isError: Boolean = true) {
        SnackbarUtils.showSnackbar(
            view = binding.root,
            message = message,
            isError = isError
        )
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                PERMISSION_REQUEST_CODE
            )
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 