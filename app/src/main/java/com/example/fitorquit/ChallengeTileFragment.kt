package com.example.fitorquit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.fitorquit.databinding.ChallengeTileBinding

class ChallengeTileFragment() : Fragment() {
    private lateinit var sharedVM: SharedViewModel
    private lateinit var viewModel: ChallengeTileViewModel
    private var _binding: ChallengeTileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ChallengeTileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ChallengeTileViewModel::class.java)
        sharedVM = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        sharedVM.getCurrentPic()
        val challenge = sharedVM.challenge
        sharedVM.uri.observe(viewLifecycleOwner) {
            Glide.with(requireContext())
                .load(it)
                .into(binding.imgChallengePic as ImageView)
        }
        sharedVM.profilePic.observe(viewLifecycleOwner) {
            Glide.with(requireContext())
                .load(it)
                .into(binding.imgCreatorProfile)
            println(it)
        }

        sharedVM.type.observe(viewLifecycleOwner) {
            binding.imgChallengeType.setImageResource(sharedVM.getTypeLogo(it))
            binding.layoutChallengeType.background = requireContext().getDrawable(sharedVM.getColor(it))
        }

        sharedVM.participants.observe(viewLifecycleOwner) {
            binding.txtChallengeParticipantNo.text = "${it.size}"
        }

        sharedVM.currentUser.observe(viewLifecycleOwner) {
            binding.txtChallengeCreatedBy.text = "Created by ${it.username}"
        }

        binding.apply {
            txtChallengeTitle.text = challenge.title ?: "Add a title"
            txtChallengeDesc.text = challenge.desc ?: "No description"
            txtChallengePeriod.text = "${challenge.startDate ?: "Start"} - ${challenge.endDate ?: "End"}"
        }
    }

}