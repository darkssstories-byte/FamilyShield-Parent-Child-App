package com.familyshield.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {
    private val authRepo = AuthRepository()
    private val deviceRepo = DeviceRepository()
    private val pairingRepo = PairingRepository()

    private val _ui = MutableStateFlow(
        UiState(
            configured = Backend.configured,
            signedIn = Backend.configured && authRepo.currentUser != null
        )
    )
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private var devicesJob: Job? = null

    init {
        if (Backend.configured) {
            Backend.auth.addAuthStateListener { fa ->
                val user = fa.currentUser
                _ui.update { it.copy(signedIn = user != null) }
                if (user != null) startObserving(user.uid) else stopObserving()
            }
        }
    }

    private fun startObserving(uid: String) {
        devicesJob?.cancel()
        _ui.update { it.copy(devicesLoading = true, devicesError = null) }
        devicesJob = viewModelScope.launch {
            deviceRepo.observe(uid)
                .catch { e ->
                    _ui.update { s -> s.copy(devicesLoading = false, devicesError = friendly(e)) }
                }
                .collect { list ->
                    _ui.update { s -> s.copy(devices = list, devicesLoading = false, devicesError = null) }
                }
        }
    }

    private fun stopObserving() {
        devicesJob?.cancel()
        _ui.update { it.copy(devices = emptyList(), devicesLoading = false, pairing = PairingUi.Idle) }
    }

    fun signIn(email: String, password: String) = runAuth { authRepo.signIn(email.trim(), password) }

    fun register(email: String, password: String) = runAuth { authRepo.register(email.trim(), password) }

    private fun runAuth(block: suspend () -> Unit) {
        viewModelScope.launch {
            _ui.update { it.copy(authBusy = true, authError = null) }
            try {
                block()
            } catch (e: Exception) {
                _ui.update { it.copy(authError = friendly(e)) }
            }
            _ui.update { it.copy(authBusy = false) }
        }
    }

    fun signOut() {
        authRepo.signOut()
    }

    fun setDemo(enabled: Boolean) {
        _ui.update { it.copy(demo = enabled) }
    }

    fun generateCode() {
        _ui.update { it.copy(pairing = PairingUi.Loading) }
        viewModelScope.launch {
            try {
                val code = pairingRepo.createCode()
                _ui.update { it.copy(pairing = PairingUi.Ready(code)) }
            } catch (e: Exception) {
                _ui.update { it.copy(pairing = PairingUi.Error(friendly(e))) }
            }
        }
    }

    fun unlink(deviceId: String) {
        viewModelScope.launch {
            try {
                pairingRepo.unlinkDevice(deviceId)
                _ui.update { it.copy(message = "Device unlinked.") }
            } catch (e: Exception) {
                _ui.update { it.copy(message = friendly(e)) }
            }
        }
    }

    fun deleteData() {
        viewModelScope.launch {
            try {
                pairingRepo.deleteFamilyData()
                _ui.update { it.copy(message = "Your family data was deleted.") }
            } catch (e: Exception) {
                _ui.update { it.copy(message = friendly(e)) }
            }
        }
    }

    fun clearMessage() {
        _ui.update { it.copy(message = null) }
    }

    private fun friendly(e: Throwable): String = when (e) {
        is FirebaseAuthWeakPasswordException -> "Password is too weak. Use at least 8 characters."
        is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password."
        is FirebaseAuthInvalidUserException -> "No account found for that email."
        is FirebaseAuthUserCollisionException -> "An account with this email already exists."
        is FirebaseNetworkException -> "No network connection. Check your internet and try again."
        is FirebaseFunctionsException -> when (e.code) {
            FirebaseFunctionsException.Code.NOT_FOUND,
            FirebaseFunctionsException.Code.UNAVAILABLE ->
                "The Family Shield backend is not deployed or not reachable yet."
            FirebaseFunctionsException.Code.UNAUTHENTICATED -> "Please sign in again."
            else -> e.message ?: "Server error."
        }
        is FirebaseFirestoreException ->
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
                "Access denied. Firestore security rules may not be deployed yet."
            else e.message ?: "Database error."
        else -> e.message ?: "Something went wrong."
    }
}
