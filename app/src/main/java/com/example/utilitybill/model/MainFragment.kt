package com.example.utilitybill.model

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.utilitybill.viewmodel.MainViewModel
import com.example.utilitybill.R
import com.example.utilitybill.database.Service
import com.example.utilitybill.databinding.FragmentMainBinding
import com.google.android.material.textfield.TextInputLayout


class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding ?: throw RuntimeException("FragmentMainBinding == null")
    private lateinit var serviceAdapter: ServiceAdapter
    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var servicesList: List<Service>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        serviceAdapter = ServiceAdapter(
            { viewType, service -> onListItemClicked(viewType, service) },
            { previousValue, serviceId, isServiceUsed ->
                onEditServiceIconClicked(previousValue, serviceId, isServiceUsed)
            },
            { editTextPreviousValue, editTextCurrentValue ->
                currentValueErrorListener(editTextPreviousValue, editTextCurrentValue)
            }
        )
        observeViewModels()
        binding.recyclerView.adapter = serviceAdapter
        binding.buttonAddService.setOnClickListener { goToSaveService() }
        binding.buttonCreateBill.setOnClickListener { goToResult(servicesList) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun goToSaveService() {
        findNavController().navigate(
            MainFragmentDirections.actionMainFragmentToSaveServiceFragment()
        )
    }

    private fun observeViewModels() {
        viewModel.getServices().observe(viewLifecycleOwner) { services ->
            servicesList = services
            serviceAdapter.submitList(services)
        }
    }

    private fun goToResult(services: List<Service>) {
        val stringBuilder = StringBuilder()
        services.forEach { service ->
            stringBuilder.append("${service.name} - ${service.isUsed}\n")
        }
        Toast.makeText(requireActivity(), stringBuilder, Toast.LENGTH_SHORT).show()
    }

    private fun onListItemClicked(view: View, service: Service) {
        val checkableLayout = view.findViewById<CheckableLayout>(R.id.checkable_layout)
        if (checkableLayout != null) {
            checkableLayout.toggle()
            val isUsed = checkableLayout.isChecked
            viewModel.updateUsedStatus(service.id, isUsed)
        }
    }

    private fun onEditServiceIconClicked(
        previousValue: Int,
        serviceId: Int,
        isServiceUsed: Boolean
    ) {
        viewModel.updateMeterValue(serviceId, previousValue)

        findNavController().navigate(
            MainFragmentDirections.actionMainFragmentToSaveServiceFragment(serviceId, isServiceUsed)
        )
    }

    private fun currentValueErrorListener(
        editTextPreviousValue: EditText,
        editTextCurrentValue: EditText
    ) {
        var currentValue = editTextCurrentValue.text.toString().trimZero().toInt()
        var previousValue = editTextPreviousValue.text.toString().trimZero().toInt()
        editTextCurrentValue.doOnTextChanged { text, _, _, _ ->
            currentValue = text.toString().trimZero().toInt()
            val isCurrentValueValid = currentValue >= previousValue
            setCurrentValueError(isCurrentValueValid, editTextCurrentValue)
        }
        editTextPreviousValue.doOnTextChanged { text, _, _, _ ->
            previousValue = text.toString().trimZero().toInt()
            val isCurrentValueValid = currentValue >= previousValue
            setCurrentValueError(isCurrentValueValid, editTextCurrentValue)
        }
    }

    private fun setCurrentValueError(
        isCurrentValueValid: Boolean,
        editTextCurrentValue: EditText
    ) {
        if (isCurrentValueValid) {
            editTextCurrentValue.error = null
        } else {
            editTextCurrentValue.error = resources.getString(R.string.current_value_error)
        }

    }
}