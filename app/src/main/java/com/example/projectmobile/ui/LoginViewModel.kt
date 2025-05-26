package com.example.projectmobile.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
    object Idle : AuthResult()
}

class LoginViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val authState: StateFlow<AuthResult> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        _authState.value = AuthResult.Loading
        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _authState.value = if (task.isSuccessful) {
                    AuthResult.Success
                } else {
                    AuthResult.Error(task.exception?.localizedMessage ?: "Errore sconosciuto")
                }
            }
    }
}

