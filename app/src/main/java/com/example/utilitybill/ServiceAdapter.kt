package com.example.utilitybill

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.utilitybill.databinding.ServiceItemBinding

class ServiceAdapter : ListAdapter<Service, ServiceAdapter.ServiceItemViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceItemViewHolder {
        val binding = ServiceItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ServiceItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceItemViewHolder, position: Int) {
        val currentService = getItem(position)
        holder.bind(currentService, holder.itemView.context)
    }

    class ServiceItemViewHolder(
        private val binding: ServiceItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(service: Service, context: Context){
            binding.apply {
                textViewServiceName.text = service.name
                textViewServiceTariff.text = context.getString(
                    R.string.service_tariff,service.tariff.toFloat()
                )
                editTextPreviousValue.setText(service.previousValue.toString())
            }
        }
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Service>() {
            override fun areItemsTheSame(oldItem: Service, newItem: Service): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Service, newItem: Service): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }
}