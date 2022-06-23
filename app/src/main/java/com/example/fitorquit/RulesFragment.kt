package com.example.fitorquit

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitorquit.databinding.RulesFragmentBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


class RulesFragment(
    private val videoCameraLauncher: ActivityResultLauncher<Intent>,
    private val videoGalleryLauncher: ActivityResultLauncher<Intent>,
    private val scroll: ScrollView,
) : Fragment() {
    private lateinit var sharedVM: SharedViewModel
    private var _binding: RulesFragmentBinding? = null
    private val binding get() = _binding!!
    private val TAG = "infoFragment"
    private var startCal = Calendar.getInstance()
    private var endCal = Calendar.getInstance()
    private var startLD: LocalDate = LocalDate.now()
    private var endLD: LocalDate = LocalDate.now()
    private var first = true
    private var startLT = LocalTime.of(0, 0)
    private var endLT = LocalTime.of(23, 59)
    private var mediaControls: MediaController? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RulesFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedVM = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        binding.videoView.clipToOutline = true

        binding.edittxtAmount.addTextChangedListener { text ->
            text?.let {
                if (it.isNotBlank()) {
                    sharedVM.challenge.amount = it.toString().trim().toInt()
                }
            }
        }

        binding.edittxtUnit.addTextChangedListener { text ->
            text?.let {
                if (it.isNotBlank()) {
                    sharedVM.challenge.unit = it.toString().trim().toInt()
                }
            }
        }

        binding.edittxtTimeLimit.addTextChangedListener { text ->
            text?.let {
                val unit = binding.spinnerTimeUnit.selectedItem as String
                if (it.isNotBlank()) {
                    val input = it.toString().trim().toInt()
                    val inSeconds = if (unit == "seconds") input else input * 60
                    sharedVM.challenge.timeLimit = inSeconds
                }
            }
        }

        binding.edittextComment.addTextChangedListener { text ->
            text?.let {
                if (it.isNotBlank()) {
                    sharedVM.challenge.comment = it.toString().trim()
                }
            }
        }

        binding.txtStartDate.setOnClickListener {
            val dialog = getDatePickerDialog(startDateListener, startCal)
            dialog.show()
        }

        binding.txtEndDate.setOnClickListener {
            val dialog = getDatePickerDialog(endDateListener, endCal)
            dialog.datePicker.minDate = startCal.timeInMillis
            dialog.show()
        }

        binding.txtStartTime.setOnClickListener {
            if (binding.txtStartDate.text != "Select date") {
                val dialog = getTimePickerDialog(startTimeListener, startLT)
                dialog.show()
            }
        }

        binding.txtEndTime.setOnClickListener {
            if (binding.txtEndDate.text != "Select date") {
                val dialog = getTimePickerDialog(endTimeListener, endLT)
                dialog.show()
            }
        }

        binding.btnFromCamera.setOnClickListener {
            openCamera(30)
            hideYoutubeEdit()
        }
        binding.btnFromGallery.setOnClickListener {
            openGallery(30)
            hideYoutubeEdit()
        }
        binding.btnFromYoutube.setOnClickListener {
            showYoutubeEdit()
        }

        binding.btnAddYoutube.setOnClickListener {
            val input = binding.edittxtYoutubeLink.text.toString()
//            showYoutube(input)
            sharedVM.viewModelScope.launch {
                val isValid = sharedVM.isValidId(input)
                if (!isValid) {
                    Toast.makeText(activity, "Invalid video id", Toast.LENGTH_SHORT).show()
                }
            }
        }

        sharedVM.ytVideo.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.btnRemoveVid.visibility = View.VISIBLE
                showYoutube(it)
            }
        }

        binding.btnRemoveVid.setOnClickListener {
            binding.btnRemoveVid.visibility = View.GONE
            if (sharedVM.challenge.hasVideo) {
                removeVideo()
            } else {
                removeYoutube()

            }
        }

        sharedVM.videoUri.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                removeYoutube()
                binding.btnRemoveVid.visibility = View.VISIBLE
                binding.videoView.apply {
                    visibility = View.VISIBLE
                    setVideoURI(uri)
                    start()
                }
                sharedVM.viewModelScope.launch {
                    delay(500)
                    delayedScrollToBottom(10)
                    setMediaControls()
                }
                sharedVM.challenge.hasVideo = true
            } ?: run {
                binding.videoView.stopPlayback()
                binding.videoView.visibility = View.GONE
                sharedVM.challenge.hasVideo = false
            }
        }

    }

    private fun removeYoutube() {
        sharedVM.challenge.youtubeVideo = null
        sharedVM.ytVideo.value = null
        binding.layoutYoutube.removeAllViews()
    }

    private fun removeVideo() {
        sharedVM.videoUri.value = null
        sharedVM.challenge.hasVideo = false
    }

    private fun delayedScrollToBottom(delay: Long) {
        sharedVM.viewModelScope.launch {
            delay(delay)
            scroll.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun setMediaControls() {
        mediaControls = MediaController(requireContext())
        binding.videoView.setMediaController(mediaControls)
        mediaControls!!.setAnchorView(binding.videoView)
    }

    private fun showYoutubeEdit() {
        binding.edittxtYoutubeLink.visibility = View.VISIBLE
        binding.btnAddYoutube.visibility = View.VISIBLE
        delayedScrollToBottom(500)
    }

    private fun showYoutube(videoId: String) {
        removeVideo()
        binding.btnAddYoutube.visibility = View.VISIBLE
        sharedVM.challenge.youtubeVideo = videoId
        sharedVM.challenge.hasVideo = false
        val player = YouTubePlayerView(requireContext())
        binding.layoutYoutube.addView(player)
        lifecycle.addObserver(player)
        player.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.cueVideo(videoId, 0F)
            }
        })
        delayedScrollToBottom(500)
    }

    private fun hideYoutubeEdit() {
        binding.edittxtYoutubeLink.visibility = View.GONE
        binding.btnAddYoutube.visibility = View.GONE
    }

    private fun openCamera(limit: Int) {
        val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, limit)
        cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
        cameraIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 104857600L);
        videoCameraLauncher.launch(cameraIntent)
    }

    private fun openGallery(limit: Int) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "video/*"
        galleryIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, limit)
        galleryIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 104857600L);
        videoGalleryLauncher.launch(galleryIntent)
    }

    private fun getDatePickerDialog(listener: DatePickerDialog.OnDateSetListener, cal: Calendar) =
        DatePickerDialog(
            requireContext(),
            listener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

    private fun getTimePickerDialog(
        listener: TimePickerDialog.OnTimeSetListener,
        localTime: LocalTime
    ) =
        TimePickerDialog(
            requireContext(),
            listener,
            localTime.hour,
            localTime.minute,
            true
        )

    private fun updateDate(tv: TextView, year: Int, month: Int, day: Int) {
        when (tv) {
            binding.txtStartDate -> {
                startLD = LocalDate.of(year, month + 1, day)
                val dateString = startLD.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                tv.text = dateString
                sharedVM.challenge.startDate = dateString
                startCal.set(year, month, day)
            }
            binding.txtEndDate -> {
                endLD = LocalDate.of(year, month + 1, day)
                val dateString = endLD.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                tv.text = dateString
                sharedVM.challenge.endDate = dateString
                endCal.set(year, month, day)
            }
        }
    }

    private fun updateTime(tv: TextView, hour: Int, minute: Int) {
        when (tv) {
            binding.txtStartTime -> {
                startLT = LocalTime.of(hour, minute)
                val timeString = startLT.format(DateTimeFormatter.ofPattern("HH:mm"))
                tv.text = timeString
                sharedVM.challenge.startTime = timeString
            }
            binding.txtEndTime -> {
                endLT = LocalTime.of(hour, minute)
                val timeString = endLT.format(DateTimeFormatter.ofPattern("HH:mm"))
                tv.text = timeString
                sharedVM.challenge.endTime = timeString
            }
        }
    }

    private val startDateListener =
        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            updateDate(binding.txtStartDate, year, monthOfYear, dayOfMonth)
            val datePicked = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
            if (first || datePicked.isAfter(endLD)) {
                first = false
                val startTimeString = startLT.format(DateTimeFormatter.ofPattern("HH:mm"))
                binding.txtStartTime.text = startTimeString
                binding.txtStartTime.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.rounded_light)
                val endTimeString = endLT.format(DateTimeFormatter.ofPattern("HH:mm"))
                binding.txtEndTime.text = endTimeString
                binding.txtEndTime.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.rounded_light)
                updateDate(binding.txtEndDate, year, monthOfYear, dayOfMonth)
            }
        }

    private val endDateListener =
        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            updateDate(binding.txtEndDate, year, monthOfYear, dayOfMonth)
        }

    private val startTimeListener =
        TimePickerDialog.OnTimeSetListener { _, h, m ->
            updateTime(binding.txtStartTime, h, m)
            val sameDay = startLD == endLD
            val startAfterEnd = LocalTime.of(h, m).isAfter(endLT)
            if (sameDay && startAfterEnd) {
                updateTime(binding.txtEndTime, h, m)
            }
        }

    private val endTimeListener =
        TimePickerDialog.OnTimeSetListener { _, h, m ->
            val sameDay = startLD == endLD
            val endBeforeStart = LocalTime.of(h, m).isBefore(startLT)
            if (sameDay && endBeforeStart) {
                Toast.makeText(activity, "End time should be after start time", Toast.LENGTH_LONG)
                    .show()
            } else {
                updateTime(binding.txtEndTime, h, m)
            }
        }

}