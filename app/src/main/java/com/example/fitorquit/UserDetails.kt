package com.example.fitorquit

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitorquit.databinding.ActivityUserDetailsBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import java.text.SimpleDateFormat
import java.util.*

class UserDetails : AppCompatActivity() {
    private lateinit var binding: ActivityUserDetailsBinding
    private lateinit var vm: UserDetailsViewModel
    private var cal = Calendar.getInstance()
    private var profileUri: Uri? = null
    private var isTaken: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        vm = ViewModelProvider(this)[UserDetailsViewModel::class.java]
        binding.btnLogout2.setOnClickListener {
            vm.logout()
            startActivity(Intent(this, Start::class.java))
            this.finish()
        }
        binding.imgProfile.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .createIntent { intent ->
                    resultLauncher.launch(intent)
                }
        }
        
        binding.edittxtUsername.addTextChangedListener { text ->
            vm.checkUsername(text.toString().trim())
        }

        vm.isTaken.observe(this) { taken ->
            isTaken = taken
        }

        binding.txtBirthdate.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnSave.setOnClickListener {
            handleSubmit()
        }
    }

    private fun handleSubmit() {
        val usernameInput = binding.edittxtUsername.text
        val country = binding.countryPicker.tvCountryInfo.text.toString()
        val birthdate = binding.txtBirthdate.text.toString()
        val currentUser = vm.currentUser!!

        if (usernameInput.isBlank()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            return
        }

        if (isTaken) {
            Toast.makeText(this, "${usernameInput.toString()} is already taken", Toast.LENGTH_SHORT).show()
            return
        }

        if (country == "Country") {
            Toast.makeText(this, "Please select your country", Toast.LENGTH_SHORT).show()
            return
        }

        if (birthdate == "Select date") {
            Toast.makeText(this, "Please select your birthdate", Toast.LENGTH_SHORT).show()
            return
        }

        val user = User(currentUser.displayName ?: "", currentUser.uid,
            currentUser.email ?: "", usernameInput.toString(), country, birthdate)

        vm.createUser(user)

        profileUri?.let {
            vm.addProfilePic(it)
        }
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val uri: Uri = data?.data!!

                    // Use Uri object instead of File to avoid storage permissions
                    profileUri = uri
                    binding.imgProfile.setImageURI(uri)
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val dateSetListener =
        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val format = "dd.MM.yyyy"
            val sdf = SimpleDateFormat(format, Locale.FRANCE)
            binding.txtBirthdate.text = sdf.format(cal.time)
        }
}

class UserDetailsViewModel: ViewModel() {
    private val TAG = "userDetailsVM"
    private val repo = FirebaseRepo
    val currentUser = repo.currentUser!!
    val isTaken: MutableLiveData<Boolean> = MutableLiveData()

    fun createUser(user: User) {
        repo.createUser(user)
            .addOnSuccessListener {
                Log.d(TAG, "createUser: " + user.email)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "createUser: ", e)
            }
    }

    fun addProfilePic(uri: Uri) {
        val filename = currentUser.uid + ".jpg"
        val path = "images/profile/$filename"
        repo.addPic(uri, path)
            .addOnSuccessListener {
                Log.d(TAG, "addProfilePic: $path")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addProfilePic: ", e)
            }
    }

    fun checkUsername(username: String) {
        repo.checkUsername(username)
            .get()
            .addOnSuccessListener { documents ->
                isTaken.value = !documents.isEmpty
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "checkUsername: ", e)
            }
    }

    fun logout() {
        repo.logout()
    }
}