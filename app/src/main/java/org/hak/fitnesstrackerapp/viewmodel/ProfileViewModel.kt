package org.hak.fitnesstrackerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.repository.FitnessRepository

class ProfileViewModel(private val repository: FitnessRepository) : ViewModel() {

    private val _profileState = MutableLiveData<ProfileState>()
    val profileState: LiveData<ProfileState> = _profileState

    private val _updateProfileState = MutableLiveData<UpdateProfileState>()
    val updateProfileState: LiveData<UpdateProfileState> = _updateProfileState

    private val _changePasswordState = MutableLiveData<ChangePasswordState>()
    val changePasswordState: LiveData<ChangePasswordState> = _changePasswordState

    fun loadProfile() {
        _profileState.value = ProfileState.Loading

        viewModelScope.launch {
            val result = repository.getUserProfile()

            when {
                result.isSuccess -> {
                    _profileState.value = ProfileState.Success(result.getOrNull()!!)
                }
                result.isFailure -> {
                    _profileState.value = ProfileState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to load profile"
                    )
                }
            }
        }
    }

    fun updateProfile(height: Double?, weight: Double?, birthDate: String?) {
        _updateProfileState.value = UpdateProfileState.Loading

        viewModelScope.launch {
            // Create UpdateProfileRequest object
            val request = org.hak.fitnesstrackerapp.network.models.UpdateProfileRequest(
                heightCm = height,
                weightKg = weight,
                birthDate = birthDate
            )

            val result = repository.updateProfile(request)

            when {
                result.isSuccess -> {
                    _updateProfileState.value = UpdateProfileState.Success(result.getOrNull()!!)
                }
                result.isFailure -> {
                    _updateProfileState.value = UpdateProfileState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to update profile"
                    )
                }
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        // Validation
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            _changePasswordState.value = ChangePasswordState.Error("All fields are required")
            return
        }

        if (newPassword.length < 6) {
            _changePasswordState.value = ChangePasswordState.Error("New password must be at least 6 characters")
            return
        }

        if (newPassword != confirmPassword) {
            _changePasswordState.value = ChangePasswordState.Error("New passwords do not match")
            return
        }

        _changePasswordState.value = ChangePasswordState.Loading

        viewModelScope.launch {
            val result = repository.changePassword(currentPassword, newPassword, confirmPassword)

            when {
                result.isSuccess -> {
                    _changePasswordState.value = ChangePasswordState.Success
                }
                result.isFailure -> {
                    _changePasswordState.value = ChangePasswordState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to change password"
                    )
                }
            }
        }
    }
}

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val response: org.hak.fitnesstrackerapp.network.models.ProfileResponse) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class UpdateProfileState {
    object Loading : UpdateProfileState()
    data class Success(val response: org.hak.fitnesstrackerapp.network.models.UserProfileResponse) : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}

sealed class ChangePasswordState {
    object Loading : ChangePasswordState()
    object Success : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}