package org.hak.fitnesstrackerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.network.UserProfileResponse
import org.hak.fitnesstrackerapp.network.models.ProfileResponse
import org.hak.fitnesstrackerapp.network.models.UpdateProfileRequest
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
            val result = repository.updateProfile(UpdateProfileRequest(height, weight, birthDate))

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
            val result = repository.changePassword(currentPassword, newPassword)

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
    data class Success(val response: ProfileResponse) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class UpdateProfileState {
    object Loading : UpdateProfileState()
    data class Success(val response: UserProfileResponse) : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}

sealed class ChangePasswordState {
    object Loading : ChangePasswordState()
    object Success : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}