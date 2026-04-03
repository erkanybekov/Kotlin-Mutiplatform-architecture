package com.erkan.experimentkmp.presentation.logs

import com.erkan.experimentkmp.logging.AppLogger
import com.erkan.experimentkmp.presentation.shared.ObservationHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LogsStateHolder(
    private val appLogger: AppLogger,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _state = MutableStateFlow(LogsUiState(entries = appLogger.entries.value))
    val state: StateFlow<LogsUiState> = _state.asStateFlow()

    val currentState: LogsUiState
        get() = state.value

    init {
        scope.launch {
            appLogger.entries.collectLatest { entries ->
                _state.value = LogsUiState(entries = entries)
            }
        }
    }

    fun clearLogs() {
        appLogger.clear()
    }

    fun watch(block: (LogsUiState) -> Unit): ObservationHandle {
        block(state.value)
        val job: Job = scope.launch {
            state.collectLatest(block)
        }
        return object : ObservationHandle {
            override fun dispose() {
                job.cancel()
            }
        }
    }
}
