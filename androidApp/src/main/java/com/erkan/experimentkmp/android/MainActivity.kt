package com.erkan.experimentkmp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.erkan.experimentkmp.android.dashboard.ExpenseDashboardScreen
import com.erkan.experimentkmp.presentation.dashboard.ExpenseDashboardStateHolder
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val dashboardStateHolder: ExpenseDashboardStateHolder by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    ExpenseDashboardRoute(dashboardStateHolder)
                }
            }
        }
    }
}

@Composable
private fun ExpenseDashboardRoute(
    stateHolder: ExpenseDashboardStateHolder,
) {
    val state by stateHolder.state.collectAsState()

    LaunchedEffect(stateHolder) {
        stateHolder.load()
    }

    ExpenseDashboardScreen(
        state = state,
        onPeriodSelected = stateHolder::selectPeriod,
        onSaveEntry = stateHolder::saveEntry,
        onDeleteEntry = stateHolder::deleteEntry,
    )
}
