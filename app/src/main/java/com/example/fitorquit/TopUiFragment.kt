package com.example.fitorquit

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.fitorquit.databinding.TopUiFragmentBinding

class TopUiFragment : Fragment() {

    private var _binding: TopUiFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var vm: TopUiViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TopUiFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm = ViewModelProvider(this)[TopUiViewModel::class.java]

        vm.picUri.observe(viewLifecycleOwner) { uri ->
            uri.let {
                Glide.with(this)
                    .load(uri)
                    .placeholder(ColorDrawable(Color.BLACK))
                    .into(binding.imgProfileSM)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.getPic()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class TopUiViewModel : ViewModel() {
    private val TAG = "topUiVM"
    private val repo = FirebaseRepo
    val picUri: MutableLiveData<Uri> = MutableLiveData()

    fun getPic() {
        repo.currentUser?.let { user ->
            val uid = user.uid
            repo.getPic("images/profile/$uid.jpg")
                .downloadUrl
                .addOnSuccessListener { uri ->
                    picUri.value = uri
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "getPic: ", e)
                }
        }

    }
}