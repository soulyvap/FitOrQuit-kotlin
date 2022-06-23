package com.example.fitorquit

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import com.example.fitorquit.databinding.ActivityCreateChallengeBinding.inflate
import com.example.fitorquit.databinding.NewChallengeFragmentBinding
import com.example.fitorquit.databinding.TopUiFragmentBinding

class NewChallengeFragment : Fragment() {

    private lateinit var viewModel: NewChallengeViewModel
    private var _binding: NewChallengeFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewChallengeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[NewChallengeViewModel::class.java]

        binding.challengeTileSpeed.setOnClickListener {
            val intent = Intent(activity, CreateChallengeActivity::class.java)
            intent.putExtra("type", ChallengeType.SPEED)
            startActivity(intent)
        }
        binding.challengeTileCompletion.setOnClickListener {
            val intent = Intent(activity, CreateChallengeActivity::class.java)
            intent.putExtra("type", ChallengeType.COMPLETION)
            startActivity(intent)
        }
        binding.challengeTileHighest.setOnClickListener {
            val intent = Intent(activity, CreateChallengeActivity::class.java)
            intent.putExtra("type", ChallengeType.HIGHEST)
            startActivity(intent)
        }
    }

}

class NewChallengeViewModel: ViewModel() {

}