package org.example.bails.data

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase

lateinit var appContext: Application

actual fun getDatabaseBuilder(): RoomDatabase.Builder<BailsDatabase> {
    val dbFile = appContext.getDatabasePath("bails.db")
    return Room.databaseBuilder<BailsDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
