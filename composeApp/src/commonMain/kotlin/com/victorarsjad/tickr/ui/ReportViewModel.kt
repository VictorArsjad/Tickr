package com.victorarsjad.tickr.ui

import com.victorarsjad.tickr.domain.model.Period
import com.victorarsjad.tickr.domain.model.ReportData
import com.victorarsjad.tickr.domain.usecase.GetReportDataUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReportViewModel(
    private val getReportDataUseCase: GetReportDataUseCase
) {
    data class State(
        val reportData: ReportData? = null,
        val selectedPeriod: Period = Period.DAY,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun loadReport(tickrId: String, period: Period) {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val data = getReportDataUseCase(tickrId, period)
                _state.update { it.copy(reportData = data, selectedPeriod = period, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun changePeriod(period: Period) {
        val id = _state.value.reportData?.tickrId ?: return
        loadReport(id, period)
    }
}