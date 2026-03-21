package org.example.bails.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import org.example.bails.data.entities.MatchEntity

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object BailsDatabaseConstructor : RoomDatabaseConstructor<BailsDatabase> {
    override fun initialize(): BailsDatabase
}

@Database(
    entities = [MatchEntity::class],
    version = 2,
    exportSchema = true
)
@ConstructedBy(BailsDatabaseConstructor::class)
abstract class BailsDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
}
