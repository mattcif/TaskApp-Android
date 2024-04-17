package com.example.taskapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.taskapp.R
import com.example.taskapp.databinding.FragmentFormTaskBinding
import com.example.taskapp.util.initToolbar

class FormTaskFragment : Fragment() {

    private var _binding: FragmentFormTaskBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormTaskBinding.inflate(inflater, container, false)
        return binding.root
        initToolbar(binding.toolbar)

        initListener()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar)

        initListener()
    }


    private fun initListener() {

        binding.btnSave.setOnClickListener{
            validateData()
        }


    }

    private fun validateData() {
        val description = binding.edtDescription.text.toString().trim()

        if(description.isNotEmpty()){
            Toast.makeText(requireContext(), "Tudo Certo", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Preencha uma descrição", Toast.LENGTH_SHORT).show()

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}