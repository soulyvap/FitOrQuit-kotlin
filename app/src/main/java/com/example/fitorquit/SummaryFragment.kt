package com.example.fitorquit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fitorquit.databinding.SummaryFragmentBinding

class SummaryFragment() : Fragment() {
    private lateinit var sharedVM: SharedViewModel
    private var _binding: SummaryFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SummaryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SummaryFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(SummaryViewModel::class.java)
        sharedVM = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        val challengeTileFragment = ChallengeTileFragment()
        childFragmentManager.beginTransaction().apply {
            replace(R.id.frag_challengeTile, challengeTileFragment)
            commit()
        }
    }

}