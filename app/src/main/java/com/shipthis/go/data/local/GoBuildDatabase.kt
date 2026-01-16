package com.shipthis.go.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shipthis.go.data.model.GoBuild

@Database(
    entities = [GoBuild::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GoBuildDatabase : RoomDatabase() {
    abstract fun goBuildDao(): GoBuildDao
}

