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

class BillListFragment : Fragment() {

    private var _binding: FragmentBillListBinding? = null
    private val binding: FragmentBillListBinding
        get() = _binding ?: throw RuntimeException("FragmentBillListBinding == null")

    private lateinit var billAdapter: BillAdapter
    private val billViewModel: BillViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        billAdapter = BillAdapter(
            { billId -> onListItemClicked(billId) },
            { deleteImageView, billId -> onDeleteItemClicked(deleteImageView, billId) }
        )
        billViewModel.getBills().observe(viewLifecycleOwner) { bills ->
            Log.d("BillListFragment", bills.joinToString())
            billAdapter.submitList(bills)
        }
        binding.recyclerViewBill.adapter = billAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onListItemClicked(billId: Int) {
        findNavController().navigate(
            BillListFragmentDirections.actionBillListFragmentToBillFragment(billId)
        )
    }

    private fun onDeleteItemClicked(deleteIconView: ImageView, billId: Int) {
        deleteIconView.setOnClickListener {
            billViewModel.removeBill(billId)
        }
    }
}