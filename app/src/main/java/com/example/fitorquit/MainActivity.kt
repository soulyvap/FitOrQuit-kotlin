package com.example.fitorquit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.fitorquit.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var vm: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProvider(this)[MainViewModel::class.java]
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        vm.hasUserDoc.observe(this, Observer { hasDoc ->
            if (hasDoc == false) {
                startActivity(Intent(this, UserDetails::class.java))
                this.finish()
            }
        })

        binding.btnLogout.setOnClickListener {
            vm.logout()
            toStartView()
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_navHostFrag) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)

    }

    override fun onStart() {
        super.onStart()
        if (vm.currentUser == null) {
            toStartView()
        } else {
            vm.checkUserDoc()
        }
    }

    private fun toStartView() {
        startActivity(Intent(this, Start::class.java))
        this.finish()
    }
}

class MainViewModel: ViewModel() {
    private val TAG = "MainVM"
    private val repo = FirebaseRepo
    val currentUser = repo.currentUser
    val hasUserDoc: MutableLiveData<Boolean?> = MutableLiveData()

    fun logout() {
        repo.logout()
    }

    fun checkUserDoc() {
        currentUser?.let {
            repo.checkUserDoc().addSnapshotListener { value, e ->
                if (e != null) {
                    Log.e(TAG, "checkUserDoc: ", e)
                    hasUserDoc.value = null
                }
                value?.let { doc ->
                    hasUserDoc.value = doc.exists()
                }
            }
        }

    }
}