package com.example.fitorquit

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.kittinunf.fuel.httpGet

class SharedViewModel : ViewModel() {
    private val TAG = "sharedVM"
    private val repo = FirebaseRepo
    val currentUid = repo.currentUser!!.uid
    var challenge = Challenge(creator = repo.currentUser?.uid)
    val uri: MutableLiveData<Uri> = MutableLiveData()
    val videoUri: MutableLiveData<Uri?> = MutableLiveData(null)
    val ytVideo: MutableLiveData<String> = MutableLiveData()
    val participants: MutableLiveData<List<User>> = MutableLiveData(listOf())
    val allUsers: MutableLiveData<List<User>> = MutableLiveData()
    val currentUser: MutableLiveData<User> = MutableLiveData()
    val profilePic: MutableLiveData<Uri> = MutableLiveData()
    val color: MutableLiveData<Int> = MutableLiveData()
    val type: MutableLiveData<ChallengeType> = MutableLiveData()
    var admins = mutableListOf<User>()

    init {
        getUsers()
        getCurrentUser()
    }

    fun getTypeLogo(challengeType: ChallengeType) = when(challengeType) {
        ChallengeType.SPEED -> R.drawable.ic_baseline_rocket_launch_24
        ChallengeType.COMPLETION -> R.drawable.ic_baseline_check_circle_24
        ChallengeType.HIGHEST -> R.drawable.ic_baseline_leaderboard_24
    }

    fun getColor(challengeType: ChallengeType) = when (challengeType) {
        ChallengeType.SPEED -> R.color.speed
        ChallengeType.COMPLETION -> R.color.completion
        ChallengeType.HIGHEST -> R.color.highest
    }

    private fun convertToId(url: String): String {
        val id: String = if (url.contains("you", true)) {
            if (url.contains("watch?v=")) {
                url.split("watch?v=").last()
            } else {
                url.split("/").last()
            }
        } else {
            url
        }
        println("id $id")
        return id
    }

    private fun getUsers() {
        repo.getUsers()
            .addOnSuccessListener {
                val usersFetched = it.toObjects(User::class.java)
                println(usersFetched)
                allUsers.value = usersFetched
            }
            .addOnFailureListener {
                Log.e(TAG, "getUsers: ", it)
            }
    }

    private fun getCurrentUser() {
        repo.getCurrentUser()
            .addOnSuccessListener {
                val current = it.toObject(User::class.java)
                println(current)
                current?.let { cu ->
                    participants.value = listOf(cu)
                    admins = mutableListOf(cu)
                    currentUser.value = current
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "getCurrentUser: ", it)
            }
    }

    fun getCurrentPic() {
        repo.getPic("images/profile/${currentUid}.jpg")
            .downloadUrl
            .addOnSuccessListener { uri ->
               profilePic.value = uri
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "getPic: ", e)
            }
    }

    fun addParticipant(user: User) {
        if (participants.value?.contains(user) == false) {
            val currentParticipants = participants.value
            participants.value = currentParticipants?.let {
                it + listOf(user)
            } ?: listOf(user)
        }
    }

    fun isValidId(input: String): Boolean {
        val id = convertToId(input)
        var isReal: Boolean? = null
        val request = "https://img.youtube.com/vi/$id/0.jpg".httpGet()
            .responseString() { request, response, result ->
                isReal = response.statusCode == 200
            }
        request.join()
        return if (isReal == true) {
            challenge.youtubeVideo = id
            ytVideo.value = id
            true
        } else {
            false
        }
    }
}