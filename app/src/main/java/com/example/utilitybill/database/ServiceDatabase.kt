package com.example.utilitybill.database

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.room.*

@Database(entities = [Service::class], version = 2, exportSchema = false)
abstract class ServiceDatabase : RoomDatabase() {
    abstract fun serviceDao(): ServiceDao

    companion object {
        private const val DB_NAME = "service.db"

        @Volatile
        private var INSTANCE: ServiceDatabase? = null

        fun getDatabase(application: Application): ServiceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    application,
                    ServiceDatabase::class.java,
                    DB_NAME
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Dao
interface ServiceDao {
    @Query("SELECT * FROM service")
    fun getServices(): LiveData<List<Service>>

    @Query("SELECT * FROM service WHERE id = :id")
    fun getService(id: Int): LiveData<Service>

    @Insert
    suspend fun addService(service: Service)

    @Update
    suspend fun updateService(service: Service)

    @Query("DELETE FROM service WHERE id = :id")
    suspend fun removeService(id: Int)

    @Query("UPDATE service SET is_used = :isUsed WHERE id = :id")
    suspend fun changeUsedStatus(id: Int, isUsed: Boolean)

    @Query("UPDATE service SET is_has_meter = :isHasMeter WHERE id = :id")
    suspend fun changeMeterAvailability(id: Int, isHasMeter: Boolean)

    @Query("UPDATE service SET previous_value = :previousValue, current_value = :currentValue WHERE id = :id")
    suspend fun changeValues(id: Int, previousValue: Int, currentValue: Int)
}