package org.hak.fitnesstrackerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.network.models.AuthResponse
import org.hak.fitnesstrackerapp.repository.FitnessRepository
import org.hak.fitnesstrackerapp.repository.SharedPrefManager

class RegisterViewModel(
    private val repository: FitnessRepository,
    private val sharedPrefManager: SharedPrefManager
) : ViewModel() {

    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState

    fun register(username: String, email: String, password: String, confirmPassword: String) {
        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _registerState.value = RegisterState.Error("All fields are required")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registerState.value = RegisterState.Error("Invalid email format")
            return
        }

        if (password.length < 6) {
            _registerState.value = RegisterState.Error("Password must be at least 6 characters")
            return
        }

        if (password != confirmPassword) {
            _registerState.value = RegisterState.Error("Passwords do not match")
            return
        }

        _registerState.value = RegisterState.Loading

        viewModelScope.launch {
            val result = repository.register(username, email, password, confirmPassword)

            when {
                result.isSuccess -> {
                    val user = result.getOrNull()!!
                    sharedPrefManager.saveUser(user)
                    _registerState.value = RegisterState.Success(user)
                }
                result.isFailure -> {
                    _registerState.value = RegisterState.Error(
                        result.exceptionOrNull()?.message ?: "Registration failed"
                    )
                }
            }
        }
    }
}

sealed class RegisterState {
    object Loading : RegisterState()
    data class Success(val user: AuthResponse) : RegisterState()
    data class Error(val message: String) : RegisterState()
}