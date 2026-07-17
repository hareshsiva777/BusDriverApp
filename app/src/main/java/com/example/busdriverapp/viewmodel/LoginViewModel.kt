package com.example.busdriverapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.busdriverapp.data.database.AppDatabase
import com.example.busdriverapp.data.model.DriverEntity
import com.example.busdriverapp.data.repository.DriverRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents everything needed by the offline login screen.
 */
data class LoginUiState(
    val driver: DriverEntity? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * Handles driver authentication using the local Room database.
 *
 * A default driver record is inserted when the app is first opened.
 * Because login reads from Room, authentication continues to work offline.
 */
class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database =
        AppDatabase.getDatabase(application)

    private val driverRepository =
        DriverRepository(
            driverDao = database.driverDao()
        )

    private val _uiState =
        MutableStateFlow(LoginUiState())

    val uiState: StateFlow<LoginUiState> =
        _uiState.asStateFlow()

    init {
        initialiseDefaultDriver()
    }

    /**
     * Creates the assessment demo account only when no drivers exist.
     */
    private fun initialiseDefaultDriver() {
        viewModelScope.launch {
            try {
                driverRepository.ensureDefaultDriver()

                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )
            } catch (exception: Exception) {
                _uiState.value =
                    LoginUiState(
                        isLoading = false,
                        errorMessage =
                            "Unable to initialise offline login: " +
                                    (exception.message ?: "Unknown error")
                    )
            }
        }
    }

    /**
     * Validates the entered credentials using locally stored driver data.
     */
    fun login(
        driverId: String,
        password: String
    ) {
        val cleanedDriverId =
            driverId.trim().uppercase()

        if (cleanedDriverId.isBlank() || password.isBlank()) {
            _uiState.value =
                _uiState.value.copy(
                    isLoading = false,
                    errorMessage =
                        "Enter your Driver ID and password."
                )
            return
        }

        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

            try {
                // Ensure the local account exists before validating it.
                driverRepository.ensureDefaultDriver()

                val driver =
                    driverRepository.login(
                        driverId = cleanedDriverId,
                        password = password
                    )

                if (driver != null) {
                    _uiState.value =
                        LoginUiState(
                            driver = driver,
                            isLoading = false,
                            errorMessage = null
                        )
                } else {
                    _uiState.value =
                        LoginUiState(
                            driver = null,
                            isLoading = false,
                            errorMessage =
                                "Invalid Driver ID or password."
                        )
                }
            } catch (exception: Exception) {
                _uiState.value =
                    LoginUiState(
                        driver = null,
                        isLoading = false,
                        errorMessage =
                            "Unable to log in: " +
                                    (exception.message ?: "Unknown error")
                    )
            }
        }
    }

    /**
     * Removes the current authenticated driver from the UI state.
     */
    fun logout() {
        _uiState.value =
            LoginUiState(
                driver = null,
                isLoading = false,
                errorMessage = null
            )
    }

    /**
     * Clears the previous error as soon as the driver edits an input.
     */
    fun clearError() {
        if (_uiState.value.errorMessage != null) {
            _uiState.value =
                _uiState.value.copy(
                    errorMessage = null
                )
        }
    }
}