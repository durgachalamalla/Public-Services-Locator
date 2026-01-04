package com.example.publicserviceslocator.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.publicserviceslocator.data.local.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Unified state class for Authentication.
 * This includes the current user, loading state, and any error messages.
 */
data class AuthState(
    val user: FirebaseUser? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    // Initialize with the current user status from Firebase
    private val _authState = MutableStateFlow(AuthState(user = auth.currentUser))
    val authState = _authState.asStateFlow()

    init {
        // Listen for changes in real-time (Login, Logout, Token refresh)
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = _authState.value.copy(user = firebaseAuth.currentUser)
        }
    }

    /**
     * Signs in an existing user with email and password.
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = _authState.value.copy(error = "Fields cannot be empty")
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            try {
                // Using await() to handle the Firebase Task in a coroutine
                auth.signInWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(error = e.localizedMessage)
            } finally {
                _authState.value = _authState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Creates a new user account in Firebase.
     */
    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = _authState.value.copy(error = "Fields cannot be empty")
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(error = e.localizedMessage)
            } finally {
                _authState.value = _authState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Clears local cache and signs the user out.
     * Uses Dispatchers.IO to prevent crashes during database operations.
     */
    fun logout(database: AppDatabase) {
        viewModelScope.launch(Dispatchers.IO) {
            try {


                //  Sign out of Firebase
                auth.signOut()
                //  Clear the local Room database so the next user starts fresh
                database.favoriteDao().clearAllFavorites()

                //  Update the UI state to reflect the logout
                _authState.value = AuthState(user = null)
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(error = "Logout failed: ${e.localizedMessage}")
            }
        }
    }
}