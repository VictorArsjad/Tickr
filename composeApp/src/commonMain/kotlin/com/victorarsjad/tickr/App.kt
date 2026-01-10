package com.victorarsjad.tickr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.victorarsjad.tickr.di.Dependencies
import com.victorarsjad.tickr.ui.FeedScreen
import com.victorarsjad.tickr.ui.FeedViewModel


@Composable
@Preview
fun App() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
        ) {
            val vm = rememberFeedViewModel()
            FeedScreen(vm)
        }
    }
}

@Composable
private fun rememberFeedViewModel(): FeedViewModel {
    return remember {
        FeedViewModel(
            Dependencies.createTickrUseCase,
            Dependencies.incrementCountUseCase,
            Dependencies.startSessionUseCase,
            Dependencies.stopSessionUseCase,
            Dependencies.tickrRepository,
            Dependencies.sessionRepository
        )
    }
}