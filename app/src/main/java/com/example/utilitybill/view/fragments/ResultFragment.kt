package com.example.utilitybill.view.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.utilitybill.R
import com.example.utilitybill.database.model.Bill
import com.example.utilitybill.database.model.Service
import com.example.utilitybill.databinding.FragmentResultBinding
import com.example.utilitybill.view.isInteger
import com.example.utilitybill.viewmodel.BillViewModel
import com.example.utilitybill.viewmodel.MainViewModel
import kotlin.properties.Delegates.notNull


class ResultFragment : Fragment() {
    private var _binding: FragmentResultBinding? = null
    private val binding: FragmentResultBinding
        get() = _binding ?: throw RuntimeException("FragmentResultBinding == false")

    private val viewModel: MainViewModel by activityViewModels()
    private val billViewModel: BillViewModel by activityViewModels()
    private lateinit var preferences: SharedPreferences
    private lateinit var servicesList: List<Service>
    private lateinit var billResult: String
    private var billId: Int by notNull()
    private lateinit var previousBills: List<Bill>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        preferences = requireActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { billId = it.getInt(BILL_ID_KEY) }
        observeViewModel()
        setListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        if (billId < 0) {
            viewModel.getServices().observe(viewLifecycleOwner) { services ->
                servicesList = services.filter { it.isUsed }
                billResult = createBill(services)
                binding.textViewBill.text = billResult
            }
        } else {
            billViewModel.getBill(billId).observe(viewLifecycleOwner) { bill ->
                binding.apply {
                    textViewBill.text = bill.billResult
                    buttonSaveBill.visibility = View.GONE
                    buttonBillDetail.visibility = View.VISIBLE
                }
            }
        }

        billViewModel.getBills().observe(viewLifecycleOwner) {
            previousBills = it
        }
    }

    private fun setListeners() {
        binding.apply {
            buttonSaveBill.setOnClickListener { saveBill(servicesList) }
            imageViewCopy.setOnClickListener { copyToClipboard() }
            buttonBillDetail.setOnClickListener { goToBillDetailFragment() }
        }
    }

    private fun copyToClipboard() {
        val textOfBill = binding.textViewBill.text.toString()
        val clipboardManager = requireActivity()
            .getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("label", textOfBill)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(
            requireContext(), getString(R.string.clipboard_text), Toast.LENGTH_SHORT
        ).show()
    }

    private fun createBill(services: List<Service>): String {
        val stringBuilder = StringBuilder(getString(R.string.bill_title))
        val cardNumber = preferences.getString(
            PREF_CARD_NUMBER_VALUE, getString(R.string.default_card_number)
        )
        var total = 0

        services.filter { it.isUsed }.forEach { service ->
            if (service.isHasMeter) {
                val valueDifference = service.currentValue - service.previousValue
                val servicePrice = (valueDifference * service.tariff).toInt()
                total += servicePrice

                if (service.tariff.isInteger()) {
                    stringBuilder.append(
                        getString(
                            R.string.service_with_meter_int_tariff_value,
                            service.name,
                            valueDifference,
                            service.unit,
                            service.tariff.toInt(),
                            servicePrice
                        )
                    )
                } else {
                    stringBuilder.append(
                        getString(
                            R.string.service_with_meter_double_tariff_value,
                            service.name,
                            valueDifference,
                            service.unit,
                            service.tariff,
                            servicePrice
                        )
                    )
                }
            } else {
                total += service.tariff.toInt()
                stringBuilder.append(
                    getString(R.string.service_without_meter, service.name, service.tariff)
                )
            }
        }
        stringBuilder.append(getString(R.string.service_total, total, cardNumber))

        return stringBuilder.toString()
    }

    private fun saveBill(services: List<Service>) {
        val currentMonth = preferences.getString(
            PREF_MONTH_VALUE, getString(R.string.default_month)
        ) ?: ""

        if (previousBills.isEmpty()) {
            addNewBill(services, currentMonth)
            goToMainFragment()
        } else {
            previousBills.forEach { previousBill ->
                if (previousBill.month == currentMonth) {
                    val currentBill = previousBill.copy(
                        services = services,
                        month = currentMonth,
                        billResult = billResult
                    )
                    billViewModel.updateBill(currentBill)

                    goToMainFragment()
                } else {
                    addNewBill(services, currentMonth)
                    goToMainFragment()
                }
            }
        }
    }

    private fun addNewBill(services: List<Service>, currentMonth: String) {
        val newBill = Bill(
            services = services,
            month = currentMonth,
            billResult = billResult
        )
        billViewModel.addBill(newBill)
    }

    private fun goToMainFragment() {
        val isBillSaved = true
        preferences.edit()
            .putBoolean(IS_BILL_SAVED_VALUE, isBillSaved)
            .apply()

        findNavController().navigate(
            ResultFragmentDirections.actionResultFragmentToMainFragment()
        )
    }

    private fun goToBillDetailFragment() {
        findNavController().navigate(
            ResultFragmentDirections.actionResultFragmentToBillListFragment(billId)
        )
    }

    companion object {
        private const val BILL_ID_KEY = "bill_id"
        private const val IS_BILL_SAVED_VALUE = "IS_BILL_SAVED_VALUE"
    }

}