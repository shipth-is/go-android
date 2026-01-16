package com.shipthis.go.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shipthis.go.data.model.GoBuild
import kotlinx.coroutines.flow.Flow

@Dao
interface GoBuildDao {
    
    /**
     * Get all builds ordered by creation date (newest first)
     */
    @Query("SELECT * FROM go_builds ORDER BY createdAt DESC")
    fun getAllGoBuilds(): Flow<List<GoBuild>>
    
    /**
     * Get a build by ID
     */
    @Query("SELECT * FROM go_builds WHERE id = :id")
    suspend fun getGoBuildById(id: String): GoBuild?
    
    /**
     * Insert a build, ignoring if it already exists (based on primary key)
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGoBuild(goBuild: GoBuild)
    
    /**
     * Delete a build
     */
    @Delete
    suspend fun deleteGoBuild(goBuild: GoBuild)
}

