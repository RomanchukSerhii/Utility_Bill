package com.example.utilitybill.model

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.utilitybill.database.Bill
import com.example.utilitybill.databinding.BillItemBinding

class BillAdapter(
    private val onListItemClicked: (billId: Int) -> Unit,
    private val onDeleteIconClicked: (deleteIconView: ImageView, billId: Int) -> Unit
): ListAdapter<Bill, BillAdapter.BillItemViewHolder>(DiffCallback){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillItemViewHolder {
        val binding = BillItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BillItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BillItemViewHolder, position: Int) {
        val currentBill = getItem(position)
        holder.bind(currentBill, onDeleteIconClicked)
        holder.itemView. setOnClickListener { onListItemClicked(currentBill.id) }
    }

    class BillItemViewHolder(
        private val binding: BillItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            bill: Bill,
            onDeleteIconClicked: (deleteIconView: ImageView, billId: Int) -> Unit
        ) {
            binding.apply {
                textViewBillMonth.text = bill.month
                val deleteIconView = imageViewDeleteBill
                imageViewDeleteBill.setOnClickListener {
                    onDeleteIconClicked(deleteIconView, bill.id)
                }
            }
        }
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Bill>() {
            override fun areItemsTheSame(oldItem: Bill, newItem: Bill): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Bill, newItem: Bill): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}