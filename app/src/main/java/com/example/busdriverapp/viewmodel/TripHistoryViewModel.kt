package com.example.busdriverapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.busdriverapp.data.database.AppDatabase
import com.example.busdriverapp.data.model.TripEntity
import com.example.busdriverapp.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TripHistoryUiState(
    val trips: List<TripEntity> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null
)

class TripHistoryViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database =
        AppDatabase.getDatabase(application)

    private val tripRepository =
        TripRepository(
            tripDao = database.tripDao()
        )

    private val _uiState =
        MutableStateFlow(
            TripHistoryUiState()
        )

    val uiState:
            StateFlow<TripHistoryUiState> =
        _uiState.asStateFlow()

    init {
        observeTripHistory()
    }

    private fun observeTripHistory() {

        viewModelScope.launch {

            try {
                tripRepository
                    .observeCompletedTrips()
                    .collect { trips ->

                        _uiState.value =
                            TripHistoryUiState(
                                trips = trips,
                                isLoading = false
                            )
                    }

            } catch (exception: Exception) {

                _uiState.value =
                    TripHistoryUiState(
                        trips = emptyList(),
                        isLoading = false,
                        message =
                            exception.message
                                ?: "Unable to load trip history."
                    )
            }
        }
    }
}