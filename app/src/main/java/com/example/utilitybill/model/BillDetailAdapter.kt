package com.example.utilitybill.model


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.utilitybill.database.Service
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
        TODO("Not yet implemented")
    }

    override fun getItemViewType(position: Int): Int {
        val service = getItem(position)
        return if (service.isHasMeter) SERVICE_WITH_METER else SERVICE_WITHOUT_METER
    }

    class ServiceWithMeterItemViewHolder(
        private val binding: ServiceWithMeterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

    }

    class ServiceWithoutMeterItemViewHolder(
        private val binding: ServiceWithoutMeterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

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