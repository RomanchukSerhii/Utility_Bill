package com.example.utilitybill.view.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.utilitybill.R
import com.example.utilitybill.database.model.Service
import com.example.utilitybill.databinding.ServiceWithMeterBinding
import com.example.utilitybill.databinding.ServiceWithoutMeterBinding

class BillDetailAdapter() : ListAdapter<Service, RecyclerView.ViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == SERVICE_WITH_METER) {
            val binding = ServiceWithMeterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ServiceWithMeterItemViewHolder(binding)
        } else {
            val binding = ServiceWithoutMeterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ServiceWithoutMeterItemViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentService = getItem(position)
        if (getItemViewType(position) == SERVICE_WITH_METER) {
            (holder as ServiceWithMeterItemViewHolder).bind(currentService, holder.itemView.context)
        } else {
            (holder as ServiceWithoutMeterItemViewHolder).bind(currentService, holder.itemView.context)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val service = getItem(position)
        return if (service.isHasMeter) SERVICE_WITH_METER else SERVICE_WITHOUT_METER
    }

    class ServiceWithMeterItemViewHolder(
        private val binding: ServiceWithMeterBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(service: Service, context: Context) {
            binding.apply {
                val consumed = service.currentValue - service.previousValue
                val sum = consumed * service.tariff
                textViewServiceName.text = service.name
                textViewTariffValue.text = context.getString(
                    R.string.tariff_value, service.tariff
                )
                textViewCurrentValue.text = context.getString(
                    R.string.current_value_detail, service.currentValue, service.unit
                )
                textViewPreviousValue.text = context.getString(
                    R.string.previous_value_detail, service.previousValue, service.unit
                )
                textViewConsumedValue.text = context.getString(
                    R.string.consumed_value, consumed, service.unit
                )
                textViewSumValue.text = context.getString(
                    R.string.sum_value, sum
                )
            }
        }
    }

    class ServiceWithoutMeterItemViewHolder(
        private val binding: ServiceWithoutMeterBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(service: Service, context: Context) {
            binding.apply {
                textViewServiceName.text = service.name
                textViewSumValue.text = context.getString(
                    R.string.sum_value, service.tariff
                )
            }
        }
    }

    companion object {
        private const val SERVICE_WITH_METER = 0
        private const val SERVICE_WITHOUT_METER = 1
        val DiffCallback = object : DiffUtil.ItemCallback<Service>(){
            override fun areItemsTheSame(oldItem: Service, newItem: Service): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Service, newItem: Service): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }
}