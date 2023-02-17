package com.example.utilitybill

data class Service(
    val id: Int,
    val name: String,
    val tariff: Double,
    val previousValue: Int
)