package com.example.utilitybill.model

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
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.activityViewModels
import com.example.utilitybill.R
import com.example.utilitybill.database.Service
import com.example.utilitybill.databinding.FragmentResultBinding
import com.example.utilitybill.viewmodel.MainViewModel
import kotlin.math.*


class ResultFragment : Fragment() {
    private var _binding: FragmentResultBinding? = null
    private val binding: FragmentResultBinding
        get() = _binding ?: throw RuntimeException("FragmentResultBinding == false")

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var preferences: SharedPreferences

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
        viewModel.getServices().observe(viewLifecycleOwner) { services ->
            val bill = createBill(services)
            binding.textViewBill.text = bill
        }

        binding.imageViewCopy.setOnClickListener { copyToClipboard() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

        services.forEach { service ->
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

}