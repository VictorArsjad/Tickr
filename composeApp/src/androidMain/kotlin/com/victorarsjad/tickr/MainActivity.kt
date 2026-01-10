package com.victorarsjad.tickr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.koin.android.ext.android.inject
import com.victorarsjad.tickr.ui.FeedScreen
import com.victorarsjad.tickr.ui.FeedViewModel
import com.victorarsjad.tickr.ui.ReportScreen
import com.victorarsjad.tickr.ui.ReportViewModel
import com.victorarsjad.tickr.domain.model.Period
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val feedVm: FeedViewModel by inject()
        val reportVm: ReportViewModel by inject()
        setContent {
            var route by remember { mutableStateOf<Screen>(Screen.Feed) }
            when (val r = route) {
                is Screen.Feed -> FeedScreen(feedVm, onNavigateToReport = { id ->
                    route = Screen.Report(id)
                    reportVm.loadReport(id, Period.DAY)
                })
                is Screen.Report -> ReportScreen(reportVm, r.tickrId, onBack = { route = Screen.Feed })
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

private sealed class Screen {
    data object Feed : Screen()
    data class Report(val tickrId: String) : Screen()
}