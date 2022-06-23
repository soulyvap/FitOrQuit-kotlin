package com.example.fitorquit

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitorquit.databinding.ActivityCreateChallengeBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import java.io.Serializable

class CreateChallengeActivity : AppCompatActivity() {
    private val TAG = "createChallengeActivity"
    private lateinit var binding: ActivityCreateChallengeBinding
    private lateinit var vm: CreateChallengeViewModel
    private lateinit var sharedVM: SharedViewModel
    private lateinit var infoFrag: InfoFragment
    private lateinit var rulesFrag: RulesFragment
    private lateinit var stakesFrag: StakesFragment
    private lateinit var participantsFrag: ParticipantsFragment
    private lateinit var summaryFrag: SummaryFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateChallengeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        vm = ViewModelProvider(this)[CreateChallengeViewModel::class.java]
        sharedVM = ViewModelProvider(this)[SharedViewModel::class.java]

        val intent = intent
        val type = intent.getSerializableExtra("type") as ChallengeType
        vm.type = type
        sharedVM.type.value = type
        sharedVM.color.value = vm.getColor()

        infoFrag = InfoFragment(imagePickerLauncher)
        rulesFrag = RulesFragment(videoCameraLauncher, videoGalleryLauncher, binding.scroll)
        stakesFrag = StakesFragment()
        participantsFrag = ParticipantsFragment()
        summaryFrag = SummaryFragment()

        setCurrentFragment(R.id.infoFragment)

        binding.botNavChallenge.setOnItemSelectedListener {
            setCurrentFragment(it.itemId)
            true
        }
        binding.txtChallengeType.text = vm.type?.text ?: "Challenge type"
        binding.headerBar.background = vm.getBackground()
        binding.btnNext.setBackgroundColor(vm.getColor())
        binding.btnNext.setOnClickListener {
            val nextItem = binding.botNavChallenge.findViewById<View>(vm.nextStepId)
            nextItem.performClick()
            if (vm.currentFragId == vm.idList.last()) {
                handleSubmit()
            }
        }
        binding.btnBack.setOnClickListener {
            quitAlert()
        }

        KeyboardVisibilityEvent.setEventListener(
            this
        ) { isOpen ->
            binding.botNavChallenge.visibility = if (isOpen) View.GONE else View.VISIBLE
            binding.btnNext.visibility = if (isOpen) View.GONE else View.VISIBLE
            binding.headerBar.visibility = if (isOpen) View.GONE else View.VISIBLE
        }
    }

    private val videoCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri = data?.data!!
                    sharedVM.videoUri.value = fileUri
                }
                else -> {
                    Log.e(TAG, "videoCamera: cancelled")
                }
            }
        }


    private val videoGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri = data?.data!!
                    sharedVM.videoUri.value = fileUri
                }
                else -> {
                    Log.e(TAG, "videoGallery: cancelled")
                }
            }
        }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!
                    sharedVM.uri.value = fileUri
                    sharedVM.challenge.picUri = fileUri
                }
                ImagePicker.RESULT_ERROR -> {
                    Log.e(TAG, "imgPicker: ${ImagePicker.getError(data)}")
                }
                else -> {
                    Log.e(TAG, "imgPicker: cancelled")
                }
            }
        }

    private fun setCurrentFragment(fragId: Int) {
        binding.scroll.scrollTo(0, 0)
        updateInfo(fragId)
        val fragment = when (fragId) {
            R.id.infoFragment -> infoFrag
            R.id.rulesFragment -> rulesFrag
            R.id.stakesFragment -> stakesFrag
            R.id.participantsFragment -> participantsFrag
            R.id.summaryFragment -> summaryFrag
            else -> infoFrag
        }
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.challenge_navHostFrag, fragment)
            commit()
        }
    }

    private fun updateInfo(fragId: Int) {
        vm.currentFragId = fragId
        setStepInfo(fragId)
        vm.getNextId(fragId)
        binding.btnNext.text =
            if (fragId == vm.idList.last()) "Create the challenge!" else "Next"
    }

    private fun quitAlert() {
        val builder = AlertDialog.Builder(this, R.style.AlertDialog)
        with(builder)
        {
            setTitle("Are you sure you want to leave?")
            setMessage(
                "If you leave, this new challenge will be cancelled.\n" +
                        "You can use the bottom icons to navigate the challenge creation steps."
            )
            setNegativeButton("Leave") { _, _ ->
                finish()
            }
            setPositiveButton("Stay") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    private fun setStepInfo(fragId: Int) {
        val info = vm.getStepInfo(fragId)
        binding.txtStep.text = info.first
        binding.txtStepDesc.text = info.second
    }

    private fun handleSubmit() {

    }
}

class CreateChallengeViewModel(application: Application) : AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext
    var type: ChallengeType? = null
    var currentFragId = R.id.infoFragment
    var nextStepId: Int = R.id.rulesFragment
    val idList = listOf(
        R.id.infoFragment,
        R.id.rulesFragment,
        R.id.stakesFragment,
        R.id.participantsFragment,
        R.id.summaryFragment
    )

    fun getBackground() = when (type) {
        ChallengeType.SPEED -> ContextCompat.getDrawable(context, R.drawable.rounded_red)
        ChallengeType.COMPLETION -> ContextCompat.getDrawable(context, R.drawable.rounded_green)
        ChallengeType.HIGHEST -> ContextCompat.getDrawable(context, R.drawable.rounded_blue)
        else -> ContextCompat.getDrawable(context, R.drawable.rounded_main)
    }

    fun getColor() = when (type) {
        ChallengeType.SPEED -> ContextCompat.getColor(context, R.color.speed)
        ChallengeType.COMPLETION -> ContextCompat.getColor(context, R.color.completion)
        ChallengeType.HIGHEST -> ContextCompat.getColor(context, R.color.highest)
        else -> ContextCompat.getColor(context, R.color.main)
    }

    fun getNextId(fragId: Int) {
        val currentIndex = idList.indexOf(fragId)
        if (currentIndex != idList.lastIndex) {
            nextStepId = idList[currentIndex + 1]
        }
    }

    fun getStepInfo(fragId: Int) = when (fragId) {
        idList[0] -> Pair(
            "General Information",
            "Let people know what the challenge is all about"
        )
        idList[1] -> Pair("Rules", "Set the rules of the competition")
        idList[2] -> Pair("Stakes", "Spice up the challenge by defining the stakes")
        idList[3] -> Pair(
            "Participants", "Add participants and define their role.\n" +
                    "You can also add them later."
        )
        idList[4] -> Pair("Summary", "Review your challenge and create it!")

        else -> Pair("Step", "Description")
    }
}

enum class ChallengeType(val text: String) : Serializable {
    SPEED("Speed contest"), COMPLETION("Completion"), HIGHEST("Highest score")
}
