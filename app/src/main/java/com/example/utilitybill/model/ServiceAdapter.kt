package com.example.utilitybill.model

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.utilitybill.R
import com.example.utilitybill.database.Service
import com.example.utilitybill.databinding.ServiceItemBinding

class ServiceAdapter(
    private val onItemClicked: (view: View, service: Service) -> Unit
) : ListAdapter<Service, ServiceAdapter.ServiceItemViewHolder>(DiffCallback) {

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
        holder.itemView.setOnClickListener {
            onItemClicked(holder.itemView, currentService)
        }
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
                checkBoxUsed.isChecked = service.isUsed
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