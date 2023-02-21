package com.example.utilitybill.model

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.utilitybill.R
import com.example.utilitybill.viewmodel.MainViewModel
import com.example.utilitybill.databinding.FragmentAddServiceBinding
import com.google.android.material.textfield.TextInputLayout

class AddServiceFragment : Fragment() {
    private var _binding: FragmentAddServiceBinding? = null
    private val binding: FragmentAddServiceBinding
        get() = _binding ?: throw RuntimeException("FragmentSaveServiceBinding == null")

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddServiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            buttonSaveService.setOnClickListener {
                saveService()
            }
            editTextNameService.doOnTextChanged { text, _, _, _ ->
                if (!text.isNullOrEmpty()) {
                    setErrorTextField(false, tilNameService)
                }
            }
            editTextServiceTariff.doOnTextChanged { text, _, _, _ ->
                if (!text.isNullOrEmpty()) {
                    setErrorTextField(false, tilServiceTariff)
                }
            }
        }
        setAdapterToMeterUnit()
        checkMeterAvailability()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setAdapterToMeterUnit() {
        val meterUnitList = resources.getStringArray(R.array.meter_unit_array)

        val unitAdapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            R.id.textViewUnitName,
            meterUnitList
        )
        binding.autoCompleteMeterUnit.setAdapter(unitAdapter)
    }

    private fun checkMeterAvailability() {
        binding.apply {
            checkBoxMeterAvailability.setOnClickListener {
                val isMeterAvailable = checkBoxMeterAvailability.isChecked
                if (isMeterAvailable) {
                    tilPreviousServiceValue.visibility = View.VISIBLE
                    autoCompleteMeterUnit.setText(R.string.cubic_meters)
                } else {
                    tilPreviousServiceValue.visibility = View.GONE
                    editTextPreviousServiceValue.setText(R.string.zero)
                    autoCompleteMeterUnit.setText("")
                }
            }
        }
    }

    private fun saveService() {
        binding.apply {
            val name = editTextNameService.text.toString().trim()
            val tariff = editTextServiceTariff.text.toString().trim()
            var previousValue = editTextPreviousServiceValue.text.toString()
                .trim()
                .trimStart('0')
            if (previousValue.isBlank()) {
                previousValue = resources.getString(R.string.zero)
            }
            val meterUnit = autoCompleteMeterUnit.text.toString()

            checkFieldIsNotBlank(name, tariff)
            if (name.isNotBlank() && tariff.isNotBlank()) {
                viewModel.addService(name, tariff.toDouble(), previousValue.toInt(), meterUnit)
                goToMainFragment()
            }
        }
    }

    private fun checkFieldIsNotBlank(name: String, tariff: String) {
        binding.apply {
            if (name.isBlank()) {
                setErrorTextField(true, tilNameService)
            }
            if (tariff.isBlank()) {
                setErrorTextField(true, tilServiceTariff)
            }
        }
    }

    private fun setErrorTextField(
        error: Boolean,
        inputLayout: TextInputLayout
    ) {
        if (error) {
            inputLayout.isErrorEnabled = true
            inputLayout.error = resources.getString(R.string.try_again)
        } else {
            inputLayout.isErrorEnabled = false
        }

    }

    private fun goToMainFragment() {
        findNavController().navigate(
            AddServiceFragmentDirections.actionSaveServiceFragmentToMainFragment()
        )
    }

}