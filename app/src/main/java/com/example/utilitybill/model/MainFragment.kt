package com.example.utilitybill.model

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.utilitybill.viewmodel.MainViewModel
import com.example.utilitybill.R
import com.example.utilitybill.database.Service
import com.example.utilitybill.databinding.FragmentMainBinding
import com.example.utilitybill.databinding.ServiceItemBinding
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

const val APP_PREFERENCES = "APP_PREFERENCES"
const val PREF_CARD_NUMBER_VALUE = "PREF_CARD_NUMBER_VALUE"

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding ?: throw RuntimeException("FragmentMainBinding == null")

    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var preferences: SharedPreferences
    private val viewModel: MainViewModel by activityViewModels()

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

        serviceAdapter = ServiceAdapter(
            { viewType, service -> onListItemClicked(viewType, service) },
            { viewType, serviceId, isServiceUsed ->
                onEditServiceIconClicked(viewType, serviceId, isServiceUsed)
            },
            { editTextPreviousValue, editTextCurrentValue ->
                currentValueErrorListener(editTextPreviousValue, editTextCurrentValue)
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
        return dateFormat.format(date).replaceFirstChar { it.uppercase() }
    }

    private fun observeViewModels() {
        viewModel.getServices().observe(viewLifecycleOwner) { services ->
            Log.d("MainFragment", services.joinToString())
            serviceAdapter.submitList(services)
        }
    }

    private fun goToSaveService() {
        saveCurrentMeterValues()
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

    private fun saveMeterValue(): Boolean {
        val recyclerView = binding.recyclerView
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            if (child is MaterialCardView) {
                if (!saveMeterValue(child)) return false
            }
        }
        return true
    }

    private fun saveMeterValue(serviceItemView: View): Boolean {
        val serviceItemBinding = ServiceItemBinding.bind(serviceItemView)
        serviceItemBinding.apply {
            val serviceId = textViewServiceId.text.toString().toInt()
            val previousValue = editTextPreviousValue.text.toString().trimZero().toInt()
            val currentValue = editTextCurrentValue.text.toString().trimZero().toInt()
            return if (currentValue - previousValue < 0) {
                editTextCurrentValue.error = getString(R.string.current_value_error)
                editTextCurrentValue.requestFocus()
                editTextCurrentValue.setSelection(editTextCurrentValue.text.length)
                false
            } else {
                viewModel.updateMeterValue(serviceId, previousValue, currentValue)
                true
            }
        }
    }

    private fun saveCurrentMeterValues() {
        val recyclerView = binding.recyclerView
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            if (child is MaterialCardView) {
                val serviceItemBinding = ServiceItemBinding.bind(child)
                serviceItemBinding.apply {
                    val serviceId = textViewServiceId.text.toString().toInt()
                    val currentValue = editTextCurrentValue.text.toString().trimZero().toInt()
                    viewModel.updateCurrentValue(serviceId, currentValue)
                }
            }
        }
    }

    private fun onListItemClicked(view: View, service: Service) {
        viewModel.updateServices(serviceAdapter.currentList)
        saveMeterValue(view)
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
        saveCurrentMeterValues()
        val editTextCurrentValue = view.findViewById<EditText>(R.id.editTextCurrentValue)
        val currentValue = editTextCurrentValue.text.toString().toInt()

        findNavController().navigate(
            MainFragmentDirections
                .actionMainFragmentToSaveServiceFragment(serviceId, isServiceUsed, currentValue)
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

    companion object {
        private const val CARD_NUMBER_LENGTH = 19
    }
}