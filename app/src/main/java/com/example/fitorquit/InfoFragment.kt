package com.example.fitorquit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CheckBox
import androidx.activity.result.ActivityResultLauncher
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fitorquit.databinding.InfoFragmentBinding
import com.github.dhaval2404.imagepicker.ImagePicker

class InfoFragment(activityResultLauncher: ActivityResultLauncher<Intent>) : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var sharedVM: SharedViewModel
    private var _binding: InfoFragmentBinding? = null
    private val binding get() = _binding!!
    private val TAG = "infoFragment"
    private val resultLauncher = activityResultLauncher

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = InfoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedVM = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        binding.imgChallenge.setOnClickListener {
            ImagePicker.with(requireActivity())
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .createIntent { intent ->
                    resultLauncher.launch(intent)
                }
        }

        sharedVM.uri.observe(viewLifecycleOwner) { uri ->
            binding.imgChallenge.setImageURI(uri)
        }

        binding.edittxtTitle.addTextChangedListener { text ->
            text?.let {
                if (it.isNotBlank()) {
                    sharedVM.challenge.title = it.toString().trim()
                }
            }
        }

        binding.edittextDesc.addTextChangedListener { text ->
            text?.let {
                if (it.isNotBlank()) {
                    sharedVM.challenge.desc = it.toString().trim()
                }
            }
        }

        binding.spinnerCategory.onItemSelectedListener = this
        binding.checkPrivate.setOnClickListener { onCheckboxClicked(it) }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: " + sharedVM.uri)
    }


    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        sharedVM.challenge.category = p0?.getItemAtPosition(p2).toString()
        Log.d(TAG, "onItemSelected: " + sharedVM.challenge.category)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    private fun onCheckboxClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked
            Log.d(TAG, "onCheckboxClicked: $checked")
            when (view.id) {
                R.id.check_private -> {
                    sharedVM.challenge.private = checked
                }
            }
        }
    }
}
