package com.example.utilitybill.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.utilitybill.R
import com.example.utilitybill.database.model.Bill
import com.example.utilitybill.databinding.FragmentBillListBinding
import com.example.utilitybill.view.adapters.BillAdapter
import com.example.utilitybill.view.adapters.BillDetailAdapter
import com.example.utilitybill.viewmodel.BillViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.properties.Delegates.notNull

class BillListFragment : Fragment() {

    private var _binding: FragmentBillListBinding? = null
    private val binding: FragmentBillListBinding
        get() = _binding ?: throw RuntimeException("FragmentBillListBinding == null")

    private lateinit var billAdapter: BillAdapter
    private lateinit var billDetailAdapter: BillDetailAdapter
    private val billViewModel: BillViewModel by activityViewModels()
    private var billId: Int by notNull()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            billId = it.getInt(BILL_ID_KEY)
        }
        if (billId < 0) {
            binding.textViewTotal.visibility = View.GONE
            billAdapter = BillAdapter(
                { billId -> onListItemClicked(billId) },
                { billId -> onDeleteItemClicked(billId) }
            )
            billViewModel.getBills().observe(viewLifecycleOwner) { bills ->
                billAdapter.submitList(bills)
            }
            binding.recyclerViewBill.adapter = billAdapter
        } else {
            (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.bill_detail)
            billDetailAdapter = BillDetailAdapter()
            billViewModel.getBill(billId).observe(viewLifecycleOwner) { bill ->
                billDetailAdapter.submitList(bill.services)
                binding.textViewTotal.visibility = View.VISIBLE
                val totalValues = getTotalValues(bill)
                binding.textViewTotal.text = getString(R.string.bill_detail_total, totalValues)
            }
            binding.recyclerViewBill.adapter = billDetailAdapter
        }
    }

    private fun getTotalValues(bill: Bill): Int {
        val services = bill.services
        var total = 0
        services.forEach { service ->
            val serviceTotal = if (service.isHasMeter) {
                val differentValues = service.currentValue - service.previousValue
                (differentValues * service.tariff).toInt()
            } else service.tariff.toInt()
            total += serviceTotal
        }
        return total
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onListItemClicked(billId: Int) {
        findNavController().navigate(
            BillListFragmentDirections.actionBillListFragmentToResultFragment(billId)
        )
    }

    private fun onDeleteItemClicked(billId: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.alert_title_delete_service))
            .setMessage(getString(R.string.alert_message_delete_bill))
            .setCancelable(true)
            .setNegativeButton(getString(R.string.alert_message_cancel)) { _, _ -> }
            .setPositiveButton(getString(R.string.alert_message_accept)) { _, _ ->
                billViewModel.removeBill(billId)
            }
            .show()
    }

    companion object {
        private const val BILL_ID_KEY = "bill_id"
    }
}