package com.shipthis.go.di

import android.content.Context
import androidx.room.Room
import com.shipthis.go.data.local.GoBuildDao
import com.shipthis.go.data.local.GoBuildDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideGoBuildDatabase(
        @ApplicationContext context: Context
    ): GoBuildDatabase {
        return Room.databaseBuilder(
            context,
            GoBuildDatabase::class.java,
            "go_build_database"
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideGoBuildDao(database: GoBuildDatabase): GoBuildDao {
        return database.goBuildDao()
    }
}

