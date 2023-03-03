package com.example.utilitybill.model

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.utilitybill.R
import com.example.utilitybill.database.Bill

class BillListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bill_list, container, false)
    }

    private fun onListItemClicked(billId: Int) {
        findNavController().navigate(
            BillListFragmentDirections.actionBillListFragmentToBillFragment(billId)
        )
    }

    private fun onDeleteItemClicked(billId: Int) {

    }
}