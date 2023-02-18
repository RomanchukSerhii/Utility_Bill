package com.example.utilitybill

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.utilitybill.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        serviceAdapter = ServiceAdapter()
        observeViewModels()
        binding.recyclerView.adapter = serviceAdapter
    }

    private fun observeViewModels() {
        viewModel.getServices().observe(this) {
            serviceAdapter.submitList(it)
        }
    }
}