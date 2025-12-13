package org.hak.fitnesstrackerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.network.models.AuthResponse
import org.hak.fitnesstrackerapp.repository.FitnessRepository
import org.hak.fitnesstrackerapp.repository.SharedPrefManager

class LoginViewModel(
    private val repository: FitnessRepository,
    private val sharedPrefManager: SharedPrefManager
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(username: String, password: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result = repository.login(username, password)

            when {
                result.isSuccess -> {
                    val user = result.getOrNull()!!
                    sharedPrefManager.saveUser(user)
                    _loginState.value = LoginState.Success(user)
                }
                result.isFailure -> {
                    _loginState.value = LoginState.Error(
                        result.exceptionOrNull()?.message ?: "Login failed"
                    )
                }
            }
        }
    }
}

sealed class LoginState {
    object Loading : LoginState()
    data class Success(val user: AuthResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}