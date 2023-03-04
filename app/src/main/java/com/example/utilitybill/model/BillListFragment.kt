package com.example.utilitybill.model

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.utilitybill.R
import com.example.utilitybill.database.Bill
import com.example.utilitybill.databinding.FragmentBillListBinding
import com.example.utilitybill.viewmodel.BillViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.properties.Delegates.notNull

class BillListFragment : Fragment() {

    private var _binding: FragmentBillListBinding? = null
    private val binding: FragmentBillListBinding
        get() = _binding ?: throw RuntimeException("FragmentBillListBinding == null")

    private lateinit var billAdapter: BillAdapter
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
        billAdapter = if (billId < 0) {
            val billListAdapter = BillAdapter(
                { billId -> onListItemClicked(billId) },
                { billId -> onDeleteItemClicked(billId) }
            )
            billViewModel.getBills().observe(viewLifecycleOwner) { bills ->
                Log.d("BillListFragment", bills.joinToString())
                billListAdapter.submitList(bills)
            }
            billListAdapter
        } else {
            return TODO()
        }
        binding.recyclerViewBill.adapter = billAdapter
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