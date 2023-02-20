package com.example.utilitybill.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service")
data class Service(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "tariff")
    val tariff: Double,

    @ColumnInfo(name = "previous_value")
    val previousValue: Int,

    @ColumnInfo(name = "current_value")
    val currentValue: Int = 0,

    @ColumnInfo(name = "is_used")
    val isUsed: Boolean = true,

    @ColumnInfo(name = "is_has_meter")
    val isHasMeter: Boolean = true,

    @ColumnInfo(name = "unit")
    val unit: String = ""
)