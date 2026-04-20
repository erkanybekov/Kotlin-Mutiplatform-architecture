package com.erkan.experimentkmp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.erkan.experimentkmp.android.chat.ChatAppScreen
import com.erkan.experimentkmp.presentation.chat.ChatAppStateHolder
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val chatAppStateHolder: ChatAppStateHolder by inject()
    private val chatAppViewModel: ChatAppViewModel by viewModels {
        ChatAppViewModelFactory(chatAppStateHolder)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    ChatAppRoute(chatAppViewModel)
                }
            }
        }
    }
}

@Composable
private fun ChatAppRoute(
    viewModel: ChatAppViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycleAwareness()

    LaunchedEffect(viewModel) {
        viewModel.load()
    }

    ChatAppScreen(
        state = state,
        viewModel = viewModel,
    )
}
