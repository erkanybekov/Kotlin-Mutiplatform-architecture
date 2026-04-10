package com.erkan.experimentkmp.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.erkan.experimentkmp.presentation.dashboard.ExpenseDashboardIntent
import com.erkan.experimentkmp.presentation.dashboard.ExpenseDashboardStateHolder

class ExpenseDashboardViewModel(
    private val stateHolder: ExpenseDashboardStateHolder,
) : ViewModel() {
    val state = stateHolder.state

    fun load() {
        stateHolder.load()
    }

    fun onIntent(intent: ExpenseDashboardIntent) {
        stateHolder.onIntent(intent)
    }
}

class ExpenseDashboardViewModelFactory(
    private val stateHolder: ExpenseDashboardStateHolder,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ExpenseDashboardViewModel::class.java)) {
            "Unsupported ViewModel class: ${modelClass.name}"
        }
        return ExpenseDashboardViewModel(stateHolder) as T
    }
}
