package com.example.utilitybill.model

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.utilitybill.R
import com.example.utilitybill.database.Service
import com.example.utilitybill.viewmodel.MainViewModel
import com.example.utilitybill.databinding.FragmentSaveServiceBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import java.util.*
import kotlin.properties.Delegates.notNull

class SaveServiceFragment : Fragment() {
    private var _binding: FragmentSaveServiceBinding? = null
    private val binding: FragmentSaveServiceBinding
        get() = _binding ?: throw RuntimeException("FragmentSaveServiceBinding == null")

    private val viewModel: MainViewModel by activityViewModels()
    private var isServiceUsed by notNull<Boolean>()
    private var serviceId by notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            serviceId = it.getInt(SERVICE_ID)
            isServiceUsed = it.getBoolean(IS_SERVICE_USED)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaveServiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (serviceId.isPositive()) bindFields()
        bindListeners()
        setAdapterToMeterUnit()
        checkMeterAvailability()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindFields() {
        (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(
            R.string.edit_service_title
        )
        viewModel.getService(serviceId).observe(viewLifecycleOwner) { service ->
            binding.apply {
                editTextNameService.setText(service.name)
                editTextServiceTariff.setText(service.tariff.toString())
                editTextPreviousServiceValue.setText(service.previousValue.toString())
                buttonDeleteService.visibility = View.VISIBLE
                viewModel.switchMeterCheck(service.isHasMeter)
            }
        }
    }

    private fun bindListeners() {
        binding.apply {
            buttonSaveService.setOnClickListener { saveService() }
            buttonDeleteService.setOnClickListener { deleteService() }

            checkBoxMeterAvailability.setOnClickListener {
                viewModel.switchMeterCheck(checkBoxMeterAvailability.isChecked)
                checkMeterAvailability()
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
            viewModel.isMeterChecked.observe(viewLifecycleOwner) { isMeterChecked ->
                checkBoxMeterAvailability.isChecked = isMeterChecked
                if (isMeterChecked) {
                    tilPreviousServiceValue.visibility = View.VISIBLE
                } else {
                    tilPreviousServiceValue.visibility = View.GONE
                    editTextPreviousServiceValue.setText(R.string.zero)
                }
            }
        }
    }

    private fun saveService() {
        binding.apply {
            val name = editTextNameService.text.toString().trim().replaceFirstChar { it.uppercase() }
            val tariff = editTextServiceTariff.text.toString().trim()
            var previousValue = editTextPreviousServiceValue.text.toString()
                .trim()
                .trimStart('0')
            if (previousValue.isBlank()) {
                previousValue = resources.getString(R.string.zero)
            }
            val isHasMeter = checkBoxMeterAvailability.isChecked
            val meterUnit = autoCompleteMeterUnit.text.toString()

            val isFieldsNotBlank = checkFieldIsNotBlank(name, tariff)
            if (isFieldsNotBlank) {
                if (serviceId.isPositive()) {
                    viewModel.updateService(
                        serviceId,
                        name,
                        tariff.toDouble(),
                        previousValue.toInt(),
                        isServiceUsed,
                        isHasMeter,
                        meterUnit
                    )
                } else {
                    viewModel.addService(
                        name,
                        tariff.toDouble(),
                        previousValue.toInt(),
                        isHasMeter,
                        meterUnit)
                }
                goToMainFragment()
            }
        }
    }

    private fun deleteService() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.alert_title_delete_service))
            .setMessage(getString(R.string.alert_message_delete_service))
            .setCancelable(true)
            .setNegativeButton(getString(R.string.alert_message_cancel)) { _, _ ->

            }
            .setPositiveButton(getString(R.string.alert_message_accept)) { _, _ ->
                viewModel.removeService(serviceId)
                goToMainFragment()
            }
            .show()
    }

    private fun checkFieldIsNotBlank(name: String, tariff: String): Boolean {
        binding.apply {
            if (name.isBlank()) {
                setErrorTextField(true, tilNameService)
            }
            if (tariff.isBlank()) {
                setErrorTextField(true, tilServiceTariff)
            }
        }
        return name.isNotBlank() && tariff.isNotBlank()
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
            SaveServiceFragmentDirections.actionSaveServiceFragmentToMainFragment()
        )
    }

    companion object {
        private const val SERVICE_ID = "service_id"
        private const val IS_SERVICE_USED = "is_service_used"
    }
}