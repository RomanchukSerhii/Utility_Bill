package com.example.utilitybill.model.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.utilitybill.R
import com.example.utilitybill.database.Service
import com.example.utilitybill.databinding.ServiceItemBinding

class ServiceAdapter(
    private val onItemClicked: (view: View, service: Service) -> Unit,
    private val onEditServiceClicked: (
        view: View,
        serviceId: Int,
        isServiceUsed: Boolean
    ) -> Unit,
    private val currentValueErrorListener: (
        editTextPreviousValue: EditText,
        editTextCurrentValue: EditText
    ) -> Unit
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
        holder.bind(
            currentService,
            holder.itemView.context,
            onEditServiceClicked,
            currentValueErrorListener
        )
        holder.itemView.setOnClickListener {
            onItemClicked(holder.itemView, currentService)
        }
    }

    class ServiceItemViewHolder(
        private val binding: ServiceItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            service: Service,
            context: Context,
            onEditServiceClicked: (
                view: View,
                serviceId: Int,
                isServiceUsed: Boolean
            ) -> Unit,
            currentValueErrorListener: (
                editTextPreviousValue: EditText,
                editTextCurrentValue: EditText
            ) -> Unit
        ) {
            binding.apply {
                textViewServiceName.text = service.name
                textViewServiceTariff.text = context.getString(
                    R.string.service_tariff, service.tariff.toFloat()
                )
                textViewServiceId.text = service.id.toString()
                checkBoxUsed.isChecked = service.isUsed
                if (service.isHasMeter) {
                    switchValueMeterVisibility(View.VISIBLE)
                    editTextPreviousValue.setText(service.previousValue.toString())
                    editTextCurrentValue.setText(service.currentValue.toString())
                } else {
                    switchValueMeterVisibility(View.GONE)
                }

                imageViewEditService.setOnClickListener {
                    onEditServiceClicked(binding.root, service.id, service.isUsed)
                }
                currentValueErrorListener(editTextPreviousValue, editTextCurrentValue)
            }
        }

        private fun switchValueMeterVisibility(meterVisibility: Int) {
            binding.apply {
                textViewPreviousTitle.visibility = meterVisibility
                textViewCurrentTitle.visibility = meterVisibility
                editTextCurrentValue.visibility = meterVisibility
                editTextPreviousValue.visibility = meterVisibility
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