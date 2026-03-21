package org.example.bails.data

import androidx.room.RoomDatabase

expect fun getDatabaseBuilder(): RoomDatabase.Builder<BailsDatabase>
