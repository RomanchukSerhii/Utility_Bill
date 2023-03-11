package com.example.utilitybill.view.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.utilitybill.viewmodel.MainViewModel
import com.example.utilitybill.R
import com.example.utilitybill.database.model.Service
import com.example.utilitybill.databinding.FragmentMainBinding
import com.example.utilitybill.view.adapters.CheckableLayout
import com.example.utilitybill.view.adapters.ItemTouchHelperCallback
import com.example.utilitybill.view.adapters.ServiceAdapter
import com.example.utilitybill.view.trimZero
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates.notNull

const val APP_PREFERENCES = "APP_PREFERENCES"
const val PREF_CARD_NUMBER_VALUE = "PREF_CARD_NUMBER_VALUE"
const val PREF_MONTH_VALUE = "PREF_MONTH_VALUE"

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding ?: throw RuntimeException("FragmentMainBinding == null")

    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var preferences: SharedPreferences
    private var isBillSaved: Boolean by notNull()
    private val viewModel: MainViewModel by activityViewModels()
    private val currentMeterValues = mutableMapOf<Int, Array<Int>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        preferences = requireActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setBackgroundDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.gradient_background)
        )
        (activity as AppCompatActivity).supportActionBar?.hide()

        isBillSaved = preferences.getBoolean(IS_BILL_SAVED_VALUE, false)

        serviceAdapter = ServiceAdapter(
            { viewType, service -> onListItemClicked(viewType, service) },
            { viewType, serviceId, isServiceUsed ->
                onEditServiceIconClicked(viewType, serviceId, isServiceUsed)
            },
            { editTextPreviousValue, editTextCurrentValue ->
                currentValueErrorListener(editTextPreviousValue, editTextCurrentValue)
            },
            { serviceId, previousValue, currentValue ->
                saveMeterValues(serviceId, previousValue, currentValue)
            }
        )

        val itemTouchHelperCallback = ItemTouchHelperCallback(serviceAdapter)
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        observeViewModels()
        binding.recyclerView.adapter = serviceAdapter
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
        setListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as AppCompatActivity).supportActionBar?.show()
    }

    private fun setListeners() {
        val cardNumber = preferences.getString(
            PREF_CARD_NUMBER_VALUE, getString(R.string.default_card_number)
        )
        cardNumber?.let { binding.textViewCardNumber.text = changeCardNumberToSecure(cardNumber) }
        binding.textViewMonthName.text = getMonthName()

        binding.apply {
            buttonAddService.setOnClickListener { goToSaveService() }

            buttonCreateBill.setOnClickListener { goToResult() }

            buttonArchiveBills.setOnClickListener { goToBillList() }

            imageViewEditCard.setOnClickListener {
                val currentCardNumber = preferences.getString(
                    PREF_CARD_NUMBER_VALUE, getString(R.string.default_card_number)
                )
                editTextCardNumber.visibility = View.VISIBLE
                editTextCardNumber.setText(currentCardNumber)
                editTextCardNumber.requestFocus()

                imageViewEditCardDone.visibility = View.VISIBLE
                textViewCardNumber.visibility = View.GONE
                imageViewEditCard.visibility = View.GONE
            }

            imageViewEditCardDone.setOnClickListener {
                val cardEditNumber = editTextCardNumber.text.toString()
                if (cardEditNumber.length == CARD_NUMBER_LENGTH) {
                    saveCurrentNumber(cardEditNumber)
                } else {
                    editTextCardNumber.error = resources.getString(R.string.card_number_value_error)
                }
            }

            editTextCardNumber.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val currentCardNumber = editTextCardNumber.text.toString()
                    if (currentCardNumber.length == CARD_NUMBER_LENGTH) {
                        saveCurrentNumber(currentCardNumber)
                    } else {
                        cardNumber?.let {
                            saveCurrentNumber(cardNumber)
                        }
                    }
                }
            }

            editTextCardNumber.addTextChangedListener(object : TextWatcher {
                private var isFormatting: Boolean = false
                private val spacer = " "

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

                override fun afterTextChanged(s: Editable?) {
                    if (isFormatting) {
                        return
                    }

                    isFormatting = true
                    if (s != null && s.length == CARD_NUMBER_LENGTH + 1) {
                        s.delete(CARD_NUMBER_LENGTH, CARD_NUMBER_LENGTH + 1)
                    }
                    val cleanString = s.toString().replace(spacer, "")
                    val formattedString = StringBuilder()


                    for (i in cleanString.indices) {
                        formattedString.append(cleanString[i])
                        if ((i + 1) % 4 == 0 && i != cleanString.length - 1) {
                            formattedString.append(spacer)
                        }
                    }

                    binding.editTextCardNumber.setText(formattedString)
                    binding.editTextCardNumber.setSelection(formattedString.length)
                    isFormatting = false
                }
            })
        }
    }

    private fun saveCurrentNumber(cardEditNumber: String) {
        preferences.edit()
            .putString(PREF_CARD_NUMBER_VALUE, cardEditNumber)
            .apply()
        binding.apply {
            textViewCardNumber.text = changeCardNumberToSecure(cardEditNumber)
            editTextCardNumber.visibility = View.GONE
            imageViewEditCardDone.visibility = View.GONE
            textViewCardNumber.visibility = View.VISIBLE
            imageViewEditCard.visibility = View.VISIBLE
        }
    }

    private fun changeCardNumberToSecure(cardNumber: String): String {
        return ("${cardNumber.take(4)} **** **** ${cardNumber.takeLast(4)}")
    }

    private fun getMonthName(): String {
        val date = Date()
        val dateFormat = SimpleDateFormat("LLLL", Locale("uk", "UA"))
        val currentMonth = dateFormat.format(date).replaceFirstChar { it.uppercase() }
        preferences.edit()
            .putString(PREF_MONTH_VALUE, currentMonth)
            .apply()
        return currentMonth
    }

    private fun observeViewModels() {
        viewModel.getServices().observe(viewLifecycleOwner) { services ->
            if (isBillSaved) {
                services.forEach { service ->
                    service.previousValue = service.currentValue
                    service.currentValue = 0
                }
                isBillSaved = false
                preferences.edit()
                    .putBoolean(IS_BILL_SAVED_VALUE, isBillSaved)
                    .apply()
                viewModel.updateServices(services)
            }

            serviceAdapter.submitList(services)
            if (currentMeterValues.isEmpty()) {
                services.forEach { service ->
                    val meterValues = arrayOf(service.previousValue, service.currentValue)
                    currentMeterValues[service.id] = meterValues
                }
            }
        }
    }

    private fun goToSaveService() {
        //Save current order in recyclerView list
        viewModel.updateServices(serviceAdapter.currentList)

        updateMeterValues()
        findNavController().navigate(
            MainFragmentDirections.actionMainFragmentToSaveServiceFragment()
        )
    }

    private fun goToResult() {
        viewModel.updateServices(serviceAdapter.currentList)
        if (saveMeterValue()) {
            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToResultFragment()
            )
        }
    }

    private fun goToBillList() {
        viewModel.updateServices(serviceAdapter.currentList)
        updateMeterValues()
        findNavController().navigate(
            MainFragmentDirections.actionMainFragmentToBillListFragment()
        )
    }

    private fun saveMeterValue(): Boolean {
        currentMeterValues.forEach { pair ->
            if (pair.value[0] > pair.value[1]) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.values_error),
                    Toast.LENGTH_SHORT
                ).show()
                return false
            } else {
                viewModel.updateMeterValue(pair.key, pair.value[0], pair.value[1])
            }
        }
        return true
    }

    private fun onListItemClicked(view: View, service: Service) {
        val meterValues = currentMeterValues[service.id]
        viewModel.updateServices(serviceAdapter.currentList)
        if (meterValues != null) {
            val previousValue = meterValues[0]
            val currentValue = meterValues[1]
            viewModel.updateMeterValue(service.id, previousValue, currentValue)
        }

        val checkableLayout = view.findViewById<CheckableLayout>(R.id.checkable_layout)

        if (checkableLayout != null) {
            checkableLayout.toggle()
            val isUsed = checkableLayout.isChecked
            viewModel.updateUsedStatus(service.id, isUsed)
        }
    }

    private fun onEditServiceIconClicked(
        view: View,
        serviceId: Int,
        isServiceUsed: Boolean
    ) {
        viewModel.updateServices(serviceAdapter.currentList)
        updateMeterValues()
        val editTextCurrentValue = view.findViewById<EditText>(R.id.editTextCurrentValue)
        val currentValue = editTextCurrentValue.text.toString().toInt()

        findNavController().navigate(
            MainFragmentDirections
                .actionMainFragmentToSaveServiceFragment(serviceId, isServiceUsed, currentValue)
        )
    }

    private fun saveMeterValues(serviceId: Int, previousValue: Int, currentValue: Int) {
        currentMeterValues[serviceId] = arrayOf(previousValue, currentValue)
    }

    private fun updateMeterValues() {
        currentMeterValues.forEach { pair ->
            viewModel.updateMeterValue(pair.key, pair.value[0], pair.value[1])
        }
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

    companion object {
        private const val CARD_NUMBER_LENGTH = 19
        private const val IS_BILL_SAVED_VALUE = "IS_BILL_SAVED_VALUE"
    }
}