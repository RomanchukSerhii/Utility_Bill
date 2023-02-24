package com.example.utilitybill.model

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.utilitybill.R
import com.example.utilitybill.database.Service
import com.example.utilitybill.databinding.FragmentResultBinding
import com.example.utilitybill.viewmodel.MainViewModel


class ResultFragment : Fragment() {
    private var _binding: FragmentResultBinding? = null
    private val binding: FragmentResultBinding
        get() = _binding ?: throw RuntimeException("FragmentResultBinding == false")

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var cardNumber: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cardNumber ="0000"
        viewModel.getServices().observe(viewLifecycleOwner) { services ->
            val bill = createBill(services)
            binding.textViewBill.text = bill
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createBill(services: List<Service>): String {
        val stringBuilder = StringBuilder(getString(R.string.bill_title))
        var total = 0
        services.forEach { service ->
            if (service.isHasMeter) {
                val valueDifference = service.currentValue - service.previousValue
                val servicePrice = (valueDifference * service.tariff).toInt()
                total += servicePrice
                stringBuilder.append(
                    getString (R.string.service_with_meter,
                    service.name,
                    valueDifference,
                    service.unit,
                    service.tariff,
                    servicePrice
                    )
                )
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