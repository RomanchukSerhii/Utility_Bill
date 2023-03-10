package com.example.utilitybill.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "services")
    val services: List<Service>,

    @ColumnInfo(name = "month")
    val month: String,

    @ColumnInfo(name = "bill_result")
    val billResult: String
)