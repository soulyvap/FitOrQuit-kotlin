package com.example.fitorquit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.fitorquit.databinding.ActivityStartBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth

class Start : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    private val signInLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()) { res ->
        this.onSignInResult(res)
    }

    private val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build()
    )

    // Create and launch sign-in intent
    private val signInIntent = AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .setTheme(R.style.Theme_FitOrQuit)
        .setIsSmartLockEnabled(false)
        .build()

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            user?.let {
                Log.d("signIn", "onSignInResult: " + user.email)
            }
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            response?.error?.let { error ->
                Log.e("signIn", "onSignInResult: " + error.errorCode )
            } ?: Log.e("signIn", "onSignInResult: cancelled" )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnStart.setOnClickListener { signInLauncher.launch(signInIntent) }
    }
}
