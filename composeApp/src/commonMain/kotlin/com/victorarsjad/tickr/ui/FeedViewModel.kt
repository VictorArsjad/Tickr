package com.victorarsjad.tickr.ui

import com.victorarsjad.tickr.domain.model.Period
import com.victorarsjad.tickr.domain.model.Tickr
import com.victorarsjad.tickr.domain.model.TickrType
import com.victorarsjad.tickr.domain.model.TimeSession
import com.victorarsjad.tickr.domain.repository.SessionRepository
import com.victorarsjad.tickr.domain.repository.TickrRepository
import com.victorarsjad.tickr.domain.usecase.CreateTickrUseCase
import com.victorarsjad.tickr.domain.usecase.IncrementCountUseCase
import com.victorarsjad.tickr.domain.usecase.StartSessionUseCase
import com.victorarsjad.tickr.domain.usecase.StopSessionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedViewModel(
    private val createTickrUseCase: CreateTickrUseCase,
    private val incrementCountUseCase: IncrementCountUseCase,
    private val startSessionUseCase: StartSessionUseCase,
    private val stopSessionUseCase: StopSessionUseCase,
    private val tickrRepository: TickrRepository,
    private val sessionRepository: SessionRepository
) {
    data class CreationFormState(
        val name: String = "",
        val type: TickrType = TickrType.COUNT,
        val period: Period = Period.DAY,
        val error: String? = null
    )

    data class State(
        val tickrs: List<Tickr> = emptyList(),
        val activeSessions: Map<String, TimeSession> = emptyMap(),
        val currentValues: Map<String, Long> = emptyMap(),
        val baseDurations: Map<String, Long> = emptyMap(),
        val isCreating: Boolean = false,
        val creationFormState: CreationFormState = CreationFormState()
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        scope.launch {
            tickrRepository.getAllTickrs().collect { list ->
                _state.update { it.copy(tickrs = list.filter { t -> !t.isDeleted }) }
                refreshBaseAndCounts()
            }
        }
        scope.launch {
            sessionRepository.getAllActiveSessions().collect { sessions ->
                _state.update { it.copy(activeSessions = sessions.associateBy { s -> s.tickrId }) }
                refreshBaseAndCounts()
            }
        }

        scope.launch {
            while (true) {
                updateTimerValues()
                val interval = if (_state.value.activeSessions.isNotEmpty()) 50L else 1000L
                kotlinx.coroutines.delay(interval)
            }
        }
    }

    fun onToggleCreationForm() {
        _state.update { it.copy(isCreating = !it.isCreating) }
    }

    fun onUpdateCreationFormName(name: String) {
        _state.update { it.copy(creationFormState = it.creationFormState.copy(name = name)) }
    }

    fun onUpdateCreationFormType(type: TickrType) {
        _state.update { it.copy(creationFormState = it.creationFormState.copy(type = type)) }
    }

    fun onUpdateCreationFormPeriod(period: Period) {
        _state.update { it.copy(creationFormState = it.creationFormState.copy(period = period)) }
    }

    fun onCreateTickr() {
        val form = _state.value.creationFormState
        scope.launch {
            try {
                createTickrUseCase(form.name, form.type, form.period)
                _state.update { it.copy(isCreating = false, creationFormState = CreationFormState()) }
            } catch (e: Exception) {
                _state.update { it.copy(creationFormState = it.creationFormState.copy(error = e.message)) }
            }
        }
    }

    fun onActionTickr(tickr: Tickr) {
        scope.launch {
            when (tickr.type) {
                TickrType.COUNT -> {
                    incrementCountUseCase(tickr.id)
                    refreshCountFor(tickr.id)
                }
                TickrType.TIME -> {
                    val active = _state.value.activeSessions[tickr.id]
                    if (active == null) startSessionUseCase(tickr.id) else stopSessionUseCase(active.id)
                    refreshBaseAndCounts()
                }
            }
        }
    }

    private fun refreshBaseAndCounts() {
        scope.launch {
            val current = _state.value
            val updated = current.currentValues.toMutableMap()
            val updatedBase = current.baseDurations.toMutableMap()
            current.tickrs.forEach { t ->
                when (t.type) {
                    TickrType.COUNT -> {
                        val count = try { tickrRepository.getCurrentCountForPeriod(t.id) } catch (_: Exception) { 0 }
                        updated[t.id] = count.toLong()
                    }
                    TickrType.TIME -> {
                        val bounds = t.period.calculateBoundaries()
                        val sessions = try { sessionRepository.getSessionsForPeriod(t.id, bounds.first, bounds.second) } catch (_: Exception) { emptyList() }
                        var base = 0L
                        sessions.forEach { s ->
                            val ended = s.endedAt
                            if (ended != null) base += (ended - s.startedAt)
                        }
                        updatedBase[t.id] = base
                        val active = current.activeSessions[t.id]
                        val now = System.currentTimeMillis()
                        val running = if (active != null) now - active.startedAt else 0L
                        updated[t.id] = base + running
                    }
                }
            }
            _state.update { it.copy(currentValues = updated, baseDurations = updatedBase) }
        }
    }

    private fun updateTimerValues() {
        val current = _state.value
        if (current.tickrs.isEmpty()) return
        val updated = current.currentValues.toMutableMap()
        val now = System.currentTimeMillis()
        current.tickrs.forEach { t ->
            if (t.type == TickrType.TIME) {
                val base = current.baseDurations[t.id] ?: 0L
                val active = current.activeSessions[t.id]
                val value = if (active != null) base + (now - active.startedAt) else base
                updated[t.id] = value
            }
        }
        _state.update { it.copy(currentValues = updated) }
    }

    private fun refreshCountFor(tickrId: String) {
        scope.launch {
            val count = try { tickrRepository.getCurrentCountForPeriod(tickrId) } catch (_: Exception) { 0 }
            _state.update { it.copy(currentValues = it.currentValues.toMutableMap().also { m -> m[tickrId] = count.toLong() }) }
        }
    }
}
