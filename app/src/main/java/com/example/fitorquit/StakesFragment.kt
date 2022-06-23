package com.example.fitorquit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fitorquit.databinding.StakesFragmentBinding

class StakesFragment : Fragment() {
    private lateinit var sharedVM: SharedViewModel
    private var _binding: StakesFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: StakesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = StakesFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[StakesViewModel::class.java]
        sharedVM = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        binding.spinnerStakes.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                p0?.let {
                    sharedVM.challenge.stakesType = it.getItemAtPosition(p2) as String
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        binding.edittextStakesDesc.addTextChangedListener {
            sharedVM.challenge.stakesDesc = it.toString().trim()
        }
    }

}