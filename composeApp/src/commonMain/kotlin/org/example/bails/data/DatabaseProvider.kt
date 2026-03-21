package org.example.bails.data

import androidx.sqlite.driver.bundled.BundledSQLiteDriver

object DatabaseProvider {
    private var database: BailsDatabase? = null

    fun getDatabase(): BailsDatabase {
        return database ?: getDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
            .also { database = it }
    }
}
