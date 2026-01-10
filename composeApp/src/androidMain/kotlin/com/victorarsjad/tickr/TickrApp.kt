package com.victorarsjad.tickr

import android.app.Application
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.victorarsjad.tickr.db.TickrDatabase
import com.victorarsjad.tickr.data.sql.SqlDelightSessionRepository
import com.victorarsjad.tickr.data.sql.SqlDelightTickrRepository
import com.victorarsjad.tickr.domain.repository.SessionRepository
import com.victorarsjad.tickr.domain.repository.TickrRepository
import com.victorarsjad.tickr.domain.usecase.CreateTickrUseCase
import com.victorarsjad.tickr.domain.usecase.GetReportDataUseCase
import com.victorarsjad.tickr.domain.usecase.IncrementCountUseCase
import com.victorarsjad.tickr.domain.usecase.StartSessionUseCase
import com.victorarsjad.tickr.domain.usecase.StopSessionUseCase
import com.victorarsjad.tickr.ui.FeedViewModel
import com.victorarsjad.tickr.ui.ReportViewModel
import com.victorarsjad.tickr.ui.SettingsViewModel
import com.victorarsjad.tickr.util.UUIDGenerator
import com.victorarsjad.tickr.domain.repository.SettingsRepository
import com.victorarsjad.tickr.settings.AndroidSettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class TickrApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val tickrModule = module {
            single { UUIDGenerator() }
            single<SqlDriver> { AndroidSqliteDriver(TickrDatabase.Schema, androidContext(), "tickr.db") }
            single { TickrDatabase(get()) }
            single<TickrRepository> { SqlDelightTickrRepository(get(), get()) }
            single<SessionRepository> { SqlDelightSessionRepository(get()) }
            single { CreateTickrUseCase(get(), get()) }
            single { IncrementCountUseCase(get()) }
            single { StartSessionUseCase(get(), get()) }
            single { StopSessionUseCase(get()) }
            single { GetReportDataUseCase(get(), get()) }
            single<SettingsRepository> { AndroidSettingsRepository(androidContext()) }
            factory { FeedViewModel(get(), get(), get(), get(), get(), get()) }
            factory { ReportViewModel(get()) }
            factory { SettingsViewModel(get()) }
        }
        startKoin {
            androidContext(this@TickrApp)
            modules(tickrModule)
        }
    }
}
