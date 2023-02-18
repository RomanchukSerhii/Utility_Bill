package com.example.utilitybill

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.room.*

@Database(entities = [Service::class], version = 1, exportSchema = false)
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

    @Insert
    suspend fun addService(service: Service)

    @Query("DELETE FROM service WHERE id = :id")
    suspend fun removeService(id: Int)
}