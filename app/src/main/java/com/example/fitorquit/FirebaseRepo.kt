package com.example.fitorquit

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object FirebaseRepo {
    private const val TAG = "FirebaseRepo"
    val currentUser get() = FirebaseAuth.getInstance().currentUser

    fun createUser(user: User): Task<Void> {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users")
        val docRef = userRef.document(uid)
        return docRef.set(user)
    }

    fun getCurrentUser(): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("users").document(currentUser!!.uid).get()
    }

    fun getUser(uid: String): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("users").document(uid).get()
    }

    fun checkUserDoc(): DocumentReference {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users")
        return userRef.document(currentUser!!.uid)
    }

    fun addPic(uri: Uri, path: String): UploadTask {
        val storage = FirebaseStorage.getInstance()
        val ref = storage.reference.child(path)
        return ref.putFile(uri)
    }

    fun getPic(path: String): StorageReference {
        val storage = FirebaseStorage.getInstance()
        return storage.reference.child(path)
    }

    fun checkUsername(username: String): Query {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users")
        return userRef.whereEqualTo("username", username)
    }

    fun getUsers(): Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("users").orderBy("username").get()
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }
}

data class User(
    @PropertyName("name") val name: String = "",
    @PropertyName("uid") val uid: String = "",
    @PropertyName("email") val email: String = "",
    @PropertyName("username") val username: String = "",
    @PropertyName("country") val country: String = "",
    @PropertyName("birthdate") val birthdate: String = ""
) : Serializable

data class Challenge(
    @PropertyName("creator") var creator: String? = null,
    @PropertyName("title") var title: String? = null,
    @Exclude var picUri: Uri? = null,
    @PropertyName("desc") var desc: String? = null,
    @PropertyName("category") var category: String? = null,
    @PropertyName("private") var private: Boolean = false,
    @PropertyName("amount") var amount: Int? = null,
    @PropertyName("unit") var unit: Int? = null,
    @PropertyName("timeLimit") var timeLimit: Int? = null,
    @PropertyName("comment") var comment: String? = null,
    @PropertyName("startDate") var startDate: String? = null,
    @PropertyName("startTime") var startTime: String = "00:00",
    @PropertyName("endDate") var endDate: String? = null,
    @PropertyName("endTime") var endTime: String = "23:59",
    @PropertyName("youtube") var youtubeVideo: String? = null,
    @PropertyName("hasVideo") var hasVideo: Boolean = false,
    @PropertyName("participants") var participants: List<String> = listOf(),
    @PropertyName("admins") var admins: List<String> = listOf(),
    @PropertyName("refereeing") var refereeing: String = "",
    @PropertyName("stakesType") var stakesType: String = "",
    @PropertyName("stakesDesc") var stakesDesc: String = "",
) : Serializable {
    fun toLocalDate(dateString: String): LocalDate =
        LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd.MM.yyyy"))

    fun toLocalTime(timeString: String): LocalTime =
        LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
}