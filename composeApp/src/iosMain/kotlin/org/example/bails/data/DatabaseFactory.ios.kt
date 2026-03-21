package org.example.bails.data

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

actual fun getDatabaseBuilder(): RoomDatabase.Builder<BailsDatabase> {
    val dbFilePath = NSHomeDirectory() + "/Documents/bails.db"
    return Room.databaseBuilder<BailsDatabase>(
        name = dbFilePath
    )
}
