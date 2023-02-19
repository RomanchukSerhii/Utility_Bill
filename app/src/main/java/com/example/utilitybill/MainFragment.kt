package com.example.utilitybill

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.utilitybill.databinding.FragmentMainBinding
import com.example.utilitybill.databinding.ServiceItemBinding


class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding ?: throw RuntimeException("FragmentMainBinding == null")
    private lateinit var serviceAdapter: ServiceAdapter
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        serviceAdapter = ServiceAdapter { onListItemClicked(it)}
        observeViewModels()
        binding.recyclerView.adapter = serviceAdapter
        binding.buttonAddService.setOnClickListener { goToSaveService() }    }

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
        viewModel.getServices().observe(viewLifecycleOwner) {
            serviceAdapter.submitList(it)
        }
    }

    private fun onListItemClicked(view: View) {
        val checkableLayout = view.findViewById<CheckableLayout>(R.id.checkable_layout)
        checkableLayout?.toggle()
    }

}