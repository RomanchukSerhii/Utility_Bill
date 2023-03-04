package com.example.utilitybill.database

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.room.*

@Database(entities = [Service::class, Bill::class], version = 4, exportSchema = false)
@TypeConverters(ServiceListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): ServiceDao

    companion object {
        private const val DB_NAME = "service.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(application: Application): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    application,
                    AppDatabase::class.java,
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
    @Query("SELECT * FROM service ORDER BY `order`")
    fun getServices(): LiveData<List<Service>>

    @Query("SELECT * FROM service WHERE id = :id")
    fun getService(id: Int): LiveData<Service>

    @Query("SELECT MAX('order') FROM service")
    suspend fun getMaxOrder(): Int?

    @Insert
    suspend fun addService(service: Service)

    @Update
    suspend fun updateService(service: Service)

    @Update
    suspend fun updateServices(services: List<Service>)

    @Query("DELETE FROM service WHERE id = :id")
    suspend fun removeService(id: Int)

    @Query("UPDATE service SET is_used = :isUsed WHERE id = :id")
    suspend fun changeUsedStatus(id: Int, isUsed: Boolean)

    @Query("UPDATE service SET is_has_meter = :isHasMeter WHERE id = :id")
    suspend fun changeMeterAvailability(id: Int, isHasMeter: Boolean)

    @Query("UPDATE service SET previous_value = :previousValue, current_value = :currentValue WHERE id = :id")
    suspend fun changeValues(id: Int, previousValue: Int, currentValue: Int)

    @Query("UPDATE service SET current_value = :currentValue WHERE id = :id")
    suspend fun changeCurrentValue(id: Int, currentValue: Int)

    @Query("SELECT * FROM bills")
    fun getBills(): LiveData<List<Bill>>

    @Query("SELECT * FROM bills WHERE id = :id")
    fun getBill(id: Int): LiveData<Bill>

    @Insert
    suspend fun addBill(bill: Bill)

    @Update
    suspend fun updateBill(bill: Bill)

    @Query("DELETE FROM bills WHERE id = :id")
    suspend fun removeBill(id: Int)
}