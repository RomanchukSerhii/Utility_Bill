package com.example.utilitybill

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.utilitybill.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var serviceAdapter: ServiceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        serviceAdapter = ServiceAdapter()
        binding.recyclerView.adapter = serviceAdapter
    }
}