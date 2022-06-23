package com.example.fitorquit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitorquit.databinding.ParticipantsFragmentBinding

class ParticipantsFragment() : Fragment() {
    private lateinit var sharedVM: SharedViewModel
    private lateinit var viewModel: ParticipantsViewModel
    private var _binding: ParticipantsFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var participantsAdapter: ParticipantsAdapter
    private lateinit var usernameAdapter: ArrayAdapter<String>
    private var selectedUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ParticipantsFragmentBinding.inflate(layoutInflater, container, false)

        setParticipantsAdapter(listOf())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ParticipantsViewModel::class.java]
        sharedVM = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        sharedVM.color.observe(viewLifecycleOwner) {
            binding.btnAddParticipant.setBackgroundColor(it)
        }
        sharedVM.allUsers.observe(viewLifecycleOwner) {
            println(it)
            val usernames = it
                .filter { user -> (sharedVM.participants.value?.contains(user) == false) }
                .sortedBy { user -> user.username }
            println(usernames)
            setACAdapter(usernames)
        }
        sharedVM.participants.observe(viewLifecycleOwner) {
            participantsAdapter.updateParticipants(it)
            sharedVM.challenge.participants = it.map { user -> user.uid }
            println(sharedVM.challenge.participants)
        }
        binding.btnAddParticipant.setOnClickListener {
            selectedUser?.let { user ->
                sharedVM.addParticipant(user)
                selectedUser = null
                binding.acParticipant.text.clear()
            }
        }
        binding.spinnerReferee.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p0 != null) {
                    sharedVM.challenge.refereeing = p0.getItemAtPosition(p2) as String
                }
                println(sharedVM.challenge.refereeing)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
    }

    private fun setParticipantsAdapter(participants: List<User>) {
        participantsAdapter = ParticipantsAdapter(participants).apply {
            onCancelClick = { user ->
                val currentUsers = sharedVM.participants.value?.toMutableList()
                currentUsers?.remove(user)
                sharedVM.participants.value = currentUsers
            }
            onAdminCheck = { user, checked ->
               if (checked) {
                   if (!sharedVM.admins.contains(user)) {
                       sharedVM.admins.add(user)
                   }
               } else {
                   if (sharedVM.admins.contains(user)) {
                       sharedVM.admins.remove(user)
                   }
               }
                sharedVM.challenge.admins = sharedVM.admins.map { it.uid }
                println(sharedVM.challenge.admins)
            }
        }
        binding.rvParticipants.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = participantsAdapter
        }
    }

    private fun setACAdapter(users: List<User>) {
        usernameAdapter = ArrayAdapter(requireContext(), R.layout.ac_row, R.id.txt_acUsername, users.map { it.username })
        binding.acParticipant.setAdapter(usernameAdapter)
        binding.acParticipant.setOnItemClickListener { _, _, i, _ ->
            val user = users[i]
            selectedUser = user
            println(user)
        }
    }

}