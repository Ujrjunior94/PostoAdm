package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FuelTank::class,
        Employee::class,
        ShiftSchedule::class,
        Appointment::class,
        DailyReport::class,
        Nozzle::class,
        Calibration::class,
        FuelConformityRecord::class,
        AuditLogEntry::class,
        UserAccount::class,
        FuelDelivery::class
    ],
    version = 6,
    exportSchema = false
)
abstract class PostoDatabase : RoomDatabase() {

    abstract fun postoDao(): PostoDao

    companion object {
        @Volatile
        private var INSTANCE: PostoDatabase? = null

        fun getDatabase(context: Context): PostoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PostoDatabase::class.java,
                    "posto_admin_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
